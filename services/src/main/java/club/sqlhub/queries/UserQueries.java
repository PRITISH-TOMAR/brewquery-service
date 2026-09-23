package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class UserQueries {

    public final String IF_USER_EXISTS = """
                    SELECT
                    ud.user_id AS userId,
                    ud.first_name AS firstName,
                    ud.last_name AS lastName,
                    ud.email AS email,
                    ud.role_id AS roleId,
                    ur.role_name AS roleName,
                    ud.phone_number AS phoneNumber,
                    ud.status AS status,
                    ud.country_code AS countryCode,
                    ud.profile_picture_url AS profilePictureUrl,
                    ud.hashed_password AS hashedPassword,
                    ud.salt AS salt
                    FROM user_details ud
                    JOIN user_roles ur ON ur.role_id = ud.role_id
                    WHERE ud.email = ?
                    """;

    // USER INSERT — returns the created row directly
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
                    RETURNING
                        user_id AS userId,
                        first_name AS firstName,
                        last_name AS lastName,
                        email AS email,
                        role_id AS roleId,
                        phone_number AS phoneNumber,
                        status AS status,
                        country_code AS countryCode,
                        profile_picture_url AS profilePictureUrl
                    """;

}
