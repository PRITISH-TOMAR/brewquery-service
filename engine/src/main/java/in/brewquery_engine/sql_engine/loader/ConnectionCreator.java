package in.brewquery_engine.sql_engine.loader;

import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import in.brewquery_engine.sql_engine.session.H2ConnectionFactory;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class ConnectionCreator {
    private final H2ConnectionFactory h2;
    private static final Logger log = LoggerFactory.getLogger(DatasetFetcher.class);

    public Connection create(String sessionId, String sqlMode) {
        try {
            return h2.createConnection(sessionId, sqlMode);
        } catch (Exception e) {
            log.error("Failed to create connection", e);
            return null;
        }
    }

    public void safeClose(Connection con) {
        if (con != null) {
            try {
                con.close();

            } catch (Exception ignored) {

            }
        }
    }
}
