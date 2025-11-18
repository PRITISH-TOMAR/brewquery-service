package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class UserQueries {

        public final String IF_USER_EXISTS = """
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
                        WHERE email = ?
                        """;
        public final String INSERT_USER_DETAILS = """
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

        public final String GET_LAST_INSERTED_USER = """
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

}
