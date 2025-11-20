package club.sqlhub.entity.user.DTO;

import club.sqlhub.entity.utlities.EmailVerifyDTO;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class RegisterUserDTO {

    private String firstName;

    private String lastName;

    @Email(message = "Invalid Email Format")
    private String email;

    private String status;

    private String phoneNumber;

    private String countryCode;

    private String profilePictureUrl = null;

    private String password;

    private EmailVerifyDTO emailKey;
}
