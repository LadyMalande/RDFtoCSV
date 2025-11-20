package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.converter.RDFtoCSV;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationManagerTest {
    @Test
    @Disabled
    @Deprecated
    public void processConfigMapTest(){
        String url = "https://raw.githubusercontent.com/LadyMalande/RDFtoCSV/refs/heads/main/src/test/resources/RDFCoreDatatypes/test002b.nt";
        Map<String, String> configMap = new HashMap<>();
        configMap.put("table", "one");
        configMap.put("readMethod", "rdf4j");
        configMap.put("firstNormalForm", "false");
        RDFtoCSV rdFtoCSV = new RDFtoCSV(url, configMap);

        Assertions.assertEquals(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.TABLES),"one");
        Assertions.assertEquals(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD),"basicQuery");
        Assertions.assertEquals(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.READ_METHOD),"rdf4j");
        Assertions.assertEquals(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.FIRST_NORMAL_FORM),"false");

        String result = null;
        try {
            result = rdFtoCSV.getMetadataAsString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(result);

    }

    @Test
    public void loadConfigTest(){
        ConfigurationManager.loadConfig();


    }
}
