package com.example.authlogin;

import com.example.authlogin.dao.ApplicantDao;
import com.example.authlogin.model.Applicant;
import com.example.authlogin.model.User;
import com.example.authlogin.util.StoragePaths;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private static final String UPLOAD_DIR = StoragePaths.getResumeDir();

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

    private static final List<String> ALLOWED_PROGRAMS = Arrays.asList(
        "Undergraduate", "Master", "PhD"
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
            String fullName = normalizeInput(request.getParameter("fullName"));
            String studentId = normalizeInput(request.getParameter("studentId"));
            String department = normalizeInput(request.getParameter("department"));
            String program = normalizeInput(request.getParameter("program"));
            String gpa = normalizeInput(request.getParameter("gpa"));
            String skills = normalizeInput(request.getParameter("skills"));
            String phone = normalizeInput(request.getParameter("phone"));
            String address = normalizeInput(request.getParameter("address"));
            String experience = normalizeInput(request.getParameter("experience"));
            String motivation = normalizeInput(request.getParameter("motivation"));

            // 输入验证
            String error = validateInput(
                    fullName, studentId, department, program,
                    gpa, skills, phone, address, experience, motivation,
                    true
            );
            if (error != null) {
                logInfo("Validation failed: " + error);
                writeJsonResponse(response, 400, false, error, null);
                return;
            }

            Optional<Applicant> existingWithStudentId = applicantDao.findByStudentId(studentId);
            if (existingWithStudentId.isPresent()) {
                if (!isUpdate || existingApplicant.isEmpty()
                        || !existingWithStudentId.get().getApplicantId().equals(existingApplicant.get().getApplicantId())) {
                    writeJsonResponse(response, 400, false, "Student ID already exists", null);
                    return;
                }
            }

            Applicant applicant;
            if (isUpdate) {
                applicant = existingApplicant.get();
            } else {
                applicant = new Applicant();
                applicant.setUserId(currentUser.getUserId());
            }

            applicant.setFullName(fullName);
            applicant.setStudentId(studentId);
            applicant.setDepartment(department);
            applicant.setProgram(program);
            applicant.setGpa(gpa);
            applicant.setPhone(phone);
            applicant.setAddress(address);
            applicant.setExperience(experience);
            applicant.setMotivation(motivation);
            applicant.setSkills(parseSkills(skills));

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

            String updateValidationError = validatePartialInput(
                    fullName, studentId, department, program,
                    gpa, skills, phone, address, experience, motivation
            );
            if (updateValidationError != null) {
                logInfo("Partial update validation failed: " + updateValidationError);
                writeJsonResponse(response, 400, false, updateValidationError, null);
                return;
            }

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
            if (fullName != null) {
                applicant.setFullName(normalizeInput(fullName));
            }
            if (studentId != null) {
                String normalizedStudentId = normalizeInput(studentId);
                Optional<Applicant> existingWithStudentId = applicantDao.findByStudentId(normalizedStudentId);
                if (existingWithStudentId.isPresent() && !existingWithStudentId.get().getApplicantId().equals(applicant.getApplicantId())) {
                    writeJsonResponse(response, 400, false, "Student ID already exists", null);
                    return;
                }
                applicant.setStudentId(normalizedStudentId);
            }
            if (department != null) {
                applicant.setDepartment(normalizeInput(department));
            }
            if (program != null) {
                applicant.setProgram(normalizeInput(program));
            }
            if (gpa != null) {
                applicant.setGpa(normalizeInput(gpa));
            }
            if (phone != null) {
                applicant.setPhone(normalizeInput(phone));
            }
            if (address != null) {
                applicant.setAddress(normalizeInput(address));
            }
            if (experience != null) {
                applicant.setExperience(normalizeInput(experience));
            }
            if (motivation != null) {
                applicant.setMotivation(normalizeInput(motivation));
            }
            if (skills != null) {
                applicant.setSkills(parseSkills(normalizeInput(skills)));
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

    private String normalizeInput(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<String> parseSkills(String skills) {
        if (skills == null || skills.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(skills.split("[;,]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String validatePartialInput(
            String fullNameRaw,
            String studentIdRaw,
            String departmentRaw,
            String programRaw,
            String gpaRaw,
            String skillsRaw,
            String phoneRaw,
            String addressRaw,
            String experienceRaw,
            String motivationRaw
    ) {
        if (fullNameRaw != null) {
            String fullName = normalizeInput(fullNameRaw);
            if (fullName == null) {
                return "Full name cannot be empty.";
            }
            String fullNameError = validateFullName(fullName);
            if (fullNameError != null) {
                return fullNameError;
            }
        }

        if (studentIdRaw != null) {
            String studentId = normalizeInput(studentIdRaw);
            if (studentId == null) {
                return "Student ID cannot be empty.";
            }
            String studentIdError = validateStudentId(studentId);
            if (studentIdError != null) {
                return studentIdError;
            }
        }

        if (departmentRaw != null) {
            String department = normalizeInput(departmentRaw);
            if (department == null) {
                return "Department cannot be empty.";
            }
            String departmentError = validateDepartment(department);
            if (departmentError != null) {
                return departmentError;
            }
        }

        if (programRaw != null) {
            String program = normalizeInput(programRaw);
            if (program == null) {
                return "Program cannot be empty.";
            }
            String programError = validateProgram(program);
            if (programError != null) {
                return programError;
            }
        }

        String gpa = normalizeInput(gpaRaw);
        if (gpaRaw != null && gpa != null) {
            String gpaError = validateGpa(gpa);
            if (gpaError != null) {
                return gpaError;
            }
        }

        String skills = normalizeInput(skillsRaw);
        if (skillsRaw != null && skills != null) {
            String skillsError = validateSkills(skills);
            if (skillsError != null) {
                return skillsError;
            }
        }

        String phone = normalizeInput(phoneRaw);
        if (phoneRaw != null && phone != null) {
            String phoneError = validatePhone(phone);
            if (phoneError != null) {
                return phoneError;
            }
        }

        String address = normalizeInput(addressRaw);
        if (addressRaw != null && address != null) {
            String addressError = validateAddress(address);
            if (addressError != null) {
                return addressError;
            }
        }

        String experience = normalizeInput(experienceRaw);
        if (experienceRaw != null && experience != null) {
            String experienceError = validateLongTextField(experience, "Related experience");
            if (experienceError != null) {
                return experienceError;
            }
        }

        String motivation = normalizeInput(motivationRaw);
        if (motivationRaw != null && motivation != null) {
            String motivationError = validateLongTextField(motivation, "Motivation");
            if (motivationError != null) {
                return motivationError;
            }
        }

        return null;
    }

    private String validateInput(
            String fullName,
            String studentId,
            String department,
            String program,
            String gpa,
            String skills,
            String phone,
            String address,
            String experience,
            String motivation,
            boolean requireRequiredFields
    ) {
        if (requireRequiredFields && fullName == null) {
            return "Full name is required.";
        }
        if (fullName != null) {
            String fullNameError = validateFullName(fullName);
            if (fullNameError != null) {
                return fullNameError;
            }
        }

        if (requireRequiredFields && studentId == null) {
            return "Student ID is required.";
        }
        if (studentId != null) {
            String studentIdError = validateStudentId(studentId);
            if (studentIdError != null) {
                return studentIdError;
            }
        }

        if (requireRequiredFields && department == null) {
            return "Department is required.";
        }
        if (department != null) {
            String departmentError = validateDepartment(department);
            if (departmentError != null) {
                return departmentError;
            }
        }

        if (requireRequiredFields && program == null) {
            return "Program is required.";
        }
        if (program != null) {
            String programError = validateProgram(program);
            if (programError != null) {
                return programError;
            }
        }

        if (gpa != null) {
            String gpaError = validateGpa(gpa);
            if (gpaError != null) {
                return gpaError;
            }
        }
        if (skills != null) {
            String skillsError = validateSkills(skills);
            if (skillsError != null) {
                return skillsError;
            }
        }
        if (phone != null) {
            String phoneError = validatePhone(phone);
            if (phoneError != null) {
                return phoneError;
            }
        }
        if (address != null) {
            String addressError = validateAddress(address);
            if (addressError != null) {
                return addressError;
            }
        }
        if (experience != null) {
            String experienceError = validateLongTextField(experience, "Related experience");
            if (experienceError != null) {
                return experienceError;
            }
        }
        if (motivation != null) {
            String motivationError = validateLongTextField(motivation, "Motivation");
            if (motivationError != null) {
                return motivationError;
            }
        }

        return null;
    }

    private String validateFullName(String value) {
        if (value.length() < 2) {
            return "Full name must be at least 2 characters.";
        }
        if (value.length() > 100) {
            return "Full name must be 100 characters or fewer.";
        }
        if (!hasLetterOrCjk(value)) {
            return "Full name must include at least one letter.";
        }
        if (!value.matches("^[A-Za-z\\u00C0-\\u024F\\u4E00-\\u9FFF\\s.'-]+$")) {
            return "Full name contains unsupported characters.";
        }
        if (hasExcessiveRepeatedChars(value, 4)) {
            return "Full name contains too many repeated characters.";
        }
        return null;
    }

    private String validateStudentId(String value) {
        if (!value.matches("^\\d{10}$")) {
            return "Student ID must be exactly 10 digits, for example 2023213039.";
        }
        if (!value.matches("^20\\d{8}$")) {
            return "Student ID should start with 20, for example 2023213051.";
        }
        int year = Integer.parseInt(value.substring(0, 4));
        if (year < 2010 || year > 2099) {
            return "Student ID year appears invalid. Please check the first 4 digits.";
        }
        if (value.matches("^(\\d)\\1{9}$")) {
            return "Student ID appears invalid. Please check your official 10-digit student number.";
        }
        return null;
    }

    private String validateDepartment(String value) {
        if (value.length() < 2) {
            return "Department must be at least 2 characters.";
        }
        if (value.length() > 100) {
            return "Department must be 100 characters or fewer.";
        }
        if (!hasLetterOrCjk(value)) {
            return "Department should include letters.";
        }
        if (!value.matches("^[A-Za-z0-9\\u00C0-\\u024F\\u4E00-\\u9FFF\\s&(),./'-]+$")) {
            return "Department contains unsupported characters.";
        }
        if (hasExcessiveRepeatedChars(value, 6)) {
            return "Department contains too many repeated characters.";
        }
        return null;
    }

    private String validateProgram(String value) {
        if (!ALLOWED_PROGRAMS.contains(value)) {
            return "Please select a valid program option.";
        }
        return null;
    }

    private String validateGpa(String value) {
        if (value.length() > 20) {
            return "GPA must be 20 characters or fewer.";
        }
        if (!value.matches("^[0-9.,/\\s]+$")) {
            return "GPA may only include digits, spaces, decimal separators, and '/'.";
        }

        String normalized = value.replaceAll("\\s+", "").replace(",", ".");
        String[] parts = normalized.split("/", -1);
        if (parts.length > 2) {
            return "GPA format is invalid. Use one optional '/'.";
        }
        if (!parts[0].matches("^\\d{1,3}(\\.\\d{1,2})?$")) {
            return "GPA value supports up to 2 decimal places.";
        }

        double actual = Double.parseDouble(parts[0]);
        if (actual < 0) {
            return "GPA cannot be negative.";
        }

        if (parts.length == 2) {
            if (!parts[1].matches("^\\d{1,3}(\\.\\d{1,2})?$")) {
                return "GPA scale supports up to 2 decimal places.";
            }
            double scale = Double.parseDouble(parts[1]);
            if (scale < 4 || scale > 100) {
                return "GPA scale should be between 4 and 100.";
            }
            if (actual > scale) {
                return "GPA value cannot be greater than the GPA scale.";
            }
        } else if (actual > 4.3) {
            return "For GPA above 4.3, please include scale (for example 85/100).";
        }

        return null;
    }

    private String validateSkills(String value) {
        if (value.length() > 300) {
            return "Skills must be 300 characters or fewer.";
        }
        if (value.matches("(^[;,].*|.*[;,]\\s*[;,].*|.*[;,]\\s*$)")) {
            return "Please remove empty skill items between separators.";
        }

        List<String> items = parseSkills(value);
        if (items.size() > 12) {
            return "Please list up to 12 skills.";
        }

        Set<String> seen = new HashSet<>();
        for (String skill : items) {
            if (skill.length() < 2 || skill.length() > 40) {
                return "Each skill should be 2 to 40 characters.";
            }
            if (!hasLetterOrCjk(skill)) {
                return "Each skill should include letters.";
            }
            if (!skill.matches("^[A-Za-z0-9\\u00C0-\\u024F\\u4E00-\\u9FFF+#&./\\-\\s]+$")) {
                return "Skills contain unsupported characters.";
            }
            if (hasExcessiveRepeatedChars(skill, 5)) {
                return "A skill item has too many repeated characters.";
            }
            String normalized = skill.toLowerCase().replaceAll("\\s+", " ").trim();
            if (!seen.add(normalized)) {
                return "Duplicate skills found. Please keep each skill only once.";
            }
        }

        return null;
    }

    private String validatePhone(String value) {
        if (value.length() > 30) {
            return "Phone number must be 30 characters or fewer.";
        }
        if (!value.matches("^[\\d+\\-()./\\s]+$")) {
            return "Phone number may only include digits, spaces, and + - ( ) . /.";
        }
        if (countOccurrences(value, '+') > 1) {
            return "Phone number can contain only one '+'.";
        }
        if (value.indexOf('+') > 0) {
            return "If used, '+' must be at the beginning.";
        }
        if (!hasBalancedParentheses(value)) {
            return "Phone number parentheses are not balanced.";
        }

        String digits = value.replaceAll("\\D", "");
        if (digits.length() < 8 || digits.length() > 15) {
            return "Phone number should contain 8 to 15 digits.";
        }
        if (digits.matches("^(\\d)\\1+$")) {
            return "Phone number appears invalid. Please check repeated digits.";
        }
        if (value.startsWith("+") && digits.length() < 10) {
            return "International format should usually contain at least 10 digits.";
        }
        return null;
    }

    private String validateAddress(String value) {
        if (value.length() > 200) {
            return "Address must be 200 characters or fewer.";
        }
        if (value.length() < 5) {
            return "Address should be at least 5 characters if provided.";
        }
        if (!hasLetterOrCjk(value)) {
            return "Address should include letters.";
        }
        if (hasOnlyPunctuationAndSpace(value)) {
            return "Address cannot contain only punctuation.";
        }
        if (!value.matches("^[A-Za-z0-9\\u00C0-\\u024F\\u4E00-\\u9FFF\\s#&(),./:'-]+$")) {
            return "Address contains unsupported characters.";
        }
        if (hasExcessiveRepeatedChars(value, 8)) {
            return "Address contains too many repeated characters.";
        }
        return null;
    }

    private String validateLongTextField(String value, String label) {
        if (value.length() > 1200) {
            return label + " must be 1200 characters or fewer.";
        }
        if (value.length() < 20) {
            return label + " should be at least 20 characters if provided.";
        }
        if (getTextContentUnits(value) < 10) {
            return label + " should contain more detail (about 10 words/characters).";
        }
        if (hasExcessiveRepeatedChars(value, 8)) {
            return label + " contains too many repeated characters.";
        }
        return null;
    }

    private boolean hasLetterOrCjk(String value) {
        return value != null && value.matches(".*[A-Za-z\\u00C0-\\u024F\\u4E00-\\u9FFF].*");
    }

    private boolean hasOnlyPunctuationAndSpace(String value) {
        return value != null && !value.matches(".*[A-Za-z0-9\\u00C0-\\u024F\\u4E00-\\u9FFF].*");
    }

    private boolean hasExcessiveRepeatedChars(String value, int threshold) {
        if (value == null) {
            return false;
        }
        int safeThreshold = Math.max(1, threshold);
        return value.matches(".*(.)\\1{" + safeThreshold + ",}.*");
    }

    private boolean hasBalancedParentheses(String value) {
        int balance = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '(') {
                balance++;
            } else if (c == ')') {
                balance--;
                if (balance < 0) {
                    return false;
                }
            }
        }
        return balance == 0;
    }

    private int countOccurrences(String value, char target) {
        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == target) {
                count++;
            }
        }
        return count;
    }

    private int getTextContentUnits(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }

        int cjkChars = value.replaceAll("[^\\u4E00-\\u9FFF]", "").length();
        String latinPart = value.replaceAll("[\\u4E00-\\u9FFF]", " ");
        String[] tokens = latinPart.split("[^A-Za-z0-9'-]+");

        int latinWords = 0;
        for (String token : tokens) {
            if (!token.isEmpty()) {
                latinWords++;
            }
        }

        return cjkChars + latinWords;
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
