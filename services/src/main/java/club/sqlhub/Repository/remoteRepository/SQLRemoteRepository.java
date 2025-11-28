package club.sqlhub.Repository.remoteRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import club.sqlhub.constants.AppConstants;
import club.sqlhub.entity.coreEngine.ExecuteQueryDTO.CompareQueryDTO;
import club.sqlhub.entity.coreEngine.ExecuteQueryDTO.EngineQueryRequestDTO;
import club.sqlhub.entity.coreEngine.ExecuteQueryDTO.EngineQueryResponseDTO;
import club.sqlhub.entity.coreEngine.LoadDatasetDTO.LoadDatasetInputDTO;
import club.sqlhub.entity.coreEngine.LoadDatasetDTO.LoadDatasetOutputDTO;
import club.sqlhub.mongo.models.ExpectedSolution;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.remoteServiceHelper.RemoteServiceConversions;
import club.sqlhub.utils.remoteServiceHelper.SQLRemoteApiHelper;
import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class SQLRemoteRepository {
    private final SQLRemoteApiHelper apiHelper;
    private final RemoteServiceConversions conversions;

    public LoadDatasetOutputDTO outputFromLoadDataset(LoadDatasetInputDTO obj) {
        ResponseEntity<ApiResponse<Object>> response = apiHelper.post(AppConstants.LOAD_DATASET, obj);

        LoadDatasetOutputDTO output = conversions.extract(response.getBody(), LoadDatasetOutputDTO.class);

        output.setMessage(response.getBody().getMessage());
        output.setStatus(response.getStatusCode());
        return output;

    }

    public EngineQueryResponseDTO outputFromSQLquery(String type, EngineQueryRequestDTO payload) {

        ResponseEntity<ApiResponse<Object>> response = apiHelper.post(AppConstants.EXECUTE_SQL, payload);

        EngineQueryResponseDTO output = conversions.extract(response.getBody(), EngineQueryResponseDTO.class);
        output.setMessage(response.getBody().getMessage());
        output.setStatus(response.getStatusCode().value());
        return output;

    }

     public CompareQueryDTO toCompareDTO(EngineQueryResponseDTO res) {
        CompareQueryDTO dto = new CompareQueryDTO();
        dto.setColumns(res.getColumns());
        dto.setRows(res.getRows());
        dto.setRowsCount(res.getRows() == null ? 0 : res.getRows().size());
        return dto;
    }

    public CompareQueryDTO toCompareDTO(ExpectedSolution.OutputData data) {
        CompareQueryDTO dto = new CompareQueryDTO();
        dto.setColumns(data.getColumns());
        dto.setRows(data.getRows());
        dto.setRowsCount(data.getRows() == null ? 0 : data.getRows().size());
        return dto;
    }
}
