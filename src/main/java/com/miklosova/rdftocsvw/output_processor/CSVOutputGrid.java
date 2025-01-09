package com.miklosova.rdftocsvw.output_processor;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The inner representation of CSV that serves as CSV output buffer.
 */
public class CSVOutputGrid {
    /**
     * The Csv output buffer.
     */
    Map<IRI, Map<String, List<Value>>> csvOutputBuffer;

    /**
     * Instantiates a new Csv output grid.
     */
    public CSVOutputGrid() {
        this.csvOutputBuffer = new HashMap<>();

    }

    /**
     * Gets csv output buffer.
     *
     * @return the csv output buffer
     */
    public Map<IRI, Map<String, List<Value>>> getCsvOutputBuffer() {
        return csvOutputBuffer;
    }

    /**
     * Print the CSV Structure.
     */
    public void print() {
        for (Map.Entry<IRI, Map<String, List<Value>>> entry : csvOutputBuffer.entrySet()) {
            System.out.print(entry.getKey() + ": ");
            for (Map.Entry<String, List<Value>> entryInside : entry.getValue().entrySet()) {

                System.out.print(" " + entryInside.getKey());
                System.out.print(" " + entryInside.getValue().get(0));
            }
            System.out.print("\n");
        }
    }
}
