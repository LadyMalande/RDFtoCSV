package com.miklosova.rdftocsvw.convertor;

import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.FileWrite;
import lombok.extern.java.Log;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Values;
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
import org.eclipse.rdf4j.sparqlbuilder.core.query.DeleteDataQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.mapdb.Atomic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

@Log
public class SplitFilesQueryConverter implements IQueryParser{

    String resultCSV;
    ArrayList<String> roots;
    ArrayList<Row> rows;
    ArrayList<String> keys;

    public Map<String, Integer> mapOfPredicatesAndTheirNumbers;
    public Map<String, Integer> mapOfTypesAndTheirNumbers;
    String delimiter;
    String CSVFileTOWriteTo;
    String allRowsOfOutput;
    Repository db;

    Integer fileNumberX;

    public SplitFilesQueryConverter(Repository db) {
        this.keys = new ArrayList<>();
        this.db = db;
        this.fileNumberX = 0;
    }

    @Override
    public PrefinishedOutput convertWithQuery(RepositoryConnection rc) {
        loadConfiguration();

        String query = getCSVTableQueryForModel();

        String queryResult = queryRDFModel(query);
        System.out.println("CSVFileTOWriteTo: " + CSVFileTOWriteTo + "delimiter: " + delimiter);
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

        Variable o = SparqlBuilder.var("o"), s = SparqlBuilder.var("s"),
                s_in = SparqlBuilder.var("s_in"),p_in = SparqlBuilder.var("p_in");
        selectQuery.prefix(skos).select(s).where(s.isA(o).filterNotExists(s_in.has(p_in, s)));
        System.out.println("getCSVTableQueryForModel query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    private String getQueryForRoot(String root){
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        SelectQuery selectQuery = Queries.SELECT();

        SimpleValueFactory rdf = SimpleValueFactory.getInstance();
        Variable o = SparqlBuilder.var("o"), p = SparqlBuilder.var("p");
        Iri subjectIRI = iri(root);
        selectQuery.prefix(skos).select(p,o).where(subjectIRI.has(p,o));
        System.out.println("getCSVTableQueryForModel query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }


    private String queryRDFModel(String queryString) {
        rows = new ArrayList<>();
        // Query the data and pass the result as String

        // Query in rdf4j
        // Create a new Repository.

        // Open a connection to the database
        try (RepositoryConnection conn = db.getConnection()) {

            while (!conn.isEmpty()) {
                TupleQuery query = conn.prepareTupleQuery(queryString);
                System.out.println("query.getDataset()" + query.getDataset());
                // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
                try (TupleQueryResult result = query.evaluate()) {
                    // we just iterate over all solutions in the result...
                    System.out.println();
                    System.out.println(result == null);
                    System.out.println("Binding names: " + result.getBindingNames());
                    if (result == null) {
                        return null;
                    }
                    roots = new ArrayList<>();
                    //System.out.println(result.stream().count());
                    for (BindingSet solution : result) {
                        // ... and print out the value of the variable binding for ?s and ?n
                        System.out.println("?subject = " + solution.getValue("s") + " ?predicate = " + solution.getValue("predicate"));

                        roots.add(solution.getValue("s").toString());
                    }

                    //countDominantPredicates(conn, roots);
                    countDominantTypes(conn, roots);
                    String dominantType = getDominantType();
                    //String dominantPredicate = getDominantPredicate();

                    recursiveQueryForFiles(conn, dominantType);
/*
                // For all the found roots, make rows. Roots must have the same rdf:type
                for (String root : roots) {
                    // new Row with the found subject as its id
                    Row newRow = new Row(root);
                    recursiveQueryForSubjects(conn, newRow, root, null);
                    rows.add(newRow);

                }

 */
                    resultCSV = result.toString();
                    result.close();
                }
            }
        }

        // Verify the output in console

        //System.out.println(resultString);


        //saveCSVasFile("resultCSVPrimer");
        //return resultCSV;
        return allRowsOfOutput;
    }

    private void recursiveQueryForFiles(RepositoryConnection conn, String dominantType) {
        // Make new rows and keys for the current file
        rows = new ArrayList<>();
        keys = new ArrayList<>();


        System.out.println("Number of Roots : " + roots.size());
        for (String root : roots) {
            // new Row with the found subject as its id
            Row newRow = new Row(root);
            // Query the model for individual rows lead by the roots and having the predicates as the headers in the file
            queryForSubjects(conn, newRow, root, null, dominantType);
            rows.add(newRow);
            //deletePredicatesAndObjectsForSubject(conn, root, dominantType);

        }
        augmentMapsByMissingKeys();
        System.out.println("Number of Rows : " + rows.size());
        System.out.println("Number of keys : " + keys.size());
        // Write the rows with respective keys to the current file
        FileWrite.saveCSFFileFromRows(CSVFileTOWriteTo + fileNumberX, keys, rows, delimiter);
        // Increase the file number so that the next file has different name
        fileNumberX = fileNumberX+1;
    }

    private void deletePredicatesAndObjectsForSubject(RepositoryConnection conn, String root, String dominantType) {
        String queryToDeleteAllPredicatesAndObjects = getDeletePredicatesObjectsForRoot(root, dominantType);


        TupleQuery query = conn.prepareTupleQuery(queryToDeleteAllPredicatesAndObjects);

        try (TupleQueryResult result = query.evaluate()) {

            System.out.println("deleted triples from conn... ");

        }

    }

    private String getDeletePredicatesObjectsForRoot(String root, String dominantType) {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        ModifyQuery selectQuery = Queries.DELETE();

        Variable o = SparqlBuilder.var("o"), p = SparqlBuilder.var("p"), s = SparqlBuilder.var("s");
        Iri subjectIRI = iri(root);
        Iri dominantTypeIRI = iri(dominantType);

        selectQuery.prefix(skos).delete(subjectIRI.has(p,o)).where(subjectIRI.has(p,o).andIsA(dominantTypeIRI));
        System.out.println("getDeletePredicatesObjectsForRoot query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    private void queryForSubjects(RepositoryConnection conn, Row newRow, String root, Object o, String dominantType) {
        String queryToGetAllPredicatesAndObjects = getQueryToGetObjectsForRoot(root, dominantType);
        TupleQuery query = conn.prepareTupleQuery(queryToGetAllPredicatesAndObjects);
        String encloseInDoubleQuotes = null;
        try (TupleQueryResult result = query.evaluate()) {
            newRow.id = root;

            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                    // We found a root row
                System.out.println("queryForSubjects solution.getBindingNames() =  " + solution.getBindingNames());
                System.out.println("queryForSubjects solution.size() =  " + solution.size());
                System.out.println("queryForSubjects solution.getBinding(\"p\").getValue() =  " );

                if(newRow.map.containsKey(solution.getBinding("p").getValue().toString())){
                    String oldStringValue = newRow.map.get(solution.getBinding("p").getValue().toString());
                    String newStringValue = oldStringValue + "," + solution.getBinding("o").getValue().toString();
                    newRow.map.put(solution.getBinding("p").getValue().toString(),newStringValue);
                    encloseInDoubleQuotes = solution.getBinding("p").getValue().toString();
                } else {
                    newRow.map.put(solution.getBinding("p").getValue().toString(),solution.getBinding("o").getValue().toString());

                }


                    if(!keys.contains(solution.getValue("p").toString())){
                        keys.add(solution.getValue("p").toString());
                    }

                System.out.println("BindingSet solution: result " + solution.getValue("p").toString() + " " + solution.getValue("o").toString());
                Resource subject = Values.iri(root);
                IRI predicate = Values.iri(solution.getValue("p").toString());
                    conn.remove(subject,predicate, solution.getValue("o"));

            }
        }
        if(encloseInDoubleQuotes != null){
            String oldStringValue = newRow.map.get(encloseInDoubleQuotes);
            String newStringValue = "\"" + oldStringValue + "\"";
            newRow.map.put(encloseInDoubleQuotes,newStringValue);
        }


    }

    private String getQueryToGetObjectsForRoot(String root, String dominantType) {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        SelectQuery selectQuery = Queries.SELECT();

        Variable o = SparqlBuilder.var("o"), p = SparqlBuilder.var("p");
        Iri subjectIRI = iri(root);
        Iri dominantTypeIRI = iri(dominantType);
        selectQuery.prefix(skos).select(p,o).where(subjectIRI.has(p,o).andIsA(dominantTypeIRI));
        System.out.println("getCSVTableQueryForModel query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    private void countDominantTypes(RepositoryConnection conn, ArrayList<String> roots) {
        mapOfTypesAndTheirNumbers = new HashMap<>();

        System.out.println("Roots number "  + roots.size());
        for (String root : roots) {
            String queryForPredicates = getQueryForTypes(root);
            TupleQuery query = conn.prepareTupleQuery(queryForPredicates);

            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                for (BindingSet solution : result) {
                    String key = solution.getValue("o").toString();

                    if (mapOfTypesAndTheirNumbers.containsKey(key)) {
                        Integer oldValue = mapOfTypesAndTheirNumbers.get(key);
                        Integer newValue = oldValue+1;
                        mapOfTypesAndTheirNumbers.put(key, newValue);
                    } else {
                        mapOfTypesAndTheirNumbers.put(key, 1);
                    }
                }

            }
        }
    }

    private String getQueryForTypes(String root) {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        SelectQuery selectQuery = Queries.SELECT();

        SimpleValueFactory rdf = SimpleValueFactory.getInstance();
        Variable o = SparqlBuilder.var("o");
        Iri subjectIRI = iri(root);
        selectQuery.prefix(skos).select(o).where(subjectIRI.isA(o));
        System.out.println("getQueryForTypes query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    private String getDominantType() {
        String dominantType = null;
        System.out.println("getDominantType");

        List<Map.Entry<String, Integer>> sortedEnties = entriesSortedByValues(mapOfTypesAndTheirNumbers);
        for(Map.Entry<String, Integer> entry : sortedEnties){
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        dominantType = sortedEnties.get(0).getKey();
        System.out.println("Chosen dominant type is " + dominantType);

        return dominantType;
    }

    private void countDominantPredicates(RepositoryConnection conn, ArrayList<String> roots) {
        mapOfPredicatesAndTheirNumbers = new HashMap<>();

        System.out.println("Roots number "  + roots.size());
        for (String root : roots) {
            String queryForPredicates = getQueryForRoot(root);
            TupleQuery query = conn.prepareTupleQuery(queryForPredicates);

            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                for (BindingSet solution : result) {
                    String key = solution.getValue("p").toString();

                    if (mapOfPredicatesAndTheirNumbers.containsKey(key)) {
                        Integer oldValue = mapOfPredicatesAndTheirNumbers.get(key);
                        Integer newValue = oldValue+1;
                        mapOfPredicatesAndTheirNumbers.put(key, newValue);
                    } else {
                        mapOfPredicatesAndTheirNumbers.put(key, 1);
                    }
                }

            }
        }
    }

    static <K,V extends Comparable<? super V>>
    List<Map.Entry<K, V>> entriesSortedByValues(Map<K,V> map) {

        List<Map.Entry<K,V>> sortedEntries = new ArrayList<Map.Entry<K,V>>(map.entrySet());

        Collections.sort(sortedEntries,
                new Comparator<Map.Entry<K,V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                }
        );

        return sortedEntries;
    }

    private String getDominantPredicate(){
        String dominantPredicate = null;
        System.out.println("getDominantPredicate");

        List<Map.Entry<String, Integer>> sortedEnties = entriesSortedByValues(mapOfPredicatesAndTheirNumbers);
        for(Map.Entry<String, Integer> entry : sortedEnties){
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        dominantPredicate = sortedEnties.get(0).getKey();
        System.out.println("Chosen dominant predicate is " + dominantPredicate);

        return dominantPredicate;
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

    private void recursiveQueryForSubjects(RepositoryConnection conn,Row row, String object,String predicateOfIRI){

        String queryForSubjects = createQueryForSubjects(object);
        TupleQuery query = conn.prepareTupleQuery(queryForSubjects);

        try (TupleQueryResult result = query.evaluate()) {
            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                if(row.id.equals(object)){
                    row.map.put(solution.getValue("p").toString(),solution.getValue("s").toString());
                    if(!keys.contains(solution.getValue("p").toString())){
                        keys.add(solution.getValue("p").toString());
                    }
                } else {
                    // Solution for one csv only. For multiple files, this is the place where we start making new files.

                    // Header for the single csv, where the header is put together from dot notation predicates behind each other.
                    String keyOfNextLevels = predicateOfIRI + "." + solution.getValue("p").toString();
                    row.map.put(keyOfNextLevels, solution.getValue("s").toString());

                    // Add column header to the key map for the rows
                    if(!keys.contains(keyOfNextLevels)) {

                        keys.add(keyOfNextLevels);
                    }
                }

                System.out.println("BindingSet solution: result " + solution.getValue("p").toString() + " " + solution.getValue("s").toString());
                if(solution.getValue("s").isIRI()){
                    recursiveQueryForSubjects(conn, row, solution.getValue("s").toString(), solution.getValue("p").toString());
                }
            }
        }
    }

    private String createQueryForSubjects(String object) {
        SelectQuery selectQuery = Queries.SELECT();
        Iri iri = iri(object);
        Variable s = SparqlBuilder.var("s"), p = SparqlBuilder.var("p");
        selectQuery.select(s,p).where(iri.has(p, s));
        System.out.println(selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }
}