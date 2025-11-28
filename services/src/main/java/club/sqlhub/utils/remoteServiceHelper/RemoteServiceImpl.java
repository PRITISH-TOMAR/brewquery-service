package club.sqlhub.utils.remoteServiceHelper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import club.sqlhub.entity.coreEngine.ExecuteQueryDTO.CompareQueryDTO;
import club.sqlhub.mongo.models.ExpectedSolution;

public class RemoteServiceImpl {
    
    
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
