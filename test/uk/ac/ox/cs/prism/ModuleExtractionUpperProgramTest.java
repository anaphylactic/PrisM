package uk.ac.ox.cs.prism;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.pagoda.constraints.UnaryBottom;
import uk.ac.ox.cs.prism.IndividualManager;
import uk.ac.ox.cs.prism.ModuleExtractionUpperProgram;
import uk.ac.ox.cs.prism.PrisM.InseparabilityRelation;


public class ModuleExtractionUpperProgramTest {

	String iri = "file://dummy#";
	
	
	
	@Test
	public void removingTransitivityAxiomsTest_remove() throws OWLOntologyCreationException, JRDFStoreException {
		OWLDataFactory factory = new OWLDataFactoryImpl();
		
		OWLClass a = factory.getOWLClass(IRI.create(iri+"A"));
		OWLClass b = factory.getOWLClass(IRI.create(iri+"B"));
		OWLObjectProperty p = factory.getOWLObjectProperty(IRI.create(iri+"P"));
		OWLAxiom ax1 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(p, b));
		OWLAxiom ax2 = factory.getOWLTransitiveObjectPropertyAxiom(p);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology(IRI.create(iri.replace("#", ".owl")));
		manager.addAxiom(ont, ax1);
		manager.addAxiom(ont, ax2);
		
		
		ModuleExtractionUpperProgram program = new ModuleExtractionUpperProgram(new IndividualManager(InseparabilityRelation.IMPLICATION_INSEPARABILITY));
		program.load(ont, new UnaryBottom());
		program.transform();
		
		System.out.println(program.toString());//should not contain the clause corresponding to the transitivity axiom
			
	}
	
	@Test
	public void removingTransitivityAxiomsTest_keep() throws OWLOntologyCreationException, JRDFStoreException {
		OWLDataFactory factory = new OWLDataFactoryImpl();
		
		OWLClass a = factory.getOWLClass(IRI.create(iri+"A"));
		OWLClass b = factory.getOWLClass(IRI.create(iri+"B"));
		OWLObjectProperty p = factory.getOWLObjectProperty(IRI.create(iri+"P"));
		OWLAxiom ax1 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(p, b), a);
		OWLAxiom ax2 = factory.getOWLTransitiveObjectPropertyAxiom(p);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology(IRI.create(iri.replace("#", ".owl")));
		manager.addAxiom(ont, ax1);
		manager.addAxiom(ont, ax2);
		
		
		ModuleExtractionUpperProgram program = new ModuleExtractionUpperProgram(new IndividualManager(InseparabilityRelation.IMPLICATION_INSEPARABILITY));
		program.load(ont, new UnaryBottom());
		program.transform();
		
		System.out.println(program.toString());//should contain the clause corresponding to the transitivity axiom
			
	}
	
	
}
