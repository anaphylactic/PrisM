package thesisTests;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.structural.OWLClausification;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;

import uk.ac.ox.cs.pagoda.approx.RLPlusOntology;
import uk.ac.ox.cs.pagoda.constraints.NullaryBottom;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.prism.RedundantAxiomRepairer;


@Deprecated
public class NormaliserViaClauses {

	
	public static Set<OWLAxiom> getNormalisedAxioms(OWLOntology o){
		
		RLPlusOntology owlOntology = new RLPlusOntology(); 
		owlOntology.load(o, new NullaryBottom());
		owlOntology.simplify();
		
		Set<OWLAxiom> ret = clausifyAndTurnToAxioms(owlOntology.getTBox());
		
		String aboxOWLFile = owlOntology.getABoxPath();
		ret.addAll(OWLHelper.loadOntology(aboxOWLFile).getAxioms());
		
		return ret;
	}
	
	protected static Set<OWLAxiom> clausifyAndTurnToAxioms(OWLOntology ontology){ //(from clausify() in class Program in Pagoda)
		OWLClausification clausifier = new OWLClausification(new Configuration());
		OWLOntology filteredOntology = null;
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		try {
			filteredOntology = manager.createOntology();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		Set<OWLAxiom> ret = new HashSet<OWLAxiom>();
		for (OWLOntology onto: ontology.getImportsClosure())
			for (OWLAxiom axiom: onto.getAxioms()) {
				if (axiom instanceof OWLTransitiveObjectPropertyAxiom)
					ret.add(axiom);
				else if (axiom instanceof OWLSubPropertyChainOfAxiom)
					ret.add(axiom);
				else if (axiom instanceof OWLSameIndividualAxiom)
					ret.add(axiom);
				else if (axiom instanceof OWLDifferentIndividualsAxiom)
					ret.add(axiom);
				else if (axiom instanceof OWLNegativeObjectPropertyAssertionAxiom)
					ret.add(axiom);
				else if (axiom instanceof OWLNegativeDataPropertyAssertionAxiom) {
//					System.out.println("IGNORING: " + axiom.toString());
				}
				else if (axiom instanceof OWLDataPropertyAssertionAxiom) {
//					System.out.println("IGNORING: " + axiom.toString());
				}
				else if (axiom instanceof OWLDataPropertyRangeAxiom) {
//					System.out.println("IGNORING: " + axiom.toString());
				} 
				else {
					manager.addAxiom(filteredOntology, axiom);
				}
			}
		
		DLOntology dlOntology = (DLOntology)clausifier.preprocessAndClausify(filteredOntology, null)[1];
		clausifier = null;	
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		for (DLClause clause : dlOntology.getDLClauses()){
			ret.add(RedundantAxiomRepairer.repair(OWLHelper.getOWLAxiom(ontology, clause), factory));
		}
		
		//these axioms would be added to the ABox in the class Program from PAGOdA
		OWLAxiom axiom;
		for (Atom atom : dlOntology.getPositiveFacts()) {
			if ((axiom = OWLHelper.getABoxAssertion(manager.getOWLDataFactory(), atom)) != null)
				ret.add(axiom);
		}
				
		return ret;
	}
	
//	public static void main(String[] args) throws OWLOntologyCreationException, JRDFStoreException {
//		
//		String iri = "file://dummy#";
//		OWLDataFactory factory = new OWLDataFactoryImpl();
//		
//		OWLClass a = factory.getOWLClass(IRI.create(iri+"A"));
//		OWLClass b = factory.getOWLClass(IRI.create(iri+"B"));
//		OWLClass c = factory.getOWLClass(IRI.create(iri+"C"));
//		OWLClass d = factory.getOWLClass(IRI.create(iri+"D"));
//		OWLClass e = factory.getOWLClass(IRI.create(iri+"E"));
//		OWLClass f = factory.getOWLClass(IRI.create(iri+"F"));
//		OWLClass g = factory.getOWLClass(IRI.create(iri+"G"));
//		OWLClass h = factory.getOWLClass(IRI.create(iri+"H"));
//		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create(iri+"R"));
//		OWLObjectProperty s = factory.getOWLObjectProperty(IRI.create(iri+"S"));
//		OWLNamedIndividual o = factory.getOWLNamedIndividual(IRI.create(iri+"o"));
//		OWLNamedIndividual i = factory.getOWLNamedIndividual(IRI.create(iri+"i"));
////		OWLAxiom ax1 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(r, b));
////		OWLAxiom ax2 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectHasValue(r, o));
////		OWLAxiom ax3 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectIntersectionOf(b, c), d);
////		OWLAxiom ax4 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(r, c), e);
////		OWLAxiom ax5 = factory.getOWLSubClassOfAxiom(d, factory.getOWLObjectUnionOf(f, factory.getOWLObjectSomeValuesFrom(r, b)));
////		OWLAxiom ax6 = factory.getOWLSubClassOfAxiom(f, factory.getOWLObjectSomeValuesFrom(s, factory.getOWLThing()));
////		OWLAxiom ax7 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(s, factory.getOWLThing()), h);
////		OWLAxiom ax8 = factory.getOWLSubClassOfAxiom(g, h);
////		OWLAxiom ax9 = factory.getOWLClassAssertionAxiom(a, o);
//		OWLAxiom ax10 = factory.getOWLDifferentIndividualsAxiom(i,o);
//		OWLAxiom ax11 = factory.getOWLSameIndividualAxiom(i,o);
//		OWLAxiom ax12 = factory.getOWLNegativeObjectPropertyAssertionAxiom(r, o, i);
//		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//		OWLOntology ont = manager.createOntology(IRI.create(iri.replace("#", ".owl")));
////		manager.addAxiom(ont, ax1);
////		manager.addAxiom(ont, ax2);
////		manager.addAxiom(ont, ax3);
////		manager.addAxiom(ont, ax4);
////		manager.addAxiom(ont, ax5);
////		manager.addAxiom(ont, ax6);
////		manager.addAxiom(ont, ax7);
////		manager.addAxiom(ont, ax8);
////		manager.addAxiom(ont, ax9);
//		manager.addAxiom(ont, ax10);
//		manager.addAxiom(ont, ax11);
//		manager.addAxiom(ont, ax12);
//
//		
//		
//		ModuleExtractionUpperProgram prog = new ModuleExtractionUpperProgram(new IndividualManager(InseparabilityRelation.CLASSIFICATION_INSEPARABILITY));
//		prog.load(ont, new UnaryBottom());
//		
//		for (OWLAxiom ax : getNormalisedAxioms(ont)){
//			System.out.println(ax.toString());
//		}
//		
//	}
	
	
}
