# Performance Logging

The RDFtoCSV converter now includes comprehensive performance logging to track execution time for each segment of the conversion process.

## Features

- **Automatic timing**: Performance is automatically tracked for all CLI conversions
- **Comprehensive checkpoints**: Tracks time for:
  - Program initialization
  - RDF file parsing and loading into repository
  - SPARQL query execution and data extraction
  - Metadata generation
  - CSV file writing
  - ZIP file creation
- **Persistent log file**: Results are appended to `performance_log.txt` in the current directory
- **Clear delimitation**: Each program run is clearly separated in the log file
- **Console summary**: Prints summary to console at the end of execution

## Log File Location

By default, performance logs are written to:
```
performance_log.txt
```

This file is created in the current working directory where the program is executed.

## Log Format

Each program run creates an entry like this:

```
================================================================================
PERFORMANCE LOG - Program Run
Start Time: 2025-11-29 14:23:45.123
End Time: 2025-11-29 14:23:52.456
Total Duration: 7.33s
---------------------------------------------------------------------------------

Checkpoint                                           Duration   Cumulative  % of Total
---------------------------------------------------------------------------------
Program initialization complete                         125ms        125ms       1.71%
Parse input - RDF file loaded into repository          2.15s        2.28s      29.35%
Convert data - SPARQL queries executed, data extra     3.42s        5.69s      46.67%
Create metadata - JSON metadata structure built        856ms        6.55s      11.68%
Write CSV files - Data written to disk                 512ms        7.06s       6.99%
Finalize output - ZIP file created on disk             267ms        7.33s       3.64%
---------------------------------------------------------------------------------
TOTAL                                                   7.33s

================================================================================
```

## Console Output

At the end of each run, you'll see:

```
=== RDFtoCSV Conversion Started ===
... conversion progress ...

================================================================================
PERFORMANCE SUMMARY
Total Duration: 7.33s
---------------------------------------------------------------------------------
Checkpoint                                           Duration  % of Total
---------------------------------------------------------------------------------
Program initialization complete                         125ms       1.71%
Parse input - RDF file loaded into repository          2.15s      29.35%
Convert data - SPARQL queries executed, data extra     3.42s      46.67%
Create metadata - JSON metadata structure built        856ms      11.68%
Write CSV files - Data written to disk                 512ms       6.99%
Finalize output - ZIP file created on disk             267ms       3.64%
---------------------------------------------------------------------------------
TOTAL                                                   7.33s
================================================================================

=== RDFtoCSV Conversion Completed Successfully ===
Total execution time: 7332ms
```

## Tracked Segments

1. **Program initialization complete**: Configuration parsing, AppConfig setup
2. **Parse input - RDF file loaded into repository**: Reading RDF file, loading into RDF4J repository
3. **Convert data - SPARQL queries executed, data extracted**: Running SPARQL queries to extract data
4. **Create metadata - JSON metadata structure built**: Generating CSVW metadata JSON
5. **Write CSV files - Data written to disk**: Writing CSV files to disk
6. **Finalize output - ZIP file created on disk**: Creating final ZIP archive

## Performance Analysis

The log file helps identify:
- **Bottlenecks**: Which segment takes the most time
- **Trends**: How performance changes with different input files
- **Optimization opportunities**: Where to focus improvement efforts

Common patterns:
- Large RDF files → High "Parse input" time
- Complex queries → High "Convert data" time
- Many dereferencing requests → High "Create metadata" time
- Large datasets → High "Write CSV files" time

## Implementation Details

The performance logging is implemented using the `PerformanceLogger` class in the `support` package:

```java
// Created automatically in RDFtoCSV constructor
performanceLogger = new PerformanceLogger();

// Checkpoints added at key points
performanceLogger.checkpoint("Program initialization complete");
// ... conversion work ...
performanceLogger.checkpoint("Parse input - RDF file loaded into repository");

// Finalization writes to file
performanceLogger.writeLogToFile();
performanceLogger.printSummary();
```

## Web Service Usage

Note: Performance logging is currently only enabled for **command-line usage** via `convertToZipFile()`. 

The web service method `convertToZip()` does not log performance to avoid file I/O overhead and concurrency issues in multi-threaded environments.

## Custom Log Location

To change the log file location, you would need to modify the `PerformanceLogger` instantiation in `RDFtoCSV.java`:

```java
// Custom location
performanceLogger = new PerformanceLogger("path/to/your/logfile.txt");
```

## Interpreting Results

### Normal Performance
- Small files (< 1MB): < 5 seconds total
- Medium files (1-10MB): 5-30 seconds total
- Large files (> 10MB): 30+ seconds total

### Performance Issues
If you see:
- "Parse input" > 50% of total → Consider streaming mode for very large files
- "Convert data" > 60% of total → Complex query structure, may need optimization
- "Create metadata" > 30% of total → Many external dereferencing requests (now cached and timeout-limited)
- "Write CSV" > 20% of total → Large number of rows, I/O bottleneck

## Related Features

- **Dereferencer timeout**: External URL requests timeout after 12 seconds (3s connection + 7s socket + 2s pool)
- **Dereferencer caching**: Failed URLs are cached to prevent retries
- **ZIP optimization**: Separate paths for web service (bytes only) vs CLI (file on disk)
