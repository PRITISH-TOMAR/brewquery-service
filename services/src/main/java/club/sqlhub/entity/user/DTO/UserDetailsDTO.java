package club.sqlhub.entity.user.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailsDTO {
    private Integer userId;

    private String firstName;

    private String lastName;

    private String email;

    private String status;

    private String phoneNumber;

    private String countryCode;

    private String profilePictureUrl;

}
