package club.sqlhub.entity.user.DTO.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileStatsDTO {
    private int questionsSolved;
    private int dayStreak;
    private int datasetsCompleted;
}
