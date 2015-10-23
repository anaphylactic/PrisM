package uk.ac.ox.cs.pagoda.approx;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uk.ac.ox.cs.pagoda.constraints.NullaryBottom;
import uk.ac.ox.cs.pagoda.constraints.UnaryBottom;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Utility;

public class RLPlusOntology implements KnowledgeBase {
	
	OWLOntologyManager manager;
	OWLDataFactory factory;
	String ontologyIRI;
	String outputPath, aBoxPath;
	
	OWLOntology inputOntology = null;
	OWLOntology tBox = null;
	OWLOntology aBox = null;
	OWLOntology restOntology = null;
	
	DLOntology dlOntology = null;
	int rlCounter = 0;
	
	LinkedList<Clause> clauses; 
//	Map<OWLAxiom, OWLAxiom> correspondence;
	
	BottomStrategy botStrategy; 
	
	@Override
	public void load(OWLOntology o, uk.ac.ox.cs.pagoda.constraints.BottomStrategy bottomStrategy) {
		if (bottomStrategy instanceof UnaryBottom)
			botStrategy = BottomStrategy.UNARY; 
		else if (bottomStrategy instanceof NullaryBottom)
			botStrategy = BottomStrategy.NULLARY; 
		else 
			botStrategy = BottomStrategy.TOREMOVE; 
		
		manager = o.getOWLOntologyManager();
		factory = manager.getOWLDataFactory(); 
		inputOntology = o; 
		
		try {
			ontologyIRI = Utility.TempDirectory + "aux.owl";
			
			String tBoxOntologyIRI, aBoxOntologyIRI; 
			tBoxOntologyIRI = ontologyIRI.replace(".owl", "-TBox.owl"); 
			aBoxOntologyIRI = ontologyIRI.replaceFirst(".owl", "-ABox.owl");
			
			String tBoxDocumentIRI = (Utility.TempDirectory + "TBox.owl");
			String aBoxDocumentIRI = (aBoxPath = Utility.TempDirectory + "ABox.owl");
			tBox = manager.createOntology(IRI.create(tBoxOntologyIRI));
			aBox = manager.createOntology(IRI.create(aBoxOntologyIRI));
			manager.setOntologyDocumentIRI(tBox, IRI.create("file:" + tBoxDocumentIRI));
			manager.setOntologyDocumentIRI(aBox, IRI.create("file:" + aBoxDocumentIRI));

			manager.saveOntology(aBox);
			
			restOntology = manager.createOntology();
		}
		catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
	}
	
	public OWLOntology getTBox() {
		return tBox; 
	}
	
	public String getABoxPath() {
		return aBoxPath; 
	}
	
	public void simplify() {
		if (simplifyABox()) { 
			save(aBox);
//			save(tBox);
		}
		else 
			tBox = inputOntology; 
	}
	
	
	protected void save(OWLOntology onto) {
		try {
			onto.getOWLOntologyManager().saveOntology(onto);
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
	}
	
	private boolean simplifyABox() {
		boolean flag = false;
		Map<OWLClassExpression, OWLClass> complex2atomic= new HashMap<OWLClassExpression, OWLClass>();
		
		for (OWLOntology imported: inputOntology.getImportsClosure())
			for (OWLAxiom axiom: imported.getAxioms()) {
				if (axiom instanceof OWLClassAssertionAxiom) {
					flag = true;
					OWLClassAssertionAxiom assertion = (OWLClassAssertionAxiom)axiom;
					OWLClassExpression clsExp = assertion.getClassExpression();
					OWLClass cls;
					if (clsExp instanceof OWLClass) {
						if (((OWLClass) clsExp).toStringID().startsWith("owl:"))
							manager.addAxiom(tBox, axiom); 
						else manager.addAxiom(aBox, axiom);
					}
					else {
						if ((cls = complex2atomic.get(clsExp)) == null) {
							complex2atomic.put(clsExp, cls = getNewConcept(tBox, rlCounter++));
							manager.addAxiom(tBox, factory.getOWLSubClassOfAxiom(cls, clsExp));
						}
						manager.addAxiom(aBox, factory.getOWLClassAssertionAxiom(cls, assertion.getIndividual()));
					} 
				}
				else if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
					flag = true;
					manager.addAxiom(aBox, axiom);
				}
				else 
					manager.addAxiom(tBox, axiom);
			}
		
		return flag;
	}

	private OWLClass getNewConcept(OWLOntology onto, int number) {
		OWLOntologyManager manager = onto.getOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		OWLClass newClass = factory.getOWLClass(IRI.create(Namespace.PAGODA_AUX + "NC" + number));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(newClass)); 
		return newClass; 
	}

	public OWLOntologyManager getOWLOntologyManager() {
		return inputOntology.getOWLOntologyManager();
	}

	public String getOntologyIRI() {
		return ontologyIRI; 
	}
	
	@Override
	public String getOutputPath() {
		return outputPath;
	}

	@Override
	public String getDirectory() {
		return outputPath.substring(0, outputPath.lastIndexOf(Utility.FILE_SEPARATOR));
	}

	private static enum BottomStrategy { TOREMOVE, NULLARY, UNARY }
}

