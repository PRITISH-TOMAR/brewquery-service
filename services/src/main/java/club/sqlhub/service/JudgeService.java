package club.sqlhub.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import club.sqlhub.mongo.models.JudgeResult.JudgeResultDTO;
import club.sqlhub.mongo.service.UserQueriesResultService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class JudgeService {
    private final UserQueriesResultService userQueriesResultService;

    public ResponseEntity<ApiResponse<JudgeResultDTO>> expectedOutput(String jobId) {
        try {
            JudgeResultDTO jobResultResponseDTO = userQueriesResultService.findById(jobId);

            return ApiResponse.call(
                    HttpStatus.OK,
                    "Job result fetched successfully",
                    jobResultResponseDTO);
        } catch (Exception ex) {
            return ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal error while fetching job result",
                    ex);
        }
    }

    public ResponseEntity<ApiResponse<List<JudgeResultDTO>>> getUserResults(String userId) {
        try {
            List<JudgeResultDTO> jobResultResponseDTO = userQueriesResultService.findByUserId(userId);

            return ApiResponse.call(
                    HttpStatus.OK,
                    "Job result fetched successfully",
                    jobResultResponseDTO);
        } catch (Exception ex) {
            return ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal error while fetching job result",
                    ex);
        }
    }
}
