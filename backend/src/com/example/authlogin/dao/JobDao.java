package com.example.authlogin.dao;

import com.example.authlogin.model.Job;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JobDao - 职位数据访问对象
 * 使用CSV文件存储职位数据
 */
public class JobDao {

    // 项目数据目录 - 使用固定绝对路径
    private static final String DATA_DIR = "D:/HuaweiMoveData/Users/Carne/Desktop/SoftwareEngineering/data";
    private static final String JOB_FILE = DATA_DIR + File.separator + "jobs.csv";
    private static final String CSV_HEADER = "jobId,moId,moName,title,courseCode,courseName,description,requiredSkills,positions,workload,salary,deadline,status,createdAt,updatedAt";

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
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    /**
     * 初始化职位数据文件
     */
    private void initJobFile() {
        File jobFile = new File(JOB_FILE);
        if (!jobFile.exists()) {
            try {
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
     * 读取所有职位
     */
    private List<Job> readAllJobs() {
        initJobFile();
        List<Job> jobs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(JOB_FILE))) {
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
     * 写入所有职位
     */
    private void writeAllJobs(List<Job> jobs) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(JOB_FILE))) {
            writer.println(CSV_HEADER);
            for (Job job : jobs) {
                writer.println(job.toCsv());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write jobs file", e);
        }
    }

    /**
     * 根据ID查找职位
     */
    public Optional<Job> findById(String jobId) {
        return readAllJobs().stream()
                .filter(j -> j.getJobId().equals(jobId))
                .findFirst();
    }

    /**
     * 根据课程代码查找职位
     */
    public List<Job> findByCourseCode(String courseCode) {
        return readAllJobs().stream()
                .filter(j -> j.getCourseCode().equalsIgnoreCase(courseCode))
                .collect(Collectors.toList());
    }

    /**
     * 根据MO ID查找职位
     */
    public List<Job> findByMoId(String moId) {
        return readAllJobs().stream()
                .filter(j -> j.getMoId().equals(moId))
                .collect(Collectors.toList());
    }

    /**
     * 根据状态查找职位
     */
    public List<Job> findByStatus(Job.Status status) {
        return readAllJobs().stream()
                .filter(j -> j.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有开放职位
     */
    public List<Job> findOpenJobs() {
        return findByStatus(Job.Status.OPEN);
    }

    /**
     * 保存职位（新建或更新）
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
     * 创建新职位
     */
    public Job create(Job job) {
        return save(job);
    }

    /**
     * 更新职位
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
     * 删除职位
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
     * 获取所有职位
     */
    public List<Job> findAll() {
        return new ArrayList<>(readAllJobs());
    }

    /**
     * 更新职位状态
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
     * 获取职位数量
     */
    public long count() {
        return readAllJobs().size();
    }

    /**
     * 获取开放职位数量
     */
    public long countOpenJobs() {
        return readAllJobs().stream()
                .filter(j -> j.getStatus() == Job.Status.OPEN)
                .count();
    }

    /**
     * 清空所有职位（仅用于测试）
     */
    public void deleteAll() {
        writeAllJobs(new ArrayList<>());
    }

    /**
     * 批量创建职位（仅用于测试初始化）
     */
    public void batchCreate(List<Job> jobs) {
        List<Job> existingJobs = readAllJobs();
        existingJobs.addAll(jobs);
        writeAllJobs(existingJobs);
    }

    /**
     * 根据关键词搜索职位
     */
    public List<Job> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }

        String lowerKeyword = keyword.toLowerCase();
        return readAllJobs().stream()
                .filter(j -> j.getTitle().toLowerCase().contains(lowerKeyword) ||
                        j.getCourseCode().toLowerCase().contains(lowerKeyword) ||
                        (j.getCourseName() != null && j.getCourseName().toLowerCase().contains(lowerKeyword)) ||
                        (j.getDescription() != null && j.getDescription().toLowerCase().contains(lowerKeyword)))
                .collect(Collectors.toList());
    }
}
