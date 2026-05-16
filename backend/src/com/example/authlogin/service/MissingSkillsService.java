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

    public static class MissingSkillsReport {
        private final MissingSkillsAnalysis analysis;
        private final String summary;
        private final List<String> recommendations;

        public MissingSkillsReport(MissingSkillsAnalysis analysis, String summary, List<String> recommendations) {
            this.analysis = analysis;
            this.summary = summary;
            this.recommendations = recommendations;
        }

        public MissingSkillsAnalysis getAnalysis() {
            return analysis;
        }

        public String getSummary() {
            return summary;
        }

        public List<String> getRecommendations() {
            return recommendations;
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

    /**
     * 阶段3：生成缺失技能报告和建议。
     */
    public MissingSkillsReport generateMissingSkillsReport(List<String> requiredSkills, List<String> applicantSkills) {
        MissingSkillsAnalysis analysis = analyzeMissingSkills(requiredSkills, applicantSkills);
        String summary = buildSummary(analysis);
        List<String> recommendations = buildRecommendations(analysis);
        return new MissingSkillsReport(
                analysis,
                summary,
                Collections.unmodifiableList(recommendations)
        );
    }

    private Set<String> normalizeSkillSet(List<String> rawSkills) {
        List<String> safeSkills = rawSkills != null ? rawSkills : Collections.emptyList();
        Set<String> result = new LinkedHashSet<>();
        for (String rawSkill : safeSkills) {
            for (String token : splitSkillTokens(rawSkill)) {
                String normalized = normalizeSkill(token);
                if (!normalized.isEmpty() && !isIgnoredPlaceholder(normalized)) {
                    result.add(normalized);
                }
            }
        }
        return result;
    }

    private List<String> splitSkillTokens(String rawSkill) {
        if (rawSkill == null || rawSkill.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] tokens = rawSkill.split("[,;，；/|]+");
        List<String> result = new ArrayList<>();
        for (String token : tokens) {
            if (token != null && !token.trim().isEmpty()) {
                result.add(token);
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

    private boolean isIgnoredPlaceholder(String normalizedSkill) {
        return "n/a".equals(normalizedSkill)
                || "na".equals(normalizedSkill)
                || "none".equals(normalizedSkill)
                || "-".equals(normalizedSkill);
    }

    private double roundTo2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String buildSummary(MissingSkillsAnalysis analysis) {
        int requiredCount = analysis.getRequiredSkills().size();
        int missingCount = analysis.getMissingSkills().size();
        int matchedCount = analysis.getMatchedSkills().size();
        return "Required: " + requiredCount
                + ", Matched: " + matchedCount
                + ", Missing: " + missingCount
                + ", Score: " + analysis.getMatchScore() + "%";
    }

    private List<String> buildRecommendations(MissingSkillsAnalysis analysis) {
        List<String> recommendations = new ArrayList<>();

        if (analysis.getMissingSkills().isEmpty()) {
            recommendations.add("Current applicant already covers all required skills.");
            recommendations.add("Recommend proceeding to interview or practical evaluation.");
            return recommendations;
        }

        recommendations.add("Prioritize closing these skill gaps: " + String.join(", ", analysis.getMissingSkills()) + ".");

        if (analysis.getMatchScore() < 50.0) {
            recommendations.add("Recommend foundational training before advanced TA tasks.");
        } else {
            recommendations.add("Recommend short-term targeted training for missing areas.");
        }

        for (String missingSkill : analysis.getMissingSkills()) {
            recommendations.add("Add a learning task for \"" + missingSkill + "\" and verify by mini-assignment.");
        }

        return recommendations;
    }
}
