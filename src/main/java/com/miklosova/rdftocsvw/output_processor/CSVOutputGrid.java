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

    public void print(){
        for(Map.Entry<IRI, Map<String, List<Value>>> entry : csvOutputBuffer.entrySet()){
            System.out.print(entry.getKey() + ": ");
            for(Map.Entry<String, List<Value>> entryInside: entry.getValue().entrySet()){

                System.out.print(" " + entryInside.getKey());
                System.out.print(" " + entryInside.getValue().get(0));
            }
            System.out.print("\n");
        }
    }
}
