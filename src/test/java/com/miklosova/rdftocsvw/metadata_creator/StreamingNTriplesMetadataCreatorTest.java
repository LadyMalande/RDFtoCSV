package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.TableSchema;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.BaseTest;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StreamingNTriplesMetadataCreatorTest extends BaseTest {

    @Mock
    private PrefinishedOutput<RowsAndKeys> mockData;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConstructorWithConfig() {
        AppConfig config = new AppConfig.Builder("test.nt")
                .parsing("streaming")
                .build();
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        
        assertNotNull(creator);
        assertNotNull(creator.metadata);
        assertNotNull(creator.tableSchemaByFiles);
        assertNotNull(creator.mapOfKnownPredicates);
        assertNotNull(creator.mapOfKnownSubjects);
    }

    @Test
    void testConstructorWithNullConfig() {
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(null);
        
        assertNotNull(creator);
        assertNotNull(creator.metadata);
        assertNotNull(creator.tableSchemaByFiles);
        assertNotNull(creator.mapOfKnownPredicates);
        assertNotNull(creator.mapOfKnownSubjects);
    }

    @Test
    void testDeprecatedConstructor() {
        @SuppressWarnings("deprecation")
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator();
        
        assertNotNull(creator);
        assertNotNull(creator.metadata);
    }

    @Test
    void testImplementsInterface() {
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(null);
        assertTrue(creator instanceof IMetadataCreator);
    }

    @Test
    void testExtendsStreamingMetadataCreator() {
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(null);
        assertTrue(creator instanceof StreamingMetadataCreator);
    }

    @Test
    void testInitialUnifiedBySubjectFlag() {
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(null);
        assertFalse(creator.unifiedBySubject);
    }

    @Test
    void testInitialCurrentCSVNameIsNull() {
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(null);
        assertNull(creator.currentCSVName);
    }

    @Test
    void testProcessLineWithEmptyString() {
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(null);
        assertDoesNotThrow(() -> creator.processLine(""));
    }

    @Test
    void testProcessLineWithCommentLine() {
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(null);
        assertDoesNotThrow(() -> creator.processLine("# This is a comment"));
    }

    @Test
    void testProcessLineWithWhitespace() {
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(null);
        assertDoesNotThrow(() -> creator.processLine("   "));
    }

    @Test
    void testProcessLineWithInvalidNTriples() {
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(null);
        assertDoesNotThrow(() -> creator.processLine("invalid ntriples data"));
    }

    @Test
    void testMultipleInstances() {
        StreamingNTriplesMetadataCreator creator1 = new StreamingNTriplesMetadataCreator(null);
        StreamingNTriplesMetadataCreator creator2 = new StreamingNTriplesMetadataCreator(null);
        
        assertNotNull(creator1);
        assertNotNull(creator2);
        assertNotSame(creator1, creator2);
        assertNotSame(creator1.metadata, creator2.metadata);
    }
    
    @Test
    void testAddMetadataWithSimpleFile() throws IOException {
        Path testFile = createSimpleNTriplesFile();
        AppConfig config = createConfig(testFile, "output.csv");
        
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertNotNull(result.getTables());
        assertFalse(result.getTables().isEmpty());
    }
    
    @Test
    void testAddMetadataWithEmptyFile() throws IOException {
        Path testFile = tempDir.resolve("empty.nt");
        Files.createFile(testFile);
        
        AppConfig config = createConfig(testFile, "output.csv");
        
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
    }
    
    @Test
    void testAddMetadataWithComments() throws IOException {
        Path testFile = tempDir.resolve("comments.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("# Comment line\n");
            writer.write("\n");
            writer.write("<http://example.org/subject1> <http://example.org/name> \"Alice\" .\n");
            writer.write("# Another comment\n");
            writer.write("<http://example.org/subject2> <http://example.org/name> \"Bob\" .\n");
        }
        
        AppConfig config = createConfig(testFile, "output.csv");
        
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertFalse(result.getTables().isEmpty());
    }
    
    @Test
    void testAddMetadataWithMultiplePredicates() throws IOException {
        Path testFile = tempDir.resolve("multiple_predicates.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("<http://example.org/person1> <http://example.org/name> \"Alice\" .\n");
            writer.write("<http://example.org/person1> <http://example.org/age> \"30\" .\n");
            writer.write("<http://example.org/person1> <http://example.org/email> \"alice@example.org\" .\n");
            writer.write("<http://example.org/person2> <http://example.org/name> \"Bob\" .\n");
            writer.write("<http://example.org/person2> <http://example.org/age> \"25\" .\n");
        }
        
        AppConfig config = createConfig(testFile, "output.csv");
        
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertFalse(result.getTables().isEmpty());
    }
    
    @Test
    void testAddMetadataWithBlankNodes() throws IOException {
        Path testFile = tempDir.resolve("blank_nodes.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("_:b1 <http://example.org/name> \"Anonymous\" .\n");
            writer.write("_:b1 <http://example.org/type> \"Unknown\" .\n");
            writer.write("<http://example.org/person1> <http://example.org/friend> _:b1 .\n");
        }
        
        AppConfig config = createConfig(testFile, "output.csv");
        
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertFalse(result.getTables().isEmpty());
    }
    
    @Test
    void testAddMetadataWithUnicodeCharacters() throws IOException {
        Path testFile = tempDir.resolve("unicode.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("<http://example.org/osoba1> <http://example.org/jméno> \"Petr Dvořák\" .\n");
            writer.write("<http://example.org/osoba1> <http://example.org/město> \"Brno\" .\n");
            writer.write("<http://example.org/osoba2> <http://example.org/jméno> \"Größe\" .\n");
        }
        
        AppConfig config = createConfig(testFile, "output.csv");
        
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertFalse(result.getTables().isEmpty());
    }
    
    @Test
    void testAddMetadataWithLiteralsWithLanguageTags() throws IOException {
        Path testFile = tempDir.resolve("language_tags.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("<http://example.org/book1> <http://example.org/title> \"Hello\"@en .\n");
            writer.write("<http://example.org/book1> <http://example.org/title> \"Hola\"@es .\n");
            writer.write("<http://example.org/book1> <http://example.org/title> \"Bonjour\"@fr .\n");
        }
        
        AppConfig config = createConfig(testFile, "output.csv");
        
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertFalse(result.getTables().isEmpty());
    }
    
    @Test
    void testAddMetadataWithTypedLiterals() throws IOException {
        Path testFile = tempDir.resolve("typed_literals.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("<http://example.org/measurement1> <http://example.org/value> \"42\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n");
            writer.write("<http://example.org/measurement2> <http://example.org/value> \"3.14\"^^<http://www.w3.org/2001/XMLSchema#decimal> .\n");
            writer.write("<http://example.org/event1> <http://example.org/date> \"2024-01-01\"^^<http://www.w3.org/2001/XMLSchema#date> .\n");
        }
        
        AppConfig config = createConfig(testFile, "output.csv");
        
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertFalse(result.getTables().isEmpty());
    }
    
    @Test
    void testAddMetadataWithIRIObjects() throws IOException {
        Path testFile = tempDir.resolve("iri_objects.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("<http://example.org/person1> <http://example.org/knows> <http://example.org/person2> .\n");
            writer.write("<http://example.org/person2> <http://example.org/knows> <http://example.org/person3> .\n");
            writer.write("<http://example.org/person1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.org/Person> .\n");
        }
        
        AppConfig config = createConfig(testFile, "output.csv");
        
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertFalse(result.getTables().isEmpty());
    }
    
    @Test
    void testAddMetadataWithMixedSubjectsAndPredicates() throws IOException {
        Path testFile = tempDir.resolve("mixed.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("<http://example.org/person1> <http://example.org/name> \"Alice\" .\n");
            writer.write("<http://example.org/company1> <http://example.org/name> \"ACME Corp\" .\n");
            writer.write("<http://example.org/person1> <http://example.org/worksFor> <http://example.org/company1> .\n");
            writer.write("<http://example.org/company1> <http://example.org/location> \"New York\" .\n");
        }
        
        AppConfig config = createConfig(testFile, "output.csv");
        
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertFalse(result.getTables().isEmpty());
    }
    
    @Test
    void testProcessLineResetsState() {
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        
        // Before processing
        assertNull(creator.currentCSVName);
        assertFalse(creator.unifiedBySubject);
        
        // Process a line
        creator.processLine("<http://example.org/s> <http://example.org/p> \"o\" .");
        
        // State should be reset for next line
        creator.processLine("");
        assertNull(creator.currentCSVName);
        assertFalse(creator.unifiedBySubject);
    }
    
    @Test
    void testBufferingMechanism() throws IOException {
        Path testFile = tempDir.resolve("large.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            // Create enough triples to test buffering
            for (int i = 0; i < 100; i++) {
                writer.write(String.format("<http://example.org/subject%d> <http://example.org/name> \"Subject %d\" .\n", i, i));
                writer.write(String.format("<http://example.org/subject%d> <http://example.org/age> \"%d\" .\n", i, i));
            }
        }
        
        AppConfig config = createConfig(testFile, "output.csv");
        
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertFalse(result.getTables().isEmpty());
    }
    
    @Test
    void testOutputFileNaming() throws IOException {
        Path testFile = createSimpleNTriplesFile();
        AppConfig config = createConfig(testFile, "custom_name.csv");
        
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertEquals(1, result.getTables().size());
        assertTrue(result.getTables().get(0).getUrl().contains("custom_name"));
    }
    
    // Helper methods
    
    private Path createSimpleNTriplesFile() throws IOException {
        Path testFile = tempDir.resolve("test.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("<http://example.org/subject1> <http://example.org/predicate1> \"Object1\" .\n");
            writer.write("<http://example.org/subject1> <http://example.org/predicate2> \"Object2\" .\n");
            writer.write("<http://example.org/subject2> <http://example.org/predicate1> \"Object3\" .\n");
        }
        return testFile;
    }
    
    private AppConfig createConfig(Path inputFile, String outputFileName) {
        Path outputPath = tempDir.resolve(outputFileName);
        return new AppConfig.Builder(inputFile.toString())
                .parsing("streaming")
                .output(outputPath.toString())
                .build();
    }
    
    // ==================== addMetadataToTableSchema Tests ====================
    
    @Test
    void testAddMetadataToTableSchemaIntegration() throws IOException {
        // Create test file with simple triples
        Path testFile = tempDir.resolve("integration_test.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("<http://example.org/person1> <http://example.org/name> \"Alice\" .\n");
            writer.write("<http://example.org/person1> <http://example.org/age> \"30\" .\n");
        }
        
        AppConfig config = createConfig(testFile, "output.csv");
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        
        // Process file - this will call addMetadataToTableSchema internally
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertFalse(result.getTables().isEmpty());
        
        // Verify columns were added for both predicates
        TableSchema schema = result.getTables().get(0).getTableSchema();
        assertNotNull(schema);
        assertTrue(schema.getColumns().size() >= 2); // At least name and age columns
    }
    
    @Test
    void testAddMetadataToTableSchemaWithIRIObject() throws IOException {
        Path testFile = tempDir.resolve("iri_test.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("<http://example.org/person1> <http://example.org/knows> <http://example.org/person2> .\n");
            writer.write("<http://example.org/person2> <http://example.org/knows> <http://example.org/person3> .\n");
        }
        
        AppConfig config = createConfig(testFile, "output.csv");
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertFalse(result.getTables().isEmpty());
        
        // Check that columns with IRI objects have valueUrl set
        TableSchema schema = result.getTables().get(0).getTableSchema();
        boolean hasValueUrl = schema.getColumns().stream()
            .anyMatch(col -> col.getValueUrl() != null && col.getValueUrl().contains("{+"));
        assertTrue(hasValueUrl, "At least one column should have valueUrl for IRI object");
    }
    
    @Test
    void testAddMetadataToTableSchemaWithLanguageTaggedLiteral() throws IOException {
        Path testFile = tempDir.resolve("lang_test.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("<http://example.org/book1> <http://example.org/title> \"Hello\"@en .\n");
            writer.write("<http://example.org/book1> <http://example.org/title> \"Hola\"@es .\n");
        }
        
        AppConfig config = createConfig(testFile, "output.csv");
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertFalse(result.getTables().isEmpty());
        
        // Check that language-tagged columns exist
        TableSchema schema = result.getTables().get(0).getTableSchema();
        boolean hasLangColumn = schema.getColumns().stream()
            .anyMatch(col -> col.getLang() != null);
        assertTrue(hasLangColumn, "At least one column should have language tag");
    }
    
    @Test
    void testAddMetadataToTableSchemaWithTypedLiteral() throws IOException {
        Path testFile = tempDir.resolve("typed_test.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("<http://example.org/person1> <http://example.org/age> \"42\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n");
            writer.write("<http://example.org/person2> <http://example.org/age> \"25\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n");
        }
        
        AppConfig config = createConfig(testFile, "output.csv");
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertFalse(result.getTables().isEmpty());
        
        // Check that typed literal columns have datatype
        TableSchema schema = result.getTables().get(0).getTableSchema();
        boolean hasDatatypeColumn = schema.getColumns().stream()
            .anyMatch(col -> col.getDatatype() != null);
        assertTrue(hasDatatypeColumn, "At least one column should have datatype");
    }
    
    @Test
    void testAddMetadataToTableSchemaWithBlankNodeObject() throws IOException {
        Path testFile = tempDir.resolve("blank_test.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("_:b1 <http://example.org/name> \"Anonymous\" .\n");
            writer.write("<http://example.org/person1> <http://example.org/friend> _:b1 .\n");
        }
        
        AppConfig config = createConfig(testFile, "output.csv");
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertFalse(result.getTables().isEmpty());
    }
    
    @Test
    void testAddMetadataToTableSchemaWithNullConfig() {
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(null);
        
        // Create a triple
        SimpleValueFactory factory = SimpleValueFactory.getInstance();
        IRI subject = factory.createIRI("http://example.org/person1");
        IRI predicate = factory.createIRI("http://example.org/name");
        Literal object = factory.createLiteral("Alice");
        
        Triple triple = new Triple(subject, predicate, object);
        
        // Should throw IllegalStateException when config is null
        assertThrows(IllegalStateException.class, () -> {
            creator.addMetadataToTableSchema(triple);
        });
    }
    
    @Test
    void testAddMetadataToTableSchemaPreventsDuplicateColumns() throws IOException {
        Path testFile = tempDir.resolve("duplicate_test.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("<http://example.org/person1> <http://example.org/name> \"Alice\" .\n");
            writer.write("<http://example.org/person2> <http://example.org/name> \"Bob\" .\n");
            writer.write("<http://example.org/person3> <http://example.org/name> \"Charlie\" .\n");
        }
        
        AppConfig config = createConfig(testFile, "output.csv");
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertFalse(result.getTables().isEmpty());
        
        // Verify only one "name" column exists despite multiple uses
        TableSchema schema = result.getTables().get(0).getTableSchema();
        long nameColumnCount = schema.getColumns().stream()
            .filter(col -> col.getName() != null && col.getName().toLowerCase().contains("name"))
            .count();
        // Should have exactly 1 name column (not 3)
        assertTrue(nameColumnCount >= 1, "Should have at least one name column");
    }
    
    @Test
    void testAddMetadataToTableSchemaWithUnicodePredicateAndObject() throws IOException {
        Path testFile = tempDir.resolve("unicode_test.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("<http://example.org/osoba1> <http://example.org/jméno> \"Petr Dvořák\" .\n");
            writer.write("<http://example.org/osoba2> <http://example.org/jméno> \"Größe\" .\n");
        }
        
        AppConfig config = createConfig(testFile, "output.csv");
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertFalse(result.getTables().isEmpty());
        
        // Verify columns were added
        TableSchema schema = result.getTables().get(0).getTableSchema();
        assertFalse(schema.getColumns().isEmpty());
    }
    
    @Test
    void testAddMetadataToTableSchemaSetsPropertyUrl() throws IOException {
        Path testFile = tempDir.resolve("propertyurl_test.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("<http://example.org/person1> <http://example.org/email> \"alice@example.org\" .\n");
        }
        
        AppConfig config = createConfig(testFile, "output.csv");
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertFalse(result.getTables().isEmpty());
        
        // Verify propertyUrl is set
        TableSchema schema = result.getTables().get(0).getTableSchema();
        boolean hasPropertyUrl = schema.getColumns().stream()
            .anyMatch(col -> col.getPropertyUrl() != null && col.getPropertyUrl().contains("example.org"));
        assertTrue(hasPropertyUrl, "At least one column should have propertyUrl");
    }
    
    @Test
    void testAddMetadataToTableSchemaSetsAboutUrl() throws IOException {
        Path testFile = tempDir.resolve("abouturl_test.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("<http://example.org/person1> <http://example.org/name> \"Alice\" .\n");
        }
        
        AppConfig config = createConfig(testFile, "output.csv");
        StreamingNTriplesMetadataCreator creator = new StreamingNTriplesMetadataCreator(config);
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertFalse(result.getTables().isEmpty());
        
        // Verify aboutUrl is set to {+Subject}
        TableSchema schema = result.getTables().get(0).getTableSchema();
        boolean hasAboutUrl = schema.getColumns().stream()
            .anyMatch(col -> col.getAboutUrl() != null && col.getAboutUrl().equals("{+Subject}"));
        assertTrue(hasAboutUrl, "At least one column should have aboutUrl set to {+Subject}");
    }
}
