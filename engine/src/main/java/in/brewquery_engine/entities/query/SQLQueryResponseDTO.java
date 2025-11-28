package in.brewquery_engine.entities.query;

import java.util.List;

import lombok.Data;

@Data
public class SQLQueryResponseDTO {
    private List<String> columns;
    private List<List<Object>> rows;
    private Integer rowsCount;
}
