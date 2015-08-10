package uk.ac.ox.cs.prism;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.Prefixes;
import uk.ac.ox.cs.JRDFox.model.GroundTerm;
import uk.ac.ox.cs.JRDFox.store.DataStore;
import uk.ac.ox.cs.JRDFox.store.Parameters;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;
import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.constraints.BottomStrategy;
import uk.ac.ox.cs.prism.PrisM.InseparabilityRelation;
import uk.ac.ox.cs.prism.util.Utility_tme;

public class ABoxManager {

	Set<OWLEntity> signature; 
	Set<OWLNamedIndividual> individualsFromTBox;
	InseparabilityRelation insepRel;
	IndividualManager indManager;
	StringBuilder initialAboxText = new StringBuilder();
	StringBuilder trackingAboxText = new StringBuilder();
	
	boolean loadABoxesByText = false;
	boolean twoDifferentIndividuals4PropertyInstantiation = true;

	public ABoxManager(Set<OWLEntity> sig, Set<OWLNamedIndividual> individuals, InseparabilityRelation insepRel, IndividualManager iManager){
		signature = sig;
		individualsFromTBox = individuals;
		this.insepRel = insepRel;
		indManager = iManager;
	}

	public void createInitialABox(String initialABoxFileName) throws Exception{
		
		PrintWriter out = new PrintWriter(new File(initialABoxFileName));
		
		if (!insepRel.equals(InseparabilityRelation.WEAK_QUERY_INSEPARABILITY)){
			for (OWLEntity e : signature){
				if (e instanceof OWLClass){
					addToInitialABox(Utility_tme.print((OWLClass) e, indManager.getInstanceIndividual(e)), out);
				}
				else if (e instanceof OWLObjectProperty){
					if (twoDifferentIndividuals4PropertyInstantiation){//we don't need to ask about the module type, the individual manager will know that already
						Individual[] i = indManager.getInstanceIndividuals((OWLObjectProperty) e);
						addToInitialABox(Utility_tme.print((OWLObjectProperty) e, i[0], i[1]),out);
					}
					else{
						Individual i = indManager.getInstanceIndividual(e);
						addToInitialABox(Utility_tme.print((OWLObjectProperty) e, i, i),out);
					}
				}
				else {
					new IllegalArgumentException("module extraction supported for signatures containing only classes and objectProperties").printStackTrace();				
				}
			}
		}
		//the following two actions must be done after any facts involving the critical instance have been created
		//we get assertions of top for all the individuals created - this way we will get them without redundance
		addToInitialABox(indManager.printTopFactsForAllIndividuals(),out);
		//we get assertions of top for all the individuals aoriginally in the TBox, and also any necessary facts equating these individuals to the critical instance
		addToInitialABox(indManager.printFactsForIndividualsFromTBox(individualsFromTBox), out);
		 		
		if (loadABoxesByText && !initialABoxFileName.equals(""))
			out.print(initialAboxText.toString());
		out.close();
	}
	
	protected void addToInitialABox(String facts, PrintWriter out){
		if (loadABoxesByText)
			initialAboxText.append(facts);
		else
			out.print(facts);
	}
	
	public String getInitialABoxText(){
		return initialAboxText.toString();
	}
	
	protected void createTrackingABox(DataStore store, TrackingRuleEncoder4TailoredModuleExtraction trEncoder, String trackingABoxFileName, BottomStrategy bottomStrategy) throws Exception{
		
		PrintWriter out = new PrintWriter(new File(trackingABoxFileName));
		
		Prefixes prefixes = MyPrefixes.PAGOdAPrefixes.getRDFoxPrefixes();

		//First of all let's retrieve all the bottom facts in the materialisation, as we will have to track these for sure in any case
		Variable X = Variable.create("X");
		TupleIterator tupleIterator = null;
		try{
			tupleIterator = store.compileQuery(
							"SELECT DISTINCT ?x WHERE{ ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Nothing> } ", 
							prefixes, 
							new Parameters());
			for (long multiplicity = tupleIterator.open(); multiplicity !=0; multiplicity = tupleIterator.getNext()) {
				GroundTerm groundTerm = tupleIterator.getGroundTerm(0);
				if (groundTerm instanceof uk.ac.ox.cs.JRDFox.model.Individual){
					String s = ((uk.ac.ox.cs.JRDFox.model.Individual)groundTerm).getIRI();
					String bottomPredicate = bottomStrategy.getEmptyHead(X)[0].getDLPredicate().toString();
					String trackingPredicate = trEncoder.getTrackingPredicate(MyPrefixes.PAGOdAPrefixes.getHermiTPrefixes().expandAbbreviatedIRI(bottomPredicate));
					
					addToTrackingABox(Utility_tme.print(trackingPredicate, s), out);
				}
			}	
		}
		catch (JRDFStoreException e){
			e.printStackTrace();
		}
		finally{
			if (tupleIterator != null) tupleIterator.dispose();
		}
		

		//And now let's retrieve the facts that depend on the kind of module we want to extract 
		switch (insepRel){
		case MODEL_INSEPARABILITY:
			createTrackingABoxForModelInseparability(store, trEncoder, prefixes, out);
			break;
		case QUERY_INSEPARABILITY:
			createTrackingABoxForQueryInseparability(store, trEncoder, prefixes, out);
			break;		
		case WEAK_QUERY_INSEPARABILITY:
			createTrackingABoxForQueryInseparability(store, trEncoder, prefixes, out);
			//the method for CQ is ok for this case since it collects ALL facts derived over the reference signature
			break;		
		case FACT_INSEPARABILITY:
			createTrackingABoxForFactInseparability(store, trEncoder, prefixes, out);
			break;
		case IMPLICATION_INSEPARABILITY:
			createTrackingABoxForImplicationInseparability(store, trEncoder, prefixes, out);
			break;
		case CLASSIFICATION_INSEPARABILITY:
			createTrackingABoxForClassificationInseparability(store, trEncoder, prefixes, out);
			break;
		}
		
		if (!trackingABoxFileName.equals(""))
			out.print(trackingAboxText.toString());
		out.close();
	}
	
	protected void addToTrackingABox(String facts, PrintWriter out){
		if (loadABoxesByText)
			trackingAboxText.append(facts);
		else
			out.print(facts);
	}

	public String getTrackingABoxText(){
		return trackingAboxText.toString();
	}

	protected void createTrackingABoxForModelInseparability(
			DataStore store, 
			TrackingRuleEncoder4TailoredModuleExtraction trEncoder, 
			Prefixes prefixes,
			PrintWriter out) throws Exception{
		Individual i;
		Set<String> classes = new HashSet<String>();
		Set<String> properties = new HashSet<String>();
		for (OWLEntity  e : signature)
			if (e instanceof OWLClass)
				classes.add(e.toStringID());
			else
				properties.add(e.toStringID());
		i = IndividualManager.getCriticalInstance();
		Parameters par = new Parameters();
		par.m_expandEquality = false;
		TupleIterator tupleIterator = null;
		try{
			tupleIterator = store.compileQuery("SELECT DISTINCT ?y WHERE{ ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?y } ", prefixes, par);
			for (long multiplicity = tupleIterator.open(); multiplicity !=0; multiplicity = tupleIterator.getNext()) {
				GroundTerm groundTerm = tupleIterator.getGroundTerm(0);
				if (groundTerm instanceof uk.ac.ox.cs.JRDFox.model.Individual){
					String s = ((uk.ac.ox.cs.JRDFox.model.Individual)groundTerm).getIRI();
					if (classes.contains(s))
						addToTrackingABox(Utility_tme.print(trEncoder.getTrackingPredicate(s), i), out);
				}
			}
		}
		catch (JRDFStoreException e){
			e.printStackTrace();
		}
		finally{
			if (tupleIterator != null) tupleIterator.dispose();
		}
		
		par = new Parameters();
		par.m_expandEquality = false;
		try{
			tupleIterator = store.compileQuery(
					"SELECT DISTINCT ?y WHERE{ ?x ?y ?z } ", 
					prefixes, 
					par);
			for (long multiplicity = tupleIterator.open(); multiplicity !=0; multiplicity = tupleIterator.getNext()) {
				GroundTerm groundTerm = tupleIterator.getGroundTerm(0);
				if (groundTerm instanceof uk.ac.ox.cs.JRDFox.model.Individual){
					String s = ((uk.ac.ox.cs.JRDFox.model.Individual)groundTerm).getIRI();
					if (isSameAsPredicate(s) || properties.contains(s))
						addToTrackingABox(Utility_tme.print(trEncoder.getTrackingPredicate(s), i, i),out);
				}
			}
		}
		catch (JRDFStoreException e){
			e.printStackTrace();
		}
		finally{
			if (tupleIterator != null) tupleIterator.dispose();
		}
	}

	protected void createTrackingABoxForQueryInseparability(
			DataStore store, 
			TrackingRuleEncoder4TailoredModuleExtraction trEncoder, 
			Prefixes prefixes,
			PrintWriter out) throws Exception{
		Set<String> classes = new HashSet<String>();
		Set<String> properties = new HashSet<String>();
		for (OWLEntity  e : signature)
			if (e instanceof OWLClass)
				classes.add(e.toStringID());
			else
				properties.add(e.toStringID());
		Parameters par = new Parameters();
		par.m_expandEquality = false;
		TupleIterator tupleIterator = null;
		try{
			tupleIterator = store.compileQuery("SELECT DISTINCT ?x ?y WHERE{ ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?y } ", prefixes, par);
			for (long multiplicity = tupleIterator.open(); multiplicity !=0; multiplicity = tupleIterator.getNext()) {
				GroundTerm arg = tupleIterator.getGroundTerm(0);
				GroundTerm cls = tupleIterator.getGroundTerm(1);
				if (arg instanceof uk.ac.ox.cs.JRDFox.model.Individual &&
						cls instanceof uk.ac.ox.cs.JRDFox.model.Individual){
					String a = ((uk.ac.ox.cs.JRDFox.model.Individual)arg).getIRI();
					String c = ((uk.ac.ox.cs.JRDFox.model.Individual)cls).getIRI();
					if (classes.contains(c))
						addToTrackingABox(Utility_tme.print(trEncoder.getTrackingPredicate(c), a), out);
				}
			}
		}
		catch (JRDFStoreException e){
			e.printStackTrace();
		}
		finally{
			if (tupleIterator != null) tupleIterator.dispose();
		}
		par = new Parameters();
		par.m_expandEquality = false;
		try{
			tupleIterator = store.compileQuery("SELECT DISTINCT ?x ?y ?z WHERE{ ?x ?y ?z } ", prefixes, par);
			for (long multiplicity = tupleIterator.open(); multiplicity !=0; multiplicity = tupleIterator.getNext()) {
				GroundTerm arg1 = tupleIterator.getGroundTerm(0);
				GroundTerm prop = tupleIterator.getGroundTerm(1);
				GroundTerm arg2 = tupleIterator.getGroundTerm(2);
				if (arg1 instanceof uk.ac.ox.cs.JRDFox.model.Individual && 
						prop instanceof uk.ac.ox.cs.JRDFox.model.Individual &&
						arg2 instanceof uk.ac.ox.cs.JRDFox.model.Individual){
					String a1 = ((uk.ac.ox.cs.JRDFox.model.Individual)arg1).getIRI();
					String p = ((uk.ac.ox.cs.JRDFox.model.Individual)prop).getIRI();
					String a2 = ((uk.ac.ox.cs.JRDFox.model.Individual)arg2).getIRI();
					if (isSameAsPredicate(p) || properties.contains(p))
						addToTrackingABox(Utility_tme.print(trEncoder.getTrackingPredicate(p), a1, a2), out);
				}
			}
		}
		catch (JRDFStoreException e){
			e.printStackTrace();
		}
		finally{
			if (tupleIterator != null) tupleIterator.dispose();
		}
	}

	protected void createTrackingABoxForFactInseparability(
			DataStore store, 
			TrackingRuleEncoder4TailoredModuleExtraction trEncoder, 
			Prefixes prefixes,
			PrintWriter out) throws Exception{
		Individual i;
		for (OWLEntity e : signature){
			i = indManager.getInstanceIndividual(e);
			if (e instanceof OWLClass)
				addToTrackingABox(Utility_tme.print(trEncoder.getTrackingPredicate(((OWLClass) e).toStringID()), i), out);
			else if (e instanceof OWLObjectProperty)
				addToTrackingABox(Utility_tme.print(trEncoder.getTrackingPredicate(((OWLObjectProperty) e).toStringID()), i, i), out);
			else 
				throw new IllegalArgumentException("module extraction supported for signatures containing only classes and objectProperties");
		}
		Parameters par = new Parameters();
		par.m_expandEquality = false;
		TupleIterator tupleIterator = null;
		try{
			tupleIterator = store.compileQuery(
					"SELECT DISTINCT ?x ?y WHERE{ ?x <http://www.w3.org/2002/07/owl#sameAs> ?y } ", 
					prefixes, 
					par);
			for (long multiplicity = tupleIterator.open(); multiplicity !=0; multiplicity = tupleIterator.getNext()) {
				GroundTerm groundTerm1 = tupleIterator.getGroundTerm(0);
				GroundTerm groundTerm2 = tupleIterator.getGroundTerm(1);
				if (
						//					!groundTerm1.toString().equals(groundTerm2.toString()) && 
						// got triples without expanding equality, so to track the equality facts that lead to the equivalence classes 
						//we need to track ALL equality facts - since we cannot distinguish those involving two individuals from those involving two predicates (owl:Nothing owl:sameAs owl:Nothing)  
						groundTerm1 instanceof uk.ac.ox.cs.JRDFox.model.Individual &&
						groundTerm2 instanceof uk.ac.ox.cs.JRDFox.model.Individual){
					String s1 = ((uk.ac.ox.cs.JRDFox.model.Individual)groundTerm1).getIRI();//.replace("<", "").replace(">", "");
					String s2 = ((uk.ac.ox.cs.JRDFox.model.Individual)groundTerm2).getIRI();//.replace("<", "").replace(">", "");
					addToTrackingABox(Utility_tme.print(trEncoder.getTrackingPredicate("<http://www.w3.org/2002/07/owl#sameAs>"), Individual.create(s1), Individual.create(s2)), out);
				}
			}
		}
		catch (JRDFStoreException e){
			e.printStackTrace();
		}
		finally{
			if (tupleIterator != null) tupleIterator.dispose();
		}
	}

	protected void createTrackingABoxForImplicationInseparability(
			DataStore store, 
			TrackingRuleEncoder4TailoredModuleExtraction trEncoder, 
			Prefixes prefixes,
			PrintWriter out) throws Exception{
		TupleIterator tupleIterator = null;
		Set<String> classes = new HashSet<String>();
		Set<String> properties = new HashSet<String>();
		for (OWLEntity  e : signature)
			if (e instanceof OWLClass)
				classes.add(e.toStringID());
			else
				properties.add(e.toStringID());
		for (OWLEntity e : signature){
			if (e instanceof OWLClass){
				Individual i = indManager.getInstanceIndividual(e);
				Parameters par = new Parameters();
				par.m_expandEquality = false;
				try{
					tupleIterator = store.compileQuery(
							"SELECT DISTINCT ?y WHERE{ " + i.toString() + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?y } ", 
							prefixes, 
							par);
					for (long multiplicity = tupleIterator.open(); multiplicity !=0; multiplicity = tupleIterator.getNext()) {
						GroundTerm cls = tupleIterator.getGroundTerm(0);
						if (cls instanceof uk.ac.ox.cs.JRDFox.model.Individual){
							String c = ((uk.ac.ox.cs.JRDFox.model.Individual)cls).getIRI();
							if (!c.equals(e.toStringID()) && classes.contains(c))
								addToTrackingABox(Utility_tme.print(trEncoder.getTrackingPredicate(c), i), out);
						}
					}
				}
				catch (JRDFStoreException ex){
					ex.printStackTrace();
				}
				finally{
					if (tupleIterator != null) tupleIterator.dispose();
				}
			}
			else if (e instanceof OWLObjectProperty){
				Individual[] i;
				if (twoDifferentIndividuals4PropertyInstantiation){
					i = indManager.getInstanceIndividuals((OWLObjectProperty) e);
				}
				else{
					Individual j = indManager.getInstanceIndividual(e);
					i = new Individual[]{j,j};
				}
				String query = "SELECT DISTINCT ?x WHERE{ " + i[0].toString() + " ?x " + i[1].toString() + " } ";
				Parameters par = new Parameters();
				par.m_expandEquality = false;
				try{
					tupleIterator = store.compileQuery(
							query, 
							prefixes, 
							par);
					for (long multiplicity = tupleIterator.open(); multiplicity !=0; multiplicity = tupleIterator.getNext()) {
						GroundTerm prop = tupleIterator.getGroundTerm(0);
						if (prop instanceof uk.ac.ox.cs.JRDFox.model.Individual){
							String p = ((uk.ac.ox.cs.JRDFox.model.Individual)prop).getIRI();
							if (isSameAsPredicate(p) || (!p.equals(e.toStringID()) && properties.contains(p)))
								addToTrackingABox(Utility_tme.print(trEncoder.getTrackingPredicate(p), i[0], i[1]), out);
						}
					}
				}
				catch (JRDFStoreException ex){
					ex.printStackTrace();
				}
				finally{
					if (tupleIterator != null) tupleIterator.dispose();
				}
			}
			else
				throw new IllegalArgumentException();
		}
	}

	protected void createTrackingABoxForClassificationInseparability(
			DataStore store, 
			TrackingRuleEncoder4TailoredModuleExtraction trEncoder, 
			Prefixes prefixes,
			PrintWriter out) throws Exception{
		TupleIterator tupleIterator = null;
		Set<String> classes = new HashSet<String>();
		Set<String> properties = new HashSet<String>();
		for (OWLEntity  e : signature)
			if (e instanceof OWLClass)
				classes.add(e.toStringID());
			else
				properties.add(e.toStringID());
		for (OWLEntity e : signature){
			if (e instanceof OWLClass){
				Individual i = indManager.getInstanceIndividual(e);
				Parameters par = new Parameters();
				par.m_expandEquality = false;
				try{
					tupleIterator = store.compileQuery(
							"SELECT DISTINCT ?y WHERE{ " + i.toString() + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?y } ", 
							prefixes, 
							par);
					for (long multiplicity = tupleIterator.open(); multiplicity !=0; multiplicity = tupleIterator.getNext()) {
						GroundTerm cls = tupleIterator.getGroundTerm(0);
						if (cls instanceof uk.ac.ox.cs.JRDFox.model.Individual){
							String c = ((uk.ac.ox.cs.JRDFox.model.Individual)cls).getIRI();
							if (!c.equals(e.toStringID()) && !builtInClass(c)){
								addToTrackingABox(Utility_tme.print(trEncoder.getTrackingPredicate(c), i), out);
							}
						}
					}
				}
				catch (JRDFStoreException ex){
					ex.printStackTrace();
				}
				finally{
					if (tupleIterator != null) tupleIterator.dispose();
				}
			}
			else if (e instanceof OWLObjectProperty){
				Individual[] i;
				if (twoDifferentIndividuals4PropertyInstantiation){
					i = indManager.getInstanceIndividuals((OWLObjectProperty) e);
				}
				else{
					Individual j = indManager.getInstanceIndividual(e);
					i = new Individual[]{j,j};
				}
				String query = "SELECT DISTINCT ?x WHERE{ " + i[0].toString() + " ?x " + i[1].toString() + " } ";
				Parameters par = new Parameters();
				par.m_expandEquality = false;
				try{
					tupleIterator = store.compileQuery(
							query, 
							prefixes, 
							par);
					for (long multiplicity = tupleIterator.open(); multiplicity !=0; multiplicity = tupleIterator.getNext()) {
						GroundTerm prop = tupleIterator.getGroundTerm(0);
						if (prop instanceof uk.ac.ox.cs.JRDFox.model.Individual){
							String p = ((uk.ac.ox.cs.JRDFox.model.Individual)prop).getIRI();
							if (isSameAsPredicate(p) || (!p.equals(e.toStringID()) && !builtInProperty(p))){
								addToTrackingABox(Utility_tme.print(trEncoder.getTrackingPredicate(p), i[0], i[1]), out);
							}
						}
					}
				}
				catch (JRDFStoreException ex){
					ex.printStackTrace();
				}
				finally{
					if (tupleIterator != null) tupleIterator.dispose();
				}
			}
			else
				throw new IllegalArgumentException();
		}
	}

	protected boolean builtInClass(String s){
		return s.endsWith("owl#Thing") || s.endsWith("owl#Nothing") || s.contains("RDFox#replace");
	}
	
	protected boolean builtInProperty(String s){
		return s.endsWith("ns#type") || s.contains("RDFox#replace");
	}

	protected boolean isSameAsPredicate(String s){
		return s.endsWith("owl#sameAs");
//		return s.equals("http://www.w3.org/2002/07/owl#sameAs");
	}
}
