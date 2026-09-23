package club.sqlhub.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.sqlhub.entity.judge.SubmissionRequestDTO;
import club.sqlhub.entity.judge.JudgeServerJobDTO.SubmissionResponseDTO;
import club.sqlhub.service.JudgeService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.Auth.UserPrincipal;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/result/")
@AllArgsConstructor
public class JudgeController {
    private final JudgeService service;

    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<SubmissionResponseDTO>> executedExpectedQuery(
            @PathVariable String jobId) {
        return service.expectedOutput(jobId);
    }

    @PostMapping("submissions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<SubmissionResponseDTO>>> findSubmissionsPerUserByFilters(
            @AuthenticationPrincipal UserPrincipal user, @RequestBody SubmissionRequestDTO req) {
        req.setUserId(user.getUserId());
        return service.findSubmissionsPerUserByFilters(req);
    }

}