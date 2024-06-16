package com.miklosova.rdftocsvw.convertor;

import com.miklosova.rdftocsvw.input_processor.RDFAssetManager;
import lombok.extern.java.Log;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;

import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import com.miklosova.rdftocsvw.support.FileWrite;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

/**
 * Class for creating CSV table with the help of SPARQL query on the RDF data
 */
@Log
public class CSVTableCreator {

    String resultCSV;
    ArrayList<Value> roots;
    ArrayList<Row> rows;
    ArrayList<Value> keys;
    String delimiter;
    String CSVFileTOWriteTo;
    String fileToRead;
    String allRowsOfOutput;

    public CSVTableCreator(String delimiter, String CSVFileTOWriteTo, String fileToRead) {
        this.delimiter = delimiter;
        this.CSVFileTOWriteTo = CSVFileTOWriteTo;
        this.fileToRead = fileToRead;
        this.keys = new ArrayList<>();
    }


    public String getCSVTableAsString(){
        String query = getCSVTableQueryForModel();
        return queryRDFModel(query);
    }

    private String queryRDFModel(String queryString) {
        rows = new ArrayList<>();
        // Query the data and pass the result as String
        String resultString = "";

        // Query in rdf4j
        // Create a new Repository.
        Repository db = new SailRepository(new MemoryStore());

        // Open a connection to the database
        try (RepositoryConnection conn = db.getConnection()) {

            try {
                // InputStream input = CSVTableCreator.class.getResourceAsStream("/" + fileToRead)
                File initialFile = new File(fileToRead);
                System.out.println("initial file absolute path: " + initialFile.getAbsolutePath() + "\n initial file canonical path: " + initialFile.getCanonicalPath());
                log.warning("initial file absolute path: " + initialFile.getAbsolutePath() + "\n initial file canonical path: " + initialFile.getCanonicalPath());
                InputStream targetStream = new FileInputStream(initialFile);
                RDFAssetManager ram = new RDFAssetManager();
                RDFFormat fileFormat = ram.load(fileToRead);
                if (fileFormat == null)
                    throw new RuntimeException("No loader registered for file type \"." + fileToRead + "\" files");
                // add the RDF data from the inputstream directly to our database
                log.warning("returned file format: " + fileFormat);
                conn.add(targetStream, "", fileFormat);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("\"" + fileToRead + "\" could not be loaded as the expected type");
            }

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
                    System.out.println("?o = " + solution.getValue("s"));
                    System.out.println("inside");
                    roots.add(solution.getValue("s"));
                }
                for (Value root : roots) {
                    Iri typeIri = iri("rdf:type");
                    Value typeValue = (IRI) typeIri;
                    Row newRow = new Row(root, typeValue);
                    recursiveQueryForSubjects(conn, newRow, root, null);
                    rows.add(newRow);

                }
                resultCSV = result.toString();
                result.close();
            }
        } finally {
        // Before our program exits, make sure the database is properly shut down.
            db.shutDown();
        }

        // Verify the output in console

        //System.out.println(resultString);
        augmentMapsByMissingKeys();
        saveCSFFileFromRows(CSVFileTOWriteTo);
        //saveCSVasFile("resultCSVPrimer");
        //return resultCSV;
        return allRowsOfOutput;
    }

    private void recursiveQueryForSubjects(RepositoryConnection conn,Row root, Value object,String predicateOfIRI){
        String queryForSubjects = createQueryForSubjects(object);
        TupleQuery query = conn.prepareTupleQuery(queryForSubjects);
        try (TupleQueryResult result = query.evaluate()) {
            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                if(root.id.equals(object)){
                    root.map.put(solution.getValue("p"), List.of(solution.getValue("s")));
                    if(!keys.contains(solution.getValue("p").toString())){
                        keys.add(solution.getValue("p"));
                    }
                } else {
                    /*
                    // TODO Provide sensible headers for all data in just one csv
                    String keyOfNextLevels = predicateOfIRI + "." + solution.getValue("p").toString();
                    root.map.put(keyOfNextLevels, solution.getValue("s").toString());
                    if(!keys.contains(keyOfNextLevels)) {

                        keys.add(keyOfNextLevels);
                    }

                     */
                }

                System.out.println(solution.getValue("p").toString() + " " + solution.getValue("s").toString());
                if(solution.getValue("s").isIRI()){
                    recursiveQueryForSubjects(conn, root, solution.getValue("s"), solution.getValue("p").toString());
                }
            }
        }
    }

    private String createQueryForSubjects(Value object) {
        SelectQuery selectQuery = Queries.SELECT();
        Iri iri = Rdf.iri(object.toString());
        Variable s = SparqlBuilder.var("s"), p = SparqlBuilder.var("p");
        selectQuery.select(s,p).where(iri.has(p, s));
        System.out.println(selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    private void augmentMapsByMissingKeys(){
        for(Row row : rows){
            ArrayList<Value> missingKeys = new ArrayList<>();
            for(Value key : keys){
                if(!row.map.keySet().contains(key)){
                    missingKeys.add(key);
                }
            }
            missingKeys.forEach(key -> row.map.put(key, null));
        }
    }

    private String getCSVTableQueryForModel() {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);

        // Create the query to get all data in CSV format
        SelectQuery selectQuery = Queries.SELECT();
        //Variable job = SparqlBuilder.var("job"), pos = SparqlBuilder.var("pos");
        //selectQuery.prefix(skos).select(job).where(job.isA(skos.iri("ConceptScheme")));
        Variable o = SparqlBuilder.var("o"), s = SparqlBuilder.var("s"),
                 s_in = SparqlBuilder.var("s_in"),p_in = SparqlBuilder.var("p_in");
        selectQuery.prefix(skos).select(s).where(s.isA(o).filterNotExists(s_in.has(p_in, s)));
        System.out.println("getCSVTableQueryForModel query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    private void saveCSVasFile(String fileName){
        File f = FileWrite.makeFileByNameAndExtension(fileName, "csv");
        FileWrite.writeToTheFile(f, resultCSV);
        System.out.println("Written CSV table to the file " + f + ".");
    }

    private void saveCSFFileFromRows(String fileName){
        StringBuilder forOutput = new StringBuilder();
        //File f = FileWrite.makeFileByNameAndExtension("../" + fileName, "csv");
        File f = FileWrite.makeFileByNameAndExtension( fileName, "csv");

        StringBuilder sb1 = new StringBuilder();
        sb1.append("id" + delimiter);
        keys.forEach(key -> sb1.append(key + delimiter));
        sb1.deleteCharAt(sb1.length() - 1);
        sb1.append("\n");
        FileWrite.writeToTheFile(f, sb1.toString());
        for(Row row : rows){
            StringBuilder sb = new StringBuilder();
            sb.append(row.id).append(delimiter);
            for(Value key : keys){
                sb.append(row.map.get(key)).append(delimiter);
                System.out.println("in entry set " + row.map.get(key) + ".");

            }

            sb.deleteCharAt(sb.length() - 1);
            sb.append("\n");
            System.out.println("row: " + sb.toString() + ".");
            FileWrite.writeToTheFile(f, sb.toString());
            forOutput.append(sb.toString());
        }
        allRowsOfOutput = forOutput.toString();
        //FileWrite.writeTotheFile(f, resultCSV);
        System.out.println("Written CSV from rows to the file " + f + ".");
    }

}
