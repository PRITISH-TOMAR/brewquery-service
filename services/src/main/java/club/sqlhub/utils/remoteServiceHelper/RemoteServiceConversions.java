package club.sqlhub.utils.remoteServiceHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import club.sqlhub.utils.APiResponse.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RemoteServiceConversions {

    private static final Logger logger = LoggerFactory.getLogger(RemoteServiceConversions.class);
    private final ObjectMapper objectMapper;

    public RemoteServiceConversions(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

   
    public <T> T extract(ApiResponse<Object> apiResponse, Class<T> outputClass) {
        if (apiResponse == null || apiResponse.getData() == null) {
            return createDefaultInstance(outputClass);
        }
        
        try {
            return objectMapper.convertValue(apiResponse.getData(), outputClass);
        } catch (Exception e) {
            logger.error("Failed to convert ApiResponse to {}", outputClass.getSimpleName(), e);
            return createDefaultInstance(outputClass);
        }
    }

    private <T> T createDefaultInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.error("Failed to create default instance of {}", clazz.getSimpleName(), e);
            return null;
        }
    }
}