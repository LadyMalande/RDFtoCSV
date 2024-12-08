package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.convertor.ConversionService;
import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.convertor.RowAndKey;
import com.miklosova.rdftocsvw.convertor.RowsAndKeys;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.MetadataService;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TestSupport {
    public static final String DEFAULT_READ_METHOD = "rdf4j";

    public static void runToRDFConverter(String pathToTable, String pathToMetadata, String outputPath) {
        try {
            // -m minimal is for less verbose translation - to translate only what is given in the metadata, no extra triples like row numbers unless specifically mentioned in metadata

            File pathToExecutable = new File("src/test/resources/OriginalIsSubsetOfCSV/csv2rdf-0.4.7-standalone.jar");
            //pathToExecutable.getAbsolutePath()
            System.out.println("command line error " + " -t " + pathToTable + "-u " + pathToMetadata + "-o " + outputPath);
            File pathToTableFile = new File(pathToTable);
            File pathToMetadataFile = new File(pathToMetadata);
            File outputPathFile = new File(outputPath);
            // rdf serialize --input-format tabular --output-format turtle --minimal --metadata csv-metadata.json
            //ProcessBuilder builder = new ProcessBuilder(  "java", "-jar", pathToExecutable.getAbsolutePath(),  " -t", pathToTableFile.getName(), "-u", pathToMetadataFile.getName(), "-o", outputPathFile.getName(), "-m", "minimal");
            ProcessBuilder builder = new ProcessBuilder("rdf serialize", "--input-format", "tabular", "--output-format", "turtle", "--minimal", "--metadata", "OriginalIsSubsetOfCSV/csv-metadata.json");
            ProcessBuilder builder2 = new ProcessBuilder("gem", "install", "rdf-tabular", "rdf-turtle");

            builder.directory(new File("src/test/resources").getAbsoluteFile()); // this is where you set the root folder for the executable to run with
            builder.redirectErrorStream(true);
            // Process process =  builder.start();
            builder2.directory(new File("src/test/resources").getAbsoluteFile()); // this is where you set the root folder for the executable to run with
            builder2.redirectErrorStream(true);
            Process process2 = builder2.start();
            //System.out.println(process.errorReader().readLine());
            //System.out.println("command line error " + process.errorReader().readLine());
            //process.waitFor();
            process2.waitFor();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("run command line process");
    }

    public static boolean isRDFSubsetOfTerms(String filePathForTest, String filePathForOriginal) throws IOException {
        ConfigurationManager.loadSettingsFromInputToConfigFile(new String[]{"-f", filePathForTest, "-p", "rdf4j"});
        Repository db = new SailRepository(new MemoryStore());
        System.out.println(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD));
        System.out.println(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.READ_METHOD));
        RepositoryConnection rc1 = parseInput(filePathForTest, DEFAULT_READ_METHOD, db);
        RepositoryConnection rc2 = parseInput(filePathForOriginal, DEFAULT_READ_METHOD, db);

        String query = prepareQueryForExtractAll();
        TupleQueryResult result1 = getResultOfQuery(rc1, query);
        TupleQueryResult result2 = getResultOfQuery(rc2, query);
        List<BindingSet> set1 = result1.stream().toList();
        List<BindingSet> set2 = result2.stream().toList();
        //ArrayList<BindingSet> testResult = connectToDbAndPrepareQuery(filePathForTest, "rdf4j", query);
        //ArrayList<BindingSet> originalResult = connectToDbAndPrepareQuery(filePathForOriginal, "rdf4j", query);

        //assert testResult != null;
        //System.out.println("isRDFSubsetOfTerms testResult.count = " + testResult.size());
        //assert originalResult != null;
        db.shutDown();
        //return testResultIsSubset(testResult, originalResult);
        return testResultIsSubset(set1, set2);
    }

    public static boolean isFileEmpty(String fileName) {
        boolean isEmpty = true;

        BufferedReader br;
        String readLine;
        try {
            br = new BufferedReader(new java.io.FileReader(fileName));
            readLine = br.readLine();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (readLine != null) {
            isEmpty = false;
        }
        return isEmpty;
    }

    public static void writeToFile(PrefinishedOutput prefinishedOutput, Metadata metadata) {
        String allFiles = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES);
        try {
            RowsAndKeys rnk = (RowsAndKeys) prefinishedOutput.getPrefinishedOutput();

            for (String filename : allFiles.split(",")) {
                System.out.println("newFileName " + filename);
                FileWrite.saveCSVFileFromRows(filename, rnk.getRowsAndKeys().get(0).getRows(), metadata);
            }
        } catch (ClassCastException ex) {
            RowAndKey rnk = (RowAndKey) prefinishedOutput.getPrefinishedOutput();
            String filename = allFiles.split(",")[0];
            System.out.println("newFileName " + filename);
            FileWrite.saveCSVFileFromRows(filename, rnk.getRows(), metadata);
        }


    }

    private static boolean testResultIsSubset(List<BindingSet> testResult, List<BindingSet> originalResult) {
        System.out.println("in testResultIsSubset ");
        System.out.println("testResult.size = " + testResult.size() + " originalResult = " + originalResult.size());
        if (testResult.isEmpty()) {
            System.out.println("in testResultIsSubset There is no testResult");
            return false;
        }
        if (originalResult.isEmpty()) {
            //System.out.println("in testResultIsSubset There is no originalResult");
            return false;
        }
        for (BindingSet solution : originalResult) {
            //System.out.println("in for (BindingSet solution : originalResult) { ");
            String subject = solution.getBinding("s").getValue().stringValue();
            String predicate = solution.getBinding("p").getValue().stringValue();
            String object = solution.getBinding("o").getValue().stringValue();
            boolean tripleIsThere = false;
            for (BindingSet solutionTest : testResult) {

                Value subjectTest = solutionTest.getBinding("s").getValue();
                Value predicateTest = solutionTest.getBinding("p").getValue();
                Value objectTest = solutionTest.getBinding("o").getValue();
                //System.out.println("Test subject before normalization " + subjectTest.toString());
/*
                String decodedSubjectTest = subjectTest.stringValue();
                String decodedPredicateTest = predicateTest.stringValue();
                String decodedObjectTest = objectTest.stringValue();


 */
                String decodedSubjectTest = java.net.URLDecoder.decode(subjectTest.stringValue(), StandardCharsets.UTF_8);
                String decodedPredicateTest = java.net.URLDecoder.decode(predicateTest.stringValue(), StandardCharsets.UTF_8);
                String decodedObjectTest = java.net.URLDecoder.decode(objectTest.stringValue(), StandardCharsets.UTF_8);
                //
                //
                if (decodedSubjectTest.equalsIgnoreCase(subject)) {
                    System.out.println("Triple1 " + decodedSubjectTest + " = " + subject);
                    System.out.println("Triple2 " + decodedPredicateTest + " = " + predicate);
                    System.out.println("Triple3 " + decodedObjectTest + " = " + object);
                }
                //System.out.println("Test subject after normalization " + decodedSubjectTest);
                if (decodedSubjectTest.equalsIgnoreCase(subject) && decodedPredicateTest.equalsIgnoreCase(predicate) && decodedObjectTest.equalsIgnoreCase(object)) {
                    System.out.println("Triple is there: " + decodedSubjectTest + " = " + subject + ", " + decodedPredicateTest + " = " + predicate + ", " + decodedObjectTest + " = " + object);
                    tripleIsThere = true;
                    //break;
                } else {
                    //

                }
            }
            if (!tripleIsThere) {
                System.out.println("Triple is NOT there: " + subject + ", " + predicate + ", " + object);
                return tripleIsThere;
            }
        }
        return true;
    }

    public static PrefinishedOutput<RowsAndKeys> createPrefinishedOutput(String filePath, String filePathForMetadata, String filePathForOutput, String PROCESS_METHOD, Repository db, String[] args) {
        System.out.println("Override before each");
        ConfigurationManager.loadSettingsFromInputToConfigFile(args);
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME, filePathForMetadata);
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_FILENAME, filePathForOutput);

        MethodService methodService = new MethodService();
        RepositoryConnection rc = null;
        try {
            rc = methodService.processInput(filePath, PROCESS_METHOD, db);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assert (rc != null);
        // Convert the table to intermediate data for processing into metadata
        ConversionService cs = new ConversionService();
        System.out.println("createMetadata @BeforeEach");
        return cs.convertByQuery(rc, db);

    }

    public static Metadata createMetadata(PrefinishedOutput<RowsAndKeys> prefinishedOutput) {

        // Convert intermediate data into basic metadata
        MetadataService ms = new MetadataService();

        return ms.createMetadata(prefinishedOutput);
    }

    public static TupleQueryResult getResultOfQuery(RepositoryConnection conn, String query) {
        TupleQuery tupleResult = conn.prepareTupleQuery(query);

        return tupleResult.evaluate();
    }

    public static ArrayList<BindingSet> connectToDbAndPrepareQuery(String filePath, String methodName, String queryString) {

        ArrayList<BindingSet> results = new ArrayList<>();
        Repository db = new SailRepository(new MemoryStore());
        MethodService methodService = new MethodService();
        System.out.println("connectToDbAndPrepareQuery " + filePath);
        RepositoryConnection rc = null;
        try {
            rc = methodService.processInput(filePath, methodName, db);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assert (rc != null);
        // Convert the table to intermediate data for processing into metadata

        try (RepositoryConnection conn = db.getConnection()) {

            TupleQuery query = conn.prepareTupleQuery(queryString);
            System.out.println("query.getDataset()" + query.getDataset());
            // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
            try {
                TupleQueryResult result = query.evaluate();
                System.out.println("connectToDbAndPrepareQuery resultCount " + filePath + " resultCount = ");
                result.stream().forEach(results::add);
                System.out.println("connectToDbAndPrepareQuery resultCountinArrayList " + filePath + " resultCount = " + results.size());
                return results;
            } catch (QueryEvaluationException ex) {
                QueryEvaluationException ex1 = ex;
                ex1.printStackTrace();
                return null;
            }
        }
    }

    public static RepositoryConnection parseInput(String fileName, String readMethod, Repository db) throws IOException {
        db = new SailRepository(new MemoryStore());
        MethodService methodService = new MethodService();
        RepositoryConnection rc = methodService.processInput(fileName, readMethod, db);
        assert (rc != null);
        return rc;


    }

    public static Model parseInputByRio(String fileName) throws IOException {

        InputStream inputStream = new FileInputStream(new File(fileName));
        Model results = Rio.parse(inputStream, "", RDFFormat.TURTLE);
        return results;
    }

    public static void createSerialization(String filename, RDFFormat format, Model model) throws IOException {
        FileOutputStream out = new FileOutputStream("./src/test/resources/typy-pracovních-vztahů_soubory/testingInput.brf");
        try {
            Rio.write(model, out, RDFFormat.BINARY);
        } finally {
            out.close();
        }

    }

    private static String prepareQueryForExtractAll() {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        SelectQuery selectQuery = Queries.SELECT();
        Variable o = SparqlBuilder.var("o"), s = SparqlBuilder.var("s"),
                p = SparqlBuilder.var("p");
        selectQuery.prefix(skos).select(s, p, o).where(s.has(p, o));
        System.out.println("prepareQueryForExtractAll query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    public File transformFileFromMetadataToRDF(String directoryPath, String metadataFileName, String resultingRDFFileName) {
        try {
            // -m minimal is for less verbose translation - to translate only what is given in the metadata, no extra triples like row numbers unless specifically mentioned in metadata

            File pathToExecutable = new File(directoryPath + "csv2rdf-0.4.7-standalone.jar");
            File pathToOutput = new File(directoryPath + resultingRDFFileName);
            File pathToMetadata = new File(directoryPath + metadataFileName);
            //pathToExecutable.getAbsolutePath()
            ProcessBuilder builder = new ProcessBuilder("java", "-jar", pathToExecutable.getAbsolutePath(), "-u", pathToMetadata.getAbsolutePath(), "-o", pathToOutput.getAbsolutePath(), "-m", "minimal");
            builder.directory(new File("src/test/resources").getAbsoluteFile()); // this is where you set the root folder for the executable to run with
            builder.redirectErrorStream(true);
            Process process = builder.start();

            //System.out.println("command line error " + process.errorReader().readLine());
            return pathToOutput;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
