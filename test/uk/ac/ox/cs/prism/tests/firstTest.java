package uk.ac.ox.cs.prism.tests;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;


public class firstTest {

	public static void main(String[] args) throws OWLOntologyCreationException{
		
		OWLDataFactoryImpl factory = new OWLDataFactoryImpl();
//		String dummyIRI = "file:/Users/Ana/Documents/Work/ontologies/dummyOntologies#";
		String dummyIRI = "file://share/Ana/AAAI2014tests/ontologies2test/dummy/dummyOntologies#";
		OWLClass a = factory.getOWLClass(IRI.create(dummyIRI + "A"));
		OWLClass b = factory.getOWLClass(IRI.create(dummyIRI + "B"));
		OWLClass c = factory.getOWLClass(IRI.create(dummyIRI + "C"));
		OWLClass d = factory.getOWLClass(IRI.create(dummyIRI + "D"));
		OWLClass e = factory.getOWLClass(IRI.create(dummyIRI + "E"));
		OWLClass f = factory.getOWLClass(IRI.create(dummyIRI + "F"));
		OWLClass g = factory.getOWLClass(IRI.create(dummyIRI + "G"));
		OWLClass h = factory.getOWLClass(IRI.create(dummyIRI + "H"));
		OWLClass i = factory.getOWLClass(IRI.create(dummyIRI + "I"));
		OWLClass j = factory.getOWLClass(IRI.create(dummyIRI + "J"));
		OWLClass k = factory.getOWLClass(IRI.create(dummyIRI + "K"));
		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create(dummyIRI + "R"));
		OWLObjectProperty s = factory.getOWLObjectProperty(IRI.create(dummyIRI + "S"));
		OWLObjectProperty q = factory.getOWLObjectProperty(IRI.create(dummyIRI + "Q"));
		
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
//		signature.add(a);
//		signature.add(d);
//		signature.add(g);
//		signature.add(h);
//		signature.add(j);
//		signature.add(q);
		signature.add(k);
		signature.add(r);
		signature.add(b);
		
		OWLAxiom ax1 = factory.getOWLSubClassOfAxiom(
				a, 
				factory.getOWLObjectSomeValuesFrom(r, b));
		OWLAxiom ax2 = factory.getOWLSubClassOfAxiom(
				a, 
				factory.getOWLObjectSomeValuesFrom(r, c));
		OWLAxiom ax3 = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectIntersectionOf(b,c), 
				d);
//		OWLAxiom ax3 = factory.getOWLSubClassOfAxiom(
//				factory.getOWLObjectIntersectionOf(b,c), 
//				factory.getOWLNothing());
		OWLAxiom ax4 = factory.getOWLSubClassOfAxiom(
				d, 
				factory.getOWLObjectSomeValuesFrom(s, e));
		OWLAxiom ax5 = factory.getOWLSubClassOfAxiom(
				d, 
				factory.getOWLObjectAllValuesFrom(s, f));
		OWLAxiom ax6 = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectSomeValuesFrom(
						s, 
						factory.getOWLObjectIntersectionOf(e,f)), 
				g);
		OWLAxiom ax7 = factory.getOWLDisjointClassesAxiom(g,h);
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		axioms.add(ax1);
		axioms.add(ax2);
		axioms.add(ax3);
		axioms.add(ax4);
		axioms.add(ax5);
		axioms.add(ax6);
		axioms.add(ax7);
		for (OWLAxiom ax : axioms){
			System.out.println(ax.toString());
		}
		System.out.println();
		System.out.println();
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology o = manager.createOntology(axioms, IRI.create("dummyOntology.owl"));
		manager.setOntologyDocumentIRI(o, IRI.create("file:/share/Ana/AAAI2014tests/ontologies2test/dummy/dummyOntology.owl"));
		try {
			manager.saveOntology(o);
			String documentIRI = OWLHelper.getOntologyPath(o);
			o = manager.loadOntologyFromOntologyDocument(new File(documentIRI));
		} catch (OWLOntologyStorageException exc) {
			exc.printStackTrace();
		}

//		TailoredModulesExtractor extractor = new TailoredModulesExtractor(o, TailoredModuleType.Bottom);
////		TailoredModulesExtractor extractor = new TailoredModulesExtractor(o, TailoredModuleType.Star);
////		TailoredModulesExtractor extractor = new TailoredModulesExtractor(o, TailoredModuleType.CQ);
////		TailoredModulesExtractor extractor = new TailoredModulesExtractor(o, TailoredModuleType.DisjDat);
////		TailoredModulesExtractor extractor = new TailoredModulesExtractor(o, TailoredModuleType.ConceptImplication);
////		TailoredModulesExtractor extractor = new TailoredModulesExtractor(o, TailoredModuleType.ConceptClassification);
//		try {
//			for (OWLAxiom ax : extractor.extract(signature)){
//				System.out.println(ax.toString());
//			}
//			
//		} catch (JRDFStoreException e1) {
//			e1.printStackTrace();
//		}
		
		Set<OWLAxiom> normalisedAxioms = NormaliserViaClauses.getNormalisedAxioms(o); 
		for (OWLAxiom ax : normalisedAxioms)
			System.out.println(ax.toString());
		
		System.out.println();
		
		OWLOntology normO = manager.createOntology(normalisedAxioms, IRI.create("dummyOntologyNormalised.owl"));
		manager.setOntologyDocumentIRI(normO, IRI.create("file:/share/Ana/AAAI2014tests/ontologies2test/dummy/dummyOntology_normalised.owl"));
		
	}
}
