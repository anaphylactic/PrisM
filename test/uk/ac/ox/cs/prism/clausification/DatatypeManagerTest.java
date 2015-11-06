package uk.ac.ox.cs.prism.clausification;

import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLLiteral;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class DatatypeManagerTest {

	@Test
	public void supportedDatatypesTest() {
		DatatypeManager dtManager = new DatatypeManager();
		OWLDataFactory factory = new OWLDataFactoryImpl();
		OWLLiteral lit = factory.getOWLLiteral((double) 56);
		Assert.assertTrue(dtManager.isSupported(lit));
		OWLDataRange range = factory.getIntegerOWLDatatype();
		Assert.assertTrue(dtManager.isSupported(range));
		range = factory.getOWLDataIntersectionOf(factory.getIntegerOWLDatatype(), factory.getDoubleOWLDatatype());
		Assert.assertTrue(!dtManager.isSupported(range));
		
	}
}
