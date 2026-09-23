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
        private Double numericTolerance;
        private String type;
        /** Pre-parsed seed data for public test cases. Null for private TCs.
         *  Shape: [ { table: String, columns: [String], rows: [[Object]] } ] */
        private Object sampleData;
        /** Expected output when expectedSql is run against this TC's seed.
         *  Shape: { columns: [String], rows: [[Object]], rowsCount: Integer }.
         *  Null for private TCs. */
        private Object expectedOutput;
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
