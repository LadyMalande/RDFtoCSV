package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.converter.ConversionService;
import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.RowAndKey;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.MetadataService;
import com.miklosova.rdftocsvw.output_processor.FileWrite;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Repository db = new SailRepository(new MemoryStore());

        AppConfig config = new AppConfig.Builder(filePathForTest).build();
        RepositoryConnection rc1 = parseInput(filePathForTest, DEFAULT_READ_METHOD, db, config);
        RepositoryConnection rc2 = parseInput(filePathForOriginal, DEFAULT_READ_METHOD, db, config);

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

        String readLine;
        try (BufferedReader br = new BufferedReader(new java.io.FileReader(fileName))) {
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
        String allFiles = metadata.getConfig().getIntermediateFileNames();
        try {
            RowsAndKeys rnk = (RowsAndKeys) prefinishedOutput.getPrefinishedOutput();

            for (String filename : allFiles.split(",")) {
                System.out.println("newFileName " + filename);
                FileWrite.saveCSVFileFromRows(filename, rnk.getRowsAndKeys().get(0).getRows(), metadata, metadata.getConfig());
            }
        } catch (ClassCastException ex) {
            RowAndKey rnk = (RowAndKey) prefinishedOutput.getPrefinishedOutput();
            String filename = allFiles.split(",")[0];
            System.out.println("newFileName " + filename);
            FileWrite.saveCSVFileFromRows(filename, rnk.getRows(), metadata, metadata.getConfig());
        }


    }

    // Utility method to check if two files are equal
    public static void assertFilesEqual(File expectedFile, File actualFile) throws IOException {
        // Check if the files exist
        assertTrue(expectedFile.exists(), "Expected file does not exist.");
        assertTrue(actualFile.exists(), "Actual file does not exist.");

        // Compare the contents of the two files
        try (BufferedReader expectedReader = new BufferedReader(new FileReader(expectedFile));
             BufferedReader actualReader = new BufferedReader(new FileReader(actualFile))) {

            String expectedLine;
            String actualLine;
            while ((expectedLine = expectedReader.readLine()) != null) {
                actualLine = actualReader.readLine();
                assertNotNull(actualLine, "Actual file has fewer lines than expected.");
                assertEquals(expectedLine, actualLine, "File contents do not match.");
            }
            assertNull(actualReader.readLine(), "Actual file has more lines than expected.");
        }
    }

    public static boolean isFile1ContainedInFile2(Path file1, Path file2) {
        try {
            // Read lines from file1 and file2
            List<String> file1Lines = Files.readAllLines(file1);
            List<String> file2Lines = Files.readAllLines(file2);

            // Convert file2Lines to a Set for efficient checking of containment
            Set<String> file2ProcessedLines = new HashSet<>();
            for (String line : file2Lines) {
                file2ProcessedLines.add(removeWhitespace(line));
            }

            // Check if all processed lines from file1 exist in processed lines from file2
            for (String line : file1Lines) {
                if (!file2ProcessedLines.contains(removeWhitespace(line))) {
                    System.err.println(Arrays.toString(new Set[]{file2ProcessedLines}) + " does not match this line: \n" + removeWhitespace(line));
                    return false; // Found a line in file1 not in file2 (ignoring whitespace)
                }
            }

            return true; // All lines from file1 are found in file2
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Return false if an error occurs during file reading
        }
    }

    public static Class<?> getClassByName(String className){
        try {

            // Get the Class object
            Class<?> clazz = Class.forName(className);

            // Print some information about the class
            System.out.println("Class Name: " + clazz.getName());
            System.out.println("Is Interface: " + clazz.isInterface());
            System.out.println("Is Array: " + clazz.isArray());
            System.out.println("Superclass: " + clazz.getSuperclass());
            return clazz;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Removes spaces and tabs from a given string.
     *
     * @param input The input string.
     * @return A string with all spaces and tabs removed.
     */
    private static String removeWhitespace(String input) {
        return input.replaceAll("[ \t]", ""); // Replace spaces and tabs with an empty string
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

    public static PrefinishedOutput<RowsAndKeys> createPrefinishedOutput(String filePath, String filePathForMetadata, String filePathForOutput, String PROCESS_METHOD, Repository db, String[] args, AppConfig config) {
        System.out.println("Override before each");
        config.setOutputMetadataFileName(filePathForMetadata);
        config.setOutputFileName(filePathForOutput);

        MethodService methodService = new MethodService(config);
        RepositoryConnection rc = null;
        try {
            rc = methodService.processInput(filePath, PROCESS_METHOD, db);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assert (rc != null);
        // Convert the table to intermediate data for processing into metadata
        ConversionService cs = new ConversionService(config);
        System.out.println("createMetadata @BeforeEach");
        return cs.convertByQuery(rc, db);

    }

    public static Metadata createMetadata(PrefinishedOutput<RowsAndKeys> prefinishedOutput, AppConfig config) {

        // Convert intermediate data into basic metadata
        MetadataService ms = new MetadataService(config);

        return ms.createMetadata(prefinishedOutput);
    }

    public static TupleQueryResult getResultOfQuery(RepositoryConnection conn, String query) {
        TupleQuery tupleResult = conn.prepareTupleQuery(query);

        return tupleResult.evaluate();
    }

    public static ArrayList<BindingSet> connectToDbAndPrepareQuery(String filePath, String methodName, String queryString, AppConfig config) {

        ArrayList<BindingSet> results = new ArrayList<>();
        Repository db = new SailRepository(new MemoryStore());
        MethodService methodService = new MethodService(config);
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

    public static RepositoryConnection parseInput(String fileName, String readMethod, Repository db, AppConfig config) throws IOException {
        db = new SailRepository(new MemoryStore());
        MethodService methodService = new MethodService(config);
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
        FileOutputStream out = new FileOutputStream("./src/test/resources/differentSerializations/testingInput.brf");
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
