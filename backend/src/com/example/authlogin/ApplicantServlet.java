package com.example.authlogin;

import com.example.authlogin.dao.ApplicantDao;
import com.example.authlogin.model.Applicant;
import com.example.authlogin.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ApplicantServlet - 处理TA申请人档案的创建和查询
 * 访问路径: /applicant
 *
 * 功能:
 * - POST /applicant - 创建新的申请人档案（支持文件上传）
 * - GET /applicant - 获取当前用户的档案
 * - PUT /applicant - 更新档案
 * - POST /applicant/upload - 上传简历文件
 */
@WebServlet("/applicant")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,       // 1 MB - 当文件超过此大小时写入磁盘
    maxFileSize = 1024 * 1024 * 10,       // 10 MB - 单个文件最大大小
    maxRequestSize = 1024 * 1024 * 15     // 15 MB - 整个请求最大大小
)
public class ApplicantServlet extends HttpServlet {

    private ApplicantDao applicantDao;

    // 上传目录
    private static final String UPLOAD_DIR = "D:/HuaweiMoveData/Users/Carne/Desktop/SoftwareEngineering/data/resumes";

    // 允许的文件类型
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    // 允许的扩展名
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".pdf", ".doc", ".docx"
    );

    // 文件大小限制 (10 MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // 简单的日志方法
    private void logInfo(String message) {
        System.out.println("[ApplicantServlet] " + message);
    }

    private void logError(String message, Throwable t) {
        System.err.println("[ApplicantServlet ERROR] " + message);
        if (t != null) {
            t.printStackTrace(System.err);
        }
    }

    @Override
    public void init() throws ServletException {
        applicantDao = ApplicantDao.getInstance();
        // 创建上传目录
        createUploadDirectory();
        logInfo("ApplicantServlet initialized");
    }

    private void createUploadDirectory() {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            // 获取当前登录用户
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                writeJsonResponse(response, 401, false, "Please login first", null);
                return;
            }

            // 查询用户的档案
            Optional<Applicant> applicantOpt = applicantDao.findByUserId(currentUser.getUserId());

            if (applicantOpt.isEmpty()) {
                writeJsonResponse(response, 404, false, "Applicant profile not found", null);
                return;
            }

            Applicant applicant = applicantOpt.get();
            String data = buildApplicantJson(applicant);
            writeJsonResponse(response, 200, true, "Applicant profile retrieved successfully", data);

        } catch (Exception e) {
            logError("Error retrieving applicant profile", e);
            writeJsonResponse(response, 500, false, "An error occurred. Please try again later.", null);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        // 检查是否是multipart请求（文件上传）
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {
            handleMultipartRequest(request, response);
            return;
        }

        // 普通表单请求
        handleFormRequest(request, response, false);
    }

    /**
     * 处理普通表单请求（创建档案）
     */
    private void handleFormRequest(HttpServletRequest request, HttpServletResponse response, boolean isUpdate)
            throws ServletException, IOException {
        try {
            // 获取当前登录用户
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                writeJsonResponse(response, 401, false, "Please login first", null);
                return;
            }

            Optional<Applicant> existingApplicant = applicantDao.findByUserId(currentUser.getUserId());

            if (isUpdate) {
                // 更新模式
                if (existingApplicant.isEmpty()) {
                    writeJsonResponse(response, 404, false, "Applicant profile not found. Please create one first.", null);
                    return;
                }
            } else {
                // 创建模式
                if (existingApplicant.isPresent()) {
                    writeJsonResponse(response, 409, false, "Applicant profile already exists. Use PUT to update.", null);
                    return;
                }
            }

            // 获取请求参数
            String fullName = request.getParameter("fullName");
            String studentId = request.getParameter("studentId");
            String department = request.getParameter("department");
            String program = request.getParameter("program");
            String gpa = request.getParameter("gpa");
            String skills = request.getParameter("skills");
            String phone = request.getParameter("phone");
            String address = request.getParameter("address");
            String experience = request.getParameter("experience");
            String motivation = request.getParameter("motivation");

            // 输入验证
            String error = validateInput(fullName, studentId, department, program);
            if (error != null) {
                logInfo("Validation failed: " + error);
                writeJsonResponse(response, 400, false, error, null);
                return;
            }

            Applicant applicant;
            if (isUpdate) {
                applicant = existingApplicant.get();
            } else {
                applicant = new Applicant();
                applicant.setUserId(currentUser.getUserId());
            }

            applicant.setFullName(fullName.trim());
            applicant.setStudentId(studentId.trim());
            applicant.setDepartment(department != null ? department.trim() : null);
            applicant.setProgram(program != null ? program.trim() : null);
            applicant.setGpa(gpa != null ? gpa.trim() : null);
            applicant.setPhone(phone != null ? phone.trim() : null);
            applicant.setAddress(address != null ? address.trim() : null);
            applicant.setExperience(experience != null ? experience.trim() : null);
            applicant.setMotivation(motivation != null ? motivation.trim() : null);

            // 处理技能列表
            if (skills != null && !skills.trim().isEmpty()) {
                List<String> skillList = Arrays.stream(skills.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                applicant.setSkills(skillList);
            }

            // 保存档案
            Applicant savedApplicant;
            if (isUpdate) {
                applicant.setUpdatedAt(LocalDateTime.now());
                savedApplicant = applicantDao.update(applicant);
                logInfo("Applicant profile updated successfully for user: " + currentUser.getUsername());
            } else {
                savedApplicant = applicantDao.create(applicant);
                logInfo("Applicant profile created successfully for user: " + currentUser.getUsername());
            }

            String data = "{\"applicantId\": \"" + savedApplicant.getApplicantId() + "\"}";
            int status = isUpdate ? 200 : 201;
            String message = isUpdate ? "Applicant profile updated successfully!" : "Applicant profile created successfully!";
            writeJsonResponse(response, status, true, message, data);

        } catch (IllegalArgumentException e) {
            logInfo("Profile operation failed: " + e.getMessage());
            writeJsonResponse(response, 400, false, e.getMessage(), null);
        } catch (Exception e) {
            logError("Unexpected error during profile operation", e);
            writeJsonResponse(response, 500, false, "An error occurred. Please try again later.", null);
        }
    }

    /**
     * 处理multipart请求（文件上传）
     */
    private void handleMultipartRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // 获取当前登录用户
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                writeJsonResponse(response, 401, false, "Please login first", null);
                return;
            }

            // 查询现有档案
            Optional<Applicant> applicantOpt = applicantDao.findByUserId(currentUser.getUserId());
            if (applicantOpt.isEmpty()) {
                writeJsonResponse(response, 404, false, "Applicant profile not found. Please create one first.", null);
                return;
            }

            Applicant applicant = applicantOpt.get();

            // 获取文本参数
            String fullName = request.getParameter("fullName");
            String studentId = request.getParameter("studentId");
            String department = request.getParameter("department");
            String program = request.getParameter("program");
            String gpa = request.getParameter("gpa");
            String skills = request.getParameter("skills");
            String phone = request.getParameter("phone");
            String address = request.getParameter("address");
            String experience = request.getParameter("experience");
            String motivation = request.getParameter("motivation");

            // 处理文件上传
            Part filePart = request.getPart("resume");
            String resumePath = null;

            if (filePart != null && filePart.getSize() > 0) {
                // 验证文件
                String fileError = validateFile(filePart);
                if (fileError != null) {
                    writeJsonResponse(response, 400, false, fileError, null);
                    return;
                }

                // 保存文件
                resumePath = saveFile(filePart, currentUser.getUserId());
                applicant.setResumePath(resumePath);
                logInfo("Resume uploaded successfully: " + resumePath);
            }

            // 更新文本字段（如果有提供）
            if (fullName != null && !fullName.trim().isEmpty()) {
                applicant.setFullName(fullName.trim());
            }
            if (studentId != null && !studentId.trim().isEmpty()) {
                Optional<Applicant> existingWithStudentId = applicantDao.findByStudentId(studentId.trim());
                if (existingWithStudentId.isPresent() && !existingWithStudentId.get().getApplicantId().equals(applicant.getApplicantId())) {
                    writeJsonResponse(response, 400, false, "Student ID already exists", null);
                    return;
                }
                applicant.setStudentId(studentId.trim());
            }
            if (department != null) {
                applicant.setDepartment(department.trim().isEmpty() ? null : department.trim());
            }
            if (program != null) {
                applicant.setProgram(program.trim().isEmpty() ? null : program.trim());
            }
            if (gpa != null) {
                applicant.setGpa(gpa.trim().isEmpty() ? null : gpa.trim());
            }
            if (phone != null) {
                applicant.setPhone(phone.trim().isEmpty() ? null : phone.trim());
            }
            if (address != null) {
                applicant.setAddress(address.trim().isEmpty() ? null : address.trim());
            }
            if (experience != null) {
                applicant.setExperience(experience.trim().isEmpty() ? null : experience.trim());
            }
            if (motivation != null) {
                applicant.setMotivation(motivation.trim().isEmpty() ? null : motivation.trim());
            }
            if (skills != null) {
                List<String> skillList = Arrays.stream(skills.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                applicant.setSkills(skillList);
            }

            // 更新时间
            applicant.setUpdatedAt(LocalDateTime.now());

            // 保存更新
            Applicant updatedApplicant = applicantDao.update(applicant);

            logInfo("Applicant profile updated with resume for user: " + currentUser.getUsername());

            String data = "{\"applicantId\": \"" + updatedApplicant.getApplicantId() + "\"";
            if (resumePath != null) {
                data += ", \"resumePath\": \"" + escapeJson(resumePath) + "\"";
            }
            data += "}";

            writeJsonResponse(response, 200, true, "Profile updated with resume!", data);

        } catch (IllegalArgumentException e) {
            logInfo("Resume upload failed: " + e.getMessage());
            writeJsonResponse(response, 400, false, e.getMessage(), null);
        } catch (ServletException e) {
            // 处理文件大小超限异常
            String message = e.getMessage();
            if (message != null && message.toLowerCase().contains("size")) {
                logInfo("File size exceeded: " + message);
                writeJsonResponse(response, 413, false, "File size exceeds the maximum limit of 10MB. Please upload a smaller file.", null);
            } else {
                logError("Servlet error during resume upload", e);
                writeJsonResponse(response, 400, false, "File upload failed. " + e.getMessage(), null);
            }
        } catch (Exception e) {
            logError("Unexpected error during resume upload", e);
            writeJsonResponse(response, 500, false, "An error occurred. Please try again later.", null);
        }
    }

    /**
     * 验证上传的文件
     */
    private String validateFile(Part filePart) {
        String contentType = filePart.getContentType();
        String fileName = extractFileName(filePart);

        // 检查文件类型
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return "Invalid file type. Only PDF, DOC, and DOCX files are allowed.";
        }

        // 检查文件扩展名
        String lowerFileName = fileName.toLowerCase();
        boolean hasValidExtension = ALLOWED_EXTENSIONS.stream()
                .anyMatch(lowerFileName::endsWith);
        if (!hasValidExtension) {
            return "Invalid file extension. Only PDF, DOC, and DOCX files are allowed.";
        }

        // 检查文件大小（已经在@MultipartConfig中配置，但额外检查一下）
        if (filePart.getSize() > MAX_FILE_SIZE) {
            return "File size exceeds 10MB limit.";
        }

        return null;
    }

    /**
     * 保存上传的文件
     */
    private String saveFile(Part filePart, String userId) throws IOException {
        String fileName = extractFileName(filePart);

        // 生成唯一文件名
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String newFileName = userId + "_" + System.currentTimeMillis() + extension;

        // 确保目录存在
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 保存文件
        File file = new File(uploadDir, newFileName);
        filePart.write(file.getAbsolutePath());

        // 返回相对路径
        return "resumes/" + newFileName;
    }

    /**
     * 从Part中提取文件名
     */
    private String extractFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition != null) {
            for (String token : contentDisposition.split(";")) {
                token = token.trim();
                if (token.startsWith("filename=")) {
                    String fileName = token.substring(9);
                    // 移除可能的引号
                    if (fileName.startsWith("\"") && fileName.endsWith("\"")) {
                        fileName = fileName.substring(1, fileName.length() - 1);
                    }
                    return fileName;
                }
            }
        }
        // 如果无法从header获取，使用原始文件名
        String submittedFileName = part.getSubmittedFileName();
        return submittedFileName != null ? submittedFileName : "resume";
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        // 检查是否是multipart请求
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {
            handleMultipartRequest(request, response);
            return;
        }

        handleFormRequest(request, response, true);
    }

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }

    /**
     * 验证必填字段
     */
    private String validateInput(String fullName, String studentId, String department, String program) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Full name is required";
        }
        if (fullName.trim().length() > 100) {
            return "Full name is too long";
        }

        if (studentId == null || studentId.trim().isEmpty()) {
            return "Student ID is required";
        }
        if (studentId.trim().length() > 50) {
            return "Student ID is too long";
        }

        if (department == null || department.trim().isEmpty()) {
            return "Department is required";
        }
        if (department.trim().length() > 100) {
            return "Department is too long";
        }

        if (program == null || program.trim().isEmpty()) {
            return "Program is required";
        }

        return null;
    }

    /**
     * 档案完整性验证结果
     */
    private static class CompletenessResult {
        int completeness;
        List<String> missingFields;

        CompletenessResult(int completeness, List<String> missingFields) {
            this.completeness = completeness;
            this.missingFields = missingFields;
        }
    }

    /**
     * 计算档案完整性
     */
    private CompletenessResult calculateCompleteness(Applicant applicant) {
        int totalFields = 12; // 总字段数
        int filledFields = 0;
        List<String> missingFields = new ArrayList<>();

        // 必填字段 (4个)
        if (isNotEmpty(applicant.getFullName())) {
            filledFields++;
        } else {
            missingFields.add("fullName");
        }
        if (isNotEmpty(applicant.getStudentId())) {
            filledFields++;
        } else {
            missingFields.add("studentId");
        }
        if (isNotEmpty(applicant.getDepartment())) {
            filledFields++;
        } else {
            missingFields.add("department");
        }
        if (isNotEmpty(applicant.getProgram())) {
            filledFields++;
        } else {
            missingFields.add("program");
        }

        // 选填字段 (8个)
        if (isNotEmpty(applicant.getGpa())) filledFields++;
        else missingFields.add("gpa");

        if (applicant.getSkills() != null && !applicant.getSkills().isEmpty()) filledFields++;
        else missingFields.add("skills");

        if (isNotEmpty(applicant.getResumePath())) filledFields++;
        else missingFields.add("resume");

        if (isNotEmpty(applicant.getPhone())) filledFields++;
        else missingFields.add("phone");

        if (isNotEmpty(applicant.getAddress())) filledFields++;
        else missingFields.add("address");

        if (isNotEmpty(applicant.getExperience())) filledFields++;
        else missingFields.add("experience");

        if (isNotEmpty(applicant.getMotivation())) filledFields++;
        else missingFields.add("motivation");

        int completeness = (int) Math.round((double) filledFields / totalFields * 100);
        return new CompletenessResult(completeness, missingFields);
    }

    /**
     * 检查字符串是否不为空
     */
    private boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 构建申请人档案JSON数据
     */
    private String buildApplicantJson(Applicant applicant) {
        // 计算完整性
        CompletenessResult completeness = calculateCompleteness(applicant);

        StringBuilder json = new StringBuilder();
        json.append("\"applicantId\": \"").append(escapeJson(applicant.getApplicantId())).append("\", ");
        json.append("\"userId\": \"").append(escapeJson(applicant.getUserId())).append("\", ");
        json.append("\"fullName\": \"").append(escapeJson(applicant.getFullName())).append("\", ");
        json.append("\"studentId\": \"").append(escapeJson(applicant.getStudentId())).append("\", ");
        json.append("\"department\": \"").append(escapeJson(applicant.getDepartment() != null ? applicant.getDepartment() : "")).append("\", ");
        json.append("\"program\": \"").append(escapeJson(applicant.getProgram() != null ? applicant.getProgram() : "")).append("\", ");
        json.append("\"gpa\": \"").append(escapeJson(applicant.getGpa() != null ? applicant.getGpa() : "")).append("\", ");
        json.append("\"skills\": \"").append(escapeJson(applicant.getSkillsAsString())).append("\", ");
        json.append("\"resumePath\": \"").append(escapeJson(applicant.getResumePath() != null ? applicant.getResumePath() : "")).append("\", ");
        json.append("\"phone\": \"").append(escapeJson(applicant.getPhone() != null ? applicant.getPhone() : "")).append("\", ");
        json.append("\"address\": \"").append(escapeJson(applicant.getAddress() != null ? applicant.getAddress() : "")).append("\", ");
        json.append("\"experience\": \"").append(escapeJson(applicant.getExperience() != null ? applicant.getExperience() : "")).append("\", ");
        json.append("\"motivation\": \"").append(escapeJson(applicant.getMotivation() != null ? applicant.getMotivation() : "")).append("\", ");

        // 添加完整性信息
        json.append("\"completeness\": ").append(completeness.completeness).append(", ");
        json.append("\"missingFields\": [");
        for (int i = 0; i < completeness.missingFields.size(); i++) {
            json.append("\"").append(completeness.missingFields.get(i)).append("\"");
            if (i < completeness.missingFields.size() - 1) {
                json.append(", ");
            }
        }
        json.append("]");
        return json.toString();
    }

    /**
     * 统一的JSON响应写入方法
     */
    private void writeJsonResponse(HttpServletResponse response, int status, boolean success, String message, String data)
            throws IOException {
        response.setStatus(status);
        PrintWriter out = response.getWriter();

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\": ").append(success).append(", ");
        json.append("\"message\": \"").append(escapeJson(message)).append("\"");

        if (data != null) {
            json.append(", ").append(data);
        }

        json.append("}");
        out.write(json.toString());
    }

    /**
     * JSON字符串转义
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
