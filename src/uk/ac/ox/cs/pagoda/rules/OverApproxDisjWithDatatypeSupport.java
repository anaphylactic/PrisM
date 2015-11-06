package uk.ac.ox.cs.pagoda.rules;


import java.util.Collection;
import java.util.LinkedList;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicNegationDataRange;
import org.semanticweb.HermiT.model.DLClause;

import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;

public class OverApproxDisjWithDatatypeSupport extends OverApproxDisj {

	@Override
	public Collection<DLClause> convert(DLClause clause, DLClause originalClause) {
		LinkedList<DLClause> distincts = new LinkedList<DLClause>();
		Atom[] headAtoms = clause.getHeadAtoms(), bodyAtoms = clause.getBodyAtoms();
		LinkedList<DLClause> newClauses = new LinkedList<DLClause>();
		DLClause newClause;
		if (headAtoms.length > 1) {
			for (Atom headAtom: headAtoms) {
				//remove all heads where the predicate is an instance of NegatedAtomicDataRange
				if (!(headAtom.getDLPredicate() instanceof AtomicNegationDataRange)) {
					newClause = DLClause.create(new Atom[] {headAtom}, bodyAtoms);
					newClauses.add(newClause);	
				}
			}

			for (DLClause cls: newClauses) {
				newClause = DLClauseHelper.simplified(cls);
				if (!isSubsumedBy(newClause, distincts)) 
					distincts.add(newClause);
			}
		}
		else if (headAtoms.length > 0 && !(headAtoms[0].getDLPredicate() instanceof AtomicNegationDataRange))
			distincts.add(clause);

		if (distincts.isEmpty())
			distincts.add(DLClause.create(new Atom[0], bodyAtoms));
		
		return distincts;
	}

}
