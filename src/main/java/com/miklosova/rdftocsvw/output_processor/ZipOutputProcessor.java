package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.support.ConfigurationManager;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipOutputProcessor implements IOutputProcessor<ZipOutputStream>{
    @Override
    public FinalizedOutput<byte[]> processCSVToOutput(PrefinishedOutput<?> prefinishedOutput) {

        ZipOutputStream zippedOutput = zipMultipleFiles(prefinishedOutput);
        byte[] baos = createBAOSWithZips(prefinishedOutput);
        return new FinalizedOutput<>(baos);
    }

    private byte[] createBAOSWithZips(PrefinishedOutput<?> prefinishedOutput) {
        String inputFilesInString = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES);
        String filenameForZip = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_ZIPFILE_NAME);
        String[] listOfFiles = inputFilesInString.split(",");

        List<String> srcFiles = new ArrayList<String>(Arrays.asList(listOfFiles));
        srcFiles.add(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME));
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
                while((length = fis.read(bytes)) >= 0) {
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    private ZipOutputStream zipPrefinishedOutput(PrefinishedOutput prefinishedOutput){
        String sourceFile = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES);


        try {
            System.out.println("sourceFile: " + sourceFile);
            String filenameForZip = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_ZIPFILE_NAME);
            System.out.println("fileOutputStream fileName for zip: " + sourceFile);
            FileOutputStream fos = new FileOutputStream(filenameForZip);

            ZipOutputStream zipOut = new ZipOutputStream(fos);

            File fileToZip = new File(sourceFile);
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }

            zipOut.close();
            fis.close();
            fos.close();
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ex){
            ex.printStackTrace();
        }
        return null;
    }

    public ZipOutputStream zipMultipleFiles(PrefinishedOutput prefinishedOutput){
        String inputFilesInString = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES);
        String filenameForZip = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_ZIPFILE_NAME);
        String[] listOfFiles = inputFilesInString.split(",");

        List<String> srcFiles = new ArrayList<String>(Arrays.asList(listOfFiles));
        srcFiles.add(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME));

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
                while((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }

            zipOut.close();
            fos.close();
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
