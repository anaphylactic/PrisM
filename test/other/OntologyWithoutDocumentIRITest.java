package other;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.ox.cs.prism.PrisM;
import uk.ac.ox.cs.prism.PrisM.InseparabilityRelation;

public class OntologyWithoutDocumentIRITest {

	@Test
	public void test(){
		OWLDataFactory factory = new OWLDataFactoryImpl();
		OWLClass a = factory.getOWLClass(IRI.create("A"));
		OWLClass b = factory.getOWLClass(IRI.create("B"));
		OWLClass c = factory.getOWLClass(IRI.create("C"));
		OWLClass d = factory.getOWLClass(IRI.create("D"));
		
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
		signature.add(a);
		
		OWLAxiom ax1 = factory.getOWLSubClassOfAxiom(a, b);
		OWLAxiom ax2 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectIntersectionOf(c, d), b);
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology o;
		try {
			o = manager.createOntology();
			manager.addAxiom(o, ax1);
			manager.addAxiom(o, ax2);
		
			PrisM prism = new PrisM(o, InseparabilityRelation.CLASSIFICATION_INSEPARABILITY);
			prism.extract(signature);
			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		
	}
	
}
