package in.brewquery_engine.sql_engine.judge;

import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import in.brewquery_engine.entities.judge.TestCaseRequest;
import in.brewquery_engine.entities.judge.TestCaseResult;
import in.brewquery_engine.entities.query.SQLQueryResponseDTO;
import in.brewquery_engine.sql_engine.executor.SQLExecutor;
import in.brewquery_engine.sql_engine.loader.SqlBatchExecutor;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class TestCaseExecutor {

    private static final Logger log = LoggerFactory.getLogger(TestCaseExecutor.class);

    private final EphemeralConnectionFactory connectionFactory;
    private final SqlBatchExecutor batchExecutor;
    private final SQLExecutor sqlExecutor;
    private final ResultComparator comparator;

    public TestCaseResult execute(TestCaseRequest tc, String userSql, String expectedSql, String sqlMode) {
        TestCaseResult result = new TestCaseResult();
        result.setTestCaseId(tc.getId());

        Connection conn = null;
        try {
            conn = connectionFactory.create(tc.getId(), sqlMode);

            batchExecutor.run(conn, tc.getSchemaSql());
            batchExecutor.run(conn, tc.getSeedSql());

            SQLQueryResponseDTO userOutput = sqlExecutor.executeQuery(conn, userSql);
            SQLQueryResponseDTO expectedOutput = sqlExecutor.executeQuery(conn, expectedSql);

            boolean passed = comparator.compare(userOutput, expectedOutput, tc.getNumericTolerance());

            result.setUserOutput(userOutput);
            result.setExpectedOutput(expectedOutput);
            result.setPassed(passed);

        } catch (Exception ex) {
            log.error("TestCase [{}] failed with error: {}", tc.getId(), ex.getMessage());
            result.setPassed(false);
            result.setError(ex.getMessage());
        } finally {
            connectionFactory.safeClose(conn);
        }

        return result;
    }
}
