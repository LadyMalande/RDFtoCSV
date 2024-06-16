package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.convertor.ConversionService;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TestSupport {
    public static void runToRDFConverter(String pathToTable, String pathToMetadata, String outputPath){
        try {
            // -m minimal is for less verbose translation - to translate only what is given in the metadata, no extra triples like row numbers unless specifically mentioned in metadata

            File pathToExecutable = new File( "src/test/resources/csv2rdf-0.4.7-standalone.jar" );
            //pathToExecutable.getAbsolutePath()
            System.out.println("command line error " + " -t "+ pathToTable+ "-u "+ pathToMetadata+"-o "+ outputPath);
            File pathToTableFile = new File(pathToTable);
            File pathToMetadataFile = new File(pathToMetadata);
            File outputPathFile = new File(outputPath);
            ProcessBuilder builder = new ProcessBuilder(  "java", "-jar", pathToExecutable.getAbsolutePath(),  " -t", pathToTableFile.getName(), "-u", pathToMetadataFile.getName(), "-o", outputPathFile.getName(), "-m", "minimal");
            builder.directory( new File( "src/test/resources" ).getAbsoluteFile() ); // this is where you set the root folder for the executable to run with
            builder.redirectErrorStream(true);
            Process process =  builder.start();

            System.out.println(process.errorReader().readLine());
            System.out.println("command line error " + process.errorReader().readLine());
            process.waitFor();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("run command line process");
    }

    public static boolean isRDFSubsetOfTerms(String filePathForTest, String filePathForOriginal){
        String query = prepareQueryForExtractAll();
        ArrayList<BindingSet> testResult = connectToDbAndPrepareQuery(filePathForTest, "rdf4j", query);
        ArrayList<BindingSet> originalResult = connectToDbAndPrepareQuery(filePathForOriginal, "rdf4j", query);

        assert testResult != null;
        System.out.println("isRDFSubsetOfTerms testResult.count = " + testResult.size());
        assert originalResult != null;
        return testResultIsSubset(testResult, originalResult);
    }

    private static boolean testResultIsSubset(ArrayList<BindingSet> testResult, ArrayList<BindingSet> originalResult) {
        System.out.println("in testResultIsSubset ");
        if(testResult.stream().count() == 0 ){
            System.out.println("in testResultIsSubset There is no testResult");
            return false;
        }
        if(originalResult.stream().count() == 0 ){
            System.out.println("in testResultIsSubset There is no originalResult");
            return false;
        }
        for (BindingSet solution : originalResult) {
            System.out.println("in for (BindingSet solution : originalResult) { ");
            String subject = solution.getBinding("s").getValue().stringValue();
            String predicate = solution.getBinding("p").getValue().stringValue();
            String object = solution.getBinding("o").getValue().stringValue();
            boolean tripleIsThere = false;
            for (BindingSet solutionTest : testResult) {

                Value subjectTest = solutionTest.getBinding("s").getValue();
                Value predicateTest = solutionTest.getBinding("p").getValue();
                Value objectTest = solutionTest.getBinding("o").getValue();
                String decodedSubjectTest = java.net.URLDecoder.decode(subjectTest.stringValue(), StandardCharsets.UTF_8);
                String decodedPredicateTest = java.net.URLDecoder.decode(predicateTest.stringValue(), StandardCharsets.UTF_8);
                String decodedObjectTest = java.net.URLDecoder.decode(objectTest.stringValue(), StandardCharsets.UTF_8);
                System.out.println("Triple " + decodedSubjectTest + " = " + subject + ", " + decodedPredicateTest + " = " + predicate + ", " + decodedObjectTest + " = "  + object);
                if(decodedSubjectTest.equals(subject) && decodedPredicateTest.equals(predicate) && decodedObjectTest.equals(object)){
                    System.out.println("Triple is there: " + decodedSubjectTest + " = " + subject + ", " + decodedPredicateTest + " = " + predicate + ", " + decodedObjectTest + " = "  + object);
                    tripleIsThere = true;
                }
            }
            if(!tripleIsThere){
                return tripleIsThere;
            }
        }
        return true;
    }

    public static ArrayList<BindingSet> connectToDbAndPrepareQuery(String filePath, String methodName, String queryString){
        ArrayList<BindingSet> results = new ArrayList<>();
        Repository db = new SailRepository(new MemoryStore());
        MethodService methodService = new MethodService();
        System.out.println("connectToDbAndPrepareQuery " + filePath);
        RepositoryConnection rc = methodService.processInput(filePath, methodName, db);
        assert(rc != null);
        // Convert the table to intermediate data for processing into metadata

        try (RepositoryConnection conn = db.getConnection()) {
            
            TupleQuery query = conn.prepareTupleQuery(queryString);
            System.out.println("query.getDataset()" + query.getDataset());
            // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
            try {
                TupleQueryResult result = query.evaluate();
                System.out.println("connectToDbAndPrepareQuery resultCount " + filePath + " resultCount = " );
                result.stream().forEach(results::add);
                System.out.println("connectToDbAndPrepareQuery resultCountinArrayList " + filePath + " resultCount = " + results.size());
                return results;
            } catch(QueryEvaluationException ex){
                QueryEvaluationException ex1 = ex;
                ex1.printStackTrace();
                return null;
            }
        }
    }
    
    private static String prepareQueryForExtractAll(){
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        SelectQuery selectQuery = Queries.SELECT();
        Variable o = SparqlBuilder.var("o"), s = SparqlBuilder.var("s"),
                p = SparqlBuilder.var("p");
        selectQuery.prefix(skos).select(s,p,o).where(s.has(p,o));
        System.out.println("prepareQueryForExtractAll query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }
}
