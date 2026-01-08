package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.support.AppConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class LabelFormatterTest {

    @ParameterizedTest
    @MethodSource("labelFormatterTestCases")
    void testLabelFormatter(String input, String format, String expected) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, IOException, InterruptedException {
        // Create AppConfig with the specified column naming convention
        AppConfig config = new AppConfig.Builder("test.ttl")
                .columnNamingConvention(format)
                .build();
        
        // Invoke the method with AppConfig
        Object result = LabelFormatter.changeLabelToTheConfiguredFormat(input, config);
        assertEquals(expected, result);
    }
    @ParameterizedTest
    @NullAndEmptySource
    void testNullAndEmpty(String input) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Get the private method
        Method method = LabelFormatter.class.getDeclaredMethod("toCamelCase", String.class);

        // Make it accessible
        method.setAccessible(true);

        // Invoke the static method (null for static methods)
        Object result = method.invoke(null, input);
        assertEquals(input, result);
    }

    @ParameterizedTest
    @MethodSource("camelCaseTestCases")
    void testToCamelCase(String input, String expected) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // Get the private method
        Method method = LabelFormatter.class.getDeclaredMethod("toCamelCase", String.class);

        // Make it accessible
        method.setAccessible(true);

        // Invoke the static method (null for static methods)
        Object result = method.invoke(null, input);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("outOfScopeCamelCaseTestCases")
    void testToCamelCaseEdgeCases(String input, String expected) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // Get the private method
        Method method = LabelFormatter.class.getDeclaredMethod("toCamelCase", String.class);

        // Make it accessible
        method.setAccessible(true);

        // Invoke the static method (null for static methods)
        Object result = method.invoke(null, input);
        assertNotEquals(expected, result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testNullAndEmptyPascalCase(String input) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Get the private method
        Method method = LabelFormatter.class.getDeclaredMethod("toPascalCase", String.class);

        // Make it accessible
        method.setAccessible(true);

        // Invoke the static method (null for static methods)
        Object result = method.invoke(null, input);
        assertEquals(input, result);
    }

    @ParameterizedTest
    @MethodSource("pascalCaseTestCases")
    void testToPascalCase(String input, String expected) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // Get the private method
        Method method = LabelFormatter.class.getDeclaredMethod("toPascalCase", String.class);

        // Make it accessible
        method.setAccessible(true);

        // Invoke the static method (null for static methods)
        Object result = method.invoke(null, input);
        assertEquals(expected, result);
    }


    @ParameterizedTest
    @MethodSource("screamingSnakeCaseTestCases")
    void testToScreamingSnakeCase(String input, String expected) {
        AppConfig config = new AppConfig.Builder("test.ttl")
                .columnNamingConvention(AppConfig.COLUMN_NAMING_SCREAMING_SNAKE_CASE)
                .build();

        assertEquals(expected, LabelFormatter.changeLabelToTheConfiguredFormat(input, config));
    }

    @ParameterizedTest
    @MethodSource("dotNotationTestCases")
    void testToDotNotation(String input, String expected) {
        AppConfig config = new AppConfig.Builder("test.ttl")
                .columnNamingConvention(AppConfig.COLUMN_NAMING_DOT_NOTATION)
                .build();

        assertEquals(expected, LabelFormatter.changeLabelToTheConfiguredFormat(input, config));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testNullAndEmptyAllCases(String input) {
        // Test all naming conventions with null/empty input
        AppConfig snakeConfig = new AppConfig.Builder("test.ttl")
                .columnNamingConvention(AppConfig.COLUMN_NAMING_SNAKE_CASE)
                .build();
        AppConfig camelConfig = new AppConfig.Builder("test.ttl")
                .columnNamingConvention(AppConfig.COLUMN_NAMING_CAMEL_CASE)
                .build();
        AppConfig pascalConfig = new AppConfig.Builder("test.ttl")
                .columnNamingConvention(AppConfig.COLUMN_NAMING_PASCAL_CASE)
                .build();
        AppConfig kebabConfig = new AppConfig.Builder("test.ttl")
                .columnNamingConvention(AppConfig.COLUMN_NAMING_KEBAB_CASE)
                .build();
        AppConfig titleConfig = new AppConfig.Builder("test.ttl")
                .columnNamingConvention(AppConfig.COLUMN_NAMING_TITLE_CASE)
                .build();
        AppConfig screamingSnakeConfig = new AppConfig.Builder("test.ttl")
                .columnNamingConvention(AppConfig.COLUMN_NAMING_SCREAMING_SNAKE_CASE)
                .build();
        AppConfig dotNotationConfig = new AppConfig.Builder("test.ttl")
                .columnNamingConvention(AppConfig.COLUMN_NAMING_DOT_NOTATION)
                .build();

        // All naming conventions should return input unchanged for null/empty
        assertEquals(input, LabelFormatter.changeLabelToTheConfiguredFormat(input, snakeConfig));
        assertEquals(input, LabelFormatter.changeLabelToTheConfiguredFormat(input, camelConfig));
        assertEquals(input, LabelFormatter.changeLabelToTheConfiguredFormat(input, pascalConfig));
        assertEquals(input, LabelFormatter.changeLabelToTheConfiguredFormat(input, kebabConfig));
        assertEquals(input, LabelFormatter.changeLabelToTheConfiguredFormat(input, titleConfig));
        assertEquals(input, LabelFormatter.changeLabelToTheConfiguredFormat(input, screamingSnakeConfig));
        assertEquals(input, LabelFormatter.changeLabelToTheConfiguredFormat(input, dotNotationConfig));
    }

    // Snake Case Tests
    @ParameterizedTest
    @MethodSource("snakeCaseTestCases")
    void testToSnakeCase(String input, String expected) {

        AppConfig config = new AppConfig.Builder("test.ttl")
                .columnNamingConvention(AppConfig.COLUMN_NAMING_SNAKE_CASE)
                .build();

        assertEquals(expected, LabelFormatter.changeLabelToTheConfiguredFormat(input, config));
    }

    // Kebab Case Tests
    @ParameterizedTest
    @MethodSource("kebabCaseTestCases")
    void testToKebabCase(String input, String expected) {
        AppConfig config = new AppConfig.Builder("test.ttl")
                .columnNamingConvention(AppConfig.COLUMN_NAMING_KEBAB_CASE)
                .build();

        assertEquals(expected, LabelFormatter.changeLabelToTheConfiguredFormat(input, config));
    }

    // Title Case Tests
    @ParameterizedTest
    @MethodSource("titleCaseTestCases")
    void testToTitleCase(String input, String expected) {
        AppConfig config = new AppConfig.Builder("test.ttl")
                .columnNamingConvention(AppConfig.COLUMN_NAMING_TITLE_CASE)
                .build();

        assertEquals(expected, LabelFormatter.changeLabelToTheConfiguredFormat(input, config));
    }

    private static Stream<Arguments> labelFormatterTestCases() {
        return Stream.of(
                Arguments.of("userAccountId", LabelFormatter.CAMEL_CASE_CONFIG_STRING, "userAccountId"),
                Arguments.of("UserAccountID", LabelFormatter.PASCAL_CASE_CONFIG_STRING, "UserAccountID" ),
                Arguments.of("user-account-id", LabelFormatter.KEBAB_CASE_CONFIG_STRING, "user-account-id"),
                Arguments.of("user account id", LabelFormatter.TITLE_CASE_CONFIG_STRING, "User Account Id")
        );
    }
    private static Stream<Arguments> snakeCaseTestCases() {
        return Stream.of(
                // Basic conversions
                Arguments.of("userAccountId", "user_account_id"),
                Arguments.of("UserAccountID", "user_account_id"),
                Arguments.of("user-account-id", "user_account_id"),
                Arguments.of("user account id", "user_account_id"),

                // Edge cases
                Arguments.of("already_snake_case", "already_snake_case"),
                Arguments.of("ALREADY_SNAKE_CASE", "already_snake_case"),
                Arguments.of("mixedCase123", "mixed_case123"),
                Arguments.of("single", "single"),
                Arguments.of("a", "a"),

                // Special cases
                Arguments.of("JSONData", "json_data"),
                Arguments.of("XMLHttpRequest", "xml_http_request"),
                Arguments.of("userID", "user_id"),
                Arguments.of("DBConnection", "db_connection"),

                // Multiple separators
                Arguments.of("user  account__id", "user_account_id"),
                Arguments.of("user--account-id", "user_account_id")
        );
    }

    private static Stream<Arguments> kebabCaseTestCases() {
        return Stream.of(
                // Basic conversions
                Arguments.of("userAccountId", "user-account-id"),
                Arguments.of("UserAccountID", "user-account-id"),
                Arguments.of("user_account_id", "user-account-id"),
                Arguments.of("user account id", "user-account-id"),

                // Existing kebab-case preservation
                Arguments.of("already-kebab-case", "already-kebab-case"),
                Arguments.of("existing-constant", "existing-constant"),

                // Acronym handling
                Arguments.of("JSONData", "json-data"),
                Arguments.of("XMLHttpRequest", "xml-http-request"),
                Arguments.of("DBConnection", "db-connection"),
                Arguments.of("userID", "user-id"),
                Arguments.of("HTTPServer", "http-server"),

                // Mixed cases with numbers
                Arguments.of("mixedCase123", "mixed-case123"),
                Arguments.of("version2.0", "version2-0"),
                Arguments.of("item2Details", "item2-details"),

                // Edge cases
                Arguments.of("single", "single"),
                Arguments.of("a", "a"),
                Arguments.of("--test--", "test"),
                Arguments.of("  test  ", "test"),

                // Complex cases
                Arguments.of("makeHTTPRequest", "make-http-request"),
                Arguments.of("parseXMLDocument", "parse-xml-document"),
                Arguments.of("getURLFromString", "get-url-from-string"),
                Arguments.of("convertJSONToXML", "convert-json-to-xml"),

                // Multiple separator cases
                Arguments.of("user  account__id", "user-account-id"),
                Arguments.of("user--account-id", "user-account-id"),
                Arguments.of("user__account--id", "user-account-id")
        );
    }

    private static Stream<Arguments> titleCaseTestCases() {
        return Stream.of(
                // Basic conversions
                Arguments.of("userAccountId", "User Account Id"),
                Arguments.of("user_account_id", "User Account Id"),
                Arguments.of("user-account-id", "User Account Id"),
                Arguments.of("user account id", "User Account Id"),

                // Edge cases
                Arguments.of("Already Title Case", "Already Title Case"),
                Arguments.of("ALREADY TITLE CASE", "Already Title Case"),
                Arguments.of("mixedCase123", "Mixed Case123"),
                Arguments.of("single", "Single"),
                Arguments.of("a", "A"),

                // Special cases
                Arguments.of("jsonData", "Json Data"),
                Arguments.of("xmlHttpRequest", "Xml Http Request"),
                Arguments.of("userID", "User Id"),
                Arguments.of("dbConnection", "Db Connection"),

                // Multiple separators
                Arguments.of("user  account__id", "User Account Id"),
                Arguments.of("user--account-id", "User Account Id"),

                // Small words (optional - depends on your title case rules)
                Arguments.of("the quick brown fox", "The Quick Brown Fox"),
                Arguments.of("a tale of two cities", "A Tale Of Two Cities")
        );
    }

    private static Stream<Arguments> screamingSnakeCaseTestCases() {
        return Stream.of(
                Arguments.of("userAccountId", "USER_ACCOUNT_ID"),
                Arguments.of("UserAccountID", "USER_ACCOUNT_ID"),
                Arguments.of("user-account-id", "USER_ACCOUNT_ID"),
                Arguments.of("user account id", "USER_ACCOUNT_ID"),
                Arguments.of("user_account_id", "USER_ACCOUNT_ID"),
                Arguments.of("JSONData", "JSON_DATA"),
                Arguments.of("XMLHttpRequest", "XML_HTTP_REQUEST"),
                Arguments.of("already_SCREAMING", "ALREADY_SCREAMING"),
                Arguments.of("mixedCase123", "MIXED_CASE123"),
                Arguments.of("single", "SINGLE")
        );
    }

    private static Stream<Arguments> dotNotationTestCases() {
        return Stream.of(
                Arguments.of("userAccountId", "user.account.id"),
                Arguments.of("UserAccountID", "user.account.id"),
                Arguments.of("user-account-id", "user.account.id"),
                Arguments.of("user account id", "user.account.id"),
                Arguments.of("user_account_id", "user.account.id"),
                Arguments.of("JSONData", "json.data"),
                Arguments.of("XMLHttpRequest", "xml.http.request"),
                Arguments.of("existing.dot.notation", "existing.dot.notation"),
                Arguments.of("mixedCase123", "mixed.case123"),
                Arguments.of("single", "single")
        );
    }

    private static Stream<Arguments> pascalCaseTestCases() {
        return Stream.of(
                // Basic cases
                Arguments.of("hello_world", "HelloWorld"),
                Arguments.of("hello-world", "HelloWorld"),
                Arguments.of("hello world", "HelloWorld"),
                Arguments.of("hello", "Hello"),
                Arguments.of("HELLO", "HELLO"),

                // Multiple separators
                Arguments.of("hello-java_world", "HelloJavaWorld"),
                Arguments.of("hello  java--world", "HelloJavaWorld"),

                // Mixed cases
                Arguments.of("helloWorld", "HelloWorld"),
                Arguments.of("AlreadyPascalCase", "AlreadyPascalCase"),
                Arguments.of("JSONData", "JSONData"),
                Arguments.of("XMLParser", "XMLParser"),

                // With numbers
                Arguments.of("user_id_123", "UserId123"),
                Arguments.of("item-2-details", "Item2Details"),

                // Edge cases
                Arguments.of("a", "A"),
                Arguments.of("_hello_", "Hello"),
                Arguments.of("  hello  ", "Hello"),

                // Complex cases (acronyms)
                Arguments.of("makeHTTPRequest", "MakeHTTPRequest"),
                Arguments.of("getURLFromString", "GetURLFromString"),
                Arguments.of("parseXMLDocument", "ParseXMLDocument"),
                Arguments.of("DBConnection", "DBConnection"),
                Arguments.of("userID", "UserID")
        );
    }

    private static Stream<Arguments> outOfScopeCamelCaseTestCases() {
        return Stream.of(

                Arguments.of("HELLO", "hello"),
                Arguments.of("JSONData", "jsonData"),
                Arguments.of("XMLParser", "xmlParser"),
                // Complex cases (acronyms)
                Arguments.of("makeHTTPRequest", "makeHttpRequest"),
                Arguments.of("getURLFromString", "getUrlFromString"),
                Arguments.of("parseXMLDocument", "parseXmlDocument"),
                Arguments.of("convertToJSON", "convertToJson"),
                // Mixed separators with acronyms
                Arguments.of("parse_XML_document", "parseXmlDocument"),
                Arguments.of("get-URL-from-string", "getUrlFromString"),
                // Special cases
                Arguments.of("DBConnection", "dbConnection"),
                Arguments.of("userID", "userId"),
                Arguments.of("XMLHTTPRequest", "xmlHttpRequest"),
                Arguments.of("UPPERCASE", "uppercase")
        );
    }

        private static Stream<Arguments> camelCaseTestCases() {
        return Stream.of(
                // Basic cases
                Arguments.of("hello_world", "helloWorld"),
                Arguments.of("Hello-World", "helloWorld"),
                Arguments.of("hello world", "helloWorld"),
                Arguments.of("hello", "hello"),

                // Multiple separators
                Arguments.of("hello-java_world", "helloJavaWorld"),
                Arguments.of("hello  java--world", "helloJavaWorld"),
                Arguments.of("hello___world", "helloWorld"),
                Arguments.of("hello---world", "helloWorld"),

                // Mixed cases
                Arguments.of("HelloWorld", "helloWorld"),
                Arguments.of("helloWorld", "helloWorld"),
                Arguments.of("AlreadyCamelCase", "alreadyCamelCase"),

                // With numbers
                Arguments.of("user_id_123", "userId123"),
                Arguments.of("item-2-details", "item2Details"),
                Arguments.of("version2.0", "version20"),

                // Edge cases
                Arguments.of("A", "a"),
                Arguments.of("a", "a"),
                Arguments.of("_hello_", "hello"),
                Arguments.of("-hello-", "hello"),
                Arguments.of("  hello  ", "hello"),
                Arguments.of("__", ""),
                Arguments.of("--", ""),
                Arguments.of("  ", ""),

                Arguments.of("lowercase", "lowercase")
        );
    }

    public static void updateColumnNamingConvention(String newValue) throws IOException {
        String filepath = "config/application.conf";
        // Read all lines from the file
        List<String> lines = Files.readAllLines(Paths.get(filepath));

        // Find and replace the target line
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("columnNamingConvention")) {
                // Replace the entire line or just the value
                lines.set(i, "  columnNamingConvention = \"" + newValue + "\"");
                break; // Remove if there might be multiple matches
            }
        }

        // Write the modified content back to the file
        Files.write(Paths.get(filepath), lines, StandardOpenOption.TRUNCATE_EXISTING);
        try (FileChannel channel = FileChannel.open(Paths.get(filepath), StandardOpenOption.WRITE)) {
            channel.force(true);  // Force all updates to be written to storage
        }
        String filepathDefault = "src/main/resources/reference.conf";
        // Read all lines from the file
        List<String> linesDefault = Files.readAllLines(Paths.get(filepathDefault));

        // Find and replace the target line
        for (int i = 0; i < linesDefault.size(); i++) {
            if (linesDefault.get(i).contains("columnNamingConvention")) {
                // Replace the entire line or just the value
                linesDefault.set(i, "  columnNamingConvention = \"" + newValue + "\"");
                break; // Remove if there might be multiple matches
            }
        }

        // Write the modified content back to the file
        Files.write(Paths.get(filepathDefault), linesDefault, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @ParameterizedTest
    @MethodSource("unicodePreservationTestCases")
    void testUnicodeCharacterPreservation(String input, String format, String expected) {
        AppConfig config = new AppConfig.Builder("test.ttl")
                .columnNamingConvention(format)
                .build();

        String result = LabelFormatter.changeLabelToTheConfiguredFormat(input, config);
        assertEquals(expected, result, "Unicode characters should be preserved in " + format);
    }

    private static Stream<Arguments> unicodePreservationTestCases() {
        return Stream.of(
                // Czech characters
                Arguments.of("má-přílohu", AppConfig.COLUMN_NAMING_TITLE_CASE, "Má Přílohu"),
                Arguments.of("má-přílohu", AppConfig.COLUMN_NAMING_CAMEL_CASE, "máPřílohu"),
                Arguments.of("má-přílohu", AppConfig.COLUMN_NAMING_PASCAL_CASE, "MáPřílohu"),
                Arguments.of("má-přílohu", AppConfig.COLUMN_NAMING_SNAKE_CASE, "má_přílohu"),
                Arguments.of("má-přílohu", AppConfig.COLUMN_NAMING_KEBAB_CASE, "má-přílohu"),
                Arguments.of("má-přílohu", AppConfig.COLUMN_NAMING_SCREAMING_SNAKE_CASE, "MÁ_PŘÍLOHU"),
                Arguments.of("má-přílohu", AppConfig.COLUMN_NAMING_DOT_NOTATION, "má.přílohu"),
                
                // More Czech words - using lowercase input since camelCase detection only works with ASCII capitals
                Arguments.of("obsahuje-autorské-dílo", AppConfig.COLUMN_NAMING_TITLE_CASE, "Obsahuje Autorské Dílo"),
                Arguments.of("podmínky-užití", AppConfig.COLUMN_NAMING_SNAKE_CASE, "podmínky_užití"),
                Arguments.of("databáze-chráněná", AppConfig.COLUMN_NAMING_KEBAB_CASE, "databáze-chráněná"),
                
                // German characters
                Arguments.of("größe-ändern", AppConfig.COLUMN_NAMING_TITLE_CASE, "Größe Ändern"),
                Arguments.of("überschrift", AppConfig.COLUMN_NAMING_PASCAL_CASE, "Überschrift"),
                
                // French characters
                Arguments.of("référence-créée", AppConfig.COLUMN_NAMING_CAMEL_CASE, "référenceCréée"),
                Arguments.of("événement", AppConfig.COLUMN_NAMING_TITLE_CASE, "Événement"),
                
                // Spanish characters
                Arguments.of("año-creación", AppConfig.COLUMN_NAMING_SNAKE_CASE, "año_creación"),
                Arguments.of("niño", AppConfig.COLUMN_NAMING_PASCAL_CASE, "Niño"),
                
                // Greek characters
                Arguments.of("αλφάβητο", AppConfig.COLUMN_NAMING_TITLE_CASE, "Αλφάβητο"),
                
                // Cyrillic characters
                Arguments.of("привет-мир", AppConfig.COLUMN_NAMING_CAMEL_CASE, "приветМир"),
                
                // Mixed Unicode and ASCII
                Arguments.of("hasNázev", AppConfig.COLUMN_NAMING_TITLE_CASE, "Has Název"),
                Arguments.of("dateVýročí", AppConfig.COLUMN_NAMING_SNAKE_CASE, "date_výročí")
        );
    }
}
