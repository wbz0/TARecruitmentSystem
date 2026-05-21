package com.example.tarecruitment.profile.dao;

import com.example.tarecruitment.profile.model.Applicant;
import com.example.tarecruitment.common.storage.StoragePaths;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ApplicantDao - TA applicant profile data access object.
 *
 * Only responsible for applicants.csv read/write and simple queries, does not handle HTTP, session, file upload or permission check.
 * Business flow in ApplicantProfileService, file saving managed by ProfileAssetService.
 */
public class ApplicantDao {

    private static final String APPLICANT_DIR = StoragePaths.getApplicantsDir();
    private static final String APPLICANT_FILE = APPLICANT_DIR + File.separator + "applicants.csv";
    private static final String CSV_HEADER = "applicantId,userId,fullName,studentId,department,program,gpa,skills,resumePath,photoPath,phone,address,experience,motivation,createdAt,updatedAt";

    private static ApplicantDao instance;

    private ApplicantDao() {
        initDataDirectory();
    }

    public static synchronized ApplicantDao getInstance() {
        if (instance == null) {
            instance = new ApplicantDao();
        }
        return instance;
    }

    private void initDataDirectory() {
        File applicantDir = new File(APPLICANT_DIR);
        if (!applicantDir.exists()) {
            applicantDir.mkdirs();
        }
    }

    /**
     * Initialize applicant data file; field order must be consistent with Applicant.toCsv()/fromCsv().
     */
    private void initApplicantFile() {
        File applicantFile = new File(APPLICANT_FILE);
        if (!applicantFile.exists()) {
            try {
                File parentDir = applicantFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                applicantFile.createNewFile();
                try (FileWriter writer = new FileWriter(APPLICANT_FILE)) {
                    writer.write(CSV_HEADER + "\n");
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to create applicants file", e);
            }
        }
    }

    /**
     * Read all applicants
     */
    private List<Applicant> readAllApplicants() {
        initApplicantFile();
        return readApplicantsFromFile(APPLICANT_FILE);
    }

    private List<Applicant> readApplicantsFromFile(String filePath) {
        List<Applicant> applicants = new ArrayList<>();
        File applicantFile = new File(filePath);
        if (!applicantFile.exists()) {
            return applicants;
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
                Applicant applicant = Applicant.fromCsv(line);
                if (applicant != null) {
                    applicants.add(applicant);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read applicants file", e);
        }

        return applicants;
    }

    /**
     * Write all applicants; use temp file atomic replace to avoid CSV half-write interruption.
     */
    private void writeAllApplicants(List<Applicant> applicants) {
        Path targetPath = Path.of(APPLICANT_FILE);
        Path tempPath = targetPath.resolveSibling(targetPath.getFileName() + ".tmp");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(tempPath))) {
            writer.println(CSV_HEADER);
            for (Applicant applicant : applicants) {
                writer.println(applicant.toCsv());
            }
            writer.flush();
            Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write applicants file", e);
        }
    }

    /**
     * Find applicant by ID
     */
    public Optional<Applicant> findById(String applicantId) {
        return readAllApplicants().stream()
                .filter(a -> a.getApplicantId().equals(applicantId))
                .findFirst();
    }

    /**
     * Find applicant by UserId
     */
    public Optional<Applicant> findByUserId(String userId) {
        return readAllApplicants().stream()
                .filter(a -> a.getUserId().equals(userId))
                .findFirst();
    }

    /**
     * Find applicant by student ID
     */
    public Optional<Applicant> findByStudentId(String studentId) {
        return readAllApplicants().stream()
                .filter(a -> a.getStudentId().equals(studentId))
                .findFirst();
    }

    /**
     * Check if student ID exists
     */
    public boolean existsByStudentId(String studentId) {
        return findByStudentId(studentId).isPresent();
    }

    /**
     * Check if user already has profile
     */
    public boolean existsByUserId(String userId) {
        return findByUserId(userId).isPresent();
    }

    /**
     * Save applicant (create or update)
     */
    public Applicant save(Applicant applicant) {
        List<Applicant> applicants = readAllApplicants();

        Optional<Applicant> existingApplicant = applicants.stream()
                .filter(a -> a.getApplicantId().equals(applicant.getApplicantId()))
                .findFirst();

        if (existingApplicant.isPresent()) {
            applicants.remove(existingApplicant.get());
        }

        applicant.setUpdatedAt(java.time.LocalDateTime.now());
        applicants.add(applicant);
        writeAllApplicants(applicants);

        return applicant;
    }

    /**
     * Create new applicant profile
     */
    public Applicant create(Applicant applicant) {
        if (existsByUserId(applicant.getUserId())) {
            throw new IllegalArgumentException("Applicant profile already exists for user: " + applicant.getUserId());
        }
        if (existsByStudentId(applicant.getStudentId())) {
            throw new IllegalArgumentException("Student ID already exists: " + applicant.getStudentId());
        }

        return save(applicant);
    }

    /**
     * Update applicant profile
     */
    public Applicant update(Applicant applicant) {
        List<Applicant> applicants = readAllApplicants();

        boolean found = applicants.stream()
                .anyMatch(a -> a.getApplicantId().equals(applicant.getApplicantId()));

        if (!found) {
            throw new IllegalArgumentException("Applicant not found: " + applicant.getApplicantId());
        }

        return save(applicant);
    }

    /**
     * Delete applicant
     */
    public boolean delete(String applicantId) {
        List<Applicant> applicants = readAllApplicants();
        boolean removed = applicants.removeIf(a -> a.getApplicantId().equals(applicantId));

        if (removed) {
            writeAllApplicants(applicants);
        }

        return removed;
    }

    /**
     * Get all applicants
     */
    public List<Applicant> findAll() {
        return new ArrayList<>(readAllApplicants());
    }

    /**
     * Get applicant count
     */
    public long count() {
        return readAllApplicants().size();
    }

    /**
     * Clear all applicants (only for test and demo data reset).
     */
    public void deleteAll() {
        writeAllApplicants(new ArrayList<>());
    }

    /**
     * Batch create applicants (only for test initialization and DemoDataSeeder).
     */
    public void batchCreate(List<Applicant> applicants) {
        List<Applicant> existingApplicants = readAllApplicants();
        existingApplicants.addAll(applicants);
        writeAllApplicants(existingApplicants);
    }
}
