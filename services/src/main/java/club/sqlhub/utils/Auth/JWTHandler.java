package club.sqlhub.utils.Auth;

import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import club.sqlhub.constants.AppConstants;
import club.sqlhub.entity.user.DBO.UserDetailsDBO;
import club.sqlhub.entity.user.DTO.UserDetailsDTO;
import club.sqlhub.entity.utlities.TokenDBO;
import club.sqlhub.entity.utlities.UserJWTDetailsDBO;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JWTHandler {

    private final Dotenv dotenv;
    private final String JWT_SECRET_KEY;

    public JWTHandler(Dotenv dotenv) {
        this.dotenv = dotenv;
        this.JWT_SECRET_KEY = dotenv.get("JWT_SECRET_KEY");
    }

    private SecretKey getSigningKey() {
        String secret = JWT_SECRET_KEY;
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured");
        }
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateToken(String subject, Map<String, Object> extraClaims) {

        var claimsBuilder = Jwts.claims().subject(subject);

        if (extraClaims != null && !extraClaims.isEmpty()) {
            claimsBuilder.add(extraClaims);
        }

        var claims = claimsBuilder.build();

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + AppConstants.ACCESS_TOKEN_TTL_MS))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    private String generateToken(String subject) {
        return generateToken(subject, null);
    }

    // Refresh token generation (longer TTL)
    private String generateRefreshToken(String subject) {

        var claims = Jwts.claims()
                .subject(subject)
                .build();

        Date now = new Date();
        Date exp = new Date(now.getTime() + AppConstants.REFRESH_TOKEN_TTL_MS);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(exp)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // Validate token signature and expiration. Returns true if token valid.
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public String extractSubject(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UserJWTDetailsDBO buildUserJWTDetails(String subject, UserDetailsDBO u) {

        TokenDBO tokenDBO = new TokenDBO();
        tokenDBO.setAccessToken(generateToken(subject));
        tokenDBO.setRefreshToken(generateRefreshToken(subject));

        UserDetailsDTO dto = new UserDetailsDTO();
        dto.setUserId(u.getUserId());
        dto.setFirstName(u.getFirstName());
        dto.setLastName(u.getLastName());
        dto.setEmail(u.getEmail());
        dto.setStatus(u.getStatus());
        dto.setPhoneNumber(u.getPhoneNumber());
        dto.setCountryCode(u.getCountryCode());
        dto.setProfilePictureUrl(u.getProfilePictureUrl());

        UserJWTDetailsDBO jwtUser = new UserJWTDetailsDBO();
        jwtUser.setTokenDetails(tokenDBO);
        jwtUser.setUser(dto);

        return jwtUser;
    }

}
