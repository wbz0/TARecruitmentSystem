package com.example.authlogin.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * SkillMatchService - 技能匹配服务
 * 第一阶段实现基础技能匹配算法：根据岗位必需技能与申请人技能计算匹配分数。
 */
public class SkillMatchService {

    private static final Set<String> KEYWORD_STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
            "in", "into", "is", "it", "of", "on", "or", "that", "the", "to",
            "with", "we", "you", "your", "our", "this", "these", "those"
    );

    private static final double SKILL_WEIGHT = 0.7;
    private static final double KEYWORD_WEIGHT = 0.3;
    private static final double NON_AI_WEIGHT = 0.6;
    private static final double AI_WEIGHT = 0.4;
    private static final long DEFAULT_CACHE_TTL_MILLIS = 5 * 60 * 1000L;
    private static final int CACHE_MAX_ENTRIES = 1000;

    private final AiSkillMatchClient aiSkillMatchClient;
    private final ConcurrentMap<String, CacheEntry> resultCache;
    private final long cacheTtlMillis;

    public SkillMatchService() {
        this(new HttpAiSkillMatchClient());
    }

    public SkillMatchService(AiSkillMatchClient aiSkillMatchClient) {
        this(aiSkillMatchClient, DEFAULT_CACHE_TTL_MILLIS);
    }

    SkillMatchService(AiSkillMatchClient aiSkillMatchClient, long cacheTtlMillis) {
        this.aiSkillMatchClient = aiSkillMatchClient != null ? aiSkillMatchClient : new HttpAiSkillMatchClient();
        this.resultCache = new ConcurrentHashMap<>();
        this.cacheTtlMillis = cacheTtlMillis > 0 ? cacheTtlMillis : DEFAULT_CACHE_TTL_MILLIS;
    }

    public enum MatchLevel {
        HIGH, MEDIUM, LOW, NONE
    }

    private static class KeywordMatchSnapshot {
        private final List<String> requiredKeywords;
        private final List<String> applicantKeywords;
        private final List<String> matchedKeywords;
        private final List<String> missingKeywords;
        private final double keywordScore;
        private final double blendedScore;

        private KeywordMatchSnapshot(List<String> requiredKeywords,
                                     List<String> applicantKeywords,
                                     List<String> matchedKeywords,
                                     List<String> missingKeywords,
                                     double keywordScore,
                                     double blendedScore) {
            this.requiredKeywords = requiredKeywords;
            this.applicantKeywords = applicantKeywords;
            this.matchedKeywords = matchedKeywords;
            this.missingKeywords = missingKeywords;
            this.keywordScore = keywordScore;
            this.blendedScore = blendedScore;
        }
    }

    private static class CacheEntry {
        private final SkillMatchResult result;
        private final long expiresAtMillis;

        private CacheEntry(SkillMatchResult result, long expiresAtMillis) {
            this.result = result;
            this.expiresAtMillis = expiresAtMillis;
        }
    }

    public static class SkillMatchResult {
        private final double score;
        private final MatchLevel level;
        private final List<String> matchedSkills;
        private final List<String> missingSkills;
        private final int requiredSkillCount;
        private final int applicantSkillCount;
        private final double skillScore;
        private final double keywordScore;
        private final List<String> matchedKeywords;
        private final List<String> missingKeywords;
        private final boolean aiEnhanced;
        private final double aiScore;
        private final String aiReason;

        public SkillMatchResult(double score,
                                MatchLevel level,
                                List<String> matchedSkills,
                                List<String> missingSkills,
                                int requiredSkillCount,
                                int applicantSkillCount) {
            this(
                    score,
                    level,
                    matchedSkills,
                    missingSkills,
                    requiredSkillCount,
                    applicantSkillCount,
                    score,
                    0.0,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    false,
                    0.0,
                    ""
            );
        }

        public SkillMatchResult(double score,
                                MatchLevel level,
                                List<String> matchedSkills,
                                List<String> missingSkills,
                                int requiredSkillCount,
                                int applicantSkillCount,
                                double skillScore,
                                double keywordScore,
                                List<String> matchedKeywords,
                                List<String> missingKeywords) {
            this(
                    score,
                    level,
                    matchedSkills,
                    missingSkills,
                    requiredSkillCount,
                    applicantSkillCount,
                    skillScore,
                    keywordScore,
                    matchedKeywords,
                    missingKeywords,
                    false,
                    0.0,
                    ""
            );
        }

        public SkillMatchResult(double score,
                                MatchLevel level,
                                List<String> matchedSkills,
                                List<String> missingSkills,
                                int requiredSkillCount,
                                int applicantSkillCount,
                                double skillScore,
                                double keywordScore,
                                List<String> matchedKeywords,
                                List<String> missingKeywords,
                                boolean aiEnhanced,
                                double aiScore,
                                String aiReason) {
            this.score = score;
            this.level = level;
            this.matchedSkills = matchedSkills;
            this.missingSkills = missingSkills;
            this.requiredSkillCount = requiredSkillCount;
            this.applicantSkillCount = applicantSkillCount;
            this.skillScore = skillScore;
            this.keywordScore = keywordScore;
            this.matchedKeywords = matchedKeywords;
            this.missingKeywords = missingKeywords;
            this.aiEnhanced = aiEnhanced;
            this.aiScore = aiScore;
            this.aiReason = aiReason;
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

        public double getSkillScore() {
            return skillScore;
        }

        public double getKeywordScore() {
            return keywordScore;
        }

        public List<String> getMatchedKeywords() {
            return matchedKeywords;
        }

        public List<String> getMissingKeywords() {
            return missingKeywords;
        }

        public boolean isAiEnhanced() {
            return aiEnhanced;
        }

        public double getAiScore() {
            return aiScore;
        }

        public String getAiReason() {
            return aiReason;
        }
    }

    /**
     * 基础匹配算法：
     * - 对技能做大小写与空白归一化
     * - 计算 requiredSkills 与 applicantSkills 的交集占比
     * - 产出匹配分数与等级
     */
    public SkillMatchResult matchSkills(List<String> requiredSkills, List<String> applicantSkills) {
        String cacheKey = buildCacheKey("skill", requiredSkills, null, applicantSkills, null);
        SkillMatchResult cached = getCachedResult(cacheKey);
        if (cached != null) {
            return cached;
        }

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
        SkillMatchResult result = new SkillMatchResult(
                score,
                level,
                Collections.unmodifiableList(matched),
                Collections.unmodifiableList(missing),
                requiredSet.size(),
                applicantSet.size()
        );
        cacheResult(cacheKey, result);
        return result;
    }

    /**
     * 关键词匹配算法：
     * - 从 requiredSkills + jobContext 提取岗位关键词
     * - 从 applicantSkills + applicantContext 提取候选人关键词
     * - 计算关键词匹配分，并与技能分加权得到总分
     */
    public SkillMatchResult matchByKeywords(List<String> requiredSkills,
                                            String jobContext,
                                            List<String> applicantSkills,
                                            String applicantContext) {
        String cacheKey = buildCacheKey("keyword", requiredSkills, jobContext, applicantSkills, applicantContext);
        SkillMatchResult cached = getCachedResult(cacheKey);
        if (cached != null) {
            return cached;
        }

        SkillMatchResult baseResult = matchSkills(requiredSkills, applicantSkills);
        KeywordMatchSnapshot snapshot = computeKeywordSnapshot(
                requiredSkills,
                jobContext,
                applicantSkills,
                applicantContext,
                baseResult.getSkillScore()
        );
        MatchLevel finalLevel = resolveLevel(snapshot.blendedScore);

        SkillMatchResult result = new SkillMatchResult(
                snapshot.blendedScore,
                finalLevel,
                baseResult.getMatchedSkills(),
                baseResult.getMissingSkills(),
                baseResult.getRequiredSkillCount(),
                baseResult.getApplicantSkillCount(),
                baseResult.getSkillScore(),
                snapshot.keywordScore,
                Collections.unmodifiableList(snapshot.matchedKeywords),
                Collections.unmodifiableList(snapshot.missingKeywords)
        );
        cacheResult(cacheKey, result);
        return result;
    }

    /**
     * AI 增强匹配：
     * - 先执行本地关键词匹配
     * - 若 AI 客户端可用，则获取 AI 评分并做加权融合
     * - AI 不可用时自动回退本地结果
     */
    public SkillMatchResult matchWithAi(List<String> requiredSkills,
                                        String jobContext,
                                        List<String> applicantSkills,
                                        String applicantContext) {
        String cacheKey = buildCacheKey("ai", requiredSkills, jobContext, applicantSkills, applicantContext);
        SkillMatchResult cached = getCachedResult(cacheKey);
        if (cached != null) {
            return cached;
        }

        SkillMatchResult baseResult = matchSkills(requiredSkills, applicantSkills);
        KeywordMatchSnapshot snapshot = computeKeywordSnapshot(
                requiredSkills,
                jobContext,
                applicantSkills,
                applicantContext,
                baseResult.getSkillScore()
        );
        SkillMatchResult keywordResult = new SkillMatchResult(
                snapshot.blendedScore,
                resolveLevel(snapshot.blendedScore),
                baseResult.getMatchedSkills(),
                baseResult.getMissingSkills(),
                baseResult.getRequiredSkillCount(),
                baseResult.getApplicantSkillCount(),
                baseResult.getSkillScore(),
                snapshot.keywordScore,
                Collections.unmodifiableList(snapshot.matchedKeywords),
                Collections.unmodifiableList(snapshot.missingKeywords)
        );

        Optional<AiSkillMatchClient.AiScoreResult> aiResultOpt = aiSkillMatchClient.score(
                snapshot.requiredKeywords,
                snapshot.applicantKeywords
        );
        if (aiResultOpt.isEmpty()) {
            cacheResult(cacheKey, keywordResult);
            return keywordResult;
        }

        AiSkillMatchClient.AiScoreResult aiResult = aiResultOpt.get();
        double boundedAiScore = clampScore(aiResult.getScore());
        double finalScore = roundTo2(keywordResult.getScore() * NON_AI_WEIGHT + boundedAiScore * AI_WEIGHT);
        MatchLevel level = resolveLevel(finalScore);

        SkillMatchResult result = new SkillMatchResult(
                finalScore,
                level,
                keywordResult.getMatchedSkills(),
                keywordResult.getMissingSkills(),
                keywordResult.getRequiredSkillCount(),
                keywordResult.getApplicantSkillCount(),
                keywordResult.getSkillScore(),
                keywordResult.getKeywordScore(),
                keywordResult.getMatchedKeywords(),
                keywordResult.getMissingKeywords(),
                true,
                boundedAiScore,
                aiResult.getReason()
        );
        cacheResult(cacheKey, result);
        return result;
    }

    public void clearCache() {
        resultCache.clear();
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
        return normalizeWhitespaceAndLower(rawSkill);
    }

    private KeywordMatchSnapshot computeKeywordSnapshot(List<String> requiredSkills,
                                                        String jobContext,
                                                        List<String> applicantSkills,
                                                        String applicantContext,
                                                        double baseSkillScore) {
        Set<String> requiredKeywordSet = buildKeywordSet(requiredSkills, jobContext);
        Set<String> applicantKeywordSet = buildKeywordSet(applicantSkills, applicantContext);

        List<String> matchedKeywords = new ArrayList<>();
        List<String> missingKeywords = new ArrayList<>();

        for (String keyword : requiredKeywordSet) {
            if (applicantKeywordSet.contains(keyword)) {
                matchedKeywords.add(keyword);
            } else {
                missingKeywords.add(keyword);
            }
        }

        double keywordScore;
        if (requiredKeywordSet.isEmpty()) {
            keywordScore = baseSkillScore;
        } else {
            keywordScore = roundTo2((matchedKeywords.size() * 100.0) / requiredKeywordSet.size());
        }

        double blendedScore = roundTo2(baseSkillScore * SKILL_WEIGHT + keywordScore * KEYWORD_WEIGHT);
        return new KeywordMatchSnapshot(
                new ArrayList<>(requiredKeywordSet),
                new ArrayList<>(applicantKeywordSet),
                matchedKeywords,
                missingKeywords,
                keywordScore,
                blendedScore
        );
    }

    private Set<String> buildKeywordSet(List<String> skills, String freeText) {
        Set<String> keywords = new LinkedHashSet<>();
        List<String> safeSkills = skills != null ? skills : Collections.emptyList();

        for (String skill : safeSkills) {
            appendKeywords(keywords, skill);
        }
        appendKeywords(keywords, freeText);

        return keywords;
    }

    private void appendKeywords(Set<String> collector, String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return;
        }
        String lower = rawText.toLowerCase(Locale.ROOT);
        StringBuilder tokenBuilder = new StringBuilder();
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if (isKeywordChar(c)) {
                tokenBuilder.append(c);
            } else {
                appendKeywordToken(collector, tokenBuilder);
            }
        }
        appendKeywordToken(collector, tokenBuilder);
    }

    private boolean isKeywordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '+' || c == '#';
    }

    private void appendKeywordToken(Set<String> collector, StringBuilder tokenBuilder) {
        if (tokenBuilder.length() == 0) {
            return;
        }
        String token = tokenBuilder.toString();
        tokenBuilder.setLength(0);
        if (isValidKeyword(token)) {
            collector.add(token);
        }
    }

    private boolean isValidKeyword(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        if (token.length() == 1 && !containsCjk(token)) {
            return false;
        }
        return !KEYWORD_STOP_WORDS.contains(token);
    }

    private boolean containsCjk(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '\u4e00' && c <= '\u9fff') {
                return true;
            }
        }
        return false;
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

    private String normalizeWhitespaceAndLower(String input) {
        StringBuilder sb = new StringBuilder();
        boolean previousWhitespace = true;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isWhitespace(c)) {
                if (!previousWhitespace) {
                    sb.append(' ');
                    previousWhitespace = true;
                }
            } else {
                sb.append(Character.toLowerCase(c));
                previousWhitespace = false;
            }
        }
        int length = sb.length();
        if (length > 0 && sb.charAt(length - 1) == ' ') {
            sb.setLength(length - 1);
        }
        return sb.toString();
    }

    private double clampScore(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 100.0) {
            return 100.0;
        }
        return value;
    }

    private SkillMatchResult getCachedResult(String key) {
        CacheEntry entry = resultCache.get(key);
        if (entry == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        if (entry.expiresAtMillis <= now) {
            resultCache.remove(key, entry);
            return null;
        }
        return entry.result;
    }

    private void cacheResult(String key, SkillMatchResult result) {
        if (cacheTtlMillis <= 0) {
            return;
        }
        if (resultCache.size() >= CACHE_MAX_ENTRIES) {
            evictExpiredEntries();
            if (resultCache.size() >= CACHE_MAX_ENTRIES) {
                resultCache.clear();
            }
        }
        long expireAt = System.currentTimeMillis() + cacheTtlMillis;
        resultCache.put(key, new CacheEntry(result, expireAt));
    }

    private void evictExpiredEntries() {
        long now = System.currentTimeMillis();
        for (String key : resultCache.keySet()) {
            CacheEntry entry = resultCache.get(key);
            if (entry != null && entry.expiresAtMillis <= now) {
                resultCache.remove(key, entry);
            }
        }
    }

    private String buildCacheKey(String mode,
                                 List<String> requiredSkills,
                                 String jobContext,
                                 List<String> applicantSkills,
                                 String applicantContext) {
        StringBuilder sb = new StringBuilder(256);
        sb.append(mode).append("|");
        appendNormalizedListPart(sb, requiredSkills);
        sb.append("|");
        appendNormalizedTextPart(sb, jobContext);
        sb.append("|");
        appendNormalizedListPart(sb, applicantSkills);
        sb.append("|");
        appendNormalizedTextPart(sb, applicantContext);
        return sb.toString();
    }

    private void appendNormalizedListPart(StringBuilder sb, List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        List<String> normalized = normalizeSkillList(values);
        Collections.sort(normalized);
        for (int i = 0; i < normalized.size(); i++) {
            if (i > 0) {
                sb.append(';');
            }
            sb.append(normalized.get(i));
        }
    }

    private void appendNormalizedTextPart(StringBuilder sb, String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        sb.append(normalizeWhitespaceAndLower(text));
    }
}
