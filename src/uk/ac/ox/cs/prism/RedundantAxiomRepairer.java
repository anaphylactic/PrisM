package uk.ac.ox.cs.prism;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class RedundantAxiomRepairer {

	public static OWLAxiom repair(OWLAxiom ax, OWLDataFactory factory){
		if (ax instanceof OWLSubClassOfAxiom){
			OWLClassExpression subClass = ((OWLSubClassOfAxiom) ax).getSubClass();
			OWLClassExpression superClass = ((OWLSubClassOfAxiom) ax).getSuperClass();
			if (superClass instanceof OWLObjectAllValuesFrom){
				OWLObjectPropertyExpression p = ((OWLObjectAllValuesFrom) superClass).getProperty();
				if (subClass instanceof OWLObjectSomeValuesFrom && 
						((OWLObjectSomeValuesFrom) subClass).getProperty().equals(p) &&
						((OWLObjectSomeValuesFrom) subClass).getFiller().isTopEntity()){
					return factory.getOWLSubClassOfAxiom(factory.getOWLThing(), superClass);
				}
				else if (subClass instanceof OWLObjectIntersectionOf){
					Set<OWLClassExpression> operands = new HashSet<OWLClassExpression>();
					for (OWLClassExpression exp : ((OWLObjectIntersectionOf) subClass).getOperands()){
						if (!(exp instanceof OWLObjectSomeValuesFrom && 
								((OWLObjectSomeValuesFrom) exp).getProperty().equals(p) &&
								((OWLObjectSomeValuesFrom) exp).getFiller().isTopEntity()))
							operands.add(exp);
					}
					return factory.getOWLSubClassOfAxiom(factory.getOWLObjectIntersectionOf(operands), superClass);
				}
				else return ax;
			}
			else
				return ax;
		}
		else
			return ax;
	}
	
}
