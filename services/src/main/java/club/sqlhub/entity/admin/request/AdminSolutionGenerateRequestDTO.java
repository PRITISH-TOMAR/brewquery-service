package club.sqlhub.entity.admin.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminSolutionGenerateRequestDTO {

    @NotBlank(message = "solutionQuery is required")
    private String solutionQuery;

    private String sqlMode;
}
