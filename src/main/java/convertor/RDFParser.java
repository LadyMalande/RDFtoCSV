package convertor;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.base.InternedIRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import support.FileWrite;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class RDFParser {

    Model model;
    CSV outputCSV;
    int columnIndex;

    public RDFParser(Model m) {
        this.model = m;
        columnIndex = 0;
    }

    public void printModelByRows(){
        Set<Resource> subjects = new HashSet<>();
        System.out.print("The model is: \n");
        //model.forEach(System.out::println);
        System.out.println(model.getNamespace("skos"));
        for(Statement statement: model){
            subjects.add(statement.getSubject());
        }
        System.out.println(subjects);
        Set<Value> types = model.filter(null, RDF.TYPE, null).objects();
        System.out.println(types);
        System.out.println(RDF.TYPE.getNamespace());
        System.out.println(RDF.TYPE.getLocalName());
        System.out.println(RDF.TYPE);
        int numberOfFile = 1;
        for(Value type : types){
            //Vocabularies.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
            String[] namespaceLocal = type.stringValue().split("#");
            InternedIRI typeIRI = new InternedIRI(namespaceLocal[0] + "#", namespaceLocal[1]);
            System.out.println("Type " + typeIRI.stringValue()  + " is IRI? " + typeIRI.isIRI());


            Set<Resource> resourcesByType = model.filter(null, RDF.TYPE, typeIRI).subjects();
            System.out.println(resourcesByType);
            // Filter separate objects in those types
            // Make x files depending on the number of types
            File fileToWriteTo = FileWrite.makeFile(numberOfFile);
            numberOfFile++;
            assert fileToWriteTo != null;
            FileWrite.writeSubjectsTotheFile(fileToWriteTo, resourcesByType);
            linkSubjectsToOtherSubjects();
        }
    }

    private void linkSubjectsToOtherSubjects(){
        Set<Resource> resourcesInObject = model.filter(null, RDF.TYPE, null).subjects();
        for(Resource r : resourcesInObject){
            Set<Resource> linked = model.filter(null, null, r).subjects();
            System.out.println("Linked to " + r.stringValue() + " are :" + linked);
            if(linked.size() > 0){
                // link it to the row of the resource it is linked to
                linkTheResourceInObjectToSubject(linked, r);
                System.out.println("We are linking " + linked.toString() + " to " + r.toString());
            }
        }
    }

    private void linkTheResourceInObjectToSubject(Set<Resource> list, Resource addColumn) {
        for(int i = 0; i < list.size(); i++){
            //outputCSV.getTable()[0][i] =
        }

    }


}
