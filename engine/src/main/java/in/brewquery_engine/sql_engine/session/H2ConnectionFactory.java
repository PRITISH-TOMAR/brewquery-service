package in.brewquery_engine.sql_engine.session;

import java.sql.Connection;
import java.sql.DriverManager;

import org.springframework.stereotype.Component;

@Component
public class H2ConnectionFactory {
    public Connection createConnection(String sessionId) throws Exception{
        String url  = "jdbc:h2:mem:" + sessionId + ";DB_CLOSE_DELAY=-1;MODE=MySQL";
        return DriverManager.getConnection(url, "user", "password");
    }
}
