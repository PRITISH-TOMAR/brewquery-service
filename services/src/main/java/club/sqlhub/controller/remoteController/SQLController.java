package club.sqlhub.controller.remoteController;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.sqlhub.entity.coreEngine.JudgeServerJobDTO.SubmissionStatusResponseDTO;
import club.sqlhub.entity.coreEngine.SQLDTO.SQLInputDTO;
import club.sqlhub.service.remoteService.SQLRemoteService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sql")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class SQLController {

    private final SQLRemoteService service;

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<SubmissionStatusResponseDTO>> executeQuery(
            @RequestBody SQLInputDTO object, @AuthenticationPrincipal UserDetails user) {
        return service.executeQuery(object, user.getUsername());
    }

    // @PostMapping("/expected")
    // public ResponseEntity<ApiResponse<EngineQueryResponseDTO>>
    // executedExpectedQuery(
    // @RequestBody ExpectedOutputRequestDTO object, @AuthenticationPrincipal
    // UserDetails user) {
    // return service.expectedOutput(object, user.getUsername());
    // }

}
