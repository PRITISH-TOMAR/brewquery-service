package club.sqlhub.Repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import club.sqlhub.constants.MessageConstants;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthRepository extends RuntimeException {

    private final JdbcTemplate jdbc;

    public void validateUser(String email, String validateUser) {
        int rowsAffected = jdbc.update(validateUser, email);

        if (rowsAffected == 0) {
            throw new IllegalStateException(MessageConstants.NO_USER_EXISTS_FOR_THIS_EMAIL);
        }
    }

}
