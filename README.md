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
