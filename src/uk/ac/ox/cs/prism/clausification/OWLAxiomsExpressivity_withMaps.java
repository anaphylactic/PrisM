package uk.ac.ox.cs.prism.clausification;

import java.util.Collection;
import java.util.Map.Entry;

import org.semanticweb.HermiT.structural.OWLAxioms;
import org.semanticweb.HermiT.structural.OWLAxiomsExpressivity;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;


public class OWLAxiomsExpressivity_withMaps extends OWLAxiomsExpressivity {

	    public OWLAxiomsExpressivity_withMaps(OWLAxioms axioms) {
	    	super(new OWLAxioms());
	    	for (Entry<OWLAxiom,Collection<OWLClassExpression[]>> entry : ((OWLAxioms_withMaps) axioms).m_conceptInclusions_map.entrySet()) 
	    		for (OWLClassExpression[] inclusion : entry.getValue())
		            for (OWLClassExpression description : inclusion)
		                description.accept(this);
	        for (OWLClassExpression[] inclusion : ((OWLAxioms_withMaps) axioms).m_auxiliaryConceptInclusions)
	            for (OWLClassExpression description : inclusion)
	                description.accept(this);
	        for (Entry<OWLAxiom,Collection<OWLObjectPropertyExpression[]>> entry : ((OWLAxioms_withMaps) axioms).m_simpleObjectPropertyInclusions_map.entrySet())
	        	for (OWLObjectPropertyExpression[] inclusion : entry.getValue()) {
	        		visitProperty(inclusion[0]);
	        		visitProperty(inclusion[1]);
	        	}
	        for (OWLAxioms.ComplexObjectPropertyInclusion inclusion : axioms.m_complexObjectPropertyInclusions) {
	            for (OWLObjectPropertyExpression subObjectProperty : inclusion.m_subObjectProperties)
	                visitProperty(subObjectProperty);
	            visitProperty(inclusion.m_superObjectProperty);
	        }
	        for (OWLObjectPropertyExpression[] disjoint : axioms.m_disjointObjectProperties)
	            for (int index=0;index<disjoint.length;index++)
	                visitProperty(disjoint[index]);
	        for (OWLObjectPropertyExpression property : axioms.m_reflexiveObjectProperties)
	            visitProperty(property);
	        for (OWLObjectPropertyExpression property : axioms.m_irreflexiveObjectProperties)
	            visitProperty(property);
	        for (OWLObjectPropertyExpression property : ((OWLAxioms_withMaps) axioms).m_symmetricObjectProperties)
	            visitProperty(property);
	        for (OWLObjectPropertyExpression property : axioms.m_asymmetricObjectProperties)
	            visitProperty(property);
	        if (!axioms.m_dataProperties.isEmpty()
	        		|| !axioms.m_disjointDataProperties.isEmpty()
	        		|| !((OWLAxioms_withMaps) axioms).m_dataPropertyInclusions_map.isEmpty()
	        		|| !((OWLAxioms_withMaps) axioms).m_dataRangeInclusions_map.isEmpty()
	        		|| !((OWLAxioms_withMaps) axioms).m_auxiliaryDataRangeInclusions.isEmpty()
	        		|| !axioms.m_definedDatatypesIRIs.isEmpty())
	            m_hasDatatypes=true;
	        for (Entry<OWLAxiom,Collection<OWLIndividualAxiom>> entry : ((OWLAxioms_withMaps) axioms).m_facts_map.entrySet())
	        	for (OWLIndividualAxiom fact : entry.getValue())
	        		fact.accept(this);
	        m_hasSWRLRules=!((OWLAxioms_withMaps) axioms).m_rules_map.isEmpty();
	    }
	}
