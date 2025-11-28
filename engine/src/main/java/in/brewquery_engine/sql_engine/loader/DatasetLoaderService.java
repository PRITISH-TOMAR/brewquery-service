package in.brewquery_engine.sql_engine.loader;

import java.sql.Connection;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import in.brewquery_engine.constants.MessageConstants;
import in.brewquery_engine.entities.CachedDatasetDTO;
import in.brewquery_engine.entities.DatasetSessionDTO;
import in.brewquery_engine.entities.SessionDTO;
import in.brewquery_engine.entities.query.SQLQueryRequestDTO;
import in.brewquery_engine.entities.query.SQLQueryResponseDTO;
import in.brewquery_engine.sql_engine.executor.SQLExecutor;
import in.brewquery_engine.sql_engine.session.SessionManager;
import in.brewquery_engine.sql_engine.validator.SafeQueryValidator;
import in.brewquery_engine.utils.APiResponse.ApiResponse;
import in.brewquery_engine.utils.helper.Conversions;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DatasetLoaderService {

    private final DatasetFetcher datasetFetcher;
    private final ConnectionCreator connectionCreator;
    private final SqlBatchExecutor sqlBatchExecutor;
    private final SessionManager sessionManager;
    private final SQLExecutor sqlExecutor;

    public ResponseEntity<ApiResponse<DatasetSessionDTO>> loadDataset(SessionDTO req) {
        String sessionId = req.getSessionId();
        String datasetId = req.getDatasetId();
        String sqlMode = req.getSqlMode();

        CachedDatasetDTO cached = datasetFetcher.fetch(datasetId);
        if (cached == null) {
            return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.NO_DATASET_FOUND);
        }

        // Session already Exists
        DatasetSessionDTO session = Conversions.SessionDTOtoDatasetSessionDTO(req);
        if (sessionManager.refreshSession(session)) {
            return ApiResponse.call(HttpStatus.OK, MessageConstants.SESSION_REFRESHED, session);
        }
        sessionManager.deleteSession(sessionId);
        // create NEW session
        Connection conn = connectionCreator.create(sessionId, sqlMode);
        if (conn == null) {
            return ApiResponse.call(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR);
        }

        try {

            sqlBatchExecutor.run(conn, cached.getSchemaStatements());
            sqlBatchExecutor.run(conn, cached.getInsertStatements());

            sessionManager.saveSession(sessionId, datasetId, conn);
            session.setLastAccessTime(LocalDateTime.now());
            return ApiResponse.call(
                    HttpStatus.OK,
                    MessageConstants.SESSION_CREATED_SUCCESSFULLY,
                    session);
        } catch (Exception ex) {
            connectionCreator.safeClose(conn);
            sessionManager.deleteSession(sessionId);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, ex);
        }
    }

    public ResponseEntity<ApiResponse<SQLQueryResponseDTO>> execute(SQLQueryRequestDTO req) {

        try {
            String sessionId = req.getSessionId();
            String query = req.getQuery();

            DatasetSessionDTO session = new DatasetSessionDTO();
            session.setSessionId(sessionId);

            if (!sessionManager.refreshSession(session)) {
                sessionManager.deleteSession(sessionId);
                return ApiResponse.call(HttpStatus.GONE, MessageConstants.SESSION_EXPIRED);
            }

            Connection conn = sessionManager.getConnection(sessionId);
            if (conn == null) {
                return ApiResponse.call(HttpStatus.GONE, MessageConstants.SESSION_EXPIRED);
            }

            boolean safe = SafeQueryValidator.validateQuery(query, req.getType());

            if (!safe) {
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.UNSAFE_QUERY);
            }
            SQLQueryResponseDTO result = sqlExecutor.executeQuery(conn, query);
            session.setLastAccessTime(LocalDateTime.now());

            return ApiResponse.call(HttpStatus.OK, MessageConstants.QUERY_RUN_SUCCESSFULL, result);
        } catch (Exception ex) {
            return ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.INTERNAL_SERVER_ERROR,
                    ex);
        }
    }
}