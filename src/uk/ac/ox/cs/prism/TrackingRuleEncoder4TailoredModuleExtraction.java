package uk.ac.ox.cs.prism;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.DatatypeRestriction;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.store.DataStore;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;
import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Utility;
import uk.ac.ox.cs.prism.util.Utility_PrisM;


public class TrackingRuleEncoder4TailoredModuleExtraction {

	//Because in PAGOdA's encoding each rule in the overapproximation only could correspond 
	//to one original rule - due to the Skolemisation strategy used - there an index is assigned to 
	//each of the original rules and then they are retrieved directly. I assign an index to each of the 
	//rules in the overapproximation and then retrieve the corresponding original rules if they 
	//are linked to some rule that has been selected


	protected DatalogStrengthening program;
	Collection<DLClause> trackingClauses = new HashSet<DLClause>();

	Map<Integer, DLClause> index2clause = new HashMap<Integer, DLClause>();
	Map<DLClause, Integer> clause2index = new HashMap<DLClause, Integer>();

	Map<Integer, OWLIndividualAxiom> index2assertion = new HashMap<Integer, OWLIndividualAxiom>();
	Map<OWLAxiom, Integer> assertion2index = new HashMap<OWLAxiom, Integer>();


	String equalityRelatedRuleText = null; 

	public TrackingRuleEncoder4TailoredModuleExtraction(DatalogStrengthening program) {
		this.program = program;
	}

	public Set<OWLAxiom> extractAxioms(DataStore trackingStore) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>(); 
		TupleIterator answers = null;
		String answer;
		try {
			answers = trackingStore.compileQuery(getSelectedSPARQLQuery());
			for (long multi = answers.open(); multi != 0; multi = answers.getNext()) {
				answer = answers.getResource(0).m_lexicalForm;
				Object selected = getSelectedClauseOrFact(MyPrefixes.PAGOdAPrefixes.expandIRI(answer));
				if (selected instanceof DLClause) {
					if (DLClauseHelper.isTautologyAboutDifferentFrom((DLClause) selected))
						continue;
					axioms.addAll(program.getCorrespondingAxioms((DLClause) selected));
				}
				else
					axioms.addAll(program.getCorrespondingAxioms((OWLIndividualAxiom) selected));
			}
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		} finally {
			if (answers != null) answers.dispose();
		}

		Utility.logDebug("# TBox extraction Done");

		return axioms;
	}

	public Object getSelectedClauseOrFact(String iri) {
		//return either a DLClause or an OWLIndividualAxiom
		Object ret;
		try {
			int i = iri.lastIndexOf("_r") + 2;
			int ruleIndex = Integer.valueOf(iri.substring(i));
			ret = index2clause.get(ruleIndex);
		}
		catch (NumberFormatException e){
			int i = iri.lastIndexOf("_f") + 2;
			int assertionIndex = Integer.valueOf(iri.substring(i));
			ret = index2assertion.get(assertionIndex);
		}
		return ret;
	}



	protected String getEqualityRelatedRuleText() {
		if (equalityRelatedRuleText != null) return equalityRelatedRuleText.replace("_tn", getTrackingPredicate("")); 

		Collection<DLClause> equalityRelatedClauses = new LinkedList<DLClause>(); 
		Variable X = Variable.create("X");
		AtomicRole trackingSameAs = AtomicRole.create(Namespace.EQUALITY + "_tn");  
		OWLOntology onto = program.getOntology();
		Atom[] headAtom = new Atom[] {Atom.create(trackingSameAs, X, X)}, bodyAtom; 
		for (OWLClass cls: onto.getClassesInSignature(true)) {
			String clsIRI = cls.getIRI().toString();
			bodyAtom = new Atom[] {
					Atom.create(AtomicConcept.create(clsIRI + "_tn"), X)
					//					Atom.create(AtomicConcept.create(GapTupleIterator.getGapPredicate(clsIRI)), X1), 
			}; 
			equalityRelatedClauses.add(DLClause.create(headAtom, bodyAtom)); 
		}

		Variable Y = Variable.create("Y"); 
		for (OWLObjectProperty prop: onto.getObjectPropertiesInSignature(true)) {
			String propIRI = prop.getIRI().toString();
			AtomicRole trackingRole = AtomicRole.create(propIRI + "_tn"); 
			bodyAtom = new Atom[] {Atom.create(trackingRole, X, Y)}; 
			equalityRelatedClauses.add(DLClause.create(headAtom, bodyAtom));

			bodyAtom = new Atom[] {Atom.create(trackingRole, Y, X)}; 
			equalityRelatedClauses.add(DLClause.create(headAtom, bodyAtom)); 
		}

		equalityRelatedClauses.add(
				DLClause.create(
						new Atom[] {Atom.create(trackingSameAs, Y, X)}, 
						new Atom[] {Atom.create(trackingSameAs, X, Y)}));

		equalityRelatedRuleText = DLClauseHelper.toString(equalityRelatedClauses).toString();
		return equalityRelatedRuleText.replace("_tn", getTrackingPredicate("")); 
	}

	boolean ruleEncoded = false; 

	public int getNtrackingClauses(){
		int n = trackingClauses.size();
		if (program.containsEquality())
			n = n + program.getOntology().getClassesInSignature(true).size() + program.getOntology().getObjectPropertiesInSignature(true).size()*2;
		return n;
	}

	public boolean encodingRulesAndAssertions() {
		if (ruleEncoded) return false;
		ruleEncoded = true; 

		for (DLClause clause : program.getClauses()) {
			encodingRule(clause);
		}
		for (OWLIndividualAxiom axiom : program.getABoxAxioms()) {
			encodingAssertion(axiom);
		}
		return true; 
	}

	protected String getIRI(String name) {
		return program.getOntology().getOntologyID().getOntologyIRI().toString() + "#" + name;
	}

	protected void encodingRule(DLClause clause) {
		LinkedList<Atom> newHeadAtoms = new LinkedList<Atom>();
		newHeadAtoms.add(Atom.create(selected, getIndividual4GeneralRule(clause)));

		Atom headAtom;
		for (Atom atom: clause.getBodyAtoms()) {
			DLPredicate trackingPredicate = getTrackingDLPredicate(atom.getDLPredicate());
			if (trackingPredicate != null) {
				headAtom = Atom.create(
						getTrackingDLPredicate(atom.getDLPredicate()), 
						DLClauseHelper.getArguments(atom));
				newHeadAtoms.add(headAtom);	
			}
		}

		DLClause newClause;

		Atom[] newBodyAtoms = new Atom[clause.getBodyLength() + 1];
		if (clause.getHeadLength() < 1)
			Utility_PrisM.logError(clause + "TrackingRuleEncoder4Tailored..., method encodingRule(DLClause)"); 
		headAtom = clause.getHeadAtom(0);
		newBodyAtoms[0] = Atom.create(
				getTrackingDLPredicate(headAtom.getDLPredicate()), 
				DLClauseHelper.getArguments(headAtom));

		for (int i = 0; i < clause.getBodyLength(); ++i)
			newBodyAtoms[i + 1] = clause.getBodyAtom(i); 

		for (Atom atom: newHeadAtoms) {
			newClause = DLClause.create(new Atom[] {atom}, newBodyAtoms); 
			trackingClauses.add(newClause);
		}

	}

	protected void encodingAssertion(OWLIndividualAxiom axiom) {
		if (axiom instanceof OWLClassAssertionAxiom || 
				axiom instanceof OWLObjectPropertyAssertionAxiom || 
				axiom instanceof OWLDataPropertyAssertionAxiom){
			DLPredicate pred = null;
			Term[] terms = null;
			if (axiom instanceof OWLClassAssertionAxiom){
				pred = AtomicConcept.create( ((OWLClassAssertionAxiom) axiom).getClassExpression().asOWLClass().toStringID());
				terms = new Term[]{Individual.create( ((OWLClassAssertionAxiom) axiom).getIndividual().toStringID() )};
			}
			else if (axiom instanceof OWLObjectPropertyAssertionAxiom){
				pred = AtomicRole.create( ((OWLObjectPropertyAssertionAxiom) axiom).getProperty().asOWLObjectProperty().toStringID());
				terms = new Term[]{
						Individual.create(((OWLObjectPropertyAssertionAxiom) axiom).getSubject().toStringID()), 
						Individual.create(((OWLObjectPropertyAssertionAxiom) axiom).getObject().toStringID())};
			} 
			else if (axiom instanceof OWLDataPropertyAssertionAxiom){
				//TODO check that this works as it should
				pred = AtomicRole.create( ((OWLDataPropertyAssertionAxiom) axiom).getProperty().asOWLDataProperty().toStringID());
				terms = new Term[]{
						Individual.create(((OWLDataPropertyAssertionAxiom) axiom).getSubject().toStringID()), 
						Individual.create(((OWLDataPropertyAssertionAxiom) axiom).getObject().toString())};
			}

			Atom headAtom = Atom.create(selected, getIndividual4Assertion(axiom));
			Atom bodyAtom = Atom.create(
					getTrackingDLPredicate(pred), 
					terms);			
			DLClause newClause = DLClause.create(new Atom[] {headAtom}, new Atom[] {bodyAtom}); 
			trackingClauses.add(newClause);
		}
	}

	protected Individual getIndividual4GeneralRule(DLClause clause) {
		//instead of assigning one individual to each original rule, we assign one to each rule 
		//in the approximation, since the same rule in the approximation can correspond to several 
		//original rules (because we sometimes Skolemise everything to the critical instance ) 
		Integer index = clause2index.get(clause); 
		if (index == null) {
			index = clause2index.size() + 1; 
			index2clause.put(index, clause);
			clause2index.put(clause, index); 
		}

		return Individual.create(getIRI("_r" + index));
	}
	protected Individual getIndividual4Assertion(OWLIndividualAxiom axiom) {
		Integer index = assertion2index.get(axiom); 
		if (index == null) {
			index = assertion2index.size() + 1; 
			index2assertion.put(index, axiom);
			assertion2index.put(axiom, index); 
		}

		return Individual.create(getIRI("_f" + index));
	}


	DLPredicate getDLPredicate(DLPredicate p, String suffix) {
		if (p instanceof AtomicConcept) 
			return AtomicConcept.create(((AtomicConcept) p).getIRI() + suffix);
		else if (p instanceof DatatypeRestriction) {
			DatatypeRestriction restriction = (DatatypeRestriction) p; 
			String newURI = restriction.getDatatypeURI() + suffix; 
			return getDatatypeRestriction(restriction, newURI);
		}
		else if (p instanceof AtomicRole)
			return AtomicRole.create(((AtomicRole) p).getIRI() + suffix);
		else if (p instanceof AnnotatedEquality || p instanceof Equality) 
			return AtomicRole.create(Namespace.EQUALITY + suffix);
		else if (p instanceof Inequality) 
			return AtomicRole.create(Namespace.INEQUALITY + suffix);
		else {
			Utility.logInfo("strange DL predicate appeared ... " + p);
			Utility.logInfo("the program paused here in TrackingRuleEncoderDisj.java"); 
			return null;
		}
	}

	protected DLPredicate getTrackingDLPredicate(DLPredicate dlPredicate) {
		return getDLPredicate(dlPredicate, getTrackingSuffix()); 
	}

	protected static String getTrackingSuffix() {
		return "_AUXt"; 
	}

	public String getTrackingPredicate(String predicateIRI) {
		if (predicateIRI.startsWith("<"))
			return predicateIRI.replace(">", getTrackingSuffix() + ">");
		else 
			return predicateIRI + getTrackingSuffix();
	}

	protected DLPredicate getDatatypeRestriction(DatatypeRestriction restriction, String newName) {
		int length = restriction.getNumberOfFacetRestrictions(); 
		String[] facets = new String[length]; 
		Constant[] values = new Constant[length]; 
		for (int i = 0; i < length; ++i) {
			facets[i] = restriction.getFacetURI(i);
			values[i] = restriction.getFacetValue(i); 
		}
		return DatatypeRestriction.create(newName, facets, values); 
	}

	DLPredicate selected;

	private String getTrackingRuleText() {
		return DLClauseHelper.toString(trackingClauses);
	}

	public String getTrackingProgram() {
		StringBuilder sb = getTrackingProgramBody();
		sb.insert(0, MyPrefixes.PAGOdAPrefixes.prefixesText()); 
		return sb.toString(); 
	}

	protected StringBuilder getTrackingProgramBody() {
		selected = AtomicConcept.create(getSelectedPredicate());
		encodingRulesAndAssertions();

		StringBuilder sb = new StringBuilder(); 
		sb.append(getTrackingRuleText());
		if (program.containsEquality()){
			sb.append(getEqualityRelatedRuleText());
			Utility_PrisM.logDebug("# adding tracking rules for EQUALITY");
		}
		return sb; 
	}

	public void saveTrackingRules(String fileName) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
			writer.write(getTrackingProgram());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return ;
		}
		Utility_PrisM.logDebug("The tracking rules are saved in " + fileName + "."); 
	}

	public String getSelectedPredicate() {
		return getIRI("_selected"); 
	}

	/**
	 * SELECT ?X
	 * WHERE {
	 * 	?X <http://www.w3.org/1999/02/22-rdf-syntax-ns#:type> :_selected?
	 * }
	 */
	public String getSelectedSPARQLQuery() {
		StringBuilder builder = new StringBuilder(); 
		builder.append("SELECT ?X\nWHERE {\n?X <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> "); 
		builder.append(selected.toString()).append("\n}");  
		return builder.toString(); 
	}

	public OWLOntology getOntology() {
		return program.getOntology(); 
	}

	public DatalogStrengthening getDatalogStrengthening() {
		return program;
	}

	public String getOriginalPredicate(String p) {
		if (p.startsWith("<")) {
			if (!p.endsWith(getTrackingSuffix() + ">")) return null;
		}
		else 
			if (!p.endsWith(getTrackingSuffix())) return null;

		return p.replace(getTrackingSuffix(), ""); 
	}

}
