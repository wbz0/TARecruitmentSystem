package com.example.tarecruitment.common.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 简单文件日志工具。
 *
 * 日志文件位置: logs/app.log。
 * 它用于本地脚本直跑时快速排查，不替代 Servlet 容器日志。
 */
public final class Logger {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "app.log";
    private static final SimpleDateFormat DATETIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static volatile Logger instance;
    private final Path logPath;

    private Logger() {
        // 使用 user.dir 获取项目根目录，兼容从 scripts 目录运行的场景。
        String userDir = System.getProperty("user.dir");
        // 如果 user.dir 是 scripts，就退到父目录。
        if (userDir != null && userDir.endsWith("scripts")) {
            userDir = new java.io.File(userDir).getParent();
        }
        Path logsDir = Paths.get(userDir, LOG_DIR);

        // 确保日志目录存在；失败时仍允许应用继续跑，只把错误打到 stderr。
        try {
            Files.createDirectories(logsDir);
        } catch (IOException e) {
            System.err.println("[Logger] 创建日志目录失败: " + logsDir);
        }

        this.logPath = logsDir.resolve(LOG_FILE);
    }

    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }

    private String formatMessage(String level, String tag, String message) {
        return String.format("[%s] [%s] [%s] %s",
                DATETIME_FMT.format(new Date()), level, tag, message);
    }

    private void write(String level, String tag, String message) {
        String formatted = formatMessage(level, tag, message);
        synchronized (Logger.class) {
            writeToFile(formatted);
        }
        // 同时打印到控制台
        if ("ERROR".equals(level)) {
            System.err.println(formatted);
        } else {
            System.out.println(formatted);
        }
    }

    private void writeToFile(String content) {
        try (FileWriter fw = new FileWriter(logPath.toString(), true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(content);
        } catch (IOException e) {
            System.err.println("[Logger] 写入日志文件失败: " + logPath);
            e.printStackTrace(System.err);
        }
    }

    public void info(String tag, String message) {
        write("INFO", tag, message);
    }

    public void debug(String tag, String message) {
        write("DEBUG", tag, message);
    }

    public void error(String tag, String message) {
        write("ERROR", tag, message);
    }

    public void error(String tag, String message, Throwable t) {
        write("ERROR", tag, message + " - " + t.getClass().getName() + ": " + t.getMessage());
    }

    // 便捷静态方法
    public static void i(String tag, String message) {
        getInstance().info(tag, message);
    }

    public static void d(String tag, String message) {
        getInstance().debug(tag, message);
    }

    public static void e(String tag, String message) {
        getInstance().error(tag, message);
    }

    public static void e(String tag, String message, Throwable t) {
        getInstance().error(tag, message, t);
    }
}
