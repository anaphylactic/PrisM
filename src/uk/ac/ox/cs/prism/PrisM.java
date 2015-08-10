package uk.ac.ox.cs.prism;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.Prefixes;
import uk.ac.ox.cs.JRDFox.store.DataStore;
import uk.ac.ox.cs.JRDFox.store.DataStore.UpdateType;
import uk.ac.ox.cs.pagoda.constraints.BottomStrategy;
import uk.ac.ox.cs.pagoda.constraints.UnaryBottom;
import uk.ac.ox.cs.pagoda.util.Utility;
import uk.ac.ox.cs.prism.util.Utility_tme;

public class PrisM{

	protected OWLOntology ontology;
	protected OWLOntology startingModule;
	protected InseparabilityRelation insepRel;
	protected IndividualManager indManager;
	protected ABoxManager aboxManager;
	protected String programFileName = "";
	protected String trackingProgramFileName = "";
	protected String initialABoxFileName = "";
	protected String trackingABoxFileName = "";
	protected String materialisationABoxFileName = "";
	
	protected int numberOfThreads = 8;
	
	boolean savingMode = true;

	protected ExecutorService executor;
	protected Future<?> disposalFuture;
	
	public PrisM(OWLOntology o, InseparabilityRelation insepRel){
		
		Logger.getLogger("").setLevel(Level.ERROR);
		new File(Utility.TempDirectory).mkdir();
		
		ontology = o;
		this.insepRel = insepRel;
		indManager = new IndividualManager(insepRel);
		if (savingMode){
			initialABoxFileName = Utility.TempDirectory + insepRel.toString() + "_initialABox.ttl";
			trackingABoxFileName = Utility.TempDirectory + insepRel.toString() + "_trackingABox.ttl";
			programFileName = Utility.TempDirectory + insepRel.toString() + "_program.dlog";
			trackingProgramFileName = Utility.TempDirectory + insepRel.toString() + "_trackingProgram.dlog";
			String[] aux = ontology.getOntologyID().toString().split("/");
			aux = aux[aux.length-1].split(">");
			aux = aux[0].split(".owl");
			aux = aux[0].split("-RTBox");
			String ontologyName = aux[0].replaceFirst("_normalised.*", "");
			initialABoxFileName = initialABoxFileName.replace("initialABox","initialABox_" + ontologyName);
			trackingABoxFileName = trackingABoxFileName.replace("trackingABox","trackingABox_" + ontologyName);
			programFileName = programFileName.replace("program","program_" + ontologyName);
			trackingProgramFileName = trackingProgramFileName.replace("trackingProgram","trackingProgram_" + ontologyName);
		}
		else {
			initialABoxFileName = "";
			trackingABoxFileName = "";
		}
	}
	
	public PrisM(OWLOntology o, InseparabilityRelation insepRel, int nThreads){
		this(o,insepRel);
		if (nThreads > 0)
			numberOfThreads = nThreads;
	}
	
	public Set<OWLAxiom> extract(Set<OWLEntity> signature) throws JRDFStoreException {
		return extract(signature,true);
	}
	public Set<OWLAxiom> extract(Set<OWLEntity> signature, boolean viaSyntacticLocality){

		initStartingModule(signature, viaSyntacticLocality);

		BottomStrategy bottomStrategy = new UnaryBottom();
		ModuleExtractionUpperProgram program = initProgram(bottomStrategy);
		//it is important that the program is initialised before the initial ABox so any necessary Skolem constants etc are created first

		aboxManager = new ABoxManager(signature, startingModule.getIndividualsInSignature(), insepRel, indManager);
		createInitialABox(aboxManager);

		DataStore store = initDataStore();
		store = materialise(store, program);

		TrackingRuleEncoder4TailoredModuleExtraction trEncoder = new TrackingRuleEncoder4TailoredModuleExtraction(program);
		createTrackingABox(aboxManager, store, trEncoder, bottomStrategy);
		materialiseTracking(store, trEncoder);

		Set<OWLAxiom> moduleAxioms = extractAxioms(store, trEncoder);
		
		dispose(store);
		
		return moduleAxioms;
	}
	

	protected void initStartingModule(Set<OWLEntity> signature, boolean viaSyntacticLocality){
		if (viaSyntacticLocality){
			//We start by extracting a syntactic locality module with the OWLAPI
			String documentIRI = ontology.getOWLOntologyManager().getOntologyDocumentIRI(ontology).toString();
			String ontologyIRI = ontology.getOntologyID().getOntologyIRI().toString();
			String originalExtension = documentIRI.substring(documentIRI.lastIndexOf("."));
			String moduleIRI = ontologyIRI.replace(originalExtension, "-startingModule.owl");

			try {
				if (insepRel == InseparabilityRelation.CLASSIFICATION_INSEPARABILITY){
					SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(ontology.getOWLOntologyManager(), ontology, ModuleType.BOT);
					startingModule = extractor.extractAsOntology(signature, IRI.create(moduleIRI));
					Utility_tme.logDebug("# we start from OWLAPI BOT-module");
					Utility_tme.logDebug("# which contains " + startingModule.getAxiomCount());
				}
				else{
					SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(ontology.getOWLOntologyManager(), ontology, ModuleType.STAR);
					startingModule = extractor.extractAsOntology(signature, IRI.create(moduleIRI));
					Utility_tme.logDebug("# we start from OWLAPI STAR-module");
					Utility_tme.logDebug("# which contains " + startingModule.getAxiomCount());
				}	

			} catch (OWLOntologyCreationException e) {
				startingModule = ontology;
			}
		}
		else
			startingModule = ontology;
	}
	
	protected DataStore initDataStore(){
		DataStore store = null;
		try {
			if (numberOfThreads > 1){
//				store = new DataStore(DataStore.StoreType.ParallelSimpleNN);
				store = new DataStore(DataStore.StoreType.ParallelComplexNN);
				store.setNumberOfThreads(numberOfThreads);
			}
			else
				store = new DataStore(DataStore.StoreType.SequentialHead);
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		}
		return store;
	}
	
	
	protected ModuleExtractionUpperProgram initProgram(BottomStrategy bottomStrategy){
		Long t = System.currentTimeMillis();

		ModuleExtractionUpperProgram program = new ModuleExtractionUpperProgram(indManager);
		program.load(startingModule, bottomStrategy);
//		program.transform();//this is done directly as part of the loading now

		Utility_tme.logDebug("# " + program.getClauses().size() + " clauses in program");
		try {
			File f = new File(programFileName);
			PrintWriter programWriter = new PrintWriter(f);
			programWriter.println(program.toString());
			programWriter.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		t = System.currentTimeMillis() - t;
		Utility_tme.logDebug("# " + t + "ms to create program");
		t = System.currentTimeMillis();

		return program;
	}

	protected void createInitialABox(ABoxManager aboxManager){
		try {
			aboxManager.createInitialABox(initialABoxFileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected DataStore materialise(DataStore store, ModuleExtractionUpperProgram program){
		Long t = System.currentTimeMillis();
		try {
			store.importFiles(new File[]{new File(initialABoxFileName)});			
			String originalAbox = program.getAdditionalDataFile(); 
			if (originalAbox!=null)  
				store.importFiles(new File[]{new File(originalAbox)});
			
			Utility_tme.logDebug("# Number of tuples after import: " + store.getTriplesCount());
			t = System.currentTimeMillis() - t;
			Utility_tme.logDebug("# " + t + "ms to load initial facts");
			t = System.currentTimeMillis();
			
			if (numberOfThreads > 1){
				store.setNumberOfThreads(1);
				t = System.currentTimeMillis() - t;
				Utility_tme.logDebug("# " + t + "ms to set the number of threads to 1 before loading rules");
				t = System.currentTimeMillis();
			}

			store.importFiles(new File[]{new File(programFileName)}, new Prefixes(), UpdateType.Add, true);
			t = System.currentTimeMillis() - t;
			Utility_tme.logDebug("# " + t + "ms to load rules");
			t = System.currentTimeMillis();

			if (numberOfThreads > 1){
				store.setNumberOfThreads(numberOfThreads);
				t = System.currentTimeMillis() - t;
				Utility_tme.logDebug("# " + t + "ms to set the number of threads to " + numberOfThreads + " again");
				t = System.currentTimeMillis();
			}
			
			store.applyReasoning();

			Utility_tme.logDebug("# Number of tuples after reasoning: " + store.getTriplesCount());

			t = System.currentTimeMillis() - t;
			Utility_tme.logDebug("# " + t + "ms to materialise first time");
			t = System.currentTimeMillis();
			
			store.clearRulesAndMakeFactsExplicit();
			t = System.currentTimeMillis() - t;
			Utility_tme.logDebug("# " + t + "ms to clear rules and make facts explicit");
			t = System.currentTimeMillis();
			
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		}
		
		return store;
	}
	protected void createTrackingABox(ABoxManager aboxManager,
			DataStore store,
			TrackingRuleEncoder4TailoredModuleExtraction trEncoder,
			BottomStrategy bottomStrategy) {
		Long t = System.currentTimeMillis();

		try {
			aboxManager.createTrackingABox(store, trEncoder, trackingABoxFileName, bottomStrategy);
		} catch (Exception e) {
			e.printStackTrace();
		}

		t = System.currentTimeMillis() - t;
		Utility_tme.logDebug("# " + t + "ms to create tracking ABox");
		t = System.currentTimeMillis();
	}
	protected void materialiseTracking(DataStore store, TrackingRuleEncoder4TailoredModuleExtraction trEncoder){
		Long t = System.currentTimeMillis();
		try {
			t = System.currentTimeMillis() - t;
			Utility_tme.logDebug("# " + t + "ms to make facts explicit");
			t = System.currentTimeMillis();
			
			store.importFiles(new File[]{new File(trackingABoxFileName)});

			t = System.currentTimeMillis();
			String trackingProgram = trEncoder.getTrackingProgram();
			Utility_tme.logDebug("# " + trEncoder.getNtrackingClauses() + " clauses in tracking program");
			try {
				File f = new File(trackingProgramFileName);
				PrintWriter programWriter = new PrintWriter(f);
				programWriter.println(trackingProgram.toString());
				programWriter.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			t = System.currentTimeMillis() - t;
			Utility_tme.logDebug("# " + t + "ms to create and (save?) tracking program");
			t = System.currentTimeMillis();
			
			if (numberOfThreads > 1){
				store.setNumberOfThreads(1);
				t = System.currentTimeMillis() - t;
				Utility_tme.logDebug("# " + t + "ms to set the number of threads to 1 before loading rules");
				t = System.currentTimeMillis();
			}
			
			
			store.importFiles(new File[]{new File(trackingProgramFileName)}, new Prefixes(), UpdateType.Add, true);
			t = System.currentTimeMillis() - t;
			Utility_tme.logDebug("# " + t + "ms to load tracking rules");
			t = System.currentTimeMillis();
			
			if (numberOfThreads > 1){
				store.setNumberOfThreads(numberOfThreads);
				t = System.currentTimeMillis() - t;
				Utility_tme.logDebug("# " + t + "ms to set the number of threads to " + numberOfThreads + " again");
				t = System.currentTimeMillis();
			}
			
			store.applyReasoning();
			Utility_tme.logDebug("# Number of tuples after materialising tracking: " + store.getTriplesCount());
			t = System.currentTimeMillis() - t;
			Utility_tme.logDebug("# " + t + "ms to materialise tracking program");			
			t = System.currentTimeMillis();
			
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		}

	}
	protected Set<OWLAxiom> extractAxioms(DataStore store,
			TrackingRuleEncoder4TailoredModuleExtraction trEncoder) {
		Long t = System.currentTimeMillis();
		
		Set<OWLAxiom> axioms = trEncoder.extractAxioms(store);

		t = System.currentTimeMillis() - t;
		Utility_tme.logDebug("# " + t + "ms to retrieve axioms");
		t = System.currentTimeMillis();
		
		return axioms;
	}
	protected void dispose(DataStore store){
		executor = Executors.newFixedThreadPool(1);
		disposalFuture = executor.submit(new StoreDisposer(store));
	}
	public void finishDisposal(){
		if (disposalFuture != null)
			try {
				disposalFuture.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} finally {
				disposalFuture.cancel(true);
				executor.shutdown();
			}
	}

	
	
	
	public enum InseparabilityRelation{
		MODEL_INSEPARABILITY, QUERY_INSEPARABILITY, WEAK_QUERY_INSEPARABILITY, FACT_INSEPARABILITY,
		IMPLICATION_INSEPARABILITY, CLASSIFICATION_INSEPARABILITY 
	}

	
	public class StoreDisposer implements Runnable{
		DataStore store;
		public StoreDisposer(DataStore store){
			this.store = store;
		}
		@Override
		public void run() {
			store.dispose();
		}
	}
}

