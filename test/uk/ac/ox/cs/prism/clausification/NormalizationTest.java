package uk.ac.ox.cs.prism.clausification;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class NormalizationTest {

	OWLDataFactory factory = new OWLDataFactoryImpl();
	OWLClass a, b, c, d, e;
	OWLNamedIndividual i, j;
	OWLObjectProperty r, p, s;
	OWLDataProperty dp;
	OWLDatatype datatype;
	OWLDataRange dataRange, dataRange2;
	OWLAxiom ax1, ax2, ax3, ax4, ax5, ax6, ax7, ax8, ax9, ax10, ax11, ax12, ax13;
	OWLNormalization_withMaps normalization = null;
	
	@Test
	public void test() {
		
		a = factory.getOWLClass(IRI.create("A"));
		b = factory.getOWLClass(IRI.create("B"));
		c = factory.getOWLClass(IRI.create("C"));
		d = factory.getOWLClass(IRI.create("D"));
		e = factory.getOWLClass(IRI.create("E"));
		i = factory.getOWLNamedIndividual(IRI.create("i"));
		j = factory.getOWLNamedIndividual(IRI.create("j"));
		r = factory.getOWLObjectProperty(IRI.create("R")); 
		p = factory.getOWLObjectProperty(IRI.create("P")); 
		s = factory.getOWLObjectProperty(IRI.create("S"));
		dp = factory.getOWLDataProperty(IRI.create("dataP"));
		datatype = factory.getOWLDatatype(IRI.create("datatype"));
		dataRange = factory.getOWLDataUnionOf(factory.getIntegerOWLDatatype(), factory.getFloatOWLDatatype());
		dataRange2 = factory.getOWLDataIntersectionOf(factory.getIntegerOWLDatatype(), factory.getFloatOWLDatatype());
		ax1 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(r, factory.getOWLObjectIntersectionOf(b,c))); 
		//will lead to an auxiliary axiom due to nested class expression
		ax2 = factory.getOWLEquivalentClassesAxiom(factory.getOWLObjectUnionOf(a,d),factory.getOWLObjectSomeValuesFrom(r, factory.getOWLObjectIntersectionOf(b,c)));
		//some of the clauses it produces were already produced by ax1
		ax3 = factory.getOWLDatatypeDefinitionAxiom(datatype, dataRange);
		ax4 = factory.getOWLDataPropertyRangeAxiom(dp, dataRange2);
		ax5 = factory.getOWLSymmetricObjectPropertyAxiom(p);
		ax6 = factory.getOWLInverseObjectPropertiesAxiom(r, s);
		ax7 = factory.getOWLSameIndividualAxiom(i,j);
		ax8 = factory.getOWLClassAssertionAxiom(e, i);
		ax9 = factory.getOWLClassAssertionAxiom(e.getComplementNNF(), j);
		ax10 = factory.getOWLObjectPropertyAssertionAxiom(p, i, j);
		ax11 = factory.getOWLNegativeObjectPropertyAssertionAxiom(p, j, i);
		ax12 = factory.getOWLDataPropertyAssertionAxiom(dp, i, factory.getOWLLiteral(2));
		ax13 = factory.getOWLNegativeDataPropertyAssertionAxiom(dp, i, factory.getOWLLiteral(2));
		
		Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
		axiomSet.add(ax1);
		axiomSet.add(ax2);
		axiomSet.add(ax3);
		axiomSet.add(ax4);
		axiomSet.add(ax5);
		axiomSet.add(ax6);
		axiomSet.add(ax7);
		axiomSet.add(ax8);
		axiomSet.add(ax9);
		axiomSet.add(ax10);
		axiomSet.add(ax11);
		axiomSet.add(ax12);
		axiomSet.add(ax13);
		
		propertyManagersTest(normalizationTest(axiomSet));
	}
	
	public void propertyManagersTest(OWLAxioms_withMaps axioms) {
		BuiltInPropertyManager_withMaps builtInPropertyManager=new BuiltInPropertyManager_withMaps(factory);
        builtInPropertyManager.axiomatizeBuiltInPropertiesAsNeeded(axioms);
        ObjectPropertyInclusionManager_withMaps objectPropertyInclusionManager=new ObjectPropertyInclusionManager_withMaps(axioms);
        objectPropertyInclusionManager.rewriteNegativeObjectPropertyAssertions(factory,axioms,normalization.getDefinitions().size());
        objectPropertyInclusionManager.rewriteAxioms(factory,axioms,0);
        
      //and now go through the output
//		m_classes
		Collection<OWLClass> expected_classes = new HashSet<OWLClass>();
		expected_classes.add(a);
		expected_classes.add(b);
		expected_classes.add(c);
		expected_classes.add(d);
		expected_classes.add(e);
		Assert.assertEquals(expected_classes, axioms.m_classes);

//		m_objectProperties
		Collection<OWLObjectProperty> expected_properties = new HashSet<OWLObjectProperty>();
		expected_properties.add(p);
		expected_properties.add(r);
		expected_properties.add(s);
		Assert.assertEquals(expected_properties, axioms.m_objectProperties);
		
//	    m_objectPropertiesOccurringInOWLAxioms
		Assert.assertEquals(expected_properties, axioms.m_objectPropertiesOccurringInOWLAxioms);

//		//		    public final Set<OWLObjectPropertyExpression> m_complexObjectPropertyExpressions; //kept
//		System.out.println(axioms.m_complexObjectPropertyExpressions); //only modified after applying the complexobjectpropertymanager

//	    m_dataProperties
		Collection<OWLDataProperty> expected_dataProperties = new HashSet<OWLDataProperty>();
		expected_dataProperties.add(dp);
		Assert.assertEquals(expected_dataProperties, axioms.m_dataProperties);
		
//	    m_namedIndividuals
		Collection<OWLNamedIndividual> expected_individuals = new HashSet<OWLNamedIndividual>();
		expected_individuals.add(i);
		expected_individuals.add(j);
		Assert.assertEquals(expected_individuals, axioms.m_namedIndividuals);
		
		OWLClassExpression def0 = null;
		OWLClassExpression def1 = null;
		OWLClassExpression def2 = null;
//	    m_conceptInclusions_map
		Assert.assertTrue(axioms.m_conceptInclusions_map.size() == 3);
		Collection<OWLClassExpression[]> inclusions = axioms.m_conceptInclusions_map.get(ax2);
		Assert.assertTrue(inclusions.size() == 2);
		boolean[] found = new boolean[]{false, false}; 
		for (OWLClassExpression[] inclusion : inclusions) {
			 if (!found[0] && inclusion.length == 3 &&
					 inclusion[0].equals(a) && inclusion[1].equals(d) && 
					 inclusion[2] instanceof OWLObjectAllValuesFrom && 
					 ((OWLObjectAllValuesFrom) inclusion[2]).getProperty().equals(r) && 
					 ((OWLObjectAllValuesFrom) inclusion[2]).getFiller() instanceof OWLObjectComplementOf) {
				 def0 = ((OWLObjectAllValuesFrom) inclusion[2]).getFiller().getComplementNNF();
				 found[0] = true;
				 continue;
			 }
			 if (!found[1] && inclusion.length == 2 &&
					 inclusion[0] instanceof OWLObjectComplementOf && 
					 inclusion[1] instanceof OWLObjectSomeValuesFrom && ((OWLObjectSomeValuesFrom) inclusion[1]).getProperty().equals(r)) {
				 def1 = ((OWLObjectComplementOf) inclusion[0]).getOperand();
				 def2 = ((OWLObjectSomeValuesFrom) inclusion[1]).getFiller();
				 found[1] = true;
				 continue;
			 }
			 Assert.assertTrue(false);
		}
		inclusions = axioms.m_conceptInclusions_map.get(ax1);
		Assert.assertTrue(inclusions.size() == 1);
		for (OWLClassExpression[] inclusion : inclusions) {
			Assert.assertTrue(inclusion.length == 2 &&
					 inclusion[0].equals(a.getComplementNNF()) && 
					 inclusion[1] instanceof OWLObjectSomeValuesFrom && 
					 ((OWLObjectSomeValuesFrom) inclusion[1]).getProperty().equals(r) &&
					 ((OWLObjectSomeValuesFrom) inclusion[1]).getFiller().equals(def2));
		}
		inclusions = axioms.m_conceptInclusions_map.get(ax4);
		Assert.assertTrue(inclusions.size() == 1);
		OWLDataRange defData0 = null;
		for (OWLClassExpression[] inclusion : inclusions) {
			if (inclusion.length == 1 &&
					 inclusion[0] instanceof OWLDataAllValuesFrom && 
					 ((OWLDataAllValuesFrom) inclusion[0]).getProperty().equals(dp))
				defData0 = ((OWLDataAllValuesFrom) inclusion[0]).getFiller();
		}
		
		
		found = new boolean[]{false, false, false, false, false};
		Assert.assertTrue(axioms.m_auxiliaryConceptInclusions.size() == 5);
		for (OWLClassExpression[] inclusion : axioms.m_auxiliaryConceptInclusions) {
			if (!found[0] && inclusion.length == 2 && inclusion[0].equals(def1) && inclusion[1].equals(d.getComplementNNF())) {
				 found[0] = true;
				 continue;
			 }
			 if (!found[1] && inclusion.length == 2 && inclusion[0].equals(c) && inclusion[1].equals(def2.getComplementNNF())) {
				 found[1] = true;
				 continue;
			 }
			 if (!found[2] && inclusion.length == 2 && inclusion[0].equals(b) && inclusion[1].equals(def2.getComplementNNF())) {
				 found[2] = true;
				 continue;
			 }
			 if (!found[3] && inclusion.length == 3 && inclusion[0].equals(def0) && inclusion[1].equals(b.getComplementNNF()) && inclusion[2].equals(c.getComplementNNF())) {
				 found[3] = true;
				 continue;
			 }
			 if (!found[4] && inclusion.length == 2 && inclusion[0].equals(def1) && inclusion[1].equals(a.getComplementNNF())) {
				 found[4] = true;
				 continue;
			 }
			 Assert.assertTrue(false);
		}
		
//		m_dataRangeInclusions_map
		Assert.assertTrue(axioms.m_dataRangeInclusions_map.size() == 1);
		Collection<OWLDataRange[]> dataRangeInclusions = axioms.m_dataRangeInclusions_map.get(ax3);
		Assert.assertTrue(dataRangeInclusions.size() == 3);
		found = new boolean[]{false, false, false}; 
		for (OWLDataRange[] inclusion : dataRangeInclusions) {
			 if (!found[0] && inclusion.length == 3 &&
					 inclusion[0].equals(factory.getFloatOWLDatatype()) && inclusion[1].equals(factory.getOWLDataComplementOf(datatype)) && 
					 inclusion[2].equals(factory.getIntegerOWLDatatype())) {
				 found[0] = true;
				 continue;
			 }
			 if (!found[1] && inclusion.length == 2 &&
					 inclusion[0].equals(datatype) && 
					 inclusion[1].equals(factory.getOWLDataComplementOf(factory.getFloatOWLDatatype()))) {
				 found[1] = true;
				 continue;
			 }
			 if (!found[2] && inclusion.length == 2 &&
					 inclusion[0].equals(datatype) && 
					 inclusion[1].equals(factory.getOWLDataComplementOf(factory.getIntegerOWLDatatype()))) {
				 found[2] = true;
				 continue;
			 }
//			 for (OWLDataRange dr : inclusion)
//				 System.out.println(dr);
			 Assert.assertTrue(false);
		}
		
//		m_auxiliaryDataRangeInclusions;
		found = new boolean[]{false, false};
		Assert.assertTrue(axioms.m_auxiliaryDataRangeInclusions.size() == 2);
		for (OWLDataRange[] inclusion : axioms.m_auxiliaryDataRangeInclusions) {
			if (!found[0] && inclusion.length == 2 && inclusion[0].equals(factory.getOWLDataComplementOf(defData0)) && inclusion[1].equals(factory.getIntegerOWLDatatype())) {
				 found[0] = true;
				 continue;
			 }
			 if (!found[1] && inclusion.length == 2 && inclusion[0].equals(factory.getOWLDataComplementOf(defData0)) && inclusion[1].equals(factory.getFloatOWLDatatype())) {
				 found[1] = true;
				 continue;
			 }
			 Assert.assertTrue(false);
		}
		
//		public final Map<OWLAxiom,Collection<OWLObjectPropertyExpression[]>> m_simpleObjectPropertyInclusions_map;
		Assert.assertTrue(axioms.m_simpleObjectPropertyInclusions_map.size() == 1);
		Collection<OWLObjectPropertyExpression[]> objectPropertyInclusions = axioms.m_simpleObjectPropertyInclusions_map.get(ax6);
		Assert.assertTrue(objectPropertyInclusions.size() == 2);
		found = new boolean[]{false, false};
		for (OWLObjectPropertyExpression[] inclusion : objectPropertyInclusions) {
			if (!found[0] && inclusion.length == 2 && inclusion[0].equals(r) && inclusion[1].equals(s.getInverseProperty())) {
				 found[0] = true;
				 continue;
			 }
			 if (!found[1] && inclusion.length == 2 && inclusion[0].equals(s) && inclusion[1].equals(r.getInverseProperty())) {
				 found[1] = true;
				 continue;
			 }
			 Assert.assertTrue(false);
		}
		
//		m_complexObjectPropertyInclusions
		Assert.assertTrue(axioms.m_complexObjectPropertyInclusions.isEmpty());
		
//	    m_disjointObjectProperties
		Assert.assertTrue(axioms.m_disjointObjectProperties.isEmpty());
		
//	    m_reflexiveObjectProperties
		Assert.assertTrue(axioms.m_reflexiveObjectProperties.isEmpty());
		
//	    m_irreflexiveObjectProperties
		Assert.assertTrue(axioms.m_irreflexiveObjectProperties.isEmpty());
		
//		m_symmetricObjectProperties;
		Assert.assertTrue(axioms.m_symmetricObjectProperties.size() == 1 &&
				axioms.m_symmetricObjectProperties.iterator().next().equals(p));
		
//	    m_asymmetricObjectProperties
		Assert.assertTrue(axioms.m_asymmetricObjectProperties.isEmpty());
		
//		m_dataPropertyInclusions_map
		Assert.assertTrue(axioms.m_dataPropertyInclusions_map.isEmpty());
		
//	    m_disjointDataProperties
		Assert.assertTrue(axioms.m_disjointDataProperties.isEmpty());
		
//		m_facts_map
		Assert.assertTrue(axioms.m_facts_map.size() == 7);
		Collection<OWLIndividualAxiom> facts = axioms.m_facts_map.get(ax7);
		Assert.assertTrue(facts.size() == 1 && facts.iterator().next().equals(ax7));
		facts = axioms.m_facts_map.get(ax8);
		Assert.assertTrue(facts.size() == 1 && facts.iterator().next().equals(ax8));
		facts = axioms.m_facts_map.get(ax9);
		Assert.assertTrue(facts.size() == 1 && facts.iterator().next().equals(ax9));
		facts = axioms.m_facts_map.get(ax10);
		Assert.assertTrue(facts.size() == 1 && facts.iterator().next().equals(ax10));
		facts = axioms.m_facts_map.get(ax11);
		Assert.assertTrue(facts.size() == 1 && facts.iterator().next().equals(ax11));
		facts = axioms.m_facts_map.get(ax12);
		Assert.assertTrue(facts.size() == 1 && facts.iterator().next().equals(ax12));
		facts = axioms.m_facts_map.get(ax13);
		Assert.assertTrue(facts.size() == 1 && facts.iterator().next().equals(ax13));
		
		
//		m_hasKeys_map
		Assert.assertTrue(axioms.m_hasKeys_map.isEmpty());
		
//	    m_definedDatatypesIRIs
		Assert.assertTrue(axioms.m_definedDatatypesIRIs.size() == 1);
		Assert.assertTrue(axioms.m_definedDatatypesIRIs.iterator().next().equals(datatype.toStringID()));
		
//		m_rules_map
		Assert.assertTrue(axioms.m_rules_map.isEmpty());
	}
	
	public OWLAxioms_withMaps normalizationTest(Set<OWLAxiom> axiomSet) {
		
		
		try {
			OWLOntology o = OWLManager.createOWLOntologyManager().createOntology(axiomSet);
			OWLAxioms_withMaps axioms = new OWLAxioms_withMaps();
			normalization = new OWLNormalization_withMaps(factory, axioms, 0);
			normalization.processOntology(o);
			
			//and now go through the output
//			m_classes
			Collection<OWLClass> expected_classes = new HashSet<OWLClass>();
			expected_classes.add(a);
			expected_classes.add(b);
			expected_classes.add(c);
			expected_classes.add(d);
			expected_classes.add(e);
			Assert.assertEquals(expected_classes, axioms.m_classes);

//			m_objectProperties
			Collection<OWLObjectProperty> expected_properties = new HashSet<OWLObjectProperty>();
			expected_properties.add(p);
			expected_properties.add(r);
			expected_properties.add(s);
			Assert.assertEquals(expected_properties, axioms.m_objectProperties);
			
//		    m_objectPropertiesOccurringInOWLAxioms
			Assert.assertEquals(expected_properties, axioms.m_objectPropertiesOccurringInOWLAxioms);

//			//		    public final Set<OWLObjectPropertyExpression> m_complexObjectPropertyExpressions; //kept
//			System.out.println(axioms.m_complexObjectPropertyExpressions); //only modified after applying the complexobjectpropertymanager

//		    m_dataProperties
			Collection<OWLDataProperty> expected_dataProperties = new HashSet<OWLDataProperty>();
			expected_dataProperties.add(dp);
			Assert.assertEquals(expected_dataProperties, axioms.m_dataProperties);
			
//		    m_namedIndividuals
			Collection<OWLNamedIndividual> expected_individuals = new HashSet<OWLNamedIndividual>();
			expected_individuals.add(i);
			expected_individuals.add(j);
			Assert.assertEquals(expected_individuals, axioms.m_namedIndividuals);
			
			OWLClassExpression def0 = null;
			OWLClassExpression def1 = null;
			OWLClassExpression def2 = null;
//		    m_conceptInclusions_map
			Assert.assertTrue(axioms.m_conceptInclusions_map.size() == 3);
			Collection<OWLClassExpression[]> inclusions = axioms.m_conceptInclusions_map.get(ax2);
			Assert.assertTrue(inclusions.size() == 2);
			boolean[] found = new boolean[]{false, false}; 
			for (OWLClassExpression[] inclusion : inclusions) {
				 if (!found[0] && inclusion.length == 3 &&
						 inclusion[0].equals(a) && inclusion[1].equals(d) && 
						 inclusion[2] instanceof OWLObjectAllValuesFrom && 
						 ((OWLObjectAllValuesFrom) inclusion[2]).getProperty().equals(r) && 
						 ((OWLObjectAllValuesFrom) inclusion[2]).getFiller() instanceof OWLObjectComplementOf) {
					 def0 = ((OWLObjectAllValuesFrom) inclusion[2]).getFiller().getComplementNNF();
					 found[0] = true;
					 continue;
				 }
				 if (!found[1] && inclusion.length == 2 &&
						 inclusion[0] instanceof OWLObjectComplementOf && 
						 inclusion[1] instanceof OWLObjectSomeValuesFrom && ((OWLObjectSomeValuesFrom) inclusion[1]).getProperty().equals(r)) {
					 def1 = ((OWLObjectComplementOf) inclusion[0]).getOperand();
					 def2 = ((OWLObjectSomeValuesFrom) inclusion[1]).getFiller();
					 found[1] = true;
					 continue;
				 }
				 Assert.assertTrue(false);
			}
			inclusions = axioms.m_conceptInclusions_map.get(ax1);
			Assert.assertTrue(inclusions.size() == 1);
			for (OWLClassExpression[] inclusion : inclusions) {
				Assert.assertTrue(inclusion.length == 2 &&
						 inclusion[0].equals(a.getComplementNNF()) && 
						 inclusion[1] instanceof OWLObjectSomeValuesFrom && 
						 ((OWLObjectSomeValuesFrom) inclusion[1]).getProperty().equals(r) &&
						 ((OWLObjectSomeValuesFrom) inclusion[1]).getFiller().equals(def2));
			}
			inclusions = axioms.m_conceptInclusions_map.get(ax4);
			Assert.assertTrue(inclusions.size() == 1);
			OWLDataRange defData0 = null;
			for (OWLClassExpression[] inclusion : inclusions) {
				if (inclusion.length == 1 &&
						 inclusion[0] instanceof OWLDataAllValuesFrom && 
						 ((OWLDataAllValuesFrom) inclusion[0]).getProperty().equals(dp))
					defData0 = ((OWLDataAllValuesFrom) inclusion[0]).getFiller();
			}
			
			
			found = new boolean[]{false, false, false, false, false};
			Assert.assertTrue(axioms.m_auxiliaryConceptInclusions.size() == 5);
			for (OWLClassExpression[] inclusion : axioms.m_auxiliaryConceptInclusions) {
				if (!found[0] && inclusion.length == 2 && inclusion[0].equals(def1) && inclusion[1].equals(d.getComplementNNF())) {
					 found[0] = true;
					 continue;
				 }
				 if (!found[1] && inclusion.length == 2 && inclusion[0].equals(c) && inclusion[1].equals(def2.getComplementNNF())) {
					 found[1] = true;
					 continue;
				 }
				 if (!found[2] && inclusion.length == 2 && inclusion[0].equals(b) && inclusion[1].equals(def2.getComplementNNF())) {
					 found[2] = true;
					 continue;
				 }
				 if (!found[3] && inclusion.length == 3 && inclusion[0].equals(def0) && inclusion[1].equals(b.getComplementNNF()) && inclusion[2].equals(c.getComplementNNF())) {
					 found[3] = true;
					 continue;
				 }
				 if (!found[4] && inclusion.length == 2 && inclusion[0].equals(def1) && inclusion[1].equals(a.getComplementNNF())) {
					 found[4] = true;
					 continue;
				 }
				 Assert.assertTrue(false);
			}
			
//			m_dataRangeInclusions_map
			Assert.assertTrue(axioms.m_dataRangeInclusions_map.size() == 1);
			Collection<OWLDataRange[]> dataRangeInclusions = axioms.m_dataRangeInclusions_map.get(ax3);
			Assert.assertTrue(dataRangeInclusions.size() == 3);
			found = new boolean[]{false, false, false}; 
			for (OWLDataRange[] inclusion : dataRangeInclusions) {
				 if (!found[0] && inclusion.length == 3 &&
						 inclusion[0].equals(factory.getFloatOWLDatatype()) && inclusion[1].equals(factory.getOWLDataComplementOf(datatype)) && 
						 inclusion[2].equals(factory.getIntegerOWLDatatype())) {
					 found[0] = true;
					 continue;
				 }
				 if (!found[1] && inclusion.length == 2 &&
						 inclusion[0].equals(datatype) && 
						 inclusion[1].equals(factory.getOWLDataComplementOf(factory.getFloatOWLDatatype()))) {
					 found[1] = true;
					 continue;
				 }
				 if (!found[2] && inclusion.length == 2 &&
						 inclusion[0].equals(datatype) && 
						 inclusion[1].equals(factory.getOWLDataComplementOf(factory.getIntegerOWLDatatype()))) {
					 found[2] = true;
					 continue;
				 }
//				 for (OWLDataRange dr : inclusion)
//					 System.out.println(dr);
				 Assert.assertTrue(false);
			}
			
//			m_auxiliaryDataRangeInclusions;
			found = new boolean[]{false, false};
			Assert.assertTrue(axioms.m_auxiliaryDataRangeInclusions.size() == 2);
			for (OWLDataRange[] inclusion : axioms.m_auxiliaryDataRangeInclusions) {
				if (!found[0] && inclusion.length == 2 && inclusion[0].equals(factory.getOWLDataComplementOf(defData0)) && inclusion[1].equals(factory.getIntegerOWLDatatype())) {
					 found[0] = true;
					 continue;
				 }
				 if (!found[1] && inclusion.length == 2 && inclusion[0].equals(factory.getOWLDataComplementOf(defData0)) && inclusion[1].equals(factory.getFloatOWLDatatype())) {
					 found[1] = true;
					 continue;
				 }
				 Assert.assertTrue(false);
			}
			
//			public final Map<OWLAxiom,Collection<OWLObjectPropertyExpression[]>> m_simpleObjectPropertyInclusions_map;
			Assert.assertTrue(axioms.m_simpleObjectPropertyInclusions_map.size() == 1);
			Collection<OWLObjectPropertyExpression[]> objectPropertyInclusions = axioms.m_simpleObjectPropertyInclusions_map.get(ax6);
			Assert.assertTrue(objectPropertyInclusions.size() == 2);
			found = new boolean[]{false, false};
			for (OWLObjectPropertyExpression[] inclusion : objectPropertyInclusions) {
				if (!found[0] && inclusion.length == 2 && inclusion[0].equals(r) && inclusion[1].equals(s.getInverseProperty())) {
					 found[0] = true;
					 continue;
				 }
				 if (!found[1] && inclusion.length == 2 && inclusion[0].equals(s) && inclusion[1].equals(r.getInverseProperty())) {
					 found[1] = true;
					 continue;
				 }
				 Assert.assertTrue(false);
			}
			
//			m_complexObjectPropertyInclusions
			Assert.assertTrue(axioms.m_complexObjectPropertyInclusions.isEmpty());
			
//		    m_disjointObjectProperties
			Assert.assertTrue(axioms.m_disjointObjectProperties.isEmpty());
			
//		    m_reflexiveObjectProperties
			Assert.assertTrue(axioms.m_reflexiveObjectProperties.isEmpty());
			
//		    m_irreflexiveObjectProperties
			Assert.assertTrue(axioms.m_irreflexiveObjectProperties.isEmpty());
			
//			m_symmetricObjectProperties;
			Assert.assertTrue(axioms.m_symmetricObjectProperties.size() == 1 &&
					axioms.m_symmetricObjectProperties.iterator().next().equals(p));
			
//		    m_asymmetricObjectProperties
			Assert.assertTrue(axioms.m_asymmetricObjectProperties.isEmpty());
			
//			m_dataPropertyInclusions_map
			Assert.assertTrue(axioms.m_dataPropertyInclusions_map.isEmpty());
			
//		    m_disjointDataProperties
			Assert.assertTrue(axioms.m_disjointDataProperties.isEmpty());
			
//			m_facts_map
			Assert.assertTrue(axioms.m_facts_map.size() == 7);
			Collection<OWLIndividualAxiom> facts = axioms.m_facts_map.get(ax7);
			Assert.assertTrue(facts.size() == 1 && facts.iterator().next().equals(ax7));
			facts = axioms.m_facts_map.get(ax8);
			Assert.assertTrue(facts.size() == 1 && facts.iterator().next().equals(ax8));
			facts = axioms.m_facts_map.get(ax9);
			Assert.assertTrue(facts.size() == 1 && facts.iterator().next().equals(ax9));
			facts = axioms.m_facts_map.get(ax10);
			Assert.assertTrue(facts.size() == 1 && facts.iterator().next().equals(ax10));
			facts = axioms.m_facts_map.get(ax11);
			Assert.assertTrue(facts.size() == 1 && facts.iterator().next().equals(ax11));
			facts = axioms.m_facts_map.get(ax12);
			Assert.assertTrue(facts.size() == 1 && facts.iterator().next().equals(ax12));
			facts = axioms.m_facts_map.get(ax13);
			Assert.assertTrue(facts.size() == 1 && facts.iterator().next().equals(ax13));
			
			
//			m_hasKeys_map
			Assert.assertTrue(axioms.m_hasKeys_map.isEmpty());
			
//		    m_definedDatatypesIRIs
			Assert.assertTrue(axioms.m_definedDatatypesIRIs.size() == 1);
			Assert.assertTrue(axioms.m_definedDatatypesIRIs.iterator().next().equals(datatype.toStringID()));
			
//			m_rules_map
			Assert.assertTrue(axioms.m_rules_map.isEmpty());
			
			return axioms;
			
		} catch (OWLOntologyCreationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
	}
}
