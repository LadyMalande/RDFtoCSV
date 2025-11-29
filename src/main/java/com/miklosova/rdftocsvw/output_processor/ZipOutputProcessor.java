package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.support.AppConfig;


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The class that zips the created CSV and CSVW metadata into one .ZIP file.
 */
public class ZipOutputProcessor implements IOutputProcessor {
    private static final Logger logger = Logger.getLogger(ZipOutputProcessor.class.getName());
    private AppConfig config;

    /**
     * Default constructor for backward compatibility.
     * @deprecated Use {@link #ZipOutputProcessor(AppConfig)} instead
     */
    @Deprecated
    public ZipOutputProcessor() {
        this.config = null;
    }

    /**
     * Constructor with AppConfig.
     * @param config The application configuration
     */
    public ZipOutputProcessor(AppConfig config) {
        this.config = config;
    }

    @Override
    public FinalizedOutput<byte[]> processCSVToOutput(PrefinishedOutput<?> prefinishedOutput) {
        // For web service: only create byte array (faster, no disk I/O)
        return processCSVToOutputBytes();
    }

    /**
     * Create ZIP as byte array only (for web service responses).
     * Does not create a file on disk - fastest option for API responses.
     * 
     * @return ZIP file contents as byte array
     */
    public FinalizedOutput<byte[]> processCSVToOutputBytes() {
        logger.log(Level.INFO, "Creating ZIP byte array for web service response...");
        byte[] baos = createBAOSWithZips();
        logger.log(Level.INFO, "ZIP byte array created successfully");
        return new FinalizedOutput<>(baos);
    }

    /**
     * Create ZIP file on disk (for command-line usage).
     * Creates physical file at location specified in config.getOutputZipFileName().
     * Does NOT create byte array - the file on disk is what CLI users need.
     * 
     * @return Empty FinalizedOutput (the real output is the file on disk)
     */
    public FinalizedOutput<byte[]> processCSVToOutputFile() {
        logger.log(Level.INFO, "Creating ZIP file on disk for command-line usage...");
        zipMultipleFiles();
        logger.log(Level.INFO, "ZIP file created on disk successfully");
        // Return empty byte array - CLI doesn't use the return value, only the disk file
        return new FinalizedOutput<>(new byte[0]);
    }

    /**
     * Create the zip file which will contain the CSV and metadata
     *
     * @return the zipped file in bytes
     */
    private byte[] createBAOSWithZips() {
        if (config == null) {
            throw new IllegalStateException("AppConfig is required");
        }
        String inputFilesInString = config.getIntermediateFileNames();
        String metadataFileName = config.getOutputMetadataFileName();
        
        //logger.log(Level.INFO, "inputFilesInString=" + inputFilesInString);
        String[] listOfFiles = inputFilesInString.split(",");

        List<String> srcFiles = new ArrayList<>(Arrays.asList(listOfFiles));
        srcFiles.add(metadataFileName);
        //logger.log(Level.INFO, "OUTPUT_METADATA_FILE_NAME=" + metadataFileName);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(baos)) {

            for (String srcFile : srcFiles) {
                File fileToZip = new File(srcFile);
                try (FileInputStream fis = new FileInputStream(fileToZip)) {
                    ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                    zipOut.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                }
            }

            zipOut.finish();
            // Now, you can retrieve the byte array containing the zip data
            return baos.toByteArray();
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "The file that was supposed to be zipped was not found. ");

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Other error occurred other than that the file to be zipped was not found. ");
        }
        return null;

    }

    /**
     * Zip multiple files into zip output stream.
     *
     * @return the zip output stream to be given to the final .ZIP file
     */
    public ZipOutputStream zipMultipleFiles() {
        if (config == null) {
            throw new IllegalStateException("AppConfig is required");
        }
        String inputFilesInString = config.getIntermediateFileNames();
        String filenameForZip = config.getOutputZipFileName();
        String metadataFileName = config.getOutputMetadataFileName();
        
        logger.log(Level.INFO, "zipFileName = " + filenameForZip);
        String[] listOfFiles = inputFilesInString.split(",");
        String[] newArray;
        if (listOfFiles[listOfFiles.length - 1].isEmpty()) {
            newArray = Arrays.copyOf(listOfFiles, listOfFiles.length - 1);
        } else {
            newArray = listOfFiles;
        }
        List<String> srcFiles = new ArrayList<>(Arrays.asList(newArray));
        srcFiles.add(metadataFileName);
        logger.log(Level.INFO, "Files to zip: " + srcFiles);
        try (FileOutputStream fos = new FileOutputStream(filenameForZip);
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {

            for (String srcFile : srcFiles) {
                logger.log(Level.INFO, "Zipping file: " + srcFile);
                File fileToZip = new File(srcFile);
                try (FileInputStream fis = new FileInputStream(fileToZip)) {
                    ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                    zipOut.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                }
            }

            return null;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "The file that was supposed to be zipped was not found: " + e.getMessage());
            logger.log(Level.SEVERE, "Attempted to create ZIP at: " + filenameForZip);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Other error occurred: " + e.getMessage());
        }
        return null;
    }
}
