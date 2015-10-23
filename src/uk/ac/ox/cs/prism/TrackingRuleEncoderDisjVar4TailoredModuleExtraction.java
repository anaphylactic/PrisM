package uk.ac.ox.cs.prism;




//@Deprecated
//public class TrackingRuleEncoderDisjVar4TailoredModuleExtraction extends TrackingRuleEncoder4TailoredModuleExtraction{
//	//based on TrackingRuleEncoderDisjVar1 from PAGOdA
//	//currently incompatible with the rest of the project, substantial changes need to be done to be able to reuse this 
//
//	
//	public TrackingRuleEncoderDisjVar4TailoredModuleExtraction(
//			ModuleExtractionUpperProgram program) {
//		super(program);
//	}
//	
//	private Set<DLClause> disjunctiveRules = new HashSet<DLClause>();
//
//	@Override
//	public boolean encodingRulesAndAssertions() {
//		if (super.encodingRulesAndAssertions()) {
//			processDisjunctiveRules();
//			return true; 
//		}
//		return false; 
//	}
//	
//	
//	
//	@Override
//	protected void encodingRule(DLClause clause) {
//		Set<DLClause> original = program.getCorrespondingClauses(clause);
//		//deal with all disjunctive corresponding clauses one by one 
//		//and with all other corresponding clauses all at once.
//		boolean nonDijsunctiveDone = false;
//		for (DLClause orig : original){
//			if (orig.getHeadLength() <= 1){ 
//				if (!nonDijsunctiveDone){
//					super.encodingRule(clause);
//					nonDijsunctiveDone = true;
//				} 
//			}
//			else {
//				addDisjunctiveRule(orig);
//			}			
//		}
//	}
//	private void addDisjunctiveRule(DLClause clause) {
//		disjunctiveRules.add(clause);
//	}
//
//	
//	//if one selected clause corresponds to several clauses and some of the are disjunctive, 
//	//these will have been treated separately and will have their own indexes assigned,
//	//so if some selected rules corresponds to several rules we olny need to take the ones 
//	//that are NOT disjunctive
//	public Set<DLClause> getSelectedClauses(String iri) {
//		Set<DLClause> clauses = super.getSelectedClauses(iri);
//		if (clauses.size() > 1){
//			Iterator<DLClause> iter = clauses.iterator();
//			while (iter.hasNext()){
//				DLClause c = iter.next();
//				if (c.getHeadLength() > 1)
//					iter.remove();
//			}
//		}
//		return clauses;
//	}
//
//
//	private void processDisjunctiveRules() {
//		for (DLClause clause: disjunctiveRules)
//			encodingDisjunctiveRule(clause);
//	}
//	
//	private void encodingDisjunctiveRule(DLClause clause) {
//		int headLength = clause.getHeadLength();
//		
//		Atom[] trackingAtoms = new Atom[headLength];
//		for (int i = 0; i < headLength; ++i)
//			trackingAtoms[i] = getTrackingAtom(clause.getHeadAtom(i));
//		
//		Atom[] bodyAtoms = clause.getBodyAtoms();
//		
//		LinkedList<Atom> newHeadAtoms = new LinkedList<Atom>();
//		DLPredicate selected = AtomicConcept.create(getSelectedPredicate()); 
//		newHeadAtoms.add(Atom.create(selected, getIndividual4GeneralRule(clause)));
//		
//		for (Atom atom: bodyAtoms) {
//			Atom newAtom = Atom.create(
//					getTrackingDLPredicate(atom.getDLPredicate()), 
//					DLClauseHelper.getArguments(atom));
//			newHeadAtoms.add(newAtom);
//		}
//
//		DLClause newClause;
//		int index; 
//		for (int j = 0; j < headLength; ++j) {
//			Atom[] newBodyAtoms = new Atom[headLength + bodyAtoms.length];
//			index = 0; 
//			for (int i = 0; i < headLength; ++i, ++index)
//				newBodyAtoms[index] = trackingAtoms[i]; 
//			
//			for (int i = 0; i < bodyAtoms.length; ++i, ++index)
//				newBodyAtoms[index] = bodyAtoms[i]; 
//			
//			for (Atom atom: newHeadAtoms) {
//				newClause = DLClause.create(new Atom[] {atom}, newBodyAtoms); 
//				addTrackingClause(newClause);
//			}
//		}
//	}
//	
//	private Atom getTrackingAtom(Atom headAtom) {
//		DLPredicate p = headAtom.getDLPredicate(); 
//		if (p instanceof AtLeast) {
//			p = Normalisation.toAtLeastConcept((AtLeast) p); 
//			return Atom.create(getTrackingDLPredicate(AtomicConcept.create(Normalisation.getAuxiliaryConcept4Disjunct((AtLeastConcept) p))), headAtom.getArgument(0)); 
//		}
//		if (p instanceof AtomicConcept) 
//			return Atom.create(getTrackingDLPredicate((AtomicConcept) p), headAtom.getArgument(0)); 
//		if (p instanceof AtomicRole) 
//			return Atom.create(getTrackingDLPredicate((AtomicRole) p), headAtom.getArgument(0), headAtom.getArgument(1));
//		if (p instanceof Equality || p instanceof AnnotatedEquality) 
//			return Atom.create(getTrackingDLPredicate(Equality.INSTANCE), headAtom.getArgument(0), headAtom.getArgument(1)); 
//		if (p instanceof Inequality) 
//			return Atom.create(getTrackingDLPredicate((Inequality) p), headAtom.getArgument(0), headAtom.getArgument(1)); 
//
//		return null;
//	}
//
//	private void addTrackingClause(DLClause clause) {
//		trackingClauses.add(clause); 
//	}
//	
//	@Override
//	public String getTrackingProgram() {
//		StringBuilder sb = getTrackingProgramBody();
//		sb.insert(0, MyPrefixes.PAGOdAPrefixes.prefixesText()); 
//		return sb.toString(); 
//	}
//
//}
