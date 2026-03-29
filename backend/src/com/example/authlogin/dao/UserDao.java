package com.example.authlogin.dao;

import com.example.authlogin.model.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    // 项目数据目录 - 使用固定绝对路径
    // 如果部署到Tomcat，需要确保此路径可写
    private static final String DATA_DIR = "D:/HuaweiMoveData/Users/Carne/Desktop/SoftwareEngineering/data";
    private static final String USER_FILE = DATA_DIR + File.separator + "users.csv";
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
    }

    /**
     * 初始化用户数据文件
     */
    private void initUserFile() {
        File userFile = new File(USER_FILE);
        if (!userFile.exists()) {
            try {
                userFile.createNewFile();
                try (FileWriter writer = new FileWriter(USER_FILE)) {
                    writer.write(CSV_HEADER + "\n");
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to create users file", e);
            }
        }
    }

    /**
     * 读取所有用户
     */
    private List<User> readAllUsers() {
        initUserFile();
        List<User> users = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
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
    private void writeAllUsers(List<User> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USER_FILE))) {
            writer.println(CSV_HEADER);
            for (User user : users) {
                writer.println(user.toCsv());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write users file", e);
        }
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
        return readAllUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    /**
     * 根据邮箱查找用户
     */
    public Optional<User> findByEmail(String email) {
        return readAllUsers().stream()
                .filter(u -> u.getEmail().equals(email))
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
        List<User> users = readAllUsers();

        Optional<User> existingUser = users.stream()
                .filter(u -> u.getUserId().equals(user.getUserId()))
                .findFirst();

        if (existingUser.isPresent()) {
            users.remove(existingUser.get());
        }

        users.add(user);
        writeAllUsers(users);

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
        List<User> users = readAllUsers();
        boolean removed = users.removeIf(u -> u.getUserId().equals(userId));

        if (removed) {
            writeAllUsers(users);
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
        return readAllUsers().stream()
                .filter(u -> u.getRole() == role)
                .collect(Collectors.toList());
    }

    /**
     * 验证用户登录
     * 返回用户对象（密码已验证）
     */
    public Optional<User> verifyLogin(String username, String password) {
        Optional<User> userOpt = findByUsername(username);

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
        writeAllUsers(new ArrayList<>());
    }

    /**
     * 批量创建用户（仅用于测试初始化）
     */
    public void batchCreate(List<User> users) {
        List<User> existingUsers = readAllUsers();
        existingUsers.addAll(users);
        writeAllUsers(existingUsers);
    }
}
