package uk.ac.ox.cs.prism.clausification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.AtLeastDataRange;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.DatatypeRestriction;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InternalDatatype;
import org.semanticweb.HermiT.model.LiteralDataRange;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.HermiT.structural.OWLAxioms;
import org.semanticweb.HermiT.structural.OWLAxiomsExpressivity;
import org.semanticweb.HermiT.structural.OWLClausification;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
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
import org.semanticweb.owlapi.model.SWRLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;

public class OWLClausification_withMaps extends OWLClausification{	

	protected DatatypeManager m_datatypeManager;
	
	public OWLClausification_withMaps(DatatypeManager datatypeManager) {
		super(new Configuration());
		m_datatypeManager = datatypeManager;
	}
	public DLOntology_withMaps preprocessAndClausify(OWLOntology rootOntology) {
		OWLDataFactory factory=rootOntology.getOWLOntologyManager().getOWLDataFactory();
		String ontologyIRI=rootOntology.getOntologyID().getDefaultDocumentIRI()==null ? "urn:hermit:kb" : rootOntology.getOntologyID().getDefaultDocumentIRI().toString();
		Collection<OWLOntology> importClosure=rootOntology.getImportsClosure();
		OWLAxioms_withMaps axioms=new OWLAxioms_withMaps();
		OWLNormalization_withMaps normalization=new OWLNormalization_withMaps(factory,axioms,0,m_datatypeManager);
		for (OWLOntology ontology : importClosure)
			normalization.processOntology(ontology);
		BuiltInPropertyManager_withMaps builtInPropertyManager=new BuiltInPropertyManager_withMaps(factory);
		builtInPropertyManager.axiomatizeBuiltInPropertiesAsNeeded(axioms);
		ObjectPropertyInclusionManager_withMaps objectPropertyInclusionManager=new ObjectPropertyInclusionManager_withMaps(axioms);
		// now object property inclusion manager added all non-simple properties to axioms.m_complexObjectPropertyExpressions
		// now that we know which roles are non-simple, we can decide which negative object property assertions have to be
		// expressed as concept assertions so that transitivity rewriting applies properly.
		objectPropertyInclusionManager.rewriteNegativeObjectPropertyAssertions(factory,axioms,normalization.getDefinitions().size());
		objectPropertyInclusionManager.rewriteAxioms(factory,axioms,0);
		OWLAxiomsExpressivity_withMaps axiomsExpressivity=new OWLAxiomsExpressivity_withMaps(axioms);
		DLOntology_withMaps dlOntology=clausify_withMaps(factory,ontologyIRI,axioms,axiomsExpressivity);
		return dlOntology;
	}
	public DLOntology_withMaps clausify_withMaps(OWLDataFactory factory,String ontologyIRI,OWLAxioms_withMaps axioms,OWLAxiomsExpressivity axiomsExpressivity) {
		Map<DLClause,Collection<OWLAxiom>> dlClauses=new HashMap<DLClause,Collection<OWLAxiom>>();
		Map<Atom,Collection<OWLAxiom>> positiveFacts=new HashMap<Atom,Collection<OWLAxiom>>();
		Map<Atom,Collection<OWLAxiom>> negativeFacts=new HashMap<Atom,Collection<OWLAxiom>>();
		for (Entry<OWLAxiom,Collection<OWLObjectPropertyExpression[]>> entry : axioms.m_simpleObjectPropertyInclusions_map.entrySet()) {
			for (OWLObjectPropertyExpression[] inclusion : entry.getValue()) {
				Atom subRoleAtom=getRoleAtom(inclusion[0],X,Y);
				Atom superRoleAtom=getRoleAtom(inclusion[1],X,Y);
				DLClause dlClause=DLClause.create(new Atom[] { superRoleAtom },new Atom[] { subRoleAtom });
				Collection<OWLAxiom> correspAxioms = dlClauses.get(dlClause);
				if (correspAxioms == null) {
					correspAxioms = new ArrayList<OWLAxiom>();
					dlClauses.put(dlClause, correspAxioms);
				}
				correspAxioms.add(entry.getKey());
			}
		}
		for (Entry<OWLAxiom,Collection<OWLDataPropertyExpression[]>> entry : axioms.m_dataPropertyInclusions_map.entrySet()) {
			for (OWLDataPropertyExpression[] inclusion : entry.getValue()) {
				Atom subProp=getRoleAtom(inclusion[0],X,Y);
				Atom superProp=getRoleAtom(inclusion[1],X,Y);
				DLClause dlClause=DLClause.create(new Atom[] { superProp },new Atom[] { subProp });
				Collection<OWLAxiom> correspAxioms = dlClauses.get(dlClause);
				if (correspAxioms == null) {
					correspAxioms = new ArrayList<OWLAxiom>();
					dlClauses.put(dlClause, correspAxioms);
				}
				correspAxioms.add(entry.getKey());
			}
			if (entry.getValue().contains(factory.getOWLDataProperty(IRI.create(AtomicRole.BOTTOM_DATA_ROLE.getIRI())))) {
				Atom bodyAtom=Atom.create(AtomicRole.BOTTOM_DATA_ROLE,X,Y);
				DLClause dlClause = DLClause.create(new Atom[] {},new Atom[] { bodyAtom });
				Collection<OWLAxiom> correspAxioms = dlClauses.get(dlClause);
				if (correspAxioms == null) {
					correspAxioms = new ArrayList<OWLAxiom>();
					dlClauses.put(dlClause, correspAxioms);
				}
				correspAxioms.add(entry.getKey());
			}
		}
		for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_symmetricObjectProperties) {
			Atom roleAtom=getRoleAtom(objectPropertyExpression,X,Y);
			Atom inverseRoleAtom=getRoleAtom(objectPropertyExpression,Y,X);
			DLClause dlClause=DLClause.create(new Atom[] { inverseRoleAtom },new Atom[] { roleAtom });
			Collection<OWLAxiom> correspAxioms = dlClauses.get(dlClause);
			if (correspAxioms == null) {
				correspAxioms = new ArrayList<OWLAxiom>();
				dlClauses.put(dlClause, correspAxioms);
			}
			correspAxioms.add(factory.getOWLSymmetricObjectPropertyAxiom(objectPropertyExpression));
		}
		for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_asymmetricObjectProperties) {
			Atom roleAtom=getRoleAtom(objectPropertyExpression,X,Y);
			Atom inverseRoleAtom=getRoleAtom(objectPropertyExpression,Y,X);
			DLClause dlClause=DLClause.create(new Atom[] {},new Atom[] { roleAtom,inverseRoleAtom });
			Collection<OWLAxiom> correspAxioms = dlClauses.get(dlClause);
			if (correspAxioms == null) {
				correspAxioms = new ArrayList<OWLAxiom>();
				dlClauses.put(dlClause, correspAxioms);
			}
			correspAxioms.add(factory.getOWLAsymmetricObjectPropertyAxiom(objectPropertyExpression));
		}
		for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_reflexiveObjectProperties) {
			Atom roleAtom=getRoleAtom(objectPropertyExpression,X,X);
			Atom bodyAtom=Atom.create(AtomicConcept.THING,X);
			DLClause dlClause=DLClause.create(new Atom[] { roleAtom },new Atom[] { bodyAtom });
			Collection<OWLAxiom> correspAxioms = dlClauses.get(dlClause);
			if (correspAxioms == null) {
				correspAxioms = new ArrayList<OWLAxiom>();
				dlClauses.put(dlClause, correspAxioms);
			}
			correspAxioms.add(factory.getOWLReflexiveObjectPropertyAxiom(objectPropertyExpression));
		}
		for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_irreflexiveObjectProperties) {
			Atom roleAtom=getRoleAtom(objectPropertyExpression,X,X);
			DLClause dlClause=DLClause.create(new Atom[] {},new Atom[] { roleAtom });
			Collection<OWLAxiom> correspAxioms = dlClauses.get(dlClause);
			if (correspAxioms == null) {
				correspAxioms = new ArrayList<OWLAxiom>();
				dlClauses.put(dlClause, correspAxioms);
			}
			correspAxioms.add(factory.getOWLIrreflexiveObjectPropertyAxiom(objectPropertyExpression));
		}
		for (OWLObjectPropertyExpression[] properties : axioms.m_disjointObjectProperties) {
			OWLAxiom axiom = factory.getOWLDisjointObjectPropertiesAxiom(properties);
			for (int i=0;i<properties.length;i++)
				for (int j=i+1;j<properties.length;j++) {
					Atom atom_i=getRoleAtom(properties[i],X,Y);
					Atom atom_j=getRoleAtom(properties[j],X,Y);
					DLClause dlClause=DLClause.create(new Atom[] {},new Atom[] { atom_i,atom_j });
					Collection<OWLAxiom> correspAxioms = dlClauses.get(dlClause);
					if (correspAxioms == null) {
						correspAxioms = new ArrayList<OWLAxiom>();
						dlClauses.put(dlClause, correspAxioms);
					}
					correspAxioms.add(axiom);
				}
		}
		for (OWLDataPropertyExpression[] properties : axioms.m_disjointDataProperties) {
			OWLAxiom axiom = factory.getOWLDisjointDataPropertiesAxiom(properties);
			for (int i=0;i<properties.length;i++)
				for (int j=i+1;j<properties.length;j++) {
					Atom atom_i=getRoleAtom(properties[i],X,Y);
					Atom atom_j=getRoleAtom(properties[j],X,Z);
					Atom atom_ij=Atom.create(Inequality.create(),Y,Z);
					DLClause dlClause=DLClause.create(new Atom[] { atom_ij },new Atom[] { atom_i,atom_j });
					Collection<OWLAxiom> correspAxioms = dlClauses.get(dlClause);
					if (correspAxioms == null) {
						correspAxioms = new ArrayList<OWLAxiom>();
						dlClauses.put(dlClause, correspAxioms);
					}
					correspAxioms.add(axiom);
				}
		}
		DataRangeConverter dataRangeConverter=new DataRangeConverter(m_configuration.warningMonitor,axioms.m_definedDatatypesIRIs,new HashSet<DatatypeRestriction>(),m_configuration.ignoreUnsupportedDatatypes);
		Set<Atom> auxiliaryFacts = new HashSet<Atom>();
		NormalizedAxiomClausifier_withMaps clausifier=new NormalizedAxiomClausifier_withMaps(dataRangeConverter,auxiliaryFacts,factory);
		for (Entry<OWLAxiom,Collection<OWLClassExpression[]>> entry : axioms.m_conceptInclusions_map.entrySet())
			for (OWLClassExpression[] inclusion : entry.getValue()) {
				for (OWLClassExpression description : inclusion)
					description.accept(clausifier);
				DLClause dlClause=clausifier.getDLClause().getSafeVersion(AtomicConcept.THING);
				Collection<OWLAxiom> correspAxioms = dlClauses.get(dlClause);
				if (correspAxioms == null) {
					correspAxioms = new ArrayList<OWLAxiom>();
					dlClauses.put(dlClause, correspAxioms);
				}
				correspAxioms.add(entry.getKey());
			}
		for (OWLClassExpression[] inclusion : axioms.m_auxiliaryConceptInclusions) {
			for (OWLClassExpression description : inclusion)
				description.accept(clausifier);
			DLClause dlClause=clausifier.getDLClause().getSafeVersion(AtomicConcept.THING);
			Collection<OWLAxiom> correspAxioms = dlClauses.get(dlClause);
			if (correspAxioms == null) {
				correspAxioms = new ArrayList<OWLAxiom>();
				dlClauses.put(dlClause, correspAxioms);
			}
		}
		//        NormalizedDataRangeAxiomClausifier_withMaps normalizedDataRangeAxiomClausifier=new NormalizedDataRangeAxiomClausifier_withMaps(dataRangeConverter,factory,axioms.m_definedDatatypesIRIs);
		//        
		//        for (Entry<OWLAxiom,Collection<OWLDataRange[]>> entry : axioms.m_dataRangeInclusions_map.entrySet()) {
		//        	for (OWLDataRange[] inclusion : entry.getValue()) {
		//        		for (OWLDataRange description : inclusion)
		//        			description.accept(normalizedDataRangeAxiomClausifier);
		//        		DLClause dlClause=normalizedDataRangeAxiomClausifier.getDLClause().getSafeVersion(InternalDatatype.RDFS_LITERAL);
		//        		Collection<OWLAxiom> correspAxioms = dlClauses.get(dlClause);
		//                if (correspAxioms == null) {
		//        			correspAxioms = new ArrayList<OWLAxiom>();
		//        			dlClauses.put(dlClause, correspAxioms);
		//        		}
		//        		correspAxioms.add(entry.getKey());
		//        	}
		//        }
		//        for (OWLDataRange[] inclusion : axioms.m_auxiliaryDataRangeInclusions) {
		//            for (OWLDataRange description : inclusion)
		//                description.accept(normalizedDataRangeAxiomClausifier);
		//            DLClause dlClause=normalizedDataRangeAxiomClausifier.getDLClause().getSafeVersion(InternalDatatype.RDFS_LITERAL);
		//            Collection<OWLAxiom> correspAxioms = dlClauses.get(dlClause);
		//            if (correspAxioms == null) {
		//    			correspAxioms = new ArrayList<OWLAxiom>();
		//    			dlClauses.put(dlClause, correspAxioms);
		//    		}
		//        }
		//        for (Entry<OWLAxiom,OWLHasKeyAxiom> entry : axioms.m_hasKeys_map.entrySet()) {
		//        	DLClause dlClause = clausifyKey(entry.getValue());
		//        	Collection<OWLAxiom> correspAxioms = dlClauses.get(dlClause);
		//            if (correspAxioms == null) {
		//    			correspAxioms = new ArrayList<OWLAxiom>();
		//    			dlClauses.put(dlClause, correspAxioms);
		//    		}
		//    		correspAxioms.add(entry.getKey());
		//        }

		FactClausifier_withMaps factClausifier=new FactClausifier_withMaps(dataRangeConverter,positiveFacts,negativeFacts);
		for (Entry<OWLAxiom,Collection<OWLIndividualAxiom>> entry : axioms.m_facts_map.entrySet())
			for (OWLIndividualAxiom fact : entry.getValue())
				factClausifier.process(entry.getKey(), fact);
		for (Atom atom : auxiliaryFacts) {
			Collection<OWLAxiom> originalAxioms = positiveFacts.get(atom);
			if (originalAxioms == null) {
				originalAxioms = new ArrayList<OWLAxiom>();
				positiveFacts.put(atom,originalAxioms);
			}
		}
//		OWLLiteral[] literals = new OWLLiteral[axioms.m_literals.size()];
//		int i = 0;
//		for (OWLLiteral lit : axioms.m_literals)
//			literals[i++] = lit;
//		for (i = 0; i<literals.length; i++)
//			for (int j = i+1; j<literals.length; j++) {
//				factClausifier.process(null, factory.getOWLDifferentIndividualsAxiom(factory.getOWLNamedIndividual(IRI.create(literals[i].toString())), factory.getOWLNamedIndividual(IRI.create(literals[j].toString()))));
//				//        		positiveFacts.put(Atom.create(Inequality.INSTANCE, literals[i], literals[j]), new ArrayList<OWLAxiom>());
//			}

		Set<Individual> individuals=new HashSet<Individual>();
		for (OWLNamedIndividual owlIndividual : axioms.m_namedIndividuals) {
			Individual individual=Individual.create(owlIndividual.getIRI().toString());
			individuals.add(individual);
			// all named individuals are tagged with a concept, so that keys/rules are
			// only applied to them
			if (!axioms.m_hasKeys.isEmpty() || !axioms.m_rules.isEmpty()) {
				Atom atom = Atom.create(AtomicConcept.INTERNAL_NAMED,individual);
				Collection<OWLAxiom> originalAxioms = positiveFacts.get(atom);
				if (originalAxioms == null) {
					originalAxioms = new ArrayList<OWLAxiom>();
					positiveFacts.put(atom,originalAxioms);
				}	
			}
		}

		// Clausify SWRL rules
		if (!axioms.m_rules.isEmpty())
			new NormalizedRuleClausifier_withMaps(axioms.m_objectPropertiesOccurringInOWLAxioms,dataRangeConverter,dlClauses).processRules(axioms.m_rules_map);
		// Create the DL ontology
		return new DLOntology_withMaps(dlClauses,positiveFacts,negativeFacts,axioms.m_literals);
	}

	protected static class NormalizedAxiomClausifier_withMaps extends NormalizedAxiomClausifier {

		public NormalizedAxiomClausifier_withMaps(DataRangeConverter dataRangeConverter,Set<Atom> positiveFacts,OWLDataFactory factory) {
			super(dataRangeConverter,positiveFacts,factory);
		}
		protected DLClause getDLClause() {
			return super.getDLClause();
		}
		public void visit(OWLDataSomeValuesFrom object) {
			if (!object.getProperty().isOWLBottomDataProperty()) {
				AtomicRole atomicRole=getAtomicRole(object.getProperty());
				OWLDataRange filler = object.getFiller();
				if (filler instanceof OWLDataOneOf) {
					for (OWLLiteral lit : ((OWLDataOneOf)filler).getValues()) {
						LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(m_factory.getOWLDataOneOf(lit));
						AtLeastDataRange atLeastDataRange=AtLeastDataRange.create(1,atomicRole,literalRange);
						if (!atLeastDataRange.isAlwaysFalse())
							m_headAtoms.add(Atom.create(atLeastDataRange,X));
					}
				}
				else {
					LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(object.getFiller());
					AtLeastDataRange atLeastDataRange=AtLeastDataRange.create(1,atomicRole,literalRange);
					if (!atLeastDataRange.isAlwaysFalse())
						m_headAtoms.add(Atom.create(atLeastDataRange,X));
				}
			}
		}
		public void visit(OWLDataAllValuesFrom object) {
			//if the filler is the negation of a supported range then we will leave the expression as if it was in the head,
			//because when we overapproximate we are only going to look at the property so there's no point in decomposing it further
			//if the filler is positive, however, we will decompose it a bit
			LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(object.getFiller());
			if (object.getProperty().isOWLTopDataProperty()) {
				if (literalRange.isAlwaysFalse())
					return; // bottom
			}

			Variable y=nextY();
			m_bodyAtoms.add(getRoleAtom(object.getProperty(),X,y));
			if (literalRange.isNegatedInternalDatatype()) {
				InternalDatatype negatedRange=(InternalDatatype)literalRange.getNegation();
				if (!negatedRange.isAlwaysTrue())
					m_bodyAtoms.add(Atom.create(negatedRange,y));
			}
			else {
				if (!literalRange.isAlwaysFalse()) {
					if (object.getFiller() instanceof OWLDataOneOf) {
						for (OWLLiteral lit : ((OWLDataOneOf) object.getFiller()).getValues())
							m_headAtoms.add(Atom.create(Equality.INSTANCE,y,(Constant) lit.accept(m_dataRangeConverter)));
					}
					else 
						m_headAtoms.add(Atom.create((DLPredicate)literalRange,y));   
				}
			}
		}
		public void visit(OWLDataMinCardinality object) {
			if (!object.getProperty().isOWLBottomDataProperty() || object.getCardinality()==0) {
				AtomicRole atomicRole=getAtomicRole(object.getProperty());
				if (object.getCardinality() == 1) {
					m_factory.getOWLDataSomeValuesFrom(object.getProperty(), object.getFiller()).accept(this); 
					// if the cardinality is more than one then we don't make the disjunction explicit because of how we are going 
					//to overapproximate it (also, making the disjunction explicitly would be exponential)
				}
				else {
					LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(object.getFiller());
					AtLeastDataRange atLeast=AtLeastDataRange.create(object.getCardinality(),atomicRole,literalRange);
					if (!atLeast.isAlwaysFalse())
						m_headAtoms.add(Atom.create(atLeast,X));
				}
			}
		}

	}

	//    protected static class NormalizedDataRangeAxiomClausifier_withMaps extends NormalizedDataRangeAxiomClausifier {
	//
	//        public NormalizedDataRangeAxiomClausifier_withMaps(DataRangeConverter dataRangeConverter,OWLDataFactory factory,Set<String> definedDatatypeIRIs) {
	//            super(dataRangeConverter,factory,definedDatatypeIRIs);
	//        }
	//        protected DLClause getDLClause() {
	//            return super.getDLClause();
	//        }
	//    }

	protected static class FactClausifier_withMaps extends OWLAxiomVisitorAdapter {
		protected final DataRangeConverter m_dataRangeConverter;
		protected final Map<Atom,Collection<OWLAxiom>> m_positiveFacts_map;
		protected final Map<Atom,Collection<OWLAxiom>> m_negativeFacts_map;
		protected OWLAxiom originalAxiom;

		public FactClausifier_withMaps(DataRangeConverter dataRangeConverter,Map<Atom,Collection<OWLAxiom>> positiveFacts_map,Map<Atom,Collection<OWLAxiom>> negativeFacts_map) {
			m_dataRangeConverter=dataRangeConverter;
			m_positiveFacts_map=positiveFacts_map;
			m_negativeFacts_map=negativeFacts_map;
		}
		public void process(OWLAxiom originalAxiom, OWLIndividualAxiom fact) {
			this.originalAxiom = originalAxiom;
			fact.accept(this);
			this.originalAxiom = null;
		}
		public void visit(OWLSameIndividualAxiom object) {
			OWLIndividual[] individuals=new OWLIndividual[object.getIndividuals().size()];
			object.getIndividuals().toArray(individuals);
			for (int i=0;i<individuals.length-1;i++) {
				Atom atom = Atom.create(Equality.create(),getIndividual(individuals[i]),getIndividual(individuals[i+1]));
				Collection<OWLAxiom> originalAxioms = m_positiveFacts_map.get(atom);
				if (originalAxioms == null) {
					originalAxioms = new ArrayList<OWLAxiom>();
					m_positiveFacts_map.put(atom,originalAxioms);
				}
				if (originalAxiom!= null)
					originalAxioms.add(originalAxiom);
			}
		}
		public void visit(OWLDifferentIndividualsAxiom object) {
			OWLIndividual[] individuals=new OWLIndividual[object.getIndividuals().size()];
			object.getIndividuals().toArray(individuals);
			for (int i=0;i<individuals.length;i++)
				for (int j=i+1;j<individuals.length;j++) {
					Atom atom = Atom.create(Inequality.create(),getIndividual(individuals[i]),getIndividual(individuals[j]));
					Collection<OWLAxiom> originalAxioms = m_positiveFacts_map.get(atom);
					if (originalAxioms == null) {
						originalAxioms = new ArrayList<OWLAxiom>();
						m_positiveFacts_map.put(atom,originalAxioms);
					}
					if (originalAxiom != null) 
						originalAxioms.add(originalAxiom);
				}
		}
		public void visit(OWLClassAssertionAxiom object) {
			OWLClassExpression description=object.getClassExpression();
			if (description instanceof OWLClass) {
				AtomicConcept atomicConcept=AtomicConcept.create(((OWLClass)description).getIRI().toString());
				Atom atom = Atom.create(atomicConcept,getIndividual(object.getIndividual()));
				Collection<OWLAxiom> originalAxioms = m_positiveFacts_map.get(atom);
				if (originalAxioms == null) {
					originalAxioms = new ArrayList<OWLAxiom>();
					m_positiveFacts_map.put(atom,originalAxioms);
				}
				if (originalAxiom != null) 
					originalAxioms.add(originalAxiom);
			}
			else if (description instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)description).getOperand() instanceof OWLClass) {
				AtomicConcept atomicConcept=AtomicConcept.create(((OWLClass)((OWLObjectComplementOf)description).getOperand()).getIRI().toString());
				Atom atom = Atom.create(atomicConcept,getIndividual(object.getIndividual()));
				Collection<OWLAxiom> originalAxioms = m_negativeFacts_map.get(atom);
				if (originalAxioms == null) {
					originalAxioms = new ArrayList<OWLAxiom>();
					m_negativeFacts_map.put(atom,originalAxioms);
				}
				if (originalAxiom != null) 
					originalAxioms.add(originalAxiom);
			}
			else if (description instanceof OWLObjectHasSelf) {
				OWLObjectHasSelf self=(OWLObjectHasSelf)description;
				Atom atom = getRoleAtom(self.getProperty(),getIndividual(object.getIndividual()),getIndividual(object.getIndividual()));
				Collection<OWLAxiom> originalAxioms = m_positiveFacts_map.get(atom);
				if (originalAxioms == null) {
					originalAxioms = new ArrayList<OWLAxiom>();
					m_positiveFacts_map.put(atom,originalAxioms);
				}
				if (originalAxiom != null) 
					originalAxioms.add(originalAxiom);
			}
			else if (description instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)description).getOperand() instanceof OWLObjectHasSelf) {
				OWLObjectHasSelf self=(OWLObjectHasSelf)(((OWLObjectComplementOf)description).getOperand());
				Atom atom = getRoleAtom(self.getProperty(),getIndividual(object.getIndividual()),getIndividual(object.getIndividual()));
				Collection<OWLAxiom> originalAxioms = m_negativeFacts_map.get(atom);
				if (originalAxioms == null) {
					originalAxioms = new ArrayList<OWLAxiom>();
					m_negativeFacts_map.put(atom,originalAxioms);
				}
				if (originalAxiom != null) 
					originalAxioms.add(originalAxiom);
			}
			else
				throw new IllegalStateException("Internal error: invalid normal form.");
		}
		public void visit(OWLObjectPropertyAssertionAxiom object) {
			Atom atom = getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),getIndividual(object.getObject()));
			Collection<OWLAxiom> originalAxioms = m_positiveFacts_map.get(atom);
			if (originalAxioms == null) {
				originalAxioms = new ArrayList<OWLAxiom>();
				m_positiveFacts_map.put(atom,originalAxioms);
			}
			if (originalAxiom != null) 
				originalAxioms.add(originalAxiom);
		}
		public void visit(OWLNegativeObjectPropertyAssertionAxiom object) {
			Atom atom = getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),getIndividual(object.getObject()));
			Collection<OWLAxiom> originalAxioms = m_negativeFacts_map.get(atom);
			if (originalAxioms == null) {
				originalAxioms = new ArrayList<OWLAxiom>();
				m_negativeFacts_map.put(atom,originalAxioms);
			}
			if (originalAxiom != null) 
				originalAxioms.add(originalAxiom);
		}
		public void visit(OWLDataPropertyAssertionAxiom object) {
			Constant targetValue=(Constant)object.getObject().accept(m_dataRangeConverter);
			Atom atom = getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),targetValue);
			Collection<OWLAxiom> originalAxioms = m_positiveFacts_map.get(atom);
			if (originalAxioms == null) {
				originalAxioms = new ArrayList<OWLAxiom>();
				m_positiveFacts_map.put(atom,originalAxioms);
			}
			if (originalAxiom != null) 
				originalAxioms.add(originalAxiom);
		}
		public void visit(OWLNegativeDataPropertyAssertionAxiom object) {
			Constant targetValue=(Constant)object.getObject().accept(m_dataRangeConverter);
			Atom atom = getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),targetValue);
			Collection<OWLAxiom> originalAxioms = m_negativeFacts_map.get(atom);
			if (originalAxioms == null) {
				originalAxioms = new ArrayList<OWLAxiom>();
				m_negativeFacts_map.put(atom,originalAxioms);
			}
			if (originalAxiom != null) 
				originalAxioms.add(originalAxiom);
		}
	}

	protected final static class NormalizedRuleClausifier_withMaps implements SWRLObjectVisitorEx<Atom> {
		protected final Set<OWLObjectProperty> m_objectPropertiesOccurringInOWLAxioms;
		protected final DataRangeConverter m_dataRangeConverter;
		//        protected final Set<DLClause> m_dlClauses;
		protected final Map<DLClause,Collection<OWLAxiom>> m_dlClauses_map;
		protected final List<Atom> m_headAtoms;
		protected final List<Atom> m_bodyAtoms;
		protected final Set<Variable> m_abstractVariables;
		protected boolean m_containsObjectProperties;
		protected boolean m_containsNonGraphObjectProperties;
		protected boolean m_containsUndeterminedObjectProperties;

		public NormalizedRuleClausifier_withMaps(Set<OWLObjectProperty> objectPropertiesOccurringInOWLAxioms,DataRangeConverter dataRangeConverter,Map<DLClause,Collection<OWLAxiom>> dlClauses) {
			m_objectPropertiesOccurringInOWLAxioms=objectPropertiesOccurringInOWLAxioms;
			m_dataRangeConverter=dataRangeConverter;
			m_dlClauses_map=dlClauses;
			m_headAtoms=new ArrayList<Atom>();
			m_bodyAtoms=new ArrayList<Atom>();
			m_abstractVariables=new HashSet<Variable>();
		}
		public void processRules(Map<OWLAxiom,Collection<OWLAxioms.DisjunctiveRule>> rules_map) {
			HashMap<OWLAxiom,Collection<OWLAxioms.DisjunctiveRule>> unprocessedRules_map=new HashMap<OWLAxiom,Collection<OWLAxioms.DisjunctiveRule>>(rules_map);
			boolean changed=true;
			while (!unprocessedRules_map.isEmpty() && changed) {
				changed=false;
				Iterator<Entry<OWLAxiom,Collection<OWLAxioms.DisjunctiveRule>>> iterator=unprocessedRules_map.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<OWLAxiom,Collection<OWLAxioms.DisjunctiveRule>> entry = iterator.next(); 
					Collection<OWLAxioms.DisjunctiveRule> rules=entry.getValue();
					for (OWLAxioms.DisjunctiveRule rule : rules) {
						determineRuleType(rule);
						determineUndeterminedObjectProperties(rule);
						if (!m_containsUndeterminedObjectProperties) {
							iterator.remove();
							clausify(rule,m_containsNonGraphObjectProperties || !m_containsObjectProperties, entry.getKey());
							changed=true;
						}
					}
				}
			}
			m_containsObjectProperties=false;
			m_containsNonGraphObjectProperties=true;
			m_containsUndeterminedObjectProperties=false;
			for (Entry<OWLAxiom,Collection<OWLAxioms.DisjunctiveRule>> entry : unprocessedRules_map.entrySet()) {
				Collection<OWLAxioms.DisjunctiveRule> rules = entry.getValue();
				for (OWLAxioms.DisjunctiveRule rule : rules) {
					determineUndeterminedObjectProperties(rule);
					clausify(rule,true,entry.getKey());
				}
			}
		}
		protected void determineRuleType(OWLAxioms.DisjunctiveRule rule) {
			m_containsObjectProperties=false;
			m_containsNonGraphObjectProperties=false;
			m_containsUndeterminedObjectProperties=false;
			for (SWRLAtom atom : rule.m_body)
				checkRuleAtom(atom);
			for (SWRLAtom atom : rule.m_head)
				checkRuleAtom(atom);
		}
		protected void checkRuleAtom(SWRLAtom atom) {
			if (atom instanceof SWRLObjectPropertyAtom) {
				m_containsObjectProperties=true;
				OWLObjectProperty objectProperty=((SWRLObjectPropertyAtom)atom).getPredicate().getNamedProperty();
				boolean isNonGraphObjectProperty=m_objectPropertiesOccurringInOWLAxioms.contains(objectProperty);
				if (isNonGraphObjectProperty)
					m_containsNonGraphObjectProperties=true;
				else
					m_containsUndeterminedObjectProperties=true;
			}
		}
		protected void determineUndeterminedObjectProperties(OWLAxioms.DisjunctiveRule rule) {
			if (m_containsUndeterminedObjectProperties) {
				if (m_containsNonGraphObjectProperties) {
					for (SWRLAtom atom : rule.m_body)
						makeNonGraphObjectProperty(atom);
					for (SWRLAtom atom : rule.m_head)
						makeNonGraphObjectProperty(atom);
					m_containsUndeterminedObjectProperties=false;
				}
			}
		}
		protected void makeNonGraphObjectProperty(SWRLAtom atom) {
			if (atom instanceof SWRLObjectPropertyAtom) {
				OWLObjectProperty objectProperty=((SWRLObjectPropertyAtom)atom).getPredicate().getNamedProperty();
				m_objectPropertiesOccurringInOWLAxioms.add(objectProperty);
			}
		}
		protected void clausify(OWLAxioms.DisjunctiveRule rule,boolean restrictToNamed, OWLAxiom originalAxiom) {
			m_headAtoms.clear();
			m_bodyAtoms.clear();
			m_abstractVariables.clear();
			for (SWRLAtom atom : rule.m_body)
				m_bodyAtoms.add(atom.accept(this));
			for (SWRLAtom atom : rule.m_head)
				m_headAtoms.add(atom.accept(this));
			if (restrictToNamed) {
				for (Variable variable : m_abstractVariables)
					m_bodyAtoms.add(Atom.create(AtomicConcept.INTERNAL_NAMED,variable));
			}
			DLClause dlClause=DLClause.create(m_headAtoms.toArray(new Atom[m_headAtoms.size()]),m_bodyAtoms.toArray(new Atom[m_bodyAtoms.size()]));

			Collection<OWLAxiom> originalAxioms = m_dlClauses_map.get(dlClause);
			if (originalAxioms == null) {
				originalAxioms = new ArrayList<OWLAxiom>();
				m_dlClauses_map.put(dlClause,originalAxioms);
			}
			originalAxioms.add(originalAxiom);

			m_headAtoms.clear();
			m_bodyAtoms.clear();
			m_abstractVariables.clear();
		}
		public Atom visit(SWRLClassAtom atom) {
			if (atom.getPredicate().isAnonymous())
				throw new IllegalStateException("Internal error: SWRL rule class atoms should be normalized to contain only named classes, but this class atom has a complex concept: "+atom.getPredicate());
			Variable variable=toVariable(atom.getArgument());
			m_abstractVariables.add(variable);
			return Atom.create(AtomicConcept.create(atom.getPredicate().asOWLClass().getIRI().toString()),variable);
		}
		public Atom visit(SWRLDataRangeAtom atom) {
			throw new IllegalAccessError("this construct is not supported and should have been eliminated during the normalization phase");
			//            Variable variable=toVariable(atom.getArgument());
			//            LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(atom.getPredicate());
			//            return Atom.create((DLPredicate)literalRange,variable);
		}
		public Atom visit(SWRLObjectPropertyAtom atom) {
			Variable variable1=toVariable(atom.getFirstArgument());
			Variable variable2=toVariable(atom.getSecondArgument());
			m_abstractVariables.add(variable1);
			m_abstractVariables.add(variable2);
			return getRoleAtom(atom.getPredicate().asOWLObjectProperty(),variable1,variable2);
		}
		public Atom visit(SWRLDataPropertyAtom atom) {
			Variable variable1=toVariable(atom.getFirstArgument());
			Variable variable2=toVariable(atom.getSecondArgument());
			m_abstractVariables.add(variable1);
			return getRoleAtom(atom.getPredicate().asOWLDataProperty(),variable1,variable2);
		}
		public Atom visit(SWRLSameIndividualAtom atom) {
			Variable variable1=toVariable(atom.getFirstArgument());
			Variable variable2=toVariable(atom.getSecondArgument());
			return Atom.create(Equality.INSTANCE,variable1,variable2);
		}
		public Atom visit(SWRLDifferentIndividualsAtom atom) {
			Variable variable1=toVariable(atom.getFirstArgument());
			Variable variable2=toVariable(atom.getSecondArgument());
			return Atom.create(Inequality.INSTANCE,variable1,variable2);
		}
		public Atom visit(SWRLBuiltInAtom node) {
			throw new UnsupportedOperationException("Rules with SWRL built-in atoms are not yet supported. ");
		}
		public Atom visit(SWRLRule rule) {
			throw new IllegalStateException("Internal error: this part of the code is unused.");
		}
		public Atom visit(SWRLVariable node) {
			throw new IllegalStateException("Internal error: this part of the code is unused.");
		}
		public Atom visit(SWRLIndividualArgument atom) {
			throw new IllegalStateException("Internal error: this part of the code is unused.");
		}
		public Atom visit(SWRLLiteralArgument arg) {
			throw new IllegalStateException("Internal error: this part of the code is unused.");
		}
		protected static Variable toVariable(SWRLIArgument argument) {
			if (argument instanceof SWRLVariable)
				return Variable.create(((SWRLVariable)argument).getIRI().toString());
			else
				throw new IllegalStateException("Internal error: all arguments in a SWRL rule should have been normalized to variables.");
		}
		protected static Variable toVariable(SWRLDArgument argument) {
			if (argument instanceof SWRLVariable)
				return Variable.create(((SWRLVariable)argument).getIRI().toString());
			else
				throw new IllegalStateException("Internal error: all arguments in a SWRL rule should have been normalized to variables.");
		}
	}

	public static class Equality_sameAs implements DLPredicate,Serializable {
		private static final long serialVersionUID=8308051741088513244L;

		public static final Equality_sameAs INSTANCE=new Equality_sameAs();

		protected Equality_sameAs () { }
		public int getArity() {
			return 2;
		}
		public String toString(Prefixes prefixes) {
			return "==";
		}
		public String toString() {
			return toString(Prefixes.STANDARD_PREFIXES);
		}
	}

	public static class Inequality_differentFrom implements DLPredicate,Serializable {
		private static final long serialVersionUID=296924110684230279L;

		public static final Inequality_differentFrom INSTANCE=new Inequality_differentFrom();

		protected Inequality_differentFrom () { }
		public int getArity() {
			return 2;
		}
		public String toString(Prefixes prefixes) {
			return "!=";
		}
		public String toString() {
			return toString(Prefixes.STANDARD_PREFIXES);
		}
	}
}
