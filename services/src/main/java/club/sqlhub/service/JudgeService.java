package club.sqlhub.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import club.sqlhub.entity.coreEngine.JudgeServerJobDTO.SubmissionResponseDTO;
import club.sqlhub.mongo.models.JudgeResult.JudgeResultDTO;
import club.sqlhub.mongo.service.UserQueriesResultService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.converter.JudgeResponseConverter;
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

    public ResponseEntity<ApiResponse<List<SubmissionResponseDTO>>> getUserResults(String userId) {
        try {
            List<JudgeResultDTO> jobResultResponseDTO = userQueriesResultService.findByUserId(userId);

            List<SubmissionResponseDTO> submissionResponseDTO = jobResultResponseDTO.stream()
                    .map(JudgeResponseConverter::convertJudgeResultToSubmissionResponse)
                    .toList();

            return ApiResponse.call(
                    HttpStatus.OK,
                    "Job result fetched successfully",
                    submissionResponseDTO);
        } catch (Exception ex) {
            return ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal error while fetching job result",
                    ex);
        }
    }
}
