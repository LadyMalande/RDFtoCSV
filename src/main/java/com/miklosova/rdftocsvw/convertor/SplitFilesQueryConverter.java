package com.miklosova.rdftocsvw.convertor;

import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.FileWrite;
import lombok.extern.java.Log;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

@Log
public class SplitFilesQueryConverter implements IQueryParser{

    String resultCSV;
    ArrayList<String> roots;
    ArrayList<Row> rows;
    ArrayList<String> keys;
    String delimiter;
    String CSVFileTOWriteTo;
    String allRowsOfOutput;
    Repository db;

    public SplitFilesQueryConverter(Repository db) {
        this.keys = new ArrayList<>();
        this.db = db;
    }

    @Override
    public PrefinishedOutput convertWithQuery(RepositoryConnection rc) {
        loadConfiguration();

        String query = getCSVTableQueryForModel();

        String queryResult = queryRDFModel(query);
        log.warning("CSVFileTOWriteTo: " + CSVFileTOWriteTo + "delimiter: " + delimiter);
        FileWrite.saveCSFFileFromRows(CSVFileTOWriteTo, keys, rows, delimiter);
        return new PrefinishedOutput(queryResult);

    }

    private void loadConfiguration(){

        delimiter = ConfigurationManager.getVariableFromConfigFile("input.delimiter");
        System.out.println("READ delimiter from input.delimiter to: " + delimiter);

        CSVFileTOWriteTo = ConfigurationManager.getVariableFromConfigFile("input.outputFileName");
        System.out.println("READ delimiter from input.CSVFileTOWriteTo to: " + CSVFileTOWriteTo);
    }

    private String getCSVTableQueryForModel() {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        // Create the query to get all data in CSV format
        SelectQuery selectQuery = Queries.SELECT();

        Variable predicate = SparqlBuilder.var("predicate"), subject = SparqlBuilder.var("subject"),
                o = SparqlBuilder.var("o"),p_in = SparqlBuilder.var("dist_subj"),
                o2 = SparqlBuilder.var("o2"),subject2 = SparqlBuilder.var("subject2");
        Expression countAgg = Expressions.count(subject).distinct();

        //selectQuery.prefix(skos).select(predicate, countAgg.as(p_in)).where(subject.has(predicate, o).and(subject2.has(predicate, o2))).groupBy(predicate);

        System.out.println("splitfiles query getCSVTableQueryForModel query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    private String queryRDFModel(String queryString) {
        rows = new ArrayList<>();
        // Query the data and pass the result as String

        // Query in rdf4j
        // Create a new Repository.

        // Open a connection to the database
        try (RepositoryConnection conn = db.getConnection()) {

            TupleQuery query = conn.prepareTupleQuery(queryString);
            System.out.println(query.getDataset());
            // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                System.out.println();
                System.out.println(result == null);
                System.out.println("Binding names: " + result.getBindingNames());
                roots = new ArrayList<>();
                //System.out.println(result.stream().count());
                for (BindingSet solution : result) {
                    // ... and print out the value of the variable binding for ?s and ?n
                    System.out.println("?subject = " + solution.getValue("subject") + " ?predicate = " + solution.getValue("predicate"));
                    System.out.println("inside");
                    roots.add(solution.getValue("subject").toString());
                }
                for (String root : roots) {
                    Row newRow = new Row(root);
                    recursiveQueryForSubjects(conn, newRow, root, null);
                    rows.add(newRow);

                }
                resultCSV = result.toString();
                result.close();
            }
        }

        // Verify the output in console

        //System.out.println(resultString);
        augmentMapsByMissingKeys();

        //saveCSVasFile("resultCSVPrimer");
        //return resultCSV;
        return allRowsOfOutput;
    }

    private void augmentMapsByMissingKeys(){
        for(Row row : rows){
            ArrayList<String> missingKeys = new ArrayList<>();
            for(String key : keys){
                if(!row.map.keySet().contains(key)){
                    missingKeys.add(key);
                }
            }
            missingKeys.forEach(key -> row.map.put(key, ""));
        }
    }

    private void recursiveQueryForSubjects(RepositoryConnection conn,Row root, String object,String predicateOfIRI){
        String queryForSubjects = createQueryForSubjects(object);
        TupleQuery query = conn.prepareTupleQuery(queryForSubjects);
        try (TupleQueryResult result = query.evaluate()) {
            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                if(root.id.equals(object)){
                    root.map.put(solution.getValue("p").toString(),solution.getValue("s").toString());
                    if(!keys.contains(solution.getValue("p").toString())){
                        keys.add(solution.getValue("p").toString());
                    }
                } else {
                    String keyOfNextLevels = predicateOfIRI + "." + solution.getValue("p").toString();
                    root.map.put(keyOfNextLevels, solution.getValue("s").toString());
                    if(!keys.contains(keyOfNextLevels)) {

                        keys.add(keyOfNextLevels);
                    }
                }

                System.out.println(solution.getValue("p").toString() + " " + solution.getValue("s").toString());
                if(solution.getValue("s").isIRI()){
                    recursiveQueryForSubjects(conn, root, solution.getValue("s").toString(), solution.getValue("p").toString());
                }
            }
        }
    }

    private String createQueryForSubjects(String object) {
        SelectQuery selectQuery = Queries.SELECT();
        Iri iri = Rdf.iri(object);
        Variable s = SparqlBuilder.var("s"), p = SparqlBuilder.var("p");
        selectQuery.select(s,p).where(iri.has(p, s));
        System.out.println(selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }
}
