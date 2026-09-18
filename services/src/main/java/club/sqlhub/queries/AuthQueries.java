package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class AuthQueries {

    // USER SELECT QUERIES
    public  final String IF_USER_EXISTS = """
            SELECT
                user_id AS userId,
                first_name AS firstName,
                last_name AS lastName,
                email AS email,
                role_id AS roleId,
                phone_number AS phoneNumber,
                status AS status,
                country_code AS countryCode,
                profile_picture_url AS profilePictureUrl,
                hashed_password AS hashedPassword,
                salt AS salt
            FROM user_details
            WHERE email = ?
            """;

    public  final String GET_LAST_INSERTED_USER = """
            SELECT
                user_id AS userId,
                first_name AS firstName,
                last_name AS lastName,
                email AS email,
                role_id AS roleId,
                phone_number AS phoneNumber,
                status AS status,
                country_code AS countryCode,
                profile_picture_url AS profilePictureUrl
            FROM user_details
            WHERE user_id = LAST_INSERT_ID()
            """;

    // USER INSERT
    public  final String INSERT_USER_DETAILS = """
            INSERT INTO user_details
            (
                first_name,
                last_name,
                email,
                status,
                role_id,
                phone_number,
                country_code,
                profile_picture_url,
                hashed_password,
                salt
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    // PASSWORD RESET 
    public  final String RESET_PASSWORD_QUERY = """
            UPDATE user_details
            SET
                hashed_password = ?,
                salt = ?
            WHERE email = ?
            """;
}
