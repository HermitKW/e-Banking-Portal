package com.e_bank.transaction_api.schema;

import org.apache.avro.Schema;
import org.apache.avro.SchemaCompatibility;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Asserts that the v2 Transaction schema (adds nullable customerId) evolves compatibly from v1,
 * the concrete demonstration of the "schema evolution" requirement.
 */
class SchemaCompatibilityTest {

    private static Schema load(String classpathResource) throws IOException {
        try (InputStream in = SchemaCompatibilityTest.class.getResourceAsStream(classpathResource)) {
            return new Schema.Parser().parse(in);
        }
    }

    @Test
    void v2ReaderCanReadV1Data_backwardCompatible() throws IOException {
        Schema v1 = load("/avro/transaction.avsc");
        Schema v2 = load("/avro/transaction-v2.avsc");
        var result = SchemaCompatibility.checkReaderWriterCompatibility(v2, v1);
        assertEquals(SchemaCompatibility.SchemaCompatibilityType.COMPATIBLE, result.getType());
    }

    @Test
    void v1ReaderCanReadV2Data_forwardCompatible() throws IOException {
        Schema v1 = load("/avro/transaction.avsc");
        Schema v2 = load("/avro/transaction-v2.avsc");
        var result = SchemaCompatibility.checkReaderWriterCompatibility(v1, v2);
        assertEquals(SchemaCompatibility.SchemaCompatibilityType.COMPATIBLE, result.getType());
    }
}
