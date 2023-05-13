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
import org.eclipse.rdf4j.sparqlbuilder.util.SparqlBuilderUtils;
import support.FileWrite;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class for creating CSV table with the help of SPARQL query on the RDF data
 */
public class CSVTableCreator {

    String resultCSV = "";


    public String getCSVTableAsString(){
        String query = getCSVTableQueryForModel();
        return queryRDFModel(query);
    }

    private String queryRDFModel(String queryString) {
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

            // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                for (BindingSet solution : result) {
                    // ... and print out the value of the variable binding for ?s and ?n
                    System.out.println("?job = " + solution.getValue("job"));
                }

            }
        } finally {
        // Before our program exits, make sure the database is properly shut down.
            db.shutDown();
        }

        // Verify the output in console

        System.out.println(resultString);
        saveCSVasFile("resultCSVPrimer");
        return resultString;
    }

    private String getCSVTableQueryForModel() {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);

        // Create the query to get all data in CSV format
        SelectQuery selectQuery = Queries.SELECT();
        Variable job = SparqlBuilder.var("job"), pos = SparqlBuilder.var("pos");
        selectQuery.prefix(skos).select(job).where(job.has(skos.iri("prefLabel"), pos));
        System.out.println(selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    private void saveCSVasFile(String fileName){
        File f = FileWrite.makeFileByNameAndExtension(fileName, "csv");
        FileWrite.writeTotheFile(f, resultCSV);
        System.out.println("Written CSV table to the file " + f + ".");
    }

}
