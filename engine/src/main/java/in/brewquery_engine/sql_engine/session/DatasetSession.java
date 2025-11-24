package in.brewquery_engine.sql_engine.session;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.Data;

@Data
public class DatasetSession {
    private String sessionId;
    private Connection connection;
    private LocalDateTime createdAt;
    private Map< Integer, String> solutions;
}
