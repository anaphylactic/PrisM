package thesisTests;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.prism.PrisM;
import uk.ac.ox.cs.prism.PrisM.InseparabilityRelation;
import uk.ac.ox.cs.prism.util.Utility_tme;

public class RandomSampleTest {

	static OWLOntology root_ontology;
	static String root_ontology_iri;
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static String iri_onto;
	static OWLOntology normalisedOntology;
	static Set<OWLAxiom> normalisedAxioms;
	//	static String outputFileName = "RandomSignaturesTestResults.txt";

	protected static boolean timeout = false;
	static long timeoutSecs = 32400;//9h
	static int nThreads = 16;
	static boolean removeABox = false;
	static int skipFirstNsignatures = 0;
	
	private static SignatureReader reader = null;
	private static RandomSampleTest inst = new RandomSampleTest();
	

	public static void main(String[] args){

		if (args.length == 0){
//			args = new String[]{"http://www.cs.ox.ac.uk/isg/ontologies/UID/00008.owl", "DataRepresentation/00008/Samples"};
			args = new String[]{"http://www.cs.ox.ac.uk/isg/ontologies/UID/00029.owl", "DataRepresentation/00029/Samples"};
		}

		iri_onto = args[0];
		loadOntology();
		
		if (args.length>4){
			skipFirstNsignatures = Integer.parseInt(args[4]);
			Utility_tme.logInfo("skipping the first " + skipFirstNsignatures + " signatures");
		}

		reader = inst.new SignatureReader(args[1]);
		
		if (args.length>2){
			timeoutSecs = Integer.parseInt(args[2]);
			Utility_tme.logInfo("timeout of " + timeoutSecs + "s is active? " + timeout);
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
					new RandomSampleTest().run();
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
		

		//and then we run the thing on the axioms that we have selected
		int counter = 0;
		while (reader.hasNext()){
			System.out.println("# run number "+ counter++);
			extractModulesForSignature(reader.next());
		}
		unloadOntology();
		System.exit(0);
	}

	protected Set<OWLEntity> getClassesAndObjectsInSignature(Set<OWLEntity> sig) {
		Set<OWLEntity> filteredSig = new HashSet<OWLEntity>();
		for (OWLEntity e : sig)
			if (e instanceof OWLClass || e instanceof OWLObjectProperty)
				filteredSig.add(e);
		return filteredSig;
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
			normalisedOntology = manager.createOntology(normalisedAxioms, IRI.create(root_ontology_iri.replace(".owl", "_normalised_RTBox.owl")));

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

	public class SignatureReader implements Iterator<Set<OWLEntity>>{

		File signaturesDirectory;
		File[] signatureFiles; 
		int counter = -1;
		File nextFile;
		Set<OWLEntity> nextSignature;
		OWLDataFactory factory = new OWLDataFactoryImpl();
		
		public SignatureReader(String s){
			signaturesDirectory = new File(s);
//			signatureFiles = signaturesDirectory.listFiles();//this wasn't loading them in alphabetical order
			int n = signaturesDirectory.listFiles().length;
			signatureFiles = new File[n - skipFirstNsignatures];
			for (int i = 0 ; i<signatureFiles.length ; i++){
				int m = i+skipFirstNsignatures;
				signatureFiles[i] = new File(signaturesDirectory + "/sample" + (m+1) + ".ttl");
			}
			Utility_tme.logDebug(signatureFiles.length + " signature files recovered");
			nextFile = null;
		}
		
		
		@Override
		public boolean hasNext() {
			return (nextFile != null || loadNextFile());
		}

		@Override
		public Set<OWLEntity> next() {
			if (nextFile == null)
				if (!loadNextFile()){
					return null;
				}
			Set<OWLEntity> ret = null;
			try {
				ret = readSignature();
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				nextFile = null;
			}
			return ret;
		}

		private Set<OWLEntity> readSignature() throws IOException{
			Set<OWLEntity> ret = new HashSet<OWLEntity>();
			FileReader reader = new FileReader(nextFile);
			BufferedReader br = new BufferedReader(reader);
			String s = br.readLine();
			while (s != null){
				int i = s.indexOf(">");
				String sig1 = s.substring(1, i);
				s = s.substring(i+1);
				i = s.indexOf(">");
				s = s.substring(i+2);
				i = s.indexOf(">");
				String sig2 = s.substring(1, i);
				ret.addAll(readEntitiesFromString(sig1));
				ret.addAll(readEntitiesFromString(sig2));
				s = br.readLine();
			}
			
			br.close();
			return ret;
		}
		
		private Set<OWLEntity> readEntitiesFromString(String s){
			Set<OWLEntity> ret = new HashSet<OWLEntity>();
			while (!s.isEmpty()){
				if (s.startsWith("*&*")){
					s = s.substring(3);
					int i = s.indexOf("*&");
					String entityName = s;
					if (i>=0){
						entityName = s.substring(0, i);
						s = s.substring(i);
					}
					else
						s = "";
					ret.add(factory.getOWLClass(IRI.create(entityName)));
				}
				else if (s.startsWith("*&&*")){
					s = s.substring(4);
					int i = s.indexOf("*&");
					String entityName = s;
					if (i>=0){
						entityName = s.substring(0, i);
						s = s.substring(i);
					}
					else
						s = "";
					ret.add(factory.getOWLObjectProperty(IRI.create(entityName)));
				}
			}
			return ret;
		}
		
		private boolean loadNextFile(){
			counter++;
			if (signatureFiles.length < counter+1)
				return false;
			else{
				nextFile = signatureFiles[counter];
				Utility_tme.logInfo("# loading signature from file " + nextFile.getAbsolutePath());
				return true;
			}
		}
		
		@Override
		public void remove() {}
		
	}
	
}






