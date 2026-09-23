package club.sqlhub.entity.user.DTO.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeatmapEntryDTO {
    private String date;  // "YYYY-MM-DD"
    private int    count;
}
