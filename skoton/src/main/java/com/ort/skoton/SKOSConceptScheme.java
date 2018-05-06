package com.ort.skoton;

import java.util.ArrayList;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SKOSConceptScheme {

  private final Node node;
  private NodeList childNodes;
  private ArrayList<String> topConcepts;

  public SKOSConceptScheme(Node node) {
    this.node = node;
    this.topConcepts = new ArrayList<>();
    process();
  }

  public ArrayList<String> getTopConcepts() {
    return topConcepts;
  }

  private void process() {
    System.out.println("SKOSConceptScheme: Start processing...");
    NodeList conceptSchemeProperties = node.getChildNodes();
    for (int n = 0; n < conceptSchemeProperties.getLength(); n++) {
      Node property = conceptSchemeProperties.item(n);
      if (property.getNodeName().equals(SKOS.HAS_TOP_CONCEPT)) {
        topConcepts.add(property.getAttributes().getNamedItem(RDF.RESOURCE).getNodeValue());
      }
    }
    System.out.println("SKOSConceptScheme: Finished processing");
    System.out.println("  " + toString());
  }

  @Override
  public String toString() {
    return "topConcepts=" + topConcepts.size();
  }
  
  
}
