package in.brewquery_engine.entities;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.Data;

@Data
public class DatasetSessionDTO {
    private String sessionId;
    private String datasetId;
    private String sqlMode;
    private LocalDateTime lastAccessTime;

}
