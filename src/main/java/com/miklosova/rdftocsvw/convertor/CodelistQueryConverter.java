package com.miklosova.rdftocsvw.convertor;

import lombok.extern.java.Log;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.util.ArrayList;


@Log
public class CodelistQueryConverter implements IQueryParser {

    String resultCSV;
    ArrayList<Value> roots;
    ArrayList<Row> rows;
    ArrayList<Value> keys;
    String delimiter;
    String CSVFileTOWriteTo;
    String allRowsOfOutput;
    Repository db;

    public CodelistQueryConverter(Repository db) {
        this.keys = new ArrayList<>();
        this.db = db;
    }

    @Override
    public PrefinishedOutput convertWithQuery(RepositoryConnection rc) {
        //loadConfiguration();
        //String query = getCSVTableQueryForModel();

        //String queryResult = queryRDFModel(query);
        //FileWrite.saveCSVFileFromRows(CSVFileTOWriteTo, keys, rows, delimiter);
        //return new PrefinishedOutput(queryResult);
        return null;

    }


    /*

    private void loadConfiguration(){
        Properties prop = new Properties();
        String fileName = "./src/main/resources/app.config";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }
        delimiter = prop.getProperty("input.delimiter");
        System.out.println("READ delimiter from input.delimiter to: " + delimiter);

        CSVFileTOWriteTo = prop.getProperty("input.outputFileName");
        System.out.println("READ delimiter from input.CSVFileTOWriteTo to: " + CSVFileTOWriteTo);
    }

    private String getCSVTableQueryForModel() {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        // Create the query to get all data in CSV format
        SelectQuery selectQuery = Queries.SELECT();

        Variable o = SparqlBuilder.var("o"), s = SparqlBuilder.var("s"),
                s_in = SparqlBuilder.var("s_in"),p_in = SparqlBuilder.var("p_in");
        selectQuery.prefix(skos).select(s).where(s.isA(SKOS.CONCEPT));
        System.out.println("getCSVTableQueryForModel query string for skos:Concept\n" + selectQuery.getQueryString());
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
                    System.out.println("?s = " + solution.getValue("s"));
                    System.out.println("inside");
                    roots.add(solution.getValue("s"));
                }
                for (Value root : roots) {
                    // TODO
                    Row newRow = new Row(root, null);
                    System.out.println("Enriching root " + root);
                    recursiveQueryForSubjects(conn, newRow, root, null);
                    rows.add(newRow);

                }
                resultCSV = result.toString();
                result.close();
            }
        }

        // Verify the output in console

        //System.out.println(resultString);
        //   augmentMapsByMissingKeys();

        //saveCSVasFile("resultCSVPrimer");
        //return resultCSV;
        return allRowsOfOutput;
    }

    private void augmentMapsByMissingKeys(){
        for(Row row : rows){
            ArrayList<Value> missingKeys = new ArrayList<>();
            for(Value key : keys){
                if(!row.columns.keySet().contains(key)){
                    missingKeys.add(key);
                }
            }
            missingKeys.forEach(key -> row.columns.put(key, null));
        }
    }

    private void recursiveQueryForSubjects(RepositoryConnection conn,Row root, Value subject,String predicateOfIRI){
        String queryForSubjects = createQueryForSubjects(subject);
        TupleQuery query = conn.prepareTupleQuery(queryForSubjects);
        try (TupleQueryResult result = query.evaluate()) {
            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {

                //System.out.println("recursiveQueryForSubjects p=" + solution.getValue("p") + " o=" + solution.getValue("o"));
                if(root.id.equals(subject)){
                    if(root.columns.get(solution.getValue("p")) != null){
                        //List<Value> oldStringValue = root.map.get(solution.getBinding("p").getValue());
                        List<Value> oldStringValue = new ArrayList<>(root.columns.get(solution.getBinding("p").getValue()).values);
                        oldStringValue.add(solution.getBinding("o").getValue());
                        TypeIdAndValues oldTypeAndValues = root.columns.get(solution.getBinding("p").getValue());
                        oldTypeAndValues.values = oldStringValue;
                        root.columns.put(solution.getBinding("p").getValue(), oldTypeAndValues );
                    } else{
                        TypeOfValue newType = (solution.getBinding("o").getValue().isIRI()) ? TypeOfValue.IRI :
                                (solution.getBinding("o").getValue().isBNode()) ? TypeOfValue.BNODE : TypeOfValue.LITERAL;
                        root.columns.put(solution.getValue("p"),new TypeIdAndValues( subject, newType, new ArrayList<>(Arrays.asList(solution.getBinding("o").getValue()))));
                    }

                    if(!keys.contains(solution.getValue("p"))){
                        keys.add(solution.getValue("p"));
                    }
                } else {
                // TODO Provide sensible headers for all data in just one csv
                    TypeOfValue newType = (solution.getBinding("o").getValue().isIRI()) ? TypeOfValue.IRI :
                            (solution.getBinding("o").getValue().isBNode()) ? TypeOfValue.BNODE : TypeOfValue.LITERAL;
                    root.columns.put(solution.getValue("p"), new TypeIdAndValues(subject, newType, new ArrayList<>(Arrays.asList(solution.getBinding("o").getValue()))));
                    if(!keys.contains(solution.getValue("p"))) {

                        keys.add(solution.getValue("p"));
                    }

                }
               System.out.println("keys size = " + keys.size());
                //System.out.println(solution.getValue("p").toString() + " " + solution.getValue("o").toString());
                if(solution.getValue("o").isIRI()){
                    System.out.println("object is IRI, recursion starting ..." + solution.getValue("p").toString() + " " + solution.getValue("o").toString());
                    recursiveQueryForSubjects(conn, root, solution.getValue("o"), solution.getValue("p").toString());
                }
            }
        }
    }

    private String createQueryForSubjects(Value object) {
        SelectQuery selectQuery = Queries.SELECT();
        Iri iri = Rdf.iri(object.toString());
        Variable o = SparqlBuilder.var("o"), p = SparqlBuilder.var("p");
        selectQuery.select(o,p).where(iri.has(p, o));
        System.out.println("createQueryForSubjects: "  + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

     */
}