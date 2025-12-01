package club.sqlhub.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import club.sqlhub.entity.Datasets.DatasetPageResponseDTO;
import club.sqlhub.mongo.models.Dataset;
import club.sqlhub.mongo.models.Question;
import club.sqlhub.mongo.service.DatasetService;
import club.sqlhub.mongo.service.QuestionService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/db/sql/")
@PreAuthorize("hasRole('USER')")
public class SQLmongoController {
    private final DatasetService datasetService;
    private final QuestionService questionService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<DatasetPageResponseDTO>> getDatabases(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return datasetService.getAll(page, size, search);
    }

    @GetMapping("/{dbId}")
    public ResponseEntity<ApiResponse<Dataset>> getDatasetbyId(@PathVariable String dbId) {
        return datasetService.getById(dbId);
    }

    @GetMapping("/{dbId}/problems")
    public ResponseEntity<ApiResponse<List<Question>>> getProblemsbyDbId(@PathVariable String dbId) {
        return questionService.getByDataset(dbId);
    }
}
