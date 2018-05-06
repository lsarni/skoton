package com.ort.skoton;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SKOSConcept {

  private final Node node;
  private String id;
  private final List<Label> prefLabels;
  private final List<Label> altLabels;
  private final List<String> narrower;
  private final List<String> related;

  public SKOSConcept(Node node) {
    this.node = node;
    this.prefLabels = new ArrayList<>();
    this.altLabels = new ArrayList<>();
    this.narrower = new ArrayList<>();
    this.related = new ArrayList<>();

    process();
  }

  public List<Label> getPrefLabels() {
    return this.prefLabels;
  }

  public List<Label> getAltLabels() {
    return this.altLabels;
  }

  public List<String> getNarrowerConcepts() {
    return this.narrower;
  }

  public List<String> getRelatedConcepts() {
    return this.related;
  }

  public String getId() {
    return this.id;
  }

  private void process() {
    id = node.getAttributes().getNamedItem(RDF.ABOUT).getNodeValue();

    System.out.println("SKOSConcept: Start processing " + id + "...");

    NodeList childNodes = node.getChildNodes();
    for (int n = 0; n < childNodes.getLength(); n++) {
      Node child = childNodes.item(n);
      switch (child.getNodeName()) {
        case SKOS.PREF_LABEL:
          prefLabels.add(createLabel(child));
          break;
        case SKOS.ALT_LABEL:
          altLabels.add(createLabel(child));
          break;
        case SKOS.NARROWER:
          narrower.add(getRDFResource(child));
          break;
        case SKOS.RELATED:
          related.add(getRDFResource(child));
          break;
        default:
          break;
      }
    }
    System.out.println("SKOSConcept: Finished processing " + id);
    System.out.println("  " + toString());
  }

  private Label createLabel(Node node) {
    String language = node.getAttributes().getNamedItem(XML.LANGUAGE).getNodeValue();
    String value = node.getTextContent();
    return new Label(language, value);
  }

  private String getRDFResource(Node node) {
    return node.getAttributes().getNamedItem(RDF.RESOURCE).getNodeValue();
  }

  @Override
  public String toString() {
    return "prefLabels=" + prefLabels.size() + ", altLabels=" + altLabels.size() + ", narrower=" + narrower.size() + ", related=" + related.size();
  }  
}
