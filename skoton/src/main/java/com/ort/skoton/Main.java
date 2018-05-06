package com.ort.skoton;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

  public static void main(String[] args) {
    try {
      InputStreamReader streamReader = new InputStreamReader(System.in);
      BufferedReader bufferedReader = new BufferedReader(streamReader);
      
      System.out.println("Enter the full path of the ontology file (.owl):");
      String ontologyPath = "C:\\Users\\lulas\\Documents\\Curricula Ontology\\acm-ccs-full.owl";
      
      System.out.println("Enter the full path of the SKOS taxonomy:");
      String skosFile = "C:\\Users\\lulas\\Documents\\Curricula Ontology\\ACMComputingClassificationSystemSKOSTaxonomy.xml";

      Ontology ontology = new Ontology(ontologyPath);
      SKOS skos = new SKOS(skosFile);

      ontology.createFromSKOS(skos);
    } catch (Exception ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
