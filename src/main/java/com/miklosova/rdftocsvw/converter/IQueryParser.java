package com.miklosova.rdftocsvw.converter;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import org.eclipse.rdf4j.repository.RepositoryConnection;

/**
 * The interface Query parser.
 */
public interface IQueryParser {
    /**
     * Convert with query prefinished output.
     *
     * @param rc the rc
     * @return the prefinished output
     */
    PrefinishedOutput<RowsAndKeys> convertWithQuery(RepositoryConnection rc);
}
