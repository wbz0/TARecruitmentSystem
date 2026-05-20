package com.example.tarecruitment.common.storage;

import java.util.regex.Pattern;

/**
 * CSV 存储的转义/拆分工具。
 *
 * 本项目用 CSV 作为轻量数据层，DAO 负责读写文件。
 * 所有包含逗号、引号或换行的字段都必须走这里，避免破坏既有 CSV 列顺序。
 */
public final class CsvCodec {

    private static final Pattern CSV_SPLIT_PATTERN = Pattern.compile(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

    private CsvCodec() {
    }

    public static String escape(String value) {
        String text = value == null ? "" : value;
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    public static String unescape(String value) {
        if (value == null) {
            return "";
        }
        String text = value;
        if (text.startsWith("\"") && text.endsWith("\"") && text.length() >= 2) {
            text = text.substring(1, text.length() - 1).replace("\"\"", "\"");
        }
        return text;
    }

    public static String[] split(String csvLine) {
        if (csvLine == null) {
            return new String[0];
        }
        return CSV_SPLIT_PATTERN.split(csvLine, -1);
    }
}
