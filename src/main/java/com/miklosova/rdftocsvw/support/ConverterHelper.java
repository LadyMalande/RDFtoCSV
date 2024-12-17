package com.miklosova.rdftocsvw.support;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
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

import java.util.*;

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

    public void changeBNodesForIri(RepositoryConnection rc) {
        Iterator<Statement> statements = rc.getStatements(null, null, null, true).iterator();
        Map<Value, Value> mapOfBlanks = new HashMap<>();
        int counter = 0;
        int i = 0;
        while (statements.hasNext()) {
            Statement st = statements.next();
            Statement statement = null;
            IRI subj = null;
            if (st.getSubject().isBNode()) {
                if (mapOfBlanks.get(st.getSubject()) != null) {
                    ValueFactory vf = SimpleValueFactory.getInstance();
                    subj = (IRI) mapOfBlanks.get(st.getSubject());
                    statement = vf.createStatement((IRI) mapOfBlanks.get(st.getSubject()), st.getPredicate(), st.getObject());

                } else {
                    ValueFactory vf = SimpleValueFactory.getInstance();
                    IRI v = vf.createIRI("https://blank_Nodes_IRI.org/" + i);
                    i++;
                    mapOfBlanks.put(st.getSubject(), v);
                    subj = (IRI) mapOfBlanks.get(st.getSubject());
                    statement = vf.createStatement((IRI) mapOfBlanks.get(st.getSubject()), st.getPredicate(), st.getObject());
                }
            }
            if (st.getObject().isBNode()) {
                if (mapOfBlanks.get(st.getObject()) != null) {
                    ValueFactory vf = SimpleValueFactory.getInstance();
                    subj = (subj == null) ? (IRI) st.getSubject() : subj;
                    statement = vf.createStatement(subj, st.getPredicate(), mapOfBlanks.get(st.getObject()));
                    rc.add(statement);
                } else {
                    ValueFactory vf = SimpleValueFactory.getInstance();
                    IRI v = vf.createIRI("https://blank_Nodes_IRI.org/" + i);

                    mapOfBlanks.put(st.getObject(), v);
                    subj = (subj == null) ? (IRI) st.getSubject() : subj;
                    statement = vf.createStatement(subj, st.getPredicate(), mapOfBlanks.get(st.getObject()));
                    i++;
                }
            }
            if (statement != null) {
                rc.add(statement);
            }
            counter = counter + 1;
        }
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
            selectQuery.prefix(skos).select(s).where(s.has(objectIri, o ));
        }

        System.out.println("askfortypes " + askForTypes + " getSelectQuery query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

}
