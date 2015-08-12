package thesisTests;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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


public class RandomSignaturesTest {


	static OWLOntology root_ontology;
	static String root_ontology_iri;
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static String iri_onto;
	static OWLOntology normalisedOntology;
	static Set<OWLAxiom> normalisedAxioms;
	//	static String outputFileName = "RandomSignaturesTestResults.txt";

	protected static boolean timeout = false;
	static long timeoutSecs = 1800;//10min

	@Deprecated
	public static void main(String[] args){

		if (args.length == 0){
			//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/aeo_v3.7/aeo_v3.7.owl"};
			//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/regulation-of-gene-expression-ontolology/regulation-of-gene-expression-ontolology.3152.owl.xml"};
			//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/00033_galen/00033.owl"};
			//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/00354_nif/00354.owl"};
			//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/cao_v1.4/cao_v1.4.owl"};
//						args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/fly_anatomy/fly_anatomy_XP/fly_anatomy_XP.owl"};
//						args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/ExtendedSNOMED/anatomy2012EL.owl"};
//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/00787_snomed/00787.owl"};
			//args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/uobm/univ-bench-dl.owl"};
//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/biomodels-21/biomodels-21.owl"};
//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/NCI/Thesaurus_14.07d/Thesaurus_14.07d.owl"};
//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/dbpedia/DBPedia.owl"};
//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/dbpedia/integratedOntology-minus-datatype.owl"};
//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/fma/fma.owl"};
		
		
		args = new String[]{"file:/share/Ana/AAAI2014tests/ontologies2test/fly_anatomy_XP/fly_anatomy_XP.owl"};
		//		args = new String[]{"file:/users/aarmas/TailoredModules/ontologies2test/Thesaurus_14.07d.owl"};
//			args = new String[]{"file:/users/aarmas/TailoredModules/ontologies2test/UOBM/univ-bench-dl.owl"};
		}

		iri_onto = args[0];
		loadOntology();

		try {
			new RandomSignaturesTest().run();
		} catch (FileNotFoundException | OWLOntologyCreationException
				| JRDFStoreException e) {
			e.printStackTrace();
		}
		
		
//		ExecutorService executor = Executors.newFixedThreadPool(1);
//		Future<?> future = executor.submit(new Runnable() {
//			public void run() {
//				try {
//					long t = System.currentTimeMillis();
//					new RandomSignaturesTest().run();
//					t = System.currentTimeMillis() - t;
//					System.out.println(t + " ms");
//
//				} catch (Exception e) {
//					throw new RuntimeException(e);
//				}
//			}
//		});
//		//check the outcome of the executor thread and limit the time allowed for it to complete
//		try {
//			future.get(timeoutSecs, TimeUnit.SECONDS);
//			future.cancel(true);
//			executor.shutdown();
//		}
//		catch (TimeoutException e) {
//			System.out.println("Time out! Process was taking longer than " + timeoutSecs + "s");
//			//interrupts the worker thread if necessary
//			future.cancel(true);
//			executor.shutdown();
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
	}


	public void run() throws JRDFStoreException, FileNotFoundException, OWLOntologyCreationException{

		System.out.println("# " + normalisedAxioms.size() + "normalised axioms");
		int sigSize = getClassesAndObjectsInSignature(normalisedOntology.getSignature()).size();
		System.out.println("# " + sigSize + "predicates in normalised ontology");
		
		System.out.println("signatureSize \t "
				+ "BottomOWLAPI \t \t "
				+ "StarOWLAPI \t \t "
//				+ "Bottom \t \t "
				+ "Star \t \t "
				+ "CQ \t \t "
				+ "DisjDat \t \t "
				+ "ConceptImp \t \t "
				+ "ConceptClassif");
		System.out.println("signatureSize \t "
				+ "size \t time \t "
				+ "size \t time \t "
//				+ "size \t time \t "
				+ "size \t time \t "
				+ "size \t time \t "
				+ "size \t time \t "
				+ "size \t time \t "
				+ "size \t time");

		Set<OWLEntity> signature = selectRandomSignature(normalisedOntology);
		String s = signature.size() + " \t ";

		PrisM extractor; 
		Long t;
		SyntacticLocalityModuleExtractor localityExtractor;
		
		t = System.currentTimeMillis();
		System.out.println("# BOT_OWLAPI");
		localityExtractor = new SyntacticLocalityModuleExtractor(manager, normalisedOntology, ModuleType.BOT);
		s = s + localityExtractor.extract(signature).size() + " \t " + (System.currentTimeMillis() - t) + " \t "; 
		t = System.currentTimeMillis();

		reloadNormalisedOntology();
		System.out.println("# STAR_OWLAPI");
		localityExtractor = new SyntacticLocalityModuleExtractor(manager, normalisedOntology, ModuleType.STAR);
		s = s + localityExtractor.extract(signature).size() + " \t " + (System.currentTimeMillis() - t) + " \t ";
		t = System.currentTimeMillis();

//		reloadNormalisedOntology();
//		System.out.println("# Bottom");
//		extractor = new TailoredModulesExtractor(normalisedOntology, TailoredModuleType.Bottom);
//		s = s + extractor.extract(signature).size() + " \t " + (System.currentTimeMillis() - t) + " \t ";
//		t = System.currentTimeMillis();

		reloadNormalisedOntology();
		System.out.println("# Star");
		extractor = new PrisM(normalisedOntology, InseparabilityRelation.MODEL_INSEPARABILITY);
		s = s + extractor.extract(signature).size() + " \t " + (System.currentTimeMillis() - t) + " \t ";
		t = System.currentTimeMillis();

		reloadNormalisedOntology();
		System.out.println("# CQ");
		extractor = new PrisM(normalisedOntology, InseparabilityRelation.QUERY_INSEPARABILITY);
		s = s + extractor.extract(signature).size() + " \t " + (System.currentTimeMillis() - t) + " \t ";
		t = System.currentTimeMillis();

		reloadNormalisedOntology();
		System.out.println("# DisjDat");
		extractor = new PrisM(normalisedOntology, InseparabilityRelation.FACT_INSEPARABILITY);
		s = s + extractor.extract(signature).size() + " \t " + (System.currentTimeMillis() - t) + " \t ";
		t = System.currentTimeMillis();

		reloadNormalisedOntology();
		System.out.println("# ConceptImplication");
		extractor = new PrisM(normalisedOntology, InseparabilityRelation.IMPLICATION_INSEPARABILITY);
		s = s + extractor.extract(signature).size() + " \t " + (System.currentTimeMillis() - t) + " \t ";
		t = System.currentTimeMillis();

		reloadNormalisedOntology();
		System.out.println("# ConceptClassification");
		extractor = new PrisM(normalisedOntology, InseparabilityRelation.CLASSIFICATION_INSEPARABILITY);
		s = s + extractor.extract(signature).size() + " \t " + (System.currentTimeMillis() - t) + " \t ";
		t = System.currentTimeMillis();

		System.out.println(s);

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
	
	protected Set<OWLEntity> selectRandomSignature(OWLOntology o){
		Set<OWLEntity> selected = new HashSet<OWLEntity>();
		
//		for (OWLEntity e : getClassesAndObjectsInSignature(o.getSignature()))
//			if (e.toString().equals("<http://purl.obolibrary.org/obo/FBbt_00002468>") ||
//					e.toString().equals("<http://purl.obolibrary.org/obo/FBbt_00004237>") ||
//					e.toString().equals("<http://purl.obolibrary.org/obo/FBbt_00100244>") ||
//					e.toString().equals("<http://purl.obolibrary.org/obo/FBbt_00001575>") ||
//					e.toString().equals("<http://purl.obolibrary.org/obo/FBbt_00000802>") ||
//					e.toString().equals("<http://purl.obolibrary.org/obo/FBbt_00000775>") ||
//					e.toString().equals("<http://purl.obolibrary.org/obo/FBbt_00004301>") ||
//					e.toString().equals("<http://purl.obolibrary.org/obo/FBbt_00004483>") ||
//					e.toString().equals("<http://purl.obolibrary.org/obo/FBbt_00100054>") ||
//					e.toString().equals("<http://purl.obolibrary.org/obo/FBbt_00100505>") ||
//					e.toString().equals("<http://purl.obolibrary.org/obo/FBbt_00004962>") ||
//					e.toString().equals("<http://purl.obolibrary.org/obo/FBbt_00004002>") ||
//					e.toString().equals("<http://purl.obolibrary.org/obo/FBbt_00001228>") ||
//					e.toString().equals("<http://purl.obolibrary.org/obo/FBbt_00003937>") ||
//					e.toString().equals("<http://purl.obolibrary.org/obo/FBbt_00002510>") ||
//					e.toString().equals("<http://purl.obolibrary.org/obo/FBbt_00001058>") ||
//					e.toString().equals("<http://purl.obolibrary.org/obo/FBbt_00000417>"))
//				selected.add(e);
		
		Random r = new Random();
		for (OWLEntity e : getClassesAndObjectsInSignature(o.getSignature()))
			if (r.nextInt(1000) < 1){//0.1% of the signature
				selected.add(e);
//				break;
			}
		System.out.println("# signature of size " + selected.size());
		System.out.println("# " + selected.toString());
		return selected;
	}


	private static void loadOntology(){
		try {
			root_ontology = manager.loadOntology(IRI.create(iri_onto));
			root_ontology_iri = manager.getOntologyDocumentIRI(root_ontology).toString().replace("<","").replace(">", "");//getOWLOntologyManager().getOntologyDocumentIRI(root_ontology).toString();

			Set<OWLAxiom> rtBox = new HashSet<OWLAxiom>(root_ontology.getTBoxAxioms(true));
			rtBox.addAll(root_ontology.getRBoxAxioms(true));

			manager.removeOntology(root_ontology);
			root_ontology = manager.createOntology(rtBox, IRI.create(root_ontology_iri.replace(".owl", "-RTBox.owl")));

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private static void unloadOntology(){
		manager.removeOntology(root_ontology);
		System.out.println("Unloaded");
	}
}






