package club.sqlhub.entity.utlities;

import club.sqlhub.constants.AppConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenDBO {
    private String accessToken;
    private String refreshToken;
    private long expiresIn = AppConstants.ACCESS_TOKEN_TTL_MS;

}