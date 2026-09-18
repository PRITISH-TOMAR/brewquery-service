package club.sqlhub.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import club.sqlhub.entity.judge.JudgeServerJobDTO.SubmissionResponseDTO;
import club.sqlhub.entity.judge.SubmissionRequestDTO;
import club.sqlhub.mongo.models.JudgeResult.JudgeResultDTO;
import club.sqlhub.mongo.service.UserQueriesResultService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.converter.JudgeResponseConverter;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class JudgeService {
    private final UserQueriesResultService userQueriesResultService;

    public ResponseEntity<ApiResponse<SubmissionResponseDTO>> expectedOutput(String jobId) {
        try {
            JudgeResultDTO jobResultResponseDTO = userQueriesResultService.findById(jobId);
            SubmissionResponseDTO submissionResponseDTO = JudgeResponseConverter
                    .convertJudgeResultToSubmissionResponse(jobResultResponseDTO);
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

    public ResponseEntity<ApiResponse<List<SubmissionResponseDTO>>> findSubmissionsPerUserByFilters(SubmissionRequestDTO req) {
        try {
            Page<SubmissionResponseDTO> judgeResultDTO = userQueriesResultService.findByFilters(req);

            List<SubmissionResponseDTO> list = judgeResultDTO.getContent();
            return ApiResponse.call(
                    HttpStatus.OK,
                    "Submissions fetched successfully",
                    list);
        } catch (Exception ex) {
            return ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal error while fetching submissions",
                    ex);
        }
    }

}
