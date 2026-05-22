package com.example.tarecruitment.application.dao;

import com.example.tarecruitment.application.model.Application;
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
 * ApplicationDao - Application data access object.
 *
 * Only responsible for reading/writing applications.csv and simple queries,
 * does not read request/session, nor decides role permissions.
 * CSV column order must be consistent with Application.toCsv/fromCsv.
 */
public class ApplicationDao {

    private static final String APPLICATION_DIR = StoragePaths.getApplicationsDir();
    private static final String APPLICATION_FILE = APPLICATION_DIR + File.separator + "applications.csv";
    private static final String CSV_HEADER = "applicationId,jobId,applicantId,applicantName,applicantEmail,jobTitle,courseCode,moId,moName,status,coverLetter,appliedAt,updatedAt,reviewedAt,progressStage,reviewStartedAt,interviewScheduledAt,finalDecisionAt";

    private static ApplicationDao instance;

    private ApplicationDao() {
        initDataDirectory();
    }

    public static synchronized ApplicationDao getInstance() {
        if (instance == null) {
            instance = new ApplicationDao();
        }
        return instance;
    }

    private void initDataDirectory() {
        File applicationDir = new File(APPLICATION_DIR);
        if (!applicationDir.exists()) {
            applicationDir.mkdirs();
        }
    }

    /**
     * Initialize application data file
     */
    private void initApplicationFile() {
        File appFile = new File(APPLICATION_FILE);
        if (!appFile.exists()) {
            try {
                File parentDir = appFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                appFile.createNewFile();
                try (FileWriter writer = new FileWriter(APPLICATION_FILE)) {
                    writer.write(CSV_HEADER + "\n");
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to create applications file", e);
            }
        }
    }

    /**
     * Read all applications
     */
    private List<Application> readAllApplications() {
        initApplicationFile();
        return readApplicationsFromFile(APPLICATION_FILE);
    }

    private List<Application> readApplicationsFromFile(String filePath) {
        List<Application> applications = new ArrayList<>();
        File appFile = new File(filePath);
        if (!appFile.exists()) {
            return applications;
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
                Application app = Application.fromCsv(line);
                if (app != null) {
                    applications.add(app);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read applications file", e);
        }

        return applications;
    }

    /**
     * Write all applications
     */
    private void writeAllApplications(List<Application> applications) {
        try {
            // Write to temporary file first, then atomically replace to reduce risk of corrupting entire CSV when write is interrupted.
            Path targetPath = Path.of(APPLICATION_FILE);
            Path parent = targetPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tempFile = Files.createTempFile(parent, "applications", ".csv.tmp");
            try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile.toFile()))) {
                writer.println(CSV_HEADER);
                for (Application app : applications) {
                    writer.println(app.toCsv());
                }
            }
            Files.move(tempFile, targetPath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            // Some filesystems don't support ATOMIC_MOVE, fallback to normal overwrite; still keep temporary file write first.
            try {
                Path targetPath = Path.of(APPLICATION_FILE);
                Path parent = targetPath.getParent();
                Path tempFile = Files.createTempFile(parent, "applications", ".csv.tmp");
                try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile.toFile()))) {
                    writer.println(CSV_HEADER);
                    for (Application app : applications) {
                        writer.println(app.toCsv());
                    }
                }
                Files.move(tempFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException inner) {
                throw new RuntimeException("Failed to write applications file", inner);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write applications file", e);
        }
    }

    /**
     * Find application by ID
     */
    public Optional<Application> findById(String applicationId) {
        return readAllApplications().stream()
                .filter(a -> a.getApplicationId().equals(applicationId))
                .findFirst();
    }

    /**
     * Find all applications by job ID
     */
    public List<Application> findByJobId(String jobId) {
        return readAllApplications().stream()
                .filter(a -> a.getJobId().equals(jobId))
                .collect(Collectors.toList());
    }

    /**
     * Find all applications by applicant ID
     */
    public List<Application> findByApplicantId(String applicantId) {
        return readAllApplications().stream()
                .filter(a -> a.getApplicantId().equals(applicantId))
                .collect(Collectors.toList());
    }

    /**
     * Find all applications by MO ID (MO views who applied for their jobs)
     */
    public List<Application> findByMoId(String moId) {
        return readAllApplications().stream()
                .filter(a -> a.getMoId().equals(moId))
                .collect(Collectors.toList());
    }

    /**
     * Find applications by status
     */
    public List<Application> findByStatus(Application.Status status) {
        return readAllApplications().stream()
                .filter(a -> a.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Find application by job ID and applicant ID (check if already applied)
     */
    public Optional<Application> findByJobIdAndApplicantId(String jobId, String applicantId) {
        return readAllApplications().stream()
                .filter(a -> a.getJobId().equals(jobId) && a.getApplicantId().equals(applicantId))
                .findFirst();
    }

    /**
     * Check if applicant has already applied for a job
     */
    public boolean hasApplied(String jobId, String applicantId) {
        return findByJobIdAndApplicantId(jobId, applicantId).isPresent();
    }

    /**
     * Save application (create or update)
     */
    public Application save(Application application) {
        List<Application> applications = readAllApplications();

        Optional<Application> existingApp = applications.stream()
                .filter(a -> a.getApplicationId().equals(application.getApplicationId()))
                .findFirst();

        if (existingApp.isPresent()) {
            applications.remove(existingApp.get());
        }

        application.setUpdatedAt(LocalDateTime.now());
        applications.add(application);
        writeAllApplications(applications);

        return application;
    }

    /**
     * Create new application
     */
    public Application create(Application application) {
        return save(application);
    }

    /**
     * Update application
     */
    public Application update(Application application) {
        List<Application> applications = readAllApplications();

        boolean found = applications.stream()
                .anyMatch(a -> a.getApplicationId().equals(application.getApplicationId()));

        if (!found) {
            throw new IllegalArgumentException("Application not found: " + application.getApplicationId());
        }

        return save(application);
    }

    /**
     * Delete application
     */
    public boolean delete(String applicationId) {
        List<Application> applications = readAllApplications();
        boolean removed = applications.removeIf(a -> a.getApplicationId().equals(applicationId));

        if (removed) {
            writeAllApplications(applications);
        }

        return removed;
    }

    /**
     * Get all applications
     */
    public List<Application> findAll() {
        return new ArrayList<>(readAllApplications());
    }

    /**
     * Update application status
     */
    public boolean updateStatus(String applicationId, Application.Status status) {
        Optional<Application> appOpt = findById(applicationId);
        if (appOpt.isPresent()) {
            Application app = appOpt.get();
            app.setStatus(status);
            app.setUpdatedAt(LocalDateTime.now());
            app.setReviewedAt(LocalDateTime.now());
            if (status != Application.Status.PENDING) {
                app.setProgressStage(Application.ProgressStage.COMPLETED);
                if (app.getFinalDecisionAt() == null) {
                    app.setFinalDecisionAt(LocalDateTime.now());
                }
            }
            save(app);
            return true;
        }
        return false;
    }

    /**
     * Accept application
     */
    public boolean accept(String applicationId) {
        return updateStatus(applicationId, Application.Status.ACCEPTED);
    }

    /**
     * Reject application
     */
    public boolean reject(String applicationId) {
        return updateStatus(applicationId, Application.Status.REJECTED);
    }

    /**
     * Withdraw application
     */
    public boolean withdraw(String applicationId) {
        return updateStatus(applicationId, Application.Status.WITHDRAWN);
    }

    /**
     * Get application count
     */
    public long count() {
        return readAllApplications().size();
    }

    /**
     * Get application count by job ID
     */
    public long countByJobId(String jobId) {
        return readAllApplications().stream()
                .filter(a -> a.getJobId().equals(jobId))
                .count();
    }

    /**
     * Get application count by applicant ID
     */
    public long countByApplicantId(String applicantId) {
        return readAllApplications().stream()
                .filter(a -> a.getApplicantId().equals(applicantId))
                .count();
    }

    /**
     * Get application count by status
     */
    public long countByStatus(Application.Status status) {
        return readAllApplications().stream()
                .filter(a -> a.getStatus() == status)
                .count();
    }

    /**
     * Get the count of accepted applications for the specified job
     */
    public long countAcceptedByJobId(String jobId) {
        return readAllApplications().stream()
                .filter(a -> a.getJobId().equals(jobId) && a.getStatus() == Application.Status.ACCEPTED)
                .count();
    }

    /**
     * Delete all applications (for testing only)
     */
    public void deleteAll() {
        // Only used for test/demo data reset; production pages have no entry to clear applications.
        writeAllApplications(new ArrayList<>());
    }

    /**
     * Batch create applications (for testing initialization only)
     */
    public void batchCreate(List<Application> applications) {
        // Only used by DemoDataSeeder/test initialization; frontend does not batch create applications.
        List<Application> existingApps = readAllApplications();
        existingApps.addAll(applications);
        writeAllApplications(existingApps);
    }

    /**
     * Get pending application count for a job
     */
    public long countPendingByJobId(String jobId) {
        return readAllApplications().stream()
                .filter(a -> a.getJobId().equals(jobId) && a.getStatus() == Application.Status.PENDING)
                .count();
    }

    /**
     * Find applications by course code
     */
    public List<Application> findByCourseCode(String courseCode) {
        return readAllApplications().stream()
                .filter(a -> a.getCourseCode().equalsIgnoreCase(courseCode))
                .collect(Collectors.toList());
    }
}
