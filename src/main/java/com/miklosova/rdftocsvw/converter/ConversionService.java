package com.miklosova.rdftocsvw.converter;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

/**
 * The Conversion service. Part of Strategy design pattern.
 */
public class ConversionService {
    private AppConfig config;
    
    /**
     * Gets conversion gateway.
     *
     * @return the conversion gateway
     */
    public ConversionGateway getConversionGateway() {
        return conversionGateway;
    }

    private ConversionGateway conversionGateway;

    /**
     * Default constructor for backward compatibility.
     * @deprecated Use {@link #ConversionService(AppConfig)} instead
     */
    @Deprecated
    public ConversionService() {
        this.config = null;
    }

    /**
     * Constructor with AppConfig.
     * @param config The application configuration
     */
    public ConversionService(AppConfig config) {
        this.config = config;
    }

    /**
     * Convert by query prefinished output.
     *
     * @param rc the RepositoryConnection to make SPARQL queries on
     * @param db the Repository
     * @return the prefinished output
     */
    public PrefinishedOutput<RowsAndKeys> convertByQuery(RepositoryConnection rc, Repository db) {
        if (rc == null) {
            return null;
        }
        conversionGateway = new ConversionGateway();
        processConversionType(db);
        return conversionGateway.processInput(rc);
    }

    /**
     * Choose the correct converted according to how many tables are going to be made.
     * @param db the Repository to make connection for querying on
     */
    private void processConversionType(Repository db) {
        String conversionChoice;
        if (config != null) {
            conversionChoice = config.getConversionMethod();
        } else {
            // Backward compatibility - use ConfigurationManager
            conversionChoice = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD);
        }

        switch (conversionChoice) {
            case "basicQuery", "trivial" -> conversionGateway.setConversionMethod(
                config != null ? new BasicQueryConverter(db, config) : new BasicQueryConverter(db));
            case "splitQuery" -> conversionGateway.setConversionMethod(
                config != null ? new SplitFilesQueryConverter(db, config) : new SplitFilesQueryConverter(db));
            default -> throw new IllegalArgumentException("Invalid conversion method: " + conversionChoice);
        }
    }
}
