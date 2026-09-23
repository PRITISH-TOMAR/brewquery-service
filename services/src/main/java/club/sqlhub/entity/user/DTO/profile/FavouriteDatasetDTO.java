package club.sqlhub.entity.user.DTO.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavouriteDatasetDTO {
    private String name;
    private int    count;
}
