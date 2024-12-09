package com.miklosova.rdftocsvw.metadata_creator;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

public class Triple {
    IRI subject;
    IRI predicate;

    Value object;

    public Triple(IRI subject, IRI predicate, Value object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public IRI getSubject() {
        return subject;
    }

    public IRI getPredicate() {
        return predicate;
    }

    public Value getObject() {
        return object;
    }
}
