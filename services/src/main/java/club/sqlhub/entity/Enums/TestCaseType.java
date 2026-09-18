package club.sqlhub.entity.Enums;

public enum TestCaseType {
    PRIVATE("private"),
    PUBLIC("public");

    private final String dbValue;

    TestCaseType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }
}