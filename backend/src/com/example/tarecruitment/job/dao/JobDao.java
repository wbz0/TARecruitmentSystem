package com.example.tarecruitment.job.dao;

import com.example.tarecruitment.job.model.Job;
import com.example.tarecruitment.common.search.FuzzySearchUtil;
import com.example.tarecruitment.common.storage.StoragePaths;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JobDao - Job data access object.
 *
 * Only responsible for jobs.csv read/write, simple queries and search field extraction, does not read request/session or do permission checks.
 * JobService is responsible for business rules, JobResponseMapper is responsible for frontend payload.
 */
public class JobDao {

    private static final String JOB_DIR = StoragePaths.getJobsDir();
    private static final String JOB_FILE = JOB_DIR + File.separator + "jobs.csv";
    private static final String CSV_HEADER = "jobId,moId,moName,title,courseCode,courseName,description,requiredSkills,positions,workload,salary,deadline,status,createdAt,updatedAt,weeklyHours,workStartDate,workEndDate";

    private static JobDao instance;

    private JobDao() {
        initDataDirectory();
    }

    public static synchronized JobDao getInstance() {
        if (instance == null) {
            instance = new JobDao();
        }
        return instance;
    }

    private void initDataDirectory() {
        File jobDir = new File(JOB_DIR);
        if (!jobDir.exists()) {
            jobDir.mkdirs();
        }
    }

    /**
     * Initialize job data file; field order must be consistent with Job.toCsv()/fromCsv().
     */
    private void initJobFile() {
        File jobFile = new File(JOB_FILE);
        if (!jobFile.exists()) {
            try {
                File parentDir = jobFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                jobFile.createNewFile();
                try (FileWriter writer = new FileWriter(JOB_FILE)) {
                    writer.write(CSV_HEADER + "\n");
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to create jobs file", e);
            }
        }
    }

    /**
     * Read all jobs; when upper layer needs filtering, combine conditions in service layer.
     */
    private List<Job> readAllJobs() {
        initJobFile();
        return readJobsFromFile(JOB_FILE);
    }

    private List<Job> readJobsFromFile(String filePath) {
        List<Job> jobs = new ArrayList<>();
        File jobFile = new File(filePath);
        if (!jobFile.exists()) {
            return jobs;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                Job job = Job.fromCsv(line);
                if (job != null) {
                    jobs.add(job);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read jobs file", e);
        }

        return jobs;
    }

    /**
     * Write all jobs; write to temp file first then atomically replace, reducing risk of CSV corruption from half-written.
     */
    private void writeAllJobs(List<Job> jobs) {
        Path targetPath = Path.of(JOB_FILE);
        Path tempPath = Path.of(JOB_FILE + ".tmp");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(tempPath))) {
            writer.println(CSV_HEADER);
            for (Job job : jobs) {
                writer.println(job.toCsv());
            }
            writer.flush();
            Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ignored) {
                // best effort cleanup
            }
            throw new RuntimeException("Failed to write jobs file", e);
        }
    }

    /**
     * Find job by ID
     */
    public Optional<Job> findById(String jobId) {
        return readAllJobs().stream()
                .filter(j -> j.getJobId().equals(jobId))
                .findFirst();
    }

    /**
     * Find jobs by course code
     */
    public List<Job> findByCourseCode(String courseCode) {
        return readAllJobs().stream()
                .filter(j -> j.getCourseCode().equalsIgnoreCase(courseCode))
                .collect(Collectors.toList());
    }

    /**
     * Find jobs by MO ID
     */
    public List<Job> findByMoId(String moId) {
        return readAllJobs().stream()
                .filter(j -> j.getMoId().equals(moId))
                .collect(Collectors.toList());
    }

    /**
     * Find jobs by status
     */
    public List<Job> findByStatus(Job.Status status) {
        LocalDateTime now = LocalDateTime.now();
        return readAllJobs().stream()
                .filter(j -> j.getEffectiveStatus(now) == status)
                .collect(Collectors.toList());
    }

    /**
     * Get all open jobs
     */
    public List<Job> findOpenJobs() {
        return findByStatus(Job.Status.OPEN);
    }

    /**
     * Save job (create or update)
     */
    public Job save(Job job) {
        List<Job> jobs = readAllJobs();

        Optional<Job> existingJob = jobs.stream()
                .filter(j -> j.getJobId().equals(job.getJobId()))
                .findFirst();

        if (existingJob.isPresent()) {
            jobs.remove(existingJob.get());
        }

        job.setUpdatedAt(LocalDateTime.now());
        jobs.add(job);
        writeAllJobs(jobs);

        return job;
    }

    /**
     * Create new job
     */
    public Job create(Job job) {
        return save(job);
    }

    /**
     * Update job
     */
    public Job update(Job job) {
        List<Job> jobs = readAllJobs();

        boolean found = jobs.stream()
                .anyMatch(j -> j.getJobId().equals(job.getJobId()));

        if (!found) {
            throw new IllegalArgumentException("Job not found: " + job.getJobId());
        }

        return save(job);
    }

    /**
     * Delete job
     */
    public boolean delete(String jobId) {
        List<Job> jobs = readAllJobs();
        boolean removed = jobs.removeIf(j -> j.getJobId().equals(jobId));

        if (removed) {
            writeAllJobs(jobs);
        }

        return removed;
    }

    /**
     * Get all jobs
     */
    public List<Job> findAll() {
        return new ArrayList<>(readAllJobs());
    }

    /**
     * Update job status
     */
    public boolean updateStatus(String jobId, Job.Status status) {
        Optional<Job> jobOpt = findById(jobId);
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            job.setStatus(status);
            job.setUpdatedAt(LocalDateTime.now());
            save(job);
            return true;
        }
        return false;
    }

    /**
     * Get job count
     */
    public long count() {
        return readAllJobs().size();
    }

    /**
     * Get open job count.
     *
     * Uses effective status here, so OPEN positions past deadline are counted as not open.
     */
    public long countOpenJobs() {
        LocalDateTime now = LocalDateTime.now();
        return readAllJobs().stream()
                .filter(j -> j.getEffectiveStatus(now) == Job.Status.OPEN)
                .count();
    }

    /**
     * Clear all jobs (only for test and demo data reset).
     */
    public void deleteAll() {
        writeAllJobs(new ArrayList<>());
    }

    /**
     * Batch create jobs (only for test initialization and DemoDataSeeder).
     */
    public void batchCreate(List<Job> jobs) {
        List<Job> existingJobs = readAllJobs();
        existingJobs.addAll(jobs);
        writeAllJobs(existingJobs);
    }

    /**
     * Search jobs by keyword.
     *
     * Legacy/to be removed: external main flow usually goes through searchWithMetadata so frontend knows approximateOnly.
     * If confirmed no tests or old calls depend on list-returning search, can later converge to one search entry.
     */
    public List<Job> search(String keyword) {
        return searchWithMetadata(keyword, readAllJobs()).getItems();
    }

    /**
     * Execute unified fuzzy search on given candidate collection and return match metadata.
     */
    public FuzzySearchUtil.SearchOutcome<Job> searchWithMetadata(String keyword, List<Job> baseJobs) {
        List<Job> safeBase = baseJobs == null ? readAllJobs() : new ArrayList<>(baseJobs);
        return FuzzySearchUtil.search(safeBase, keyword, this::buildSearchFields);
    }

    private List<String> buildSearchFields(Job job) {
        List<String> fields = new ArrayList<>();
        if (job == null) {
            return fields;
        }
        fields.add(job.getTitle());
        fields.add(job.getCourseCode());
        fields.add(job.getCourseName());
        fields.add(job.getDescription());
        fields.add(job.getMoName());
        if (job.getRequiredSkills() != null && !job.getRequiredSkills().isEmpty()) {
            fields.add(String.join(" ", job.getRequiredSkills()));
        }
        return fields;
    }
}
