package club.sqlhub.entity.user.DTO.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DifficultyDTO {
    private int easy;
    private int medium;
    private int hard;
}
