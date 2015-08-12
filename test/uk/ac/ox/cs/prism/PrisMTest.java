package uk.ac.ox.cs.prism;

import static org.junit.Assert.assertTrue;


import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.prism.PrisM;
import uk.ac.ox.cs.prism.PrisM.InseparabilityRelation;
import util.TestUtility;

public class PrisMTest{//TODO organise tests by module type - one test class for each

	String iri = "file://dummy#";
	
	@Test
	public void RoleInstatiationInClassificationModulesTest() throws OWLOntologyCreationException, JRDFStoreException {
		OWLDataFactory factory = new OWLDataFactoryImpl();
		
		OWLClass a = factory.getOWLClass(IRI.create(iri+"A"));
		OWLObjectProperty p = factory.getOWLObjectProperty(IRI.create(iri+"P"));
		OWLObjectProperty q = factory.getOWLObjectProperty(IRI.create(iri+"Q"));
		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create(iri+"R"));
		OWLAxiom ax1 = factory.getOWLSubObjectPropertyOfAxiom(p, r);
		OWLAxiom ax2 = factory.getOWLSubObjectPropertyOfAxiom(q, r);
		OWLAxiom ax3 = factory.getOWLFunctionalObjectPropertyAxiom(r);
		OWLAxiom ax4 = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectSomeValuesFrom(p, factory.getOWLThing()), 
				factory.getOWLObjectSomeValuesFrom(q, factory.getOWLThing()));
		OWLAxiom ax5 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(r, a));
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology o = manager.createOntology(IRI.create(iri.replace("#", ".owl")));
		manager.addAxiom(o, ax1);
		manager.addAxiom(o, ax2);
		manager.addAxiom(o, ax3);
		manager.addAxiom(o, ax4);
		manager.addAxiom(o, ax5);
		
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
		signature.add(p);
		
		PrisM extractor = new PrisM(o, InseparabilityRelation.CLASSIFICATION_INSEPARABILITY);
		
		Set<String> actual = new HashSet<String>();
		for (OWLAxiom ax : extractor.extract(signature)){
			actual.add(ax.toString());
//			System.out.println(ax.toString());
		}
		
		Set<String> control = new HashSet<String>();
		control.add("SubObjectPropertyOf(<file://dummy#Q> <file://dummy#R>)");
		control.add("FunctionalObjectProperty(<file://dummy#R>)");
		control.add("SubObjectPropertyOf(<file://dummy#P> <file://dummy#R>)");
		control.add("SubClassOf(ObjectSomeValuesFrom(<file://dummy#P> owl:Thing) ObjectSomeValuesFrom(<file://dummy#Q> owl:Thing))");
		
		assertTrue(TestUtility.compareCollections(actual, control));
	}

	
	@Test
	public void RoleInstatiationInClassificationModulesTest2() throws OWLOntologyCreationException, JRDFStoreException {
		OWLDataFactory factory = new OWLDataFactoryImpl();
		
		OWLClass a = factory.getOWLClass(IRI.create(iri+"A"));
		OWLObjectProperty p = factory.getOWLObjectProperty(IRI.create(iri+"P"));
		OWLObjectProperty q = factory.getOWLObjectProperty(IRI.create(iri+"Q"));
		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create(iri+"R"));
		OWLAxiom ax1 = factory.getOWLSubObjectPropertyOfAxiom(p, r);
		OWLAxiom ax2 = factory.getOWLSubObjectPropertyOfAxiom(q, r);
		OWLAxiom ax3 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(p, factory.getOWLThing()));
		OWLAxiom ax4 = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectHasSelf(p), 
				factory.getOWLObjectHasSelf(q));
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology o = manager.createOntology(IRI.create(iri.replace("#", ".owl")));
		manager.addAxiom(o, ax1);
		manager.addAxiom(o, ax2);
		manager.addAxiom(o, ax3);
		manager.addAxiom(o, ax4);
		
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
		signature.add(p);
		
		PrisM extractor = new PrisM(o, InseparabilityRelation.CLASSIFICATION_INSEPARABILITY);
		
		Set<String> actual = new HashSet<String>();
		for (OWLAxiom ax : extractor.extract(signature)){
			actual.add(ax.toString());
//			System.out.println(ax.toString());
		}
		
		Set<String> control = new HashSet<String>();
		control.add("SubObjectPropertyOf(<file://dummy#P> <file://dummy#R>)");
		
		assertTrue(TestUtility.compareCollections(actual, control));
	}
	
	@Test
	public void NotRetrievingProofsForTautologiesInClassificationTest() throws OWLOntologyCreationException, JRDFStoreException {
		OWLDataFactory factory = new OWLDataFactoryImpl();
		
		OWLClass a = factory.getOWLClass(IRI.create(iri+"A"));
		OWLClass b = factory.getOWLClass(IRI.create(iri+"B"));
		OWLClass c = factory.getOWLClass(IRI.create(iri+"C"));
		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create(iri+"R"));
		OWLAxiom ax1 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(r, c));
		OWLAxiom ax2 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(r, c), a);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology o = manager.createOntology(IRI.create(iri.replace("#", ".owl")));
		manager.addAxiom(o, ax1);
		manager.addAxiom(o, ax2);

		
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
		signature.add(a);
		
		PrisM extractor = new PrisM(o, InseparabilityRelation.CLASSIFICATION_INSEPARABILITY);
		
		Set<String> actual = new HashSet<String>();
		for (OWLAxiom ax : extractor.extract(signature)){
			actual.add(ax.toString());
//			System.out.println(ax.toString());
		}
		
		Set<String> control = new HashSet<String>();
		
		assertTrue(TestUtility.compareCollections(actual, control));
	}
	

	@Test
	public void RunningExampleImplicationInsepTest() throws OWLOntologyCreationException, JRDFStoreException {
		OWLDataFactory factory = new OWLDataFactoryImpl();
		
		OWLClass a = factory.getOWLClass(IRI.create(iri+"A"));
		OWLClass b = factory.getOWLClass(IRI.create(iri+"B"));
		OWLClass c = factory.getOWLClass(IRI.create(iri+"C"));
		OWLClass d = factory.getOWLClass(IRI.create(iri+"D"));
		OWLClass e = factory.getOWLClass(IRI.create(iri+"E"));
		OWLClass f = factory.getOWLClass(IRI.create(iri+"F"));
		OWLClass g = factory.getOWLClass(IRI.create(iri+"G"));
		OWLClass h = factory.getOWLClass(IRI.create(iri+"H"));
		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create(iri+"R"));
		OWLObjectProperty s = factory.getOWLObjectProperty(IRI.create(iri+"S"));
		OWLNamedIndividual o = factory.getOWLNamedIndividual(IRI.create(iri+"o"));
		OWLAxiom ax1 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(r, b));
		OWLAxiom ax2 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectHasValue(r, o));
		OWLAxiom ax3 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectIntersectionOf(b, c), d);
		OWLAxiom ax4 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(r, c), e);
		OWLAxiom ax5 = factory.getOWLSubClassOfAxiom(d, factory.getOWLObjectUnionOf(f, g));
		OWLAxiom ax6 = factory.getOWLSubClassOfAxiom(f, factory.getOWLObjectSomeValuesFrom(s, factory.getOWLThing()));
		OWLAxiom ax7 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(s, factory.getOWLThing()), h);
		OWLAxiom ax8 = factory.getOWLSubClassOfAxiom(g, h);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology(IRI.create(iri.replace("#", ".owl")));
		manager.addAxiom(ont, ax1);
		manager.addAxiom(ont, ax2);
		manager.addAxiom(ont, ax3);
		manager.addAxiom(ont, ax4);
		manager.addAxiom(ont, ax5);
		manager.addAxiom(ont, ax6);
		manager.addAxiom(ont, ax7);
		manager.addAxiom(ont, ax8);

		
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
		signature.add(b);
		signature.add(c);
		signature.add(d);
		signature.add(h);
		
		PrisM extractor = new PrisM(ont, InseparabilityRelation.IMPLICATION_INSEPARABILITY);
		
		Set<String> actual = new HashSet<String>();
		for (OWLAxiom ax : extractor.extract(signature)){
//			System.out.println(ax.toString());
			actual.add(ax.toString());
		}
//		System.out.println();
		Set<String> control = new HashSet<String>();
		control.add("SubClassOf(<file://dummy#D> ObjectUnionOf(<file://dummy#F> <file://dummy#G>))");
		control.add("SubClassOf(<file://dummy#F> ObjectSomeValuesFrom(<file://dummy#S> owl:Thing))");
		control.add("SubClassOf(ObjectSomeValuesFrom(<file://dummy#S> owl:Thing) <file://dummy#H>)");
		control.add("SubClassOf(<file://dummy#G> <file://dummy#H>)");
		
		assertTrue(TestUtility.compareCollections(actual, control));
	}
	
	@Test
	public void RunningExampleFactInsep1Test() throws OWLOntologyCreationException, JRDFStoreException {
		OWLDataFactory factory = new OWLDataFactoryImpl();
		
		OWLClass a = factory.getOWLClass(IRI.create(iri+"A"));
		OWLClass b = factory.getOWLClass(IRI.create(iri+"B"));
		OWLClass c = factory.getOWLClass(IRI.create(iri+"C"));
		OWLClass d = factory.getOWLClass(IRI.create(iri+"D"));
		OWLClass e = factory.getOWLClass(IRI.create(iri+"E"));
		OWLClass f = factory.getOWLClass(IRI.create(iri+"F"));
		OWLClass g = factory.getOWLClass(IRI.create(iri+"G"));
		OWLClass h = factory.getOWLClass(IRI.create(iri+"H"));
		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create(iri+"R"));
		OWLObjectProperty s = factory.getOWLObjectProperty(IRI.create(iri+"S"));
		OWLNamedIndividual o = factory.getOWLNamedIndividual(IRI.create(iri+"o"));
		OWLAxiom ax1 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(r, b));
		OWLAxiom ax2 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectHasValue(r, o));
		OWLAxiom ax3 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectIntersectionOf(b, c), d);
		OWLAxiom ax4 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(r, c), e);
		OWLAxiom ax5 = factory.getOWLSubClassOfAxiom(d, factory.getOWLObjectUnionOf(f, g));
		OWLAxiom ax6 = factory.getOWLSubClassOfAxiom(f, factory.getOWLObjectSomeValuesFrom(s, factory.getOWLThing()));
		OWLAxiom ax7 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(s, factory.getOWLThing()), h);
		OWLAxiom ax8 = factory.getOWLSubClassOfAxiom(g, h);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology(IRI.create(iri.replace("#", ".owl")));
		manager.addAxiom(ont, ax1);
		manager.addAxiom(ont, ax2);
		manager.addAxiom(ont, ax3);
		manager.addAxiom(ont, ax4);
		manager.addAxiom(ont, ax5);
		manager.addAxiom(ont, ax6);
		manager.addAxiom(ont, ax7);
		manager.addAxiom(ont, ax8);

		
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
		signature.add(b);
		signature.add(c);
		signature.add(d);
		signature.add(h);
		
		PrisM extractor = new PrisM(ont, InseparabilityRelation.FACT_INSEPARABILITY);
		
		Set<String> actual = new HashSet<String>();
		for (OWLAxiom ax : extractor.extract(signature)){
//			System.out.println(ax.toString());
			actual.add(ax.toString());
		}
//		System.out.println();
		Set<String> control = new HashSet<String>();
		control.add("SubClassOf(ObjectIntersectionOf(<file://dummy#B> <file://dummy#C>) <file://dummy#D>)");
		control.add("SubClassOf(<file://dummy#D> ObjectUnionOf(<file://dummy#F> <file://dummy#G>))");
		control.add("SubClassOf(<file://dummy#F> ObjectSomeValuesFrom(<file://dummy#S> owl:Thing))");
		control.add("SubClassOf(ObjectSomeValuesFrom(<file://dummy#S> owl:Thing) <file://dummy#H>)");
		control.add("SubClassOf(<file://dummy#G> <file://dummy#H>)");
		
		assertTrue(TestUtility.compareCollections(actual, control));
	}
	
	@Test
	public void RunningExampleFactInsep2Test() throws OWLOntologyCreationException, JRDFStoreException {
		OWLDataFactory factory = new OWLDataFactoryImpl();
		
		OWLClass a = factory.getOWLClass(IRI.create(iri+"A"));
		OWLClass b = factory.getOWLClass(IRI.create(iri+"B"));
		OWLClass c = factory.getOWLClass(IRI.create(iri+"C"));
		OWLClass d = factory.getOWLClass(IRI.create(iri+"D"));
		OWLClass e = factory.getOWLClass(IRI.create(iri+"E"));
		OWLClass f = factory.getOWLClass(IRI.create(iri+"F"));
		OWLClass g = factory.getOWLClass(IRI.create(iri+"G"));
		OWLClass h = factory.getOWLClass(IRI.create(iri+"H"));
		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create(iri+"R"));
		OWLObjectProperty s = factory.getOWLObjectProperty(IRI.create(iri+"S"));
		OWLNamedIndividual o = factory.getOWLNamedIndividual(IRI.create(iri+"o"));
		OWLAxiom ax1 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(r, b));
		OWLAxiom ax2 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectHasValue(r, o));
		OWLAxiom ax3 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectIntersectionOf(b, c), d);
		OWLAxiom ax4 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(r, c), e);
		OWLAxiom ax5 = factory.getOWLSubClassOfAxiom(d, factory.getOWLObjectUnionOf(f, g));
		OWLAxiom ax6 = factory.getOWLSubClassOfAxiom(f, factory.getOWLObjectSomeValuesFrom(s, factory.getOWLThing()));
		OWLAxiom ax7 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(s, factory.getOWLThing()), h);
		OWLAxiom ax8 = factory.getOWLSubClassOfAxiom(g, h);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology(IRI.create(iri.replace("#", ".owl")));
		manager.addAxiom(ont, ax1);
		manager.addAxiom(ont, ax2);
		manager.addAxiom(ont, ax3);
		manager.addAxiom(ont, ax4);
		manager.addAxiom(ont, ax5);
		manager.addAxiom(ont, ax6);
		manager.addAxiom(ont, ax7);
		manager.addAxiom(ont, ax8);

		
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
		signature.add(a);
		signature.add(c);
		signature.add(e);
		
		PrisM extractor = new PrisM(ont, InseparabilityRelation.FACT_INSEPARABILITY);
		
		Set<String> actual = new HashSet<String>();
		for (OWLAxiom ax : extractor.extract(signature)){
//			System.out.println(ax.toString());
			actual.add(ax.toString());
		}
//		System.out.println();
		Set<String> control = new HashSet<String>();
		control.add("SubClassOf(<file://dummy#A> ObjectHasValue(<file://dummy#R> <file://dummy#o>))");
		control.add("SubClassOf(ObjectSomeValuesFrom(<file://dummy#R> <file://dummy#C>) <file://dummy#E>)");
		
		assertTrue(TestUtility.compareCollections(actual, control));
	}
	
	@Test
	public void RunningExampleCQInsepTest() throws OWLOntologyCreationException, JRDFStoreException {
		OWLDataFactory factory = new OWLDataFactoryImpl();
		
		OWLClass a = factory.getOWLClass(IRI.create(iri+"A"));
		OWLClass b = factory.getOWLClass(IRI.create(iri+"B"));
		OWLClass c = factory.getOWLClass(IRI.create(iri+"C"));
		OWLClass d = factory.getOWLClass(IRI.create(iri+"D"));
		OWLClass e = factory.getOWLClass(IRI.create(iri+"E"));
		OWLClass f = factory.getOWLClass(IRI.create(iri+"F"));
		OWLClass g = factory.getOWLClass(IRI.create(iri+"G"));
		OWLClass h = factory.getOWLClass(IRI.create(iri+"H"));
		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create(iri+"R"));
		OWLObjectProperty s = factory.getOWLObjectProperty(IRI.create(iri+"S"));
		OWLNamedIndividual o = factory.getOWLNamedIndividual(IRI.create(iri+"o"));
		OWLAxiom ax1 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(r, b));
		OWLAxiom ax2 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectHasValue(r, o));
		OWLAxiom ax3 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectIntersectionOf(b, c), d);
		OWLAxiom ax4 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(r, c), e);
		OWLAxiom ax5 = factory.getOWLSubClassOfAxiom(d, factory.getOWLObjectUnionOf(f, g));
		OWLAxiom ax6 = factory.getOWLSubClassOfAxiom(f, factory.getOWLObjectSomeValuesFrom(s, factory.getOWLThing()));
		OWLAxiom ax7 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(s, factory.getOWLThing()), h);
		OWLAxiom ax8 = factory.getOWLSubClassOfAxiom(g, h);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology(IRI.create(iri.replace("#", ".owl")));
		manager.addAxiom(ont, ax1);
		manager.addAxiom(ont, ax2);
		manager.addAxiom(ont, ax3);
		manager.addAxiom(ont, ax4);
		manager.addAxiom(ont, ax5);
		manager.addAxiom(ont, ax6);
		manager.addAxiom(ont, ax7);
		manager.addAxiom(ont, ax8);

		
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
		signature.add(a);
		signature.add(b);
		
		PrisM extractor = new PrisM(ont, InseparabilityRelation.QUERY_INSEPARABILITY);
		
		Set<String> actual = new HashSet<String>();
		for (OWLAxiom ax : extractor.extract(signature)){
//			System.out.println(ax.toString());
			actual.add(ax.toString());
		}
//		System.out.println();
		Set<String> control = new HashSet<String>();
		control.add("SubClassOf(<file://dummy#A> ObjectSomeValuesFrom(<file://dummy#R> <file://dummy#B>))");
		
		assertTrue(TestUtility.compareCollections(actual, control));
	}
	@Test
	public void RunningExampleWeakCQInsepTest() throws OWLOntologyCreationException, JRDFStoreException {
		OWLDataFactory factory = new OWLDataFactoryImpl();
		
		OWLClass a = factory.getOWLClass(IRI.create(iri+"A"));
		OWLClass b = factory.getOWLClass(IRI.create(iri+"B"));
		OWLClass c = factory.getOWLClass(IRI.create(iri+"C"));
		OWLClass d = factory.getOWLClass(IRI.create(iri+"D"));
		OWLClass e = factory.getOWLClass(IRI.create(iri+"E"));
		OWLClass f = factory.getOWLClass(IRI.create(iri+"F"));
		OWLClass g = factory.getOWLClass(IRI.create(iri+"G"));
		OWLClass h = factory.getOWLClass(IRI.create(iri+"H"));
		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create(iri+"R"));
		OWLObjectProperty s = factory.getOWLObjectProperty(IRI.create(iri+"S"));
		OWLNamedIndividual o = factory.getOWLNamedIndividual(IRI.create(iri+"o"));
		OWLAxiom ax1 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(r, b));
		OWLAxiom ax2 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectHasValue(r, o));
		OWLAxiom ax3 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectIntersectionOf(b, c), d);
		OWLAxiom ax4 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(r, c), e);
		OWLAxiom ax5 = factory.getOWLSubClassOfAxiom(d, factory.getOWLObjectUnionOf(f, g));
		OWLAxiom ax6 = factory.getOWLSubClassOfAxiom(f, factory.getOWLObjectSomeValuesFrom(s, factory.getOWLThing()));
		OWLAxiom ax7 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(s, factory.getOWLThing()), h);
		OWLAxiom ax8 = factory.getOWLSubClassOfAxiom(g, h);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology(IRI.create(iri.replace("#", ".owl")));
		manager.addAxiom(ont, ax1);
		manager.addAxiom(ont, ax2);
		manager.addAxiom(ont, ax3);
		manager.addAxiom(ont, ax4);
		manager.addAxiom(ont, ax5);
		manager.addAxiom(ont, ax6);
		manager.addAxiom(ont, ax7);
		manager.addAxiom(ont, ax8);

		
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
		signature.add(a);
		signature.add(b);
		
		PrisM extractor = new PrisM(ont, InseparabilityRelation.WEAK_QUERY_INSEPARABILITY);
		
		Set<String> actual = new HashSet<String>();
		for (OWLAxiom ax : extractor.extract(signature)){
//			System.out.println(ax.toString());
			actual.add(ax.toString());
		}
//		System.out.println();
		Set<String> control = new HashSet<String>();
		
		assertTrue(TestUtility.compareCollections(actual, control));
	}
	@Test
	public void OtherExampleWeakCQInsepTest() throws OWLOntologyCreationException, JRDFStoreException {
		OWLDataFactory factory = new OWLDataFactoryImpl();
		
		OWLClass a = factory.getOWLClass(IRI.create(iri+"A"));
		OWLClass b = factory.getOWLClass(IRI.create(iri+"B"));
		OWLClass c = factory.getOWLClass(IRI.create(iri+"C"));
		OWLClass d = factory.getOWLClass(IRI.create(iri+"D"));
		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create(iri+"R"));
		OWLNamedIndividual o = factory.getOWLNamedIndividual(IRI.create(iri+"o"));
		OWLNamedIndividual i = factory.getOWLNamedIndividual(IRI.create(iri+"i"));
		OWLAxiom ax1 = factory.getOWLClassAssertionAxiom(a, o);
		OWLAxiom ax2 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(r, b));
		OWLAxiom ax3 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(r, c));
		OWLAxiom ax4 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(r, factory.getOWLThing()), d);
		OWLAxiom ax5 = factory.getOWLSubClassOfAxiom(b,c);
		OWLAxiom ax6 = factory.getOWLClassAssertionAxiom(c,i);
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology(IRI.create(iri.replace("#", ".owl")));
		
		manager.addAxiom(ont, ax1);
		manager.addAxiom(ont, ax2);
		manager.addAxiom(ont, ax3);
		manager.addAxiom(ont, ax4);
		manager.addAxiom(ont, ax5);
		manager.addAxiom(ont, ax6);
		
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
		signature.add(a);
		signature.add(d);
		
		PrisM extractor = new PrisM(ont, InseparabilityRelation.WEAK_QUERY_INSEPARABILITY);
		
		Set<String> actual = new HashSet<String>();
		for (OWLAxiom ax : extractor.extract(signature)){
//			System.out.println(ax.toString());
			actual.add(ax.toString());
		}
//		System.out.println();
		Set<String> control = new HashSet<String>();
		control.add(ax1.toString());
		control.add(ax2.toString());
		control.add(ax3.toString());
		control.add(ax4.toString());
		
		assertTrue(TestUtility.compareCollections(actual, control));
	}
	
	
	
	@Test
	public void RunningExampleModelInsepTest() throws OWLOntologyCreationException, JRDFStoreException {
		OWLDataFactory factory = new OWLDataFactoryImpl();
		
		OWLClass a = factory.getOWLClass(IRI.create(iri+"A"));
		OWLClass b = factory.getOWLClass(IRI.create(iri+"B"));
		OWLClass c = factory.getOWLClass(IRI.create(iri+"C"));
		OWLClass d = factory.getOWLClass(IRI.create(iri+"D"));
		OWLClass e = factory.getOWLClass(IRI.create(iri+"E"));
		OWLClass f = factory.getOWLClass(IRI.create(iri+"F"));
		OWLClass g = factory.getOWLClass(IRI.create(iri+"G"));
		OWLClass h = factory.getOWLClass(IRI.create(iri+"H"));
		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create(iri+"R"));
		OWLObjectProperty s = factory.getOWLObjectProperty(IRI.create(iri+"S"));
		OWLNamedIndividual o = factory.getOWLNamedIndividual(IRI.create(iri+"o"));
		OWLAxiom ax1 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(r, b));
		OWLAxiom ax2 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectHasValue(r, o));
		OWLAxiom ax3 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectIntersectionOf(b, c), d);
		OWLAxiom ax4 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(r, c), e);
		OWLAxiom ax5 = factory.getOWLSubClassOfAxiom(d, factory.getOWLObjectUnionOf(f, g));
		OWLAxiom ax6 = factory.getOWLSubClassOfAxiom(f, factory.getOWLObjectSomeValuesFrom(s, factory.getOWLThing()));
		OWLAxiom ax7 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(s, factory.getOWLThing()), h);
		OWLAxiom ax8 = factory.getOWLSubClassOfAxiom(g, h);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology(IRI.create(iri.replace("#", ".owl")));
		manager.addAxiom(ont, ax1);
		manager.addAxiom(ont, ax2);
		manager.addAxiom(ont, ax3);
		manager.addAxiom(ont, ax4);
		manager.addAxiom(ont, ax5);
		manager.addAxiom(ont, ax6);
		manager.addAxiom(ont, ax7);
		manager.addAxiom(ont, ax8);

		
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
		signature.add(a);
		signature.add(c);
		signature.add(d);
		signature.add(r);
		
		PrisM extractor = new PrisM(ont, InseparabilityRelation.MODEL_INSEPARABILITY);
		
		Set<String> actual = new HashSet<String>();
		for (OWLAxiom ax : extractor.extract(signature)){
//			System.out.println(ax.toString());
			actual.add(ax.toString());
		}
//		System.out.println();
		Set<String> control = new HashSet<String>();
		control.add("SubClassOf(<file://dummy#A> ObjectSomeValuesFrom(<file://dummy#R> <file://dummy#B>))");
		control.add("SubClassOf(<file://dummy#A> ObjectHasValue(<file://dummy#R> <file://dummy#o>))");
		control.add("SubClassOf(ObjectIntersectionOf(<file://dummy#B> <file://dummy#C>) <file://dummy#D>)");
		
		assertTrue(TestUtility.compareCollections(actual, control));
	}
	
	
	@Test
	public void RunningExampleClassificationInsepTest() throws OWLOntologyCreationException, JRDFStoreException {
		OWLDataFactory factory = new OWLDataFactoryImpl();
		
		OWLClass a = factory.getOWLClass(IRI.create(iri+"A"));
		OWLClass b = factory.getOWLClass(IRI.create(iri+"B"));
		OWLClass c = factory.getOWLClass(IRI.create(iri+"C"));
		OWLClass d = factory.getOWLClass(IRI.create(iri+"D"));
		OWLClass e = factory.getOWLClass(IRI.create(iri+"E"));
		OWLClass f = factory.getOWLClass(IRI.create(iri+"F"));
		OWLClass g = factory.getOWLClass(IRI.create(iri+"G"));
		OWLClass h = factory.getOWLClass(IRI.create(iri+"H"));
		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create(iri+"R"));
		OWLObjectProperty s = factory.getOWLObjectProperty(IRI.create(iri+"S"));
		OWLNamedIndividual o = factory.getOWLNamedIndividual(IRI.create(iri+"o"));
		OWLAxiom ax1 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(r, b));
		OWLAxiom ax2 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectHasValue(r, o));
		OWLAxiom ax3 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectIntersectionOf(b, c), d);
		OWLAxiom ax4 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(r, c), e);
		OWLAxiom ax5 = factory.getOWLSubClassOfAxiom(d, factory.getOWLObjectUnionOf(f, g));
		OWLAxiom ax6 = factory.getOWLSubClassOfAxiom(f, factory.getOWLObjectSomeValuesFrom(s, factory.getOWLThing()));
		OWLAxiom ax7 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(s, factory.getOWLThing()), h);
		OWLAxiom ax8 = factory.getOWLSubClassOfAxiom(g, h);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology(IRI.create(iri.replace("#", ".owl")));
		manager.addAxiom(ont, ax1);
		manager.addAxiom(ont, ax2);
		manager.addAxiom(ont, ax3);
		manager.addAxiom(ont, ax4);
		manager.addAxiom(ont, ax5);
		manager.addAxiom(ont, ax6);
		manager.addAxiom(ont, ax7);
		manager.addAxiom(ont, ax8);

		
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
		signature.add(g);
		
		PrisM extractor = new PrisM(ont, InseparabilityRelation.CLASSIFICATION_INSEPARABILITY);
		
		Set<String> actual = new HashSet<String>();
		for (OWLAxiom ax : extractor.extract(signature)){
//			System.out.println(ax.toString());
			actual.add(ax.toString());
		}
//		System.out.println();
		Set<String> control = new HashSet<String>();
		control.add("SubClassOf(<file://dummy#G> <file://dummy#H>)");
		
		assertTrue(TestUtility.compareCollections(actual, control));
	}

	@Test
	public void ExampleWithNominalsConceptClassificationTest() throws OWLOntologyCreationException, JRDFStoreException {
		OWLDataFactory factory = new OWLDataFactoryImpl();
		
		OWLClass a = factory.getOWLClass(IRI.create(iri+"A"));
		OWLClass b = factory.getOWLClass(IRI.create(iri+"B"));
		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create(iri+"R"));
		OWLNamedIndividual o = factory.getOWLNamedIndividual(IRI.create(iri+"o"));
		OWLNamedIndividual i = factory.getOWLNamedIndividual(IRI.create(iri+"i"));
		
		OWLAxiom ax1 = factory.getOWLSubClassOfAxiom(a,factory.getOWLObjectOneOf(o));
		OWLAxiom ax2 = factory.getOWLObjectPropertyAssertionAxiom(r, o, i);
		OWLAxiom ax3 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectHasValue(r, i), b);
		OWLAxiom ax4 = factory.getOWLSubClassOfAxiom(a,factory.getOWLObjectHasValue(r, i));
		
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology(IRI.create(iri.replace("#", ".owl")));
		
		manager.addAxiom(ont, ax1);
		manager.addAxiom(ont, ax2);
		manager.addAxiom(ont, ax3);
		manager.addAxiom(ont, ax4);
		
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
		signature.add(a);

		PrisM extractor = new PrisM(ont, InseparabilityRelation.CLASSIFICATION_INSEPARABILITY);
		
		Set<String> actual = new HashSet<String>();
		for (OWLAxiom ax : extractor.extract(signature)){
			actual.add(ax.toString());
//			System.out.println(ax.toString());
		}
		extractor.finishDisposal();
		Set<String> control = new HashSet<String>();
		control.add(ax1.toString());
		control.add(ax2.toString());
		control.add(ax3.toString());
		control.add(ax4.toString());
		
		assertTrue(TestUtility.compareCollections(actual, control));
	}
	

}
