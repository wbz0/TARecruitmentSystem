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
 * Simple file logging utility.
 *
 * Log file location: logs/app.log.
 * Used for quick troubleshooting when running scripts directly, does not replace Servlet container logs.
 */
public final class Logger {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "app.log";
    private static final SimpleDateFormat DATETIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static volatile Logger instance;
    private final Path logPath;

    private Logger() {
        // Use user.dir to get project root directory, compatible with running from scripts directory.
        String userDir = System.getProperty("user.dir");
        // If user.dir is scripts, go to parent directory.
        if (userDir != null && userDir.endsWith("scripts")) {
            userDir = new java.io.File(userDir).getParent();
        }
        Path logsDir = Paths.get(userDir, LOG_DIR);

        // Ensure log directory exists; if failed, still allow application to continue, just print error to stderr.
        try {
            Files.createDirectories(logsDir);
        } catch (IOException e) {
            System.err.println("[Logger] Failed to create log directory: " + logsDir);
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
        // Also print to console
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
            System.err.println("[Logger] Failed to write log file: " + logPath);
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

    // Convenient static methods
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
