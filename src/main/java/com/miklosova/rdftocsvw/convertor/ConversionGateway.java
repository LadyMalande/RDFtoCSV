package com.miklosova.rdftocsvw.convertor;

import org.eclipse.rdf4j.repository.RepositoryConnection;

public class ConversionGateway {
    private IQueryParser conversionMethod;

    public IQueryParser getConversionMethod() {
        return conversionMethod;
    }

    public void setConversionMethod(IQueryParser conversionMethod) {
        this.conversionMethod = conversionMethod;
    }

    public PrefinishedOutput<RowsAndKeys> processInput(RepositoryConnection repositoryConnection) {
        return conversionMethod.convertWithQuery(repositoryConnection);
    }
}
