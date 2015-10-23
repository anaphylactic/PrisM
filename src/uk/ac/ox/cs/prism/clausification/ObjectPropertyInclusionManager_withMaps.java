package uk.ac.ox.cs.prism.clausification;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.HermiT.graph.Graph;
import org.semanticweb.HermiT.structural.OWLAxioms;
import org.semanticweb.HermiT.structural.OWLAxioms.ComplexObjectPropertyInclusion;
import org.semanticweb.HermiT.structural.ObjectPropertyInclusionManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import rationals.Automaton;
import rationals.State;
import rationals.Transition;

public class ObjectPropertyInclusionManager_withMaps extends ObjectPropertyInclusionManager{

	    protected final Map<OWLObjectPropertyExpression,Automaton> m_automataByProperty;

	    public ObjectPropertyInclusionManager_withMaps(OWLAxioms_withMaps axioms) {
	    	super(new OWLAxioms());
	        m_automataByProperty=new HashMap<OWLObjectPropertyExpression,Automaton>();
	        createAutomata(m_automataByProperty,axioms.m_complexObjectPropertyExpressions,axioms.m_simpleObjectPropertyInclusions_map,axioms.m_complexObjectPropertyInclusions,axioms.m_symmetricObjectProperties);
	    }

	    public int rewriteNegativeObjectPropertyAssertions(OWLDataFactory factory,OWLAxioms axioms,int replacementIndex) {
	        // now object property inclusion manager added all non-simple properties to axioms.m_complexObjectPropertyExpressions
	        // now that we know which roles are non-simple, we can decide which negative object property assertions have to be
	        // expressed as concept assertions so that transitivity rewriting applies properly. All new concepts for the concept
	        // assertions must be normalised, because we are done with the normal normalisation phase.
	        Set<OWLIndividualAxiom> redundantFacts=new HashSet<OWLIndividualAxiom>();
	        Set<OWLIndividualAxiom> additionalFacts=new HashSet<OWLIndividualAxiom>();
	        for (Entry<OWLAxiom,Collection<OWLIndividualAxiom>> entry : ((OWLAxioms_withMaps) axioms).m_facts_map.entrySet()) {
	        	for (OWLIndividualAxiom axiom : entry.getValue()) {
		            if (axiom instanceof OWLNegativeObjectPropertyAssertionAxiom) {
		                OWLNegativeObjectPropertyAssertionAxiom negAssertion=(OWLNegativeObjectPropertyAssertionAxiom)axiom;
		                OWLObjectPropertyExpression prop=negAssertion.getProperty().getSimplified();
		                if (axioms.m_complexObjectPropertyExpressions.contains(prop)) {
		                    // turn not op(a b) into
		                    // C(a) and not C or forall op not{b}
		                    OWLIndividual individual=negAssertion.getObject();
		                    // neg. op assertions cannot contain anonymous individuals
		                    OWLClass individualConcept=factory.getOWLClass(IRI.create("internal:nom#"+individual.asOWLNamedIndividual().getIRI().toString()));
		                    OWLClassExpression notIndividualConcept=factory.getOWLObjectComplementOf(individualConcept);
		                    OWLClassExpression allNotIndividualConcept=factory.getOWLObjectAllValuesFrom(prop,notIndividualConcept);
		                    OWLClassExpression definition=factory.getOWLClass(IRI.create("internal:def#"+(replacementIndex++)));
		                    ((OWLAxioms_withMaps) axioms).m_auxiliaryConceptInclusions.add(new OWLClassExpression[] { factory.getOWLObjectComplementOf(definition), allNotIndividualConcept });
		                    additionalFacts.add(factory.getOWLClassAssertionAxiom(definition,negAssertion.getSubject()));
		                    additionalFacts.add(factory.getOWLClassAssertionAxiom(individualConcept,individual));
		                    redundantFacts.add(negAssertion);
		                }
		            }
		        }
		        entry.getValue().addAll(additionalFacts);
		        entry.getValue().removeAll(redundantFacts);
	        }
	        return replacementIndex;
	    } //DONE    
	    public void rewriteAxioms(OWLDataFactory dataFactory,OWLAxioms axioms,int firstReplacementIndex) {
	        // Check the asymmetric object properties for simplicity
	        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_asymmetricObjectProperties)
	            if (axioms.m_complexObjectPropertyExpressions.contains(objectPropertyExpression))
	                throw new IllegalArgumentException("Non-simple property '"+objectPropertyExpression+"' or its inverse appears in asymmetric object property axiom.");
	        // Check the irreflexive object properties for simplicity
	        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_irreflexiveObjectProperties)
	            if (axioms.m_complexObjectPropertyExpressions.contains(objectPropertyExpression))
	                throw new IllegalArgumentException("Non-simple property '"+objectPropertyExpression+"' or its inverse appears in irreflexive object property axiom.");
	        // Check the disjoint object properties for simplicity
	        for (OWLObjectPropertyExpression[] properties : axioms.m_disjointObjectProperties)
	            for (int i=0;i<properties.length;i++)
	                if (axioms.m_complexObjectPropertyExpressions.contains(properties[i]))
	                    throw new IllegalArgumentException("Non-simple property '"+properties[i]+"' or its inverse appears in disjoint properties axiom.");
	        // Check simple properties in the number restrictions and replace universals
	        Map<OWLObjectAllValuesFrom,OWLClassExpression> replacedDescriptions=new HashMap<OWLObjectAllValuesFrom,OWLClassExpression>();
	        
	        for (Entry<OWLAxiom,Collection<OWLClassExpression[]>> entry : ((OWLAxioms_withMaps) axioms).m_conceptInclusions_map.entrySet()) {
	        	for (OWLClassExpression[] inclusion : entry.getValue()) {
		            for (int index=0;index<inclusion.length;index++) {
		                OWLClassExpression classExpression=inclusion[index];
		                if (classExpression instanceof OWLObjectCardinalityRestriction) {
		                    OWLObjectCardinalityRestriction objectCardinalityRestriction=(OWLObjectCardinalityRestriction)classExpression;
		                    OWLObjectPropertyExpression objectPropertyExpression=objectCardinalityRestriction.getProperty();
		                    if (axioms.m_complexObjectPropertyExpressions.contains(objectPropertyExpression))
		                        throw new IllegalArgumentException("Non-simple property '"+objectPropertyExpression+"' or its inverse appears in the cardinality restriction '"+objectCardinalityRestriction+"'.");
		                }
		                else if (classExpression instanceof OWLObjectHasSelf) {
		                	OWLObjectHasSelf objectSelfRestriction=(OWLObjectHasSelf)classExpression;
		                	if (axioms.m_complexObjectPropertyExpressions.contains(objectSelfRestriction.getProperty()))
		                        throw new IllegalArgumentException("Non-simple property '"+objectSelfRestriction.getProperty()+"' or its inverse appears in the Self restriction '"+objectSelfRestriction+"'.");
		                }
		                if (classExpression instanceof OWLObjectAllValuesFrom) {
		                    OWLObjectAllValuesFrom objectAll=(OWLObjectAllValuesFrom)classExpression;
		                    if (!objectAll.getFiller().equals(dataFactory.getOWLThing())) {
		                        OWLObjectPropertyExpression objectProperty=objectAll.getProperty();
		                        if (m_automataByProperty.containsKey(objectProperty)) {
		                            OWLClassExpression replacement=replacedDescriptions.get(objectAll);
		                            if (replacement==null) {
		                                replacement=dataFactory.getOWLClass(IRI.create("internal:all#"+(firstReplacementIndex++)));
		                                if (objectAll.getFiller() instanceof OWLObjectComplementOf || objectAll.getFiller().equals(dataFactory.getOWLNothing()))
		                                    replacement=replacement.getComplementNNF();
		                                replacedDescriptions.put(objectAll,replacement);
		                            }
		                            inclusion[index]=replacement;
		                        }
		                    }
		                }
		            }
		        }
	        }
	        for (OWLClassExpression[] inclusion : ((OWLAxioms_withMaps) axioms).m_auxiliaryConceptInclusions) {
	            for (int index=0;index<inclusion.length;index++) {
	                OWLClassExpression classExpression=inclusion[index];
	                if (classExpression instanceof OWLObjectCardinalityRestriction) {
	                    OWLObjectCardinalityRestriction objectCardinalityRestriction=(OWLObjectCardinalityRestriction)classExpression;
	                    OWLObjectPropertyExpression objectPropertyExpression=objectCardinalityRestriction.getProperty();
	                    if (axioms.m_complexObjectPropertyExpressions.contains(objectPropertyExpression))
	                        throw new IllegalArgumentException("Non-simple property '"+objectPropertyExpression+"' or its inverse appears in the cardinality restriction '"+objectCardinalityRestriction+"'.");
	                }
	                else if (classExpression instanceof OWLObjectHasSelf) {
	                	OWLObjectHasSelf objectSelfRestriction=(OWLObjectHasSelf)classExpression;
	                	if (axioms.m_complexObjectPropertyExpressions.contains(objectSelfRestriction.getProperty()))
	                        throw new IllegalArgumentException("Non-simple property '"+objectSelfRestriction.getProperty()+"' or its inverse appears in the Self restriction '"+objectSelfRestriction+"'.");
	                }
	                if (classExpression instanceof OWLObjectAllValuesFrom) {
	                    OWLObjectAllValuesFrom objectAll=(OWLObjectAllValuesFrom)classExpression;
	                    if (!objectAll.getFiller().equals(dataFactory.getOWLThing())) {
	                        OWLObjectPropertyExpression objectProperty=objectAll.getProperty();
	                        if (m_automataByProperty.containsKey(objectProperty)) {
	                            OWLClassExpression replacement=replacedDescriptions.get(objectAll);
	                            if (replacement==null) {
	                                replacement=dataFactory.getOWLClass(IRI.create("internal:all#"+(firstReplacementIndex++)));
	                                if (objectAll.getFiller() instanceof OWLObjectComplementOf || objectAll.getFiller().equals(dataFactory.getOWLNothing()))
	                                    replacement=replacement.getComplementNNF();
	                                replacedDescriptions.put(objectAll,replacement);
	                            }
	                            inclusion[index]=replacement;
	                        }
	                    }
	                }
	            }
	        }
	        // Generate the automaton for each replacement
	        for (Map.Entry<OWLObjectAllValuesFrom,OWLClassExpression> replacement : replacedDescriptions.entrySet()) {
	            Automaton automaton=m_automataByProperty.get(replacement.getKey().getProperty());
	            boolean isOfNegativePolarity=(replacement.getValue() instanceof OWLObjectComplementOf);
	            // Generate states of the automaton
	            Map<State,OWLClassExpression> statesToConcepts=new HashMap<State,OWLClassExpression>();
	            for (Object stateObject : automaton.states()) {
	                State state=(State)stateObject;
	                if (state.isInitial())
	                    statesToConcepts.put(state,replacement.getValue());
	                else {
	                    OWLClassExpression stateConcept=dataFactory.getOWLClass(IRI.create("internal:all#"+(firstReplacementIndex++)));
	                    if (isOfNegativePolarity)
	                        stateConcept=stateConcept.getComplementNNF();
	                    statesToConcepts.put(state,stateConcept);
	                }
	            }
	            // Generate the transitions
	            for (Object transitionObject : automaton.delta()) {
	                Transition transition=(Transition)transitionObject;
	                OWLClassExpression fromStateConcept=statesToConcepts.get(transition.start()).getComplementNNF();
	                OWLClassExpression toStateConcept=statesToConcepts.get(transition.end());
	                if (transition.label()==null)
	                    ((OWLAxioms_withMaps) axioms).m_auxiliaryConceptInclusions.add(new OWLClassExpression[] { fromStateConcept,toStateConcept });
	                else {
	                    OWLObjectAllValuesFrom consequentAll=dataFactory.getOWLObjectAllValuesFrom((OWLObjectPropertyExpression)transition.label(),toStateConcept);
	                    ((OWLAxioms_withMaps) axioms).m_auxiliaryConceptInclusions.add(new OWLClassExpression[] { fromStateConcept,consequentAll });
	                }
	            }
	            // Generate the final states
	            OWLClassExpression filler=replacement.getKey().getFiller();
	            for (Object finalStateObject : automaton.terminals()) {
	                OWLClassExpression finalStateConceptComplement=statesToConcepts.get(finalStateObject).getComplementNNF();
	                if (filler.isOWLNothing())
	                	((OWLAxioms_withMaps) axioms).m_auxiliaryConceptInclusions.add(new OWLClassExpression[] { finalStateConceptComplement });
	                else
	                	((OWLAxioms_withMaps) axioms).m_auxiliaryConceptInclusions.add(new OWLClassExpression[] { finalStateConceptComplement,filler });
	            }
	        }
	    } //DONE
	    protected void createAutomata(
	    		Map<OWLObjectPropertyExpression,Automaton> automataByProperty,
	    		Set<OWLObjectPropertyExpression> complexObjectPropertyExpressions,
	    		Collection<OWLObjectPropertyExpression[]> simpleObjectPropertyInclusions,
	    		Collection<ComplexObjectPropertyInclusion> complexObjectPropertyInclusions) {} //called from the constructor of the superclass but I don't want it to do anything
	    //extended next with slightly different types of arguments
	    protected void createAutomata(
	    		Map<OWLObjectPropertyExpression,Automaton> automataByProperty,
	    		Set<OWLObjectPropertyExpression> complexObjectPropertyExpressions,
	    		Map<OWLAxiom,Collection<OWLObjectPropertyExpression[]>> simpleObjectPropertyInclusions_map,
	    		Collection<ComplexObjectPropertyInclusion> complexObjectPropertyInclusions,
	    		Collection<OWLObjectProperty> symmetricObjectProperties_atomic) {
	        Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> equivalentPropertiesMap=findEquivalentProperties(simpleObjectPropertyInclusions_map);
	        Set<OWLObjectPropertyExpression> symmetricObjectProperties=findAllSymmetricPropertyExpressions(symmetricObjectProperties_atomic);
	        Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> inversePropertiesMap=buildInversePropertiesMap(simpleObjectPropertyInclusions_map,symmetricObjectProperties_atomic);
	        Graph<OWLObjectPropertyExpression> propertyDependencyGraph=buildPropertyOrdering(simpleObjectPropertyInclusions_map,complexObjectPropertyInclusions,equivalentPropertiesMap);
	        checkForRegularity(propertyDependencyGraph,equivalentPropertiesMap);

	        Graph<OWLObjectPropertyExpression> complexPropertiesDependencyGraph=propertyDependencyGraph.clone();
	        Set<OWLObjectPropertyExpression> transitiveProperties=new HashSet<OWLObjectPropertyExpression>();
	        Map<OWLObjectPropertyExpression,Automaton> individualAutomata=buildIndividualAutomata(complexPropertiesDependencyGraph,null,complexObjectPropertyInclusions,equivalentPropertiesMap,transitiveProperties);
	        //this method in superclass actually doesn't use the simpleObjectPropertyInclusions so we can reuse it
	        Set<OWLObjectPropertyExpression> simpleProperties=findSimpleProperties(complexPropertiesDependencyGraph,individualAutomata);
	        propertyDependencyGraph.removeElements(simpleProperties);
	        complexPropertiesDependencyGraph.removeElements(simpleProperties);
	        complexObjectPropertyExpressions.addAll(complexPropertiesDependencyGraph.getElements());
	        Set<OWLObjectPropertyExpression> inverseOfComplexProperties = new HashSet<OWLObjectPropertyExpression>();
	        for( OWLObjectPropertyExpression complexProp : complexObjectPropertyExpressions )
	        	inverseOfComplexProperties.add( complexProp.getInverseProperty().getSimplified() );
	        complexObjectPropertyExpressions.addAll(inverseOfComplexProperties);
	        connectAllAutomata(automataByProperty,propertyDependencyGraph,inversePropertiesMap,individualAutomata,null,symmetricObjectProperties,transitiveProperties);
	        //this method in superclass actually doesn't use the simpleObjectPropertyInclusions so we can reuse it
	        Map<OWLObjectPropertyExpression,Automaton> individualAutomataForEquivRoles=new HashMap<OWLObjectPropertyExpression,Automaton>();
	        for (OWLObjectPropertyExpression propExprWithAutomaton : automataByProperty.keySet())
	        	if (equivalentPropertiesMap.get(propExprWithAutomaton)!=null) {
	        		Automaton autoOfPropExpr = automataByProperty.get(propExprWithAutomaton);
		        	for (OWLObjectPropertyExpression equivProp : equivalentPropertiesMap.get(propExprWithAutomaton)) {
		        		if (!equivProp.equals(propExprWithAutomaton) && !automataByProperty.containsKey(equivProp)) {
		        			Automaton automatonOfEquivalent=(Automaton)autoOfPropExpr.clone();
							individualAutomataForEquivRoles.put(equivProp, automatonOfEquivalent);
							simpleProperties.remove(equivProp);
					        complexObjectPropertyExpressions.add(equivProp);
		        		}
		        		OWLObjectPropertyExpression inverseEquivProp = equivProp.getInverseProperty().getSimplified();
		        		if (!inverseEquivProp.equals(propExprWithAutomaton) && !automataByProperty.containsKey(inverseEquivProp)) {
		        			Automaton automatonOfEquivalent=(Automaton)autoOfPropExpr.clone();
							individualAutomataForEquivRoles.put(inverseEquivProp, getMirroredCopy(automatonOfEquivalent));
							simpleProperties.remove(inverseEquivProp);
					        complexObjectPropertyExpressions.add(inverseEquivProp);
		        		}
		        	}
	        	}
	        automataByProperty.putAll(individualAutomataForEquivRoles);
	    }//DONE
	    private Set<OWLObjectPropertyExpression> findAllSymmetricPropertyExpressions(Collection<OWLObjectProperty> symmetricProperties_atomic) {
	    	Set<OWLObjectPropertyExpression> symmetricPropertyExpressions = new HashSet<OWLObjectPropertyExpression>(symmetricProperties_atomic);
	    	for (OWLObjectProperty p : symmetricProperties_atomic)
	    		symmetricPropertyExpressions.add(p.getInverseProperty());
			return symmetricPropertyExpressions;
		}//DONE
		protected Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> buildInversePropertiesMap(
				Map<OWLAxiom,Collection<OWLObjectPropertyExpression[]>> simpleObjectPropertyInclusions,
				Collection<OWLObjectProperty> symmetricObjectProperties) {
	        Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> inversePropertiesMap=new HashMap<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>>();
	        for (Entry<OWLAxiom,Collection<OWLObjectPropertyExpression[]>> entry : simpleObjectPropertyInclusions.entrySet())
	        	for (OWLObjectPropertyExpression[] inclusion : entry.getValue())
	        		if (inclusion[1] instanceof OWLObjectInverseOf) {
	        			Set<OWLObjectPropertyExpression> inverseProperties=inversePropertiesMap.get(inclusion[0]);
	        			if (inverseProperties==null)
	        				inverseProperties=new HashSet<OWLObjectPropertyExpression>();
	        			inverseProperties.add(inclusion[1].getInverseProperty().getSimplified());
	        			inversePropertiesMap.put(inclusion[0],inverseProperties);
	        		}
	        
	        //symmetric object properties used to be reflected as simple object property inclusions, but not anymore, so need to deal with them explicitly
	        for (OWLObjectProperty p : symmetricObjectProperties) {
	        	Set<OWLObjectPropertyExpression> inverseProperties=inversePropertiesMap.get(p);
	        	if (inverseProperties==null)
	        		inverseProperties=new HashSet<OWLObjectPropertyExpression>();
	        	inverseProperties.add(p);
	        	inversePropertiesMap.put(p,inverseProperties);
	        }
	        
	        return inversePropertiesMap;
	    }//DONE
	    protected Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> findEquivalentProperties(Map<OWLAxiom,Collection<OWLObjectPropertyExpression[]>> simpleObjectPropertyInclusions_map) {
	        Graph<OWLObjectPropertyExpression> propertyDependencyGraph=new Graph<OWLObjectPropertyExpression>();
	        Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> equivalentObjectPropertiesMapping=new HashMap<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>>();
	        for (Entry<OWLAxiom,Collection<OWLObjectPropertyExpression[]>> entry : simpleObjectPropertyInclusions_map.entrySet())
	        	for (OWLObjectPropertyExpression[] inclusion : entry.getValue())
	        		if (!inclusion[0].equals(inclusion[1]) && !inclusion[0].equals(inclusion[1].getInverseProperty().getSimplified()))
	        			propertyDependencyGraph.addEdge(inclusion[0],inclusion[1]);
	        propertyDependencyGraph.transitivelyClose();
	        for (OWLObjectPropertyExpression objExpr : propertyDependencyGraph.getElements()) {
	            if (propertyDependencyGraph.getSuccessors(objExpr).contains(objExpr) || propertyDependencyGraph.getSuccessors(objExpr).contains(objExpr.getInverseProperty().getSimplified())) {
	                Set<OWLObjectPropertyExpression> equivPropertiesSet=new HashSet<OWLObjectPropertyExpression>();
	                for (OWLObjectPropertyExpression succ : propertyDependencyGraph.getSuccessors(objExpr)) {
	                    if (!succ.equals(objExpr) && (propertyDependencyGraph.getSuccessors(succ).contains(objExpr) || propertyDependencyGraph.getSuccessors(succ).contains(objExpr.getInverseProperty().getSimplified())))
	                        equivPropertiesSet.add(succ);
	                }
	                equivalentObjectPropertiesMapping.put(objExpr,equivPropertiesSet);
	            }
	        }
	        return equivalentObjectPropertiesMapping;
	    } //DONE
	    protected Graph<OWLObjectPropertyExpression> buildPropertyOrdering(Map<OWLAxiom,Collection<OWLObjectPropertyExpression[]>> simpleObjectPropertyInclusions_map,Collection<ComplexObjectPropertyInclusion> complexObjectPropertyInclusions,Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> equivalentPropertiesMap) {
	        Graph<OWLObjectPropertyExpression> propertyDependencyGraph=new Graph<OWLObjectPropertyExpression>();
	        for (Entry<OWLAxiom,Collection<OWLObjectPropertyExpression[]>> entry : simpleObjectPropertyInclusions_map.entrySet())
	        	for (OWLObjectPropertyExpression[] inclusion : entry.getValue())
	        		//not necessary to take into account symmetric properties - see second condition in if right below
	        		if (!inclusion[0].equals(inclusion[1]) && !inclusion[0].equals(inclusion[1].getInverseProperty().getSimplified()) && (equivalentPropertiesMap.get(inclusion[0])==null || !equivalentPropertiesMap.get(inclusion[0]).contains(inclusion[1])))
	        			propertyDependencyGraph.addEdge(inclusion[0],inclusion[1]);
	        for (OWLAxioms.ComplexObjectPropertyInclusion inclusion : complexObjectPropertyInclusions) {
	            OWLObjectPropertyExpression owlSuperProperty=inclusion.m_superObjectProperty;
	            OWLObjectPropertyExpression owlSubPropertyInChain=null;
	            OWLObjectPropertyExpression[] owlSubProperties=inclusion.m_subObjectProperties;
	            if (owlSubProperties.length!=2 && owlSuperProperty.equals(owlSubProperties[0]) && owlSuperProperty.equals(owlSubProperties[owlSubProperties.length-1]))
	                throw new IllegalArgumentException("The given property hierarchy is not regular.");

	            for (int i=0;i<owlSubProperties.length;i++) {
	                owlSubPropertyInChain=owlSubProperties[i];

	                if (owlSubProperties.length!=2 && i>0 && i<owlSubProperties.length-1 && (owlSubPropertyInChain.equals(owlSuperProperty) || (equivalentPropertiesMap.containsKey(owlSuperProperty) && equivalentPropertiesMap.get(owlSuperProperty).contains(owlSubPropertyInChain))))
	                    throw new IllegalArgumentException("The given property hierarchy is not regular.");
	                else if (owlSubPropertyInChain.getInverseProperty().getSimplified().equals(owlSuperProperty))
	                    throw new IllegalArgumentException("The given property hierarchy is not regular.");
	                else if (!owlSubPropertyInChain.equals(owlSuperProperty))
	                    propertyDependencyGraph.addEdge(owlSubPropertyInChain,owlSuperProperty);
	            }
	        }
	        return propertyDependencyGraph;
	    } //DONE
	    
	}

