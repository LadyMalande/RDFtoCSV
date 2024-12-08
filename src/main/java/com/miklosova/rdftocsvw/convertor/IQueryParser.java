package com.miklosova.rdftocsvw.convertor;

import org.eclipse.rdf4j.repository.RepositoryConnection;

public interface IQueryParser {
    PrefinishedOutput<RowsAndKeys> convertWithQuery(RepositoryConnection rc);
}
