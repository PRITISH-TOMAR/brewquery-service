package in.brewquery_engine.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.brewquery_engine.entities.judge.JudgeJobPayload;
import in.brewquery_engine.entities.judge.RunTestcaseResponseDTO;
import in.brewquery_engine.service.JobsService;
import in.brewquery_engine.utils.APiResponse.ApiResponse;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/jobs")
@AllArgsConstructor
public class JobsController {

    private final JobsService jobsService;

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<RunTestcaseResponseDTO>> runPublicTestCases(
            @RequestBody JudgeJobPayload req) {
        return jobsService.runPublicTestCases(req);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RunTestcaseResponseDTO>> submitQuery(
            @RequestBody JudgeJobPayload req) {
        return jobsService.submitQuery(req);
    }
}
