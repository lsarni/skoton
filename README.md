# skoton (beta)
Java Application to re-engineering a SKOS taxonomy into a lightweight ontology.

## What it does
The process of re-engineering the taxonomy is based in the [pattern for re-engineering a term-based thesaurus, which follows the recordbased data model, into an ontology schema](http://ontologydesignpatterns.org/wiki/Submissions:Pattern_for_re-engineering_a_term-based_thesaurus%2C_which_follows_the_recordbased_data_model%2C_into_an_ontology_schema) developed by Boris Villazón-Terrazas, Mari Carmen Suárez-Figueroa, and Asunción
Gómez-Pérez (better explained in [their paper](http://ceur-ws.org/Vol-516/pat03.pdf)).

To do so we start from a XML file that follows the SKOS (Simple Knowledge Organization System) recommendations and carry on the following steps:
1. Create an `AnnotationProperty` called `altLabel` to store the values of `skos:altLabel`.
2. Create a `OWLClass` for each `Concept`, named using the value of the `rdf:about` attribute.
3. Add a `label` to the class for each `skos:prefLabel`, using the given value and language (`xml:lang`).
4. Add a `altLabel` to the class for each `skos:altLabel`, using the given value and language.
5. Set all the `skos:narrower` concepts as `subClassOf` the given class.

## Limitations
Since this is a beta version there are some limitations:
* The first node of the .xml file (inside the `rdf:RDF` node) must be the `skos:ConceptSchema`. There can be more than one `skos:ConceptSchema` but the rest will be ignored.
* The `skos:ConceptSchema` must declare all the top nodes of the taxonomy using this format `<skos:hasTopConcept rdf:resource="#someConcept"/>`. Only the nodes connected to this ones using the `skos:narrower` relationship will end up on the ontology. This means that if the `ConceptSchema` has no `skos:hasTopConcept` properties the resulting ontology will have no classes.
* For now the `skos:related` property won't be represented on the ontology (although it is part of the described pattern).

## How to use
After downloading and building the project just run it. 
It will ask for 3 strings (press enter after each one):
* Full file path of the ontology (.owl)
* IRI of the ontology (this is the value of the `rdf:about` of the `owl:Ontology`)
* Full file path of the taxonomy (.xml)
After that it will start processing the taxonomy and finally save the ontology.

### Example
This project was developed for (and tested with) the [2012 ACM Computing Classification System](https://www.acm.org/publications/class-2012), the SKOS file can be obtained [here](http://dl.acm.org/ft_gateway.cfm?id=2371137&ftid=1290922&dwn=1).
In the [example folder](example) I will included the target ontology and a reduced version of the classification that was used for testing (just keeping 3 concepts).

Just run the project and input the strings as required:
```
Enter the full path of the ontology file (.owl):C:\Users\lulas\Documents\skoton\example\acm-css.owl
Enter the IOR of the ontology file:http://www.semanticweb.org/lsarni/ontologies/acm-ccs
Enter the full path of the SKOS taxonomy:
C:\Users\lulas\Documents\skoton\example\acm-css-lite.xml
SKOSConceptScheme: Start processing...
SKOSConceptScheme: Finished processing
  topConcepts=1
SKOS: Start processing...
SKOSConcept: Start processing #10002944...
SKOSConcept: Finished processing #10002944
  prefLabels=1, altLabels=1, narrower=2, related=0
SKOSConcept: Start processing #10011122...
SKOSConcept: Finished processing #10011122
  prefLabels=1, altLabels=0, narrower=0, related=0
SKOSConcept: Start processing #10011123...
SKOSConcept: Finished processing #10011123
  prefLabels=1, altLabels=2, narrower=0, related=0
SKOS: Finished processing
Ontology: Start processing concepts...
Ontology: Processing #10002944
Ontology: Processing #10011122
Ontology: Processing #10011123
Ontology: Saving ontology...
```

The result of running the application should be the same ontology as the one on the [acm-css-result.owl](example/acm-css-result.owl) file.


