package club.sqlhub.utils.User;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Component;

import club.sqlhub.constants.AppConstants;
import club.sqlhub.entity.user.DBO.UserDetailsDBO;
import club.sqlhub.entity.user.DTO.RegisterUserDTO;

@Component
public class UserHandler {

    public UserDetailsDBO convertRegisterDtoToDbo(RegisterUserDTO regUser) {
        UserDetailsDBO dbo = new UserDetailsDBO();

        dbo.setFirstName(regUser.getFirstName());
        dbo.setLastName(regUser.getLastName());
        dbo.setEmail(regUser.getEmail());
        dbo.setPhoneNumber(regUser.getPhoneNumber());
        dbo.setCountryCode(regUser.getCountryCode());
        dbo.setProfilePictureUrl(regUser.getProfilePictureUrl());
        return dbo;
    }

    public String generateSalt() {
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public String hashPassword(String password, String salt) {
        try {
            int iterations = 65535;
            int keyLength = 256;
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    Base64.getDecoder().decode(salt),
                    iterations,
                    keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(AppConstants.PASSWORD_ALGO_KEY);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) {
            throw new RuntimeException("", ex);
        }
    }

    public boolean validPassword(String password, String hashPassword, String salt) {
        String calculatedHash = hashPassword(password, salt);
        return calculatedHash.equals(hashPassword);
    }

}
