package uk.ac.ox.cs.prism;

import java.util.Collection;
import java.util.LinkedList;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.AtLeastDataRange;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.ConstantEnumeration;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.LiteralDataRange;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;

import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.hermit.RuleHelper;
import uk.ac.ox.cs.prism.clausification.DatatypeManager;



public class OverApproxExistWithDatatypeSupport extends OverApproxExistForModuleExtraction {

	protected DatatypeManager datatypeManager;
	
	public OverApproxExistWithDatatypeSupport(IndividualManager iManager, DatatypeManager dManager){
		super(iManager);
		datatypeManager = dManager;
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
			if (card > 2) card = 2; //because of how the disjunctions are approximated, there will be no difference here between creating two or more distinct successors
			Individual[] individuals = new Individual[card];
			for (int i = 0; i < card; ++i) 
				individuals[i] = indManager.getSkolemIndividual(originalClause, offset + i);
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
			AtLeastDataRange atLeastDataRange = (AtLeastDataRange) predicate;
			LiteralDataRange range = atLeastDataRange.getToDataRange();
			Role role = atLeastDataRange.getOnRole();
			
			if (atLeastDataRange.getNumber() == 1) {
				if (range instanceof ConstantEnumeration) {
					if (((ConstantEnumeration) range).getNumberOfConstants() > 1)
						throw new IllegalArgumentException("this should have been normalised in the clausification stage");
					else {
						ret.add(DLClause.create(new Atom[]{
								Atom.create(
										(AtomicRole) role, 
										headAtom.getArgument(0),
										((ConstantEnumeration) range).getConstant(0))
								}, bodyAtoms));
					}
				}
				else {
					Constant[] c = datatypeManager.getConstantsForDataType(range, atLeastDataRange.getNumber());
					ret.add(DLClause.create(new Atom[]{
							Atom.create(
									(AtomicRole) role, 
									headAtom.getArgument(0),
									c[0])
							}, bodyAtoms));
				}
			}
			else {
				Constant[] c = datatypeManager.getConstantsForDataType(range, atLeastDataRange.getNumber());
				ret.add(DLClause.create(new Atom[]{
						Atom.create(
								(AtomicRole) role, 
								headAtom.getArgument(0),
								c[0])
						}, bodyAtoms));
				if (c[1] == null)
					ret.add(DLClause.create(new Atom[0], bodyAtoms));
				else
					ret.add(DLClause.create(new Atom[]{
							Atom.create(
									(AtomicRole) role, 
									headAtom.getArgument(0),
									c[1])
							}, bodyAtoms));
			}
		}
		else if (predicate instanceof LiteralDataRange) {
			
			Atom[] newBodyAtoms = new Atom[bodyAtoms.length+2];
			int i;
			for (i = 0; i<bodyAtoms.length; i++)
				newBodyAtoms[i] = bodyAtoms[i]; 
			Variable freshVar = DLClauseHelper.getFreshVariable(bodyAtoms);
			newBodyAtoms[i++] = BindDatatypeAtom.create(headAtom.getArgument(0),freshVar);
			newBodyAtoms[i++] = FilterAtom.create(freshVar, (LiteralDataRange) predicate);
			ret.add(DLClause.create(new Atom[0], newBodyAtoms));
		}
		else 
			ret.add(DLClause.create(new Atom[] {headAtom}, bodyAtoms)); 
		
		return ret; 
	}
	
	
	public static class BindDatatypeAtom extends Atom {
		private static final long serialVersionUID = -72728938484538310L;
		public static BindDatatypeAtom create(Term t, Variable x){
			return new BindDatatypeAtom(t, x);
		}
		protected BindDatatypeAtom(Term t, Variable x) {
			super(BindDatatypePredicate.create(), new Term[]{t, x});
	    }
		public String toString(Prefixes prefixes) {
	        return ((BindDatatypePredicate) m_dlPredicate).toString(m_arguments[0], (Variable) m_arguments[1], prefixes);
	    }
	}
	
	public static class BindDatatypePredicate implements DLPredicate {

		public static BindDatatypePredicate create() {
			return new BindDatatypePredicate();
		}
		
		@Override
		public int getArity() {
			return 2;
		}

		@Override
		public String toString(Prefixes prefixes) {
			throw new IllegalAccessError("a term and a variable must be supplied to get a representation of a BindDatatypePredicate");
		}
		
		public String toString(Term t, Variable x, Prefixes prefixes) {
			StringBuilder sb = new StringBuilder();
			sb.append("BIND(DATATYPE");
			sb.append("(");
			sb.append(RuleHelper.getText(t));
			sb.append(")");
			sb.append(" ");
			sb.append("AS");
			sb.append(" ");
			sb.append(RuleHelper.getText(x));
			sb.append(")");
			return sb.toString();
		}
		
	}
	public static class FilterAtom extends Atom {
		
		private static final long serialVersionUID = 8137038408877489864L;
		public static FilterAtom create(Term t, LiteralDataRange range){
			return new FilterAtom(t, range);
		}
		protected FilterAtom(Term t, LiteralDataRange range) {
			super(FilterPredicate.create(range), new Term[]{t});
	    }
		@Override
		public String toString(Prefixes prefixes) {
			return ((FilterPredicate) m_dlPredicate).toString(m_arguments[0]);
	    }
		public String toString() {
			return toString(Prefixes.STANDARD_PREFIXES);
	    }
	}
	static class FilterPredicate implements DLPredicate {

		LiteralDataRange m_range;
		
		public static FilterPredicate create(LiteralDataRange range) {
			return new FilterPredicate(range);
		}
		
		protected FilterPredicate(LiteralDataRange range) {
			m_range = range;
		}
		
		@Override
		public int getArity() {
			return 1;
		}

		@Override
		public String toString(Prefixes prefixes) {
			throw new IllegalAccessError("a term must be supplied to get a representation of a FilterPredicate");
		}
		
		public String toString(Term t) {
			StringBuilder sb = new StringBuilder();
			sb.append("FILTER");
			sb.append("(");
			sb.append(RuleHelper.getText(t));
			sb.append(" ");
			sb.append("!=");
			sb.append(" ");
			sb.append(m_range.toString(Prefixes.STANDARD_PREFIXES));
			sb.append(")");
			return sb.toString();
		}
		
	}
	
}
