package in.brewquery_engine.entities;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.Data;

@Data
public class DatasetSessionDTO {
    private String sessionId;
    private Connection connection;
    private String sqlMode;

    private LocalDateTime createdAt;
    private Map<Integer, String> solutions;

}
