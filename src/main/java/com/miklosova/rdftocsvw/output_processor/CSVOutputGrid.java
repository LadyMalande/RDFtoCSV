package com.miklosova.rdftocsvw.output_processor;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVOutputGrid {
    Map<IRI, Map<String, List<Value>>> csvOutputBuffer;

    public Map<IRI, Map<String, List<Value>>> getCsvOutputBuffer() {
        return csvOutputBuffer;
    }

    public CSVOutputGrid() {
        this.csvOutputBuffer = new HashMap<>();

    }
}
