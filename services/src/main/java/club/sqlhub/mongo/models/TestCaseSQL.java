package club.sqlhub.mongo.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

import club.sqlhub.entity.Enums.TestCaseType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TestCaseSQL {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestCase {
        @Id
        @Field("_id")
        private String id;
        private String schemaSql;
        private String seedSql;
        private Double numericTolerance;
        private String type;
    }

    @Document(collection = "testcases")
    @Data
    public static class TestCases {
        @Id
        private String id;
        private String questionId;
        @JsonProperty("type")
        private String type;
        private String expectedSql;
        List<TestCase> testCases;
    }

}
