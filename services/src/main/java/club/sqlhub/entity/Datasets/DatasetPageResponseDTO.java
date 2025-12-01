package club.sqlhub.entity.Datasets;

import java.util.List;

import club.sqlhub.mongo.models.Dataset;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatasetPageResponseDTO {
    private List<Dataset> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
