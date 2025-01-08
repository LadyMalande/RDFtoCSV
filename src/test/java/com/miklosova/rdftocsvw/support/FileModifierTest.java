package com.miklosova.rdftocsvw.support;

import java.nio.file.Files;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.BeforeEach;
import java.io.*;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class FileModifierTest {

    @TempDir
    Path tempDir;

    private File testFile;

    @BeforeEach
    void setUp() throws IOException {
        testFile = tempDir.resolve("test.txt").toFile();
    }

    //BaseRock generated method id: ${addColonsToIRIsInFile_withValidInput_shouldModifyContent}, hash: 573626530C07EC7EFE905C9DB1C45522
    @Test
@Disabled
    void addColonsToIRIsInFile_withValidInput_shouldModifyContent() throws IOException {
        String initialContent = "<example> <test> <data>.\n<more> <sample> <content>.";
        Files.write(testFile.toPath(), initialContent.getBytes());
        FileModifier.addColonsToIRIsInFile(testFile);
        String modifiedContent = Files.readString(testFile.toPath());
        String expectedContent = "<file://example> <file://test> <file://data>.\n" +
                "<file://more> <file://sample> <file://content>.";
        assertEquals(expectedContent, modifiedContent.trim());
    }

    //BaseRock generated method id: ${addColonsToIRIsInFile_withEmptyFile_shouldNotModifyContent}, hash: EDC907B778700FA09B1942D80EF43AC2
    @Test
    void addColonsToIRIsInFile_withEmptyFile_shouldNotModifyContent() throws IOException {
        Files.write(testFile.toPath(), "".getBytes());
        FileModifier.addColonsToIRIsInFile(testFile);
        String modifiedContent = Files.readString(testFile.toPath());
        assertEquals("", modifiedContent);
    }

    //BaseRock generated method id: ${addColonsToIRIsInFile_withNoIRIs_shouldNotModifyContent}, hash: 51CD1B9D481B8BA407CD8AE3DB91E724
    @Test
    void addColonsToIRIsInFile_withNoIRIs_shouldNotModifyContent() throws IOException {
        String initialContent = "This is a test file without IRIs";
        Files.write(testFile.toPath(), initialContent.getBytes());
        FileModifier.addColonsToIRIsInFile(testFile);
        String modifiedContent = Files.readString(testFile.toPath());
        assertEquals(initialContent, modifiedContent.trim());
    }

    //BaseRock generated method id: ${addColonsToIRIsInFile_withMixedContent_shouldModifyOnlyIRIs}, hash: 6696245A5BC3E2578451FA2965ABD06A
    @Test
    void addColonsToIRIsInFile_withMixedContent_shouldModifyOnlyIRIs() throws IOException {
        String initialContent = "Text <iri1> more text <iri2> final text";
        Files.write(testFile.toPath(), initialContent.getBytes());
        FileModifier.addColonsToIRIsInFile(testFile);
        String modifiedContent = Files.readString(testFile.toPath());
        String expectedContent = "Text <file://iri1> more text <file://iri2> final text";
        assertEquals(expectedContent, modifiedContent.trim());
    }

    //BaseRock generated method id: ${addColonsToIRIsInFile_withNonExistentFile_shouldHandleIOException}, hash: 003F3A8F93D1B7E09240C17CB506DC0E
    @Test
    void addColonsToIRIsInFile_withNonExistentFile_shouldHandleIOException() throws IOException {
        File nonExistentFile = new File("non_existent_file.txt");
        assertDoesNotThrow(() -> FileModifier.addColonsToIRIsInFile(nonExistentFile));
    }

    //BaseRock generated method id: ${addColonsToIRIsInFile_withReadOnlyFile_shouldHandleIOException}, hash: 4F02F0A0B4FC75748E911F99A3F0B762
    @Test
    void addColonsToIRIsInFile_withReadOnlyFile_shouldHandleIOException() throws IOException {
        String initialContent = "<test>";
        Files.write(testFile.toPath(), initialContent.getBytes());
        assertTrue(testFile.setReadOnly());
        assertDoesNotThrow(() -> FileModifier.addColonsToIRIsInFile(testFile));
    }
}
