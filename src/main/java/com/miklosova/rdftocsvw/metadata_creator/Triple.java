package com.miklosova.rdftocsvw.metadata_creator;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

/**
 * The RDF Triple object.
 */
public class Triple {
    /**
     * The Subject.
     */
    IRI subject;
    /**
     * The Predicate.
     */
    IRI predicate;

    /**
     * The Object.
     */
    Value object;

    /**
     * Instantiates a new Triple.
     *
     * @param subject   the subject
     * @param predicate the predicate
     * @param object    the object
     */
    public Triple(IRI subject, IRI predicate, Value object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    /**
     * Gets subject.
     *
     * @return the subject
     */
    public IRI getSubject() {
        return subject;
    }

    /**
     * Gets predicate.
     *
     * @return the predicate
     */
    public IRI getPredicate() {
        return predicate;
    }

    /**
     * Gets object.
     *
     * @return the object
     */
    public Value getObject() {
        return object;
    }
}
