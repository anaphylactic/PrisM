package uk.ac.ox.cs.prism;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.simpleETL.SimpleETL;

import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.approx.RLPlusOntology;
import uk.ac.ox.cs.pagoda.constraints.BottomStrategy;
import uk.ac.ox.cs.pagoda.constraints.NullaryBottom;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.rules.Approximator;
import uk.ac.ox.cs.pagoda.util.Utility;
import uk.ac.ox.cs.prism.clausification.DLOntology_withMaps;
import uk.ac.ox.cs.prism.clausification.DatatypeManager;
import uk.ac.ox.cs.prism.clausification.OWLClausification_withMaps;
import uk.ac.ox.cs.prism.util.Utility_PrisM;

public class DatalogStrengthening {

	protected String ontologyDirectory = null;
	protected OWLOntology ontology; 
	protected DLOntology_withMaps dlOntology;	//stores the correspondence between (non-overapproximated) DLCLauses and the original axioms that lead to them
	protected BottomStrategy botStrategy; 

	protected String additionalDataFile = null;
	protected OWLOntology aBox;

	//	protected Collection<DLClause> clauses = new LinkedList<DLClause>();
	protected Collection<DLClause> additionalClauses = new LinkedList<DLClause>();

	protected Approximator m_approx = null; 
	boolean containsEquality = false;
	Set<AtomicRole> binaryPredsOnBodies = new HashSet<AtomicRole>();
	protected DatatypeManager datatypeManager = new DatatypeManager();

	protected LinkedList<OWLTransitiveObjectPropertyAxiom> transitiveAxioms;
	protected LinkedList<OWLSubPropertyChainOfAxiom> subPropChainAxioms; 

	/**
	 * mapping from over-approximated DLClauses to DLClauses from the original ontology
	 */
	protected Map<DLClause, Object> correspondence = new HashMap<DLClause, Object>();
	protected Map<OWLIndividualAxiom, Collection<OWLAxiom>> aBoxCorrespondence = new HashMap<OWLIndividualAxiom, Collection<OWLAxiom>>();

	public DatalogStrengthening(IndividualManager indManager, DatatypeManager datatypeManager){
		m_approx = new OverApproximatorWithDatatypeSupport(indManager, datatypeManager);
		this.datatypeManager = datatypeManager;
	}

	public void load(OWLOntology o, BottomStrategy botStrategy) {
		this.botStrategy = botStrategy; 
		RLPlusOntology owlOntology = new RLPlusOntology(); 
		owlOntology.load(o, new NullaryBottom());
		owlOntology.simplify();
		ontology = owlOntology.getTBox(); 
		String ontologyPath = OWLHelper.getOntologyPath(ontology); 
		if (ontologyPath.lastIndexOf("/") >=0)
			ontologyDirectory = ontologyPath.substring(0, ontologyPath.lastIndexOf("/")); // Really is a '/' not the system file separator.
		else 
			ontologyDirectory = ontologyPath;
		clausify(); 
		transform();

		String aboxOWLFile = owlOntology.getABoxPath();
		aBox = OWLHelper.loadOntology(aboxOWLFile);
		for (OWLAxiom ax : aBox.getABoxAxioms(true)) {
			Set<OWLAxiom> aux = new HashSet<OWLAxiom>();
			aux.add(ax);
			aBoxCorrespondence.put((OWLIndividualAxiom) ax, aux);
		}
		OWLOntologyManager manager = aBox.getOWLOntologyManager();
		OWLIndividualAxiom axiom;
		for (Entry<Atom,Collection<OWLAxiom>> entry : dlOntology.getPositiveFactsMap().entrySet())
			if ((axiom = OWLHelper.getABoxAssertion(manager.getOWLDataFactory(), entry.getKey())) != null) {
				manager.addAxiom(aBox, axiom);
				Collection<OWLAxiom> aux = aBoxCorrespondence.get(axiom);
				if (aux == null)
					aBoxCorrespondence.put(axiom, entry.getValue());
				else 
					aux.addAll(entry.getValue());
			}
			else
				Utility_PrisM.logDebug("got null assertion corresponding to " + entry.getKey().toString());

		try {
			FileOutputStream out = new FileOutputStream(aboxOWLFile); 
			manager.saveOntology(aBox, out);
			out.close();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!aBox.isEmpty()) {
			SimpleETL rewriter = new SimpleETL(owlOntology.getOntologyIRI(), aboxOWLFile);
			try {
				rewriter.rewrite();
			} catch (Exception e) {
				e.printStackTrace();
			} 
			additionalDataFile = rewriter.getExportedFile();
		}
	}

	protected void clausify() {
		OWLClausification_withMaps clausifier = new OWLClausification_withMaps(datatypeManager);
		OWLOntology filteredOntology = null;
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		try {
			filteredOntology = manager.createOntology();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}

		transitiveAxioms = new LinkedList<OWLTransitiveObjectPropertyAxiom>();
		subPropChainAxioms = new LinkedList<OWLSubPropertyChainOfAxiom>();

		for (OWLOntology onto: ontology.getImportsClosure())
			for (OWLAxiom axiom: onto.getAxioms()) {
				if (axiom instanceof OWLTransitiveObjectPropertyAxiom) 
					transitiveAxioms.add((OWLTransitiveObjectPropertyAxiom) axiom);
				else if (axiom instanceof OWLSubPropertyChainOfAxiom) 
					subPropChainAxioms.add((OWLSubPropertyChainOfAxiom) axiom);
				else {
					manager.addAxiom(filteredOntology, axiom);
				}
			}

		dlOntology = clausifier.preprocessAndClausify(filteredOntology);
		clausifier = null;
	}//DONE

	public String getAdditionalDataFile() {
		return additionalDataFile; 
	}


	public void transform() {
		for (Entry<DLClause,Collection<OWLAxiom>> entry : dlOntology.getDLClausesMap().entrySet()) {
			DLClause simplifiedDLClause = DLClauseHelper.removeNominalConcept(entry.getKey()); //HERE
			simplifiedDLClause = removeAuxiliaryBodyAtoms(simplifiedDLClause);
			//			simplifiedDLClause  = DLClauseHelper.replaceWithDataValue(simplifiedDLClause);

			Collection<DLClause> convertedClauses = botStrategy.process(m_approx.convert(simplifiedDLClause, simplifiedDLClause));
			for (DLClause newClause: convertedClauses) {
				addCorrespondence(newClause, entry.getKey());

				//register the binary predicates in its body
				for (Atom at : newClause.getBodyAtoms()){
					DLPredicate pred = at.getDLPredicate();
					if (pred instanceof AtomicRole) binaryPredsOnBodies.add((AtomicRole) pred);
				}

				if (!containsEquality) updateContainsEquality(newClause);
			}
		}

		for (Entry<Atom,Collection<OWLAxiom>> entry : dlOntology.getNegativeFactsMap().entrySet()) {
			List<DLClause> aux = new ArrayList<DLClause>();
			aux.add(DLClause.create(new Atom[0], new Atom[] {entry.getKey()}));
			for (DLClause clause : botStrategy.process(aux)){
				addCorrespondence(clause, entry.getValue());
			}
		}

		addingTransitiveAxioms();
		addingSubPropertyChainAxioms();

		Collection<DLClause> botRelated = new LinkedList<DLClause>(); 
		Variable X = Variable.create("X"); 
		botRelated.add(DLClause.create(new Atom[0], new Atom[] {Atom.create(Inequality.INSTANCE, X, X)}));
		additionalClauses.addAll(botStrategy.process(botRelated));
	}//DONE

	private DLClause removeAuxiliaryBodyAtoms(DLClause dlClause) {
		Collection<Atom> newBodyAtoms = new LinkedList<Atom>();
		DLPredicate p; 
		for (Atom bodyAtom: dlClause.getBodyAtoms()) {
			p = bodyAtom.getDLPredicate(); 
			if (p instanceof AtomicConcept || p instanceof AtomicRole || 
					p instanceof Equality || p instanceof AnnotatedEquality || p instanceof Inequality)
				newBodyAtoms.add(bodyAtom); 
		}

		if (newBodyAtoms.size() == dlClause.getBodyLength())
			return dlClause; 
		return DLClause.create(dlClause.getHeadAtoms(), newBodyAtoms.toArray(new Atom[0])); 
	}

	protected void addingTransitiveAxioms() {
		DLClause transitiveClause;
		Atom headAtom;
		Variable X = Variable.create("X"), Y = Variable.create("Y"), Z = Variable.create("Z");
		Iterator<OWLTransitiveObjectPropertyAxiom> iter = transitiveAxioms.iterator();
		while (iter.hasNext()){
			OWLTransitiveObjectPropertyAxiom axiom = iter.next();
			OWLObjectPropertyExpression objExp = axiom.getProperty(); 
			headAtom = getAtom(objExp, X, Z);
			if (binaryPredsOnBodies.contains((AtomicRole) headAtom.getDLPredicate())){
				transitiveClause = DLClause.create(new Atom[] {headAtom}, new Atom[]{getAtom(objExp, X, Y), getAtom(objExp, Y, Z)}); 
				addCorrespondence(transitiveClause, axiom);
			}
		}
	}//DONE


	protected Atom getAtom(OWLObjectPropertyExpression exp, Variable x, Variable y) {
		if (exp instanceof OWLObjectProperty)
			return Atom.create(AtomicRole.create(((OWLObjectProperty) exp).toStringID()), x, y);
		OWLObjectInverseOf inverseOf; 
		if (exp instanceof OWLObjectInverseOf && (inverseOf = (OWLObjectInverseOf) exp).getInverse() instanceof OWLObjectProperty)
			return Atom.create(AtomicRole.create(((OWLObjectProperty) inverseOf).toStringID()), x, y);
		return null;
	}//DONE

	protected void addingSubPropertyChainAxioms() {
		DLClause dlClause; 
		Atom headAtom;
		Iterator<OWLObjectPropertyExpression> iterExp; 
		OWLObjectPropertyExpression objExp; 
		for (OWLSubPropertyChainOfAxiom axiom: subPropChainAxioms) {
			objExp = axiom.getSuperProperty();
			List<OWLObjectPropertyExpression> objs = axiom.getPropertyChain();
			headAtom = getAtom(objExp, Variable.create("X"), Variable.create("X" + objs.size()));
			iterExp = objs.iterator();
			int index = 1; 
			Atom[] bodyAtoms = new Atom[objs.size()]; 
			bodyAtoms[0] = getAtom(iterExp.next(), Variable.create("X"), Variable.create("X1")); 
			while (index < objs.size()) {
				bodyAtoms[index] = getAtom(iterExp.next(), Variable.create("X" + index), Variable.create("X" + (index + 1)));
				++index; 
			}
			dlClause = DLClause.create(new Atom[] {headAtom}, bodyAtoms); 
			addCorrespondence(dlClause, axiom);
		}
	}//DONE

	public void save() {
		try {
			BufferedWriter ruleWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(getOutputPath())));
			ruleWriter.write(toString());
			ruleWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Utility.logDebug("The rules are saved in " + getOutputPath() + "."); 
	}

	@Override
	public String toString() {
		Collection<DLClause> clauses = new HashSet<DLClause>(correspondence.keySet());
		clauses.addAll(additionalClauses);
		return toString(clauses);
	}//DONE

	public static String toString(Collection<DLClause> clauses) {
		StringBuilder sb = new StringBuilder(DLClauseHelper.toString(clauses)); 
		sb.insert(0, MyPrefixes.PAGOdAPrefixes.prefixesText()); 
		return sb.toString(); 
	}//DONE

	protected void updateContainsEquality(DLClause clause){
		if (clause.getHeadAtom(0).getDLPredicate().toString().contains("==")){
			containsEquality = true;
			Utility.logDebug("# contains equality");
		}
	}//DONE

	public boolean containsEquality(){
		return containsEquality;
	}

	//copied from class ApproxProgram in PAGOdA and modified
	@SuppressWarnings("unchecked")
	private void addCorrespondence(DLClause newClause, Object corresponding) {
		//corresponding could be a DLClause or an OWLAxiom
		//or even a set of DLClause and/or OWLAxiom
		Object originalCorrespondent = correspondence.get(newClause); 
		if (originalCorrespondent != null) {
			if (originalCorrespondent.equals(corresponding))
				return ; 
			else if (originalCorrespondent instanceof Set<?>){
				if (corresponding instanceof Set<?>)
					((Set<Object>) originalCorrespondent).addAll((Set<Object>) corresponding);
				else
					((Set<Object>) originalCorrespondent).add(corresponding);
			}
			else  {
				HashSet<Object> aux = new HashSet<Object>();
				aux.add(originalCorrespondent);
				if (corresponding instanceof Set<?>)
					aux.addAll((Set<Object>) corresponding);
				else
					aux.add(corresponding);
				correspondence.put(newClause, aux);
			}
		}
		else 
			correspondence.put(newClause, corresponding);
	}//DONE

	//will need this method in the future for TrackingRuleEncoderDisjVar4TailoredModuleExtraction
	//	public Set<DLClause> getCorrespondingClauses(DLClause clause) {
	//		Object obj = correspondence.get(clause);
	//		if (obj instanceof Set<?>)
	//			return (Set<DLClause>) obj;
	//		else {
	//			//obj is null or an OWLAxiom; in either case we want to return clause
	//			//it may be null because clause is a clause from the original program 
	//			//(e.g. a disjunctive clause) and not its corresponding rule in the overapprox 
	//			//this may happen when we use the DisjVar TrackingRuleEncoding
	//			Set<DLClause> aux = new HashSet<DLClause>();
	//			aux.add(clause);
	//			return aux;
	//		}
	//	}

	public Set<OWLAxiom> getCorrespondingAxioms(DLClause clause) {
		Set<OWLAxiom> ret = new HashSet<OWLAxiom>();
		Object obj = correspondence.get(clause);

		if (obj instanceof OWLAxiom) 
			ret.add((OWLAxiom) obj);
		else if (obj instanceof DLClause) {
			Collection<OWLAxiom> aux = dlOntology.getDLClausesMap().get(obj);
			if (aux != null)
				ret.addAll(dlOntology.getDLClausesMap().get(obj));
		}
		else {//obj must be a set containing instances of DLClause and/or of OWLAxiom
			for (Object o : (Set<Object>) obj) {
				if (o instanceof OWLAxiom) ret.add((OWLAxiom) o);
				else ret.addAll(dlOntology.getCorrespondingAxioms((DLClause) o));
			}
		}
		return ret;
	}

	public Collection<OWLAxiom> getCorrespondingAxioms(OWLIndividualAxiom axiom) {
		Collection<OWLAxiom> ret = aBoxCorrespondence.get(axiom);
		return ret == null ? new HashSet<OWLAxiom>() : ret;
	}


	public Collection<OWLIndividualAxiom> getABoxAxioms(){
		return aBoxCorrespondence.keySet();
	}

	public String getOutputPath() {
		return getDirectory() + Utility.FILE_SEPARATOR + "upper.dlog";
	}

	public OWLOntology getOntology() {
		return ontology;
	}

	public Collection<DLClause> getClauses() {
		Collection<DLClause> clauses = new HashSet<DLClause>(correspondence.keySet());
		clauses.addAll(additionalClauses);
		return clauses;
	}

	public int getNclauses() {
		return additionalClauses.size() + correspondence.size();
	}

	public final String getDirectory() {
		return Utility.TempDirectory; 
	}

}
