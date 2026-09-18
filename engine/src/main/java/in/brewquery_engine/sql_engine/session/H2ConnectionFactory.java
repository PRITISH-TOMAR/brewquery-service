package in.brewquery_engine.sql_engine.session;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Component
public class H2ConnectionFactory {

    public Connection createConnection(String sessionId, String mode) throws SQLException {

        String normalizedMode = normalizeMode(mode);

        String url = "jdbc:h2:mem:" + sessionId +
                ";MODE=" + normalizedMode +
                ";DB_CLOSE_DELAY=0" +
                ";DB_CLOSE_ON_EXIT=TRUE" +
                ";TRACE_LEVEL_FILE=0" +
                ";TRACE_LEVEL_SYSTEM_OUT=0"+
                ";DATABASE_TO_UPPER=false";

        Properties props = new Properties();
        props.setProperty("user", "sa");
        props.setProperty("password", "");

        return DriverManager.getConnection(url, props);
    }

    private String normalizeMode(String mode) {
        if (mode == null) return "MySQL";

        switch (mode.toUpperCase()) {
            case "MYSQL": return "MySQL";
            case "POSTGRES": 
            case "POSTGRESQL": return "PostgreSQL";
            case "ORACLE": return "Oracle";
            case "MSSQL":
            case "SQLSERVER": return "MSSQLServer";
            default: return "MySQL"; 
        }
    }
}
