package club.sqlhub.controller.remoteController;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import club.sqlhub.utils.Auth.UserPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.sqlhub.entity.judge.JudgeServerJobDTO.RunTestcaseResponseDTO;
import club.sqlhub.entity.judge.JudgeServerJobDTO.SubmissionStatusResponseDTO;
import club.sqlhub.entity.judge.SQLDTO.SQLInputDTO;
import club.sqlhub.service.remoteService.SQLRemoteService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sql")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class SQLController {

    private final SQLRemoteService service;

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<RunTestcaseResponseDTO>> runQuery(@RequestBody SQLInputDTO input, @AuthenticationPrincipal UserPrincipal user) {
        return service.runQuery(input, user.getUserId());
    }

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<SubmissionStatusResponseDTO>> executeQuery(
            @RequestBody SQLInputDTO object, @AuthenticationPrincipal UserPrincipal user) {
        return service.executeQuery(object, user.getUserId());
    }
}
