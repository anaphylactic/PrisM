package uk.ac.ox.cs.prism.tests;

import java.util.Set;

import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ox.cs.pagoda.constraints.UnaryBottom;
import uk.ac.ox.cs.prism.IndividualManager;
import uk.ac.ox.cs.prism.ModuleExtractionUpperProgram;
import uk.ac.ox.cs.prism.PrisM.InseparabilityRelation;

public class EmptyBodyDebugger {

	static OWLOntology root_ontology;
	static String root_ontology_iri;
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static String iri_onto;
	static OWLOntology normalisedOntology;
	static Set<OWLAxiom> normalisedAxioms;
	//	static String outputFileName = "RandomSignaturesTestResults.txt";

	
	
	

	public static void main(String[] args){


//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00001.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00004.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00024.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00026.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00029.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00032.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00347.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00350.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00351.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00354.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00463.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00471.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00477.owl";
		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00512.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00545.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00774.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00775.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00778.owl";
//		iri_onto = "http://www.cs.ox.ac.uk/isg/ontologies/UID/00786.owl";

		loadOntology();
		
		
		ModuleExtractionUpperProgram program = new ModuleExtractionUpperProgram(new IndividualManager(InseparabilityRelation.QUERY_INSEPARABILITY));
//		ModuleExtractionUpperProgram program = new ModuleExtractionUpperProgram(new IndividualManager(InseparabilityRelation.MODEL_INSEPARABILITY));
		program.load(normalisedOntology, new UnaryBottom());
		for (DLClause c : program.getClauses()){
			if (c.getBodyLength() == 0)
				System.out.println(c.toString());
			if (c.getBodyLength() > 5)
				System.out.println(c.toString());
		}

	}


	

	private static void loadOntology(){
		try {
			root_ontology = manager.loadOntology(IRI.create(iri_onto));
			root_ontology_iri = manager.getOntologyDocumentIRI(root_ontology).toString().replace("<","").replace(">", "");//getOWLOntologyManager().getOntologyDocumentIRI(root_ontology).toString();

			normalisedAxioms = NormaliserViaClauses.getNormalisedAxioms(root_ontology);
			normalisedOntology = manager.createOntology(normalisedAxioms, IRI.create(root_ontology_iri.replace(".owl", "_normalised.owl")));

			System.out.println("\nLoaded ontology: " + iri_onto);

		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			System.exit(0);
		}

	}

}

