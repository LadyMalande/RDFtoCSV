package com.miklosova.rdftocsvw.convertor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.IRDF4JParsingMethod;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;

public class ConversionGateway {
    private IQueryParser conversionMethod;

    public void setConversionMethod(IQueryParser conversionMethod) {
        this.conversionMethod = conversionMethod;
    }

    public PrefinishedOutput processInput(RepositoryConnection repositoryConnection) {
        return conversionMethod.convertWithQuery(repositoryConnection);
    }
}
