package com.example.authlogin.service;

import java.util.List;
import java.util.Optional;

/**
 * AI 技能匹配客户端抽象。
 * 统一外部 AI 能力调用，便于测试注入和实现替换。
 */
public interface AiSkillMatchClient {

    class AiScoreResult {
        private final double score;
        private final String reason;

        public AiScoreResult(double score, String reason) {
            this.score = score;
            this.reason = reason;
        }

        public double getScore() {
            return score;
        }

        public String getReason() {
            return reason;
        }
    }

    Optional<AiScoreResult> score(List<String> requiredKeywords, List<String> applicantKeywords);
}
