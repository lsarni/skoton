package com.ort.skoton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
import org.semanticweb.owlapi.util.AutoIRIMapper;

public class Ontology {

  private final IRI IOR;
  private final IRI ontologyIOR;
  private final OWLOntologyManager manager;
  private final OWLOntology ontology;
  private final OWLDataFactory dataFactory;
  Set<String> used;

  public Ontology(String filePath) throws OWLOntologyCreationException {
    manager = OWLManager.createOWLOntologyManager();

    File folder = new File("C:\\Users\\lulas\\Documents\\Curricula Ontology");
    AutoIRIMapper mapper = new AutoIRIMapper(folder, true);
    manager.getIRIMappers().add(mapper);

    // Open the existing ontology
    File file = new File(filePath);
    ontology = manager.loadOntologyFromOntologyDocument(file);
    dataFactory = ontology.getOWLOntologyManager().getOWLDataFactory();

    ontologyIOR = IRI.create("http://www.semanticweb.org/lsarni/ontologies/acm-ccs");
    IOR = ontology.getOntologyID().getOntologyIRI().get();

    used = new HashSet<>();
  }

  public void createFromSKOS(SKOS skos) {
    createFromSKOS(skos, false);
  }

  public void createFromSKOS(SKOS skos, boolean prune) {
    try {
      SKOSConceptScheme scheme = skos.getConceptScheme();

      System.out.println("Ontology: Start processing concepts...");
      if (prune) {
        loadUsedTopics();
        processByConcept(skos);
      } else {
        List<String> topConcepts = scheme.getTopConcepts();
        processByLevel(skos, null, topConcepts);
      }

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
        OWLIndividual individual;
        if (ontology.containsIndividualInSignature(IRI.create(IOR + concept.getId()))) {
            individual = dataFactory.getOWLNamedIndividual(IOR + concept.getId());
          } else {
            individual = createIndividual(topic, IOR + concept.getId());
          }
        
        if (broader != null) {
          addObjectPropertyToIndividual("#subtopicOf", individual, broader);
        }

        List<String> relatedConcepts = concept.getRelatedConcepts();
        for (int n = 0; n < relatedConcepts.size(); n++) {
          OWLIndividual related;
          if (ontology.containsIndividualInSignature(IRI.create(IOR + relatedConcepts.get(n)))) {
            related = dataFactory.getOWLNamedIndividual(IOR + relatedConcepts.get(n));
          } else {
            related = createIndividual(topic, IOR + relatedConcepts.get(n));
          }
          addObjectPropertyToIndividual("#relatedTo", individual, related);
        }

        addAnnotationsToIndividual(individual, dataFactory.getRDFSLabel(), concept.getPrefLabels());
        addAnnotationsToIndividual(individual, dataFactory.getOWLAnnotationProperty(ontologyIOR + "#altLabel"), concept.getAltLabels());

        processByLevel(skos, individual, concept.getNarrowerConcepts());

      } else {
        System.out.println("ERROR: Couldn't find concept " + id);
      }
    }
  }

  private OWLClass getClass(String name) {
    return dataFactory.getOWLClass(ontologyIOR + name);
  }

  private OWLIndividual createIndividual(OWLClass owlClass, String name) {
    OWLIndividual individual = dataFactory.getOWLNamedIndividual(IOR + name);
    OWLAxiom axiom = dataFactory.getOWLClassAssertionAxiom(owlClass, individual);
    manager.addAxiom(ontology, axiom);
    return individual;
  }

  private OWLAnnotationProperty createAnnotationProperty(String name) {
    OWLAnnotationProperty annotationProperty = dataFactory.getOWLAnnotationProperty(ontologyIOR + name);
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
    OWLObjectProperty property = dataFactory.getOWLObjectProperty(ontologyIOR + objectProperty);
    OWLObjectPropertyAssertionAxiom axiom = dataFactory.getOWLObjectPropertyAssertionAxiom(property, to, from);
    manager.addAxiom(ontology, axiom);
  }

  private void loadUsedTopics() {
    try {
      Workbook workbook = WorkbookFactory.create(new File("C:\\Users\\lulas\\Desktop\\Proyecto\\Datos\\Clasificacion cursos\\Listado de cursos y temas.xlsx"));
      Sheet sheet = workbook.getSheetAt(0);
      for (int i = 0; i < sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        used.add(row.getCell(1).getStringCellValue());
      }
    } catch (Exception ex) {
      Logger.getLogger(Ontology.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void processByConcept(SKOS skos) {
    OWLClass topic = getClass("#Topic");

    for (String id : used) {
      System.out.println("Processing " + id);
      SKOSConcept concept = skos.getConcept(id);

      OWLIndividual individual = processConcept(skos, id);

      List<String> relatedConcepts = concept.getRelatedConcepts();
      for (int n = 0; n < relatedConcepts.size(); n++) {
        if (used.contains(relatedConcepts.get(n))) {
          OWLIndividual related;
          if (!ontology.containsIndividualInSignature(IRI.create(IOR + relatedConcepts.get(n)))) {
            related = processConcept(skos, relatedConcepts.get(n));
          } else {
            related = dataFactory.getOWLNamedIndividual(IOR + relatedConcepts.get(n));
          }
          addObjectPropertyToIndividual("#relatedTo", individual, related);
        }
      }
    }
  }

  private OWLIndividual processConcept(SKOS skos, String id) {
    SKOSConcept concept = skos.getConcept(id);

    OWLClass topic = getClass("#Topic");

    if (!ontology.containsIndividualInSignature(IRI.create(IOR + concept.getId()))) {
      OWLIndividual individual = createIndividual(topic, IOR + concept.getId());

      addAnnotationsToIndividual(individual, dataFactory.getRDFSLabel(), concept.getPrefLabels());
      addAnnotationsToIndividual(individual, dataFactory.getOWLAnnotationProperty(ontologyIOR + "#altLabel"), concept.getAltLabels());

      List<String> broaderConcepts = concept.getBroaderConcepts();
      for (int n = 0; n < broaderConcepts.size(); n++) {
        OWLIndividual broader;
        if (!ontology.containsIndividualInSignature(IRI.create(IOR + broaderConcepts.get(n)))) {
          broader = processConcept(skos, broaderConcepts.get(n));
        } else {
          broader = dataFactory.getOWLNamedIndividual(IOR + broaderConcepts.get(n));
        }
        addObjectPropertyToIndividual("#subtopicOf", individual, broader);
      }
      return individual;
    }
    return null;
  }
}
