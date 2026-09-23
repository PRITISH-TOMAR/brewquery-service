package club.sqlhub.entity.user.DTO.profile;

import java.util.List;

import lombok.Data;

@Data
public class UserStatsResponseDTO {
    private int    totalSubmissions;
    private int    correctSubmissions;
    private int    correctPct;
    private int    wrongSubmissions;
    private int    wrongPct;
    private String avgTime;             // "00:12:36"
    private Integer avgTimePct;         // nullable — percentile vs other users

    private DifficultyDTO         difficulty;
    private List<FavouriteDatasetDTO> favouriteDatasets;
    private List<BadgeDTO>        recentBadges;
}
