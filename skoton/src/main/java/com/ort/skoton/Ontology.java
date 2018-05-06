package com.ort.skoton;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.parameters.Imports;

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

      createAnnotationProperty("#altLabel");

      System.out.println("Ontology: Start processing concepts...");
      processByLevel(skos, null, topConcepts);

      System.out.println("Ontology: Saving ontology...");

      OWLDifferentIndividualsAxiom axiom = dataFactory.getOWLDifferentIndividualsAxiom(ontology.getIndividualsInSignature(Imports.EXCLUDED));
      manager.addAxiom(ontology, axiom);

      manager.saveOntology(ontology);
    } catch (OWLOntologyStorageException ex) {
      Logger.getLogger(Ontology.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void processByLevel(SKOS skos, OWLIndividual broader, List<String> topConcepts) {
    for (int i = 0; i < topConcepts.size(); i++) {
      String id = topConcepts.get(i);
      System.out.println("Ontology: Processing " + id);
      SKOSConcept concept = skos.getConcept(id);
      if (concept != null) {
        OWLClass topic = getClass("#Topic");
        OWLIndividual individual = createIndividual(topic, IOR + concept.getId());
        if (broader != null) {
          addObjectPropertyToIndividual("#subtopicOf", individual, broader);
        }

        List<String> relatedConcepts = concept.getRelatedConcepts();
        for (int n = 0; n < relatedConcepts.size(); n++) {
          if (ontology.containsIndividualInSignature(IRI.create(IOR + relatedConcepts.get(n)))) {
            addObjectPropertyToIndividual("#relatedTo", individual, dataFactory.getOWLNamedIndividual(IOR + relatedConcepts.get(n)));
          }
        }

        addAnnotationsToIndividual(individual, dataFactory.getRDFSLabel(), concept.getPrefLabels());
        addAnnotationsToIndividual(individual, dataFactory.getOWLAnnotationProperty(IOR + "#altLabel"), concept.getAltLabels());

        processByLevel(skos, individual, concept.getNarrowerConcepts());

      } else {
        System.out.println("ERROR: Couldn't find concept " + id);
      }
    }
  }

  private OWLClass getClass(String name) {
    return dataFactory.getOWLClass(IOR + name);
  }

  private OWLIndividual createIndividual(OWLClass owlClass, String name) {
    OWLIndividual individual = dataFactory.getOWLNamedIndividual(IOR + name);
    OWLAxiom axiom = dataFactory.getOWLClassAssertionAxiom(owlClass, individual);
    manager.addAxiom(ontology, axiom);
    return individual;
  }

  private OWLAnnotationProperty createAnnotationProperty(String name) {
    OWLAnnotationProperty annotationProperty = dataFactory.getOWLAnnotationProperty(IOR + name);
    OWLAxiom declare = dataFactory.getOWLDeclarationAxiom(annotationProperty);
    manager.addAxiom(ontology, declare);

    return annotationProperty;
  }

  private void addAnnotationsToIndividual(OWLIndividual individual, OWLAnnotationProperty annotationProperty, List<Label> labels) {
    for (int n = 0; n < labels.size(); n++) {
      Label label = labels.get(n);
      addAnnotationToIndividual(individual, annotationProperty, label.getValue(), label.getLanguage());
    }
  }

  private void addAnnotationToIndividual(OWLIndividual individual, OWLAnnotationProperty annotationProperty, String value, String language) {
    OWLAnnotation annotation = dataFactory.getOWLAnnotation(annotationProperty,
            dataFactory.getOWLLiteral(value, language));
    OWLAxiom axiom = dataFactory.getOWLAnnotationAssertionAxiom(individual.asOWLNamedIndividual().getIRI(), annotation);
    manager.addAxiom(ontology, axiom);
  }

  private void addObjectPropertyToIndividual(String objectProperty, OWLIndividual to, OWLIndividual from) {
    OWLObjectProperty property = dataFactory.getOWLObjectProperty(IOR + objectProperty);
    OWLObjectPropertyAssertionAxiom axiom = dataFactory.getOWLObjectPropertyAssertionAxiom(property, to, from);
    manager.addAxiom(ontology, axiom);
  }
}
