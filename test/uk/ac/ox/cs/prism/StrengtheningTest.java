package uk.ac.ox.cs.prism;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.pagoda.constraints.BottomStrategy;
import uk.ac.ox.cs.pagoda.constraints.UnaryBottom;
import uk.ac.ox.cs.pagoda.hermit.RuleHelper;
import uk.ac.ox.cs.prism.PrisM.InseparabilityRelation;
import uk.ac.ox.cs.prism.clausification.DatatypeManager;
import uk.ac.ox.cs.prism.clausification.OWLNormalization_withMaps;

public class StrengtheningTest {

	String iri = "file://dummy#";	
	OWLDataFactory factory = new OWLDataFactoryImpl();
	OWLClass a, b, c, d, e;
	OWLNamedIndividual i, j;
	OWLObjectProperty r, p, s;
	OWLDataProperty dp;
	OWLDatatype datatype;
	OWLDataRange dataRange, dataRange2;
	OWLAxiom ax1, ax2, ax3, ax4, ax5, ax6, ax7, ax8, ax9, ax10, ax11, ax12, ax13, ax14, ax15, ax16, ax17, ax18, ax19,
	ax20, ax21, ax22, ax23, ax24, ax25, ax26, ax27, ax28, ax29, ax30, ax31, ax32, ax33, ax34;
	OWLNormalization_withMaps normalization = null;
	
	@Test
	public void test() {
		a = factory.getOWLClass(IRI.create(iri+"A"));
		b = factory.getOWLClass(IRI.create(iri+"B"));
		c = factory.getOWLClass(IRI.create(iri+"C"));
		d = factory.getOWLClass(IRI.create(iri+"D"));
		e = factory.getOWLClass(IRI.create(iri+"E"));
		i = factory.getOWLNamedIndividual(IRI.create(iri+"i"));
		j = factory.getOWLNamedIndividual(IRI.create(iri+"j"));
		r = factory.getOWLObjectProperty(IRI.create(iri+"R")); 
		p = factory.getOWLObjectProperty(IRI.create(iri+"P")); 
		s = factory.getOWLObjectProperty(IRI.create(iri+"S"));
		dp = factory.getOWLDataProperty(IRI.create(iri+"dataP"));
		datatype = factory.getOWLDatatype(IRI.create(iri+"datatype"));
		dataRange = factory.getOWLDataUnionOf(factory.getIntegerOWLDatatype(), factory.getFloatOWLDatatype());
		dataRange2 = factory.getOWLDataIntersectionOf(factory.getIntegerOWLDatatype(), factory.getFloatOWLDatatype());
		ax1 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(r, factory.getOWLObjectIntersectionOf(b,c))); 
		ax2 = factory.getOWLEquivalentClassesAxiom(factory.getOWLObjectUnionOf(a,d),factory.getOWLObjectSomeValuesFrom(r, factory.getOWLObjectIntersectionOf(b,c)));
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
		ax18 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectMaxCardinality(4, r));
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
		ax30 = factory.getOWLSubClassOfAxiom(c,factory.getOWLDataSomeValuesFrom(dp, factory.getTopDatatype()));
		ax31 = factory.getOWLSubClassOfAxiom(factory.getOWLDataHasValue(dp, factory.getOWLLiteral(8)), b);
		ax32 = factory.getOWLSubClassOfAxiom(a, factory.getOWLDataMinCardinality(3, dp, factory.getBooleanOWLDatatype()));
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
		axiomSet.add(ax30);
		axiomSet.add(ax31);
		axiomSet.add(ax32);
		axiomSet.add(ax33);
		
		try {
			OWLOntology o = OWLManager.createOWLOntologyManager().createOntology(axiomSet);
			DatalogStrengthening program = new DatalogStrengthening(new IndividualManager(InseparabilityRelation.IMPLICATION_INSEPARABILITY), new DatatypeManager());
			BottomStrategy bottomStrategy = new UnaryBottom();
			program.load(o, bottomStrategy);

			for (Entry<DLClause,Object> entry : program.correspondence.entrySet()) {
				System.out.println(RuleHelper.getText(entry.getKey()) + "\t\t\t" + entry.getValue());
			}
			System.out.println();
			
			for (Entry<OWLIndividualAxiom,Collection<OWLAxiom>> entry : program.aBoxCorrespondence.entrySet()) {
				System.out.println(entry.getKey() + "\t\t\t" + entry.getValue());
			}
			System.out.println();
			
			for (DLClause clause : program.additionalClauses) {
				System.out.println(RuleHelper.getText(clause));
			}
		
		} catch (OWLOntologyCreationException e1) {
			e1.printStackTrace();
		}
		
		
//		2015/11/06 17:50:54 WARN  tmextractor- Unsupported construct: DataIntersectionOf(xsd:float xsd:integer )
//		2015/11/06 17:50:54 WARN  tmextractor- Unsupported construct: DatatypeDefinition(<file://dummy#datatype> DataUnionOf(xsd:float xsd:integer ))
//		2015/11/06 17:50:55 INFO  - SimpleETL rewriting DONE
//		additional ontology data is saved in /Users/Ana/Documents/Work/DatalogModules/PrisM/tmp/ABox.ttl.
//		owl:differentFrom(prefix0:skolem6,prefix0:skolem7) :- owl:Thing(?X).			<file://dummy#B>(X) v atLeast(5 <file://dummy#R> owl:Thing)(X) :- owl:Thing(X)
//		prefix1:dataP(?X,"2"^^xsd:integer) :- aux:NC0(?X).			atLeast(1 <file://dummy#dataP> { "2"^^xsd:integer })(X) v atLeast(1 <file://dummy#dataP> { "6"^^xsd:integer })(X) :- <http://www.cs.ox.ac.uk/PAGOdA/auxiliary#NC0>(X)
//		prefix1:P(?Y,?X) :- prefix1:P(?X,?Y).			<file://dummy#P>(Y,X) :- <file://dummy#P>(X,Y)
//		prefix1:dataP(?X,"true"^^xsd:boolean) :- prefix1:C(?X).			atLeast(1 <file://dummy#dataP> xsd:boolean)(X) :- <file://dummy#C>(X)
//		prefix1:B(?X) :- prefix1:R(?X,?Y4).			<file://dummy#B>(X) v [Y1 == Y2]@atMost(3 <file://dummy#R> owl:Thing)(X) v [Y1 == Y3]@atMost(3 <file://dummy#R> owl:Thing)(X) v [Y1 == Y4]@atMost(3 <file://dummy#R> owl:Thing)(X) v [Y2 == Y3]@atMost(3 <file://dummy#R> owl:Thing)(X) v [Y2 == Y4]@atMost(3 <file://dummy#R> owl:Thing)(X) v [Y3 == Y4]@atMost(3 <file://dummy#R> owl:Thing)(X) :- <file://dummy#R>(X,Y1), <file://dummy#R>(X,Y2), <file://dummy#R>(X,Y3), <file://dummy#R>(X,Y4), Y1 <= Y2, Y2 <= Y3, Y3 <= Y4, NodeIDsAscendingOrEqual(Y1,Y2,Y3,Y4)
//		owl:sameAs(?Y1,?Y2) :- prefix1:P(?X,?Y1), prefix1:P(?X,?Y2).			[Y1 == Y2]@atMost(1 <file://dummy#P> owl:Thing)(X) :- <file://dummy#P>(X,Y1), <file://dummy#P>(X,Y2)
//		prefix1:dataP(?X,"6"^^xsd:integer) :- aux:NC0(?X).			atLeast(1 <file://dummy#dataP> { "2"^^xsd:integer })(X) v atLeast(1 <file://dummy#dataP> { "6"^^xsd:integer })(X) :- <http://www.cs.ox.ac.uk/PAGOdA/auxiliary#NC0>(X)
//		owl:sameAs(?Y,"2"^^xsd:integer) :- prefix1:dataP(?X,?Y), prefix1:A(?X).			Y == "2"^^xsd:integer v Y == "6"^^xsd:integer :- <file://dummy#A>(X), <file://dummy#dataP>(X,Y)
//		owl:Nothing(prefix1:i) :- prefix1:dataP(prefix1:i,"2"^^xsd:integer).			[NegativeDataPropertyAssertion(<file://dummy#dataP> <file://dummy#i> "2"^^xsd:integer)]
//		owl:sameAs(?Y,?Z) :- prefix1:S(?X,?Y), prefix1:A(?X).			Y == Z v Y == Z1 :- <file://dummy#A>(X), <file://dummy#S>(X,Y), <internal:nom#file://dummy#i>(Z), <internal:nom#file://dummy#j>(Z1)
//		prefix1:B(?X) :- prefix1:dataP(?X,?Y).			[<file://dummy#B>(X) v not({ "8"^^xsd:integer })(Y) :- <file://dummy#dataP>(X,Y), <file://dummy#B>(X) v not({ "2"^^xsd:integer "6"^^xsd:integer })(Y) :- <file://dummy#dataP>(X,Y)]
//		prefix1:dataP(?X,"a"^^xsd:string) :- prefix1:C(?X).			atLeast(1 <file://dummy#dataP> rdfs:Literal)(X) :- <file://dummy#C>(X)
//		prefix2:2(?X) :- prefix1:D(?X).			<internal:def#2>(X) :- <file://dummy#D>(X)
//		prefix1:R(?X,prefix0:skolem2) :- prefix1:A(?X).			atLeast(4 <file://dummy#R> owl:Thing)(X) :- <file://dummy#A>(X)
//		owl:sameAs(?Y1,?Y2) :- prefix1:R(?X,?Y1), prefix1:A(?X), prefix1:R(?X,?Y2).			[Y1 == Y2]@atMost(4 <file://dummy#R> owl:Thing)(X) v [Y1 == Y3]@atMost(4 <file://dummy#R> owl:Thing)(X) v [Y1 == Y4]@atMost(4 <file://dummy#R> owl:Thing)(X) v [Y1 == Y5]@atMost(4 <file://dummy#R> owl:Thing)(X) v [Y2 == Y3]@atMost(4 <file://dummy#R> owl:Thing)(X) v [Y2 == Y4]@atMost(4 <file://dummy#R> owl:Thing)(X) v [Y2 == Y5]@atMost(4 <file://dummy#R> owl:Thing)(X) v [Y3 == Y4]@atMost(4 <file://dummy#R> owl:Thing)(X) v [Y3 == Y5]@atMost(4 <file://dummy#R> owl:Thing)(X) v [Y4 == Y5]@atMost(4 <file://dummy#R> owl:Thing)(X) :- <file://dummy#A>(X), <file://dummy#R>(X,Y1), <file://dummy#R>(X,Y2), <file://dummy#R>(X,Y3), <file://dummy#R>(X,Y4), <file://dummy#R>(X,Y5), Y1 <= Y2, Y2 <= Y3, Y3 <= Y4, Y4 <= Y5, NodeIDsAscendingOrEqual(Y1,Y2,Y3,Y4,Y5)
//		prefix1:R(?X,prefix1:j) :- aux:NC2(?X).			<file://dummy#R>(X,Z) v <file://dummy#R>(X,Z1) :- <http://www.cs.ox.ac.uk/PAGOdA/auxiliary#NC2>(X), <internal:nom#file://dummy#i>(Z), <internal:nom#file://dummy#j>(Z1)
//		owl:Nothing(?X) :- prefix1:dataP(?X,?Y), BIND(DATATYPE(?Y) AS ?Z), FILTER(?Z != xsd:boolean).			xsd:boolean(Y) :- <file://dummy#dataP>(X,Y)
//		prefix1:B(?X) :- owl:Thing(?X).			<file://dummy#B>(X) v atLeast(5 <file://dummy#R> owl:Thing)(X) :- owl:Thing(X)
//		owl:Nothing(?X) :- prefix1:E(?X), aux:NC1(?X).			 :- <file://dummy#E>(X), <http://www.cs.ox.ac.uk/PAGOdA/auxiliary#NC1>(X)
//		prefix1:R(?Y,?X) :- prefix1:S(?X,?Y).			<file://dummy#R>(Y,X) :- <file://dummy#S>(X,Y)
//		owl:Nothing(?X) :- prefix1:A(?X).			atLeast(3 <file://dummy#dataP> xsd:boolean)(X) :- <file://dummy#A>(X)
//		prefix1:R(?X,prefix0:skolem3) :- prefix1:A(?X).			atLeast(4 <file://dummy#R> owl:Thing)(X) :- <file://dummy#A>(X)
//		prefix2:1(?X) :- prefix1:B(?X), prefix1:C(?X).			<internal:def#1>(X) :- <file://dummy#B>(X), <file://dummy#C>(X)
//		owl:sameAs(?Y1,?Y2) :- prefix1:R(?X,?Y1), prefix1:R(?X,?Y2).			<file://dummy#B>(X) v [Y1 == Y2]@atMost(3 <file://dummy#R> owl:Thing)(X) v [Y1 == Y3]@atMost(3 <file://dummy#R> owl:Thing)(X) v [Y1 == Y4]@atMost(3 <file://dummy#R> owl:Thing)(X) v [Y2 == Y3]@atMost(3 <file://dummy#R> owl:Thing)(X) v [Y2 == Y4]@atMost(3 <file://dummy#R> owl:Thing)(X) v [Y3 == Y4]@atMost(3 <file://dummy#R> owl:Thing)(X) :- <file://dummy#R>(X,Y1), <file://dummy#R>(X,Y2), <file://dummy#R>(X,Y3), <file://dummy#R>(X,Y4), Y1 <= Y2, Y2 <= Y3, Y3 <= Y4, NodeIDsAscendingOrEqual(Y1,Y2,Y3,Y4)
//		owl:differentFrom(prefix0:skolem2,prefix0:skolem3) :- prefix1:A(?X).			atLeast(4 <file://dummy#R> owl:Thing)(X) :- <file://dummy#A>(X)
//		prefix1:R(?X,prefix1:i) :- aux:NC2(?X).			<file://dummy#R>(X,Z) v <file://dummy#R>(X,Z1) :- <http://www.cs.ox.ac.uk/PAGOdA/auxiliary#NC2>(X), <internal:nom#file://dummy#i>(Z), <internal:nom#file://dummy#j>(Z1)
//		prefix1:B(?X) :- prefix2:0(?X).			<file://dummy#B>(X) :- <internal:def#0>(X)
//		prefix1:R(?X,prefix0:skolem7) :- owl:Thing(?X).			<file://dummy#B>(X) v atLeast(5 <file://dummy#R> owl:Thing)(X) :- owl:Thing(X)
//		prefix1:R(?X,prefix0:skolem1) :- prefix1:A(?X).			atLeast(1 <file://dummy#R> <internal:def#0>)(X) :- <file://dummy#A>(X)
//		prefix1:A(?X) :- prefix2:1(?Y), prefix1:R(?X,?Y).			<file://dummy#A>(X) v <file://dummy#D>(X) :- <file://dummy#R>(X,Y), <internal:def#1>(Y)
//		prefix2:0(prefix0:skolem1) :- prefix1:A(?X).			atLeast(1 <file://dummy#R> <internal:def#0>)(X) :- <file://dummy#A>(X)
//		prefix1:dataP(?X,"true"^^xsd:boolean) :- prefix1:A(?X).			atLeast(3 <file://dummy#dataP> xsd:boolean)(X) :- <file://dummy#A>(X)
//		prefix2:0(prefix0:skolem0) :- prefix2:2(?X).			atLeast(1 <file://dummy#R> <internal:def#0>)(X) :- <internal:def#2>(X)
//		prefix1:dataP(?X,"2"^^xsd:integer) :- prefix1:A(?X).			atLeast(1 <file://dummy#dataP> { "2"^^xsd:integer })(X) v atLeast(1 <file://dummy#dataP> { "6"^^xsd:integer })(X) :- <file://dummy#A>(X)
//		prefix1:dataP(?X,"5"^^xsd:integer) :- prefix1:C(?X).			atLeast(1 <file://dummy#dataP> { "5"^^xsd:integer })(X) :- <file://dummy#C>(X)
//		prefix1:R(?X,prefix0:skolem6) :- owl:Thing(?X).			<file://dummy#B>(X) v atLeast(5 <file://dummy#R> owl:Thing)(X) :- owl:Thing(X)
//		prefix1:C(?X) :- prefix2:0(?X).			<file://dummy#C>(X) :- <internal:def#0>(X)
//		owl:sameAs(?Y,"6"^^xsd:integer) :- prefix1:dataP(?X,?Y), prefix1:A(?X).			Y == "2"^^xsd:integer v Y == "6"^^xsd:integer :- <file://dummy#A>(X), <file://dummy#dataP>(X,Y)
//		prefix1:D(?X) :- prefix2:1(?Y), prefix1:R(?X,?Y).			<file://dummy#A>(X) v <file://dummy#D>(X) :- <file://dummy#R>(X,Y), <internal:def#1>(Y)
//		prefix2:2(?X) :- prefix1:A(?X).			<internal:def#2>(X) :- <file://dummy#A>(X)
//		prefix1:R(?X,prefix1:i) :- prefix1:A(?X).			<file://dummy#R>(X,Z) v <file://dummy#R>(X,Z1) :- <file://dummy#A>(X), <internal:nom#file://dummy#i>(Z), <internal:nom#file://dummy#j>(Z1)
//		prefix1:dataP(?X,"6"^^xsd:integer) :- prefix1:A(?X).			atLeast(1 <file://dummy#dataP> { "2"^^xsd:integer })(X) v atLeast(1 <file://dummy#dataP> { "6"^^xsd:integer })(X) :- <file://dummy#A>(X)
//		prefix1:S(?Y,?X) :- prefix1:R(?X,?Y).			<file://dummy#S>(Y,X) :- <file://dummy#R>(X,Y)
//		owl:Nothing(prefix1:i) :- prefix1:P(prefix1:j,prefix1:i).			[NegativeObjectPropertyAssertion(<file://dummy#P> <file://dummy#j> <file://dummy#i>)]
//		prefix1:R(?X,prefix0:skolem0) :- prefix2:2(?X).			atLeast(1 <file://dummy#R> <internal:def#0>)(X) :- <internal:def#2>(X)
//		owl:Nothing(?X) :- prefix1:A(?X), prefix1:dataP(?X,?Y), BIND(DATATYPE(?Y) AS ?Z), FILTER(?Z != xsd:integer).			xsd:integer(Y) :- <file://dummy#A>(X), <file://dummy#dataP>(X,Y)
//		prefix1:R(?X,prefix1:j) :- prefix1:A(?X).			<file://dummy#R>(X,Z) v <file://dummy#R>(X,Z1) :- <file://dummy#A>(X), <internal:nom#file://dummy#i>(Z), <internal:nom#file://dummy#j>(Z1)
//
//		ObjectPropertyAssertion(<file://dummy#P> <file://dummy#i> <file://dummy#j>)			[ObjectPropertyAssertion(<file://dummy#P> <file://dummy#i> <file://dummy#j>)]
//		ClassAssertion(<file://dummy#E> <file://dummy#i>)			[ClassAssertion(<file://dummy#E> <file://dummy#i>)]
//		ClassAssertion(<http://www.cs.ox.ac.uk/PAGOdA/auxiliary#NC2> <file://dummy#i>)			[ClassAssertion(<http://www.cs.ox.ac.uk/PAGOdA/auxiliary#NC2> <file://dummy#i>)]
//		ClassAssertion(<http://www.cs.ox.ac.uk/PAGOdA/auxiliary#NC1> <file://dummy#j>)			[ClassAssertion(<http://www.cs.ox.ac.uk/PAGOdA/auxiliary#NC1> <file://dummy#j>)]
//		ClassAssertion(<http://www.cs.ox.ac.uk/PAGOdA/auxiliary#NC0> <file://dummy#i>)			[ClassAssertion(<http://www.cs.ox.ac.uk/PAGOdA/auxiliary#NC0> <file://dummy#i>)]
//
//		owl:Nothing(?X) :- owl:differentFrom(?X,?X).
		
	}
	
	
	
	
	@Test
	public void removingTransitivityAxiomsTest_remove() throws OWLOntologyCreationException, JRDFStoreException {
		
		a = factory.getOWLClass(IRI.create(iri+"A"));
		b = factory.getOWLClass(IRI.create(iri+"B"));
		p = factory.getOWLObjectProperty(IRI.create(iri+"P")); 
		
		ax1 = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(p, b));
		ax2 = factory.getOWLTransitiveObjectPropertyAxiom(p);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology(IRI.create(iri.replace("#", ".owl")));
		manager.addAxiom(ont, ax1);
		manager.addAxiom(ont, ax2);
		
		
		DatalogStrengthening program = new DatalogStrengthening(new IndividualManager(InseparabilityRelation.IMPLICATION_INSEPARABILITY), new DatatypeManager());
		program.load(ont, new UnaryBottom());
		program.transform();
		
		for (DLClause clause : program.getClauses())
			if (transitivityClause(clause))
				Assert.assertTrue(false);
//		System.out.println(program.toString());//should not contain the clause corresponding to the transitivity axiom
			
	}
	
	@Test
	public void removingTransitivityAxiomsTest_keep() throws OWLOntologyCreationException, JRDFStoreException {
		a = factory.getOWLClass(IRI.create(iri+"A"));
		b = factory.getOWLClass(IRI.create(iri+"B"));
		p = factory.getOWLObjectProperty(IRI.create(iri+"P"));
		ax1 = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(p, b), a);
		ax2 = factory.getOWLTransitiveObjectPropertyAxiom(p);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology(IRI.create(iri.replace("#", ".owl")));
		manager.addAxiom(ont, ax1);
		manager.addAxiom(ont, ax2);
		
		
		DatalogStrengthening program = new DatalogStrengthening(new IndividualManager(InseparabilityRelation.IMPLICATION_INSEPARABILITY), new DatatypeManager());
		program.load(ont, new UnaryBottom());
		program.transform();
		
		boolean containsTransitivityClause = false;
		for (DLClause clause : program.getClauses())
			if (transitivityClause(clause))
				containsTransitivityClause = true;
		
		Assert.assertTrue(containsTransitivityClause);
//		System.out.println(program.toString());//should contain the clause corresponding to the transitivity axiom
			
	}
	
	protected boolean transitivityClause(DLClause clause) {
		Atom[] body = clause.getBodyAtoms();
		Atom[] head = clause.getHeadAtoms();
		boolean transitivity = body.length == 2 && head.length == 1 &&
				body[0].getDLPredicate().equals(body[1].getDLPredicate()) &&
				body[1].getDLPredicate().equals(body[1].getDLPredicate()) &&
				body[0].getArgument(0).equals(head[0].getArgument(0)) &&
				body[0].getArgument(1).equals(body[1].getArgument(0)) &&
				body[1].getArgument(1).equals(head[0].getArgument(1));
		return transitivity;
	}
}
