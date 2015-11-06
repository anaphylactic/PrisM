package uk.ac.ox.cs.prism.clausification;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class ClausificationTest {


	OWLDataFactory factory = new OWLDataFactoryImpl();
	OWLClass a, b, c, d, e;
	OWLNamedIndividual i, j;
	OWLObjectProperty r, p, s;
	OWLDataProperty dp;
	OWLDatatype datatype;
	OWLDataRange dataRange, dataRange2;
	OWLAxiom ax1, ax2, ax3, ax4, ax5, ax6, ax7, ax8, ax9, ax10, ax11, ax12, ax13, ax14, ax15, ax16, 
	ax17, ax18, ax19, ax20, ax21, ax22, ax23, ax24, ax25, ax26, ax27, ax28, ax29, ax30, ax31, ax32, ax33;
	OWLNormalization_withMaps normalization = null;

	@Test
	public void clausificationTest() {

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



		test1();
		
	}


	public void test1() {
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
		ax14 = factory.getOWLFunctionalObjectPropertyAxiom(p);
		ax15 = factory.getOWLDifferentIndividualsAxiom(i,j);
		ax16 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectMinCardinality(4, r));
		ax17 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectMinCardinality(4, r), b);
		ax18 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectMaxCardinality(2, r));
		ax19 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectMaxCardinality(4, r), b);
		ax20 = factory.getOWLSubClassOfAxiom(a,factory.getOWLDataSomeValuesFrom(dp, factory.getOWLDataOneOf(factory.getOWLLiteral(2), factory.getOWLLiteral(6))));
		ax21 = factory.getOWLSubClassOfAxiom(a,factory.getOWLDataAllValuesFrom(dp, factory.getOWLDataOneOf(factory.getOWLLiteral(2), factory.getOWLLiteral(6))));
		ax22 = factory.getOWLClassAssertionAxiom(factory.getOWLDataSomeValuesFrom(dp, factory.getOWLDataOneOf(factory.getOWLLiteral(2), factory.getOWLLiteral(6))), i);
		ax23 = factory.getOWLSubClassOfAxiom(c,factory.getOWLDataSomeValuesFrom(dp, factory.getOWLDataOneOf(factory.getOWLLiteral(5))));
		ax23 = factory.getOWLSubClassOfAxiom(c,factory.getOWLDataHasValue(dp, factory.getOWLLiteral(5)));
		ax24 = factory.getOWLClassAssertionAxiom(factory.getOWLObjectSomeValuesFrom(r, factory.getOWLObjectOneOf(i, j)), i);
		ax25= factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(r, factory.getOWLObjectOneOf(i, j)));
		ax26 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectAllValuesFrom(s, factory.getOWLObjectOneOf(i, j)));
		ax27 = factory.getOWLSubClassOfAxiom(factory.getOWLDataSomeValuesFrom(dp, factory.getOWLDataOneOf(factory.getOWLLiteral(2), factory.getOWLLiteral(6))),b);
		ax28 = factory.getOWLSubClassOfAxiom(a,factory.getOWLDataAllValuesFrom(dp, factory.getIntegerOWLDatatype()));
		ax29 = factory.getOWLSubClassOfAxiom(c,factory.getOWLDataSomeValuesFrom(dp, factory.getBooleanOWLDatatype()));
		ax31 = factory.getOWLSubClassOfAxiom(factory.getOWLDataSomeValuesFrom(dp, factory.getIntegerOWLDatatype()), b);
		ax32 = factory.getOWLSubClassOfAxiom(factory.getOWLDataSomeValuesFrom(dp, factory.getTopDatatype()), b);
		ax33 = factory.getOWLDataPropertyRangeAxiom(dp, factory.getBooleanOWLDatatype());
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
		axiomSet.add(ax14);
		axiomSet.add(ax15);
		axiomSet.add(ax16);
		axiomSet.add(ax17);
		axiomSet.add(ax18);
		axiomSet.add(ax19);
		axiomSet.add(ax20);
		axiomSet.add(ax21);
		axiomSet.add(ax22);
		axiomSet.add(ax23);
		axiomSet.add(ax24);
		axiomSet.add(ax25);
		axiomSet.add(ax26);
		axiomSet.add(ax27);
		axiomSet.add(ax28);
		axiomSet.add(ax29);
		axiomSet.add(ax31);
		axiomSet.add(ax32);
		axiomSet.add(ax33);
		try {
			OWLOntology o = OWLManager.createOWLOntologyManager().createOntology(axiomSet);
			OWLClausification_withMaps clausification = new OWLClausification_withMaps(new DatatypeManager());
			DLOntology_withMaps dlOntology = (DLOntology_withMaps) clausification.preprocessAndClausify(o);

//			m_dlClauses_map;
			for (Entry<DLClause,Collection<OWLAxiom>> entry : dlOntology.m_dlClauses_map.entrySet()) {
				System.out.println(entry.getKey() + "     " + entry.getValue());
			}
			
			System.out.println();
//			m_positiveFacts_map;
			for (Entry<Atom,Collection<OWLAxiom>> entry : dlOntology.m_positiveFacts_map.entrySet()) {
				System.out.println(entry.getKey() + "     " + entry.getValue());
			}

			System.out.println();
//			m_negativeFacts_map;
			for (Entry<Atom,Collection<OWLAxiom>> entry : dlOntology.m_negativeFacts_map.entrySet()) {
				System.out.println(entry.getKey() + "     " + entry.getValue());
			}

		} catch (OWLOntologyCreationException e1) {
			e1.printStackTrace();
		}
		
//		internal concepts may vary; reference:
//		2015/11/06 16:53:29 WARN  tmextractor- Unsupported construct: DataIntersectionOf(xsd:float xsd:integer )
//		2015/11/06 16:53:29 WARN  tmextractor- Unsupported construct: DatatypeDefinition(<datatype> DataUnionOf(xsd:float xsd:integer ))
//		atLeast(1 <R> <internal:def#1>)(X) :- <internal:def#3>(X)     [EquivalentClasses(ObjectUnionOf(<A> <D>) ObjectSomeValuesFrom(<R> ObjectIntersectionOf(<B> <C>)) )]
//		Y == Z v Y == Z1 :- <A>(X), <S>(X,Y), <internal:nom#j>(Z), <internal:nom#i>(Z1)     [SubClassOf(<A> ObjectAllValuesFrom(<S> ObjectOneOf(<i> <j>)))]
//		atLeast(1 <R> <internal:def#1>)(X) :- <A>(X)     [SubClassOf(<A> ObjectSomeValuesFrom(<R> ObjectIntersectionOf(<B> <C>)))]
//		<B>(X) :- <internal:def#1>(X)     []
//		<R>(Y,X) :- <S>(X,Y)     [InverseObjectProperties(<R> <S>)]
//		<internal:def#3>(X) :- <A>(X)     []
//		<A>(X) v <D>(X) :- <R>(X,Y), <internal:def#2>(Y)     [EquivalentClasses(ObjectUnionOf(<A> <D>) ObjectSomeValuesFrom(<R> ObjectIntersectionOf(<B> <C>)) )]
//		xsd:boolean(Y) :- <dataP>(X,Y)     [DataPropertyRange(<dataP> xsd:boolean)]
//		<B>(X) v not({ "2"^^xsd:integer "6"^^xsd:integer })(Y) :- <dataP>(X,Y)     [SubClassOf(DataSomeValuesFrom(<dataP> DataOneOf("2"^^xsd:integer "6"^^xsd:integer )) <B>)]
//		<R>(X,Z) v <R>(X,Z1) :- <A>(X), <internal:nom#j>(Z), <internal:nom#i>(Z1)     [SubClassOf(<A> ObjectSomeValuesFrom(<R> ObjectOneOf(<i> <j>)))]
//		<P>(Y,X) :- <P>(X,Y)     [SymmetricObjectProperty(<P>)]
//		<R>(X,Z) v <R>(X,Z1) :- <internal:def#0>(X), <internal:nom#j>(Z), <internal:nom#i>(Z1)     []
//		[Y1 == Y2]@atMost(1 <P> owl:Thing)(X) :- <P>(X,Y1), <P>(X,Y2)     [FunctionalObjectProperty(<P>)]
//		xsd:integer(Y) :- <A>(X), <dataP>(X,Y)     [SubClassOf(<A> DataAllValuesFrom(<dataP> xsd:integer))]
//		[Y1 == Y2]@atMost(2 <R> owl:Thing)(X) v [Y1 == Y3]@atMost(2 <R> owl:Thing)(X) v [Y2 == Y3]@atMost(2 <R> owl:Thing)(X) :- <A>(X), <R>(X,Y1), <R>(X,Y2), <R>(X,Y3), Y1 <= Y2, Y2 <= Y3, NodeIDsAscendingOrEqual(Y1,Y2,Y3)     [SubClassOf(<A> ObjectMaxCardinality(2 <R> owl:Thing))]
//		Y == "2"^^xsd:integer v Y == "6"^^xsd:integer :- <A>(X), <dataP>(X,Y)     [SubClassOf(<A> DataAllValuesFrom(<dataP> DataOneOf("2"^^xsd:integer "6"^^xsd:integer )))]
//		<internal:def#2>(X) :- <B>(X), <C>(X)     []
//		atLeast(1 <dataP> xsd:boolean)(X) :- <C>(X)     [SubClassOf(<C> DataSomeValuesFrom(<dataP> xsd:boolean))]
//		atLeast(4 <R> owl:Thing)(X) :- <A>(X)     [SubClassOf(<A> ObjectMinCardinality(4 <R> owl:Thing))]
//		atLeast(1 <dataP> { "2"^^xsd:integer })(X) v atLeast(1 <dataP> { "6"^^xsd:integer })(X) :- <A>(X)     [SubClassOf(<A> DataSomeValuesFrom(<dataP> DataOneOf("2"^^xsd:integer "6"^^xsd:integer )))]
//		<C>(X) :- <internal:def#1>(X)     []
//		<B>(X) v atLeast(5 <R> owl:Thing)(X) :- owl:Thing(X)     [SubClassOf(ObjectMaxCardinality(4 <R> owl:Thing) <B>)]
//		<B>(X) :- <dataP>(X,Y)     [SubClassOf(DataSomeValuesFrom(<dataP> rdfs:Literal) <B>)]
//		<B>(X) v not(xsd:integer)(Y) :- <dataP>(X,Y)     [SubClassOf(DataSomeValuesFrom(<dataP> xsd:integer) <B>)]
//		atLeast(1 <dataP> { "5"^^xsd:integer })(X) :- <C>(X)     [SubClassOf(<C> DataHasValue(<dataP> "5"^^xsd:integer))]
//		<B>(X) v [Y1 == Y2]@atMost(3 <R> owl:Thing)(X) v [Y1 == Y3]@atMost(3 <R> owl:Thing)(X) v [Y1 == Y4]@atMost(3 <R> owl:Thing)(X) v [Y2 == Y3]@atMost(3 <R> owl:Thing)(X) v [Y2 == Y4]@atMost(3 <R> owl:Thing)(X) v [Y3 == Y4]@atMost(3 <R> owl:Thing)(X) :- <R>(X,Y1), <R>(X,Y2), <R>(X,Y3), <R>(X,Y4), Y1 <= Y2, Y2 <= Y3, Y3 <= Y4, NodeIDsAscendingOrEqual(Y1,Y2,Y3,Y4)     [SubClassOf(ObjectMinCardinality(4 <R> owl:Thing) <B>)]
//		<internal:def#3>(X) :- <D>(X)     []
//		<S>(Y,X) :- <R>(X,Y)     [InverseObjectProperties(<R> <S>)]
//
//		<internal:def#0>(<i>)     [ClassAssertion(ObjectSomeValuesFrom(<R> ObjectOneOf(<i> <j>)) <i>)]
//		<internal:nom#i>(<i>)     []
//		<P>(<i>,<j>)     [ObjectPropertyAssertion(<P> <i> <j>)]
//		<i> == <j>     [SameIndividual(<i> <j> )]
//		<i> != <j>     [DifferentIndividuals(<i> <j> )]
//		<E>(<i>)     [ClassAssertion(<E> <i>)]
//		<internal:nom#j>(<j>)     []
//		<dataP>(<i>,"2"^^xsd:integer)     [DataPropertyAssertion(<dataP> <i> "2"^^xsd:integer)]
//
//		<P>(<j>,<i>)     [NegativeObjectPropertyAssertion(<P> <j> <i>)]
//		<E>(<j>)     [ClassAssertion(ObjectComplementOf(<E>) <j>)]
//		<dataP>(<i>,"2"^^xsd:integer)     [NegativeDataPropertyAssertion(<dataP> <i> "2"^^xsd:integer)]

	}
}
