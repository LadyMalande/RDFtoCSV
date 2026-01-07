package com.miklosova.rdftocsvw.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceLoggerTest {

    private PerformanceLogger performanceLogger;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Path logFile = tempDir.resolve("test_performance.log");
        performanceLogger = new PerformanceLogger(logFile.toString());
    }

    @Test
    void testDefaultConstructor() {
        PerformanceLogger defaultLogger = new PerformanceLogger();
        assertNotNull(defaultLogger);
        assertEquals(0, defaultLogger.getCheckpointCount());
    }

    @Test
    void testConstructorWithCustomPath() {
        Path customLog = tempDir.resolve("custom.log");
        PerformanceLogger customLogger = new PerformanceLogger(customLog.toString());
        assertNotNull(customLogger);
    }

    @Test
    void testStart() {
        performanceLogger.start();
        assertTrue(performanceLogger.getTotalElapsedTime() >= 0);
    }

    @Test
    void testCheckpoint() throws InterruptedException {
        performanceLogger.checkpoint("First checkpoint");
        Thread.sleep(10);
        performanceLogger.checkpoint("Second checkpoint");
        
        assertEquals(2, performanceLogger.getCheckpointCount());
    }

    @Test
    void testCheckpointWithNullName() {
        performanceLogger.checkpoint(null);
        assertEquals(1, performanceLogger.getCheckpointCount());
    }

    @Test
    void testCheckpointWithEmptyName() {
        performanceLogger.checkpoint("");
        assertEquals(1, performanceLogger.getCheckpointCount());
    }

    @Test
    void testMultipleCheckpoints() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            Thread.sleep(5);
            performanceLogger.checkpoint("Checkpoint " + i);
        }
        assertEquals(5, performanceLogger.getCheckpointCount());
    }

    @Test
    void testSetFileInfo() {
        performanceLogger.setFileInfo("/path/to/test.ttl", 1024);
        // Should not throw exception
        performanceLogger.checkpoint("After file info");
        assertEquals(1, performanceLogger.getCheckpointCount());
    }

    @Test
    void testSetFileInfoWithNullPath() {
        performanceLogger.setFileInfo(null, 0);
        performanceLogger.checkpoint("Test");
        assertEquals(1, performanceLogger.getCheckpointCount());
    }

    @Test
    void testSetFileInfoWithLargeSize() {
        performanceLogger.setFileInfo("/path/to/large.nt", 1_000_000);
        performanceLogger.checkpoint("Large file test");
        assertEquals(1, performanceLogger.getCheckpointCount());
    }

    @Test
    void testSetConfigInfo() {
        performanceLogger.setConfigInfo("rdf4j", true, "label", "en,cs", true, false, "output.csv");
        performanceLogger.checkpoint("After config");
        assertEquals(1, performanceLogger.getCheckpointCount());
    }

    @Test
    void testSetConfigInfoWithNullValues() {
        performanceLogger.setConfigInfo(null, false, null, null, false, false, null);
        performanceLogger.checkpoint("Null config test");
        assertEquals(1, performanceLogger.getCheckpointCount());
    }

    @Test
    void testSetConfigInfoWithMultipleFiles() {
        performanceLogger.setConfigInfo("streaming", true, "label", "en", false, false, 
                                       "file1.csv,file2.csv,file3.csv");
        performanceLogger.checkpoint("Multiple files");
        assertEquals(1, performanceLogger.getCheckpointCount());
    }

    @Test
    void testCalculateOutputSizes() {
        performanceLogger.setConfigInfo("rdf4j", false, "label", "en", false, false, "");
        performanceLogger.calculateOutputSizes();
        // Should not throw exception
    }

    @Test
    void testCalculateOutputSizesWithExistingFiles() throws IOException {
        Path testFile1 = tempDir.resolve("output1.csv");
        Path testFile2 = tempDir.resolve("output2.csv");
        Files.writeString(testFile1, "test data 1");
        Files.writeString(testFile2, "test data 2");
        
        String fileNames = testFile1.toString() + "," + testFile2.toString();
        performanceLogger.setConfigInfo("test", false, "label", "en", false, false, fileNames);
        performanceLogger.calculateOutputSizes();
        // Should calculate sizes without errors
    }

    @Test
    void testGetCheckpointCount() {
        assertEquals(0, performanceLogger.getCheckpointCount());
        performanceLogger.checkpoint("First");
        assertEquals(1, performanceLogger.getCheckpointCount());
        performanceLogger.checkpoint("Second");
        assertEquals(2, performanceLogger.getCheckpointCount());
    }

    @Test
    void testGetTotalElapsedTime() throws InterruptedException {
        long elapsed1 = performanceLogger.getTotalElapsedTime();
        Thread.sleep(50);
        long elapsed2 = performanceLogger.getTotalElapsedTime();
        
        assertTrue(elapsed2 > elapsed1, "Elapsed time should increase");
        assertTrue(elapsed2 >= 50, "Should have at least 50ms elapsed");
    }

    @Test
    void testPrintSummary() {
        performanceLogger.checkpoint("Checkpoint 1");
        performanceLogger.checkpoint("Checkpoint 2");
        performanceLogger.printSummary();
        // Should not throw exception
    }

    @Test
    void testPrintSummaryWithoutCheckpoints() {
        performanceLogger.printSummary();
        // Should handle empty checkpoints
    }

    @Test
    void testPrintSummaryWithFileInfo() {
        performanceLogger.setFileInfo("/path/to/input.ttl", 512);
        performanceLogger.checkpoint("Test");
        performanceLogger.printSummary();
    }

    @Test
    void testPrintSummaryWithConfigInfo() {
        performanceLogger.setConfigInfo("bigfilestreaming", true, "propertyUrl", "cs,en", true, true, "out.csv");
        performanceLogger.checkpoint("Config test");
        performanceLogger.printSummary();
    }

    @Test
    void testWriteLogToFile() throws IOException {
        Path logFile = tempDir.resolve("performance_output.log");
        PerformanceLogger logger = new PerformanceLogger(logFile.toString());
        
        logger.checkpoint("Checkpoint 1");
        logger.checkpoint("Checkpoint 2");
        logger.writeLogToFile();
        
        assertTrue(Files.exists(logFile));
        assertTrue(Files.size(logFile) > 0);
        
        List<String> lines = Files.readAllLines(logFile);
        assertTrue(lines.size() > 0, "Log file should have content");
    }

    @Test
    void testWriteLogToFileWithAllInfo() throws IOException {
        Path logFile = tempDir.resolve("complete_log.log");
        PerformanceLogger logger = new PerformanceLogger(logFile.toString());
        
        logger.setFileInfo("/input/test.nt", 2048);
        logger.setConfigInfo("rdf4j", false, "label", "en,cs,de", false, false, "output1.csv,output2.csv");
        logger.checkpoint("Parse input");
        logger.checkpoint("Convert to CSV");
        logger.checkpoint("Write metadata");
        logger.calculateOutputSizes();
        logger.writeLogToFile();
        
        assertTrue(Files.exists(logFile));
        List<String> lines = Files.readAllLines(logFile);
        
        assertTrue(lines.stream().anyMatch(line -> line.contains("PERFORMANCE LOG")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Parse input")));
    }

    @Test
    void testWriteLogToFileMultipleTimes() throws IOException {
        Path logFile = tempDir.resolve("append_test.log");
        PerformanceLogger logger1 = new PerformanceLogger(logFile.toString());
        logger1.checkpoint("Run 1");
        logger1.writeLogToFile();
        
        long size1 = Files.size(logFile);
        
        PerformanceLogger logger2 = new PerformanceLogger(logFile.toString());
        logger2.checkpoint("Run 2");
        logger2.writeLogToFile();
        
        long size2 = Files.size(logFile);
        
        assertTrue(size2 > size1, "Second write should append to file");
    }

    @Test
    void testCompleteWorkflow() throws IOException, InterruptedException {
        Path logFile = tempDir.resolve("workflow_test.log");
        PerformanceLogger logger = new PerformanceLogger(logFile.toString());
        
        // Simulate a complete workflow
        logger.setFileInfo("/data/example.ttl", 5000);
        logger.setConfigInfo("streaming", true, "propertyUrl", "en", true, false, "output.csv");
        
        logger.checkpoint("Start processing");
        Thread.sleep(10);
        
        logger.checkpoint("Parse input file");
        Thread.sleep(10);
        
        logger.checkpoint("Convert to intermediate format");
        Thread.sleep(10);
        
        logger.checkpoint("Generate metadata");
        Thread.sleep(10);
        
        logger.checkpoint("Write output files");
        
        logger.calculateOutputSizes();
        logger.printSummary();
        logger.writeLogToFile();
        
        assertEquals(5, logger.getCheckpointCount());
        assertTrue(Files.exists(logFile));
        assertTrue(Files.size(logFile) > 0);
    }

    @Test
    void testCheckpointTimingAccuracy() throws InterruptedException {
        long start = System.currentTimeMillis();
        performanceLogger.checkpoint("Start");
        Thread.sleep(100);
        performanceLogger.checkpoint("After 100ms");
        long elapsed = performanceLogger.getTotalElapsedTime();
        
        assertTrue(elapsed >= 100, "Should measure at least 100ms");
        assertTrue(elapsed < 200, "Should not have excessive overhead");
    }
}
