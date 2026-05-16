package com.example.authlogin.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * SkillMatchService - 技能匹配服务
 * 第一阶段实现基础技能匹配算法：根据岗位必需技能与申请人技能计算匹配分数。
 */
public class SkillMatchService {

    public enum MatchLevel {
        HIGH, MEDIUM, LOW, NONE
    }

    public static class SkillMatchResult {
        private final double score;
        private final MatchLevel level;
        private final List<String> matchedSkills;
        private final List<String> missingSkills;
        private final int requiredSkillCount;
        private final int applicantSkillCount;

        public SkillMatchResult(double score,
                                MatchLevel level,
                                List<String> matchedSkills,
                                List<String> missingSkills,
                                int requiredSkillCount,
                                int applicantSkillCount) {
            this.score = score;
            this.level = level;
            this.matchedSkills = matchedSkills;
            this.missingSkills = missingSkills;
            this.requiredSkillCount = requiredSkillCount;
            this.applicantSkillCount = applicantSkillCount;
        }

        public double getScore() {
            return score;
        }

        public MatchLevel getLevel() {
            return level;
        }

        public List<String> getMatchedSkills() {
            return matchedSkills;
        }

        public List<String> getMissingSkills() {
            return missingSkills;
        }

        public int getRequiredSkillCount() {
            return requiredSkillCount;
        }

        public int getApplicantSkillCount() {
            return applicantSkillCount;
        }
    }

    /**
     * 基础匹配算法：
     * - 对技能做大小写与空白归一化
     * - 计算 requiredSkills 与 applicantSkills 的交集占比
     * - 产出匹配分数与等级
     */
    public SkillMatchResult matchSkills(List<String> requiredSkills, List<String> applicantSkills) {
        List<String> safeRequired = requiredSkills != null ? requiredSkills : Collections.emptyList();
        List<String> safeApplicant = applicantSkills != null ? applicantSkills : Collections.emptyList();

        List<String> normalizedRequired = normalizeSkillList(safeRequired);
        List<String> normalizedApplicant = normalizeSkillList(safeApplicant);

        Set<String> requiredSet = new LinkedHashSet<>(normalizedRequired);
        Set<String> applicantSet = new LinkedHashSet<>(normalizedApplicant);

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String required : requiredSet) {
            if (applicantSet.contains(required)) {
                matched.add(required);
            } else {
                missing.add(required);
            }
        }

        double score;
        if (requiredSet.isEmpty()) {
            score = 100.0;
        } else {
            score = roundTo2((matched.size() * 100.0) / requiredSet.size());
        }

        MatchLevel level = resolveLevel(score);
        return new SkillMatchResult(
                score,
                level,
                Collections.unmodifiableList(matched),
                Collections.unmodifiableList(missing),
                requiredSet.size(),
                applicantSet.size()
        );
    }

    private List<String> normalizeSkillList(List<String> rawSkills) {
        List<String> normalized = new ArrayList<>();
        for (String rawSkill : rawSkills) {
            String skill = normalizeSkill(rawSkill);
            if (!skill.isEmpty()) {
                normalized.add(skill);
            }
        }
        return normalized;
    }

    private String normalizeSkill(String rawSkill) {
        if (rawSkill == null) {
            return "";
        }
        String compact = rawSkill.trim().replaceAll("\\s+", " ");
        return compact.toLowerCase(Locale.ROOT);
    }

    private MatchLevel resolveLevel(double score) {
        if (score >= 85.0) {
            return MatchLevel.HIGH;
        }
        if (score >= 60.0) {
            return MatchLevel.MEDIUM;
        }
        if (score > 0.0) {
            return MatchLevel.LOW;
        }
        return MatchLevel.NONE;
    }

    private double roundTo2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
