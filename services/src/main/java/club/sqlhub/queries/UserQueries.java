package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class UserQueries {

    public final String IF_USER_EXISTS_BY_ID = """
                    SELECT
                    user_id AS userId,
                    first_name AS firstName,
                    last_name AS lastName,
                    email AS email,
                    role AS role,
                    phone_number AS phoneNumber,
                    status AS status,
                    country_code AS countryCode,
                    profile_picture_url AS profilePictureUrl,
                    hashed_password AS hashedPassword,
                    salt AS salt
                    FROM user_details
                    WHERE user_id = ?
                    """;

    public final String IF_USER_EXISTS = """
                    SELECT
                    user_id AS userId,
                    first_name AS firstName,
                    last_name AS lastName,
                    email AS email,
                    role AS role,
                    phone_number AS phoneNumber,
                    status AS status,
                    country_code AS countryCode,
                    profile_picture_url AS profilePictureUrl,
                    hashed_password AS hashedPassword,
                    salt AS salt
                    FROM user_details
                    WHERE email = ?
                    """;

    // USER INSERT — returns the created row directly
    public final String INSERT_USER_DETAILS = """
                    INSERT INTO user_details
                    (
                        first_name,
                        last_name,
                        email,
                        status,
                        role,
                        phone_number,
                        country_code,
                        profile_picture_url,
                        hashed_password,
                        salt
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    RETURNING
                        user_id AS userId,
                        first_name AS firstName,
                        last_name AS lastName,
                        email AS email,
                        role AS role,
                        phone_number AS phoneNumber,
                        status AS status,
                        country_code AS countryCode,
                        profile_picture_url AS profilePictureUrl
                    """;

}
