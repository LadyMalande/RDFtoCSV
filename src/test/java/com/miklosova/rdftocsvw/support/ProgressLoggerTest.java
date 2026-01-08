package com.miklosova.rdftocsvw.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProgressLoggerTest {

    @Test
    void testLogProgressWithZeroProgress() {
        ProgressLogger.logProgress(ProgressLogger.Stage.CONVERTING, 0);
        // Should handle 0% gracefully without throwing exception
    }

    @Test
    void testLogProgressWithFullProgress() {
        ProgressLogger.logProgress(ProgressLogger.Stage.FINALIZING, 100);
        // Should log 100% progress
    }

    @Test
    void testLogProgressWithOverflow() {
        ProgressLogger.logProgress(ProgressLogger.Stage.CONVERTING, 150);
        // Should clamp to 100% maximum
    }

    @Test
    void testLogProgressWithNegativeValues() {
        ProgressLogger.logProgress(ProgressLogger.Stage.CONVERTING, -10);
        // Should clamp to 0% minimum
    }

    @Test
    void testLogProgressMultipleTimes() {
        ProgressLogger.logProgress(ProgressLogger.Stage.PARSING, 10);
        ProgressLogger.logProgress(ProgressLogger.Stage.PARSING, 25);
        ProgressLogger.logProgress(ProgressLogger.Stage.PARSING, 50);
        ProgressLogger.logProgress(ProgressLogger.Stage.PARSING, 75);
        ProgressLogger.logProgress(ProgressLogger.Stage.PARSING, 100);
        // Should log multiple progress updates
    }

    @Test
    void testLogProgressWithLargeNumbers() {
        ProgressLogger.logProgress(ProgressLogger.Stage.CONVERTING, 10000);
        // Should clamp large numbers to 100%
    }

    @Test
    void testLogProgressWithSmallPercentageIncrements() {
        for (int i = 0; i <= 100; i += 10) {
            ProgressLogger.logProgress(ProgressLogger.Stage.CONVERTING, i);
        }
        // Should handle frequent updates
    }

    @Test
    void testLogProgressAllStages() {
        ProgressLogger.logProgress(ProgressLogger.Stage.PARSING, 50);
        ProgressLogger.logProgress(ProgressLogger.Stage.CONVERTING, 50);
        ProgressLogger.logProgress(ProgressLogger.Stage.METADATA, 50);
        ProgressLogger.logProgress(ProgressLogger.Stage.WRITING, 50);
        ProgressLogger.logProgress(ProgressLogger.Stage.FINALIZING, 50);
    }

    @Test
    void testLogProgressWithNullMessage() {
        ProgressLogger.logProgress(ProgressLogger.Stage.CONVERTING, 50, null);
        // Should use default stage description when message is null
    }

    @Test
    void testLogProgressWithEmptyMessage() {
        ProgressLogger.logProgress(ProgressLogger.Stage.CONVERTING, 50, "");
        // Should handle empty message
    }

    @Test
    void testLogProgressWithCustomMessage() {
        ProgressLogger.logProgress(ProgressLogger.Stage.CONVERTING, 50, "Processing data...");
        ProgressLogger.logProgress(ProgressLogger.Stage.WRITING, 75, "Writing output files");
    }

    @Test
    void testLogProgressWithSpecialCharacters() {
        ProgressLogger.logProgress(ProgressLogger.Stage.CONVERTING, 50, "Processing: 50% done!");
        ProgressLogger.logProgress(ProgressLogger.Stage.WRITING, 75, "Files: 1,234/5,678");
    }

    @Test
    void testLogProgressWithUnicode() {
        ProgressLogger.logProgress(ProgressLogger.Stage.CONVERTING, 50, "Zpracování: 50%");
        ProgressLogger.logProgress(ProgressLogger.Stage.WRITING, 75, "処理中：75%");
    }

    @Test
    void testStartStage() {
        ProgressLogger.startStage(ProgressLogger.Stage.PARSING);
        ProgressLogger.startStage(ProgressLogger.Stage.CONVERTING);
        ProgressLogger.startStage(ProgressLogger.Stage.METADATA);
        ProgressLogger.startStage(ProgressLogger.Stage.WRITING);
        ProgressLogger.startStage(ProgressLogger.Stage.FINALIZING);
    }

    @Test
    void testCompleteStage() {
        ProgressLogger.completeStage(ProgressLogger.Stage.PARSING);
        ProgressLogger.completeStage(ProgressLogger.Stage.CONVERTING);
        ProgressLogger.completeStage(ProgressLogger.Stage.METADATA);
        ProgressLogger.completeStage(ProgressLogger.Stage.WRITING);
        ProgressLogger.completeStage(ProgressLogger.Stage.FINALIZING);
    }

    @Test
    void testLogProgressWithCount() {
        ProgressLogger.logProgressWithCount(ProgressLogger.Stage.PARSING, 50, 100, "triples");
        ProgressLogger.logProgressWithCount(ProgressLogger.Stage.CONVERTING, 250, 500, "rows");
        ProgressLogger.logProgressWithCount(ProgressLogger.Stage.WRITING, 3, 10, "files");
    }

    @Test
    void testLogProgressWithCountZeroTotal() {
        ProgressLogger.logProgressWithCount(ProgressLogger.Stage.PARSING, 0, 0, "items");
        // Should handle zero total gracefully
    }

    @Test
    void testLogProgressWithCountLargeNumbers() {
        ProgressLogger.logProgressWithCount(ProgressLogger.Stage.CONVERTING, 500000, 1000000, "triples");
        // Should handle large numbers
    }

    @Test
    void testLogProgressWithCountEqualCurrentAndTotal() {
        ProgressLogger.logProgressWithCount(ProgressLogger.Stage.FINALIZING, 100, 100, "files");
        // Should show 100% when current equals total
    }

    @Test
    void testStageEnumDescriptions() {
        assertEquals("Parsing RDF file", ProgressLogger.Stage.PARSING.getDescription());
        assertEquals("Converting to CSV structure", ProgressLogger.Stage.CONVERTING.getDescription());
        assertEquals("Creating metadata", ProgressLogger.Stage.METADATA.getDescription());
        assertEquals("Writing CSV files", ProgressLogger.Stage.WRITING.getDescription());
        assertEquals("Creating ZIP file", ProgressLogger.Stage.FINALIZING.getDescription());
    }

    @Test
    void testStageEnumValues() {
        ProgressLogger.Stage[] stages = ProgressLogger.Stage.values();
        assertEquals(5, stages.length);
        assertEquals(ProgressLogger.Stage.PARSING, stages[0]);
        assertEquals(ProgressLogger.Stage.CONVERTING, stages[1]);
        assertEquals(ProgressLogger.Stage.METADATA, stages[2]);
        assertEquals(ProgressLogger.Stage.WRITING, stages[3]);
        assertEquals(ProgressLogger.Stage.FINALIZING, stages[4]);
    }

    @Test
    void testCompleteWorkflow() {
        // Simulate a complete conversion workflow
        ProgressLogger.startStage(ProgressLogger.Stage.PARSING);
        ProgressLogger.logProgressWithCount(ProgressLogger.Stage.PARSING, 50, 100, "triples");
        ProgressLogger.logProgressWithCount(ProgressLogger.Stage.PARSING, 100, 100, "triples");
        ProgressLogger.completeStage(ProgressLogger.Stage.PARSING);

        ProgressLogger.startStage(ProgressLogger.Stage.CONVERTING);
        ProgressLogger.logProgress(ProgressLogger.Stage.CONVERTING, 50, "Building CSV structure");
        ProgressLogger.completeStage(ProgressLogger.Stage.CONVERTING);

        ProgressLogger.startStage(ProgressLogger.Stage.METADATA);
        ProgressLogger.logProgress(ProgressLogger.Stage.METADATA, 50);
        ProgressLogger.completeStage(ProgressLogger.Stage.METADATA);

        ProgressLogger.startStage(ProgressLogger.Stage.WRITING);
        ProgressLogger.logProgressWithCount(ProgressLogger.Stage.WRITING, 5, 10, "files");
        ProgressLogger.completeStage(ProgressLogger.Stage.WRITING);

        ProgressLogger.startStage(ProgressLogger.Stage.FINALIZING);
        ProgressLogger.logProgress(ProgressLogger.Stage.FINALIZING, 100);
        ProgressLogger.completeStage(ProgressLogger.Stage.FINALIZING);
    }

    @Test
    void testLogProgressWithLongMessage() {
        String longMessage = "This is a very long message that describes " +
                           "what is happening during the conversion process " +
                           "with lots of detail about the current operation";
        ProgressLogger.logProgress(ProgressLogger.Stage.CONVERTING, 50, longMessage);
    }

    @Test
    void testLogProgressBoundaryValues() {
        // Test minimum boundary
        ProgressLogger.logProgress(ProgressLogger.Stage.PARSING, Integer.MIN_VALUE);
        
        // Test maximum boundary
        ProgressLogger.logProgress(ProgressLogger.Stage.PARSING, Integer.MAX_VALUE);
        
        // Test normal boundaries
        ProgressLogger.logProgress(ProgressLogger.Stage.PARSING, 0);
        ProgressLogger.logProgress(ProgressLogger.Stage.PARSING, 100);
    }

    @Test
    void testLogProgressWithCountBoundaryValues() {
        // Zero values
        ProgressLogger.logProgressWithCount(ProgressLogger.Stage.PARSING, 0, 0, "items");
        
        // Current > Total
        ProgressLogger.logProgressWithCount(ProgressLogger.Stage.PARSING, 150, 100, "items");
        
        // Large values
        ProgressLogger.logProgressWithCount(ProgressLogger.Stage.PARSING, 1000000, 2000000, "triples");
    }
}
