package club.sqlhub.entity.user.DBO;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRawProfileDBO {
    private Integer userId;
    private String  firstName;
    private String  lastName;
    private String  email;
    private String  phoneNumber;
    private String  countryCode;
    private String  profilePictureUrl;
    private String  bio;
    private String  location;
    private Timestamp createdAt;
    private String  roleName;
}
