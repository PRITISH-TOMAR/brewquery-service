package club.sqlhub.utils.remoteServiceHelper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import club.sqlhub.entity.coreEngine.JudgeServerJobDTO.CompareQueryDTO;
import club.sqlhub.mongo.models.ExpectedSolution;

public class RemoteServiceImpl {

    private static String hashUserName(String userName) {

        return DigestUtils.sha256Hex(userName.getBytes(StandardCharsets.UTF_8));

    }

    public static String hashSessionId(String userName, String datasetId) {
        String hashedUserName = hashUserName(userName);
        long timestamp = Instant.now().toEpochMilli();

        String combined = hashedUserName + ":" + datasetId + ":" + timestamp;

        return hashString(combined);
    }

    private static String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convert to Base64 for shorter, URL-safe string
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static boolean matchActualWithExpected(CompareQueryDTO actual, ExpectedSolution expected) {

        String actualHash = hashOutput(actual);
        if (actualHash == null)
            return false;

        for (ExpectedSolution.SolutionEntry entry : expected.getSolutions()) {
            if (actualHash.equals(entry.getResultHash())) {
                return true;
            }
        }
        return false;
    }

    public static String hashOutput(CompareQueryDTO data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(data);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(json.getBytes(StandardCharsets.UTF_8));

            return DigestUtils.sha256Hex(digest);

        } catch (Exception e) {
            return null;
        }
    }
}
