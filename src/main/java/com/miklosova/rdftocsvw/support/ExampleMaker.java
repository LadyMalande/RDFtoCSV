package com.miklosova.rdftocsvw.support;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

public class ExampleMaker {

    public Model getModel() {
        return model;
    }

    Model model;

    public void makeExample(){
        ModelBuilder builder = new ModelBuilder();

        // set some com.miklosova.RDFtoCSV.namespaces
        builder.setNamespace("ex", "http://example.org/").setNamespace(DCTERMS.NS).setNamespace("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        String dcmitype = "http://purl.org/dc/dcmitype/";
        IRI picasso = Values.iri(dcmitype, "Text");
        // add a new named graph to the model
        builder.namedGraph("ex:myGraph")
                // add statements about resource ex:john
                .subject("ex:myThesis")
                .add(RDF.TYPE, picasso)
                .add(DCTERMS.CREATOR, "ex:TerezaMiklóšová")
                .add(DCTERMS.TITLE, Values.literal("Automatický převod RDF dat do CSV", "cs"))
                .add(DCTERMS.TITLE, Values.literal("Automated transformation of RDF data to CSV", "en"));



        // add a triple to the default graph
        builder.defaultGraph().subject("ex:myGraph").add(RDF.TYPE, "rdf:Graph");

        // return the Model object
        model = builder.build();
        Rio.write(model, System.out, RDFFormat.TURTLE);
        System.out.println();
        Rio.write(model, System.out, RDFFormat.TRIG);
        System.out.println();
        Rio.write(model, System.out, RDFFormat.NTRIPLES);
        System.out.println();
        Rio.write(model, System.out, RDFFormat.NQUADS);
        System.out.println();
        Rio.write(model, System.out, RDFFormat.JSONLD);
        //m.forEach(System.out::println);
    }

    public Model makeExampleModel(){
        ModelBuilder builder = new ModelBuilder();

        // set some com.miklosova.RDFtoCSV.namespaces
        builder.setNamespace("ex", "http://example.org/").setNamespace(DCTERMS.NS).setNamespace("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        String dcmitype = "http://purl.org/dc/dcmitype/";
        IRI picasso = Values.iri(dcmitype, "Text");
        // add a new named graph to the model
        builder.namedGraph("ex:myGraph")
                // add statements about resource ex:john
                .subject("ex:myThesis")
                .add(RDF.TYPE, picasso)
                .add(DCTERMS.CREATOR, "ex:TerezaMiklóšová")
                .add(DCTERMS.TITLE, Values.literal("Automatický převod RDF dat do CSV", "cs"))
                .add(DCTERMS.TITLE, Values.literal("Automated transformation of RDF data to CSV", "en"));



        // add a triple to the default graph
        builder.defaultGraph().subject("ex:myGraph").add(RDF.TYPE, "rdf:Graph");

        // return the Model object
        model = builder.build();
        Rio.write(model, System.out, RDFFormat.TURTLE);
        System.out.println();
        Rio.write(model, System.out, RDFFormat.TRIG);
        System.out.println();
        Rio.write(model, System.out, RDFFormat.NTRIPLES);
        System.out.println();
        Rio.write(model, System.out, RDFFormat.NQUADS);
        System.out.println();
        Rio.write(model, System.out, RDFFormat.JSONLD);
        //m.forEach(System.out::println);
        return model;
    }

    public void exampleFilesInAllExtensions(){
        // Creation of example RDFs in various formats
        Model exampleModel = this.makeExampleModel();
        CreateOtherRDFFormats cof = new CreateOtherRDFFormats(exampleModel);
        cof.writeModelToFile("exampleFile.brf", RDFFormat.BINARY);
        //cof.writeModelToFile("exampleFile.hdt", RDFFormat.HDT);
        //cof.writeModelToFile("exampleFile.html", RDFFormat.RDFA);
        cof.writeModelToFile("exampleFile.jsonl", RDFFormat.NDJSONLD);
        cof.writeModelToFile("exampleFile.jsonld", RDFFormat.JSONLD);
        cof.writeModelToFile("exampleFile.n3", RDFFormat.N3);
        cof.writeModelToFile("exampleFile.ndjson", RDFFormat.NDJSONLD);
        cof.writeModelToFile("exampleFile.ndjsonld", RDFFormat.NDJSONLD);
        cof.writeModelToFile("exampleFile.nq", RDFFormat.NQUADS);
        cof.writeModelToFile("exampleFile.nt", RDFFormat.NTRIPLES);
        cof.writeModelToFile("exampleFile.owl", RDFFormat.RDFXML);
        cof.writeModelToFile("exampleFile.rdf", RDFFormat.RDFXML);
        cof.writeModelToFile("exampleFile.rdfs", RDFFormat.RDFXML);
        cof.writeModelToFile("exampleFile.rj", RDFFormat.RDFJSON);
        cof.writeModelToFile("exampleFile.trig", RDFFormat.TRIG);
        cof.writeModelToFile("exampleFile.trigs", RDFFormat.TRIGSTAR);
        cof.writeModelToFile("exampleFile.trix", RDFFormat.TRIX);
        cof.writeModelToFile("exampleFile.ttl", RDFFormat.TURTLE);
        cof.writeModelToFile("exampleFile.ttls", RDFFormat.TURTLESTAR);
        //cof.writeModelToFile("exampleFile.xhtml", RDFFormat.RDFA);
        cof.writeModelToFile("exampleFile.xml", RDFFormat.RDFXML);
    }
}
