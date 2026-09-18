package club.sqlhub.mongo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import club.sqlhub.constants.MessageConstants;
import club.sqlhub.entity.Datasets.DatasetPageResponseDTO;
import club.sqlhub.mongo.models.Dataset;
import club.sqlhub.mongo.repository.DatasetRepository;
import club.sqlhub.utils.APiResponse.ApiResponse;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DatasetService {
    private final DatasetRepository repo;

    public ResponseEntity<ApiResponse<DatasetPageResponseDTO>> getAll(int page, int size, String search) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<Dataset> resultPage;

            if (search != null && !search.isBlank()) {
                resultPage = repo.findByTitleContainingIgnoreCase(search, pageable);
            } else {
                resultPage = repo.findAll(pageable);
            }

            DatasetPageResponseDTO dto = new DatasetPageResponseDTO(
                    resultPage.getContent(),
                    resultPage.getNumber(),
                    resultPage.getSize(),
                    resultPage.getTotalElements(),
                    resultPage.getTotalPages());

            return ApiResponse.call(HttpStatus.OK, MessageConstants.OK, dto);

        } catch (Exception e) {
            return ApiResponse.call(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR);
        }
    }
    public ResponseEntity<ApiResponse<Dataset>> getById(String dbId) {
        try {
            Dataset dataset = repo.findById(dbId).orElse(null);
            if (dataset == null) {
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.DATASET_NOT_FOUND);
            }
            return ApiResponse.call(HttpStatus.OK, MessageConstants.OK, dataset);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }
}
