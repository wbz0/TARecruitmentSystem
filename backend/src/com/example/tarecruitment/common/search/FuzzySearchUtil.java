package com.example.tarecruitment.common.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * FuzzySearchUtil - Unified keyword fuzzy search utility.
 *
 * Capabilities:
 * - Case normalization, space/hyphen normalization
 * - Token matching
 * - English typo (edit distance 1-2) tolerance
 * - Chinese text matching
 *
 * Legacy/to be removed: Pinyin search was considered in early stages, but the current frontend has no pinyin search prompts or entry points.
 * Now only retains Chinese and English text search and English lightweight typo tolerance.
 */
public final class FuzzySearchUtil {

    private static final Pattern ALNUM_PATTERN = Pattern.compile("^[a-z0-9]+$");
    private static final Pattern SEPARATOR_PATTERN = Pattern.compile("[\\s\\-_]+");
    private static final Pattern NON_SEARCH_CHAR_PATTERN = Pattern.compile("[^\\p{L}\\p{Nd}\\u4E00-\\u9FFF]+");
    private static final Pattern MULTI_SPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern CJK_PATTERN = Pattern.compile("[\\u4E00-\\u9FFF]");

    private FuzzySearchUtil() {
    }

    public static final class SearchOutcome<T> {
        private final List<T> items;
        private final boolean keywordApplied;
        private final boolean approximateOnly;
        private final boolean hasMatches;

        private SearchOutcome(List<T> items,
                              boolean keywordApplied,
                              boolean approximateOnly,
                              boolean hasMatches) {
            this.items = items;
            this.keywordApplied = keywordApplied;
            this.approximateOnly = approximateOnly;
            this.hasMatches = hasMatches;
        }

        public List<T> getItems() {
            return items;
        }

        public boolean isKeywordApplied() {
            return keywordApplied;
        }

        public boolean isApproximateOnly() {
            return approximateOnly;
        }

        public boolean hasMatches() {
            return hasMatches;
        }
    }

    private enum MatchTier {
        EXACT(1000, false),
        PREFIX(900, false),
        CONTAINS(820, false),
        TOKEN_COVERAGE(760, false),
        TYPO(680, true),
        NONE(0, true);

        private final int baseScore;
        private final boolean approximate;

        MatchTier(int baseScore, boolean approximate) {
            this.baseScore = baseScore;
            this.approximate = approximate;
        }
    }

    private static final class MatchResult {
        private final MatchTier tier;
        private final int score;
        private final boolean matched;

        private MatchResult(MatchTier tier, int score, boolean matched) {
            this.tier = tier;
            this.score = score;
            this.matched = matched;
        }

        private static MatchResult noMatch() {
            return new MatchResult(MatchTier.NONE, 0, false);
        }

        private static MatchResult of(MatchTier tier, int score) {
            return new MatchResult(tier, score, true);
        }
    }

    private static final class RankedItem<T> {
        private final T item;
        private final int score;
        private final int index;

        private RankedItem(T item, int score, int index) {
            this.item = item;
            this.score = score;
            this.index = index;
        }
    }

    private static final class QueryProfile {
        private final String normalized;
        private final String compact;
        private final List<String> tokens;
        private final boolean hasChinese;
        private final boolean alphaNumericCompact;

        private QueryProfile(String normalized) {
            this.normalized = normalized;
            this.compact = compact(normalized);
            this.tokens = tokenize(normalized);
            this.hasChinese = containsChinese(normalized);
            this.alphaNumericCompact = ALNUM_PATTERN.matcher(this.compact).matches();
        }
    }

    private static final class CandidateProfile {
        private final List<String> normalizedFields;
        private final List<String> compactFields;
        private final String normalizedCombined;
        private final String compactCombined;
        private final List<String> tokens;
        private final boolean hasChinese;

        private CandidateProfile(List<String> normalizedFields,
                                 List<String> compactFields,
                                 String normalizedCombined,
                                 String compactCombined,
                                 List<String> tokens,
                                 boolean hasChinese) {
            this.normalizedFields = normalizedFields;
            this.compactFields = compactFields;
            this.normalizedCombined = normalizedCombined;
            this.compactCombined = compactCombined;
            this.tokens = tokens;
            this.hasChinese = hasChinese;
        }
    }

    public static <T> SearchOutcome<T> search(List<T> sourceItems,
                                              String keyword,
                                              Function<T, List<String>> fieldExtractor) {
        List<T> safeSource = sourceItems == null ? Collections.emptyList() : sourceItems;
        String normalizedKeyword = normalize(keyword);

        if (normalizedKeyword.isEmpty()) {
            return new SearchOutcome<>(new ArrayList<>(safeSource), false, false, !safeSource.isEmpty());
        }

        QueryProfile query = new QueryProfile(normalizedKeyword);
        List<RankedItem<T>> rankedItems = new ArrayList<>();
        boolean hasDirectMatch = false;

        for (int i = 0; i < safeSource.size(); i++) {
            T item = safeSource.get(i);
            List<String> fields = fieldExtractor == null ? Collections.emptyList() : fieldExtractor.apply(item);
            CandidateProfile candidate = buildCandidateProfile(fields);
            MatchResult match = match(query, candidate);
            if (!match.matched) {
                continue;
            }
            rankedItems.add(new RankedItem<>(item, match.score, i));
            if (!match.tier.approximate) {
                hasDirectMatch = true;
            }
        }

        rankedItems.sort(
                Comparator.<RankedItem<T>>comparingInt(item -> item.score).reversed()
                        .thenComparingInt(item -> item.index)
        );

        List<T> ordered = new ArrayList<>(rankedItems.size());
        for (RankedItem<T> ranked : rankedItems) {
            ordered.add(ranked.item);
        }

        boolean hasMatches = !ordered.isEmpty();
        boolean approximateOnly = hasMatches && !hasDirectMatch;
        // approximateOnly is passed to frontend, used to display “no exact match, showing similar results” hint.
        return new SearchOutcome<>(ordered, true, approximateOnly, hasMatches);
    }

    private static MatchResult match(QueryProfile query, CandidateProfile candidate) {
        if (candidate.normalizedCombined.isEmpty()) {
            return MatchResult.noMatch();
        }

        if (matchesExact(query, candidate)) {
            return MatchResult.of(MatchTier.EXACT, MatchTier.EXACT.baseScore + scoreBonus(query.compact.length(), 24));
        }

        if (matchesPrefix(query, candidate)) {
            return MatchResult.of(MatchTier.PREFIX, MatchTier.PREFIX.baseScore + scoreBonus(query.compact.length(), 18));
        }

        if (matchesContains(query, candidate)) {
            return MatchResult.of(MatchTier.CONTAINS, MatchTier.CONTAINS.baseScore + scoreBonus(query.compact.length(), 14));
        }

        if (matchesTokenCoverage(query, candidate)) {
            return MatchResult.of(MatchTier.TOKEN_COVERAGE, MatchTier.TOKEN_COVERAGE.baseScore + scoreBonus(query.tokens.size(), 20));
        }

        int typoDistance = bestTypoDistance(query, candidate);
        if (typoDistance >= 0) {
            int score = MatchTier.TYPO.baseScore - (typoDistance * 36);
            return MatchResult.of(MatchTier.TYPO, score);
        }

        return MatchResult.noMatch();
    }

    private static boolean matchesExact(QueryProfile query, CandidateProfile candidate) {
        for (int i = 0; i < candidate.normalizedFields.size(); i++) {
            String field = candidate.normalizedFields.get(i);
            if (field.equals(query.normalized)) {
                return true;
            }
            if (candidate.compactFields.get(i).equals(query.compact)) {
                return true;
            }
        }
        return candidate.normalizedCombined.equals(query.normalized) || candidate.compactCombined.equals(query.compact);
    }

    private static boolean matchesPrefix(QueryProfile query, CandidateProfile candidate) {
        for (int i = 0; i < candidate.normalizedFields.size(); i++) {
            String field = candidate.normalizedFields.get(i);
            if (field.startsWith(query.normalized)) {
                return true;
            }
            if (candidate.compactFields.get(i).startsWith(query.compact)) {
                return true;
            }
        }
        return candidate.normalizedCombined.startsWith(query.normalized)
                || candidate.compactCombined.startsWith(query.compact);
    }

    private static boolean matchesContains(QueryProfile query, CandidateProfile candidate) {
        for (int i = 0; i < candidate.normalizedFields.size(); i++) {
            String field = candidate.normalizedFields.get(i);
            if (field.contains(query.normalized)) {
                return true;
            }
            if (candidate.compactFields.get(i).contains(query.compact)) {
                return true;
            }
        }
        return candidate.normalizedCombined.contains(query.normalized)
                || candidate.compactCombined.contains(query.compact);
    }

    private static boolean matchesTokenCoverage(QueryProfile query, CandidateProfile candidate) {
        if (query.tokens.size() <= 1) {
            return false;
        }

        for (String token : query.tokens) {
            if (token.isEmpty()) {
                continue;
            }
            String compactToken = compact(token);
            boolean hit = candidate.normalizedCombined.contains(token)
                    || (!compactToken.isEmpty() && candidate.compactCombined.contains(compactToken));
            if (!hit) {
                return false;
            }
        }
        return true;
    }

    private static int bestTypoDistance(QueryProfile query, CandidateProfile candidate) {
        if (query.hasChinese || !query.alphaNumericCompact) {
            return -1;
        }
        int queryLength = query.compact.length();
        if (queryLength < 5) {
            return -1;
        }

        int maxDistance = maxTypoDistance(queryLength);
        if (maxDistance <= 0) {
            return -1;
        }

        int best = Integer.MAX_VALUE;
        for (String token : candidate.tokens) {
            if (!ALNUM_PATTERN.matcher(token).matches()) {
                continue;
            }
            if (Math.abs(token.length() - queryLength) > maxDistance) {
                continue;
            }
            int distance = levenshteinWithin(query.compact, token, maxDistance);
            if (distance >= 0) {
                best = Math.min(best, distance);
            }
        }

        if (ALNUM_PATTERN.matcher(candidate.compactCombined).matches()
                && Math.abs(candidate.compactCombined.length() - queryLength) <= maxDistance) {
            int distance = levenshteinWithin(query.compact, candidate.compactCombined, maxDistance);
            if (distance >= 0) {
                best = Math.min(best, distance);
            }
        }

        return best == Integer.MAX_VALUE ? -1 : best;
    }

    private static int maxTypoDistance(int queryLength) {
        if (queryLength >= 8) {
            return 2;
        }
        if (queryLength >= 5) {
            return 1;
        }
        return 0;
    }

    private static int levenshteinWithin(String source, String target, int threshold) {
        int sourceLength = source.length();
        int targetLength = target.length();
        if (Math.abs(sourceLength - targetLength) > threshold) {
            return -1;
        }

        int[] previous = new int[targetLength + 1];
        int[] current = new int[targetLength + 1];
        for (int j = 0; j <= targetLength; j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= sourceLength; i++) {
            current[0] = i;
            int rowMin = current[0];
            char sourceChar = source.charAt(i - 1);

            for (int j = 1; j <= targetLength; j++) {
                int cost = sourceChar == target.charAt(j - 1) ? 0 : 1;
                int insertion = current[j - 1] + 1;
                int deletion = previous[j] + 1;
                int substitution = previous[j - 1] + cost;
                current[j] = Math.min(Math.min(insertion, deletion), substitution);
                rowMin = Math.min(rowMin, current[j]);
            }

            if (rowMin > threshold) {
                return -1;
            }

            int[] swap = previous;
            previous = current;
            current = swap;
        }

        return previous[targetLength] <= threshold ? previous[targetLength] : -1;
    }

    private static CandidateProfile buildCandidateProfile(List<String> fields) {
        List<String> normalizedFields = new ArrayList<>();
        List<String> compactFields = new ArrayList<>();
        boolean hasChinese = false;

        if (fields != null) {
            for (String field : fields) {
                if (field == null || field.trim().isEmpty()) {
                    continue;
                }
                String raw = field.trim();
                String normalized = normalize(raw);
                if (normalized.isEmpty()) {
                    continue;
                }
                normalizedFields.add(normalized);
                compactFields.add(compact(normalized));
                if (!hasChinese && containsChinese(raw)) {
                    hasChinese = true;
                }
            }
        }

        String normalizedCombined = normalize(String.join(" ", normalizedFields));
        String compactCombined = compact(normalizedCombined);
        List<String> tokens = tokenize(normalizedCombined);

        return new CandidateProfile(
                normalizedFields,
                compactFields,
                normalizedCombined,
                compactCombined,
                tokens,
                hasChinese
        );
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.toLowerCase(Locale.ROOT)
                .replace('－', '-')
                .replace('—', '-')
                .replace('–', '-')
                .replace('‑', '-');
        normalized = SEPARATOR_PATTERN.matcher(normalized).replaceAll(" ");
        normalized = NON_SEARCH_CHAR_PATTERN.matcher(normalized).replaceAll(" ");
        normalized = MULTI_SPACE_PATTERN.matcher(normalized).replaceAll(" ").trim();
        return normalized;
    }

    private static String compact(String normalized) {
        return normalized == null ? "" : normalized.replace(" ", "");
    }

    private static List<String> tokenize(String normalized) {
        if (normalized == null || normalized.isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = normalized.split(" ");
        List<String> tokens = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (!part.isEmpty()) {
                tokens.add(part);
            }
        }
        return tokens;
    }

    private static boolean containsChinese(String text) {
        return text != null && CJK_PATTERN.matcher(text).find();
    }

    private static int scoreBonus(int value, int cap) {
        return Math.min(value, cap);
    }
}
