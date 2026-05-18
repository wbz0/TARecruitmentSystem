package com.example.authlogin.dao;

import com.example.authlogin.model.User;
import com.example.authlogin.util.StoragePaths;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UserDao - 用户数据访问对象
 * 使用CSV文件存储用户数据
 */
public class UserDao {

    // 项目数据目录 - 使用可移植路径，避免依赖本机绝对路径
    private static final String DATA_DIR = StoragePaths.getDataDir();
    private static final String USER_FILE_TA = DATA_DIR + File.separator + "users_ta.csv";
    private static final String USER_FILE_MO = DATA_DIR + File.separator + "users_mo.csv";
    private static final String USER_FILE_ADMIN = DATA_DIR + File.separator + "users_admin.csv";
    private static final String LEGACY_USER_FILE = DATA_DIR + File.separator + "users.csv";
    private static final String CSV_HEADER = "userId,username,password,email,role,createdAt,lastLoginAt";

    private static UserDao instance;

    private UserDao() {
        initDataDirectory();
    }

    public static synchronized UserDao getInstance() {
        if (instance == null) {
            instance = new UserDao();
        }
        return instance;
    }

    private void initDataDirectory() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
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
     * 读取所有用户
     */
    private List<User> readAllUsers() {
        initUserFiles();
        List<User> users = new ArrayList<>();

        users.addAll(readUsersFromFile(USER_FILE_TA));
        users.addAll(readUsersFromFile(USER_FILE_MO));
        users.addAll(readUsersFromFile(USER_FILE_ADMIN));

        // 兼容历史 users.csv。只有在新拆分文件都为空时回退读取旧文件。
        if (users.isEmpty()) {
            users.addAll(readUsersFromFile(LEGACY_USER_FILE));
        }

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

    /**
     * 写入所有用户
     */
    private void writeUsersToFile(String filePath, List<User> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println(CSV_HEADER);
            for (User user : users) {
                writer.println(user.toCsv());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write users file", e);
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
        String normalized = username.trim();
        return readAllUsers().stream()
                .filter(u -> u.getUsername().equals(normalized))
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

        String targetFile = getUserFileByRole(user.getRole());
        List<User> targetUsers = readUsersFromFile(targetFile);
        List<User> taUsers = readUsersFromFile(USER_FILE_TA);
        List<User> moUsers = readUsersFromFile(USER_FILE_MO);
        List<User> adminUsers = readUsersFromFile(USER_FILE_ADMIN);

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

        // 加密密码
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

        return save(user);
    }

    /**
     * 删除用户
     */
    public boolean delete(String userId) {
        List<User> taUsers = readUsersFromFile(USER_FILE_TA);
        List<User> moUsers = readUsersFromFile(USER_FILE_MO);
        List<User> adminUsers = readUsersFromFile(USER_FILE_ADMIN);

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
        List<User> usersByRole = readUsersFromFile(getUserFileByRole(role));
        if (!usersByRole.isEmpty()) {
            return usersByRole.stream().collect(Collectors.toList());
        }
        return readUsersFromFile(LEGACY_USER_FILE).stream()
                .filter(u -> u.getRole() == role)
                .collect(Collectors.toList());
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
        writeUsersToFile(USER_FILE_TA, new ArrayList<>());
        writeUsersToFile(USER_FILE_MO, new ArrayList<>());
        writeUsersToFile(USER_FILE_ADMIN, new ArrayList<>());
    }

    /**
     * 批量创建用户（仅用于测试初始化）
     */
    public void batchCreate(List<User> users) {
        List<User> taUsers = readUsersFromFile(USER_FILE_TA);
        List<User> moUsers = readUsersFromFile(USER_FILE_MO);
        List<User> adminUsers = readUsersFromFile(USER_FILE_ADMIN);

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
