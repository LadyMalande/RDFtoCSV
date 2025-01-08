package com.miklosova.rdftocsvw.converter;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import org.eclipse.rdf4j.repository.RepositoryConnection;

/**
 * The type Conversion gateway.
 */
public class ConversionGateway {
    private IQueryParser conversionMethod;

    /**
     * Gets conversion method.
     *
     * @return the conversion method
     */
    public IQueryParser getConversionMethod() {
        return conversionMethod;
    }

    /**
     * Sets conversion method.
     *
     * @param conversionMethod the conversion method
     */
    public void setConversionMethod(IQueryParser conversionMethod) {
        this.conversionMethod = conversionMethod;
    }

    /**
     * Process input prefinished output.
     *
     * @param repositoryConnection the repository connection
     * @return the prefinished output
     */
    public PrefinishedOutput<RowsAndKeys> processInput(RepositoryConnection repositoryConnection) {
        return conversionMethod.convertWithQuery(repositoryConnection);
    }
}
