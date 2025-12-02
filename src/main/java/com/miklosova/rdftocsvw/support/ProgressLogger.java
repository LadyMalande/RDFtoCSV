package com.miklosova.rdftocsvw.support;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Progress logger for tracking transformation progress.
 * Outputs structured log messages that can be parsed by web services to display progress bars.
 * 
 * Log format: [PROGRESS] stage=STAGE_NAME progress=PERCENTAGE message=DESCRIPTION
 */
public class ProgressLogger {
    private static final Logger logger = Logger.getLogger(ProgressLogger.class.getName());
    private static final String PROGRESS_PREFIX = "[PROGRESS]";
    
    /**
     * Conversion stages
     */
    public enum Stage {
        PARSING("Parsing RDF file"),
        CONVERTING("Converting to CSV structure"),
        METADATA("Creating metadata"),
        WRITING("Writing CSV files"),
        FINALIZING("Creating ZIP file");
        
        private final String description;
        
        Stage(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Log progress for a specific stage.
     * 
     * @param stage The current stage
     * @param progress Progress percentage (0-100)
     * @param message Optional detailed message
     */
    public static void logProgress(Stage stage, int progress, String message) {
        String logMessage = String.format("%s stage=%s progress=%d%% message=%s",
            PROGRESS_PREFIX,
            stage.name(),
            Math.min(100, Math.max(0, progress)),
            message != null ? message : stage.getDescription());
        
        logger.log(Level.INFO, logMessage);
        
        // Also output to console for CLI users
        System.out.println(logMessage);
    }
    
    /**
     * Log progress for a specific stage without custom message.
     * 
     * @param stage The current stage
     * @param progress Progress percentage (0-100)
     */
    public static void logProgress(Stage stage, int progress) {
        logProgress(stage, progress, null);
    }
    
    /**
     * Log the start of a stage (0% progress).
     * 
     * @param stage The stage being started
     */
    public static void startStage(Stage stage) {
        logProgress(stage, 0, "Starting " + stage.getDescription());
    }
    
    /**
     * Log the completion of a stage (100% progress).
     * 
     * @param stage The stage being completed
     */
    public static void completeStage(Stage stage) {
        logProgress(stage, 100, "Completed " + stage.getDescription());
    }
    
    /**
     * Log progress with item counts (e.g., "Processing 50/200 triples").
     * 
     * @param stage The current stage
     * @param current Current item count
     * @param total Total item count
     * @param itemName Name of items being processed (e.g., "triples", "rows")
     */
    public static void logProgressWithCount(Stage stage, long current, long total, String itemName) {
        int percentage = total > 0 ? (int) ((current * 100) / total) : 0;
        String message = String.format("Processing %d/%d %s", current, total, itemName);
        logProgress(stage, percentage, message);
    }
}
