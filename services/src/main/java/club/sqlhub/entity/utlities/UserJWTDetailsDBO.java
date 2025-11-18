package club.sqlhub.entity.utlities;

import club.sqlhub.constants.AppConstants;
import club.sqlhub.entity.user.DTO.UserDetailsDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserJWTDetailsDBO {
    private String accessToken;
    private String refreshToken;
    private long expiresIn = AppConstants.ACCESS_TOKEN_TTL_MS;

    private UserDetailsDTO user;
}
