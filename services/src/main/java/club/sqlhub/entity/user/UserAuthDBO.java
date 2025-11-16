package club.sqlhub.entity.user;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAuthDBO {
    private Integer id;

    private Integer userId;

    private String  hashedPassword;

    private String salt;
    
    private LocalDateTime LastLogin;

    private Boolean verified;   
}
