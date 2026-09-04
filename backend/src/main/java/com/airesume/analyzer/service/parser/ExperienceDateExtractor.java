package com.airesume.analyzer.service.parser;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExperienceDateExtractor {

    private static final Pattern EXPLICIT_YEARS_PATTERN = Pattern.compile(
            "(?i)\\b(\\d{1,2})\\+?\\s*years?\\s+(?:of\\s+)?(?:experience|exp|working|in)\\b"
    );

    private static final Pattern YEAR_RANGE_PATTERN = Pattern.compile(
            "\\b(19\\d\\d|20\\d\\d)\\s*(?:-|–|—|to)\\s*(19\\d\\d|20\\d\\d|present|current|now|today)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern MONTH_YEAR_RANGE_PATTERN = Pattern.compile(
            "\\b(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\.?\\s+(19\\d\\d|20\\d\\d)\\s*(?:-|–|—|to)\\s*(?:(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\.?\\s+(19\\d\\d|20\\d\\d)|present|current|now|today)\\b",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Extracts estimated total years of work experience from parsed resume text.
     */
    public static int extractTotalExperienceYears(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        int currentYear = LocalDate.now().getYear();
        int maxExplicitYears = 0;

        // 1. Check explicit mentions like "5+ years of experience"
        Matcher explicitMatcher = EXPLICIT_YEARS_PATTERN.matcher(text);
        while (explicitMatcher.find()) {
            try {
                int years = Integer.parseInt(explicitMatcher.group(1));
                if (years > maxExplicitYears && years <= 40) {
                    maxExplicitYears = years;
                }
            } catch (Exception ignored) {}
        }

        // 2. Parse year ranges like "2018 - 2023" or "2020 - Present"
        List<int[]> ranges = new ArrayList<>();
        Matcher rangeMatcher = YEAR_RANGE_PATTERN.matcher(text);

        while (rangeMatcher.find()) {
            try {
                int startYear = Integer.parseInt(rangeMatcher.group(1));
                String endStr = rangeMatcher.group(2).toLowerCase();
                int endYear = (endStr.contains("present") || endStr.contains("current") || endStr.contains("now") || endStr.contains("today"))
                        ? currentYear
                        : Integer.parseInt(endStr);

                if (startYear <= endYear && startYear >= 1970 && endYear <= currentYear + 1) {
                    ranges.add(new int[]{startYear, endYear});
                }
            } catch (Exception ignored) {}
        }

        // 3. Merge overlapping ranges to avoid double counting multiple roles in same year
        ranges.sort(Comparator.comparingInt(r -> r[0]));
        int totalCalculatedYears = 0;

        if (!ranges.isEmpty()) {
            int currentStart = ranges.get(0)[0];
            int currentEnd = ranges.get(0)[1];

            for (int i = 1; i < ranges.size(); i++) {
                int nextStart = ranges.get(i)[0];
                int nextEnd = ranges.get(i)[1];

                if (nextStart <= currentEnd) {
                    currentEnd = Math.max(currentEnd, nextEnd);
                } else {
                    totalCalculatedYears += (currentEnd - currentStart);
                    currentStart = nextStart;
                    currentEnd = nextEnd;
                }
            }
            totalCalculatedYears += (currentEnd - currentStart);
        }

        return Math.max(maxExplicitYears, totalCalculatedYears);
    }
}
