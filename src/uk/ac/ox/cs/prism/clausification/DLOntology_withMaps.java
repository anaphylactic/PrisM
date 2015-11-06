package uk.ac.ox.cs.prism.clausification;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;

public class DLOntology_withMaps{
	
	protected final Map<DLClause,Collection<OWLAxiom>> m_dlClauses_map;
	protected final Map<Atom,Collection<OWLAxiom>> m_positiveFacts_map;
	protected final Map<Atom,Collection<OWLAxiom>> m_negativeFacts_map;
	protected final Collection<OWLLiteral> m_literals;

    public DLOntology_withMaps(
    		Map<DLClause,Collection<OWLAxiom>> dlClauses,
    		Map<Atom,Collection<OWLAxiom>> positiveFacts,
    		Map<Atom,Collection<OWLAxiom>> negativeFacts,
    		Collection<OWLLiteral> literals) {
        m_dlClauses_map=dlClauses;
        m_positiveFacts_map=positiveFacts;
        m_negativeFacts_map=negativeFacts;
        m_literals = literals;
    }
    
    public Map<DLClause,Collection<OWLAxiom>> getDLClausesMap() {
        return m_dlClauses_map;
    }
    public Map<Atom,Collection<OWLAxiom>> getPositiveFactsMap() {
        return m_positiveFacts_map;
    }
    public Map<Atom,Collection<OWLAxiom>> getNegativeFactsMap() {
        return m_negativeFacts_map;
    }
    public Collection<OWLLiteral> getLiterals() {
        return m_literals;
    }

    public Collection<OWLAxiom> getCorrespondingAxioms(DLClause clause){
    	Collection<OWLAxiom> ret = m_dlClauses_map.get(clause);
    	return (ret == null) ? new HashSet<OWLAxiom>() : ret;
    }
    
}
