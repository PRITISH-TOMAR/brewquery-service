package club.sqlhub.entity.coreEngine;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatusCode;

import lombok.Data;

public class LoadDatasetDTO {
    @Data
    public static class LoadDatasetInputDTO {
        private String sessionId;
        private String datasetId;
        private String sqlMode;
    }

    @Data
    public static class LoadDatasetOutputDTO {
        private String sessionId;
        private String datasetId;
        private String sqlMode;
        private LocalDateTime lastAccessTime;
        private String message;
        private HttpStatusCode status;
    }
}
