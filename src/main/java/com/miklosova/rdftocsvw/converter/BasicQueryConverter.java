package com.miklosova.rdftocsvw.converter;

import com.miklosova.rdftocsvw.converter.data_structure.*;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.ConverterHelper;
import lombok.extern.java.Log;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.SailConnectionListener;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;

import java.util.*;

import static com.miklosova.rdftocsvw.support.StandardModeCsvwIris.CSVW_TableGroup;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

/**
 * The type Basic query converter.
 */
@Log
public class BasicQueryConverter extends ConverterHelper implements IQueryParser {

    /**
     * The Roots.
     */
    Set<Value> roots;
    /**
     * The Rows.
     */
    ArrayList<Row> rows;
    /**
     * The Keys.
     */
    Set<Value> keys;

    /**
     * The Ir ialready processed times.
     */
    Map<Value, Integer> IRIalreadyProcessedTimes;
    /**
     * The Already processed times.
     */
    int alreadyProcessedTimes = 0;
    /**
     * The Db.
     */
    Repository db;

    /**
     * The Rc.
     */
    RepositoryConnection rc;

    /**
     * Instantiates a new Basic query converter.
     *
     * @param db the db
     */
    public BasicQueryConverter(Repository db) {
        this.keys = new HashSet<>();
        this.db = db;
        this.IRIalreadyProcessedTimes = new HashMap<>();
    }

    @Override
    public PrefinishedOutput<RowsAndKeys> convertWithQuery(RepositoryConnection rc) {
        this.rc = rc;
        changeBNodesForIri(rc);
        deleteBlankNodes(rc);
        rows = new ArrayList<>();
        PrefinishedOutput<RowAndKey> queryResult = new PrefinishedOutput<>((new RowAndKey.RowAndKeyFactory()).factory());
        String query = getCSVTableQueryForModel(true);
        try {
            queryResult = queryRDFModel(query, true);

        } catch (IndexOutOfBoundsException ex) {
            query = getCSVTableQueryForModel(false);
            queryResult = queryRDFModel(query, false);
        }
        RowsAndKeys rak = new RowsAndKeys();
        ArrayList<RowAndKey> rakArray = new ArrayList<>();
        assert queryResult != null;
        System.out.println("queryResult instanceof PrefinishedOutput<RowAndKey> " + (queryResult instanceof PrefinishedOutput<RowAndKey>));
        System.out.println("queryResult instanceof PrefinishedOutput<RowAndKey> " + (queryResult.getPrefinishedOutput() instanceof RowAndKey));
        rakArray.add((RowAndKey) queryResult.getPrefinishedOutput());
        System.out.println("after rakArray.add(queryResult.getPrefinishedOutput());");
        rak.setRowsAndKeys(rakArray);
        return new PrefinishedOutput<RowsAndKeys>(rak);
    }


    private PrefinishedOutput<RowAndKey> queryRDFModel(String queryString, boolean askForTypes) throws IndexOutOfBoundsException {
        System.out.println("queryRDFModel start");
        PrefinishedOutput<RowAndKey> gen = new PrefinishedOutput<>((new RowAndKey.RowAndKeyFactory()).factory());
        System.out.println("gen instanceof PrefinishedOutput<RowAndKey> " + (gen instanceof PrefinishedOutput<RowAndKey>));
        System.out.println("gen.prefinishedOutput instanceof RowAndKey " + (gen.getPrefinishedOutput() instanceof RowAndKey));
        System.out.println("after initializing gen start");
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, String.valueOf(askForTypes));
        //System.out.println("CONVERSION_HAS_RDF_TYPES at the beginning " + ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES));
        // Query the data and pass the result as String

        // Query in rdf4j
        // Create a new Repository.

        try (SailRepositoryConnection conn = (SailRepositoryConnection) db.getConnection()) {

            // Open a connection to the database
            NotifyingSailConnection sailConn = (NotifyingSailConnection) conn.getSailConnection();
            sailConn.addConnectionListener(new SailConnectionListener() {

                @Override
                public void statementRemoved(Statement removed) {
                    System.out.println("removed: " + removed);
                }

                @Override
                public void statementAdded(Statement added) {
                    System.out.println("added: " + added);
                }
            });

            while (!conn.isEmpty()) {
                System.out.println("Conn is not empty , size: " + conn.size());
                System.out.println("queryString: \n" + queryString);
                TupleQuery query = conn.prepareTupleQuery(queryString);
                // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
                try (TupleQueryResult result = query.evaluate()) {
                    // we just iterate over all solutions in the result...
                    if (result == null) {
                        System.out.println("result == null ");
                        return null;
                    }
                    List<BindingSet> resultList = result.stream().toList(); // Collect into a list

                    System.out.println("result count = " + resultList.size());

                  if (resultList.isEmpty() && askForTypes) {
                        System.out.println("resultList.isEmpty() " + true);
                        throw new IndexOutOfBoundsException();
                    }
                    roots = new HashSet<>();
                    for (BindingSet solution : resultList) {
                        // ... and print out the value of the variable binding for ?s and ?n
                        if (solution.getValue("o").stringValue().equalsIgnoreCase(CSVW_TableGroup)) {
                            //System.out.println("Table Group, engaging in StandardModeConverter");
                            StandardModeConverter smc = new StandardModeConverter(db);
                            RowAndKey smcOutputRowAndKey = new RowAndKey();
                            PrefinishedOutput<RowAndKey> smcOutput = new PrefinishedOutput<>((new RowAndKey.RowAndKeyFactory()).factory());
                            smcOutputRowAndKey = smc.convertWithQuery(this.rc).getPrefinishedOutput().getRowsAndKeys().get(0);
                            smcOutput.getPrefinishedOutput().getRows().addAll(smcOutputRowAndKey.getRows());
                            smcOutput.getPrefinishedOutput().getKeys().addAll(smcOutputRowAndKey.getKeys());
                            return smcOutput;
                        }
                        roots.add(solution.getValue("s"));

                        System.out.println("Root: " + solution.getValue("s"));
                        Row newRow = new Row(solution.getValue("s"), solution.getValue("o"), askForTypes);
                        queryForSubjects(conn, newRow, solution.getValue("s"), solution.getValue("s"),  0);

                        deleteQueryToGetObjectsForRoot(solution.getValue("s"));
                        System.out.println("Deleting triples with s = " + solution.getValue("s").stringValue());


                        System.out.println("new Row is: " + newRow.id.stringValue() +
                                " type: " + newRow.type.stringValue() +
                                " isRdfType=" + newRow.isRdfType +
                                " newRow columns " + newRow.columns.entrySet());


                        if (rows.stream().anyMatch(row -> row.id.equals(newRow.id))) {
                            // a row with the same id is already present in the data, don't create new one
                        } else {
                            rows.add(newRow);
                        }

                    }
                    //countDominantPredicates(conn, roots);
                    if (roots.isEmpty()) {
                        System.out.println("Roots is empty");
                        // NO ROOTS found, find different supplement roots
                        TupleQuery queryForSubstituteRoots = conn.prepareTupleQuery(getQueryForSubstituteRoots(askForTypes));
                        try (TupleQueryResult resultForSubstituteRoots = queryForSubstituteRoots.evaluate()) {
                            if (resultForSubstituteRoots.hasNext()) {
                                int i = 0;
                                for (BindingSet solution : resultForSubstituteRoots) {
                                    // ... and print out the value of the variable binding for ?s and ?n
                                    //System.out.println("?subject = " + solution.getValue("s") + " ?predicate = " + solution.getValue("p") + " is a o=" + solution.getValue("o"));
                                    if (!(roots.contains(solution.getValue("s")))) {
                                        System.out.println("Adding to roots in substituteRoots ?subject = " + solution.getValue("s") + " ?predicate = " + solution.getValue("p") + " is a o=" + solution.getValue("o") + " roots size = " + roots.size());
                                        roots.add(solution.getValue("s"));
                                    }
                                    System.out.println("Number of substitute roots: " + i);
                                    i++;
                                }
                            } else {
                                throw new IndexOutOfBoundsException();

                            }

                        }
                    }
                } catch(IndexOutOfBoundsException ex){
                    throw new IndexOutOfBoundsException();
                }
                /*
                countDominantTypes(conn, roots, askForTypes);
                Value dominantType = getDominantType();
                getSelectQuery(askForTypes, null, null, dominantType.toString());
                 */
                queryString = getQueryForSubstituteRoots(askForTypes);//getCSVTableQueryForModel(askForTypes);
            }
            //rows.forEach(k -> System.out.println("Row: " + k.id.stringValue() + " " + k.columns.entrySet()));

            ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, String.valueOf(askForTypes));
        }catch(IndexOutOfBoundsException ex){
            throw new IndexOutOfBoundsException();
        }

        // Verify the output in console
        gen.getPrefinishedOutput().getRows().addAll(rows);
        gen.getPrefinishedOutput().getKeys().addAll(keys);
        System.out.println("gen instanceof PrefinishedOutput<RowAndKey> 2" + (gen instanceof PrefinishedOutput<RowAndKey>));
        System.out.println("gen.prefinishedOutput instanceof RowAndKey 2" + (gen.getPrefinishedOutput() instanceof RowAndKey));
        return gen;
    }

    private void queryForSubjects(RepositoryConnection conn, Row newRow, Value root, Value subject, int level) {
        String queryToGetAllPredicatesAndObjects = getQueryToGetObjectsForRoot(subject);
        System.out.println("conn.size() " + conn.size() + " root("+ root.stringValue() +") subject = " + subject.stringValue()) ;
        TupleQuery query = conn.prepareTupleQuery(queryToGetAllPredicatesAndObjects);
        //System.out.println("tuple query:" + query.toString()) ;
        try (TupleQueryResult result = query.evaluate()) {


            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                // We found a root row
                //System.out.println("newRow.columns.containsKey(solution.getBinding(\"p\").getValue() =  " + newRow.columns.containsKey(solution.getBinding("p").getValue()));
                //System.out.println("subject = "+ subject + " p = " +solution.getBinding("p").getValue().stringValue()+ " o = " + solution.getBinding("o").getValue().stringValue());

                Value keyForColumnsMap = solution.getBinding("p").getValue();
                if (level != 0) {
                    ValueFactory valueFactory = SimpleValueFactory.getInstance();

                    // Create a new IRI

                    String newValueForMap = solution.getBinding("p").getValue().stringValue() + "_MULTILEVEL_";// + ((IRI) subject).getLocalName();
                    //System.out.println("newValueForMap" + newValueForMap);
                    keyForColumnsMap = valueFactory.createIRI(newValueForMap);
                }
                if (newRow.columns.containsKey(keyForColumnsMap) &&
                        newRow.columns.get(keyForColumnsMap).type == TypeOfValue.IRI &&
                        solution.getBinding("o").getValue().isIRI()) {
                    List<Value> oldStringValue = newRow.columns.get(keyForColumnsMap).values;
                    oldStringValue.add(solution.getBinding("o").getValue());
                    TypeIdAndValues oldTypeIdAndValues = newRow.columns.get(keyForColumnsMap);
                    oldTypeIdAndValues.values = oldStringValue;
                    newRow.columns.put(keyForColumnsMap, oldTypeIdAndValues);
                    //System.out.println("IRI already in the map o Added to oldRow.columns predicate = " + solution.getBinding("p").getValue() + "  Value of = " + solution.getBinding("o").getValue());

                } else if (newRow.columns.containsKey(keyForColumnsMap) &&
                        newRow.columns.get(keyForColumnsMap).type == TypeOfValue.LITERAL &&
                        solution.getBinding("o").getValue().isLiteral()) {
                    List<Value> oldStringValue = newRow.columns.get(keyForColumnsMap).values;
                    oldStringValue.add(solution.getBinding("o").getValue());
                    TypeIdAndValues oldTypeIdAndValues = newRow.columns.get(keyForColumnsMap);
                    oldTypeIdAndValues.values = oldStringValue;
                    newRow.columns.put(keyForColumnsMap, oldTypeIdAndValues);
                    //System.out.println("LITERAL already in the map o Added to oldRow.columns predicate = " + solution.getBinding("p").getValue() + "  Value of = " + solution.getBinding("o").getValue());

                } else { // There is no such key (column) in the map
                    //if(!solution.getValue("p").toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")){
                    TypeOfValue newType = (solution.getBinding("o").getValue().isIRI()) ? TypeOfValue.IRI :
                            (solution.getBinding("o").getValue().isBNode()) ? TypeOfValue.BNODE : TypeOfValue.LITERAL;
                    newRow.columns.put(keyForColumnsMap, new TypeIdAndValues(subject, newType,
                            new ArrayList<>(List.of(solution.getBinding("o").getValue()))));
                    System.out.println("There is no such key in columns map o Added to newRow.columns predicate = " + keyForColumnsMap + "  Value of = " + solution.getBinding("o").getValue());
                    //}
                }

                if (!keys.contains(keyForColumnsMap)) {
                    keys.forEach(k -> System.out.print("key: " + k));
                    keys.add(keyForColumnsMap);
                }

                if (solution.getValue("o") != null && solution.getValue("o").isIRI()) {
                    //System.out.println("Querying with queryForSubjects for o=" + solution.getValue("o").stringValue() + " level = " + level);
                    if(solution.getValue("o") != root) {
                        queryForSubjects(conn, newRow, root, solution.getValue("o"), level + 1);
                    } else {
                        //System.out.println("There is a cycle!");
                    }
                }

                // Delete the triple from the storage
                Resource subjectToDelete = Values.iri(subject.toString());
                IRI predicate = Values.iri(solution.getValue("p").toString());
                //System.out.println("Wanting to delete =  " + subject + ", " + predicate + ", " + "" + solution.getValue("o").toString());
                if (subjectIsInOnlyOneTripleAsObject(conn, subjectToDelete)) {
                    conn.remove(subjectToDelete, predicate, solution.getValue("o"));
                    System.out.println("Deleting =  " + subject + ", " + predicate + ", " + "" + solution.getValue("o").toString());
                } else {
                    if(IRIalreadyProcessedTimes.get(subjectToDelete) != null){
                        if(IRIalreadyProcessedTimes.get(subjectToDelete) +1 == alreadyProcessedTimes){
                            deleteQueryToGetObjectsForRoot(subjectToDelete);
                            System.out.println("Deleting "+ subjectToDelete +" = IRIalreadyProcessedTimes.get(subjectToDelete) +1 == alreadyProcessedTimes " + IRIalreadyProcessedTimes.get(subjectToDelete) + "+1 == " + alreadyProcessedTimes);
                        } else {
                            IRIalreadyProcessedTimes.put(subjectToDelete, IRIalreadyProcessedTimes.get(subjectToDelete) + 1);
                            //System.out.println("Increased IRIalreadyProcessedTimes for =  " + subjectToDelete.stringValue() + " times " + IRIalreadyProcessedTimes.get(subjectToDelete));
                        }
                    } else {
                        IRIalreadyProcessedTimes.put(subjectToDelete, 1);

                    }
                }


            }
        } catch (QueryEvaluationException ex) {
            System.out.println("QueryEvaluationException");
            ex.printStackTrace();
        }
    }

    private boolean subjectIsInOnlyOneTripleAsObject(RepositoryConnection conn, Resource subjectToDelete) {
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
        //System.out.println("query to ASK \n" + selectQueryString);

        String countQuery = """

                    SELECT (COUNT(?s) AS ?count)
                    WHERE {
                      ?s ?p <%s> .
                    }
                  
                """.formatted(subjectToDelete.toString());

        BooleanQuery query = conn.prepareBooleanQuery(selectQueryString);

        TupleQuery queryCount = conn.prepareTupleQuery(countQuery);
        try (TupleQueryResult result = queryCount.evaluate()) {
            for(BindingSet set : result){
                System.out.println("Result of the count for "+ subjectToDelete.stringValue() +": " + set.getValue("count"));
                alreadyProcessedTimes = Integer.parseInt(((Literal)set.getValue("count")).getLabel());
            }

        }

        //System.out.println("Result of the ASK: " + query.evaluate());

        return query.evaluate();
    }

    private String getQueryToGetObjectsForRoot(Value root) {
        SelectQuery selectQuery = Queries.SELECT();
        String query;
        Variable o = SparqlBuilder.var("o"), p = SparqlBuilder.var("p");
        Iri subjectIRI = iri(root.toString());
        query = selectQuery.select(p, o).where(subjectIRI.has(p, o)).getQueryString(); //andIsA(dominantTypeIRI)).getQueryString();
        if (root.isBNode()) {
            System.out.println("getQueryToGetObjectsForRoot if (root.isBNode())");
            query = changeIRItoBNode(query);
        }
        //System.out.println("Returning query: \n" + query);
        return query;
    }

    private void deleteQueryToGetObjectsForRoot(Value root) {
        System.out.println("Value to delete " + root.stringValue());
        String del = "DELETE { ?s ?p ?o .} WHERE { <"+root.stringValue()+"> ?p ?o .}";
        Update deleteQuery = rc.prepareUpdate(del);
        deleteQuery.execute();
        System.out.println("Deleted blank NOdes: " + deleteQuery.toString());

    }


}

