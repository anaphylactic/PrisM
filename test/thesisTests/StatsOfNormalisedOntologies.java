package thesisTests;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.metrics.DLExpressivity;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;


public class StatsOfNormalisedOntologies {

	
	static OWLOntology root_ontology;
	static String root_ontology_iri;
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static String iri_onto;
	static OWLOntology normalisedOntology;
	static Set<OWLAxiom> normalisedAxioms;
	static boolean removeABox = false;
	
	
	public static void main(String[] args){
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00001.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00004.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00024.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00026.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00029.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00032.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00347.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00350.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00351.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00354.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00463.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00471.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00477.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00512.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00545.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00774.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00775.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00778.owl";
		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00786.owl";
		
		
		loadOntology();
		
		int nDisjunctive = 0;
		int nExistential = 0;
		int nAxWithIndividuals = 0;
		for (OWLAxiom ax : normalisedOntology.getAxioms()){
			if (disjunctive(ax)) nDisjunctive++;
			if (existential(ax)) nExistential++;
			if (!ax.getIndividualsInSignature().isEmpty()) nAxWithIndividuals++;
		}
		
		DLExpressivity expressivity = new DLExpressivity(manager);
		expressivity.setOntology(normalisedOntology);
		
		System.out.println("nDisjunctive: " + nDisjunctive);
		System.out.println("nExistential: " + nExistential);
		System.out.println("nAxWithIndividuals: " + nAxWithIndividuals);
		System.out.println("expressivity: " + expressivity.getValue());
		System.out.println("total number of rules: " + normalisedOntology.getAxiomCount());
		System.out.println("total number of predicates: " + getClassesAndObjectsInSignature(normalisedOntology.getSignature()).size());
		
		
		//DataPropertyAssertions are ignored (both positive and negative)
		//Also DataPropertyRangeAxioms are ignored
		//But DataPropertySomeValuesFrom and DataPropertyHasValue is not 
	}
	
	private static boolean disjunctive(OWLAxiom ax){
		if (ax instanceof OWLSubClassOfAxiom){
			return ((OWLSubClassOfAxiom) ax).getSuperClass() instanceof OWLObjectUnionOf;
		}
		else if (!(ax instanceof OWLSubObjectPropertyOfAxiom) && 
				!(ax instanceof OWLTransitiveObjectPropertyAxiom) && 
				!(ax instanceof OWLFunctionalObjectPropertyAxiom) && 
				!(ax instanceof OWLInverseFunctionalObjectPropertyAxiom) &&
				!(ax instanceof OWLFunctionalDataPropertyAxiom)){
//			System.out.println(ax.toString() + " disjunctive?");
		}
		return false;
	}
	
	private static boolean existential(OWLAxiom ax){
		if (ax instanceof OWLSubClassOfAxiom){
			OWLClassExpression superClass = ((OWLSubClassOfAxiom) ax).getSuperClass();
			if (superClass instanceof OWLObjectSomeValuesFrom || 
					superClass instanceof OWLDataSomeValuesFrom ||
					superClass instanceof OWLObjectMinCardinality || 
					superClass instanceof OWLDataMinCardinality)
				return true;
			else {
				if (superClass instanceof OWLObjectUnionOf){
					for (OWLClassExpression e : ((OWLObjectUnionOf) superClass).getOperands())
						if (e instanceof OWLObjectSomeValuesFrom || 
								e instanceof OWLDataSomeValuesFrom ||
								e instanceof OWLObjectMinCardinality || 
								e instanceof OWLDataMinCardinality)
							return true;
				}
				else if (superClass instanceof OWLObjectIntersectionOf){
					for (OWLClassExpression e : ((OWLObjectIntersectionOf) superClass).getOperands())
						if (e instanceof OWLObjectSomeValuesFrom || 
								e instanceof OWLDataSomeValuesFrom ||
								e instanceof OWLObjectMinCardinality || 
								e instanceof OWLDataMinCardinality)
							return true;
				}
			}
		}
		else if (!(ax instanceof OWLSubObjectPropertyOfAxiom) && 
				!(ax instanceof OWLTransitiveObjectPropertyAxiom) && 
				!(ax instanceof OWLFunctionalObjectPropertyAxiom) && 
				!(ax instanceof OWLInverseFunctionalObjectPropertyAxiom) &&
				!(ax instanceof OWLFunctionalDataPropertyAxiom)){
//			System.out.println(ax.toString() + " existential?");
		}
		return false;
	}
	
//	private static boolean equality(OWLAxiom ax){
//		if (ax instanceof OWLSubClassOfAxiom){
//			return (new EqualityClass().containsEquality(((OWLSubClassOfAxiom) ax).getSubClass()) ||
//					new EqualityClass().containsEquality(((OWLSubClassOfAxiom) ax).getSuperClass()));
//		}
//		else if (ax instanceof OWLFunctionalObjectPropertyAxiom || 
//				ax instanceof OWLInverseFunctionalObjectPropertyAxiom || 
//				ax instanceof OWLFunctionalDataPropertyAxiom) 
//			return true;
//		else if (!(ax instanceof OWLSubObjectPropertyOfAxiom) && !(ax instanceof OWLTransitiveObjectPropertyAxiom)){
//			System.out.println(ax.toString() + " equality?");
//		}
//		return false;
//	}
	
	
	
	private static void loadOntology(){
		try {
			root_ontology = manager.loadOntology(IRI.create(iri_onto));
			
			DLExpressivity expressivity = new DLExpressivity(manager);
			expressivity.setOntology(root_ontology);
			System.out.println("expressivity before normalisation: " + expressivity.getValue());
			
			root_ontology_iri = manager.getOntologyDocumentIRI(root_ontology).toString().replace("<","").replace(">", "");//getOWLOntologyManager().getOntologyDocumentIRI(root_ontology).toString();

			if (removeABox){
				Set<OWLAxiom> rtBox = new HashSet<OWLAxiom>(root_ontology.getTBoxAxioms(true));
				rtBox.addAll(root_ontology.getRBoxAxioms(true));
				manager.removeOntology(root_ontology);
				root_ontology = manager.createOntology(rtBox, IRI.create(root_ontology_iri.replace(".owl", "-RTBox.owl")));	
			}

//			////
//			for (OWLAxiom ax : root_ontology.getABoxAxioms(true))
//				System.out.println(ax.toString());
//			////
			
			normalisedAxioms = NormaliserViaClauses.getNormalisedAxioms(root_ontology);
			normalisedOntology = manager.createOntology(normalisedAxioms, IRI.create(root_ontology_iri.replace(".owl", "_normalised_RTBox.owl")));

			System.out.println("\nLoaded ontology: " + iri_onto);

		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			System.exit(0);
		}

	}
	
	protected static Set<OWLEntity> getClassesAndObjectsInSignature(Set<OWLEntity> sig) {
		Set<OWLEntity> filteredSig = new HashSet<OWLEntity>();
		for (OWLEntity e : sig)
			if (e instanceof OWLObjectProperty ||
					(e instanceof OWLClass && !(((OWLClass) e).isOWLThing() || ((OWLClass) e).isOWLNothing())))
				filteredSig.add(e);
		return filteredSig;
	}

}
