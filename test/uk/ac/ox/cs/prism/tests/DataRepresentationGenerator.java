package uk.ac.ox.cs.prism.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class DataRepresentationGenerator{
	
	static OWLOntology root_ontology;
	static String root_ontology_iri;
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static String iri_onto;
	static OWLOntology normalisedOntology;
	static Set<OWLAxiom> normalisedAxioms;
	
	static DataRepresentationGenerator inst = new DataRepresentationGenerator();
	static String reachabilityProperty = "<canReach>";
	static boolean axiomsAsNodes = false;
	static boolean removeABox = false;
	
	public static void main(String[] args){
		if (args.length == 0){
//			args = new String[]{"http://www.cs.ox.ac.uk/isg/ontologies/UID/00007.owl", "DataRepresentation/00007","data_00007.ttl"};
//			args = new String[]{"http://www.cs.ox.ac.uk/isg/ontologies/UID/00001.owl", "DataRepresentation/00049","data_00001.ttl"};
//			args = new String[]{"http://www.cs.ox.ac.uk/isg/ontologies/UID/00024.owl", "DataRepresentation/00049","data_00024.ttl"};
//			args = new String[]{"http://www.cs.ox.ac.uk/isg/ontologies/UID/00285.owl", "DataRepresentation/00049","data_00285.ttl"};
//			args = new String[]{"http://www.cs.ox.ac.uk/isg/ontologies/UID/00350.owl", "DataRepresentation/00049","data_00350.ttl"};
//			args = new String[]{"http://www.cs.ox.ac.uk/isg/ontologies/UID/00351.owl", "DataRepresentation/00049","data_00351.ttl"};
			args = new String[]{"http://www.cs.ox.ac.uk/isg/ontologies/UID/00354.owl", "DataRepresentation/00049","data_00354.ttl"};
//			args = new String[]{"http://www.cs.ox.ac.uk/isg/ontologies/UID/00775.owl", "DataRepresentation/00049","data_00775.ttl"};
		}

		iri_onto = args[0];
		loadOntology();
		
		new File(args[1]).mkdirs();
		
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File(args[1] + "/" + args[2]));
			
			
			if (axiomsAsNodes){
				Axiom[] indexedAxioms = indexAxioms(normalisedAxioms);
				for (int i = 0 ; i<indexedAxioms.length ; i++)
					for (int j = i ; j<indexedAxioms.length ; j++){
						Axiom ax1 = indexedAxioms[i];
						Axiom ax2 = indexedAxioms[j];
						Set<OWLEntity> aux = new HashSet<OWLEntity>(ax1.sig);
						aux.retainAll(ax2.sig);
						if (!aux.isEmpty()){
							out.println(getFact(ax1.stringRep.toString(),ax2.stringRep.toString()));
						}
					}	
			}
			else{
				Map<OWLEntity,Set<Axiom>> map = new HashMap<OWLEntity, Set<Axiom>>();	
				for (OWLAxiom ax : normalisedAxioms){
					Axiom axiom = inst.new Axiom(ax, false);
					for (OWLEntity e : axiom.sig){
						Set<Axiom> aux = map.get(e);
						if (aux == null){
							aux = new HashSet<Axiom>();
							map.put(e, aux);
						}
						aux.add(axiom);							
					}
				}
				for (Entry<OWLEntity, Set<Axiom>> entry : map.entrySet()){
					OWLEntity e1 = entry.getKey();
					String s1 = e1.toString();
					
					if (e1 instanceof OWLClass)
						s1 = s1.replace("<", "<*&*");
					else if (e1 instanceof OWLObjectProperty)
						s1 = s1.replace("<", "<*&&*");
					else
						throw new IllegalStateException("we only want to consider Classes and Properties in signatures");
					
					for (Axiom axiom : entry.getValue()){
						Iterator<OWLEntity> iter = axiom.sig.iterator();
						while (iter.hasNext()){
							OWLEntity e2 = iter.next();
							if (e2.equals(e1))
								iter.remove();
							else{
								String s2 = e2.toString();
								
								if (e2 instanceof OWLClass)
									s2 = s2.replace("<", "<*&*");
								else if (e2 instanceof OWLObjectProperty)
									s2 = s2.replace("<", "<*&&*");
								else
									throw new IllegalStateException("we only want to consider Classes and Properties in signatures");
								
								out.println(getFact(s1,s2));
							}
						}
					}
				}
				
				
							
				
				
				
//				for (OWLAxiom ax : normalisedAxioms){
//					OWLEntity[] indexedSignature = indexSignature(ax.getSignature());
//					for (int i = 0 ; i<indexedSignature.length ; i++)
//						for (int j = i+1 ; j<indexedSignature.length ; j++){
//							OWLEntity e1 = indexedSignature[i];
//							OWLEntity e2 = indexedSignature[j];
//							if (!e1.isBottomEntity() && !e1.isTopEntity() && !e2.isBottomEntity() && !e2.isTopEntity()){
//								Set<OWLEntity> fromE1 = map.get(e1);
//								Set<OWLEntity> fromE2 = map.get(e2);
//								if ((fromE1 == null || !fromE1.contains(e2)) &&
//										(fromE2 == null || !fromE2.contains(e1))){
//									out.println(getFact(e1.toString(),e2.toString()));
//									if (fromE1 == null){
//										fromE1 = new HashSet<OWLEntity>();
//										map.put(e1, fromE1);
//									}
//									fromE1.add(e2);
//								}	
//							}
//						}	
//				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			out.close();
		}
		
		unloadOntology();
	}
	
	protected static String getFact(String arg1, String arg2){
		StringBuilder sb = new StringBuilder();
		sb.append(arg1.toString() + " " + reachabilityProperty + " " + arg2.toString() + " .");
		return sb.toString();
	}
	
	
	protected static Axiom[] indexAxioms(Set<OWLAxiom> axioms){
		Axiom[] ret = new Axiom[axioms.size()];
		int counter = 0;
		for (OWLAxiom ax : axioms)
			ret[counter++] = inst.new Axiom(ax,true);
		return ret;
	}
	
	private static void loadOntology(){
		try {
			root_ontology = manager.loadOntology(IRI.create(iri_onto));
			root_ontology_iri = manager.getOntologyDocumentIRI(root_ontology).toString().replace("<","").replace(">", "");//getOWLOntologyManager().getOntologyDocumentIRI(root_ontology).toString();

//			//////
//			Set<OWLAxiom> aBox = root_ontology.getABoxAxioms(true);
//			if (!aBox.isEmpty())
//				for (OWLAxiom ax : aBox)
//					System.out.println(ax.toString());
//			else
//				System.out.println("no aBox axioms! :D");
//			//////
			
			if (removeABox){
				Set<OWLAxiom> rtBox = new HashSet<OWLAxiom>(root_ontology.getTBoxAxioms(true));
				rtBox.addAll(root_ontology.getRBoxAxioms(true));
				manager.removeOntology(root_ontology);
				root_ontology = manager.createOntology(rtBox, IRI.create(root_ontology_iri.replace(".owl", "-RTBox.owl")));	
			}

			normalisedAxioms = NormaliserViaClauses.getNormalisedAxioms(root_ontology);
			normalisedOntology = manager.createOntology(normalisedAxioms, IRI.create(root_ontology_iri.replace(".owl", "_normalised_RTBox.owl")));
//			try {
//				manager.saveOntology(normalisedOntology);
//			} catch (OWLOntologyStorageException e) {
//				e.printStackTrace();
//			}

			System.out.println("\nLoaded ontology: " + iri_onto);

		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			System.exit(0);
		}

	}

	private static void unloadOntology(){
		manager.removeOntology(root_ontology);
		System.out.println("Unloaded");
	}

	protected static Set<OWLEntity> getClassesAndPropertiesInSignature(Set<OWLEntity> sig) {
		Set<OWLEntity> filteredSig = new HashSet<OWLEntity>();
		for (OWLEntity e : sig)
			if (e instanceof OWLObjectProperty ||
					(e instanceof OWLClass && !(((OWLClass) e).isOWLThing() || ((OWLClass) e).isOWLNothing())))
				filteredSig.add(e);
		return filteredSig;
	}

	public class Axiom{
		OWLAxiom ax;
		Set<OWLEntity> sig;
		StringBuilder stringRep;
		
		public Axiom(OWLAxiom ax, boolean withStringRep){
			this.ax = ax;
			sig = getClassesAndPropertiesInSignature(ax.getSignature());
			if (withStringRep){
				stringRep = new StringBuilder("<");
				for (OWLEntity e : sig){
					if (e instanceof OWLClass){
						stringRep.append("*&*");
						stringRep.append(e.toStringID());
					}
					else if (e instanceof OWLObjectProperty){
						stringRep.append("*&&*");
						stringRep.append(e.toStringID());
					}
				}
				stringRep.append(">");	
			}
		}
		
	}
	
	
}