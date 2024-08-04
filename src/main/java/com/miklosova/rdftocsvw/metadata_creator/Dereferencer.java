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
    private String FOAF_PREFIX = "http://xmlns.com/foaf/0.1/";
    private String DC_PREFIX = "http://purl.org/dc/elements/1.1/";
    private String DCTERMS_PREFIX = "http://purl.org/dc/terms/";
    private String VANN_PREFIX = "http://purl.org/vocab/vann/";
    private String CC_PREFIX = "http://web.resource.org/cc/";
    private String VS_PREFIX = "http://www.w3.org/2003/06/sw-vocab-status/ns#";
    private String WOT_PREFIX = "http://xmlns.com/wot/0.1/";
    private String GEO_PREFIX = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    private String SKOS_PREFIX = "http://www.w3.org/2004/02/skos/core#";
    //private String _PREFIX = "";




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
        if(this.url.startsWith(FOAF_PREFIX)) {
            return foafDereference();
        } else if(this.url.startsWith(DC_PREFIX) || this.url.startsWith(DCTERMS_PREFIX)){
            return dcDereference();
        } else if(this.url.startsWith(VANN_PREFIX)){
            return vannDereference();
        } else if(this.url.startsWith(VS_PREFIX)){
            return vsDereference();
        } else if(this.url.startsWith(WOT_PREFIX)){
            return wotDereference();
        } else if(this.url.startsWith(SKOS_PREFIX)){
            return skosDereference();
        } else{
            System.out.println("No special title found.");
            throw new NullPointerException();
        }
    }

    private String vannDereference() {
        try {
            // dereference for skos
            // Fetch the HTML page

            IRI iri = iri(this.url);
            Document doc = Jsoup.connect(iri.getNamespace()).get();

            System.out.println("iri=" + this.url);
            String cssQuery = "h3[id=\"" + iri.getLocalName() + "\"]";
            // Find the <h3> element with id=<iriLocalName>
            System.out.println("cssQuery " + cssQuery);
            Element broaderTr = doc.select(cssQuery).first();
            if (broaderTr != null) {
                System.out.println("text of dereferenced " + broaderTr.text());
                return broaderTr.text();
            } else {
                System.out.println("No <tr> element with id='broader' found.");
            }
        } catch (NullPointerException ex){
            throw ex;
        } catch (IOException e) {
            System.out.println("IOException in Dereferencer.");
        }
        throw new NullPointerException();
    }

    private String wotDereference() {
        try {
            // dereference for foaf prefix
            // Fetch the HTML page
            Document doc = Jsoup.connect(this.url).get();
            IRI iri = iri(this.url);

            System.out.println("iri=" + this.url);
            String cssQuery = "div[id=\"term_" + iri.getLocalName() + "\"]";
            // Find the <tr> element with id="broader"
            System.out.println("cssQuery " + cssQuery);
            Element foundElement = doc.select(cssQuery).first();//.parent().parent();
            //broaderTr.parent().parent();
            System.out.println("text on broaderTr " + foundElement.text());
            if (foundElement != null) {
                // Find the next <tr> element relative to the <tr> with id="broader"
                Element elementEm = foundElement.selectFirst("em");


                assert elementEm != null;
                System.out.println("element em text = " + elementEm.text());

                return elementEm.text();
            } else {
                System.out.println("No <tr> element with id='broader' found.");
            }
        } catch (NullPointerException ex){
            throw ex;
        } catch (IOException e) {
            System.out.println("IOException in Dereferencer.");
            //e.printStackTrace();
        }
        throw new NullPointerException();
    }

    private String vsDereference() {
        try {
            // dereference for vs
            // Fetch the HTML page
            Document doc = Jsoup.connect(this.url).get();
            IRI iri = iri(this.url);

            System.out.println("iri=" + this.url);
            String cssQuery = "rdf\\:Property[rdf\\:about=\"#" + iri.getLocalName() + "\"]";
            String xpath = "//rdf:Property[@rdf:about='#" + iri.getLocalName() + "']";
            // Find the <h3> element with id=<iriLocalName>
            System.out.println("cssQuery " + cssQuery);
            //Element broaderTr = doc.select(cssQuery).first();
            Element broaderTr = doc.selectXpath(xpath).first();
            if (broaderTr != null) {
                Element label = broaderTr.select("rdfs:label").first();
                assert label != null;
                System.out.println("text of dereferenced " + label.text());
                return label.text();
            } else {
                System.out.println("No <tr> element with id='broader' found.");
            }
        } catch (NullPointerException ex){
            throw ex;
        } catch (IOException e) {
            System.out.println("IOException in Dereferencer.");
        }
        throw new NullPointerException();
    }

    private String skosDereference() {
        try {
            // dereference for skos
            // Fetch the HTML page
            Document doc = Jsoup.connect(this.url).get();
            IRI iri = iri(this.url);

            System.out.println("iri=" + this.url);
            String cssQuery = "#" + iri.getLocalName();
            // Find the <tr> element with id="broader"
            System.out.println("cssQuery " + cssQuery);
            Element broaderTr = doc.select(cssQuery).first().parent().parent();
            //broaderTr.parent().parent();
            System.out.println("text on broaderTr " + broaderTr.text());
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
                    System.out.println("End of  while (nextTr != null)");
                }
                System.out.println("End of if (broaderTr != null) ");
            } else {
                System.out.println("No <tr> element with id='broader' found.");
            }
        } catch (NullPointerException ex){
            throw ex;
        } catch (IOException e) {
            System.out.println("IOException in Dereferencer.");
            //e.printStackTrace();
        }
        throw new NullPointerException();
    }

    private String dcDereference() {
        try {
            // dereference for dcterms for example http://purl.org/dc/terms/title
            // Fetch the HTML page
            Document doc = Jsoup.connect(this.url).get();
            IRI iri = iri(this.url);

            System.out.println("iri=" + this.url);
            String xpath = "//td[text()='"+iri+"']";
            // Find the <tr> element with id="broader"

            Element tr = doc.selectXpath(xpath).first().parent();//.parent().parent();
            //broaderTr.parent().parent();
            System.out.println("text on broaderTr " + tr.text());
            if (tr != null) {
                // Find the next <tr> element relative to the <tr> with id="broader"
                Element nextTr = tr.nextElementSibling();

                while (nextTr != null) {
                    // Find <td> elements within the sibling <tr>
                    Elements tds = nextTr.select("td");
                    for (Element td : tds) {
                        // Check if any <td> contains the text "Label:"
                        if (td.text().contains("Label")) {
                            Element titleElement = td.nextElementSibling();
                            //System.out.println("Found <tr> sibling with <td> containing 'Label:': " + nextTr);

                            assert titleElement != null;
                            System.out.println("Found td with Label name: " + titleElement.text());
                            return titleElement.text();
                        }
                    }
                    nextTr = nextTr.nextElementSibling();
                    System.out.println("End of  while (nextTr != null)");
                }
                System.out.println("End of if (broaderTr != null) ");
            } else {
                System.out.println("No <tr> element with id='broader' found.");
            }
        } catch (NullPointerException ex){
            throw ex;
        } catch (IOException e) {
            System.out.println("IOException in Dereferencer.");
            //e.printStackTrace();
        }
        throw new NullPointerException();
    }

    private String foafDereference() {
        try {
            // dereference for foaf prefix
            // Fetch the HTML page
            Document doc = Jsoup.connect(this.url).get();
            IRI iri = iri(this.url);

            System.out.println("iri=" + this.url);
            String cssQuery = "div[about=\"" + iri + "\"]";
            // Find the <tr> element with id="broader"
            System.out.println("cssQuery " + cssQuery);
            Element foundElement = doc.select(cssQuery).first();//.parent().parent();
            //broaderTr.parent().parent();
            System.out.println("text on broaderTr " + foundElement.text());
            if (foundElement != null) {
                // Find the next <tr> element relative to the <tr> with id="broader"
                Element elementEm = foundElement.selectFirst("em");


                assert elementEm != null;
                System.out.println("element em text = " + elementEm.text());

                return elementEm.text();
            } else {
                System.out.println("No <tr> element with id='broader' found.");
            }
        } catch (NullPointerException ex){
            throw ex;
        } catch (IOException e) {
            System.out.println("IOException in Dereferencer.");
            //e.printStackTrace();
        }
        throw new NullPointerException();
    }
}
