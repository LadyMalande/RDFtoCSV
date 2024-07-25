package com.miklosova.rdftocsvw.metadata_creator;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.rdf4j.model.IRI;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static org.eclipse.rdf4j.model.util.Values.iri;

public class Dereferencer {
    private String url;

    public Dereferencer(String url) {
        System.out.println("Dereferencer constructor");
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        try {
            // dereference for skos
            // Fetch the HTML page
            Document doc = Jsoup.connect(this.url).get();
            IRI iri = iri(this.url);

            String cssQuery = "#" + iri.getLocalName();
            // Find the <tr> element with id="broader"
            //System.out.println("cssQuery " + cssQuery);
            Element broaderTr = doc.select(cssQuery).first().parent().parent();
            //broaderTr.parent().parent();
            //System.out.println("text on broaderTr " + broaderTr.text());
            if (broaderTr != null) {
                // Find the next <tr> element relative to the <tr> with id="broader"
                Element nextTr = broaderTr.nextElementSibling();

                while (nextTr != null) {
                    // Find <td> elements within the sibling <tr>
                    Elements tds = nextTr.select("td");
                    for (Element td : tds) {
                        // Check if any <td> contains the text "Label:"
                        if (td.text().contains("Label:")) {
                            Element titleElement = td.nextElementSibling();
                            //System.out.println("Found <tr> sibling with <td> containing 'Label:': " + nextTr);
                            assert titleElement != null;
                            return titleElement.text();
                        }
                    }
                    nextTr = nextTr.nextElementSibling();
                }
            } else {
                System.out.println("No <tr> element with id='broader' found.");
            }
        }catch (NullPointerException ex){
            throw ex;
        } catch (IOException e) {
            System.out.println("IOException in Dereferencer.");
            //e.printStackTrace();
        }
        return null;
    }
}
