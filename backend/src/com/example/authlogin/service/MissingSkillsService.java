package com.example.authlogin.service;

import java.util.Collections;
import java.util.List;

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
     * 阶段1仅提供服务骨架，具体技能对比逻辑在后续阶段实现。
     */
    public MissingSkillsAnalysis analyzeMissingSkills(List<String> requiredSkills, List<String> applicantSkills) {
        List<String> safeRequired = requiredSkills != null ? requiredSkills : Collections.emptyList();
        List<String> safeApplicant = applicantSkills != null ? applicantSkills : Collections.emptyList();

        return new MissingSkillsAnalysis(
                safeRequired,
                safeApplicant,
                Collections.emptyList(),
                Collections.emptyList(),
                0.0
        );
    }
}
