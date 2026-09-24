package club.sqlhub.utils.Auth;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailVerificationTokenHandler {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final String DELIMITER = "|";

    private final Dotenv dotenv;

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(dotenv.get("AES_SECRET_KEY"));
        return new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String email, long expiresAt) {
        try {
            String payload = email + DELIMITER + expiresAt;
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[GCM_IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, GCM_IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, GCM_IV_LENGTH, encrypted.length);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt token", e);
        }
    }

    public String[] decrypt(String token) {
        try {
            byte[] combined = Base64.getUrlDecoder().decode(token);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            String payload = new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);

            return payload.split("\\" + DELIMITER, 2);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt token", e);
        }
    }
}
