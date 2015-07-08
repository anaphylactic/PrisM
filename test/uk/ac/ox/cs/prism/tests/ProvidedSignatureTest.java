package uk.ac.ox.cs.prism.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
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
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.prism.PrisM;
import uk.ac.ox.cs.prism.PrisM.InseparabilityRelation;

public class ProvidedSignatureTest {

	static OWLOntology root_ontology;
	static String root_ontology_iri;
	static String ontologyName;
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static String iri_onto;
	static OWLOntology normalisedOntology;
	static Set<OWLAxiom> normalisedAxioms;
	static String signature;
	//	static String outputFileName = "RandomSignaturesTestResults.txt";

	protected static boolean timeout = false;
	static long timeoutSecs = 32400;//9h
	static int nThreads = 16;
	static boolean removeABox = false;


	public static void main(String[] args){

//		signature  = "<http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Intercellular_Communication>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Protein-Protein_Interaction>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Biosynthesis>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Dentatorubropallidoluysian_Atrophy_Pathway>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Pentose_and_Glucuronate_Interconversions_Pathway>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#LIM_Domains_Containing_1>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Ricin_Immunoconjugate>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Oxidoreductase>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Common_Data_Element>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#ChREBP_Pathway>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Extracellular_Space>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Valine_Leucine_and_Isoleucine_Degradation_Pathway>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Aldehyde_Dehydrogenase_Mitochondrial>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Arginine_and_Proline_Metabolism_Pathway>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Gene_Is_Element_In_Pathway>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Aldehyde_Dehydrogenase_X_Mitochondrial>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Angiogenesis_Inhibitory_Protein>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#HK2_Gene>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#_12q24>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#INS_Gene>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#MAP-Kinase-14>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#NTRK1-PI3K-PLC-Gamma_Signaling_Pathway>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Visit_Day>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Inflammatory_Response>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#CREB1_Gene>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#_12q24_2>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Precursor_T_Lymphoblastic_Leukemia>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Pro-Platelet_Basic_Protein>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#DOK1_wt_Allele>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#_2p13>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Protein_Phosphatase_Regulatory_Protein>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#CASP1_Gene>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Macrophage>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Propanoate_Metabolism_Pathway>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Glycolysis_Gluconeogenesis_Pathway>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Human>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Cytoplasm>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Apoptosis>, <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#WNT4_Gene>";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00786.owl";
//		ontologyName = "00786";
		
//		signature  = "";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00351.owl";
//		ontologyName = "00351";
		
		signature  = "<http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#hasPrenyl_Group>, <internal:def#371>, <internal:def#172>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#Quinone_ring_system>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_Stigmasterol_structural_derivative>, <internal:def#174>, <internal:def#375>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#Prenyl>, <internal:def#179>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#Simple_Organic_Group>, <internal:def#332>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#Cyclopropane>, <internal:def#134>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_Sterol>, <internal:def#402>, <internal:def#446>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#Galactose>, <internal:def#67>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_C23_bile_acid_structural_derivative>, <internal:def#68>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_Diacylglycerophosphate>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_Vitamin_K>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_Quinone_par_inclusive_of_hydroquinone_par_>, <internal:def#146>, <internal:def#145>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#Bicyclic_5_membered_Heterocyclic_Group>, <internal:def#138>, <internal:def#217>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_Ergosterol_structural_derivative>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#Isoprene_Chain>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_Alkenylacylglycerophosphoglycerol>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#Ketone>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_Diacylglycerophosphomonoradylglycerol>, <internal:def#36>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#Monomeric_Glycan_Group>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_Calysterol_structural_derivative>, <internal:def#193>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_Ubiquinone>, <internal:def#393>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_Monoacylglycerophosphate>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_Isoprenoid>, <internal:def#350>, <internal:def#199>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_Dialkylglycerophosphoglycerol>, <internal:def#103>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_General_Methoxy_mycolic_acid>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#Alkenyl_Group>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_Hopanoid>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#non-terminal_Methoxy>, <internal:def#83>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_Monoacylglycerophosphoglycerol>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_C28_bile_acid_structural_derivative>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#Glycerophosphotidylglycerophosphoglycerol>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#hasPart>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#Tocoquinone_ring>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_CDP-Alkylacylglycerol>, <internal:def#164>, <internal:def#241>, <internal:def#361>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_CDP-monoacylglycerol>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#Ether>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_num1-alkyl_glycerophosphoglycerol>, <internal:def#117>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#Ubiquinone_ring>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#LC_Dialkylglycerophosphoglycerophosphodiradylglycerol>, <internal:def#51>, <internal:def#95>, <internal:def#96>, <http://NUS.I2R.lipidontology.biochem.nus.edu.sg/lipidversion3.owl#Trehalose>";
		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00512.owl";
		ontologyName = "00512";
		
		
		loadOntology();
		
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Future<?> future = executor.submit(new Runnable() {
			public void run() {
				try {
					long t = System.currentTimeMillis();
					new ProvidedSignatureTest().run();
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
		extractModulesForSignature(readEntitiesFromString(signature));
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
		System.out.println("# ConceptClassification");
		extractor = new PrisM(normalisedOntology, InseparabilityRelation.CLASSIFICATION_INSEPARABILITY, nThreads);
		s = s + extractor.extract(signature).size() + " \t " + (System.currentTimeMillis() - t) + " \t ";
		extractor.finishDisposal();
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
//			normalisedOntology = manager.createOntology(normalisedAxioms, IRI.create(root_ontology_iri.replace(".owl", "_normalised_RTBox.owl")));
			File f = new File("DataRepresentation/" + ontologyName + "/normalised_" + ontologyName + ".owl");
			String path = f.getAbsolutePath();
			normalisedOntology = manager.createOntology(normalisedAxioms, IRI.create("file:"+path));
			
			
			try {
				System.out.println(manager.getOntologyDocumentIRI(normalisedOntology).toString());
				manager.saveOntology(normalisedOntology);
			} catch (OWLOntologyStorageException e) {
				e.printStackTrace();
			}
				
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


	private Set<OWLEntity> readEntitiesFromString(String s){
		OWLDataFactory factory = new OWLDataFactoryImpl();
		Set<OWLEntity> ret = new HashSet<OWLEntity>();
//		first parse String
		Set<OWLEntity> aux = new HashSet<OWLEntity>();
		while (!s.isEmpty()){
			int i = s.indexOf(">");
			String aux2 = s.substring(1, i);
			aux.add(factory.getOWLClass(IRI.create(aux2)));
			aux.add(factory.getOWLObjectProperty(IRI.create(aux2)));
			
			i = s.indexOf(",");
			if (i >= 0)
				s = s.substring(i+2);
			else
				s = "";
			
			
		}
//		then collect entities
		for (OWLEntity e : normalisedOntology.getSignature()){
			if (aux.contains(e))
				ret.add(e);
		}
		System.out.println("Signature has been processed");
		return ret;
	}


}
