package com.ort.skoton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SKOS {

  static final String HAS_TOP_CONCEPT = "skos:hasTopConcept";
  static final String RELATED = "skos:related";
  static final String NARROWER = "skos:narrower";
  static final String PREF_LABEL = "skos:prefLabel";
  static final String ALT_LABEL = "skos:altLabel";
  static final String CONCEPT = "skos:Concept";
  static final String BROADER = "skos:broader";

  private final String filePath;
  private Document document;
  private SKOSConceptScheme conceptScheme;
  private List<SKOSConcept> concepts;

  public SKOS(String filePath) throws ParserConfigurationException, SAXException, IOException {
    this.filePath = filePath;
    concepts = new ArrayList<>();
    process();
  }

  public SKOSConceptScheme getConceptScheme() {
    return conceptScheme;
  }

  public List<SKOSConcept> getConcepts() {
    return concepts;
  }

  public SKOSConcept getConcept(String id) {
    return concepts.stream()
            .filter(x -> x.getId().equals(id))
            .findFirst().orElse(null);
  }

  private void process() throws SAXException, ParserConfigurationException, IOException {
    File file = new File(filePath);
    DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    document = dBuilder.parse(file);

    Node scheme = document.getChildNodes().item(1).getChildNodes().item(1);
    conceptScheme = new SKOSConceptScheme(scheme);

    NodeList nodes = document.getChildNodes().item(1).getChildNodes();

    System.out.println("SKOS: Start processing...");
    for (int count = 0; count < nodes.getLength(); count++) {
      Node concept = nodes.item(count);
      if (concept.getNodeName().equals(SKOS.CONCEPT)) {
        concepts.add(new SKOSConcept(concept));
      }
    }
    System.out.println("SKOS: Finished processing");
  }
}
