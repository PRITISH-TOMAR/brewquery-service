package in.brewquery_engine.sql_engine.executor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import in.brewquery_engine.entities.query.SQLQueryResponseDTO;
import in.brewquery_engine.utils.helper.SQLResultMapper;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SQLExecutor {
    @Autowired
    private SQLResultMapper mapper;

    public SQLQueryResponseDTO executeQuery(Connection conn, String query) throws Exception {
        SQLQueryResponseDTO response = new SQLQueryResponseDTO();

        try (PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            List<String> columns = mapper.extractColumns(rs);
            int columnCount = columns.size();

            List<List<Object>> rows = mapper.extractRows(rs, columnCount);

            response.setColumns(columns);
            response.setRows(rows);
            response.setRowsCount(rows.size());

            return response;

        } catch (Exception e) {
            throw e;
        }
    }

}
