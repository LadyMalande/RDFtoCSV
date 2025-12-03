package com.miklosova.rdftocsvw.support;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Performance logger that tracks timing information for each segment of program execution.
 * Automatically appends performance logs to a file with clear delimitation between program runs.
 */
public class PerformanceLogger {
    private static final Logger logger = Logger.getLogger(PerformanceLogger.class.getName());
    private static final String DEFAULT_LOG_FILE = "performance_log.txt";
    private static final String DELIMITER = "=" + "=".repeat(80) + "\n";
    
    private final String logFilePath;
    private final List<TimingEntry> timings;
    private long programStartTime;
    private long lastCheckpointTime;
    private final SimpleDateFormat dateFormat;
    private String inputFilePath;
    private long inputFileSizeKB;
    private String parsingMethod;
    private boolean streaming;
    private String namingConvention;
    private String languagePreference;
    private boolean firstNormalForm;
    private boolean skipDereferencing;
    private int intermediateFilesCount;
    private String intermediateFileNames;
    private long totalOutputSizeKB;
    
    /**
     * Timing entry stores information about a single checkpoint.
     */
    private static class TimingEntry {
        String name;
        long startTime;
        long duration;
        long cumulativeTime;
        
        TimingEntry(String name, long startTime, long duration, long cumulativeTime) {
            this.name = name;
            this.startTime = startTime;
            this.duration = duration;
            this.cumulativeTime = cumulativeTime;
        }
    }
    
    /**
     * Creates a new PerformanceLogger with default log file location.
     */
    public PerformanceLogger() {
        this(DEFAULT_LOG_FILE);
    }
    
    /**
     * Creates a new PerformanceLogger with specified log file location.
     * 
     * @param logFilePath Path to the log file
     */
    public PerformanceLogger(String logFilePath) {
        this.logFilePath = logFilePath;
        this.timings = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        start();
    }
    
    /**
     * Starts the performance tracking session.
     */
    public void start() {
        programStartTime = System.currentTimeMillis();
        lastCheckpointTime = programStartTime;
        logger.log(Level.INFO, "Performance tracking started at " + dateFormat.format(new Date(programStartTime)));
    }
    
    /**
     * Sets the input file information for logging.
     * 
     * @param filePath Absolute path to the input file
     * @param fileSizeKB File size in kilobytes
     */
    public void setFileInfo(String filePath, long fileSizeKB) {
        this.inputFilePath = filePath;
        this.inputFileSizeKB = fileSizeKB;
    }
    
    /**
     * Sets the configuration parameters for logging.
     * 
     * @param parsingMethod The parsing method (rdf4j, streaming, bigfilestreaming)
     * @param streaming Whether streaming mode is enabled
     * @param namingConvention The column naming convention
     * @param languagePreference Preferred languages for labels
     * @param firstNormalForm Whether first normal form is enabled
     * @param skipDereferencing Whether vocabulary dereferencing is skipped
     * @param intermediateFileNames Comma-separated list of intermediate file names
     */
    public void setConfigInfo(String parsingMethod, boolean streaming, String namingConvention, 
                              String languagePreference, boolean firstNormalForm, boolean skipDereferencing, String intermediateFileNames) {
        this.parsingMethod = parsingMethod;
        this.streaming = streaming;
        this.namingConvention = namingConvention;
        this.languagePreference = languagePreference;
        this.firstNormalForm = firstNormalForm;
        this.skipDereferencing = skipDereferencing;
        this.intermediateFileNames = intermediateFileNames;
        
        // Count intermediate files
        if (intermediateFileNames != null && !intermediateFileNames.isEmpty()) {
            this.intermediateFilesCount = intermediateFileNames.split(",").length;
        } else {
            this.intermediateFilesCount = 0;
        }
    }
    
    /**
     * Calculates and sets the total output file sizes.
     * Call this after files are written but before writeLogToFile().
     */
    public void calculateOutputSizes() {
        this.totalOutputSizeKB = 0;
        if (intermediateFileNames != null && !intermediateFileNames.isEmpty()) {
            String[] files = intermediateFileNames.split(",");
            for (String fileName : files) {
                try {
                    java.io.File file = new java.io.File(fileName.trim());
                    if (file.exists() && file.isFile()) {
                        totalOutputSizeKB += file.length() / 1024;
                    }
                } catch (Exception e) {
                    // Ignore errors for individual files
                }
            }
        }
    }
    
    /**
     * Records a timing checkpoint.
     * 
     * @param checkpointName Name of the checkpoint/segment
     */
    public void checkpoint(String checkpointName) {
        long currentTime = System.currentTimeMillis();
        long duration = currentTime - lastCheckpointTime;
        long cumulativeTime = currentTime - programStartTime;
        
        timings.add(new TimingEntry(checkpointName, lastCheckpointTime, duration, cumulativeTime));
        
        logger.log(Level.INFO, String.format("%s: %dms (total: %dms)", 
            checkpointName, duration, cumulativeTime));
        
        lastCheckpointTime = currentTime;
    }
    
    /**
     * Finalizes the performance tracking and writes the comprehensive log to file.
     * This appends to the log file with clear delimitation from previous runs.
     */
    public void writeLogToFile() {
        long programEndTime = System.currentTimeMillis();
        long totalDuration = programEndTime - programStartTime;
        
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(logFilePath, true)))) {
            // Write delimiter and header for this run
            writer.println(DELIMITER);
            writer.println("PERFORMANCE LOG - Program Run");
            writer.println("Start Time: " + dateFormat.format(new Date(programStartTime)));
            writer.println("End Time: " + dateFormat.format(new Date(programEndTime)));
            writer.println("Total Duration: " + formatDuration(totalDuration));
            writer.println("-".repeat(81));
            
            // Input file information
            if (inputFilePath != null) {
                writer.println("Input File: " + inputFilePath);
                writer.println("Input File Size: " + inputFileSizeKB + " KB");
            }
            
            // Configuration parameters
            if (parsingMethod != null) {
                writer.println();
                writer.println("Configuration:");
                writer.println("  Parsing Method: " + parsingMethod);
                writer.println("  Streaming Mode: " + (streaming ? "Yes" : "No"));
                writer.println("  Naming Convention: " + (namingConvention != null ? namingConvention : "default"));
                writer.println("  Language Preference: " + (languagePreference != null ? languagePreference : "default"));
                writer.println("  First Normal Form: " + (firstNormalForm ? "Yes" : "No"));
                writer.println("  Skip Dereferencing: " + (skipDereferencing ? "Yes" : "No"));
                writer.println("  Intermediate Files Count: " + intermediateFilesCount);
                writer.println("  Total Output Size: " + totalOutputSizeKB + " KB");
            }
            
            writer.println("-".repeat(81));
            writer.println();
            
            // Write detailed timing information
            writer.printf("%-50s %12s %12s %12s%n", "Checkpoint", "Duration", "Cumulative", "% of Total");
            writer.println("-".repeat(81));
            
            for (TimingEntry entry : timings) {
                double percentage = (entry.duration * 100.0) / totalDuration;
                writer.printf("%-50s %12s %12s %11.2f%%%n",
                    truncate(entry.name, 50),
                    formatDuration(entry.duration),
                    formatDuration(entry.cumulativeTime),
                    percentage);
            }
            
            writer.println("-".repeat(81));
            writer.printf("%-50s %12s%n", "TOTAL", formatDuration(totalDuration));
            writer.println();
            writer.println(DELIMITER);
            writer.println();
            
            logger.log(Level.INFO, "Performance log written to: " + logFilePath);
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to write performance log to file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Prints the performance summary to console without writing to file.
     */
    public void printSummary() {
        long programEndTime = System.currentTimeMillis();
        long totalDuration = programEndTime - programStartTime;
        
        System.out.println("\n" + DELIMITER);
        System.out.println("PERFORMANCE SUMMARY");
        System.out.println("Total Duration: " + formatDuration(totalDuration));
        System.out.println("-".repeat(81));
        
        // Input file information
        if (inputFilePath != null) {
            System.out.println("Input File: " + inputFilePath);
            System.out.println("Input File Size: " + inputFileSizeKB + " KB");
        }
        
        // Configuration parameters
        if (parsingMethod != null) {
            System.out.println();
            System.out.println("Configuration:");
            System.out.println("  Parsing Method: " + parsingMethod);
            System.out.println("  Streaming Mode: " + (streaming ? "Yes" : "No"));
            System.out.println("  Naming Convention: " + (namingConvention != null ? namingConvention : "default"));
            System.out.println("  Language Preference: " + (languagePreference != null ? languagePreference : "default"));
            System.out.println("  First Normal Form: " + (firstNormalForm ? "Yes" : "No"));
            System.out.println("  Skip Dereferencing: " + (skipDereferencing ? "Yes" : "No"));
            System.out.println("  Intermediate Files Count: " + intermediateFilesCount);
            System.out.println("  Total Output Size: " + totalOutputSizeKB + " KB");
        }
        
        System.out.println("-".repeat(81));
        System.out.printf("%-50s %12s %12s%n", "Checkpoint", "Duration", "% of Total");
        System.out.println("-".repeat(81));
        
        for (TimingEntry entry : timings) {
            double percentage = (entry.duration * 100.0) / totalDuration;
            System.out.printf("%-50s %12s %11.2f%%%n",
                truncate(entry.name, 50),
                formatDuration(entry.duration),
                percentage);
        }
        
        System.out.println("-".repeat(81));
        System.out.printf("%-50s %12s%n", "TOTAL", formatDuration(totalDuration));
        System.out.println(DELIMITER + "\n");
    }
    
    /**
     * Format duration in milliseconds to human-readable format.
     */
    private String formatDuration(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + "ms";
        } else if (milliseconds < 60000) {
            return String.format("%.2fs", milliseconds / 1000.0);
        } else {
            long minutes = milliseconds / 60000;
            long seconds = (milliseconds % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }
    
    /**
     * Truncate string to specified length.
     */
    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Get the total number of checkpoints recorded.
     */
    public int getCheckpointCount() {
        return timings.size();
    }
    
    /**
     * Get the total elapsed time since start.
     */
    public long getTotalElapsedTime() {
        return System.currentTimeMillis() - programStartTime;
    }
}
