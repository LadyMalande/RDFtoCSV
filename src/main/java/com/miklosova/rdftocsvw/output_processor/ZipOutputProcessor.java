package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.support.ConfigurationManager;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipOutputProcessor implements IOutputProcessor{
    @Override
    public FinalizedOutput processCSVToOutput(PrefinishedOutput prefinishedOutput) {
        ZipOutputStream zippedOutput = zipPrefinishedOutput(prefinishedOutput);

        return new FinalizedOutput(zippedOutput);
    }

    private ZipOutputStream zipPrefinishedOutput(PrefinishedOutput prefinishedOutput){
        String sourceFile = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAME);


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
}
