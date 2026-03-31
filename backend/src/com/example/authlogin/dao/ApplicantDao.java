package com.example.authlogin.dao;

import com.example.authlogin.model.Applicant;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ApplicantDao - TA申请人档案数据访问对象
 * 使用CSV文件存储申请人数据
 */
public class ApplicantDao {

    // 项目数据目录 - 使用固定绝对路径
    private static final String DATA_DIR = "D:/HuaweiMoveData/Users/Carne/Desktop/SoftwareEngineering/data";
    private static final String APPLICANT_FILE = DATA_DIR + File.separator + "applicants.csv";
    private static final String CSV_HEADER = "applicantId,userId,fullName,studentId,department,program,gpa,skills,resumePath,phone,address,experience,motivation,createdAt,updatedAt";

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
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    /**
     * 初始化申请人数据文件
     */
    private void initApplicantFile() {
        File applicantFile = new File(APPLICANT_FILE);
        if (!applicantFile.exists()) {
            try {
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
        List<Applicant> applicants = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(APPLICANT_FILE))) {
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
     * 写入所有申请人
     */
    private void writeAllApplicants(List<Applicant> applicants) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(APPLICANT_FILE))) {
            writer.println(CSV_HEADER);
            for (Applicant applicant : applicants) {
                writer.println(applicant.toCsv());
            }
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
     * 根据院系查找申请人
     */
    public List<Applicant> findByDepartment(String department) {
        return readAllApplicants().stream()
                .filter(a -> a.getDepartment() != null && a.getDepartment().equalsIgnoreCase(department))
                .collect(Collectors.toList());
    }

    /**
     * 根据项目查找申请人
     */
    public List<Applicant> findByProgram(String program) {
        return readAllApplicants().stream()
                .filter(a -> a.getProgram() != null && a.getProgram().equalsIgnoreCase(program))
                .collect(Collectors.toList());
    }

    /**
     * 获取申请人数量
     */
    public long count() {
        return readAllApplicants().size();
    }

    /**
     * 清空所有申请人（仅用于测试）
     */
    public void deleteAll() {
        writeAllApplicants(new ArrayList<>());
    }

    /**
     * 批量创建申请人（仅用于测试初始化）
     */
    public void batchCreate(List<Applicant> applicants) {
        List<Applicant> existingApplicants = readAllApplicants();
        existingApplicants.addAll(applicants);
        writeAllApplicants(existingApplicants);
    }
}
