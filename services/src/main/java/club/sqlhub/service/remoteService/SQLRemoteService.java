package club.sqlhub.service.remoteService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import club.sqlhub.Repository.remoteRepository.SQLRemoteRepository;
import club.sqlhub.entity.coreEngine.ExecuteQueryDTO.CompareQueryDTO;
import club.sqlhub.entity.coreEngine.ExecuteQueryDTO.EngineQueryRequestDTO;
import club.sqlhub.entity.coreEngine.ExecuteQueryDTO.EngineQueryResponseDTO;
import club.sqlhub.entity.coreEngine.ExecuteQueryDTO.QueryRequestInputDTO;
import club.sqlhub.entity.coreEngine.LoadDatasetDTO.LoadDatasetInputDTO;
import club.sqlhub.entity.coreEngine.LoadDatasetDTO.LoadDatasetOutputDTO;
import club.sqlhub.mongo.models.ExpectedSolution;
import club.sqlhub.mongo.models.Question;
import club.sqlhub.mongo.service.ExpectedSolutionService;
import club.sqlhub.mongo.service.QuestionService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.remoteServiceHelper.RemoteServiceImpl;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SQLRemoteService {

    private final ExpectedSolutionService expectedSolutionService;
    private final QuestionService questionService;
    private final SQLRemoteRepository repository;

    public ResponseEntity<ApiResponse<LoadDatasetOutputDTO>> loadDataset(LoadDatasetInputDTO obj, String userName) {
        LoadDatasetOutputDTO payload = repository.outputFromLoadDataset(obj, userName);
        return ApiResponse.call(HttpStatus.CREATED, "OK", payload);
    }

    public ResponseEntity<ApiResponse<EngineQueryResponseDTO>> executeQuery(QueryRequestInputDTO obj) {

        // 1) Validate Question
        Question question = questionService.getById(obj.getQuestionId());
        if (question == null) {
            return ApiResponse.call(HttpStatus.BAD_REQUEST, "Invalid questionId");
        }

        // 2) Fetch ExpectedSolution document
        ExpectedSolution expected = expectedSolutionService.getOne(obj.getQuestionId(), obj.getSqlMode());
        if (expected == null) {
            return ApiResponse.call(HttpStatus.BAD_REQUEST, "No expected solution found");
        }

        String type = question.getType();

        // 3) Ensure all expectedEntries have expectedOutput + resultHash
        ensureExpectedData(expected, obj.getSessionId(), type);
        expectedSolutionService.save(expected);

        // 4) Run user query
        EngineQueryRequestDTO payload = new EngineQueryRequestDTO(
                obj.getSessionId(),
                obj.getQuery(),
                type);

        EngineQueryResponseDTO userRes = repository.outputFromSQLquery(type, payload);
        if (userRes.getStatus() != 200) {
            return ApiResponse.call(HttpStatus.INTERNAL_SERVER_ERROR, "Error executing user query", userRes);
        }

        // 5) Convert user result to CompareQueryDTO
        CompareQueryDTO actualDTO = repository.toCompareDTO(userRes);

        // 6) Compare actual hash to expected hashes
        boolean correct = RemoteServiceImpl.matchActualWithExpected(actualDTO, expected);

        userRes.setValid(correct);

        // 7) Final Response
        return ApiResponse.call(
                HttpStatus.OK,
                correct ? "CORRECT" : "WRONG",
                userRes);
    }

    private void ensureExpectedData(ExpectedSolution expected, String sessionId, String type) {

        for (ExpectedSolution.SolutionEntry entry : expected.getSolutions()) {

            // 1) Fetch expectedOutput first time only
            if (entry.getResultHash() == null) {

                EngineQueryResponseDTO out = repository.outputFromSQLquery(
                        type,
                        new EngineQueryRequestDTO(sessionId, entry.getSolutionQuery(), type));

                if (out != null) {
                    ExpectedSolution.OutputData data = new ExpectedSolution.OutputData();
                    data.setColumns(out.getColumns());
                    data.setRows(out.getRows());
                    entry.setExpectedOutput(data);
                }
            }

            // 2) Generate resultHash using CompareQueryDTO
            if (entry.getResultHash() == null && entry.getExpectedOutput() != null) {
                CompareQueryDTO dto = repository.toCompareDTO(entry.getExpectedOutput());
                String hash = RemoteServiceImpl.hashOutput(dto);
                entry.setResultHash(hash);
            }
        }
    }

}
