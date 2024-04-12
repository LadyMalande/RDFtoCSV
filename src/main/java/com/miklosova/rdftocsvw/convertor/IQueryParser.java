package com.miklosova.rdftocsvw.convertor;

import org.eclipse.rdf4j.repository.RepositoryConnection;

public interface IQueryParser {
    public PrefinishedOutput convertWithQuery(RepositoryConnection rc);
}
