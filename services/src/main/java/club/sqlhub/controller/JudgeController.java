package club.sqlhub.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.sqlhub.entity.coreEngine.JudgeServerJobDTO.SubmissionResponseDTO;
import club.sqlhub.mongo.models.JudgeResult.JudgeResultDTO;
import club.sqlhub.service.JudgeService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/result/")
@AllArgsConstructor
public class JudgeController {
    private final JudgeService service;

    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JudgeResultDTO>> executedExpectedQuery(
            @PathVariable String jobId) {
        return service.expectedOutput(jobId);
    }

    @GetMapping("user/{userId}")
    public ResponseEntity<ApiResponse<List<SubmissionResponseDTO>>> getUserResults(
            @PathVariable String userId) {
        return service.getUserResults(userId);
    }

}