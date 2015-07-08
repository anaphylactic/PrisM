package uk.ac.ox.cs.prism.tests;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.prism.PrisM;
import uk.ac.ox.cs.prism.PrisM.InseparabilityRelation;


public class secondTest {


	static OWLOntology root_ontology;
	static String root_ontology_iri;
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static String iri_onto;

	protected static boolean timeout = false;
	static long timeoutSecs = 2400;//40min
	TimerTask task = new TimerTask(){  
		public void run(){  
			if( timeout ){  
				System.out.println( "TIMEOUT!!" );  
				System.exit( 0 );  
			}  
		}  
	};



	public static void main(String[] args){

		if (args.length == 0)
			//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/aeo_v3.7/aeo_v3.7.owl"};
			//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/regulation-of-gene-expression-ontolology/regulation-of-gene-expression-ontolology.3152.owl.xml"};
			//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/00033_galen/00033.owl"};
			//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/00354_nif/00354.owl"};
			//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/cao_v1.4/cao_v1.4.owl"};
//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/fly_anatomy/fly_anatomy_XP/fly_anatomy_XP.owl"};
		//			args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/ExtendedSNOMED/anatomy2012EL.owl"};

			args = new String[]{"file:/share/Ana/AAAI2014tests/ontologies2test/fly_anatomy_XP/fly_anatomy_XP.owl"};
//			args = new String[]{"file:/share/Ana/AAAI2014tests/ontologies2test/dummy/dummyOntology.owl"};
//			args = new String[]{"file:/share/Ana/AAAI2014tests/ontologies2test/00040_go/00040.owl"};
		


		iri_onto = args[0];
		loadOntology();

		ExecutorService executor = Executors.newFixedThreadPool(1);
		Future<?> future = executor.submit(new Runnable() {
			public void run() {
				try {
					long t = System.currentTimeMillis();
					new secondTest().run();
					t = System.currentTimeMillis() - t;
					System.out.println(t + " ms");

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
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
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void run() throws JRDFStoreException{
		
		Set<OWLEntity> signature;
		Set<OWLAxiom> ret;
		PrisM extractor;
		
		
		
//		signature = new HashSet<OWLEntity>(selectNrandomClasses(50, root_ontology.getClassesInSignature()));
		signature = new HashSet<OWLEntity>(
				selectEntities(
						new String[]{
								"<http://purl.obolibrary.org/obo/FBbt_00000017>", 
								"<http://purl.obolibrary.org/obo/BFO_0000050>", 
								"<http://purl.obolibrary.org/obo/FBbt_00007633>"}, 
						root_ontology.getSignature()));
		

		
		
		Long t = System.currentTimeMillis();
		
		
		SyntacticLocalityModuleExtractor slExtractor =new SyntacticLocalityModuleExtractor(manager, root_ontology, ModuleType.STAR); 
		for (OWLAxiom ax : slExtractor.extract(signature))
			System.out.println(ax.toString());
		System.out.println();
		
		
//		//////
//		signature = new HashSet<OWLEntity>();
//		for (OWLEntity e : root_ontology.getSignature()){
//			if (e.toString().endsWith("#A>") || e.toString().endsWith("#D>") || e.toString().endsWith("#R>"))
//				signature.add(e);
//		}
//		//////
//		extractor = new TailoredModulesExtractor(root_ontology, TailoredModuleType.Bottom);
//		ret = extractor.extract(signature, true); 
//		System.out.println(ret.size());
//		print(ret);
//		System.out.println(System.currentTimeMillis() - t + " ms");
//		t = System.currentTimeMillis();
//
//		reloadOntology();
//		extractor = new TailoredModulesExtractor(root_ontology, TailoredModuleType.Star);
//		ret = extractor.extract(signature, true); 
//		System.out.println(ret.size());
//		print(ret);
//		System.out.println(System.currentTimeMillis() - t + " ms");
//		t = System.currentTimeMillis();

//		reloadOntology();
////		//////
////		signature = new HashSet<OWLEntity>();
////		for (OWLClass c : root_ontology.getClassesInSignature()){
////			if (c.toString().endsWith("#A>") || c.toString().endsWith("#B>"))
////				signature.add(c);
////		}
////		//////
//		extractor = new TailoredModulesExtractor(root_ontology, TailoredModuleType.CQ);
//		ret = extractor.extract(signature, true); 
//		System.out.println(ret.size());
//		print(ret);
//		//		System.out.println(extractor.extract(signature, false).size());
//		System.out.println(System.currentTimeMillis() - t + " ms");
//		t = System.currentTimeMillis();
//
////		//////
////		signature = new HashSet<OWLEntity>();
////		for (OWLClass c : root_ontology.getClassesInSignature()){
////			if (c.toString().endsWith("#B>") || c.toString().endsWith("#C>") || c.toString().endsWith("#D>") || c.toString().endsWith("#G>") )
////				signature.add(c);
////		}
////		for (OWLEntity e : root_ontology.getSignature()){
////			if (e.toString().endsWith("#R>"))
////				signature.add(e);
////		}
////		//////
//		reloadOntology();
//		extractor = new TailoredModulesExtractor(root_ontology, TailoredModuleType.DisjDat);
//		ret = extractor.extract(signature, true); 
//		System.out.println(ret.size());
//		print(ret);
//		//		System.out.println(extractor.extract(signature, false).size());
//		System.out.println(System.currentTimeMillis() - t + " ms");
//		t = System.currentTimeMillis();
//
//		reloadOntology();
		extractor = new PrisM(root_ontology, InseparabilityRelation.IMPLICATION_INSEPARABILITY);
		ret = extractor.extract(signature, true); 
		System.out.println(ret.size());
		print(ret);
		//		System.out.println(extractor.extract(signature, false).size());
		System.out.println(System.currentTimeMillis() - t + " ms");
		t = System.currentTimeMillis();
//
//		reloadOntology();
//		extractor = new TailoredModulesExtractor(root_ontology, TailoredModuleType.ConceptClassification);
//		ret = extractor.extract(signature, true); 
//		System.out.println(ret.size());
//		print(ret);
//		//		System.out.println(extractor.extract(signature, false).size());
//		System.out.println(System.currentTimeMillis() - t + " ms");
//		t = System.currentTimeMillis();

		unloadOntology();
		System.exit(0);
	}


	public static void print(Set<OWLAxiom> axioms){
		for (OWLAxiom ax : axioms)
			System.out.println(ax.toString());
	}
	
	
	protected Map<AtomicConcept, Collection<AtomicConcept>> getLowerBound(
			OWLReasoner reasoner, OWLOntology module) {
		Map<AtomicConcept, Collection<AtomicConcept>> lowerBound = new HashMap<AtomicConcept, Collection<AtomicConcept>>();
		for (OWLClass c : module.getClassesInSignature()){
			Set<AtomicConcept> aux = new HashSet<AtomicConcept>();
			if (reasoner.isSatisfiable(c)){
				for (OWLClass sup : reasoner.getEquivalentClasses(c).getEntities())	
					aux.add(AtomicConcept.create(sup.getIRI().toString()));
				for (OWLClass sup : reasoner.getSuperClasses(c, false).getFlattened())
					aux.add(AtomicConcept.create(sup.getIRI().toString()));
			}
			else
				aux.add(AtomicConcept.NOTHING);
			lowerBound.put(AtomicConcept.create(c.getIRI().toString()), aux);			
		}
		return lowerBound;
	}


	protected Set<OWLClass> selectNrandomClasses(int n, Set<OWLClass> set){
		if (set.size() < n)
			return set;

		Set<OWLClass> selectedElements = new HashSet<OWLClass>();
		int counter = 0;
		Random r = new Random();
		Iterator<OWLClass> it = set.iterator();
		while (counter < n){
			if (it.hasNext()){
				OWLClass e = it.next();
				if (!e.toString().contains("internal:") && r.nextInt(100) < 50){
					selectedElements.add(e);
					counter++;
				}
			}
			else
				it = set.iterator();
		}
		return selectedElements;
	}

	protected Set<OWLEntity> selectEntities(String[] entities, Set<OWLEntity> allEntities){
		Set<OWLEntity> selectedElements = new HashSet<OWLEntity>();
		int counter = 0;
		
		for (OWLEntity e : allEntities){
			for (String s : entities)
				if (e.toString().equals(s)){
					selectedElements.add(e);
					counter++;
					break;
				}

			if (counter == entities.length)
				break;
		}
		return selectedElements;
	}


	//	protected OWLOntology normalize(OWLOntology o){
	//		//Input needs to be a SRIF ontology
	//		try {
	//			MyOWLNormalizationForDatalogEncoding normalization = new MyOWLNormalizationForDatalogEncoding(o);
	//			OWLOntology normalizedOntology = OWLManager.createOWLOntologyManager().createOntology(
	//					normalization.getNormalizedOntology(), 
	//					IRI.create("normalizedOntology"));
	//			return normalizedOntology;
	//		} catch (OWLOntologyCreationException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		} 
	//		return null;
	//	}


	private static void loadOntology(){
		try {
			root_ontology = manager.loadOntology(IRI.create(iri_onto));
			root_ontology_iri = manager.getOntologyDocumentIRI(root_ontology).toString().replace("<","").replace(">", "");//getOWLOntologyManager().getOntologyDocumentIRI(root_ontology).toString();

			Set<OWLAxiom> rtBox = new HashSet<OWLAxiom>(root_ontology.getTBoxAxioms(true));
			rtBox.addAll(root_ontology.getRBoxAxioms(true));

			manager.removeOntology(root_ontology);
			root_ontology = manager.createOntology(rtBox, IRI.create(root_ontology_iri.replace(".owl", "-RTBox.owl")));

			System.out.println("\nLoaded ontology: " + iri_onto);

		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			System.exit(0);
		}

	}

	private static void reloadOntology(){
		unloadOntology();
		manager = OWLManager.createOWLOntologyManager();
		loadOntology();
	}


	private static void unloadOntology(){
		manager.removeOntology(root_ontology);
		System.out.println("Unloaded");
	}
}






