package club.sqlhub.entity.utlities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerifyDTO {
    private String key;
    private String email;
}
