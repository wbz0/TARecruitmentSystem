package com.example.authlogin.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * MissingSkillsService - 缺失技能分析服务（阶段1骨架）
 */
public class MissingSkillsService {

    public static class MissingSkillsAnalysis {
        private final List<String> requiredSkills;
        private final List<String> applicantSkills;
        private final List<String> matchedSkills;
        private final List<String> missingSkills;
        private final double matchScore;

        public MissingSkillsAnalysis(List<String> requiredSkills,
                                     List<String> applicantSkills,
                                     List<String> matchedSkills,
                                     List<String> missingSkills,
                                     double matchScore) {
            this.requiredSkills = requiredSkills;
            this.applicantSkills = applicantSkills;
            this.matchedSkills = matchedSkills;
            this.missingSkills = missingSkills;
            this.matchScore = matchScore;
        }

        public List<String> getRequiredSkills() {
            return requiredSkills;
        }

        public List<String> getApplicantSkills() {
            return applicantSkills;
        }

        public List<String> getMatchedSkills() {
            return matchedSkills;
        }

        public List<String> getMissingSkills() {
            return missingSkills;
        }

        public double getMatchScore() {
            return matchScore;
        }
    }

    /**
     * 阶段2：实现职位要求与申请人技能对比逻辑。
     */
    public MissingSkillsAnalysis analyzeMissingSkills(List<String> requiredSkills, List<String> applicantSkills) {
        Set<String> requiredSet = normalizeSkillSet(requiredSkills);
        Set<String> applicantSet = normalizeSkillSet(applicantSkills);

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String requiredSkill : requiredSet) {
            if (applicantSet.contains(requiredSkill)) {
                matchedSkills.add(requiredSkill);
            } else {
                missingSkills.add(requiredSkill);
            }
        }

        double score = requiredSet.isEmpty() ? 100.0 : roundTo2((matchedSkills.size() * 100.0) / requiredSet.size());
        return new MissingSkillsAnalysis(
                Collections.unmodifiableList(new ArrayList<>(requiredSet)),
                Collections.unmodifiableList(new ArrayList<>(applicantSet)),
                Collections.unmodifiableList(matchedSkills),
                Collections.unmodifiableList(missingSkills),
                score
        );
    }

    private Set<String> normalizeSkillSet(List<String> rawSkills) {
        List<String> safeSkills = rawSkills != null ? rawSkills : Collections.emptyList();
        Set<String> result = new LinkedHashSet<>();
        for (String rawSkill : safeSkills) {
            String normalized = normalizeSkill(rawSkill);
            if (!normalized.isEmpty()) {
                result.add(normalized);
            }
        }
        return result;
    }

    private String normalizeSkill(String rawSkill) {
        if (rawSkill == null) {
            return "";
        }
        return rawSkill.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private double roundTo2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
