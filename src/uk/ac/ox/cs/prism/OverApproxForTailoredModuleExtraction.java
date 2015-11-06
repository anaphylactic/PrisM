package uk.ac.ox.cs.prism;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Variable;

import uk.ac.ox.cs.pagoda.rules.Approximator;
import uk.ac.ox.cs.pagoda.rules.OverApproxDisjWithDatatypeSupport;
import uk.ac.ox.cs.prism.clausification.DatatypeManager;

public class OverApproxForTailoredModuleExtraction implements Approximator{

	protected Approximator approxDist;
	protected Approximator approxExist;
	protected static int freshPredicateCounter = 0;
	
	public OverApproxForTailoredModuleExtraction(IndividualManager indManager, DatatypeManager dtManager){
		approxDist = new OverApproxDisjWithDatatypeSupport();
		approxExist = new OverApproxExistWithDatatypeSupport(indManager, dtManager);
	}

	public Collection<DLClause> convert(DLClause clause, DLClause originalClause) {
		Collection<DLClause> ret = new LinkedList<DLClause>(); 
		for (DLClause tClause : approxDist.convert(clause, originalClause))
			ret.addAll(approxExist.convert(tClause, originalClause)); 
		
		return avoidLongBodies(ret); 
	}
	
	
	
	public Collection<DLClause> avoidLongBodies(Collection<DLClause> clauses){
		Collection<DLClause> ret = new LinkedList<DLClause>(); 
		for (DLClause clause : clauses){
			Set<Variable> headVars = new HashSet<Variable>();
			for (Atom at : clause.getHeadAtoms())
				at.getVariables(headVars);
			
			
			if (clause.getBodyAtoms().length > 5){				
				Map<Set<Atom>, Set<Variable>> newBodies = splitBody(clause.getBodyAtoms(), headVars, false);
				ret.addAll(makeClausesWithNewBodies(clause.getHeadAtoms(), headVars, newBodies));
			}
			else
				ret.add(clause);
		}
		
		return ret; 
	}

	
	
	protected Map<Set<Atom>, Set<Variable>> splitBody(Atom[] body, Set<Variable> headVars, boolean forceExtraHeadVars){
		Map<Set<Atom>, Set<Variable>> newBodies = new HashMap<Set<Atom>, Set<Variable>>();
		if (forceExtraHeadVars) headVars.addAll(getExtraHeadVars(body,headVars,1));
		else {
			int diff = 2-headVars.size();
			if (diff>0) headVars.addAll(getExtraHeadVars(body,headVars,diff));
		}

		Set<Atom> remainingBodyAtoms = new HashSet<Atom>(Arrays.asList(body));
		Iterator<Atom> iter = remainingBodyAtoms.iterator();

		//form groups of body atoms that have the same non-head variables
		Map<Set<Atom>,Set<Variable>> groupsAndHeadVars = new HashMap<Set<Atom>, Set<Variable>>();
		Set<Atom> currentGroup = new HashSet<Atom>();
		Set<Variable> currentGroupNonHeadVars = new HashSet<Variable>();
		Set<Variable> currentGroupHeadVars = new HashSet<Variable>();
		Atom previousToLastAtomAdded = null;
		Atom previousAtom = null;
		Atom atom;
		boolean startNewGroup = true; 
		while (iter.hasNext()){
			atom = iter.next();
			if (startNewGroup){
				iter.remove();
				currentGroup = new HashSet<Atom>();
				currentGroup.add(atom);

				atom.getVariables(currentGroupHeadVars = new HashSet<Variable>());
				currentGroupNonHeadVars = new HashSet<Variable>(currentGroupHeadVars);
				currentGroupNonHeadVars.removeAll(headVars);
				currentGroupHeadVars.removeAll(currentGroupNonHeadVars);
				
				previousToLastAtomAdded = null;
				previousAtom = null;
				startNewGroup = false;
				if (!iter.hasNext())
					groupsAndHeadVars.put(currentGroup,currentGroupHeadVars);		
			}
			else{
				Set<Variable> currentAtomHeadVars = new HashSet<Variable>();
				atom.getVariables(currentAtomHeadVars);
				Set<Variable> currentAtomNonHeadVars = new HashSet<Variable>(currentAtomHeadVars);
				currentAtomNonHeadVars.removeAll(headVars);
				currentAtomHeadVars.removeAll(currentAtomNonHeadVars);
				if ((currentAtomNonHeadVars.isEmpty() && currentGroupNonHeadVars.isEmpty()) ||
						currentAtomNonHeadVars.removeAll(currentGroupNonHeadVars)){
					//then add this atom to the current group
					currentGroup.add(atom);
					iter.remove();
					currentGroupHeadVars.addAll(currentAtomHeadVars);
					currentGroupNonHeadVars.addAll(currentAtomNonHeadVars);
					previousToLastAtomAdded = previousAtom;
					if (!iter.hasNext())
						if (remainingBodyAtoms.isEmpty()){
							if (currentGroupNonHeadVars.isEmpty() && currentGroup.size() > 5)
								groupsAndHeadVars.putAll(breakBodyWithNoNonHeadVars(currentGroup, currentGroupHeadVars));
							else
								groupsAndHeadVars.put(currentGroup,currentGroupHeadVars);
						}
						else
							iter = remainingBodyAtoms.iterator();
				}
				else{
					if (atom.equals(previousToLastAtomAdded) || (previousToLastAtomAdded == null && !iter.hasNext())){
						//then we have completed one full round of the iterator without adding anything to the current group
						if (currentGroupNonHeadVars.isEmpty() && currentGroup.size() > 5)
							groupsAndHeadVars.putAll(breakBodyWithNoNonHeadVars(currentGroup, currentGroupHeadVars));	
						else
							groupsAndHeadVars.put(currentGroup,currentGroupHeadVars);
						startNewGroup = true;
						iter = remainingBodyAtoms.iterator();
					}
					else{
						previousAtom = atom;
						if (!iter.hasNext())
							iter = remainingBodyAtoms.iterator();
					}
				}
			}
		}
		
		//once the groups are formed, join together groups that are small
		Set<Atom> newBody = new HashSet<Atom>();
		Set<Variable> newHeadVars= new HashSet<Variable>();
		Set<Atom> newSmallBody = new HashSet<Atom>();
		Set<Variable> newSmallHeadVars= new HashSet<Variable>();
		for (Entry<Set<Atom>,Set<Variable>> entry : groupsAndHeadVars.entrySet()){
			newBody.addAll(entry.getKey());
			newHeadVars.addAll(entry.getValue());
			if (newBody.size() < 3){
				newSmallBody.addAll(newBody);
				newSmallHeadVars.addAll(newHeadVars);
				if (newSmallBody.size() > 2){
					newBodies.put(newSmallBody,newSmallHeadVars);
					newSmallBody = new HashSet<Atom>();
					newSmallHeadVars = new HashSet<Variable>();
				}
			}
			else{
				newBodies.put(newBody,newHeadVars);
			}
			newBody = new HashSet<Atom>();
			newHeadVars = new HashSet<Variable>();						
		}
		if (!newSmallBody.isEmpty())
			newBodies.put(newSmallBody,newSmallHeadVars);
		
		return newBodies;
	}

	protected Map<Set<Atom>,Set<Variable>> breakBodyWithNoNonHeadVars(Set<Atom> body, Set<Variable> headVars){
		Map<Set<Atom>,Set<Variable>> ret = new HashMap<Set<Atom>, Set<Variable>>();
		
		Set<Atom> currentGroup = new HashSet<Atom>();
		Set<Variable> currentGroupHeadVars = new HashSet<Variable>();
		Iterator<Atom> iter = body.iterator();
		while (iter.hasNext()){
			Atom at = iter.next();
			currentGroup.add(at);
			at.getVariables(currentGroupHeadVars);
			if (currentGroup.size() == 4 || !iter.hasNext()) {
				currentGroupHeadVars.retainAll(headVars);
				ret.put(currentGroup, currentGroupHeadVars);
				currentGroup = new HashSet<Atom>();
				currentGroupHeadVars = new HashSet<Variable>();
			}
		}
		
		return ret;
	}
	
	protected Collection<DLClause> makeClausesWithNewBodies(Atom[] head, Set<Variable> headVars, Map<Set<Atom>, Set<Variable>> newBodies ){
		Collection<DLClause> ret  = new HashSet<DLClause>();
		
		//now create the new rules with auxiliary predicates in the head
		Set<Atom> newBodyAtomsForOriginalHead = new HashSet<Atom>();
		for (Entry<Set<Atom>,Set<Variable>> entry : newBodies.entrySet()){
			Atom[] body = getAsArray(entry.getKey());
			//introduce a fresh Predicate for every two head variables
			int counter = 0;
			Iterator<Variable> iterVar = entry.getValue().iterator();

			if (!iterVar.hasNext()){//then just choose one random variable or individual from the body
				AtomicConcept c = AtomicConcept.create("internal:def_shortenBodies#"+(freshPredicateCounter++));
				Atom newAtom = Atom.create(c, body[0].getArgument(0));
				newBodyAtomsForOriginalHead.add(newAtom);
				ret.add(DLClause.create(new Atom[]{newAtom}, body));
			}
			else{
				Variable[] pair = new Variable[2];
				while (iterVar.hasNext()){
					pair[(counter++)%2] = iterVar.next();
					if (pair[1] != null){
						AtomicRole role = AtomicRole.create("internal:def_shortenBodies#"+(freshPredicateCounter++));
						Atom newAtom = Atom.create(role, pair);
						newBodyAtomsForOriginalHead.add(newAtom);
						ret.add(DLClause.create(new Atom[]{newAtom}, body));
						pair = new Variable[2];
					}
					else if (!iterVar.hasNext()){
						AtomicConcept c = AtomicConcept.create("internal:def_shortenBodies#"+(freshPredicateCounter++));
						Atom newAtom = Atom.create(c, pair[0]);
						newBodyAtomsForOriginalHead.add(newAtom);
						ret.add(DLClause.create(new Atom[]{newAtom}, body));
					}
				}						
			}
		}
		DLClause shortenedClause = DLClause.create(head, getAsArray(newBodyAtomsForOriginalHead));
		if (shortenedClause.getBodyAtoms().length > 5){
			newBodies = splitBody(shortenedClause.getBodyAtoms(), headVars, false);
			ret.addAll(makeClausesWithNewBodies(shortenedClause.getHeadAtoms(), headVars, newBodies));
		}
		else ret.add(shortenedClause);

		return ret;
	}
	
	protected Collection<Variable> getExtraHeadVars(Atom[] body, Set<Variable> headVars, int nExtraVars){
		Collection<Variable> ret = new HashSet<Variable>();
		Map<Variable,Integer> bodyVarOccurrences = new HashMap<Variable, Integer>();
		for (Atom at : body){
			Set<Variable> vars = new HashSet<Variable>();
			at.getVariables(vars);
			vars.removeAll(headVars);
			for (Variable v : vars){
				Integer i = bodyVarOccurrences.get(v);
				if (i == null) i = 0;
				bodyVarOccurrences.put(v, i+1);
			}
		}
		Variable[] artifHeadVars = new Variable[2];
		Integer[] n = new Integer[]{-1,-1};
		if (!bodyVarOccurrences.isEmpty()){
			for (Entry<Variable,Integer> entry : bodyVarOccurrences.entrySet())
				if (entry.getValue() > n[0]){
						artifHeadVars[1] = artifHeadVars[0];
						n[1] = n[0];	
					artifHeadVars[0] = entry.getKey();
					n[0] = entry.getValue();
				}
				else if (artifHeadVars[1] == null){
					artifHeadVars[1] = entry.getKey();
					n[1] = entry.getValue();
				}
		}
		if (artifHeadVars[0] != null) ret.add(artifHeadVars[0]);
		if (nExtraVars>1 && artifHeadVars[1] != null) ret.add(artifHeadVars[1]);
		
		return ret;
	}
	
	
	protected Atom[] getAsArray(Collection<Atom> atoms){
		Atom[] ret = new Atom[atoms.size()];
		int i = 0;
		for (Atom atom : atoms)
			ret[i++] = atom;
		return ret;
	}
	
}

