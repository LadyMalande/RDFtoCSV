package com.miklosova.rdftocsvw.convertor;

import com.google.common.collect.Iterators;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import lombok.extern.java.Log;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.SailConnectionListener;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.jruby.RubyProcess;

import java.util.*;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

@Log
public class BasicQueryConverter implements IQueryParser{

    String resultCSV;
    ArrayList<Value> roots;
    ArrayList<Row> rows;
    ArrayList<Value> keys;
    ArrayList<ArrayList<Row>> allRows;
    ArrayList<ArrayList<Value>> allKeys;
    Metadata metadata;

    ArrayList<String> fileNamesCreated;
    public Map<String, Integer> mapOfPredicatesAndTheirNumbers;
    public Map<Value, Integer> mapOfTypesAndTheirNumbers;
    String delimiter;
    String CSVFileTOWriteTo;
    String allRowsOfOutput;
    Repository db;

    Integer fileNumberX;

    public BasicQueryConverter(Repository db) {
        this.keys = new ArrayList<>();
        this.db = db;
        this.fileNumberX = 0;
        this.fileNamesCreated = new ArrayList<>();
        this.metadata = new Metadata();
    }

    public void changeBNodesForIri(RepositoryConnection rc){
        Iterator statements = rc.getStatements(null, null, null, true).iterator();
        Map<Value, Value> mapOfBlanks = new HashMap<>();
        //System.out.println("Iterator size: " + Iterators.size(statements));
        int counter = 0;
        int i = 0;
        while(statements.hasNext()){
            Statement st = (Statement)statements.next();
            Statement statement = null;
            IRI subj = null;
            if(st.getSubject().isBNode()){
                if(mapOfBlanks.get(st.getSubject()) != null){
                    ValueFactory vf = SimpleValueFactory.getInstance();
                    subj = (IRI)mapOfBlanks.get(st.getSubject());
                    statement = vf.createStatement((IRI)mapOfBlanks.get(st.getSubject()), st.getPredicate(), st.getObject());

                } else {
                    ValueFactory vf = SimpleValueFactory.getInstance();
                    IRI v = vf.createIRI("https://blank_Nodes_IRI.org/" + i);
                    //IRI v = (IRI) iri("https://blank_Nodes_IRI.org/" + i);
                    i++;
                    mapOfBlanks.put(st.getSubject(), v);
                    subj = (IRI)mapOfBlanks.get(st.getSubject());
                    statement = vf.createStatement((IRI)mapOfBlanks.get(st.getSubject()), st.getPredicate(), st.getObject());
                }
            }
            if(st.getObject().isBNode()){
                if(mapOfBlanks.get(st.getObject()) != null){
                    ValueFactory vf = SimpleValueFactory.getInstance();
                    subj = (subj == null) ? (IRI)st.getSubject() : subj;
                    statement = vf.createStatement(subj, st.getPredicate(), (IRI)mapOfBlanks.get(st.getObject()));
                    rc.add(statement);
                } else{
                    ValueFactory vf = SimpleValueFactory.getInstance();
                    IRI v = vf.createIRI("https://blank_Nodes_IRI.org/" + i);

                    mapOfBlanks.put(st.getObject(),v);
                    subj = (subj == null) ? (IRI)st.getSubject() : subj;
                    statement = vf.createStatement(subj, st.getPredicate(), (IRI)mapOfBlanks.get(st.getObject()));
                    i++;
                }
            }
            if(statement != null){
                rc.add(statement);
            }
            System.out.println(st);
            counter = counter +1;
        }
        System.out.println("Count " + counter);
    }

    @Override
    public PrefinishedOutput convertWithQuery(RepositoryConnection rc) {
        loadConfiguration();
        changeBNodesForIri(rc);
        deleteBlankNodes(rc);
        rows = new ArrayList<>();
        PrefinishedOutput queryResult;
        String query = getCSVTableQueryForModel(true);
        try{
            System.out.println("Query at top level in convertWithQuery\n" + query.toString());
            queryResult = queryRDFModel(query, true);

        } catch(IndexOutOfBoundsException ex){
            query = getCSVTableQueryForModel(false);
            queryResult = queryRDFModel(query, false);
        }

        //System.out.println("CSVFileTOWriteTo: " + CSVFileTOWriteTo + "delimiter: " + delimiter);
        //FileWrite.saveCSFFileFromRows(CSVFileTOWriteTo, keys, rows, delimiter, metadata);
        return queryResult;

    }

    private void deleteBlankNodes(RepositoryConnection rc) {
        String del = "DELETE {?s ?p ?o .} WHERE { ?s ?p ?o . FILTER (isBlank(?s) || isBlank(?o))}";
        Update deleteQuery = rc.prepareUpdate(del);
        deleteQuery.execute();
        TupleQuery query = rc.prepareTupleQuery("SELECT ?s ?p ?o WHERE { ?s ?p ?o .}");
        try (TupleQueryResult result = query.evaluate()) {
            for(BindingSet sol : result){
                //System.out.println("s: " + sol.getBinding("s") +" p: " + sol.getBinding("p")+" o: " + sol.getBinding("o") + " count: ");
            }
        }
    }

    private void loadConfiguration(){

        delimiter = ConfigurationManager.getVariableFromConfigFile("input.delimiter");
        System.out.println("READ delimiter from input.delimiter to: " + delimiter);

        CSVFileTOWriteTo = ConfigurationManager.getVariableFromConfigFile("input.outputFileName");
        System.out.println("READ delimiter from input.CSVFileTOWriteTo to: " + CSVFileTOWriteTo);
    }

    private String getCSVTableQueryForModel(boolean askForTypes) {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        // Create the query to get all data in CSV format
        SelectQuery selectQuery = Queries.SELECT();

        Variable o = SparqlBuilder.var("o"), s = SparqlBuilder.var("s"),
                s_in = SparqlBuilder.var("s_in"),p_in = SparqlBuilder.var("p_in"),
                p = SparqlBuilder.var("p");;

        if(askForTypes){
            selectQuery.prefix(skos).select(s,o).where(s.isA(o).filterNotExists(s_in.has(p_in, s)));
        } else{
            selectQuery.prefix(skos).select(s,o).where(s.has(p, o).filterNotExists(s_in.has(p_in, s)));
        }

        System.out.println("getCSVTableQueryForModel query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    public String getQueryForSubstituteRoots(boolean askForTypes){
        // Create the query to get all data in CSV format
        SelectQuery selectQuery = Queries.SELECT();

        Variable o = SparqlBuilder.var("o"), s = SparqlBuilder.var("s"),
                p = SparqlBuilder.var("p");;

        if(askForTypes){
            selectQuery.select(s,o).where(s.isA(o));
        } else{
            selectQuery.select(s,o).where(s.has(p, o));
        }

        System.out.println("getQueryForSubstituteRoots query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    private String getQueryForRoot(String root){
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        SelectQuery selectQuery = Queries.SELECT();

        SimpleValueFactory rdf = SimpleValueFactory.getInstance();
        Variable o = SparqlBuilder.var("o"), p = SparqlBuilder.var("p");
        Iri subjectIRI = iri(root);
        selectQuery.prefix(skos).select(p,o).where(subjectIRI.has(p,o));
        //System.out.println("getCSVTableQueryForModel query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }


    private PrefinishedOutput<RowAndKey> queryRDFModel(String queryString, boolean askForTypes) {

        PrefinishedOutput<RowAndKey> gen = new PrefinishedOutput<RowAndKey>(new RowAndKey.RowAndKeyFactory());
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, String.valueOf(askForTypes));
        System.out.println("CONVERSION_HAS_RDF_TYPES at the beginning " + ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES));
        // Query the data and pass the result as String

        // Query in rdf4j
        // Create a new Repository.

        try (SailRepositoryConnection conn = (SailRepositoryConnection) db.getConnection()) {

            // Open a connection to the database
            NotifyingSailConnection sailConn = (NotifyingSailConnection) conn.getSailConnection();
            sailConn.addConnectionListener(new SailConnectionListener() {

                @Override
                public void statementRemoved(Statement removed) {
                    //System.out.println("removed: " + removed);
                }

                @Override
                public void statementAdded(Statement added) {
                    System.out.println("added: " + added);
                }
            });

            while(!conn.isEmpty()) {

                TupleQuery query = conn.prepareTupleQuery(queryString);
                //System.out.println("query.getDataset()" + query.getDataset());
                // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
                try (TupleQueryResult result = query.evaluate()) {
                    // we just iterate over all solutions in the result...
                    //System.out.println();
                    //System.out.println(result == null);
                    //System.out.println("Binding names: " + result.getBindingNames());
                    if (result == null) {
                        return null;
                    }
                    roots = new ArrayList<>();
                    //System.out.println(result.stream().count());
                    for (BindingSet solution : result) {
                        // ... and print out the value of the variable binding for ?s and ?n
                        //System.out.println("?subject = " + solution.getValue("s") + " ?predicate = " + solution.getValue("p") + " is a o=" + solution.getValue("o"));
                        //System.out.println("?subject = " + solution.getValue("s") + " added to roots");
                        System.out.println("?subject = " + solution.getValue("s") + " is a o=" + solution.getValue("o") + " added to roots");
                        if(!roots.contains(solution.getValue("s"))){
                            roots.add(solution.getValue("s"));
                            System.out.println("Root: " + solution.getValue("s"));
                        }
                        Row newRow = new Row(solution.getValue("s"),solution.getValue("o"),askForTypes);
                        queryForSubjects(conn, newRow, solution.getValue("s"), solution.getValue("s"), null, askForTypes, 0);
                        System.out.println();
                        rows.add(newRow);
                    }
                    System.out.println("After loop with results of query " + queryString);
                    //countDominantPredicates(conn, roots);
                    if(roots.isEmpty()){
                        System.out.println("Roots is empty");
                        // NO ROOTS found, find different supplement roots
                        TupleQuery queryForSubstituteRoots = conn.prepareTupleQuery(getQueryForSubstituteRoots(askForTypes));
                        try (TupleQueryResult resultForSubstituteRoots = queryForSubstituteRoots.evaluate()) {
                            if(resultForSubstituteRoots.hasNext()){
                                for (BindingSet solution : resultForSubstituteRoots) {
                                    // ... and print out the value of the variable binding for ?s and ?n
                                    //System.out.println("?subject = " + solution.getValue("s") + " ?predicate = " + solution.getValue("p") + " is a o=" + solution.getValue("o"));
                                    if(!roots.contains(solution.getValue("s"))){
                                        roots.add(solution.getValue("s"));
                                    }
                                }
                            } else{
                                queryForSubstituteRoots = conn.prepareTupleQuery(getQueryForSubstituteRoots(askForTypes));
                                try (TupleQueryResult resultForSubstituteRoots2 = queryForSubstituteRoots.evaluate()) {
                                }

                            }

                        }
                    }
                    countDominantTypes(conn, roots, askForTypes);
                    //Value dominantType = getDominantType();
                    //String dominantPredicate = getDominantPredicate();
                    //System.out.println("Here begins creating of of file");
                    //System.out.println("Before recursiveQueryForFiles(conn, dominantType, askForTypes)");

                    //System.out.println("After recursiveQueryForFiles(conn, dominantType, askForTypes)");

/*
                    // For all the found roots, make rows. Roots must have the same rdf:type
                    for (Value root : roots) {
                        // new Row with the found subject as its id
                        Row newRow = new Row(root,solution.getValue("o"),askForTypes);
                        queryForSubjects(conn, newRow, root, root, dominantType, askForTypes);
                        rows.add(newRow);

                    }

 */


                    //resultCSV = result.toString();
                    result.close();
                }
                queryString = getCSVTableQueryForModel(true);
            }
            //System.out.println("allRows size #: " + allRows.size());
            //allKeys.forEach(k -> System.out.print("key: " + k + " "));
            //System.out.println();
            rows.forEach(k -> System.out.println("Row: " + k.id.stringValue() + " " + k.columns.entrySet()));
            //System.out.println();

                //System.out.println("Adding rowAndKey #: " + i);

                ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, String.valueOf(askForTypes));


            //System.out.print(gen.get());


        }

        // Verify the output in console

        //System.out.println(resultString);
        gen.prefinishedOutput.rows.addAll(rows);
        gen.prefinishedOutput.keys.addAll(keys);

      /*
        int i = 0;
        for( RowAndKey rowsAndKey : gen.prefinishedOutput.rowsAndKeys) {
            //System.out.println("KEYS[" + i + "]:");
            //rowsAndKey.getKeys().forEach(k -> System.out.print(k + ", "));
            //System.out.println("ROWS[" + i + "]:");
            //rowsAndKey.getRows().forEach(r -> System.out.println("id=" + r.id + " type=" + r.type + " columns=" + r.columns));
            //System.out.println();
            i++;
        }
*/


        //saveCSVasFile("resultCSVPrimer");
        //return resultCSV;
        return gen;
    }

    private String getDeletePredicatesObjectsForRoot(Value root, Value dominantType, boolean askForTypes) {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);

        ModifyQuery selectQuery = Queries.DELETE();
        String query;

        Variable o = SparqlBuilder.var("o"), p = SparqlBuilder.var("p"), s = SparqlBuilder.var("s");
        Iri subjectIRI = iri(root.toString());
        Iri dominantTypeIRI = iri(dominantType.toString());

        if(askForTypes){
            query = selectQuery.prefix(skos).delete().where(subjectIRI.has(p,o).andIsA(dominantTypeIRI)).getQueryString();
        } else{
            query = selectQuery.prefix(skos).delete().where(subjectIRI.has(p,o)).getQueryString();
        }

        if(root.isBNode()){
            query = changeIRItoBNode(query);
        }
        //System.out.println("getDeletePredicatesObjectsForRoot query string\n" + query);
        return query;
    }

    private void queryForSubjects(RepositoryConnection conn, Row newRow, Value root, Value subject, Value dominantType, boolean askForTypes, int level) {
        String queryToGetAllPredicatesAndObjects = getQueryToGetObjectsForRoot(subject, dominantType, askForTypes);
        //System.out.println("queryToGetAllPredicatesAndObjects =  " + queryToGetAllPredicatesAndObjects);
        TupleQuery query = conn.prepareTupleQuery(queryToGetAllPredicatesAndObjects);
        Value encloseInDoubleQuotes = null;
        Value predicateToDelete = null;
        Value objectToDelete = null;
        assert root != null;
        try (TupleQueryResult result = query.evaluate()) {
            predicateToDelete = null;
            objectToDelete = null;
            //System.out.println("try (TupleQueryResult result = query.evaluate() " );
            //newRow.id = root;

            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                // We found a root row
                //System.out.println("queryForSubjects solution.getBindingNames() =  " + solution.getBindingNames());
                //System.out.println("queryForSubjects solution.size() =  " + solution.size());
                //System.out.println("queryForSubjects solution.getBinding(\"o\").getValue() =  " + solution.getBinding("o").getValue().toString());
                System.out.println("newRow.columns.containsKey(solution.getBinding(\"p\").getValue() =  " + newRow.columns.containsKey(solution.getBinding("p").getValue()));
                /*
                if(newRow.columns.get(solution.getBinding("p")) != null){
                    //System.out.println(" newRow.columns.get(solution.getBinding(\"p\").getValue()).type == TypeOfValue.IRI =  " +  newRow.columns.get(solution.getBinding("p").getValue()).type );
                }
                //System.out.println("isIRI =  " + solution.getBinding("o").getValue().isIRI() + " isBNode = " + solution.getBinding("o").getValue().isBNode() + " isLiteral =" + solution.getBinding("o").getValue().isLiteral());

                // Old value in the column have IRI objects and the new object is IRI
                //System.out.println("newRow.columns.keySet()");

                for(Value p : newRow.columns.keySet()){
                    //System.out.println(p.toString());
                }
                if(newRow.columns.keySet().stream().anyMatch( key -> ((IRI)key).toString().equalsIgnoreCase(solution.getBinding("p").getValue().toString()))){
                    //System.out.println("KEY STRING MATCHES");
                } else{
                    //System.out.println("KEY STRING NOT MATCHES in keyset: ");
                    //newRow.columns.keySet().stream().forEach( key -> System.out.println(key.toString()));
                    //System.out.println( " key string in solution: " + solution.getBinding("p").getValue().toString());
                    //System.out.println();
                }

                 */
                Value keyForColumnsMap = solution.getBinding("p").getValue();
                if(level != 0){
                    ValueFactory valueFactory = SimpleValueFactory.getInstance();

                    // Create a new IRI

                    String newValueForMap = solution.getBinding("p").getValue().stringValue() + "_MULTILEVEL_" + ((IRI)subject).getLocalName();
                    System.out.println("newValueForMap" +newValueForMap);
                    IRI iri = valueFactory.createIRI(newValueForMap);
                    keyForColumnsMap = iri;
                }
                if(newRow.columns.containsKey(keyForColumnsMap) &&
                        newRow.columns.get(keyForColumnsMap).type == TypeOfValue.IRI &&
                        solution.getBinding("o").getValue().isIRI()){
                    List<Value> oldStringValue = newRow.columns.get(keyForColumnsMap).values;
                    oldStringValue.add(solution.getBinding("o").getValue());
                    TypeIdAndValues oldTypeIdAndValues =  newRow.columns.get(keyForColumnsMap);
                    oldTypeIdAndValues.values = oldStringValue;
                    newRow.columns.put(keyForColumnsMap, oldTypeIdAndValues);
                    encloseInDoubleQuotes = solution.getBinding("p").getValue();
                    System.out.println("IRI already in the map o Added to oldRow.columns predicate = " + solution.getBinding("p").getValue() + "  Value of = " + solution.getBinding("o").getValue());

                } else if(newRow.columns.containsKey(keyForColumnsMap) &&
                        newRow.columns.get(keyForColumnsMap).type == TypeOfValue.LITERAL &&
                        solution.getBinding("o").getValue().isLiteral()){
                    List<Value> oldStringValue = newRow.columns.get(keyForColumnsMap).values;
                    oldStringValue.add(solution.getBinding("o").getValue());
                    TypeIdAndValues oldTypeIdAndValues =  newRow.columns.get(keyForColumnsMap);
                    oldTypeIdAndValues.values = oldStringValue;
                    newRow.columns.put(keyForColumnsMap, oldTypeIdAndValues);
                    encloseInDoubleQuotes = solution.getBinding("p").getValue();
                    System.out.println("LITERAL already in the map o Added to oldRow.columns predicate = " + solution.getBinding("p").getValue() + "  Value of = " + solution.getBinding("o").getValue());

                }
                else { // There is no such key (column) in the map
                    //if(!solution.getValue("p").toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")){
                    TypeOfValue newType = (solution.getBinding("o").getValue().isIRI()) ? TypeOfValue.IRI :
                            (solution.getBinding("o").getValue().isBNode()) ? TypeOfValue.BNODE : TypeOfValue.LITERAL;
                    assert root != null;
                    newRow.columns.put(keyForColumnsMap, new TypeIdAndValues(subject, newType,
                            new ArrayList<>(Arrays.asList(solution.getBinding("o").getValue()))));
                    System.out.println("There is no such key in columns map o Added to newRow.columns predicate = " + keyForColumnsMap + "  Value of = " + solution.getBinding("o").getValue());
                    //}
                    if(solution.getBinding("o").getValue().isBNode()){
                        //System.out.println("o is BNode with Value of = " + solution.getBinding("o").getValue());
                    }


                }

                if(!keys.contains(keyForColumnsMap)){
                    keys.forEach(k -> System.out.print("key: " + k));
                    keys.add(keyForColumnsMap);
                    //System.out.println();
                    //System.out.println("Key added from solution: " + solution.getValue("p").toString() );
                }

                if(solution.getValue("o") != null && solution.getValue("o").isIRI()){
                    System.out.println("Querying with queryForSubjects for o=" + solution.getValue("o").stringValue());
                    queryForSubjects(conn, newRow, root, solution.getValue("o"), dominantType, askForTypes, level + 1);
                }
                //System.out.println("BindingSet solution: result " + solution.getValue("p").toString() + " " + solution.getValue("o").toString());

                // Delete the triple from the storage
                Resource subjectToDelete = Values.iri(subject.toString());
                IRI predicate = Values.iri(solution.getValue("p").toString());
                System.out.println("Wanting to delete =  " + subject + ", " + predicate +  ", " + ""  + solution.getValue("o").toString());
                if(subjectIsInOnlyOneTripleAsObject(conn, subjectToDelete)){
                    conn.remove(subjectToDelete,predicate, solution.getValue("o"));
                }


            }
        } catch(QueryEvaluationException ex){
            System.out.println("QueryEvaluationException");
            ex.printStackTrace();
        }
    }

    private boolean subjectIsInOnlyOneTripleAsObject(RepositoryConnection conn, Resource subjectToDelete) {
        SelectQuery selectQuery = Queries.SELECT();

        SimpleValueFactory rdf = SimpleValueFactory.getInstance();
        Variable o = SparqlBuilder.var("o"), p = SparqlBuilder.var("p");
        String selectQueryString = """
                ASK {
                  {
                    SELECT (COUNT(?s) AS ?count)
                    WHERE {
                      ?s ?p <%s> .
                    }
                    HAVING (?count <= 1)
                  }
                }""".formatted(subjectToDelete.toString());
        System.out.println("query to ASK \n" + selectQueryString);

        BooleanQuery query = conn.prepareBooleanQuery(selectQueryString);

        System.out.println("Result of the ASK: " + query.evaluate());

        return query.evaluate();
    }

    private String getQueryToGetObjectsForRoot(Value root, Value dominantType, boolean askForTypes) {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        SelectQuery selectQuery = Queries.SELECT();
        String query;
        Variable o = SparqlBuilder.var("o"), p = SparqlBuilder.var("p");
        Iri subjectIRI = iri(root.toString());
        //Iri dominantTypeIRI = iri(dominantType.toString());
        if(askForTypes){
            query = selectQuery.prefix(skos).select(p,o).where(subjectIRI.has(p,o)).getQueryString(); //andIsA(dominantTypeIRI)).getQueryString();
        } else{
            query = selectQuery.prefix(skos).select(p,o).where(subjectIRI.has(p,o)).getQueryString();
        }
        if(root.isBNode()){
            query = changeIRItoBNode(query);
        }
        //System.out.println("getCSVTableQueryForModel query string\n" + query);
        return query;
    }

    private String changeIRItoBNode(String query){
        String newQuery = query.replace("<_:", "_:");
        newQuery = newQuery.replace("> ?p", " ?p");
        newQuery = newQuery.replace("> a", " a");
        return newQuery;
    }

    private void countDominantTypes(RepositoryConnection conn, ArrayList<Value> roots, boolean askForTypes) {
        mapOfTypesAndTheirNumbers = new HashMap<>();

        //System.out.println("Roots number "  + roots.size());
        for (Value root : roots) {
            String queryForPredicates = getQueryForTypes(root, askForTypes);
            TupleQuery query = conn.prepareTupleQuery(queryForPredicates);

            try (TupleQueryResult result = query.evaluate()) {
                //System.out.println("Found dominant possibility predicates: " + query.evaluate().stream().count());
                // we just iterate over all solutions in the result...
                for (BindingSet solution : result) {
                    Value key;
                    if(askForTypes){
                        key = solution.getValue("o");
                    } else{
                        key = solution.getValue("p");
                        //System.out.println("Found dominant possibility predicates: " + key);

                    }


                    if (mapOfTypesAndTheirNumbers.containsKey(key)) {
                        Integer oldValue = mapOfTypesAndTheirNumbers.get(key);
                        Integer newValue = oldValue+1;
                        mapOfTypesAndTheirNumbers.put(key, newValue);
                        //System.out.println("Adding key for sorting predicates: " + key + " number= " + newValue);
                    } else {
                        mapOfTypesAndTheirNumbers.put(key, 1);
                        //System.out.println("Adding key for sorting predicates: " + key + " number=1");
                    }
                }

            }
            catch(QueryEvaluationException ex){
                System.out.println("There has been a problem with query evaluation " );
                ex.printStackTrace();
            }
        }
    }

    private String getQueryForTypes(Value root, boolean askForTypes) {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        SelectQuery selectQuery = Queries.SELECT();
        String query;
        SimpleValueFactory rdf = SimpleValueFactory.getInstance();
        Variable o = SparqlBuilder.var("o"),  p = SparqlBuilder.var("p");
        Iri subjectIRI = iri(root.toString());

        if(askForTypes){
            query = selectQuery.prefix(skos).select(o).where(subjectIRI.isA(o)).getQueryString();
        } else{
            query = selectQuery.prefix(skos).select(p, o).where(subjectIRI.has(p, o)).getQueryString();
        }
        if(root.isBNode()){
            query = changeIRItoBNode(query);
        }
        //System.out.println("getQueryForTypes query string\n" + query);
        return query;
    }

    private Value getDominantType() {
        Value dominantType = null;
        //System.out.println("getDominantType");

        List<Map.Entry<Value, Integer>> sortedEnties = entriesSortedByValues(mapOfTypesAndTheirNumbers);
        for(Map.Entry<Value, Integer> entry : sortedEnties){
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        dominantType = sortedEnties.get(0).getKey();

        //System.out.println("Chosen dominant type is " + dominantType);

        return dominantType;
    }

    private void countDominantPredicates(RepositoryConnection conn, ArrayList<String> roots) {
        mapOfPredicatesAndTheirNumbers = new HashMap<>();

        //System.out.println("Roots number "  + roots.size());
        for (String root : roots) {
            String queryForPredicates = getQueryForRoot(root);
            TupleQuery query = conn.prepareTupleQuery(queryForPredicates);

            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                for (BindingSet solution : result) {
                    String key = solution.getValue("p").toString();

                    if (mapOfPredicatesAndTheirNumbers.containsKey(key)) {
                        Integer oldValue = mapOfPredicatesAndTheirNumbers.get(key);
                        Integer newValue = oldValue+1;
                        mapOfPredicatesAndTheirNumbers.put(key, newValue);
                    } else {
                        mapOfPredicatesAndTheirNumbers.put(key, 1);
                    }
                }

            }
        }
    }

    static <K,V extends Comparable<? super V>>
    List<Map.Entry<K, V>> entriesSortedByValues(Map<K,V> map) {

        List<Map.Entry<K,V>> sortedEntries = new ArrayList<Map.Entry<K,V>>(map.entrySet());

        Collections.sort(sortedEntries,
                new Comparator<Map.Entry<K,V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                }
        );

        return sortedEntries;
    }

    private String getDominantPredicate(){
        String dominantPredicate = null;
        //System.out.println("getDominantPredicate");

        List<Map.Entry<String, Integer>> sortedEnties = entriesSortedByValues(mapOfPredicatesAndTheirNumbers);
        for(Map.Entry<String, Integer> entry : sortedEnties){
            //System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        dominantPredicate = sortedEnties.get(0).getKey();
        //System.out.println("Chosen dominant predicate is " + dominantPredicate);

        return dominantPredicate;
    }

    private void augmentMapsByMissingKeys(){
        for(Row row : rows){
            ArrayList<Value> missingKeys = new ArrayList<>();
            for(Value key : keys){
                if(!row.columns.keySet().contains(key)){
                    missingKeys.add(key);
                }
            }
            missingKeys.forEach(key -> row.columns.put(key, null));
        }
    }

    private void recursiveQueryForSubjects(RepositoryConnection conn,Row row, Value object, boolean askForTypes){

        String queryForSubjects = createQueryForSubjects(object);
        TupleQuery query = conn.prepareTupleQuery(queryForSubjects);

        try (TupleQueryResult result = query.evaluate()) {
            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                if(row.id.equals(object)){
                    TypeOfValue newType = (solution.getBinding("s").getValue().isIRI()) ? TypeOfValue.IRI :
                            (solution.getBinding("s").getValue().isBNode()) ? TypeOfValue.BNODE : TypeOfValue.LITERAL;
                    assert solution.getBinding("s").getValue() != null;
                    row.columns.put(solution.getValue("p"), new TypeIdAndValues(solution.getBinding("s").getValue(), newType,new ArrayList<>(Arrays.asList(solution.getBinding("s").getValue()))));
                    if(!keys.contains(solution.getValue("p"))){
                        keys.add(solution.getValue("p"));
                    }
                } else {
                    TypeOfValue newType = (solution.getBinding("s").getValue().isIRI()) ? TypeOfValue.IRI :
                            (solution.getBinding("s").getValue().isBNode()) ? TypeOfValue.BNODE : TypeOfValue.LITERAL;
                    assert solution.getBinding("s").getValue() != null;
                    row.columns.put(solution.getValue("p"), new TypeIdAndValues(solution.getBinding("s").getValue(), newType,new ArrayList<>(Arrays.asList(solution.getBinding("s").getValue()))));
                    if(!keys.contains(solution.getValue("p"))){
                        keys.add(solution.getValue("p"));
                    }
                    /*
                    if(!keys.contains(keyOfNextLevels)) {

                        keys.add(keyOfNextLevels);
                    }

                     */



                }

                //System.out.println("BindingSet solution: result " + solution.getValue("p").toString() + " " + solution.getValue("s").toString());
                if(solution.getValue("s").isIRI()){
                    recursiveQueryForSubjects(conn, row, solution.getValue("s"), askForTypes);
                }
            }
        }
    }

    private String createQueryForSubjects(Value object) {
        SelectQuery selectQuery = Queries.SELECT();
        Iri iri = iri(((IRI)object).stringValue());
        Variable s = SparqlBuilder.var("s"), p = SparqlBuilder.var("p");
        selectQuery.select(s,p).where(iri.has(p, s));
        //System.out.println(selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }
}

/*
private void augmentMapsByMissingKeys(){
        for(Row row : rows){
            ArrayList<Value> missingKeys = new ArrayList<>();
            for(Value key : keys){
                if(!row.columns.keySet().contains(key)){
                    missingKeys.add(key);
                }
            }
            missingKeys.forEach(key -> row.columns.put(key, null));
        }
    }


 */
