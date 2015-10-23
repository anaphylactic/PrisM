package uk.ac.ox.cs.prism.clausification;

import java.util.Collection;
import java.util.Map.Entry;

import org.semanticweb.HermiT.structural.BuiltInPropertyManager;
import org.semanticweb.HermiT.structural.OWLAxioms;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;

public class BuiltInPropertyManager_withMaps extends BuiltInPropertyManager {

	public BuiltInPropertyManager_withMaps(OWLDataFactory factory) {
        super(factory);
    }
    public void axiomatizeBuiltInPropertiesAsNeeded(OWLAxioms axioms,boolean skipTopObjectProperty,boolean skipBottomObjectProperty,boolean skipTopDataProperty,boolean skipBottomDataProperty) {
        Checker checker=new Checker(axioms);
        if (checker.m_usesTopObjectProperty && !skipTopObjectProperty)
            axiomatizeTopObjectProperty(axioms);
        if (checker.m_usesBottomObjectProperty && !skipBottomObjectProperty)
            axiomatizeBottomObjectProperty(axioms);
        if (checker.m_usesTopDataProperty && !skipTopDataProperty)
            axiomatizeTopDataProperty(axioms);
        if (checker.m_usesBottomDataProperty && !skipBottomDataProperty)
            axiomatizeBottomDataProperty(axioms);
    }
    public void axiomatizeBuiltInPropertiesAsNeeded(OWLAxioms axioms) {
        axiomatizeBuiltInPropertiesAsNeeded(axioms,false,false,false,false);
    }
    protected void axiomatizeTopObjectProperty(OWLAxioms axioms) {
        // TransitiveObjectProperty( owl:topObjectProperty )
        axioms.m_complexObjectPropertyInclusions.add(new OWLAxioms.ComplexObjectPropertyInclusion(m_topObjectProperty));
        // SymmetricObjectProperty( owl:topObjectProperty )
        axioms.m_simpleObjectPropertyInclusions.add(new OWLObjectPropertyExpression[] { m_topObjectProperty,m_topObjectProperty.getInverseProperty() });
        // SubClassOf( owl:Thing ObjectSomeValuesFrom( owl:topObjectProperty ObjectOneOf( <internal:nam#topIndividual> ) ) )
        OWLIndividual newIndividual=m_factory.getOWLNamedIndividual(IRI.create("internal:nam#topIndividual"));
        OWLObjectOneOf oneOfNewIndividual=m_factory.getOWLObjectOneOf(newIndividual);
        OWLObjectSomeValuesFrom hasTopNewIndividual=m_factory.getOWLObjectSomeValuesFrom(m_topObjectProperty,oneOfNewIndividual);
        axioms.m_conceptInclusions.add(new OWLClassExpression[] { hasTopNewIndividual });
    }
    protected void axiomatizeBottomObjectProperty(OWLAxioms axioms) {
        axioms.m_conceptInclusions.add(new OWLClassExpression[] { m_factory.getOWLObjectAllValuesFrom(m_bottomObjectProperty,m_factory.getOWLNothing()) });
    }
    protected void axiomatizeTopDataProperty(OWLAxioms axioms) {
        OWLDatatype anonymousConstantsDatatype=m_factory.getOWLDatatype(IRI.create("internal:anonymous-constants"));
        OWLLiteral newConstant=m_factory.getOWLLiteral("internal:constant",anonymousConstantsDatatype);
        OWLDataOneOf oneOfNewConstant=m_factory.getOWLDataOneOf(newConstant);
        OWLDataSomeValuesFrom hasTopNewConstant=m_factory.getOWLDataSomeValuesFrom(m_topDataProperty,oneOfNewConstant);
        axioms.m_conceptInclusions.add(new OWLClassExpression[] { hasTopNewConstant });
    }
    protected void axiomatizeBottomDataProperty(OWLAxioms axioms) {
        axioms.m_conceptInclusions.add(new OWLClassExpression[] { m_factory.getOWLDataAllValuesFrom(m_bottomDataProperty,m_factory.getOWLDataComplementOf(m_factory.getTopDatatype())) });
    }

    protected class Checker_withMaps implements OWLClassExpressionVisitor { 
    	//since everything happens in the constructor it doesn't make sense to extend Checker from BuiltInPropertyManager
        public boolean m_usesTopObjectProperty;
        public boolean m_usesBottomObjectProperty;
        public boolean m_usesTopDataProperty;
        public boolean m_usesBottomDataProperty;

        public Checker_withMaps(OWLAxioms_withMaps axioms) {
        	for (Entry<OWLAxiom,Collection<OWLClassExpression[]>> entry : axioms.m_conceptInclusions_map.entrySet())
        		for (OWLClassExpression[] inclusion : entry.getValue())
                    for (OWLClassExpression description : inclusion)
                        description.accept(this);
            for (OWLClassExpression[] inclusion : axioms.m_auxiliaryConceptInclusions)
                for (OWLClassExpression description : inclusion)
                    description.accept(this);
            for (Entry<OWLAxiom,Collection<OWLObjectPropertyExpression[]>> entry : axioms.m_simpleObjectPropertyInclusions_map.entrySet())
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
            for (OWLObjectPropertyExpression property : axioms.m_symmetricObjectProperties)
                visitProperty(property);
            for (OWLObjectPropertyExpression property : axioms.m_asymmetricObjectProperties)
                visitProperty(property);
            for (Entry<OWLAxiom,Collection<OWLDataPropertyExpression[]>> entry : axioms.m_dataPropertyInclusions_map.entrySet())
            	for (OWLDataPropertyExpression[] inclusion : entry.getValue()) {
            		visitProperty(inclusion[0]);
            		visitProperty(inclusion[1]);
            	}
            for (OWLDataPropertyExpression[] disjoint : axioms.m_disjointDataProperties)
                for (int index=0;index<disjoint.length;index++)
                    visitProperty(disjoint[index]);
            FactVisitor_withMaps factVisitor=new FactVisitor_withMaps();
            for (Entry<OWLAxiom, Collection<OWLIndividualAxiom>> entry : axioms.m_facts_map.entrySet())
            	for (OWLIndividualAxiom fact : entry.getValue())
            		fact.accept(factVisitor);
        }
        protected void visitProperty(OWLObjectPropertyExpression object) {
            if (object.getNamedProperty().equals(m_topObjectProperty))
                m_usesTopObjectProperty=true;
            else if (object.getNamedProperty().equals(m_bottomObjectProperty))
                m_usesBottomObjectProperty=true;
        }

        protected void visitProperty(OWLDataPropertyExpression object) {
            if (object.asOWLDataProperty().equals(m_topDataProperty))
                m_usesTopDataProperty=true;
            else if (object.asOWLDataProperty().equals(m_bottomDataProperty))
                m_usesBottomDataProperty=true;
        }

        public void visit(OWLClass object) {
        }

        public void visit(OWLObjectComplementOf object) {
            object.getOperand().accept(this);
        }

        public void visit(OWLObjectIntersectionOf object) {
            for (OWLClassExpression description : object.getOperands())
                description.accept(this);
        }

        public void visit(OWLObjectUnionOf object) {
            for (OWLClassExpression description : object.getOperands())
                description.accept(this);
        }

        public void visit(OWLObjectOneOf object) {
        }

        public void visit(OWLObjectSomeValuesFrom object) {
            visitProperty(object.getProperty());
            object.getFiller().accept(this);
        }

        public void visit(OWLObjectHasValue object) {
            visitProperty(object.getProperty());
        }

        public void visit(OWLObjectHasSelf object) {
            visitProperty(object.getProperty());
        }

        public void visit(OWLObjectAllValuesFrom object) {
            visitProperty(object.getProperty());
            object.getFiller().accept(this);
        }

        public void visit(OWLObjectMinCardinality object) {
            visitProperty(object.getProperty());
            object.getFiller().accept(this);
        }

        public void visit(OWLObjectMaxCardinality object) {
            visitProperty(object.getProperty());
            object.getFiller().accept(this);
        }

        public void visit(OWLObjectExactCardinality object) {
            visitProperty(object.getProperty());
            object.getFiller().accept(this);
        }

        public void visit(OWLDataHasValue object) {
            visitProperty(object.getProperty());
        }

        public void visit(OWLDataSomeValuesFrom object) {
            visitProperty(object.getProperty());
        }

        public void visit(OWLDataAllValuesFrom object) {
            visitProperty(object.getProperty());
        }

        public void visit(OWLDataMinCardinality object) {
            visitProperty(object.getProperty());
        }

        public void visit(OWLDataMaxCardinality object) {
            visitProperty(object.getProperty());
        }

        public void visit(OWLDataExactCardinality object) {
            visitProperty(object.getProperty());
        }

        protected class FactVisitor_withMaps extends OWLAxiomVisitorAdapter {

            public void visit(OWLSameIndividualAxiom object) {
            }

            public void visit(OWLDifferentIndividualsAxiom object) {
            }

            public void visit(OWLClassAssertionAxiom object) {
                object.getClassExpression().accept(Checker_withMaps.this);
            }

            public void visit(OWLObjectPropertyAssertionAxiom object) {
                visitProperty(object.getProperty());
            }

            public void visit(OWLNegativeObjectPropertyAssertionAxiom object) {
                visitProperty(object.getProperty());
            }

            public void visit(OWLDataPropertyAssertionAxiom object) {
                visitProperty(object.getProperty());
            }

            public void visit(OWLNegativeDataPropertyAssertionAxiom object) {
                visitProperty(object.getProperty());
            }
        }
    }
    
}
