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
 * ApplicantDao - TA 申请人档案数据访问对象。
 *
 * 只负责 applicants.csv 的读写和简单查询，不处理 HTTP、session、文件上传或权限判断。
 * 业务流程在 ApplicantProfileService，文件保存由 ProfileAssetService 管理。
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
     * 初始化申请人数据文件；字段顺序要和 Applicant.toCsv()/fromCsv() 保持一致。
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
     * 读取所有申请人
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
     * 写入所有申请人；使用临时文件原子替换，避免 CSV 写一半被中断。
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
     * 根据ID查找申请人
     */
    public Optional<Applicant> findById(String applicantId) {
        return readAllApplicants().stream()
                .filter(a -> a.getApplicantId().equals(applicantId))
                .findFirst();
    }

    /**
     * 根据UserId查找申请人
     */
    public Optional<Applicant> findByUserId(String userId) {
        return readAllApplicants().stream()
                .filter(a -> a.getUserId().equals(userId))
                .findFirst();
    }

    /**
     * 根据学号查找申请人
     */
    public Optional<Applicant> findByStudentId(String studentId) {
        return readAllApplicants().stream()
                .filter(a -> a.getStudentId().equals(studentId))
                .findFirst();
    }

    /**
     * 检查学号是否存在
     */
    public boolean existsByStudentId(String studentId) {
        return findByStudentId(studentId).isPresent();
    }

    /**
     * 检查用户是否已有档案
     */
    public boolean existsByUserId(String userId) {
        return findByUserId(userId).isPresent();
    }

    /**
     * 保存申请人（新建或更新）
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
     * 创建新申请人档案
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
     * 更新申请人档案
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
     * 删除申请人
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
     * 获取所有申请人
     */
    public List<Applicant> findAll() {
        return new ArrayList<>(readAllApplicants());
    }

    /**
     * 获取申请人数量
     */
    public long count() {
        return readAllApplicants().size();
    }

    /**
     * 清空所有申请人（仅用于测试和 demo 数据重置）。
     */
    public void deleteAll() {
        writeAllApplicants(new ArrayList<>());
    }

    /**
     * 批量创建申请人（仅用于测试初始化和 DemoDataSeeder）。
     */
    public void batchCreate(List<Applicant> applicants) {
        List<Applicant> existingApplicants = readAllApplicants();
        existingApplicants.addAll(applicants);
        writeAllApplicants(existingApplicants);
    }
}
