package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.ConfigurationManager;

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

        zipMultipleFiles();
        byte[] baos = createBAOSWithZips();
        return new FinalizedOutput<>(baos);
    }

    /**
     * Create the zip file which will contain the CSV and metadata
     *
     * @return the zipped file in bytes
     */
    private byte[] createBAOSWithZips() {
        String inputFilesInString;
        String metadataFileName;
        
        if (config != null) {
            inputFilesInString = config.getIntermediateFileNames();
            metadataFileName = config.getOutputFilePath() + ".csv-metadata.json";
        } else {
            // Backward compatibility
            inputFilesInString = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES);
            metadataFileName = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME);
        }
        
        //logger.log(Level.INFO, "inputFilesInString=" + inputFilesInString);
        String[] listOfFiles = inputFilesInString.split(",");

        List<String> srcFiles = new ArrayList<>(Arrays.asList(listOfFiles));
        srcFiles.add(metadataFileName);
        //logger.log(Level.INFO, "OUTPUT_METADATA_FILE_NAME=" + metadataFileName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ZipOutputStream zipOut = new ZipOutputStream(baos);

            for (String srcFile : srcFiles) {
                File fileToZip = new File(srcFile);
                FileInputStream fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }

            zipOut.close();

            // Now, you can retrieve the byte array containing the zip data
            byte[] zipBytes = baos.toByteArray();
            baos.close();
            return zipBytes;
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
        String inputFilesInString;
        String filenameForZip;
        String metadataFileName;
        
        if (config != null) {
            inputFilesInString = config.getIntermediateFileNames();
            filenameForZip = config.getOutputZipFileName();
            metadataFileName = config.getOutputFilePath() + ".csv-metadata.json";
        } else {
            // Backward compatibility
            inputFilesInString = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES);
            filenameForZip = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_ZIPFILE_NAME);
            metadataFileName = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME);
        }
        
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
        try {
            final FileOutputStream fos = new FileOutputStream(filenameForZip);
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            for (String srcFile : srcFiles) {
                File fileToZip = new File(srcFile);
                FileInputStream fis = new FileInputStream(fileToZip);

                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }

            zipOut.close();
            fos.close();
            return null;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "The file that was supposed to be zipped was not found. ");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Other error occurred other than that the file to be zipped was not found. ");
        }
        return null;
    }
}
