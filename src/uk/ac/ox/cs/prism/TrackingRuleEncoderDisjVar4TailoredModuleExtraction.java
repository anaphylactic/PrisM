package uk.ac.ox.cs.prism;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.AtLeast;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.owlapi.model.OWLAxiom;

import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.multistage.Normalisation;

@Deprecated
public class TrackingRuleEncoderDisjVar4TailoredModuleExtraction extends TrackingRuleEncoder4TailoredModuleExtraction{
	//based on TrackingRuleEncoderDisjVar1 from PAGOdA

	
	public TrackingRuleEncoderDisjVar4TailoredModuleExtraction(
			ModuleExtractionUpperProgram program) {
		super(program);
	}
	
	private Set<DLClause> disjunctiveRules = new HashSet<DLClause>();

	@Override
	public boolean encodingRulesAndAssertions() {
		if (super.encodingRulesAndAssertions()) {
			processDisjunctiveRules();
			return true; 
		}
		return false; 
	}
	
	
	
	@Override
	protected void encodingRule(DLClause clause) {
		Set<DLClause> original = program.getCorrespondingClauses(clause);
		//deal with all disjunctive corresponding clauses one by one 
		//and with all other corresponding clauses all at once.
		boolean nonDijsunctiveDone = false;
		for (DLClause orig : original){
			if (orig.getHeadLength() <= 1){ 
				if (!nonDijsunctiveDone){
					super.encodingRule(clause);
					nonDijsunctiveDone = true;
				} 
			}
			else {
				addDisjunctiveRule(orig);
			}			
		}
	}
	private void addDisjunctiveRule(DLClause clause) {
		disjunctiveRules.add(clause);
	}

	
	//if one selected clause corresponds to several clauses and some of the are disjunctive, 
	//these will have been treated separately and will have their own indexes assigned,
	//so if some selected rules corresponds to several rules we olny need to take the ones 
	//that are NOT disjunctive
	public Set<DLClause> getSelectedClauses(String iri) {
		Set<DLClause> clauses = super.getSelectedClauses(iri);
		if (clauses.size() > 1){
			Iterator<DLClause> iter = clauses.iterator();
			while (iter.hasNext()){
				DLClause c = iter.next();
				if (c.getHeadLength() > 1)
					iter.remove();
			}
		}
		return clauses;
	}


	private void processDisjunctiveRules() {
		for (DLClause clause: disjunctiveRules)
			encodingDisjunctiveRule(clause);
	}
	
	private void encodingDisjunctiveRule(DLClause clause) {
		int headLength = clause.getHeadLength();
		
		Atom[] trackingAtoms = new Atom[headLength];
		for (int i = 0; i < headLength; ++i)
			trackingAtoms[i] = getTrackingAtom(clause.getHeadAtom(i));
		
		Atom[] bodyAtoms = clause.getBodyAtoms();
		
		LinkedList<Atom> newHeadAtoms = new LinkedList<Atom>();
		DLPredicate selected = AtomicConcept.create(getSelectedPredicate()); 
		newHeadAtoms.add(Atom.create(selected, getIndividual4GeneralRule(clause)));
		
		for (Atom atom: bodyAtoms) {
			Atom newAtom = Atom.create(
					getTrackingDLPredicate(atom.getDLPredicate()), 
					DLClauseHelper.getArguments(atom));
			newHeadAtoms.add(newAtom);
		}

		DLClause newClause;
		int index; 
		for (int j = 0; j < headLength; ++j) {
			Atom[] newBodyAtoms = new Atom[headLength + bodyAtoms.length];
			index = 0; 
			for (int i = 0; i < headLength; ++i, ++index)
				newBodyAtoms[index] = trackingAtoms[i]; 
			
			for (int i = 0; i < bodyAtoms.length; ++i, ++index)
				newBodyAtoms[index] = bodyAtoms[i]; 
			
			for (Atom atom: newHeadAtoms) {
				newClause = DLClause.create(new Atom[] {atom}, newBodyAtoms); 
				addTrackingClause(newClause);
			}
		}
	}
	
	private Atom getTrackingAtom(Atom headAtom) {
		DLPredicate p = headAtom.getDLPredicate(); 
		if (p instanceof AtLeast) {
			p = Normalisation.toAtLeastConcept((AtLeast) p); 
			return Atom.create(getTrackingDLPredicate(AtomicConcept.create(Normalisation.getAuxiliaryConcept4Disjunct((AtLeastConcept) p))), headAtom.getArgument(0)); 
		}
		if (p instanceof AtomicConcept) 
			return Atom.create(getTrackingDLPredicate((AtomicConcept) p), headAtom.getArgument(0)); 
		if (p instanceof AtomicRole) 
			return Atom.create(getTrackingDLPredicate((AtomicRole) p), headAtom.getArgument(0), headAtom.getArgument(1));
		if (p instanceof Equality || p instanceof AnnotatedEquality) 
			return Atom.create(getTrackingDLPredicate(Equality.INSTANCE), headAtom.getArgument(0), headAtom.getArgument(1)); 
		if (p instanceof Inequality) 
			return Atom.create(getTrackingDLPredicate((Inequality) p), headAtom.getArgument(0), headAtom.getArgument(1)); 

		return null;
	}

	private void addTrackingClause(DLClause clause) {
		trackingClauses.add(clause); 
	}

	
//	
////	private Atom getAuxiliaryAtom(Atom headAtom) {
////		DLPredicate p = headAtom.getDLPredicate(); 
////		if (p instanceof AtLeast || p instanceof AtLeast) {
////			return Atom.create(generateAuxiliaryRule((AtLeast) p, true), headAtom.getArgument(0)); 
////		}
////		if (p instanceof AtomicConcept) 
////			return Atom.create(generateAuxiliaryRule((AtomicConcept) p), headAtom.getArgument(0)); 
////		if (p instanceof AtomicRole) 
////			return Atom.create(generateAuxiliaryRule((AtomicRole) p), headAtom.getArgument(0), headAtom.getArgument(1));
////		if (p instanceof Equality || p instanceof AnnotatedEquality) 
////			return Atom.create(generateAuxiliaryRule(Equality.INSTANCE), headAtom.getArgument(0), headAtom.getArgument(1)); 
////		if (p instanceof Inequality) 
////			return Atom.create(generateAuxiliaryRule((Inequality) p), headAtom.getArgument(0), headAtom.getArgument(1)); 
////
////		return null;
////	}
//
//
//	
//	
//
//	
//	private DLPredicate getAuxPredicate(DLPredicate p) {
//		if (p instanceof AtLeastConcept) {
//			StringBuilder builder = new StringBuilder(
//					Normalisation.getAuxiliaryConcept4Disjunct((AtLeastConcept) p));
//			builder.append("_AUXa").append(currentQuery.getQueryID()); 
//			return AtomicConcept.create(builder.toString()); 
//		}
//		
//		return getDLPredicate(p, "_AUXa" + currentQuery.getQueryID());
//	}
//
////	private DLPredicate getTrackingBottomDLPredicate(DLPredicate p) {
////		return getDLPredicate(p, getTrackingSuffix("0"));
////	}
//
//	private DLPredicate generateAuxiliaryRule(AtLeast p1, boolean withAux) {
//		AtLeastConcept p = Normalisation.toAtLeastConcept(p1); 
//		
//		int num = p.getNumber(); 
//		Variable[] Ys = new Variable[num]; 
//		if (num > 1)
//			for (int i = 0; i < num; ++i) 
//				Ys[i] = Variable.create("Y" + (i + 1));
//		else 
//			Ys[0] = Y; 
//		
//		Collection<Atom> expandedAtom = new LinkedList<Atom>(); 
//		Collection<Atom> representativeAtom = new LinkedList<Atom>(); 
//		if (p.getOnRole() instanceof AtomicRole) {
//			AtomicRole r = (AtomicRole) p.getOnRole(); 
//			for (int i = 0; i < num; ++i) 
//				expandedAtom.add(Atom.create(r, X, Ys[i]));
//			representativeAtom.add(Atom.create(r, X, Ys[0])); 
//		}
//		else {
//			AtomicRole r = ((InverseRole) p.getOnRole()).getInverseOf(); 
//			for (int i = 0; i < num; ++i) 
//				expandedAtom.add(Atom.create(r, Ys[i], X));
//			representativeAtom.add(Atom.create(r, Ys[0], X)); 
//		}
//		
//		if (num > 1) {
//			representativeAtom.add(Atom.create(Inequality.INSTANCE, Ys[0], Ys[1])); 
//		}
//		for (int i = 0; i < num; ++i)
//			for (int j = i + 1; j < num; ++j)
//				expandedAtom.add(Atom.create(Inequality.INSTANCE, Ys[i], Ys[j])); 
//		
//		if (!p.getToConcept().equals(AtomicConcept.THING)) {
//			AtomicConcept c; 
//			if (p.getToConcept() instanceof AtomicConcept) 
//				c = (AtomicConcept) p.getToConcept();
//			else {
//				c = OverApproxExist.getNegationConcept(((AtomicNegationConcept) p.getToConcept()).getNegatedAtomicConcept());
//			}
//			for (int i = 0; i < num; ++i)
//				expandedAtom.add(Atom.create(c, Ys[i])); 
//			representativeAtom.add(Atom.create(c, Ys[0]));
//		}
//
//		AtomicConcept ac = AtomicConcept.create(Normalisation.getAuxiliaryConcept4Disjunct(p));
//		DLPredicate trackingPredicate = getTrackingDLPredicate(ac); 
////		DLPredicate gapPredicate = getGapDLPredicate(ac); 
//		DLPredicate auxPredicate = withAux ? getAuxPredicate(p) : null;
//		
//		for (Atom atom: representativeAtom) {
//			Atom[] bodyAtoms = new Atom[expandedAtom.size() + 1]; 
//			if (atom.getArity() == 1)
//				bodyAtoms[0] = Atom.create(getTrackingDLPredicate(atom.getDLPredicate()), atom.getArgument(0));
//			else 
//				bodyAtoms[0] = Atom.create(getTrackingDLPredicate(atom.getDLPredicate()), atom.getArgument(0), atom.getArgument(1));
//			int i = 0; 
//			for (Atom bodyAtom: expandedAtom)
//				bodyAtoms[++i] = bodyAtom;  
//			addTrackingClause(DLClause.create(new Atom[] {Atom.create(trackingPredicate, X)}, bodyAtoms));
//			
//			bodyAtoms = new Atom[expandedAtom.size() + 1]; 
//			if (atom.getArity() == 1)
//				bodyAtoms[0] = Atom.create(getGapDLPredicate(atom.getDLPredicate()), atom.getArgument(0));
//			else 
//				bodyAtoms[0] = Atom.create(getGapDLPredicate(atom.getDLPredicate()), atom.getArgument(0), atom.getArgument(1));
//			i = 0; 
//			for (Atom bodyAtom: expandedAtom)
//				bodyAtoms[++i] = bodyAtom;  
//			addTrackingClause(DLClause.create(new Atom[] {Atom.create(gapPredicate, X)}, bodyAtoms));
//			
////			if (withAux) {
////				bodyAtoms = new Atom[expandedAtom.size() + 1]; 
////				bodyAtoms[0] = getAuxiliaryAtom(atom);
////				i = 0; 
////				for (Atom bodyAtom: expandedAtom)
////					bodyAtoms[++i] = bodyAtom;  
////				addTrackingClause(DLClause.create(new Atom[] {Atom.create(auxPredicate, X)}, bodyAtoms));
////			}
//		}
//		
//		return withAux ? auxPredicate : trackingPredicate;
//	}
//
////	private DLPredicate generateAuxiliaryRule(AtomicRole p) {
//////		if (currentQuery.isBottom()) 
//////			return getTrackingDLPredicate(p);
////		
////		DLPredicate ret = getAuxPredicate(p); 
////		Atom[] headAtom = new Atom[] {Atom.create(ret, X, Y)};
////
////		addTrackingClause(
////				DLClause.create(headAtom, new Atom[] {Atom.create(getTrackingDLPredicate(p), X, Y)})); 
//////		addTrackingClause(
//////				DLClause.create(headAtom, new Atom[] {Atom.create(getTrackingBottomDLPredicate(p), X, Y)})); 
////		
////		return ret; 
////	}
//	
//	private Variable X = Variable.create("X"), Y = Variable.create("Y"); 
//
////	private DLPredicate generateAuxiliaryRule(AtomicConcept p) {
//////		if (currentQuery.isBottom())
//////			return getTrackingDLPredicate(p); 
////		
////		DLPredicate ret = getAuxPredicate(p); 
////		Atom[] headAtom = new Atom[] {Atom.create(ret, X)}; 
////		addTrackingClause(
////				DLClause.create(headAtom, 
////						new Atom[] { Atom.create(getTrackingDLPredicate(p), X)})); 
//////		addTrackingClause(
//////				DLClause.create(headAtom, 
//////						new Atom[] { Atom.create(getTrackingBottomDLPredicate(p), X)}));
////		
////		return ret; 
////	}
//
////	private DLPredicate generateAuxiliaryRule(Equality instance) {
////		return generateAuxiliaryRule(AtomicRole.create(Namespace.EQUALITY));
////	}
////
////	private DLPredicate generateAuxiliaryRule(Inequality instance) {
////		return generateAuxiliaryRule(AtomicRole.create(Namespace.INEQUALITY)); 
////	}
	
	@Override
	public String getTrackingProgram() {
		StringBuilder sb = getTrackingProgramBody();
		sb.insert(0, MyPrefixes.PAGOdAPrefixes.prefixesText()); 
		return sb.toString(); 
	}

}
