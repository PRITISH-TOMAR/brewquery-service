package club.sqlhub.entity.Enums;

public enum ModuleOperationEnum {
    READ,    // View module content (datasets, questions, testcases, etc.)
    WRITE,   // Create new or edit existing content (requires role > USER)
    DELETE   // Soft delete content via DELETED_AT field (requires role > USER)
}
