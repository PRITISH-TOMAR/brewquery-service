package club.sqlhub.utils.User;

import club.sqlhub.entity.user.DTO.profile.LevelDTO;

public final class LevelUtils {

    // Each entry: minimum submissions to reach this level
    private static final int[]    TIER_STARTS = { 0, 10, 25, 50, 100, 200, 300, 500 };
    private static final String[] TIER_TITLES = {
        "Beginner", "SQL Novice", "Query Writer", "Data Explorer",
        "SQL Practitioner", "Query Builder", "Schema Architect", "Data Wizard"
    };

    private LevelUtils() {}

    public static LevelDTO compute(int totalSubmissions) {
        int level = 1;
        for (int i = 0; i < TIER_STARTS.length; i++) {
            if (totalSubmissions >= TIER_STARTS[i]) level = i + 1;
            else break;
        }

        // Clamp to max defined level
        level = Math.min(level, TIER_STARTS.length);

        int tierStart = TIER_STARTS[level - 1];
        int tierEnd   = level < TIER_STARTS.length ? TIER_STARTS[level] : TIER_STARTS[level - 1] + 200;
        int tierRange = tierEnd - tierStart;

        int xpToNext   = Math.max(0, tierEnd - totalSubmissions);
        int xpProgress = tierRange > 0
                ? (int) Math.min(100, (totalSubmissions - tierStart) * 100.0 / tierRange)
                : 100;

        return new LevelDTO(level, TIER_TITLES[level - 1], xpToNext, xpProgress);
    }
}
