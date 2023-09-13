package convertor;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
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
import org.eclipse.rdf4j.sparqlbuilder.util.SparqlBuilderUtils;
import support.FileWrite;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

/**
 * Class for creating CSV table with the help of SPARQL query on the RDF data
 */
public class CSVTableCreator {

    String resultCSV;
    ArrayList<String> roots;
    ArrayList<Row> rows;
    ArrayList<String> keys;
    String delimiter;

    public CSVTableCreator(String delimiter) {
        this.delimiter = delimiter;
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
            String filename = "typy-pracovních-vztahů.ttl";
            try (InputStream input = CSVTableCreator.class.getResourceAsStream("/" + filename)) {
                // add the RDF data from the inputstream directly to our database
                conn.add(input, "", RDFFormat.TURTLE);
            } catch (IOException e) {
                e.printStackTrace();
            }

            TupleQuery query = conn.prepareTupleQuery(queryString);
            System.out.println(query.getDataset());
            // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                System.out.println();
                System.out.println(result == null);
                System.out.println(result.getBindingNames());
                roots = new ArrayList<>();
                //System.out.println(result.stream().count());
                for (BindingSet solution : result) {
                    // ... and print out the value of the variable binding for ?s and ?n
                    System.out.println("?o = " + solution.getValue("s"));
                    System.out.println("inside");
                    roots.add(solution.getValue("s").toString());
                }
                for (String root : roots) {
                    Row newRow = new Row(root);
                    recursiveQueryForSubjects(conn, newRow, root);
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
        saveCSFFileFromRows("resultCSVPrimerFromRows");
        //saveCSVasFile("resultCSVPrimer");
        return resultCSV;
    }

    private void recursiveQueryForSubjects(RepositoryConnection conn,Row root, String object){
        String queryForSubjects = createQueryForSubjects(object);
        TupleQuery query = conn.prepareTupleQuery(queryForSubjects);
        try (TupleQueryResult result = query.evaluate()) {
            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                root.map.put(solution.getValue("p").toString(),solution.getValue("s").toString());
                System.out.println(solution.getValue("p").toString() + " " + solution.getValue("s").toString());
                if(solution.getValue("s").isIRI()){
                    recursiveQueryForSubjects(conn, root, solution.getValue("s").toString());
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

    private String getCSVTableQueryForModel() {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);

        // Create the query to get all data in CSV format
        SelectQuery selectQuery = Queries.SELECT();
        //Variable job = SparqlBuilder.var("job"), pos = SparqlBuilder.var("pos");
        //selectQuery.prefix(skos).select(job).where(job.isA(skos.iri("ConceptScheme")));
        Variable o = SparqlBuilder.var("o"), s = SparqlBuilder.var("s"),
                 s_in = SparqlBuilder.var("s_in"),p_in = SparqlBuilder.var("p_in");
        selectQuery.prefix(skos).select(s).where(s.isA(o).filterNotExists(s_in.has(p_in, s)));
        System.out.println(selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    private void saveCSVasFile(String fileName){
        File f = FileWrite.makeFileByNameAndExtension(fileName, "csv");
        FileWrite.writeTotheFile(f, resultCSV);
        System.out.println("Written CSV table to the file " + f + ".");
    }

    private void saveCSFFileFromRows(String fileName){
        File f = FileWrite.makeFileByNameAndExtension(fileName, "csv");

        for(Row row : rows){
            StringBuilder sb = new StringBuilder();
            sb.append(row.id).append(delimiter);
            for(Map.Entry<String, String> entry : row.map.entrySet()){
                sb.append(entry.getValue()).append(delimiter);
                System.out.println("in entry set " + entry.getValue() + ".");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("\n");
            System.out.println("row: " + sb.toString() + ".");
            FileWrite.writeTotheFile(f, sb.toString());
        }
        //FileWrite.writeTotheFile(f, resultCSV);
        System.out.println("Written CSV from rows to the file " + f + ".");
    }

}
