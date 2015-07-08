package uk.ac.ox.cs.prism.tests;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;


public class LoadingTest {

	static OWLOntology root_ontology;
	static String root_ontology_iri;
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static String iri_onto;
	static OWLOntology normalisedOntology;
	static Set<OWLAxiom> normalisedAxioms;
	//	static String outputFileName = "RandomSignaturesTestResults.txt";
	static boolean removeABox = false;

	
	public static void main(String[] args){
//		args = new String[]{"file:/Users/Ana/Documents/Work/ontologies/fly_anatomy/fly_anatomy_XP/fly_anatomy_XP.owl"};
//		args = new String[]{"http://www.cs.ox.ac.uk/isg/ontologies/UID/00001.owl"};
		
		iri_onto = args[0];
		loadOntology();
		Reasoner r = new Reasoner(normalisedOntology);
		r.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		reloadNormalisedOntology();
		r = new Reasoner(normalisedOntology); 
		unloadOntology();
		System.exit(0);
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






