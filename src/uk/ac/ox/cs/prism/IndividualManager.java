package uk.ac.ox.cs.prism;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.HermiT.model.AtLeast;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.ox.cs.prism.PrisM.InseparabilityRelation;
import uk.ac.ox.cs.prism.util.Utility_PrisM;

public class IndividualManager {

	boolean injectiveSkolemisation;
	int skolemisationIndividualCounter = 0;
	Map<DLClause, Integer> index4clause = new HashMap<DLClause, Integer>();
	
	boolean injectiveInstantiation;
	int instantiationIndividualCounter = 0;
	Map<OWLClass, Integer> index4Class;
	Map<OWLObjectProperty, Integer> index4Property;
	
	static Individual criticalInstance;
	boolean criticalInstanceHasBeenUsed = false;
	InseparabilityRelation insepRel;
	

	
	public IndividualManager(InseparabilityRelation insepRel){
		this.insepRel = insepRel;
		injectiveSkolemisation = 
				(insepRel == InseparabilityRelation.QUERY_INSEPARABILITY) ||
				(insepRel == InseparabilityRelation.WEAK_QUERY_INSEPARABILITY) ||
				(insepRel == InseparabilityRelation.FACT_INSEPARABILITY) || 
				(insepRel == InseparabilityRelation.IMPLICATION_INSEPARABILITY) || 
				(insepRel == InseparabilityRelation.CLASSIFICATION_INSEPARABILITY);
		injectiveInstantiation =  
				(insepRel == InseparabilityRelation.IMPLICATION_INSEPARABILITY) || 
				(insepRel == InseparabilityRelation.CLASSIFICATION_INSEPARABILITY);
		if (injectiveInstantiation){
			index4Class = new HashMap<OWLClass, Integer>();
			index4Property = new HashMap<OWLObjectProperty, Integer>();
		}
	}
	
	public Individual getInstanceIndividual(OWLEntity e){
		if (injectiveInstantiation){
			Integer i = getIndex4Entity(e);
			if (i != null)
				return getInstanceIndividualForIndex(i);
			else{
				i = assignFreshInstanceIndividual(e);
				return getInstanceIndividualForIndex(i);
			}
		}
		criticalInstanceHasBeenUsed = true;
		return getCriticalInstance();
	}
	public Individual[] getInstanceIndividuals(OWLObjectProperty p){
		if (injectiveInstantiation){
			Integer i = getIndex4Entity(p);
			if (i == null)
				i = assignFreshInstanceIndividual(p);
			return new Individual[]{getInstanceIndividualForIndex(i,0), getInstanceIndividualForIndex(i, 1)};
		}
		else return new Individual[]{getCriticalInstance(), getCriticalInstance()};
	}
	public Individual getSkolemIndividual(DLClause originalClause, int offset) {
		if (injectiveSkolemisation){
			Integer i = getIndex4Clause(originalClause);
			if (i == null)
				i = assignFreshSkolemIndividual(originalClause);
			return getSkolemIndividualForIndex(i+offset);
		}
		criticalInstanceHasBeenUsed = true;
		return getCriticalInstance();
	}
	
	protected Integer getIndex4Entity(OWLEntity e){//instantiation
		if (e instanceof OWLClass)
			return index4Class.get((OWLClass) e);
		else if (e instanceof OWLObjectProperty)
			return index4Property.get((OWLObjectProperty) e);
		else throw new IllegalArgumentException(e.toString());
	}
	protected Integer getIndex4Clause(DLClause cls){//skolemisation
		return index4clause.get(cls);
	}
	
	protected Integer assignFreshInstanceIndividual(OWLEntity e){
		Integer i = instantiationIndividualCounter;
		if (e instanceof OWLClass)
			index4Class.put((OWLClass) e, i);
		else if (e instanceof OWLObjectProperty)
			index4Property.put((OWLObjectProperty) e, i);
		else throw new IllegalArgumentException();
		instantiationIndividualCounter++;
		return i;
	}
	protected Integer assignFreshSkolemIndividual(DLClause originalClause){
		Integer i = skolemisationIndividualCounter;
		index4clause.put(originalClause, i);
		skolemisationIndividualCounter += noOfExistential(originalClause);
		return i;
	}
	
	public static Individual getCriticalInstance(){
		if (criticalInstance == null)
			criticalInstance = Individual.create(MyNamespace.TM_ANONY + "criticalInstance");
		return criticalInstance;
	}
	public Individual getInstanceIndividualForIndex(Integer i){
		return Individual.create(MyNamespace.TM_ANONY + "instantiation" + i);
	}
	public Individual getInstanceIndividualForIndex(Integer i, int position){
		return Individual.create(MyNamespace.TM_ANONY + "instantiation" + i+ "_" + position);
	}
	public Individual getSkolemIndividualForIndex(Integer i){
		return Individual.create(MyNamespace.TM_ANONY + "skolem" + i);
	}

	public String printTopFactsForAllIndividuals() throws Exception{
		StringBuilder sb = new StringBuilder();
		OWLClass top = new OWLDataFactoryImpl().getOWLThing();
		if (criticalInstanceHasBeenUsed)
			sb.append(Utility_PrisM.print(top, getCriticalInstance()));
		if (injectiveInstantiation){
			for (Entry<OWLClass, Integer> e : index4Class.entrySet()){
				sb.append(Utility_PrisM.print(top,getInstanceIndividualForIndex(e.getValue())));
			}
			for (Entry<OWLObjectProperty, Integer> e : index4Property.entrySet()){
				sb.append(Utility_PrisM.print(top,getInstanceIndividualForIndex(e.getValue())));
			}
		}
		if (injectiveSkolemisation){
			for (Entry<DLClause, Integer> e : index4clause.entrySet()){
				sb.append(Utility_PrisM.print(top,getSkolemIndividualForIndex(e.getValue())));
			}
		}
		return sb.toString();
	}
	
	public String printFactsForIndividualsFromTBox(Set<OWLNamedIndividual> individuals) throws Exception{
		StringBuilder sb = new StringBuilder();
		boolean makeAllEqualToCriticalInstace = criticalInstanceHasBeenUsed &&
				(insepRel.equals(InseparabilityRelation.FACT_INSEPARABILITY) || 
						insepRel.equals(InseparabilityRelation.QUERY_INSEPARABILITY) || 
						insepRel.equals(InseparabilityRelation.MODEL_INSEPARABILITY)); 

		OWLClass top = new OWLDataFactoryImpl().getOWLThing();
		String sameAs = "<http://www.w3.org/2002/07/owl#sameAs>";
		for (OWLIndividual i : individuals){
			Individual ind = Individual.create(i.toStringID());
			sb.append(Utility_PrisM.print(top, ind));
			if (makeAllEqualToCriticalInstace)
				sb.append(Utility_PrisM.print(sameAs, ind, IndividualManager.getCriticalInstance()));
		}

		return sb.toString();
	}
	
	
	private static int noOfExistential(DLClause originalClause) {
		int no = 0; 
		for (Atom atom: originalClause.getHeadAtoms())
			if (atom.getDLPredicate() instanceof AtLeast)
				no += ((AtLeast) atom.getDLPredicate()).getNumber(); 
		return no; 
	}
	
	public class MyNamespace extends uk.ac.ox.cs.pagoda.util.Namespace{
		
		public static final String TM_ANONY = "http://www.cs.ox.ac.uk/TailoredModulesExtractor#";
	 
	}

	
}
