package com.ort.skoton;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class Ontology {

  private final IRI IOR;
  private final OWLOntologyManager manager;
  private final OWLOntology ontology;
  private final OWLDataFactory dataFactory;

  public Ontology(String filePath) throws OWLOntologyCreationException {
    manager = OWLManager.createOWLOntologyManager();

    // Open the existing ontology
    File file = new File(filePath);
    ontology = manager.loadOntologyFromOntologyDocument(file);
    dataFactory = ontology.getOWLOntologyManager().getOWLDataFactory();

    IOR = ontology.getOntologyID().getOntologyIRI().get();
  }

  public void createFromSKOS(SKOS skos) {
    try {
      SKOSConceptScheme scheme = skos.getConceptScheme();
      List<String> topConcepts = scheme.getTopConcepts();

      OWLObjectProperty property = createObjectProperty("#relatedClass");
      Set<OWLAxiom> relatedClassAxioms = new HashSet<>();
      relatedClassAxioms.add(dataFactory.getOWLSymmetricObjectPropertyAxiom(property));
      manager.addAxioms(ontology, relatedClassAxioms.stream());

      createAnnotationProperty("#altLabel");

      System.out.println("Ontology: Start processing concepts...");
      processByLevel(skos, null, topConcepts);

      System.out.println("Ontology: Saving ontology...");
      manager.saveOntology(ontology);
    } catch (OWLOntologyStorageException ex) {
      Logger.getLogger(Ontology.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void processByLevel(SKOS skos, OWLClass broader, List<String> topConcepts) {
    for (int i = 0; i < topConcepts.size(); i++) {
      String id = topConcepts.get(i);
      System.out.println("Ontology: Processing " + id);
      SKOSConcept concept = skos.getConcept(id);
      if (concept != null) {
        OWLClass owlClass;
        if (broader == null) {
          owlClass = createClass(concept.getId());
        } else {
          owlClass = createSubClass(broader, concept.getId());
        }
        List<String> relatedConcepts = concept.getRelatedConcepts();
        // TODO: create related concepts

        addAnnotationsToClass(owlClass, dataFactory.getRDFSLabel(), concept.getPrefLabels());
        addAnnotationsToClass(owlClass, dataFactory.getOWLAnnotationProperty(IOR + "#altLabel"), concept.getAltLabels());

        processByLevel(skos, owlClass, concept.getNarrowerConcepts());

      } else {
        System.out.println("ERROR: Couldn't find concept " + id);
      }
    }
  }
  
  private OWLClass getClass(String name){
    return dataFactory.getOWLClass(IOR + name);
  }

  private OWLClass createClass(String name) {
    OWLClass owlClass = dataFactory.getOWLClass(IOR + name);
    OWLAxiom declare = dataFactory.getOWLDeclarationAxiom(owlClass);
    manager.addAxiom(ontology, declare);

    return owlClass;
  }

  private OWLClass createSubClass(OWLClass owlClass, String subclassName) {
    OWLClass subclass = dataFactory.getOWLClass(IOR + subclassName);
    OWLAxiom axiom = dataFactory.getOWLSubClassOfAxiom(subclass, owlClass);
    manager.addAxiom(ontology, axiom);

    return subclass;
  }

  private OWLObjectProperty createObjectProperty(String name) {
    OWLObjectProperty objectProperty = dataFactory.getOWLObjectProperty(IOR + name);
    OWLAxiom declare = dataFactory.getOWLDeclarationAxiom(objectProperty);
    manager.addAxiom(ontology, declare);

    return objectProperty;
  }

  private OWLAnnotationProperty createAnnotationProperty(String name) {
    OWLAnnotationProperty annotationProperty = dataFactory.getOWLAnnotationProperty(IOR + name);
    OWLAxiom declare = dataFactory.getOWLDeclarationAxiom(annotationProperty);
    manager.addAxiom(ontology, declare);

    return annotationProperty;
  }

  private void addAnnotationsToClass(OWLClass owlClass, OWLAnnotationProperty annotationProperty, List<Label> labels) {
    for (int n = 0; n < labels.size(); n++) {
      Label label = labels.get(n);
      addAnnotationToClass(owlClass, annotationProperty, label.getValue(), label.getLanguage());
    }
  }

  private void addAnnotationToClass(OWLClass owlClass, OWLAnnotationProperty annotationProperty, String value, String language) {
    OWLAnnotation annotation = dataFactory.getOWLAnnotation(annotationProperty,
            dataFactory.getOWLLiteral(value, language));
    OWLAxiom axiom = dataFactory.getOWLAnnotationAssertionAxiom(owlClass.getIRI(), annotation);
    manager.addAxiom(ontology, axiom);
  }
}
