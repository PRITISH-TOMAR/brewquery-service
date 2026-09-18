package in.brewquery_engine.sql_engine.loader;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SqlBatchExecutor {

    public void run(Connection conn, String statements) throws Exception {

        try (Statement stmt = conn.createStatement()) {
            System.out.println("EXECUTING: " + statements);
            stmt.execute(statements);
        }
        
    }

}
