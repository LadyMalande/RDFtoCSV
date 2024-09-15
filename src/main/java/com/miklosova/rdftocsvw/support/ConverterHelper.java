package com.miklosova.rdftocsvw.support;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

public class ConverterHelper {
    protected static boolean rootHasThisType(List<Value> rootsThatHaveSomeType, Value root) {
        return rootsThatHaveSomeType.contains(root);
    }

    protected static List<Value> rootsThatHaveThisType(RepositoryConnection conn, Value dominantType, boolean askForTypes) {
        List<Value> compliantRoots = new ArrayList<>();
        String queryForPredicates = getSelectQuery(askForTypes, null, null, dominantType.toString());
        TupleQuery query = conn.prepareTupleQuery(queryForPredicates);

        try (TupleQueryResult result = query.evaluate()) {
            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                compliantRoots.add(solution.getValue("s"));
            }
        }
        return compliantRoots;
    }

    private static String getSelectQuery(boolean askForTypes, String subject, String predicate, String object) {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        // Create the query to get all data in CSV format
        SelectQuery selectQuery = Queries.SELECT();

        Variable o = SparqlBuilder.var("o"), s = SparqlBuilder.var("s"),
                s_in = SparqlBuilder.var("s_in"), p_in = SparqlBuilder.var("p_in"),
                p = SparqlBuilder.var("p");
        ;
        Iri objectIri = iri(object);
        if (askForTypes) {
            selectQuery.prefix(skos).select(s).where(s.isA(objectIri));
        } else {
            selectQuery.prefix(skos).select(s).where(s.isA(objectIri));
        }

        //System.out.println("getCSVTableQueryForModel query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

}
