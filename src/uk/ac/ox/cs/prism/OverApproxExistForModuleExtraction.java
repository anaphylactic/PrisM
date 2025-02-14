package uk.ac.ox.cs.prism;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.semanticweb.HermiT.model.AtLeast;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.AtLeastDataRange;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;

import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.rules.Approximator;
import uk.ac.ox.cs.pagoda.util.Namespace;



public class OverApproxExistForModuleExtraction implements Approximator{
	//copied from OverApproxExists with minor alterations

	protected IndividualManager indManager;
	
	public OverApproxExistForModuleExtraction(IndividualManager iManager){
		indManager = iManager;
	}
	
	
	@Override
	public Collection<DLClause> convert(DLClause clause, DLClause originalClause) {
		Collection<DLClause> ret;
		switch (clause.getHeadLength()) {
		case 1:
			return overApprox(clause.getHeadAtom(0), clause.getBodyAtoms(), originalClause);
		case 0:
			ret = new LinkedList<DLClause>();
			ret.add(clause); 
			return ret; 
		default:
			ret = new LinkedList<DLClause>();
			for (Iterator<DLClause> iter = new DisjunctiveHeads(clause, originalClause); iter.hasNext(); )
				ret.add(iter.next());
			return ret;
		}
	}


	private static int indexOfExistential(Atom headAtom, DLClause originalClause) {
		if (!(headAtom.getDLPredicate() instanceof AtLeast)) return -1; 
		int index = 0; 
		for (Atom atom: originalClause.getHeadAtoms()) {
			if (atom.equals(headAtom)) 
				return index; 
			if (atom.getDLPredicate() instanceof AtLeast)
				index += ((AtLeast) atom.getDLPredicate()).getNumber(); 
		}
		return -1; 
	}

	protected static final Variable X = Variable.create("X"); 
	public static final String negativeSuffix = "_neg"; 
	
	public static AtomicConcept getNegationConcept(DLPredicate p) {
		if (p.equals(AtomicConcept.THING)) return AtomicConcept.NOTHING;
		if (p.equals(AtomicConcept.NOTHING)) return AtomicConcept.THING; 
		
		if (p instanceof AtomicConcept) {
			String iri = ((AtomicConcept) p).getIRI();
			if (iri.endsWith(negativeSuffix)) 
				iri = iri.substring(0, iri.length() - 4);
			else 
				iri += negativeSuffix; 

			return AtomicConcept.create(iri);
		}
		if (p instanceof AtLeastConcept) {
			// FIXME !!! here
			return null; 
		}
		return null; 
	}	
	
	public static final String skolemisedIndividualPrefix = Namespace.PAGODA_ANONY + "individual"; 
	
	public static int indexOfSkolemisedIndividual(Atom atom) {
		Term t; 
		for (int index = 0; index < atom.getArity(); ++index) {
			t = atom.getArgument(index);
			if (t instanceof Individual && ((Individual) t).getIRI().contains(skolemisedIndividualPrefix)) return index;
		}
		return -1; 
	}
	
	public Collection<DLClause> overApprox(Atom headAtom, Atom[] bodyAtoms, DLClause originalClause) {
		return overApprox(headAtom, bodyAtoms, originalClause, indexOfExistential(headAtom, originalClause)); 
	}
	
	public Collection<DLClause> overApprox(Atom headAtom, Atom[] bodyAtoms, DLClause originalClause, int offset) {
		Collection<DLClause> ret = new LinkedList<DLClause>(); 
		DLPredicate predicate = headAtom.getDLPredicate();
		if (predicate instanceof AtLeastConcept) {
			AtLeastConcept atLeastConcept = (AtLeastConcept) predicate;
			LiteralConcept concept = atLeastConcept.getToConcept();
			Role role = atLeastConcept.getOnRole();
			AtomicConcept atomicConcept = null;
			
			if (concept instanceof AtomicNegationConcept) {
				Atom atom1 = Atom.create(atomicConcept = ((AtomicNegationConcept) concept).getNegatedAtomicConcept(), X);
				Atom atom2 = Atom.create(atomicConcept = getNegationConcept(atomicConcept), X);
				ret.add(DLClause.create(new Atom[0], new Atom[] {atom1, atom2})); 
			}
			else {
				atomicConcept = (AtomicConcept) concept;
				if (atomicConcept.equals(AtomicConcept.THING))
					atomicConcept = null;
			}

			int card = atLeastConcept.getNumber(); 
			Individual[] individuals = new Individual[card];
			for (int i = 0; i < card; ++i) individuals[i] = indManager.getSkolemIndividual(originalClause, offset + i);
			
			for (int i = 0; i < card; ++i) {
				if (atomicConcept != null){
					ret.add(DLClause.create(new Atom[] {Atom.create(atomicConcept, individuals[i])}, bodyAtoms));
					//unlike in MORe, here we can't just add these facts in a dataset without connecting them to the rule that produces them 
					//this would not be enough, e.g. for query inseparability - checking the existence of an instance of a given concept	
					//top assertions about skolem individuals are handled by the IndividualManager and the ABoxManager
				}

				Atom atom = role instanceof AtomicRole ?
						Atom.create((AtomicRole) role, X, individuals[i]) : 
						Atom.create(((InverseRole) role).getInverseOf(), individuals[i], X);

				ret.add(DLClause.create(new Atom[] {atom}, bodyAtoms)); 
			}
			
			for (int i = 0; i < card; ++i)
				for (int j = i + 1; j < card; ++j)
					// TODO to be checked ... different as 
					ret.add(DLClause.create(new Atom[] {Atom.create(Inequality.INSTANCE, individuals[i], individuals[j])}, bodyAtoms)); 
							//DLClauseHelper.contructor_differentAs(individuals[i], individuals[j]));  
										
		}
		else if (predicate instanceof AtLeastDataRange) {
			// TODO to be implemented ... 
		}
		else
			ret.add(DLClause.create(new Atom[] {headAtom}, bodyAtoms)); 
		
		return ret; 
	}
	
	class DisjunctiveHeads implements Iterator<DLClause> {

		Atom[] bodyAtoms; 
		Atom[][] disjunctHeadAtoms; 
		int[] pointer;
		int length, l; 
		LinkedList<DLClause> auxiliaryClauses = new LinkedList<DLClause>();  

		public DisjunctiveHeads(DLClause clause, DLClause originalClause) {
			length = clause.getHeadLength();
			
			bodyAtoms = clause.getBodyAtoms(); 
			disjunctHeadAtoms = new Atom[length][];
			pointer = new int[length];
			if (length > 0) l = length - 1;
			else length = 0; 
			
			int index = 0, offset = 0;
			Collection<DLClause> datalogRules;
			DLClause newClause; 
			for (Atom headAtom: clause.getHeadAtoms()) {
				pointer[index] = 0;
				
				datalogRules = overApprox(headAtom, bodyAtoms, originalClause, offset);
				
				if (datalogRules.isEmpty()) {
					l = -1; 
					auxiliaryClauses.clear(); 
					return ; 
				}
				
				for (Iterator<DLClause> iter = datalogRules.iterator(); iter.hasNext(); ) {
					newClause = iter.next(); 
					if (!DLClauseHelper.hasSubsetBodyAtoms(newClause, clause)) {
						auxiliaryClauses.add(newClause); 
						iter.remove();
					}
				}
				
				disjunctHeadAtoms[index] = new Atom[datalogRules.size()];
				
				int j = 0; 
				for (DLClause disjunct: datalogRules) {
					disjunctHeadAtoms[index][j++] = disjunct.getHeadAtom(0); 
				}
				
				++index; 
				if (headAtom.getDLPredicate() instanceof AtLeast) ++offset;
				
			}
				
		}

		@Override
		public boolean hasNext() {
			return l != -1 || !auxiliaryClauses.isEmpty(); 
		}

		@Override
		public DLClause next() {
			if (l == -1) 
				return auxiliaryClauses.removeFirst(); 
			
			if (length > 0) {
				Atom[] headAtoms = new Atom[length]; 
				for (int i = 0; i < length; ++i)
					headAtoms[i] = disjunctHeadAtoms[i][pointer[i]];
				
				++pointer[l];
				while (l >= 0 && pointer[l] >= disjunctHeadAtoms[l].length) {
					pointer[l] = 0; 
					--l; 
					if (l >= 0) 
						++pointer[l];
				}
				
				if (l >= 0) l = length - 1; 
				
				return DLClauseHelper.simplified(DLClause.create(headAtoms, bodyAtoms));  
			}
			else {
				--l; 
				return DLClauseHelper.simplified(DLClause.create(new Atom[0], bodyAtoms));
			}
		}

		@Override
		public void remove() { }
		
	}
	
}
