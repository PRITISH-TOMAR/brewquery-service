package club.sqlhub.Repository;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import club.sqlhub.constants.MessageConstants;
import club.sqlhub.entity.user.DBO.UserDetailsDBO;
import club.sqlhub.entity.user.DTO.ResetPasswordDTO;
import club.sqlhub.entity.user.DTO.UserDetailsDTO;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthRepository {

    private final JdbcTemplate jdbc;

    // CHECK IF USER EXISTS
    public List<UserDetailsDBO> userExists(String email, String query) {
        return jdbc.query(
                query,
                new BeanPropertyRowMapper<>(UserDetailsDBO.class),
                email);
    }

    // ADD USER & RETURN CREATED USER
    public UserDetailsDTO addUser(UserDetailsDBO user, String insertQuery, String getLastUserQuery) {

        jdbc.update(
                insertQuery,
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getStatus(),
                user.getRoleId(),
                user.getPhoneNumber(),
                user.getCountryCode(),
                user.getProfilePictureUrl(),
                user.getHashedPassword(),
                user.getSalt());

        return jdbc.queryForObject(
                getLastUserQuery,
                new BeanPropertyRowMapper<>(UserDetailsDTO.class));
    }

    // USER VALIDATION
    public void validateUser(String email, String validateUserQuery) {
        int rows = jdbc.update(validateUserQuery, email);

        if (rows == 0) {
            throw new IllegalStateException(MessageConstants.NO_USER_EXISTS_FOR_THIS_EMAIL);
        }
    }

    // RESET PASSWORD
    public void resetPassword(UserDetailsDBO user, String resetPasswordQuery) {
        int rows = jdbc.update(
                resetPasswordQuery,
                user.getHashedPassword(),
                user.getSalt(),
                user.getEmail());

        if (rows == 0) {
            throw new IllegalStateException("Unable to reset password — no rows updated");
        }
    }
}
