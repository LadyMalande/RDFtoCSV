package com.miklosova.rdftocsvw.support;

import java.util.Map;

/**
 * Helper class for creating AppConfig instances in tests.
 * Provides convenient factory methods for common test scenarios.
 */
public class AppConfigTestHelper {

    /**
     * Create a basic test config with minimal required parameters.
     * @param fileName The input file name
     * @return AppConfig instance
     */
    public static AppConfig createBasicConfig(String fileName) {
        return new AppConfig.Builder(fileName).build();
    }

    /**
     * Create a config for testing multiple tables.
     * @param fileName The input file name
     * @return AppConfig instance configured for multiple tables
     */
    public static AppConfig createMultipleTablesConfig(String fileName) {
        return new AppConfig.Builder(fileName)
                .multipleTables(true)
                .build();
    }

    /**
     * Create a config for testing streaming mode.
     * @param fileName The input file name (should be .nt file)
     * @return AppConfig instance configured for streaming
     */
    public static AppConfig createStreamingConfig(String fileName) {
        return new AppConfig.Builder(fileName)
                .parsing("streaming")
                .streaming(true)
                .build();
    }

    /**
     * Create a config for testing big file streaming mode.
     * @param fileName The input file name (should be .nt file)
     * @return AppConfig instance configured for big file streaming
     */
    public static AppConfig createBigFileStreamingConfig(String fileName) {
        return new AppConfig.Builder(fileName)
                .parsing("bigfilestreaming")
                .streaming(true)
                .build();
    }

    /**
     * Create a config with first normal form enabled.
     * @param fileName The input file name
     * @return AppConfig instance with first normal form
     */
    public static AppConfig createFirstNormalFormConfig(String fileName) {
        return new AppConfig.Builder(fileName)
                .firstNormalForm(true)
                .build();
    }

    /**
     * Create a config from a legacy configuration map.
     * This is useful for maintaining compatibility with existing tests.
     * @param fileName The input file name
     * @param configMap Legacy configuration map
     * @return AppConfig instance
     */
    public static AppConfig createFromLegacyMap(String fileName, Map<String, String> configMap) {
        AppConfig.Builder builder = new AppConfig.Builder(fileName);

        if (configMap != null) {
            if (configMap.containsKey("table")) {
                boolean multipleTables = "splitQuery".equalsIgnoreCase(configMap.get("table"))
                        || "MORE".equalsIgnoreCase(configMap.get("table"))
                        || "more".equalsIgnoreCase(configMap.get("table"));
                builder.multipleTables(multipleTables);
            }
            if (configMap.containsKey("readMethod")) {
                builder.parsing(configMap.get("readMethod"));
            }
            if (configMap.containsKey("firstNormalForm")) {
                builder.firstNormalForm(Boolean.parseBoolean(configMap.get("firstNormalForm")));
            }
            if (configMap.containsKey("output")) {
                builder.output(configMap.get("output"));
            }
            if (configMap.containsKey("logLevel")) {
                builder.logLevel(configMap.get("logLevel"));
            }
            if (configMap.containsKey("preferredLanguages")) {
                builder.preferredLanguages(configMap.get("preferredLanguages"));
            }
            if (configMap.containsKey("columnNamingConvention")) {
                builder.columnNamingConvention(configMap.get("columnNamingConvention"));
            }
        }

        return builder.build();
    }

    /**
     * Create a fully customized config for testing.
     * @param fileName The input file name
     * @param parsing Parsing method
     * @param multipleTables Whether to use multiple tables
     * @param firstNormalForm Whether to use first normal form
     * @param streaming Whether to use streaming
     * @return AppConfig instance
     */
    public static AppConfig createCustomConfig(String fileName, String parsing, 
                                                boolean multipleTables, 
                                                boolean firstNormalForm, 
                                                boolean streaming) {
        return new AppConfig.Builder(fileName)
                .parsing(parsing)
                .multipleTables(multipleTables)
                .firstNormalForm(firstNormalForm)
                .streaming(streaming)
                .build();
    }

    /**
     * Create a config with all optional parameters set.
     * @param fileName The input file name
     * @return AppConfig instance with all parameters configured
     */
    public static AppConfig createFullConfig(String fileName) {
        return new AppConfig.Builder(fileName)
                .parsing("rdf4j")
                .multipleTables(false)
                .firstNormalForm(true)
                .streaming(false)
                .output(fileName + ".output.csv")
                .preferredLanguages("en,cs,pl")
                .columnNamingConvention("Title case")
                .logLevel("INFO")
                .build();
    }
}
