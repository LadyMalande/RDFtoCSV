package com.miklosova.rdftocsvw.support;

import org.eclipse.rdf4j.model.IRI;

/**
 * The class contains a list of common XML datatypes.
 */
public class BuiltInDatatypes {

    /**
     * Is built in datatype boolean -> helps find the XML value for the given datatype that is supposed to be written in the datatype in column.
     *
     * @param datatype the datatype in IRI shape
     * @return the boolean - true if it is a XML datatype
     */
    public static boolean isBuiltInDatatype(IRI datatype) {
        // List of common built-in datatypes (e.g., from XML Schema)
        return datatype != null && (
                datatype.stringValue().equals("http://www.w3.org/2001/XMLSchema#string") ||
                        datatype.stringValue().equals("http://www.w3.org/2001/XMLSchema#decimal") ||
                        datatype.stringValue().equals("http://www.w3.org/2001/XMLSchema#integer") ||
                        datatype.stringValue().equals("http://www.w3.org/2001/XMLSchema#float") ||
                        datatype.stringValue().equals("http://www.w3.org/2001/XMLSchema#double") ||
                        datatype.stringValue().equals("http://www.w3.org/2001/XMLSchema#boolean") ||
                        datatype.stringValue().equals("http://www.w3.org/2001/XMLSchema#dateTime") ||
                        datatype.stringValue().equals("http://www.w3.org/2001/XMLSchema#date") ||
                        datatype.stringValue().equals("http://www.w3.org/2001/XMLSchema#time")
        );
    }
}
