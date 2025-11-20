package club.sqlhub.entity.user.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginDTO {
    private String email;
    private String password;
    private Boolean rememberMe;
}
