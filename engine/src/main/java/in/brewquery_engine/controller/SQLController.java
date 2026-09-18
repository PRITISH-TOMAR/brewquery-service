package in.brewquery_engine.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.brewquery_engine.entities.SessionDTO;
import in.brewquery_engine.entities.query.SQLQueryRequestDTO;
import in.brewquery_engine.entities.query.SQLQueryResponseDTO;
import in.brewquery_engine.entities.DatasetSessionDTO;
import in.brewquery_engine.sql_engine.loader.DatasetLoaderService;
import in.brewquery_engine.utils.APiResponse.ApiResponse;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("engine/sql")
@AllArgsConstructor
public class SQLController {
    private final DatasetLoaderService loaderService;

    @PostMapping("/load")
    public ResponseEntity<ApiResponse<DatasetSessionDTO>> loadDataset(
            @Validated @RequestBody SessionDTO req) {
        return loaderService.loadDataset(req);
    }

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<SQLQueryResponseDTO>> executeQuery(
            @Validated @RequestBody SQLQueryRequestDTO req) {
        return loaderService.execute(req);
    }

}
