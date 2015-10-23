package uk.ac.ox.cs.prism.clausification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.HermiT.structural.OWLAxioms;
import org.semanticweb.HermiT.structural.OWLAxioms.DisjunctiveRule;
import org.semanticweb.HermiT.structural.OWLNormalization;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;


public class OWLNormalization_withMaps extends OWLNormalization{

//    protected final OWLAxioms_withMaps m_axioms_withMap;

    public OWLNormalization_withMaps(OWLDataFactory factory,OWLAxioms_withMaps axioms,int firstReplacementIndex) {
        super(factory,axioms,firstReplacementIndex);
    }
    
    public Map<OWLClassExpression,OWLClassExpression> getDefinitions() {
    	return m_definitions;
    }
    
    public void processAxioms(Collection<? extends OWLAxiom> axioms) {
        AxiomVisitor_withMaps axiomVisitor=new AxiomVisitor_withMaps();
        for (OWLAxiom axiom : axioms)
            axiom.accept(axiomVisitor);
        List<OWLDataRange[]> auxDataRangeInclusions = new ArrayList<OWLDataRange[]>();
        RuleNormalizer_withMaps ruleNormalizer=new RuleNormalizer_withMaps(((OWLAxioms_withMaps) m_axioms).m_rules_map,axiomVisitor.m_classExpressionInclusionsAsDisjunctions_map,axiomVisitor.m_auxiliaryClassExpressionInclusions,axiomVisitor.m_dataRangeInclusionsAsDisjunctions_map, auxDataRangeInclusions);
        for (SWRLRule rule : axiomVisitor.getSWRLRules())
            ruleNormalizer.visit(rule);
        normalizeInclusions(((AxiomVisitor_withMaps) axiomVisitor).m_classExpressionInclusionsAsDisjunctions_map,((AxiomVisitor_withMaps) axiomVisitor).m_auxiliaryClassExpressionInclusions,((AxiomVisitor_withMaps) axiomVisitor).m_dataRangeInclusionsAsDisjunctions_map, auxDataRangeInclusions);
    }
    protected void addFact(OWLIndividualAxiom axiom) {
    	Collection<OWLIndividualAxiom> aux = new HashSet<OWLIndividualAxiom>();
    	aux.add(axiom);
    	((OWLAxioms_withMaps) m_axioms).m_facts_map.put(axiom, aux);
    }//done
    protected void addFact(OWLIndividualAxiom axiom, OWLAxiom originalAxiom) {
    	Collection<OWLIndividualAxiom> aux = new HashSet<OWLIndividualAxiom>();
    	aux.add(axiom);
        ((OWLAxioms_withMaps) m_axioms).m_facts_map.put(originalAxiom, aux);
    }
    protected void addFacts(Collection<OWLIndividualAxiom> axioms, OWLAxiom originalAxiom) {
    	Collection<OWLIndividualAxiom> previous = ((OWLAxioms_withMaps) m_axioms).m_facts_map.get(originalAxiom);
    	if (previous != null)
    		previous.addAll(axioms);
    	else
    		((OWLAxioms_withMaps) m_axioms).m_facts_map.put(originalAxiom,axioms);
    }
    protected void addObjectPropertyInclusionsToMap(OWLAxiom axiom, Collection<OWLObjectPropertyExpression[]> inclusions) {
        ((OWLAxioms_withMaps) m_axioms).m_simpleObjectPropertyInclusions_map.put(axiom, inclusions);
    }
    protected void addDataPropertyInclusionsToMap(OWLAxiom axiom, Collection<OWLDataPropertyExpression[]> inclusions) {
        ((OWLAxioms_withMaps) m_axioms).m_dataPropertyInclusions_map.put(axiom, inclusions);
    }
    protected void addSymmetricObjectProperty(OWLObjectProperty p){
    	((OWLAxioms_withMaps) m_axioms).m_symmetricObjectProperties.add(p);
    }
    protected void normalizeInclusions(Map<OWLAxiom,List<OWLClassExpression[]>> inclusions_map,List<OWLClassExpression[]> auxInclusions,Map<OWLAxiom,List<OWLDataRange[]>> dataRangeInclusions_map, List<OWLDataRange[]> auxDataRangeInclusions) {
    	ClassExpressionNormalizer classExpressionNormalizer=new ClassExpressionNormalizer(auxInclusions, auxDataRangeInclusions);
        // normalize all class expression inclusions
        
        for (Entry<OWLAxiom,List<OWLClassExpression[]>> entry : inclusions_map.entrySet()){
        	Collection<OWLClassExpression[]> aux = normalizeClassExpressionInclusions(entry.getKey(),entry.getValue(),classExpressionNormalizer);
        	((OWLAxioms_withMaps) m_axioms).m_conceptInclusions_map.put(entry.getKey(),aux);
        }
        inclusions_map.clear();
        Collection<OWLClassExpression[]> aux = normalizeClassExpressionInclusions(null,auxInclusions,classExpressionNormalizer);
    	((OWLAxioms_withMaps) m_axioms).m_auxiliaryConceptInclusions.addAll(aux);
        
        // normalize data range inclusions
    	DataRangeNormalizer dataRangeNormalizer=new DataRangeNormalizer(auxDataRangeInclusions);
        for (Entry<OWLAxiom,List<OWLDataRange[]>> entry : dataRangeInclusions_map.entrySet()){
        	Collection<OWLDataRange[]> aux2 = normalizeDataRangeInclusions(entry.getValue(),dataRangeNormalizer);
        	((OWLAxioms_withMaps) m_axioms).m_dataRangeInclusions_map.put(entry.getKey(),aux2);
        }
        dataRangeInclusions_map.clear();
        Collection<OWLDataRange[]> aux2 = normalizeDataRangeInclusions(auxDataRangeInclusions,dataRangeNormalizer);
    	((OWLAxioms_withMaps) m_axioms).m_auxiliaryDataRangeInclusions.addAll(aux2);
    }
    private Collection<OWLDataRange[]> normalizeDataRangeInclusions(
			List<OWLDataRange[]> dataRangeInclusions, 
			DataRangeNormalizer dataRangeNormalizer) {
    	Collection<OWLDataRange[]> aux = new HashSet<OWLDataRange[]>();
    	while (!dataRangeInclusions.isEmpty()) {
            OWLDataRange simplifiedDescription=m_expressionManager.getNNF(m_expressionManager.getSimplified(m_factory.getOWLDataUnionOf(dataRangeInclusions.remove(dataRangeInclusions.size()-1))));
            if (!simplifiedDescription.isTopDatatype()) {
                if (simplifiedDescription instanceof OWLDataUnionOf) {
                    OWLDataUnionOf dataOr=(OWLDataUnionOf)simplifiedDescription;
                    OWLDataRange[] descriptions=new OWLDataRange[dataOr.getOperands().size()];
                    dataOr.getOperands().toArray(descriptions);
                    if (!distributeUnionOverAnd(descriptions,dataRangeInclusions)) {
                        for (int index=0;index<descriptions.length;index++)
                            descriptions[index]=descriptions[index].accept(dataRangeNormalizer);
                        aux.add(descriptions);
                    }
                }
                else if (simplifiedDescription instanceof OWLDataIntersectionOf) {
                    OWLDataIntersectionOf dataAnd=(OWLDataIntersectionOf)simplifiedDescription;
                    for (OWLDataRange conjunct : dataAnd.getOperands())
                        dataRangeInclusions.add(new OWLDataRange[] { conjunct });
                }
                else {
                    OWLDataRange normalized=simplifiedDescription.accept(dataRangeNormalizer);
                    dataRangeInclusions.add(new OWLDataRange[] { normalized });
                }
            }
        }
		return aux;
	}

	private Collection<OWLClassExpression[]> normalizeClassExpressionInclusions(
			OWLAxiom axiom,
			List<OWLClassExpression[]> inclusions,
			ClassExpressionNormalizer classExpressionNormalizer) {
    	Collection<OWLClassExpression[]> aux = new HashSet<OWLClassExpression[]>();
    	while (!inclusions.isEmpty()) {
    		OWLClassExpression simplifiedDescription=m_expressionManager.getNNF(m_expressionManager.getSimplified(m_factory.getOWLObjectUnionOf(inclusions.remove(inclusions.size()-1))));
            if (!simplifiedDescription.isOWLThing()) {
                if (simplifiedDescription instanceof OWLObjectUnionOf) {
                    OWLObjectUnionOf objectOr=(OWLObjectUnionOf)simplifiedDescription;
                    OWLClassExpression[] descriptions=new OWLClassExpression[objectOr.getOperands().size()];
                    objectOr.getOperands().toArray(descriptions);
                    if (!distributeUnionOverAnd(descriptions,inclusions) && !optimizedNegativeOneOfTranslation(descriptions,((OWLAxioms_withMaps) m_axioms).m_facts_map, axiom)) {
                        for (int index=0;index<descriptions.length;index++)
                            descriptions[index]=descriptions[index].accept(classExpressionNormalizer);
                        aux.add(descriptions);
                    }
                }
                else if (simplifiedDescription instanceof OWLObjectIntersectionOf) {
                    OWLObjectIntersectionOf objectAnd=(OWLObjectIntersectionOf)simplifiedDescription;
                    for (OWLClassExpression conjunct : objectAnd.getOperands())
                        inclusions.add(new OWLClassExpression[] { conjunct });
                }
                else {
                    OWLClassExpression normalized=simplifiedDescription.accept(classExpressionNormalizer);
                    aux.add(new OWLClassExpression[] { normalized });
                }
            }	
    	}
		return aux;
	}

	protected boolean optimizedNegativeOneOfTranslation(OWLClassExpression[] descriptions,Map<OWLAxiom,Collection<OWLIndividualAxiom>> facts_map, OWLAxiom originalAxiom) {
        if (descriptions.length==2) {
            OWLObjectOneOf nominal=null;
            OWLClassExpression other=null;
            if (descriptions[0] instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)descriptions[0]).getOperand() instanceof OWLObjectOneOf) {
                nominal=(OWLObjectOneOf)((OWLObjectComplementOf)descriptions[0]).getOperand();
                other=descriptions[1];
            }
            else if (descriptions[1] instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)descriptions[1]).getOperand() instanceof OWLObjectOneOf) {
                other=descriptions[0];
                nominal=(OWLObjectOneOf)((OWLObjectComplementOf)descriptions[1]).getOperand();
            }
            if (nominal!=null && (other instanceof OWLClass || (other instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)other).getOperand() instanceof OWLClass))) {
            	Collection<OWLIndividualAxiom> aux = new HashSet<OWLIndividualAxiom>(); 
                for (OWLIndividual individual : nominal.getIndividuals())
                    aux.add(m_factory.getOWLClassAssertionAxiom(other,individual));
                addFacts(aux,originalAxiom);
                return true;
            }
        }
        return false;
    }
    
    protected class AxiomVisitor_withMaps extends AxiomVisitor {
        protected final Map<OWLAxiom,List<OWLClassExpression[]>> m_classExpressionInclusionsAsDisjunctions_map;
        protected final List<OWLClassExpression[]> m_auxiliaryClassExpressionInclusions;
        protected final Map<OWLAxiom,List<OWLDataRange[]>> m_dataRangeInclusionsAsDisjunctions_map;
//        protected final Collection<SWRLRule> m_rules;
//        protected final boolean[] m_alreadyExists;

        public AxiomVisitor_withMaps() {
        	super();
            m_classExpressionInclusionsAsDisjunctions_map=new HashMap<OWLAxiom,List<OWLClassExpression[]>>();
            m_auxiliaryClassExpressionInclusions=new ArrayList<OWLClassExpression[]>();
            m_dataRangeInclusionsAsDisjunctions_map = new HashMap<OWLAxiom, List<OWLDataRange[]>>();
        }
        
        public Collection<SWRLRule> getSWRLRules(){
        	return m_rules;
        }
        // Class axioms

        public void visit(OWLSubClassOfAxiom axiom) {
        	List<OWLClassExpression[]> aux = new ArrayList<OWLClassExpression[]>();
        	aux.add(new OWLClassExpression[] { negative(axiom.getSubClass()),positive(axiom.getSuperClass()) });
            m_classExpressionInclusionsAsDisjunctions_map.put(axiom,aux);
        }
        public void visit(OWLEquivalentClassesAxiom axiom) {
            if (axiom.getClassExpressions().size()>1) {
            	List<OWLClassExpression[]> aux = new ArrayList<OWLClassExpression[]>();
                Iterator<OWLClassExpression> iterator=axiom.getClassExpressions().iterator();
                OWLClassExpression first=iterator.next();
                OWLClassExpression last=first;
                while (iterator.hasNext()) {
                    OWLClassExpression next=iterator.next();
                    aux.add(new OWLClassExpression[] { negative(last),positive(next) });
                    last=next;
                }
                aux.add(new OWLClassExpression[] { negative(last),positive(first) });
                m_classExpressionInclusionsAsDisjunctions_map.put(axiom,aux);
            }
        }
        public void visit(OWLDisjointClassesAxiom axiom) {
            if (axiom.getClassExpressions().size()<=1) {
                throw new IllegalArgumentException("Error: Parsed "+axiom.toString()+". A DisjointClasses axiom in OWL 2 DL must have at least two classes as parameters. ");
            }
            List<OWLClassExpression[]> aux = new ArrayList<OWLClassExpression[]>();
            OWLClassExpression[] descriptions=new OWLClassExpression[axiom.getClassExpressions().size()];
            axiom.getClassExpressions().toArray(descriptions);
            for (int i=0;i<descriptions.length;i++)
                descriptions[i]=m_expressionManager.getComplementNNF(descriptions[i]);
            for (int i=0;i<descriptions.length;i++)
                for (int j=i+1;j<descriptions.length;j++)
                	aux.add(new OWLClassExpression[] { descriptions[i],descriptions[j] });
            m_classExpressionInclusionsAsDisjunctions_map.put(axiom,aux);
        }
        public void visit(OWLDisjointUnionAxiom axiom) {
        	List<OWLClassExpression[]> aux = new ArrayList<OWLClassExpression[]>();
            // DisjointUnion(C CE1 ... CEn)
            // 1. add C implies CE1 or ... or CEn, which is { not C or CE1 or ... or CEn }
            Set<OWLClassExpression> inclusion=new HashSet<OWLClassExpression>(axiom.getClassExpressions());
            inclusion.add(m_expressionManager.getComplementNNF(axiom.getOWLClass()));
            OWLClassExpression[] inclusionArray=new OWLClassExpression[axiom.getClassExpressions().size()+1];
            inclusion.toArray(inclusionArray);
            aux.add(inclusionArray);
            // 2. add CEi implies C, which is { not CEi or C }
            for (OWLClassExpression description : axiom.getClassExpressions())
                aux.add(new OWLClassExpression[] { negative(description),axiom.getOWLClass() });
            // 3. add CEi and CEj implies bottom (not CEi or not CEj) for 1 <= i < j <= n
            OWLClassExpression[] descriptions=new OWLClassExpression[axiom.getClassExpressions().size()];
            axiom.getClassExpressions().toArray(descriptions);
            for (int i=0;i<descriptions.length;i++)
                descriptions[i]=m_expressionManager.getComplementNNF(descriptions[i]);
            for (int i=0;i<descriptions.length;i++)
                for (int j=i+1;j<descriptions.length;j++)
                    aux.add(new OWLClassExpression[] { descriptions[i],descriptions[j] });
            m_classExpressionInclusionsAsDisjunctions_map.put(axiom,aux);
        }

        // Object property axioms

        public void visit(OWLSubObjectPropertyOfAxiom axiom) {
            if (!axiom.getSubProperty().isOWLBottomObjectProperty() && !axiom.getSuperProperty().isOWLTopObjectProperty()){
            	Collection<OWLObjectPropertyExpression[]> aux = new HashSet<OWLObjectPropertyExpression[]>();
            	aux.add(new OWLObjectPropertyExpression[]{axiom.getSubProperty().getSimplified(),axiom.getSuperProperty().getSimplified()});
            	addObjectPropertyInclusionsToMap(axiom, aux);
            }
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getSubProperty().getNamedProperty());
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getSuperProperty().getNamedProperty());
        }
        public void visit(OWLSubPropertyChainOfAxiom axiom) {
            List<OWLObjectPropertyExpression> subPropertyChain=axiom.getPropertyChain();
            if (!containsBottomObjectProperty(subPropertyChain) && !axiom.getSuperProperty().isOWLTopObjectProperty()) {
                OWLObjectPropertyExpression superObjectPropertyExpression=axiom.getSuperProperty();
                if (subPropertyChain.size()==1){
                	Collection<OWLObjectPropertyExpression[]> aux = new HashSet<OWLObjectPropertyExpression[]>();
                	aux.add(new OWLObjectPropertyExpression[]{subPropertyChain.get(0).getSimplified(),superObjectPropertyExpression.getSimplified()});
                	addObjectPropertyInclusionsToMap(axiom, aux);
                }
                else if (subPropertyChain.size()==2 && subPropertyChain.get(0).equals(superObjectPropertyExpression) && subPropertyChain.get(1).equals(superObjectPropertyExpression))
                    makeTransitive(axiom.getSuperProperty());
                else if (subPropertyChain.size()==0)
                    throw new IllegalArgumentException("Error: In OWL 2 DL, an empty property chain in property chain axioms is not allowd, but the ontology contains an axiom that the empty chain is a subproperty of "+superObjectPropertyExpression+".");
                else {
                    OWLObjectPropertyExpression[] subObjectProperties=new OWLObjectPropertyExpression[subPropertyChain.size()];
                    subPropertyChain.toArray(subObjectProperties);
                    addInclusion(subObjectProperties,superObjectPropertyExpression);
                }
            }
            for (OWLObjectPropertyExpression objectPropertyExpression : subPropertyChain)
                m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(objectPropertyExpression.getNamedProperty());
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getSuperProperty().getNamedProperty());
        }
        public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
            Set<OWLObjectPropertyExpression> objectPropertyExpressions=axiom.getProperties();
            Collection<OWLObjectPropertyExpression[]> aux = new HashSet<OWLObjectPropertyExpression[]>();
            if (objectPropertyExpressions.size()>1) {
                Iterator<OWLObjectPropertyExpression> iterator=objectPropertyExpressions.iterator();
                OWLObjectPropertyExpression first=iterator.next();
                OWLObjectPropertyExpression last=first;
                while (iterator.hasNext()) {
                    OWLObjectPropertyExpression next=iterator.next();
                    aux.add(new OWLObjectPropertyExpression[]{last.getSimplified(),next.getSimplified()});
                    last=next;
                }
                aux.add(new OWLObjectPropertyExpression[]{last.getSimplified(),first.getSimplified()});
            }
            addObjectPropertyInclusionsToMap(axiom, aux);
            for (OWLObjectPropertyExpression objectPropertyExpression : objectPropertyExpressions)
                m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(objectPropertyExpression.getNamedProperty());
        }
        public void visit(OWLInverseObjectPropertiesAxiom axiom) {
            OWLObjectPropertyExpression first=axiom.getFirstProperty();
            OWLObjectPropertyExpression second=axiom.getSecondProperty();
            Collection<OWLObjectPropertyExpression[]> aux = new HashSet<OWLObjectPropertyExpression[]>();
            aux.add(new OWLObjectPropertyExpression[]{first.getSimplified(),second.getInverseProperty().getSimplified()});
            aux.add(new OWLObjectPropertyExpression[]{second.getSimplified(),first.getInverseProperty().getSimplified()});
            addObjectPropertyInclusionsToMap(axiom, aux);
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(first.getNamedProperty());
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(second.getNamedProperty());
        }
        public void visit(OWLObjectPropertyDomainAxiom axiom) {
            OWLObjectAllValuesFrom allPropertyNohting=m_factory.getOWLObjectAllValuesFrom(axiom.getProperty().getSimplified(),m_factory.getOWLNothing());
            List<OWLClassExpression[]> aux = new ArrayList<OWLClassExpression[]>();
            aux.add(new OWLClassExpression[] { positive(axiom.getDomain()),allPropertyNohting });
            m_classExpressionInclusionsAsDisjunctions_map.put(axiom,aux);
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getProperty().getNamedProperty());
        }
        public void visit(OWLObjectPropertyRangeAxiom axiom) {
            OWLObjectAllValuesFrom allPropertyRange=m_factory.getOWLObjectAllValuesFrom(axiom.getProperty().getSimplified(),positive(axiom.getRange()));
            List<OWLClassExpression[]> aux = new ArrayList<OWLClassExpression[]>();
            aux.add(new OWLClassExpression[] { allPropertyRange });
            m_classExpressionInclusionsAsDisjunctions_map.put(axiom, aux);
        }
        public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        	List<OWLClassExpression[]> aux = new ArrayList<OWLClassExpression[]>();
            aux.add(new OWLClassExpression[] { m_factory.getOWLObjectMaxCardinality(1,axiom.getProperty().getSimplified()) });
            m_classExpressionInclusionsAsDisjunctions_map.put(axiom, aux);
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getProperty().getNamedProperty());
        }
        public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        	List<OWLClassExpression[]> aux = new ArrayList<OWLClassExpression[]>();
            aux.add(new OWLClassExpression[] { m_factory.getOWLObjectMaxCardinality(1,axiom.getProperty().getSimplified().getInverseProperty()) });
            m_classExpressionInclusionsAsDisjunctions_map.put(axiom,  aux);
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getProperty().getNamedProperty());
        }
        public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
            OWLObjectPropertyExpression objectProperty=axiom.getProperty();
            addSymmetricObjectProperty(objectProperty.getNamedProperty());
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getProperty().getNamedProperty());
        }

        // Data property axioms
        
        public void visit(OWLSubDataPropertyOfAxiom axiom) {
            OWLDataPropertyExpression subDataProperty=axiom.getSubProperty();
            checkTopDataPropertyUse(subDataProperty,axiom);
            OWLDataPropertyExpression superDataProperty=axiom.getSuperProperty();
            if (!subDataProperty.isOWLBottomDataProperty() && !superDataProperty.isOWLTopDataProperty()){
            	Collection<OWLDataPropertyExpression[]> aux = new HashSet<OWLDataPropertyExpression[]>();
            	aux.add(new OWLDataPropertyExpression[]{subDataProperty,superDataProperty});
                addDataPropertyInclusionsToMap(axiom, aux);
            }
        }
        public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
            for (OWLDataPropertyExpression dataPropertyExpression : axiom.getProperties())
                checkTopDataPropertyUse(dataPropertyExpression,axiom);
            if (axiom.getProperties().size()>1) {
            	Collection<OWLDataPropertyExpression[]> aux = new HashSet<OWLDataPropertyExpression[]>();
                Iterator<OWLDataPropertyExpression> iterator=axiom.getProperties().iterator();
                OWLDataPropertyExpression first=iterator.next();
                OWLDataPropertyExpression last=first;
                while (iterator.hasNext()) {
                    OWLDataPropertyExpression next=iterator.next();
                    aux.add(new OWLDataPropertyExpression[]{last,next});
                    last=next;
                }
                aux.add(new OWLDataPropertyExpression[]{last,first});
                addDataPropertyInclusionsToMap(axiom, aux);
            }
        }
        public void visit(OWLDataPropertyDomainAxiom axiom) {
        	List<OWLClassExpression[]> aux = new ArrayList<OWLClassExpression[]>();
            OWLDataPropertyExpression dataProperty=axiom.getProperty();
            checkTopDataPropertyUse(dataProperty,axiom);
            OWLDataRange dataNothing=m_factory.getOWLDataComplementOf(m_factory.getTopDatatype());
            OWLDataAllValuesFrom allPropertyDataNothing=m_factory.getOWLDataAllValuesFrom(dataProperty,dataNothing);
            aux.add(new OWLClassExpression[] { positive(axiom.getDomain()),allPropertyDataNothing });
            m_classExpressionInclusionsAsDisjunctions_map.put(axiom, aux);
        }
        public void visit(OWLDataPropertyRangeAxiom axiom) {
        	List<OWLClassExpression[]> aux = new ArrayList<OWLClassExpression[]>();
            OWLDataPropertyExpression dataProperty=axiom.getProperty();
            checkTopDataPropertyUse(dataProperty,axiom);
            OWLDataAllValuesFrom allPropertyRange=m_factory.getOWLDataAllValuesFrom(dataProperty,positive(axiom.getRange()));
            aux.add(new OWLClassExpression[] { allPropertyRange });
            m_classExpressionInclusionsAsDisjunctions_map.put(axiom, aux);
        }
        public void visit(OWLFunctionalDataPropertyAxiom axiom) {
        	List<OWLClassExpression[]> aux = new ArrayList<OWLClassExpression[]>();
            OWLDataPropertyExpression dataProperty=axiom.getProperty();
            checkTopDataPropertyUse(dataProperty,axiom);
            aux.add(new OWLClassExpression[] { m_factory.getOWLDataMaxCardinality(1,dataProperty) });
            m_classExpressionInclusionsAsDisjunctions_map.put(axiom, aux);
        }

        // Assertions

        public void visit(OWLClassAssertionAxiom axiom) {
            OWLClassExpression classExpression=axiom.getClassExpression();
            if (classExpression instanceof OWLDataHasValue) {
                OWLDataHasValue hasValue=(OWLDataHasValue)classExpression;
                addFact(m_factory.getOWLDataPropertyAssertionAxiom(hasValue.getProperty(), axiom.getIndividual(), hasValue.getValue()));
                return;
            }
            if (classExpression instanceof OWLDataSomeValuesFrom) {
                OWLDataSomeValuesFrom someValuesFrom=(OWLDataSomeValuesFrom)classExpression;
                OWLDataRange dataRange=someValuesFrom.getFiller();
                if (dataRange instanceof OWLDataOneOf) {
                    OWLDataOneOf oneOf=(OWLDataOneOf)dataRange;
                    if (oneOf.getValues().size()==1) {
                        addFact(m_factory.getOWLDataPropertyAssertionAxiom(someValuesFrom.getProperty(),axiom.getIndividual(),oneOf.getValues().iterator().next()));
                        return;
                    }
                }
            }
            classExpression=positive(classExpression);
            if (!isSimple(classExpression)) {
                OWLClassExpression definition=getDefinitionFor(classExpression,m_alreadyExists);
                if (!m_alreadyExists[0])
                	m_auxiliaryClassExpressionInclusions.add(new OWLClassExpression[] { negative(definition),classExpression });
                classExpression=definition;
            }
            addFact(axiom,m_factory.getOWLClassAssertionAxiom(classExpression,axiom.getIndividual()));
        }

        // Datatype definitions

        public void visit(OWLDatatypeDefinitionAxiom axiom) {
            m_axioms.m_definedDatatypesIRIs.add(axiom.getDatatype().getIRI().toString());
            List<OWLDataRange[]> aux = new ArrayList<OWLDataRange[]>();
            aux.add(new OWLDataRange[] { negative(axiom.getDatatype()),positive(axiom.getDataRange()) });
            aux.add(new OWLDataRange[] { negative(axiom.getDataRange()),positive(axiom.getDatatype()) });
            m_dataRangeInclusionsAsDisjunctions_map.put(axiom,aux);
        }

        // Keys

        public void visit(OWLHasKeyAxiom axiom) {
            for (OWLDataPropertyExpression dataPropertyExpression : axiom.getDataPropertyExpressions())
                checkTopDataPropertyUse(dataPropertyExpression,axiom);
            OWLClassExpression description=positive(axiom.getClassExpression());
            if (!isSimple(description)) {
                OWLClassExpression definition=getDefinitionFor(description,m_alreadyExists);
                if (!m_alreadyExists[0]){
                	m_auxiliaryClassExpressionInclusions.add(new OWLClassExpression[] { negative(definition),description });
                }
                description=definition;
            }
            ((OWLAxioms_withMaps) m_axioms).m_hasKeys_map.put(axiom,m_factory.getOWLHasKeyAxiom(description,axiom.getPropertyExpressions()));
            for (OWLObjectPropertyExpression objectPropertyExpression : axiom.getObjectPropertyExpressions())
                m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(objectPropertyExpression.getNamedProperty());
        }

        // Rules

        public void visit(SWRLRule rule) {
            for (SWRLAtom atom : rule.getBody())
                if (atom instanceof SWRLDataPropertyAtom)
                    checkTopDataPropertyUse(((SWRLDataPropertyAtom)atom).getPredicate(),rule);
            for (SWRLAtom atom : rule.getHead())
                if (atom instanceof SWRLDataPropertyAtom)
                    checkTopDataPropertyUse(((SWRLDataPropertyAtom)atom).getPredicate(),rule);
            if (rule.getBody().isEmpty()) {
                // process as fact
                Rule2FactConverter_map r2fConverter=new Rule2FactConverter_map(rule,m_classExpressionInclusionsAsDisjunctions_map,m_auxiliaryClassExpressionInclusions);
                for (SWRLAtom at : rule.getHead())
                    at.accept(r2fConverter);
                r2fConverter.doneWithRule();
            }
            else
                m_rules.add(rule);
        }
    }
    
    protected class Rule2FactConverter_map extends Rule2FactConverter {

    	SWRLRule rule;
    	protected final Map<OWLAxiom,List<OWLClassExpression[]>> m_newInclusions_map;
    	protected final List<OWLClassExpression[]> m_auxInclusions;
    	protected final List<OWLClassExpression[]> m_newInclusions4thisRule= new ArrayList<OWLClassExpression[]>();
    	protected final Collection<OWLIndividualAxiom> m_newFacts4thisRule= new HashSet<OWLIndividualAxiom>();
//        protected final boolean[] m_alreadyExists;
//        protected int freshDataProperties=0;
//        protected int freshIndividuals=0;

        public Rule2FactConverter_map(SWRLRule rule, Map<OWLAxiom,List<OWLClassExpression[]>> newInclusions, List<OWLClassExpression[]> auxInclusions) {
            super(null);
            m_newInclusions_map=newInclusions;
            m_auxInclusions=auxInclusions;
            this.rule = rule;
        }
        public void doneWithRule(){
        	if (!m_newInclusions4thisRule.isEmpty())
        		m_newInclusions_map.put(rule, m_newInclusions4thisRule);
        	if (!m_newFacts4thisRule.isEmpty())
            	addFacts(m_newFacts4thisRule,rule);
        }
        public void visit(SWRLClassAtom atom) {
            if (!(atom.getArgument() instanceof SWRLIndividualArgument))
                throw new IllegalArgumentException("A SWRL rule contains a head atom "+atom+" with a variable that does not occur in the body. ");
            OWLIndividual ind=((SWRLIndividualArgument)atom.getArgument()).getIndividual();
            if (ind.isAnonymous())
                throwAnonIndError(atom);
            if (!isSimple(atom.getPredicate())) {
                OWLClassExpression definition=getDefinitionFor(atom.getPredicate(),m_alreadyExists);
                if (!m_alreadyExists[0]){
                	m_auxInclusions.add(new OWLClassExpression[] { negative(definition),atom.getPredicate() });
                }
                m_newFacts4thisRule.add(m_factory.getOWLClassAssertionAxiom(definition,ind.asOWLNamedIndividual()));
            }
            else
            	m_newFacts4thisRule.add(m_factory.getOWLClassAssertionAxiom(atom.getPredicate(),ind.asOWLNamedIndividual()));
        }
        public void visit(SWRLDataRangeAtom atom) {
            if (atom.getArgument() instanceof SWRLVariable)
                throwVarError(atom);
            // dr(literal) :-
            // convert to: ClassAssertion(DataSomeValuesFrom(freshDP DataOneOf(literal)) freshIndividual)
            // and top -> \forall freshDP.dr
            OWLLiteral lit=((SWRLLiteralArgument)atom.getArgument()).getLiteral();
            OWLDataRange dr=atom.getPredicate();
            OWLNamedIndividual freshIndividual=getFreshIndividual();
            OWLDataProperty freshDP=getFreshDataProperty();
            OWLDataSomeValuesFrom some=m_factory.getOWLDataSomeValuesFrom(freshDP,m_factory.getOWLDataOneOf(lit));
            OWLClassExpression definition=getDefinitionFor(some,m_alreadyExists);
            if (!m_alreadyExists[0])
            	m_auxInclusions.add(new OWLClassExpression[] { negative(definition),some });
            m_newFacts4thisRule.add(m_factory.getOWLClassAssertionAxiom(definition,freshIndividual));
            m_newInclusions4thisRule.add(new OWLClassExpression[] { m_factory.getOWLDataAllValuesFrom(freshDP,dr) });
        }

        public void visit(SWRLObjectPropertyAtom atom) {
            if (!(atom.getFirstArgument() instanceof SWRLIndividualArgument) || !(atom.getSecondArgument() instanceof SWRLIndividualArgument))
                throwVarError(atom);
            OWLObjectPropertyExpression ope=atom.getPredicate().getSimplified();
            OWLIndividual first=((SWRLIndividualArgument)atom.getFirstArgument()).getIndividual();
            OWLIndividual second=((SWRLIndividualArgument)atom.getSecondArgument()).getIndividual();
            if (first.isAnonymous() || second.isAnonymous())
                throwAnonIndError(atom);
            if (ope.isAnonymous())
            	m_newFacts4thisRule.add(m_factory.getOWLObjectPropertyAssertionAxiom(ope.getNamedProperty(),second.asOWLNamedIndividual(),first.asOWLNamedIndividual()));
            else
            	m_newFacts4thisRule.add(m_factory.getOWLObjectPropertyAssertionAxiom(ope.asOWLObjectProperty(),first.asOWLNamedIndividual(),second.asOWLNamedIndividual()));
        }
        public void visit(SWRLDataPropertyAtom atom) {
            if (!(atom.getSecondArgument() instanceof SWRLLiteralArgument))
                throwVarError(atom);
            if (!(atom.getFirstArgument() instanceof SWRLIndividualArgument))
                throwVarError(atom);
            OWLIndividual ind=((SWRLIndividualArgument)atom.getFirstArgument()).getIndividual();
            if (ind.isAnonymous())
                throwAnonIndError(atom);
            OWLLiteral lit=((SWRLLiteralArgument)atom.getSecondArgument()).getLiteral();
            m_newFacts4thisRule.add(m_factory.getOWLDataPropertyAssertionAxiom(atom.getPredicate().asOWLDataProperty(),ind.asOWLNamedIndividual(),lit));
        }
        public void visit(SWRLSameIndividualAtom atom) {
            Set<OWLNamedIndividual> inds=new HashSet<OWLNamedIndividual>();
            for (SWRLArgument arg : atom.getAllArguments()) {
                if (!(arg instanceof SWRLIndividualArgument))
                    throwVarError(atom);
                OWLIndividual ind=((SWRLIndividualArgument)arg).getIndividual();
                if (ind.isAnonymous())
                    throwAnonIndError(atom);
                inds.add(ind.asOWLNamedIndividual());
            }
            m_newFacts4thisRule.add(m_factory.getOWLSameIndividualAxiom(inds));
        }
        public void visit(SWRLDifferentIndividualsAtom atom) {
            Set<OWLNamedIndividual> inds=new HashSet<OWLNamedIndividual>();
            for (SWRLArgument arg : atom.getAllArguments()) {
                if (!(arg instanceof SWRLIndividualArgument))
                    throwVarError(atom);
                OWLIndividual ind=((SWRLIndividualArgument)arg).getIndividual();
                if (ind.isAnonymous())
                    throwAnonIndError(atom);
                inds.add(ind.asOWLNamedIndividual());
            }
            m_newFacts4thisRule.add(m_factory.getOWLDifferentIndividualsAxiom(inds));
        }
    }
    
    protected final class RuleNormalizer_withMaps implements SWRLObjectVisitor { //cannot extend RuleNormalizer because it is final
        protected final Map<OWLAxiom,Collection<OWLAxioms.DisjunctiveRule>> m_rules_map;
        protected final Map<OWLAxiom,List<OWLClassExpression[]>> m_classExpressionInclusions_map;
        protected final Map<OWLAxiom,List<OWLDataRange[]>> m_dataRangeInclusions_map;
        protected final List<OWLClassExpression[]> m_auxiliaryClassExpressionInclusions;
        protected final List<OWLDataRange[]> m_auxiliaryDataRangeInclusions;
        protected final boolean[] m_alreadyExists;
        protected final List<SWRLAtom> m_bodyAtoms=new ArrayList<SWRLAtom>();
        protected final List<SWRLAtom> m_headAtoms=new ArrayList<SWRLAtom>();
        protected final Set<SWRLAtom> m_normalizedBodyAtoms=new HashSet<SWRLAtom>();
        protected final Set<SWRLAtom> m_normalizedHeadAtoms=new HashSet<SWRLAtom>();
        protected final Map<SWRLVariable,SWRLVariable> m_variableRepresentative=new HashMap<SWRLVariable,SWRLVariable>();
        protected final Map<OWLNamedIndividual,SWRLVariable> m_individualsToVariables=new HashMap<OWLNamedIndividual,SWRLVariable>();
        protected final Set<SWRLVariable> m_bodyDataRangeVariables=new HashSet<SWRLVariable>();
        protected final Set<SWRLVariable> m_headDataRangeVariables=new HashSet<SWRLVariable>();
        protected int m_newVariableIndex=0;
        protected boolean m_isPositive;

        public RuleNormalizer_withMaps(
        		Map<OWLAxiom,Collection<OWLAxioms.DisjunctiveRule>> rules_map,
        		Map<OWLAxiom, List<OWLClassExpression[]>> classExpressionInclusions_map,
        		List<OWLClassExpression[]> auxiliaryClassExpressionInclusions,
        		Map<OWLAxiom, List<OWLDataRange[]>> dataRangeInclusionsAsDisjunctions_map,
        		List<OWLDataRange[]> auxiliaryDataRangeInclusions) {
            m_rules_map=rules_map;
            m_classExpressionInclusions_map=classExpressionInclusions_map;
            m_auxiliaryClassExpressionInclusions=auxiliaryClassExpressionInclusions;
            m_dataRangeInclusions_map=dataRangeInclusionsAsDisjunctions_map;
            m_auxiliaryDataRangeInclusions=auxiliaryDataRangeInclusions;
            m_alreadyExists=new boolean[1];
        }
        public void visit(SWRLRule rule) {
            // Process head one-by-one and thus break up the conjunction in the head.
        	Collection<DisjunctiveRule> aux = new HashSet<OWLAxioms.DisjunctiveRule>();
            for (SWRLAtom headAtom : rule.getHead()) {
                m_individualsToVariables.clear();
                m_bodyAtoms.clear();
                m_headAtoms.clear();
                m_variableRepresentative.clear();
                m_normalizedBodyAtoms.clear();
                m_normalizedHeadAtoms.clear();
                m_bodyDataRangeVariables.clear();
                m_headDataRangeVariables.clear();

                // Initialize body with all atoms, and initialize head with just the atom we are processing.
                m_bodyAtoms.addAll(rule.getBody());
                m_headAtoms.add(headAtom);

                // First process sameIndividual in the body to set up variable normalizations.
                for (SWRLAtom atom : rule.getBody()) {
                    if (atom instanceof SWRLSameIndividualAtom) {
                        m_bodyAtoms.remove(atom);
                        SWRLSameIndividualAtom sameIndividualAtom=(SWRLSameIndividualAtom)atom;
                        SWRLVariable variable1=getVariableFor(sameIndividualAtom.getFirstArgument());
                        SWRLIArgument argument2=sameIndividualAtom.getSecondArgument();
                        if (argument2 instanceof SWRLVariable)
                            m_variableRepresentative.put((SWRLVariable)argument2,variable1);
                        else {
                            OWLIndividual individual=((SWRLIndividualArgument)argument2).getIndividual();
                            if (individual.isAnonymous())
                                throw new IllegalArgumentException("Internal error: Rules with anonymous individuals are not supported. ");
                            m_individualsToVariables.put(individual.asOWLNamedIndividual(),variable1);
                            m_bodyAtoms.add(m_factory.getSWRLClassAtom(m_factory.getOWLObjectOneOf(individual),variable1));
                        }
                    }
                }

                // Now process head atoms; this might increase the number of body atoms.
                m_isPositive=true;
                while (!m_headAtoms.isEmpty())
                    m_headAtoms.remove(0).accept(this);

                // Now process body atoms.
                m_isPositive=false;
                while (!m_bodyAtoms.isEmpty())
                    m_bodyAtoms.remove(0).accept(this);

                // Do some checking and return the rule.
                if (!m_bodyDataRangeVariables.containsAll(m_headDataRangeVariables))
                    throw new IllegalArgumentException("A SWRL rule contains data range variables in the head, but not in the body, and this is not supported.");
                aux.add(new OWLAxioms.DisjunctiveRule(m_normalizedBodyAtoms.toArray(new SWRLAtom[m_normalizedBodyAtoms.size()]),m_normalizedHeadAtoms.toArray(new SWRLAtom[m_normalizedHeadAtoms.size()])));
            }
            m_rules_map.put(rule,aux);
//            m_classExpressionInclusions_map.put(rule, m_additionalClassExpressionInclusions);
//            m_dataRangeInclusions_map.put(rule, m_additionalDataRangeInclusions);
        }
        public void visit(SWRLClassAtom at) {
            OWLClassExpression c=m_expressionManager.getSimplified(m_expressionManager.getNNF(at.getPredicate()));
            SWRLVariable variable=getVariableFor(at.getArgument());
            if (m_isPositive) {
                // head
                if (c instanceof OWLClass)
                    m_normalizedHeadAtoms.add(m_factory.getSWRLClassAtom(c,variable));
                else {
                    OWLClass definition=getClassFor(at.getPredicate(),m_alreadyExists);
                    if (!m_alreadyExists[0])
                        m_auxiliaryClassExpressionInclusions.add(new OWLClassExpression[] { negative(definition),at.getPredicate() });
                    m_normalizedHeadAtoms.add(m_factory.getSWRLClassAtom(definition,variable));
                }
            }
            else {
                // body
                if (c instanceof OWLClass)
                    m_normalizedBodyAtoms.add(m_factory.getSWRLClassAtom(c,variable));
                else {
                    OWLClass definition=getClassFor(at.getPredicate(),m_alreadyExists);
                    if (!m_alreadyExists[0])
                    	m_auxiliaryClassExpressionInclusions.add(new OWLClassExpression[] { negative(at.getPredicate()),definition });
                    m_normalizedBodyAtoms.add(m_factory.getSWRLClassAtom(definition,variable));
                }
            }
        }
        public void visit(SWRLDataRangeAtom at) {
            OWLDataRange dr=at.getPredicate();
            SWRLDArgument argument=at.getArgument();
            if (!(argument instanceof SWRLVariable))
                throw new IllegalArgumentException("A SWRL rule contains a data range with an argument that is not a literal, and such rules are not supported.");
            if (!m_isPositive)
                dr=m_factory.getOWLDataComplementOf(dr);
            dr=m_expressionManager.getNNF(m_expressionManager.getSimplified(dr));
            if (dr instanceof OWLDataIntersectionOf || dr instanceof OWLDataUnionOf) {
                OWLDatatype definition=getDefinitionFor(dr,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_auxiliaryDataRangeInclusions.add(new OWLDataRange[] { negative(definition),dr });
                dr=definition;
            }
            SWRLAtom atom=m_factory.getSWRLDataRangeAtom(dr,argument);
            m_normalizedHeadAtoms.add(atom);
            m_headDataRangeVariables.add((SWRLVariable)argument);
        }
        public void visit(SWRLObjectPropertyAtom at) {
            OWLObjectPropertyExpression ope=at.getPredicate().getSimplified();
            OWLObjectProperty op=ope.getNamedProperty();
            SWRLVariable variable1;
            SWRLVariable variable2;
            if (ope.isAnonymous()) {
                variable1=getVariableFor(at.getSecondArgument());
                variable2=getVariableFor(at.getFirstArgument());
            }
            else {
                variable1=getVariableFor(at.getFirstArgument());
                variable2=getVariableFor(at.getSecondArgument());

            }
            SWRLAtom newAtom=m_factory.getSWRLObjectPropertyAtom(op,variable1,variable2);
            if (m_isPositive) {
                // head
                m_normalizedHeadAtoms.add(newAtom);
            }
            else {
                // body
                m_normalizedBodyAtoms.add(newAtom);
            }
        }
        public void visit(SWRLDataPropertyAtom at) {
            OWLDataProperty dp=at.getPredicate().asOWLDataProperty();
            SWRLVariable variable1=getVariableFor(at.getFirstArgument());
            SWRLDArgument argument2=at.getSecondArgument();
            if (argument2 instanceof SWRLVariable) {
                SWRLVariable variable2=getVariableFor((SWRLVariable)argument2);
                if (m_isPositive) {
                    m_normalizedHeadAtoms.add(m_factory.getSWRLDataPropertyAtom(dp,variable1,variable2));
                    m_headDataRangeVariables.add(variable2);
                }
                else {
                    if (m_bodyDataRangeVariables.add(variable2))
                        m_normalizedBodyAtoms.add(m_factory.getSWRLDataPropertyAtom(dp,variable1,variable2));
                    else {
                        SWRLVariable variable2Fresh=getFreshVariable();
                        m_normalizedBodyAtoms.add(m_factory.getSWRLDataPropertyAtom(dp,variable1,variable2Fresh));
                        m_normalizedHeadAtoms.add(m_factory.getSWRLDifferentIndividualsAtom(variable2,variable2Fresh));
                    }
                }
            }
            else {
                OWLLiteral literal=((SWRLLiteralArgument)argument2).getLiteral();
                SWRLAtom newAtom=m_factory.getSWRLClassAtom(m_factory.getOWLDataHasValue(dp,literal),variable1);
                if (m_isPositive)
                    m_headAtoms.add(newAtom);
                else
                    m_bodyAtoms.add(newAtom);
            }
        }
        public void visit(SWRLBuiltInAtom at) {
            throw new IllegalArgumentException("A SWRL rule uses a built-in atom, but built-in atoms are not supported yet.");
        }
        public void visit(SWRLSameIndividualAtom at) {
            if (m_isPositive)
                m_normalizedHeadAtoms.add(m_factory.getSWRLSameIndividualAtom(getVariableFor(at.getFirstArgument()),getVariableFor(at.getSecondArgument())));
            else
                throw new IllegalStateException("Internal error: this SWRLSameIndividualAtom should have been processed earlier.");
        }
        public void visit(SWRLDifferentIndividualsAtom at) {
            if (m_isPositive)
                m_normalizedHeadAtoms.add(m_factory.getSWRLDifferentIndividualsAtom(getVariableFor(at.getFirstArgument()),getVariableFor(at.getSecondArgument())));
            else
                m_normalizedHeadAtoms.add(m_factory.getSWRLSameIndividualAtom(getVariableFor(at.getFirstArgument()),getVariableFor(at.getSecondArgument())));
        }
        public void visit(SWRLVariable variable) {
            // nothing to do
        }
        public void visit(SWRLIndividualArgument argument) {
            // nothing to do
        }
        public void visit(SWRLLiteralArgument argument) {
            // nothing to do
        }
        protected SWRLVariable getVariableFor(SWRLIArgument term) {
            SWRLVariable variable;
            if (term instanceof SWRLIndividualArgument) {
                OWLIndividual individual=((SWRLIndividualArgument)term).getIndividual();
                if (individual.isAnonymous())
                    throw new IllegalArgumentException("Internal error: Rules with anonymous individuals are not supported. ");
                variable=m_individualsToVariables.get(individual.asOWLNamedIndividual());
                if (variable==null) {
                    variable=getFreshVariable();
                    m_individualsToVariables.put(individual.asOWLNamedIndividual(),variable);
                    m_bodyAtoms.add(m_factory.getSWRLClassAtom(m_factory.getOWLObjectOneOf(individual),variable));
                }
            }
            else
                variable=(SWRLVariable)term;
            SWRLVariable representative=m_variableRepresentative.get(variable);
            if (representative==null)
                return variable;
            else
                return representative;
        }
        protected SWRLVariable getFreshVariable() {
            SWRLVariable variable=m_factory.getSWRLVariable(IRI.create("internal:swrl#"+m_newVariableIndex));
            m_newVariableIndex++;
            return variable;
        }
    }
    
}