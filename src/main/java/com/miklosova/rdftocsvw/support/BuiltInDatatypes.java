package com.miklosova.rdftocsvw.support;

import org.eclipse.rdf4j.model.IRI;

public class BuiltInDatatypes {

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
