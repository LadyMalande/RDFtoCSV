package com.miklosova.rdftocsvw.output_processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class FinalizedOutputTest {

    //BaseRock generated method id: ${testConstructorAndGetter}, hash: 6013C68D8F115B89B2F696C94D71B374
    @Test
    void testConstructorAndGetter() {
        String testData = "Test Data";
        FinalizedOutput<String> finalizedOutput = new FinalizedOutput<>(testData);
        assertEquals(testData, finalizedOutput.getOutputData());
    }

    //BaseRock generated method id: ${testWithIntegerData}, hash: 5DB9FFEACA7D0207E2580C62F4A58165
    @ParameterizedTest
    @ValueSource(ints = { 0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE })
    void testWithIntegerData(int testData) {
        FinalizedOutput<Integer> finalizedOutput = new FinalizedOutput<>(testData);
        assertEquals(testData, finalizedOutput.getOutputData());
    }

    //BaseRock generated method id: ${testWithNullData}, hash: 15E2E3ABBAA0D335AF8B9ADA25DF5DC6
    @Test
    void testWithNullData() {
        FinalizedOutput<Object> finalizedOutput = new FinalizedOutput<>(null);
        assertNull(finalizedOutput.getOutputData());
    }

    //BaseRock generated method id: ${testWithCustomObject}, hash: AAFEDD8D9ED920B99F3384BD45B5AAFE
    @Test
    void testWithCustomObject() {
        CustomObject customObject = new CustomObject("test");
        FinalizedOutput<CustomObject> finalizedOutput = new FinalizedOutput<>(customObject);
        assertEquals(customObject, finalizedOutput.getOutputData());
    }

    private static class CustomObject {

        private final String value;

        CustomObject(String value) {
            this.value = value;
        }
    }
}
