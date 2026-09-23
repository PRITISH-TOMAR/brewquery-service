package club.sqlhub.mongo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import club.sqlhub.Repository.DatasetSQLRepository;
import club.sqlhub.constants.MessageConstants;
import club.sqlhub.entity.Datasets.DatasetPageResponseDTO;
import club.sqlhub.mongo.models.Dataset;
import club.sqlhub.utils.APiResponse.ApiResponse;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DatasetService {

    private final DatasetSQLRepository repo;

    public ResponseEntity<ApiResponse<DatasetPageResponseDTO>> getAll(int page, int size, String search) {
        try {
            int limit = size;
            int offset = page * size;

            List<Dataset> content;
            long total;

            if (search != null && !search.isBlank()) {
                content = repo.findByTitlePaged(search, limit, offset);
                total   = repo.countByTitle(search);
            } else {
                content = repo.findAllPaged(limit, offset);
                total   = repo.countAll();
            }

            int totalPages = (int) Math.ceil((double) total / size);
            DatasetPageResponseDTO dto = new DatasetPageResponseDTO(content, page, size, total, totalPages);
            return ApiResponse.call(HttpStatus.OK, MessageConstants.OK, dto);

        } catch (Exception e) {
            return ApiResponse.call(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ApiResponse<Dataset>> getById(String dbId) {
        try {
            Dataset dataset = repo.findById(dbId);
            if (dataset == null) {
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.DATASET_NOT_FOUND);
            }
            return ApiResponse.call(HttpStatus.OK, MessageConstants.OK, dataset);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }
}
