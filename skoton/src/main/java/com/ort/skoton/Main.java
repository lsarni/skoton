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
      
      System.out.print("Enter the full path of the ontology file (.owl):");
      String ontologyPath = bufferedReader.readLine();

      System.out.print("Enter the IOR of the ontology file:");
      String ior = bufferedReader.readLine();

      System.out.println("Enter the full path of the SKOS taxonomy:");
      String skosFile = bufferedReader.readLine();

      Ontology ontology = new Ontology(ontologyPath, ior);
      SKOS skos = new SKOS(skosFile);

      ontology.createFromSKOS(skos);
    } catch (Exception ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
