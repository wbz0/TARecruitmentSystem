package com.example.authlogin.dao;

import com.example.authlogin.model.Application;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ApplicationDao - 申请数据访问对象
 * 使用CSV文件存储申请数据
 */
public class ApplicationDao {

    // 项目数据目录 - 使用固定绝对路径
    private static final String DATA_DIR = "D:/HuaweiMoveData/Users/Carne/Desktop/SoftwareEngineering/data";
    private static final String APPLICATION_FILE = DATA_DIR + File.separator + "applications.csv";
    private static final String CSV_HEADER = "applicationId,jobId,applicantId,applicantName,applicantEmail,jobTitle,courseCode,moId,moName,status,coverLetter,appliedAt,updatedAt,reviewedAt";

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
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    /**
     * 初始化申请数据文件
     */
    private void initApplicationFile() {
        File appFile = new File(APPLICATION_FILE);
        if (!appFile.exists()) {
            try {
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
     * 读取所有申请
     */
    private List<Application> readAllApplications() {
        initApplicationFile();
        List<Application> applications = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(APPLICATION_FILE))) {
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
     * 写入所有申请
     */
    private void writeAllApplications(List<Application> applications) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(APPLICATION_FILE))) {
            writer.println(CSV_HEADER);
            for (Application app : applications) {
                writer.println(app.toCsv());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write applications file", e);
        }
    }

    /**
     * 根据ID查找申请
     */
    public Optional<Application> findById(String applicationId) {
        return readAllApplications().stream()
                .filter(a -> a.getApplicationId().equals(applicationId))
                .findFirst();
    }

    /**
     * 根据职位ID查找所有申请
     */
    public List<Application> findByJobId(String jobId) {
        return readAllApplications().stream()
                .filter(a -> a.getJobId().equals(jobId))
                .collect(Collectors.toList());
    }

    /**
     * 根据申请人ID查找所有申请
     */
    public List<Application> findByApplicantId(String applicantId) {
        return readAllApplications().stream()
                .filter(a -> a.getApplicantId().equals(applicantId))
                .collect(Collectors.toList());
    }

    /**
     * 根据MO ID查找所有申请（MO查看哪些人申请了自己的职位）
     */
    public List<Application> findByMoId(String moId) {
        return readAllApplications().stream()
                .filter(a -> a.getMoId().equals(moId))
                .collect(Collectors.toList());
    }

    /**
     * 根据状态查找申请
     */
    public List<Application> findByStatus(Application.Status status) {
        return readAllApplications().stream()
                .filter(a -> a.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * 根据职位ID和申请人ID查找申请（检查是否已申请）
     */
    public Optional<Application> findByJobIdAndApplicantId(String jobId, String applicantId) {
        return readAllApplications().stream()
                .filter(a -> a.getJobId().equals(jobId) && a.getApplicantId().equals(applicantId))
                .findFirst();
    }

    /**
     * 检查申请人是否已申请某职位
     */
    public boolean hasApplied(String jobId, String applicantId) {
        return findByJobIdAndApplicantId(jobId, applicantId).isPresent();
    }

    /**
     * 保存申请（新建或更新）
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
     * 创建新申请
     */
    public Application create(Application application) {
        return save(application);
    }

    /**
     * 更新申请
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
     * 删除申请
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
     * 获取所有申请
     */
    public List<Application> findAll() {
        return new ArrayList<>(readAllApplications());
    }

    /**
     * 更新申请状态
     */
    public boolean updateStatus(String applicationId, Application.Status status) {
        Optional<Application> appOpt = findById(applicationId);
        if (appOpt.isPresent()) {
            Application app = appOpt.get();
            app.setStatus(status);
            app.setUpdatedAt(LocalDateTime.now());
            app.setReviewedAt(LocalDateTime.now());
            save(app);
            return true;
        }
        return false;
    }

    /**
     * 接受申请
     */
    public boolean accept(String applicationId) {
        return updateStatus(applicationId, Application.Status.ACCEPTED);
    }

    /**
     * 拒绝申请
     */
    public boolean reject(String applicationId) {
        return updateStatus(applicationId, Application.Status.REJECTED);
    }

    /**
     * 撤回申请
     */
    public boolean withdraw(String applicationId) {
        return updateStatus(applicationId, Application.Status.WITHDRAWN);
    }

    /**
     * 获取申请数量
     */
    public long count() {
        return readAllApplications().size();
    }

    /**
     * 根据职位ID获取申请数量
     */
    public long countByJobId(String jobId) {
        return readAllApplications().stream()
                .filter(a -> a.getJobId().equals(jobId))
                .count();
    }

    /**
     * 根据申请人ID获取申请数量
     */
    public long countByApplicantId(String applicantId) {
        return readAllApplications().stream()
                .filter(a -> a.getApplicantId().equals(applicantId))
                .count();
    }

    /**
     * 根据状态获取申请数量
     */
    public long countByStatus(Application.Status status) {
        return readAllApplications().stream()
                .filter(a -> a.getStatus() == status)
                .count();
    }

    /**
     * 清空所有申请（仅用于测试）
     */
    public void deleteAll() {
        writeAllApplications(new ArrayList<>());
    }

    /**
     * 批量创建申请（仅用于测试初始化）
     */
    public void batchCreate(List<Application> applications) {
        List<Application> existingApps = readAllApplications();
        existingApps.addAll(applications);
        writeAllApplications(existingApps);
    }

    /**
     * 获取职位的待审核申请数量
     */
    public long countPendingByJobId(String jobId) {
        return readAllApplications().stream()
                .filter(a -> a.getJobId().equals(jobId) && a.getStatus() == Application.Status.PENDING)
                .count();
    }

    /**
     * 根据课程代码查找申请
     */
    public List<Application> findByCourseCode(String courseCode) {
        return readAllApplications().stream()
                .filter(a -> a.getCourseCode().equalsIgnoreCase(courseCode))
                .collect(Collectors.toList());
    }
}
