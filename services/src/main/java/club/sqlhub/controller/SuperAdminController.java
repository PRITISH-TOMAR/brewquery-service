package club.sqlhub.controller;

import club.sqlhub.entity.admin.request.SuperAdminScopeRequestDTO;
import club.sqlhub.entity.admin.response.AdminScopeSummaryDTO;
import club.sqlhub.service.AdminManagementService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/superadmin")
@PreAuthorize("hasRole('SUPERADMIN')")
public class SuperAdminController {

    private final AdminManagementService adminManagementService;

    /** Returns all ADMINs with their module scopes. */
    @GetMapping("/scopes")
    public ResponseEntity<ApiResponse<List<AdminScopeSummaryDTO>>> listAdminScopes() {
        return adminManagementService.listAdminsWithScopes();
    }

    /** Create or replace a module scope entry for a target ADMIN. */
    @PostMapping("/scopes")
    public ResponseEntity<ApiResponse<Void>> setAdminScope(
            @Valid @RequestBody SuperAdminScopeRequestDTO req) {
        return adminManagementService.setAdminScope(req);
    }
}
