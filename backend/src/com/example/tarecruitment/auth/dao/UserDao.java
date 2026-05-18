package com.example.tarecruitment.auth.dao;

import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.storage.StoragePaths;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UserDao - 用户数据访问对象。
 *
 * 用户按角色拆成 3 个 CSV 文件，便于演示数据查看和按角色加载。
 * DAO 只负责存储和账号唯一性，不处理 HTTP session 或页面跳转。
 */
public class UserDao {

    private static final String USER_DIR = StoragePaths.getUsersDir();
    private static final String USER_FILE_TA = USER_DIR + File.separator + "users_ta.csv";
    private static final String USER_FILE_MO = USER_DIR + File.separator + "users_mo.csv";
    private static final String USER_FILE_ADMIN = USER_DIR + File.separator + "users_admin.csv";
    private static final String CSV_HEADER = "userId,username,password,email,role,createdAt,lastLoginAt,displayName,realName,professionalTitle,avatarPath";
    private static final String DEFAULT_DEMO_PASSWORD = "Pass1234";
    private static final String DEFAULT_TA_DEMO_EMAIL = "ta_demo@local.test";
    private static final String DEFAULT_MO_DEMO_EMAIL = "mo_demo@local.test";
    private static final String DEFAULT_ADMIN_DEMO_EMAIL = "admin_demo@local.test";
    private static final int FILE_WRITE_RETRY_COUNT = 5;
    private static final long FILE_WRITE_RETRY_DELAY_MS = 100L;

    private static UserDao instance;

    private UserDao() {
        initDataDirectory();
        ensureDefaultDemoAccounts();
    }

    public static synchronized UserDao getInstance() {
        if (instance == null) {
            instance = new UserDao();
        }
        return instance;
    }

    private void initDataDirectory() {
        File userDir = new File(USER_DIR);
        if (!userDir.exists()) {
            userDir.mkdirs();
        }
        initUserFiles();
    }

    /**
     * 初始化用户数据文件
     */
    private void initUserFile(String filePath) {
        File userFile = new File(filePath);
        if (!userFile.exists()) {
            try {
                File parentDir = userFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                userFile.createNewFile();
                try (FileWriter writer = new FileWriter(filePath)) {
                    writer.write(CSV_HEADER + "\n");
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to create users file", e);
            }
        }
    }

    private void initUserFiles() {
        initUserFile(USER_FILE_TA);
        initUserFile(USER_FILE_MO);
        initUserFile(USER_FILE_ADMIN);
    }

    /**
     * 启动时补齐固定测试账号，但不覆盖已有本地数据。
     */
    public synchronized void ensureDefaultDemoAccounts() {
        // 演示账号语义是项目约定，不能被公开注册或数据初始化意外覆盖。
        ensureDefaultDemoAccount("ta_demo", DEFAULT_TA_DEMO_EMAIL, User.Role.TA);
        ensureDefaultDemoAccount("mo_demo", DEFAULT_MO_DEMO_EMAIL, User.Role.MO);
        ensureDefaultDemoAccount("admin_demo", DEFAULT_ADMIN_DEMO_EMAIL, User.Role.ADMIN);
    }

    private void ensureDefaultDemoAccount(String username, String preferredEmail, User.Role role) {
        if (findByEmail(preferredEmail).isPresent()) {
            return;
        }
        if (findByUsername(username).isPresent()) {
            return;
        }

        String email = resolveAvailableDemoEmail(username, preferredEmail);
        create(new User(username, DEFAULT_DEMO_PASSWORD, email, role));
    }

    private String resolveAvailableDemoEmail(String username, String preferredEmail) {
        if (!existsByEmail(preferredEmail)) {
            return preferredEmail;
        }

        int suffix = 1;
        while (true) {
            String candidateEmail = username + "+" + suffix + "@local.test";
            if (!existsByEmail(candidateEmail)) {
                return candidateEmail;
            }
            suffix++;
        }
    }

    /**
     * 读取所有用户
     */
    private List<User> readAllUsers() {
        initUserFiles();
        List<User> users = new ArrayList<>();

        users.addAll(readUsersForRole(User.Role.TA));
        users.addAll(readUsersForRole(User.Role.MO));
        users.addAll(readUsersForRole(User.Role.ADMIN));

        return users;
    }

    private List<User> readUsersFromFile(String filePath) {
        List<User> users = new ArrayList<>();
        File userFile = new File(filePath);
        if (!userFile.exists()) {
            return users;
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
                User user = User.fromCsv(line);
                if (user != null) {
                    users.add(user);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read users file", e);
        }

        return users;
    }

    private List<User> readUsersForRole(User.Role role) {
        return readUsersFromFile(getUserFileByRole(role));
    }

    /**
     * 写入所有用户
     */
    private void writeUsersToFile(String filePath, List<User> users) {
        // 分角色文件写入时先写临时文件，再替换目标文件，降低 CSV 写坏的风险。
        Path targetPath = Path.of(filePath);
        Path tempPath = targetPath.resolveSibling(targetPath.getFileName() + ".tmp");
        try {
            writeUserFile(tempPath.toFile(), users);
            IOException moveFailure = moveTempFileWithRetry(tempPath, targetPath);
            if (moveFailure != null) {
                IOException overwriteFailure = overwriteTargetFileWithRetry(targetPath, tempPath, users);
                if (overwriteFailure != null) {
                    overwriteFailure.addSuppressed(moveFailure);
                    throw overwriteFailure;
                }
            }
        } catch (IOException e) {
            deleteTempFileQuietly(tempPath);
            throw new RuntimeException("Failed to write users file", e);
        }
    }

    private void writeUserFile(File file, List<User> users) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println(CSV_HEADER);
            for (User user : users) {
                writer.println(user.toCsv());
            }
            writer.flush();
        }
    }

    private IOException moveTempFileWithRetry(Path tempPath, Path targetPath) throws IOException {
        IOException lastException = null;
        for (int attempt = 0; attempt < FILE_WRITE_RETRY_COUNT; attempt++) {
            try {
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                return null;
            } catch (IOException atomicMoveException) {
                lastException = atomicMoveException;
                try {
                    Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    return null;
                } catch (IOException replaceMoveException) {
                    lastException = replaceMoveException;
                }
            }

            waitBeforeRetry(attempt);
        }
        return lastException;
    }

    private IOException overwriteTargetFileWithRetry(Path targetPath, Path tempPath, List<User> users) throws IOException {
        IOException lastException = null;
        for (int attempt = 0; attempt < FILE_WRITE_RETRY_COUNT; attempt++) {
            try {
                writeUserFile(targetPath.toFile(), users);
                deleteTempFileQuietly(tempPath);
                return null;
            } catch (IOException overwriteException) {
                lastException = overwriteException;
            }

            waitBeforeRetry(attempt);
        }
        return lastException;
    }

    private void waitBeforeRetry(int attempt) throws IOException {
        if (attempt >= FILE_WRITE_RETRY_COUNT - 1) {
            return;
        }

        try {
            Thread.sleep(FILE_WRITE_RETRY_DELAY_MS * (attempt + 1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while retrying users file write", e);
        }
    }

    private void deleteTempFileQuietly(Path tempPath) {
        try {
            Files.deleteIfExists(tempPath);
        } catch (IOException ignored) {
        }
    }

    private String getUserFileByRole(User.Role role) {
        if (role == User.Role.TA) {
            return USER_FILE_TA;
        }
        if (role == User.Role.MO) {
            return USER_FILE_MO;
        }
        if (role == User.Role.ADMIN) {
            return USER_FILE_ADMIN;
        }
        throw new IllegalArgumentException("Unsupported role: " + role);
    }

    /**
     * 根据ID查找用户
     */
    public Optional<User> findById(String userId) {
        return readAllUsers().stream()
                .filter(u -> u.getUserId().equals(userId))
                .findFirst();
    }

    /**
     * 根据用户名查找用户
     */
    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        String normalized = username.trim().toLowerCase();
        return readAllUsers().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(normalized))
                .findFirst();
    }

    /**
     * 根据邮箱查找用户
     */
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        String normalized = email.trim();
        return readAllUsers().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(normalized))
                .findFirst();
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    /**
     * 检查邮箱是否存在
     */
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    /**
     * 保存用户（新建或更新）
     */
    public User save(User user) {
        initUserFiles();

        // 用户改角色时要从三个角色文件中都移除旧记录，再写入目标角色文件。
        String targetFile = getUserFileByRole(user.getRole());
        List<User> targetUsers = readUsersForRole(user.getRole());
        List<User> taUsers = readUsersForRole(User.Role.TA);
        List<User> moUsers = readUsersForRole(User.Role.MO);
        List<User> adminUsers = readUsersForRole(User.Role.ADMIN);

        taUsers.removeIf(u -> u.getUserId().equals(user.getUserId()));
        moUsers.removeIf(u -> u.getUserId().equals(user.getUserId()));
        adminUsers.removeIf(u -> u.getUserId().equals(user.getUserId()));
        targetUsers.removeIf(u -> u.getUserId().equals(user.getUserId()));
        targetUsers.add(user);

        if (USER_FILE_TA.equals(targetFile)) {
            taUsers = targetUsers;
        } else if (USER_FILE_MO.equals(targetFile)) {
            moUsers = targetUsers;
        } else {
            adminUsers = targetUsers;
        }

        writeUsersToFile(USER_FILE_TA, taUsers);
        writeUsersToFile(USER_FILE_MO, moUsers);
        writeUsersToFile(USER_FILE_ADMIN, adminUsers);

        return user;
    }

    /**
     * 创建新用户
     */
    public User create(User user) {
        if (existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        if (existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        // 项目演示环境使用 SHA-256 哈希；没有引入数据库或额外密码库。
        user.setPassword(hashPassword(user.getPassword()));

        return save(user);
    }

    /**
     * 更新用户
     */
    public User update(User user) {
        List<User> users = readAllUsers();

        boolean found = users.stream()
                .anyMatch(u -> u.getUserId().equals(user.getUserId()));

        if (!found) {
            throw new IllegalArgumentException("User not found: " + user.getUserId());
        }
        Optional<User> sameUsername = findByUsername(user.getUsername());
        if (sameUsername.isPresent() && !sameUsername.get().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }

        return save(user);
    }

    /**
     * 删除用户
     */
    public boolean delete(String userId) {
        List<User> taUsers = readUsersForRole(User.Role.TA);
        List<User> moUsers = readUsersForRole(User.Role.MO);
        List<User> adminUsers = readUsersForRole(User.Role.ADMIN);

        boolean removed = taUsers.removeIf(u -> u.getUserId().equals(userId));
        removed = moUsers.removeIf(u -> u.getUserId().equals(userId)) || removed;
        removed = adminUsers.removeIf(u -> u.getUserId().equals(userId)) || removed;

        if (removed) {
            writeUsersToFile(USER_FILE_TA, taUsers);
            writeUsersToFile(USER_FILE_MO, moUsers);
            writeUsersToFile(USER_FILE_ADMIN, adminUsers);
        }
        return removed;
    }

    /**
     * 获取所有用户
     */
    public List<User> findAll() {
        return new ArrayList<>(readAllUsers());
    }

    /**
     * 根据角色查找用户
     */
    public List<User> findByRole(User.Role role) {
        return new ArrayList<>(readUsersForRole(role));
    }

    /**
     * 验证用户登录
     * 返回用户对象（密码已验证）
     */
    public Optional<User> verifyLogin(String usernameOrEmail, String password) {
        Optional<User> userOpt = findByUsername(usernameOrEmail);
        if (!userOpt.isPresent()) {
            userOpt = findByEmail(usernameOrEmail);
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String hashedInput = hashPassword(password);

            if (hashedInput.equals(user.getPassword())) {
                // 更新最后登录时间
                user.setLastLoginAt(java.time.LocalDateTime.now());
                save(user);
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    /**
     * 密码哈希（SHA-256）
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    /**
     * 获取用户数量
     */
    public long count() {
        return readAllUsers().size();
    }

    /**
     * 清空所有用户（仅用于测试）
     */
    public void deleteAll() {
        // 仅测试/演示重置使用，前端没有清空用户的入口。
        writeUsersToFile(USER_FILE_TA, new ArrayList<>());
        writeUsersToFile(USER_FILE_MO, new ArrayList<>());
        writeUsersToFile(USER_FILE_ADMIN, new ArrayList<>());
    }

    /**
     * 批量创建用户（仅用于测试初始化）
     */
    public void batchCreate(List<User> users) {
        // 仅 DemoDataSeeder/测试初始化使用，正常注册仍走 create() 唯一性校验。
        List<User> taUsers = readUsersForRole(User.Role.TA);
        List<User> moUsers = readUsersForRole(User.Role.MO);
        List<User> adminUsers = readUsersForRole(User.Role.ADMIN);

        for (User user : users) {
            if (user.getRole() == User.Role.TA) {
                taUsers.add(user);
            } else if (user.getRole() == User.Role.MO) {
                moUsers.add(user);
            } else if (user.getRole() == User.Role.ADMIN) {
                adminUsers.add(user);
            }
        }

        writeUsersToFile(USER_FILE_TA, taUsers);
        writeUsersToFile(USER_FILE_MO, moUsers);
        writeUsersToFile(USER_FILE_ADMIN, adminUsers);
    }
}
