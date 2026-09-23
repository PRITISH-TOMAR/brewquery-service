package club.sqlhub.entity.user.DTO.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LevelDTO {
    private int    number;
    private String title;
    private int    xpToNext;
    private int    xpProgress; // 0 – 100
}
