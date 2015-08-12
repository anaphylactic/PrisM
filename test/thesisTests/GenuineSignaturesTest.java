package thesisTests;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.prism.PrisM;
import uk.ac.ox.cs.prism.PrisM.InseparabilityRelation;
import uk.ac.ox.cs.prism.util.Utility_tme;


public class GenuineSignaturesTest {

	static OWLOntology root_ontology;
	static String root_ontology_iri;
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static String iri_onto;
	static OWLOntology normalisedOntology;
	static Set<OWLAxiom> normalisedAxioms;
	//	static String outputFileName = "RandomSignaturesTestResults.txt";

	protected static boolean timeout = false;
	static long timeoutSecs = 32400;//9h
	TimerTask task = new TimerTask(){  
		public void run(){  
			if( timeout ){  
				System.out.println( "TIMEOUT!!" );  
				System.exit( 0 );  
			}  
		}  
	};
	
	static int nIterations = 400;
	static int nThreads = 16;
	static boolean removeABox = false;
	
	

	public static void main(String[] args){

		if (args.length == 0){
//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/uobm/univ-bench-dl.owl"};
//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/npd/npd-all-minus-datatype.owl"};
//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/fly_anatomy/fly_anatomy_XP/fly_anatomy_XP.owl"};
//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/NCI/Thesaurus_14.07d/Thesaurus_14.07d.owl"};
//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/ExtendedSNOMED/anatomy2012EL.owl"};
//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/biomodels-21/biomodels-21.owl"};
		
//		args = new String[]{"file:/share/Ana/AAAI2014tests/ontologies2test/fly_anatomy_XP/fly_anatomy_XP.owl"};
		//		args = new String[]{"file:/share/Ana_ModuleExtractionTests/ontologies2test/Thesaurus_14.07d.owl"};
//			args = new String[]{"file:/share/Ana_ModuleExtractionTests/ontologies2test/UOBM/univ-bench-dl.owl"};
//			args = new String[]{"file:/share/Ana_ModuleExtractionTests/ontologies2test/NPD/npd-all-minus-datatype.owl"};
//			args = new String[]{"file:/share/Ana_ModuleExtractionTests/ontologies2test/BIOMODELS/biomodels-21.owl"};
//		args = new String[]{"file:/share/Ana_ModuleExtractionTests/ontologies2test/classified_snomed_sdd_ontology.owl"};
			
			args = new String[]{"http://www.cs.ox.ac.uk/isg/ontologies/UID/00347.owl"};
			
		}

		iri_onto = args[0];
		loadOntology();
		
//		for (OWLAxiom ax : normalisedAxioms)
//			System.out.println(ax.toString());
		
		
		
		if (args.length>1){
			int to = Integer.parseInt(args[1]);
			if (to > 0)
			timeoutSecs = to;
			Utility_tme.logInfo("timeout of " + timeoutSecs + "s is active? " + timeout);
		}
		if (args.length>2){
			nIterations = Integer.parseInt(args[2]);
			Utility_tme.logInfo(nIterations + " iterations");
		}
		if (args.length>3){
			nThreads = Integer.parseInt(args[3]);
			Utility_tme.logInfo(nThreads + " threads");
		}

		
		
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Future<?> future = executor.submit(new Runnable() {
			public void run() {
				try {
					long t = System.currentTimeMillis();
					new GenuineSignaturesTest().run();
					t = System.currentTimeMillis() - t;
					System.out.println(t + " ms");

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		if (timeout){
			//check the outcome of the executor thread and limit the time allowed for it to complete
			try {
				future.get(timeoutSecs, TimeUnit.SECONDS);
				future.cancel(true);
				executor.shutdown();
			}
			catch (TimeoutException e) {
				System.out.println("Time out! Process was taking longer than " + timeoutSecs + "s");
				//interrupts the worker thread if necessary
				future.cancel(true);
				executor.shutdown();
				System.exit(0);
			}
			catch (Exception e) {
				e.printStackTrace();
			}	
		}
		else{
			//check the outcome of the executor thread and without limiting the time allowed for it to complete
			try {
				future.get();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		
		System.exit(0);
	}


	public void run() throws JRDFStoreException, FileNotFoundException, OWLOntologyCreationException{

		System.out.println("# " + normalisedAxioms.size() + "normalised axioms");
		int sigSize = getClassesAndObjectsInSignature(normalisedOntology.getSignature()).size();
		System.out.println("# " + sigSize + "predicates in normalised ontology");

		System.out.println("signatureSize \t "
				+ "BottomOWLAPI \t \t "
				+ "ConceptClassif \t \t "
				+ "StarOWLAPI \t \t "
				+ "Star \t \t "
				+ "CQ \t \t "
				+ "WeakCQ \t \t "
				+ "DisjDat \t \t "
				+ "ConceptImp");
		System.out.println("signatureSize \t "
				+ "size \t time \t "
				+ "size \t time \t "
				+ "size \t time \t "
				+ "size \t time \t "
				+ "size \t time \t "
				+ "size \t time \t "
				+ "size \t time \t "
				+ "size \t time");
		
		
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		for (OWLAxiom ax : normalisedAxioms)
			if (getClassesAndObjectsInSignature(ax.getSignature()).size() > 1)
				axioms.add(ax);
		if (axioms.size() > nIterations){
			OWLAxiom[] axiomsIndexing = indexAxioms(axioms);
			axioms.clear();
			Random r = new Random();
			int counter = 0;
			while (counter < nIterations){
				int i = r.nextInt(axiomsIndexing.length);
				OWLAxiom ax = axiomsIndexing[i];
				if (ax != null){
					if (getClassesAndObjectsInSignature(ax.getSignature()).size() < 2){
						axiomsIndexing[i] = null;
					}
					else{
						axioms.add(ax);
						axiomsIndexing[i] = null;
						counter++;
					}
				}
			}
		}

		System.out.println("# " + axioms.size() + " axioms selected out of " + normalisedAxioms.size());

		//and then we run the thing on the axioms that we have selected
		int counter = 0;
		for (OWLAxiom ax : axioms){
			System.out.println("# run number "+ counter++);
			System.out.println("# axiom: " + ax.toString());
			extractModulesForSignature(getClassesAndObjectsInSignature(ax.getSignature()));
		}

		unloadOntology();
		System.exit(0);
	}
	protected OWLAxiom[] indexAxioms(Set<OWLAxiom> axioms){
		OWLAxiom[] ret = new OWLAxiom[axioms.size()];
		int counter = 0;
		for (OWLAxiom ax : axioms)
			if (getClassesAndObjectsInSignature(ax.getSignature()).size()>1)
				ret[counter++] = ax;
		return ret;
	}

	protected Set<OWLEntity> getClassesAndObjectsInSignature(Set<OWLEntity> sig) {
		Set<OWLEntity> filteredSig = new HashSet<OWLEntity>();
		for (OWLEntity e : sig)
			if (e instanceof OWLObjectProperty ||
					(e instanceof OWLClass && !(((OWLClass) e).isOWLThing() || ((OWLClass) e).isOWLNothing())))
				filteredSig.add(e);
		return filteredSig;
	}

	protected boolean alreadyEncountered(Set<OWLEntity> sig, Set<Set<OWLEntity>> encounteredSignatures){
		for (Set<OWLEntity> encSig : encounteredSignatures)
			if (emptyDiff(sig, encSig))
				return true;
		return false;		
	}

	protected boolean emptyDiff(Set<OWLEntity> set1, Set<OWLEntity> set2){
		Set<OWLEntity> auxSet1 = new HashSet<OWLEntity>(set1);
		Set<OWLEntity> auxSet2 = new HashSet<OWLEntity>(set2);
		auxSet1.removeAll(set2);
		auxSet2.removeAll(set1);
		return (auxSet1.isEmpty() && auxSet2.isEmpty());
	}


	public void extractModulesForSignature(Set<OWLEntity> signature) throws JRDFStoreException, FileNotFoundException, OWLOntologyCreationException{


		System.out.println("# " + signature.toString());

		String s = signature.size() + " \t ";

		PrisM extractor; 
		Long t = System.currentTimeMillis();

		t = System.currentTimeMillis();
		System.out.println("# BOT_OWLAPI");
		SyntacticLocalityModuleExtractor localityExtractor = new SyntacticLocalityModuleExtractor(manager, normalisedOntology, ModuleType.BOT);
		s = s + localityExtractor.extract(signature).size() + " \t " + (System.currentTimeMillis() - t) + " \t "; 
		t = System.currentTimeMillis();

		reloadNormalisedOntology();
		System.out.println("# ConceptClassification");
		extractor = new PrisM(normalisedOntology, InseparabilityRelation.CLASSIFICATION_INSEPARABILITY, nThreads);
		s = s + extractor.extract(signature).size() + " \t " + (System.currentTimeMillis() - t) + " \t ";
		extractor.finishDisposal();
		t = System.currentTimeMillis();
		
		reloadNormalisedOntology();
		System.out.println("# STAR_OWLAPI");
		localityExtractor = new SyntacticLocalityModuleExtractor(manager, normalisedOntology, ModuleType.STAR);
		s = s + localityExtractor.extract(signature).size() + " \t " + (System.currentTimeMillis() - t) + " \t ";
		t = System.currentTimeMillis();

		reloadNormalisedOntology();
		System.out.println("# Star");
		extractor = new PrisM(normalisedOntology, InseparabilityRelation.MODEL_INSEPARABILITY, nThreads);
		s = s + extractor.extract(signature).size() + " \t " + (System.currentTimeMillis() - t) + " \t ";
		extractor.finishDisposal();
		t = System.currentTimeMillis();

		reloadNormalisedOntology();
		System.out.println("# CQ");
		extractor = new PrisM(normalisedOntology, InseparabilityRelation.QUERY_INSEPARABILITY, nThreads);
		s = s + extractor.extract(signature).size() + " \t " + (System.currentTimeMillis() - t) + " \t ";
		extractor.finishDisposal();
		t = System.currentTimeMillis();

		reloadNormalisedOntology();
		System.out.println("# WeakCQ");
		extractor = new PrisM(normalisedOntology, InseparabilityRelation.WEAK_QUERY_INSEPARABILITY, nThreads);
		s = s + extractor.extract(signature).size() + " \t " + (System.currentTimeMillis() - t) + " \t ";
		extractor.finishDisposal();
		t = System.currentTimeMillis();

		reloadNormalisedOntology();
		System.out.println("# DisjDat");
		extractor = new PrisM(normalisedOntology, InseparabilityRelation.FACT_INSEPARABILITY, nThreads);
		s = s + extractor.extract(signature).size() + " \t " + (System.currentTimeMillis() - t) + " \t ";
		extractor.finishDisposal();
		t = System.currentTimeMillis();

		reloadNormalisedOntology();
		System.out.println("# ConceptImplication");
		extractor = new PrisM(normalisedOntology, InseparabilityRelation.IMPLICATION_INSEPARABILITY, nThreads);
		s = s + extractor.extract(signature).size() + " \t " + (System.currentTimeMillis() - t) + " \t ";
		extractor.finishDisposal();
		t = System.currentTimeMillis();

		System.out.println(s);
	}


	private static void loadOntology(){
		try {
			root_ontology = manager.loadOntology(IRI.create(iri_onto));
			root_ontology_iri = manager.getOntologyDocumentIRI(root_ontology).toString().replace("<","").replace(">", "");//getOWLOntologyManager().getOntologyDocumentIRI(root_ontology).toString();

			if (removeABox){
				Set<OWLAxiom> rtBox = new HashSet<OWLAxiom>(root_ontology.getTBoxAxioms(true));
				rtBox.addAll(root_ontology.getRBoxAxioms(true));
				manager.removeOntology(root_ontology);
				root_ontology = manager.createOntology(rtBox, IRI.create(root_ontology_iri.replace(".owl", "-RTBox.owl")));	
			}

			normalisedAxioms = NormaliserViaClauses.getNormalisedAxioms(root_ontology);
			normalisedOntology = manager.createOntology(normalisedAxioms, IRI.create(root_ontology_iri.replace(".owl", "_normalised.owl")));

			System.out.println("\nLoaded ontology: " + iri_onto);

		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			System.exit(0);
		}

	}

	private static void reloadNormalisedOntology(){
		manager.removeOntology(normalisedOntology);
		manager = OWLManager.createOWLOntologyManager();
		try {
			normalisedOntology = manager.createOntology(normalisedAxioms, IRI.create(root_ontology_iri.replace(".owl", "_normalised_RTBox.owl")));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}


	private static void unloadOntology(){
		manager.removeOntology(root_ontology);
		System.out.println("Unloaded");
	}


	//	public void run() throws JRDFStoreException, FileNotFoundException, OWLOntologyCreationException{
	//
	//		System.out.println("# " + normalisedAxioms.size() + "normalised axioms");
	//		System.out.println("# " + getClassesAndObjectsInSignature(normalisedOntology.getSignature()).size() + "predicates in normalised ontology");
	//		
	//		System.out.println("signatureSize \t \t BottomOWLAPI \t \t StarOWLAPI \t \t Bottom \t \t Star \t \t CQ \t \t DisjDat \t \t ConceptImp \t \t ConceptClassif");
	//		System.out.println("signatureSize \t size \t time \t size \t time \t size \t time \t size \t time \t size \t time \t size \t time \t size \t time \t size \t time");
	//
	//		Long t = System.currentTimeMillis();
	//		
	//		Set<Set<OWLEntity>> signaturesAlreadyEncountered = new HashSet<Set<OWLEntity>>(); 
	//		Set<OWLEntity> entitiesAlreadyEncountered = new HashSet<OWLEntity>();
	//		
	//		
	////		Set<OWLEntity> sig = new HashSet<OWLEntity>();
	////		for (OWLEntity e : normalisedOntology.getSignature())
	////			if (e.toString().equals("<http://www.opengis.net/ont/gml#ArcByCenterPoint>") || e.toString().equals("<http://www.opengis.net/ont/gml#CircleByCenterPoint>") )
	////				sig.add(e);
	////		extractModulesForSignature(sig);
	//			
	//		int counter = 0;
	////		int counterSaved = 0;
	//		for (OWLAxiom ax : normalisedAxioms){
	//			Set<OWLEntity> sig = getClassesAndObjectsInSignature(ax.getSignature());
	//			boolean runForSig = true;
	//			if (entitiesAlreadyEncountered.addAll(sig)){
	////				counterSaved++;
	////				if (counterSaved % 100 == 0)
	////					System.out.println(counterSaved + "full containment tests saved");
	//				signaturesAlreadyEncountered.add(sig);
	//			}
	//			else if (!alreadyEncountered(sig, signaturesAlreadyEncountered)){
	//				signaturesAlreadyEncountered.add(sig);
	//			}
	//			else
	//				runForSig = false;
	//			
	//			counter++;
	//			if (counter % 100 == 0)
	//				System.out.println(counter + "/" + normalisedAxioms.size());
	//			
	//			if (runForSig){
	//				System.out.println(counter);
	//				extractModulesForSignature(sig);
	//			}
	//		}
	//
	//		System.out.println(signaturesAlreadyEncountered.size() + " distinct signatures emcountered ");
	//		System.out.println(System.currentTimeMillis() - t + "ms");
	//		
	//		unloadOntology();
	//		System.exit(0);
	//	}

}






