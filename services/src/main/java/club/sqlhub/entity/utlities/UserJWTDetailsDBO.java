package club.sqlhub.entity.utlities;

import club.sqlhub.entity.user.DTO.UserDetailsDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserJWTDetailsDBO {

    private TokenDBO tokenDetails;
    private UserDetailsDTO user;
}