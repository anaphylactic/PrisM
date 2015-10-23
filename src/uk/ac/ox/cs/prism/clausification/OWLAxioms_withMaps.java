package uk.ac.ox.cs.prism.clausification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.structural.OWLAxioms;
import org.semanticweb.HermiT.structural.OWLAxioms.ComplexObjectPropertyInclusion;
import org.semanticweb.HermiT.structural.OWLAxioms.DisjunctiveRule;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

public class OWLAxioms_withMaps extends OWLAxioms{
	
//	  public final Set<OWLClass> m_classes; //kept
//    public final Set<OWLObjectProperty> m_objectProperties; //kept
//    public final Set<OWLObjectProperty> m_objectPropertiesOccurringInOWLAxioms; //kept
//    public final Set<OWLObjectPropertyExpression> m_complexObjectPropertyExpressions; //kept
//    public final Set<OWLDataProperty> m_dataProperties; //kept
//    public final Set<OWLNamedIndividual> m_namedIndividuals; // kept
//    public final Collection<OWLClassExpression[]> m_conceptInclusions; //replaced by the following 2:
    public final Map<OWLAxiom,Collection<OWLClassExpression[]>> m_conceptInclusions_map;
	public final Collection<OWLClassExpression[]> m_auxiliaryConceptInclusions;
	//each axiom may lead to several axioms when normalised; one (or more, if distribution of union over disjunction is applied) 
	//of them will be the 'core' of its normalization, and the rest will be auxiliary axioms to abbreviate nested complex class expressions 
//    public final Collection<OWLDataRange[]> m_dataRangeInclusions; //replaced by the following 2:
	public final Map<OWLAxiom,Collection<OWLDataRange[]>> m_dataRangeInclusions_map;
	public final Collection<OWLDataRange[]> m_auxiliaryDataRangeInclusions;
	//similar to the case of concept inclusions
//	public final Collection<OWLObjectPropertyExpression[]> m_simpleObjectPropertyInclusions; //replaced by the following
	public final Map<OWLAxiom,Collection<OWLObjectPropertyExpression[]>> m_simpleObjectPropertyInclusions_map;
//	public final Collection<ComplexObjectPropertyInclusion> m_complexObjectPropertyInclusions; //kept - can be straightforwardly reconstructed
//    public final Collection<OWLObjectPropertyExpression[]> m_disjointObjectProperties; //kept
//    public final Set<OWLObjectPropertyExpression> m_reflexiveObjectProperties; //kept
//    public final Set<OWLObjectPropertyExpression> m_irreflexiveObjectProperties; //kept
	public final Collection<OWLObjectProperty> m_symmetricObjectProperties; //were previously stored as object property inclusions
//    public final Set<OWLObjectPropertyExpression> m_asymmetricObjectProperties; //kept
//	public final Collection<OWLDataPropertyExpression[]> m_dataPropertyInclusions; //replaced by the following
	public final Map<OWLAxiom,Collection<OWLDataPropertyExpression[]>> m_dataPropertyInclusions_map;
//    public final Collection<OWLDataPropertyExpression[]> m_disjointDataProperties; //kept
//    public final Collection<OWLIndividualAxiom> m_facts; //replaced by the following
	public final Map<OWLAxiom,Collection<OWLIndividualAxiom>> m_facts_map;
//	public final Set<OWLHasKeyAxiom> m_hasKeys; //replaced by the following 
	public final Map<OWLAxiom,OWLHasKeyAxiom> m_hasKeys_map;
//    public final Set<String> m_definedDatatypesIRIs; //kept
//    public final Collection<DisjunctiveRule> m_rules; //replaced by the following
	public final Map<OWLAxiom,Collection<DisjunctiveRule>> m_rules_map;
    

	public OWLAxioms_withMaps() {
		super();    	
		m_conceptInclusions_map=new HashMap<OWLAxiom, Collection<OWLClassExpression[]>>();
		m_auxiliaryConceptInclusions = new ArrayList<OWLClassExpression[]>();
		m_simpleObjectPropertyInclusions_map = new HashMap<OWLAxiom, Collection<OWLObjectPropertyExpression[]>>();
		m_symmetricObjectProperties=new ArrayList<OWLObjectProperty>();
		m_dataPropertyInclusions_map = new HashMap<OWLAxiom, Collection<OWLDataPropertyExpression[]>>();
		m_facts_map = new HashMap<OWLAxiom, Collection<OWLIndividualAxiom>>();
		m_hasKeys_map = new HashMap<OWLAxiom, OWLHasKeyAxiom>();
		m_dataRangeInclusions_map = new HashMap<OWLAxiom, Collection<OWLDataRange[]>>();
		m_auxiliaryDataRangeInclusions = new ArrayList<OWLDataRange[]>();
		m_rules_map = new HashMap<OWLAxiom, Collection<DisjunctiveRule>>();
	}

}
