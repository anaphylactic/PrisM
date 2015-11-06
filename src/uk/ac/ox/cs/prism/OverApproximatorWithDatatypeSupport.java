package uk.ac.ox.cs.prism;

import uk.ac.ox.cs.prism.clausification.DatatypeManager;

public class OverApproximatorWithDatatypeSupport extends OverApproxForTailoredModuleExtraction{

	
	public OverApproximatorWithDatatypeSupport(IndividualManager indManager, DatatypeManager dtManager){
		super(indManager, dtManager);
		approxExist = new OverApproxExistWithDatatypeSupport(indManager, dtManager);
	}

//	public Collection<DLClause> convert(DLClause clause, DLClause originalClause) {
//		Collection<DLClause> ret = new LinkedList<DLClause>(); 
//		for (DLClause tClause : approxDist.convert(clause, originalClause))
//			ret.addAll(approxExist.convert(tClause, originalClause)); 
//		
//		return avoidLongBodies(ret); 
//	}
	
}

