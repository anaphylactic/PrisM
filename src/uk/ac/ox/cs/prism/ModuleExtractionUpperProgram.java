package uk.ac.ox.cs.prism;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.HermiT.structural.OWLClausification;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.simpleETL.SimpleETL;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.ox.cs.pagoda.approx.RLPlusOntology;
import uk.ac.ox.cs.pagoda.constraints.BottomStrategy;
import uk.ac.ox.cs.pagoda.constraints.NullaryBottom;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.rules.Approximator;
import uk.ac.ox.cs.pagoda.rules.Program;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Utility;
import uk.ac.ox.cs.prism.util.Utility_tme;

public class ModuleExtractionUpperProgram extends Program{

	/**
	 * mapping from over-approximated DLClauses to DLClauses from the original ontology
	 */
	Map<DLClause, Object> correspondence = new HashMap<DLClause, Object>();
	Map<OWLAxiom, OWLAxiom> aBoxCorrespondence = new HashMap<OWLAxiom, OWLAxiom>();
	protected Approximator m_approx = null; 
	boolean containsEquality = false;
	Set<AtomicRole> binaryPredsOnBodies = new HashSet<AtomicRole>();
	OWLOntology aBox;
	LinkedList<OWLSameIndividualAxiom> equalityAssertionAxioms = new LinkedList<OWLSameIndividualAxiom>();  
	LinkedList<OWLDifferentIndividualsAxiom> inequalityAssertionAxioms = new LinkedList<OWLDifferentIndividualsAxiom>();
	LinkedList<OWLAxiom> negativeAssertions = new LinkedList<OWLAxiom>(); 
	
	public ModuleExtractionUpperProgram(IndividualManager indManager){
		m_approx = new OverApproxForTailoredModuleExtraction(indManager);
	}

	//copied from class ApproxProgram in Pagoda
	public int getBottomNumber() {
		return botStrategy.getBottomNumber();
	}

	@Override
	public void load(OWLOntology o, BottomStrategy botStrategy) {
		this.botStrategy = botStrategy; 
		RLPlusOntology owlOntology = new RLPlusOntology(); 
		owlOntology.load(o, new NullaryBottom());
		owlOntology.simplify();

		ontology = owlOntology.getTBox(); 
		String ontologyPath = OWLHelper.getOntologyPath(ontology); 
		// Really is a '/' not the system file seperator.
		ontologyDirectory = ontologyPath.substring(0, ontologyPath.lastIndexOf("/"));
		clausify(); 
		transform();
		
		
		
		String aboxOWLFile = owlOntology.getABoxPath();
		aBox = OWLHelper.loadOntology(aboxOWLFile);
		OWLOntologyManager manager = aBox.getOWLOntologyManager();
		OWLAxiom axiom; 
		for (Atom atom: dlOntology.getPositiveFacts()) {
			if ((axiom = OWLHelper.getABoxAssertion(manager.getOWLDataFactory(), atom)) != null)
				manager.addAxiom(aBox, axiom);
		}
		addingEqualityRelatedAssertions();
		
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
	
	@Override
	protected void clausify() {
		Configuration conf = new Configuration();
		OWLClausification clausifier = new OWLClausification(conf);
		OWLOntology filteredOntology = null;
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		try {
			filteredOntology = manager.createOntology();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		transitiveAxioms = new LinkedList<OWLTransitiveObjectPropertyAxiom>();
		subPropChainAxioms = new LinkedList<OWLSubPropertyChainOfAxiom>();
		equalityAssertionAxioms = new LinkedList<OWLSameIndividualAxiom>();
		inequalityAssertionAxioms = new LinkedList<OWLDifferentIndividualsAxiom>();
		negativeAssertions = new LinkedList<OWLAxiom>();
		
		int noOfDataPropertyRangeAxioms = 0, noOfAxioms = 0; 
		for (OWLOntology onto: ontology.getImportsClosure())
			for (OWLAxiom axiom: onto.getAxioms()) {
				if (axiom instanceof OWLTransitiveObjectPropertyAxiom) 
					transitiveAxioms.add((OWLTransitiveObjectPropertyAxiom) axiom);
				else if (axiom instanceof OWLSubPropertyChainOfAxiom) 
					subPropChainAxioms.add((OWLSubPropertyChainOfAxiom) axiom);
				else if (axiom instanceof OWLSameIndividualAxiom)
					equalityAssertionAxioms.add((OWLSameIndividualAxiom) axiom);
				else if (axiom instanceof OWLDifferentIndividualsAxiom)
					inequalityAssertionAxioms.add((OWLDifferentIndividualsAxiom) axiom);
				else if (axiom instanceof OWLNegativeObjectPropertyAssertionAxiom)
					negativeAssertions.add(axiom);
				else if (axiom instanceof OWLNegativeDataPropertyAssertionAxiom)
					negativeAssertions.add(axiom);
				// TODO to filter out datatype axioms
				else if (axiom instanceof OWLDataPropertyRangeAxiom) {
					++noOfDataPropertyRangeAxioms; 
				}
				else {
					manager.addAxiom(filteredOntology, axiom);
				}
				
				if (axiom instanceof OWLAnnotationAssertionAxiom ||
						axiom instanceof OWLSubAnnotationPropertyOfAxiom ||
						axiom instanceof OWLDeclarationAxiom ||
						axiom instanceof OWLDataPropertyRangeAxiom); 
				else {
					++noOfAxioms;
				}
					
			}
		Utility.logInfo("The number of data property range axioms that are ignored: " + noOfDataPropertyRangeAxioms + "(" + noOfAxioms + ")");
		
		dlOntology = (DLOntology)clausifier.preprocessAndClausify(filteredOntology, null)[1];
		clausifier = null;
	}
	
	@Override //copied from class ApproxProgram in Pagoda
	public void transform() {
		super.transform();
		addingNegativeAssertions();
	}
	
	@Override
	protected void addingTransitiveAxioms() {
		DLClause transitiveClause;
		Atom headAtom;
		Variable X = Variable.create("X"), Y = Variable.create("Y"), Z = Variable.create("Z");
		transitiveClauses = new LinkedList<DLClause>();
		Iterator<OWLTransitiveObjectPropertyAxiom> iter = transitiveAxioms.iterator();
		while (iter.hasNext()){
			OWLTransitiveObjectPropertyAxiom axiom = iter.next();
			OWLObjectPropertyExpression objExp = axiom.getProperty(); 
			headAtom = getAtom(objExp, X, Z);
			
			if (binaryPredsOnBodies.contains((AtomicRole) headAtom.getDLPredicate())){
				Atom[] bodyAtoms = new Atom[2];
				bodyAtoms[0] = getAtom(objExp, X, Y); 
				bodyAtoms[1] = getAtom(objExp, Y, Z); 
				transitiveClause = DLClause.create(new Atom[] {headAtom}, bodyAtoms); 
				clauses.add(transitiveClause);
				transitiveClauses.add(transitiveClause);
				addCorrespondence(transitiveClause, axiom);
			}
		}
	}
	
	@Override
	protected void addingSubPropertyChainAxioms() {
		DLClause dlClause; 
		subPropChainClauses = new LinkedList<DLClause>();
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
			clauses.add(dlClause); 
			subPropChainClauses.add(dlClause);
			addCorrespondence(dlClause, axiom);
		}
	}
	
	protected void addingNegativeAssertions() {
		List<DLClause> negAssertionClauses = new LinkedList<DLClause>();
		for (OWLAxiom axiom: negativeAssertions) {
			if (axiom instanceof OWLNegativeObjectPropertyAssertionAxiom){
				AtomicRole role = AtomicRole.create(((OWLObjectProperty) ((OWLNegativeObjectPropertyAssertionAxiom) axiom).getProperty()).toStringID());
				Individual subject = Individual.create(((OWLNegativeObjectPropertyAssertionAxiom) axiom).getSubject().toStringID());
				Individual object = Individual.create(((OWLNegativeObjectPropertyAssertionAxiom) axiom).getObject().toStringID());
				Atom bodyAtom = Atom.create(role, subject, object); 
				negAssertionClauses.add(DLClause.create(new Atom[0], new Atom[] {bodyAtom}));
			}
			//TODO deal with the case of NegativeDataPropertyAssertionAxiom
			else if (axiom instanceof OWLNegativeDataPropertyAssertionAxiom){
				Utility_tme.logDebug("there was an OWLNegativeDataPropertyAssertionAxiom that ahd to be ignored: " + axiom.toString());
			}
			for (DLClause clause : botStrategy.process(negAssertionClauses)){
				clauses.add(clause);
				addCorrespondence(clause, axiom);
			}
			negAssertionClauses.clear();
		}
		
	}
	
	protected void addingEqualityRelatedAssertions() {
		Set<OWLObjectPropertyAssertionAxiom> equalityRelatedAssertions = new HashSet<OWLObjectPropertyAssertionAxiom>();
		OWLOntologyManager manager = aBox.getOWLOntologyManager();
		OWLDataFactory factory = new OWLDataFactoryImpl();
		for (OWLSameIndividualAxiom axiom: equalityAssertionAxioms) {
			OWLObjectPropertyExpression eq = factory.getOWLObjectProperty(IRI.create(Namespace.EQUALITY));
			Iterator<OWLIndividual> iter = axiom.getIndividuals().iterator();
			OWLIndividual first = iter.next();
			OWLIndividual i = first;
			OWLIndividual j;
			while (iter.hasNext()){
				j= iter.next();
				OWLObjectPropertyAssertionAxiom newAxiom = factory.getOWLObjectPropertyAssertionAxiom(eq, i, j); 
				equalityRelatedAssertions.add(newAxiom);
				aBoxCorrespondence.put(newAxiom, axiom);
				i=j;
			}
			OWLObjectPropertyAssertionAxiom newAxiom = factory.getOWLObjectPropertyAssertionAxiom(eq, i, first); 
			equalityRelatedAssertions.add(newAxiom);
			aBoxCorrespondence.put(newAxiom, axiom);
		}
		for (OWLDifferentIndividualsAxiom axiom: inequalityAssertionAxioms) {
			OWLObjectPropertyExpression eq = factory.getOWLObjectProperty(IRI.create(Namespace.INEQUALITY));
			Iterator<OWLIndividual> iter = axiom.getIndividuals().iterator();
			OWLIndividual first = iter.next();
			OWLIndividual i = first;
			OWLIndividual j;
			while (iter.hasNext()){
				j= iter.next();
				OWLObjectPropertyAssertionAxiom newAxiom = factory.getOWLObjectPropertyAssertionAxiom(eq, i, j); 
				equalityRelatedAssertions.add(newAxiom);
				aBoxCorrespondence.put(newAxiom, axiom);
				i=j;
			}
			OWLObjectPropertyAssertionAxiom newAxiom = factory.getOWLObjectPropertyAssertionAxiom(eq, i, first); 
			equalityRelatedAssertions.add(newAxiom);
			aBoxCorrespondence.put(newAxiom, axiom);
		}
		manager.addAxioms(aBox, equalityRelatedAssertions);
	}
	
	@Override //copied from class ApproxProgram in PAGOdA and modified 
	public Collection<DLClause> convert2Clauses(DLClause clause) {
		Collection<DLClause> ret = botStrategy.process(m_approx.convert(clause, clause));
		for (DLClause newClause: ret) {
			addCorrespondence(newClause, clause);
			
			//register the binary predicates in its body
			for (Atom at : newClause.getBodyAtoms()){
				DLPredicate pred = at.getDLPredicate();
				if (pred instanceof AtomicRole)
					binaryPredsOnBodies.add((AtomicRole) pred);
			}
			
			if (!containsEquality)
				updateContainsEquality(newClause);
		}
		return ret; 
	}
	
	protected void updateContainsEquality(DLClause clause){
		if (clause.getHeadAtom(0).getDLPredicate().toString().contains("==")){
			containsEquality = true;
			Utility.logDebug("# contains equality");
		}
	}

	public boolean containsEquality(){
		return containsEquality;
	}
	
	//copied from class ApproxProgram in PAGOdA and modified
	@SuppressWarnings("unchecked")
	private void addCorrespondence(DLClause newClause, Object corresponding) {
		//corresponding could be a DLClause or an OWLAxiom --- if it's an OWLAxiom it must be a transitivity or subPropertyChain axiom
		Object originalCorrespondent = correspondence.get(newClause); 
		if (originalCorrespondent != null) {
			if (originalCorrespondent.equals(corresponding))
				return ; 
			else if (originalCorrespondent instanceof Set<?> && corresponding instanceof DLClause){
				((Set<DLClause>) originalCorrespondent).add((DLClause) corresponding);
			}
			else 
				throw new IllegalArgumentException("trying to establish a weird corespondence!?!: " 
						+ newClause.toString() + "  " + corresponding.toString());
		}
		else if (corresponding instanceof OWLAxiom)
			correspondence.put(newClause, corresponding);
		else if (corresponding instanceof DLClause){
			Set<DLClause> aux = new HashSet<DLClause>();
			aux.add((DLClause) corresponding);
			correspondence.put(newClause, aux);
		}
		else throw new IllegalArgumentException("trying to establish a weird corespondence!?!");
	}

	public OWLAxiom getEquivalentAxiom(DLClause clause) {
		Object obj = correspondence.get(clause);
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		if (obj instanceof OWLAxiom) 
			return (OWLAxiom) obj; 
		else 
			return RedundantAxiomRepairer.repair(OWLHelper.getOWLAxiom(ontology, clause), factory); 
	}

	public Set<DLClause> getCorrespondingClauses(DLClause clause) {
		Object obj = correspondence.get(clause);
		if (obj instanceof Set<?>)
			return (Set<DLClause>) obj;
		else {
			//obj is null or an OWLAxiom; in either case we want to return clause
			//it may be null because clause is a clause from the original program 
			//(e.g. a disjunctive clause) and not its corresponding rule in the overapprox 
			//this may happen when we use the DisjVar TrackingRuleEncoding
			Set<DLClause> aux = new HashSet<DLClause>();
			aux.add(clause);
			return aux;
		}
	}
	
	public OWLAxiom getCorrespondingAxiom(OWLAxiom axiom) {
		OWLAxiom ret = aBoxCorrespondence.get(axiom);
		return ret == null ? axiom : ret;
	}
	
	
	public OWLOntology getABox(){
		return aBox;
	}

	@Override
	public String getOutputPath() {
		return getDirectory() + Utility.FILE_SEPARATOR + "upper.dlog";
	}


}











