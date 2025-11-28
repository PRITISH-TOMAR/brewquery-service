package club.sqlhub.controller.remoteController;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.sqlhub.entity.coreEngine.ExecuteQueryDTO.EngineQueryResponseDTO;
import club.sqlhub.entity.coreEngine.ExecuteQueryDTO.QueryRequestInputDTO;
import club.sqlhub.entity.coreEngine.LoadDatasetDTO.LoadDatasetInputDTO;
import club.sqlhub.entity.coreEngine.LoadDatasetDTO.LoadDatasetOutputDTO;
import club.sqlhub.service.remoteService.SQLRemoteService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sql")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class SQLController {

    private final SQLRemoteService service;

    @PostMapping("/load")
    public ResponseEntity<ApiResponse<LoadDatasetOutputDTO>> loadDataset(@RequestBody LoadDatasetInputDTO object) {
        return service.loadDataset(object);
    }

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<EngineQueryResponseDTO>> executeQuery(@RequestBody QueryRequestInputDTO object) {
        return service.executeQuery(object);
    }

}
