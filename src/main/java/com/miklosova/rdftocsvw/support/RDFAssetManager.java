package com.miklosova.rdftocsvw.support;

import org.eclipse.rdf4j.rio.RDFFormat;

import java.util.HashMap;

public class RDFAssetManager {
    private static HashMap<String, RDFFormat> loaders = new HashMap<>();

    public void addLoader(RDFFormat format, String extension)
    {
        loaders.put(extension, format);
    }

    public RDFAssetManager() {
        addLoader(RDFFormat.RDFXML,"rdf");
        addLoader(RDFFormat.RDFXML,"rdfs");
        addLoader(RDFFormat.RDFXML,"owl");
        addLoader(RDFFormat.RDFXML,"xml");
        addLoader(RDFFormat.NTRIPLES,"nt");
        addLoader(RDFFormat.TURTLE,"ttl");
        addLoader(RDFFormat.TURTLESTAR,"ttls");
        addLoader(RDFFormat.N3,"n3");
        addLoader(RDFFormat.TRIX,"xml");
        addLoader(RDFFormat.TRIX,"trix");
        addLoader(RDFFormat.TRIG,"trig");
        addLoader(RDFFormat.TRIGSTAR,"trigs");
        addLoader(RDFFormat.BINARY,"brf");
        addLoader(RDFFormat.NQUADS,"nq");
        addLoader(RDFFormat.JSONLD,"jsonld");
        addLoader(RDFFormat.NDJSONLD,"ndjsonld");
        addLoader(RDFFormat.NDJSONLD,"jsonl");
        addLoader(RDFFormat.NDJSONLD,"ndjson");
        addLoader(RDFFormat.RDFJSON,"rj");
        addLoader(RDFFormat.RDFA,"xhtml");
        addLoader(RDFFormat.RDFA,"html");
        addLoader(RDFFormat.HDT,"hdt");
    }

    @SuppressWarnings("unchecked")
    public <T> T load(String name)
    {
        int i = name.lastIndexOf('.');
        if (i == -1)
            throw new RuntimeException("\"" + name + "\" has no extension, and so has no associated asset loader");

        String extension = name.substring(i+1);
        RDFFormat format = loaders.get(extension);
        return (T) format;
        /*


        if (loader == null)
            throw new RuntimeException("No loader registered for \"." + extension + "\" files");
        try
        {
            return (T) loader.load(name);
        }
        catch(ClassCastException e)
        {
            throw new RuntimeException("\"" + name + "\" could not be loaded as the expected type");
        }
        catch(Exception e)
        {
            throw new RuntimeException("Failed to load " + name, e);
        }
         */

    }
}
