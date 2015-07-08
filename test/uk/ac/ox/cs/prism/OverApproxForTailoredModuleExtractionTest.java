package uk.ac.ox.cs.prism;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Variable;

import uk.ac.ox.cs.prism.OverApproxForTailoredModuleExtraction;

public class OverApproxForTailoredModuleExtractionTest {

	@Test
	public void avoidLongBodiesTest() {
		
		int n = 10;
		
		AtomicConcept A = AtomicConcept.create("A");
		AtomicConcept B = AtomicConcept.create("B");
		Variable X = Variable.create("?X");
		
		AtomicConcept[] C = new AtomicConcept[n];
		AtomicRole[] R = new AtomicRole[n];
		Variable[] Y = new Variable[n];
		for (int i = 0 ; i<n ; i++){
			C[i] = AtomicConcept.create("C"+i);
			R[i] = AtomicRole.create("R"+i);
			Y[i] = Variable.create("?Y"+i);
		}
		
		Atom[] head = new Atom[]{Atom.create(A, new Variable[]{X})};
		Atom[] bodyAtoms = new Atom[2*n+1];
		bodyAtoms[0] = Atom.create(B, new Variable[]{X});
		int counter = 1;
		for (int i = 0 ; i<n ; i++){
			bodyAtoms[counter++] = Atom.create(R[i], new Variable[]{X,Y[i]});
			bodyAtoms[counter++] = Atom.create(C[i], new Variable[]{Y[i]});
		}
		
		
		DLClause clause = DLClause.create(head, bodyAtoms);
		Set<DLClause> set = new HashSet<DLClause>();
		set.add(clause);
		System.out.println(clause.toString());
		
		OverApproxForTailoredModuleExtraction approx = new OverApproxForTailoredModuleExtraction(null);
		
		for (DLClause c : approx.avoidLongBodies(set))
			System.out.println(c.toString());

	}
	
//	@Test
//	public void avoidLongBodiesTest2() {
//		
//		int n = 10;
//		
//		AtomicConcept A = AtomicConcept.create("A");
//		AtomicConcept B = AtomicConcept.create("B");
//		Variable X = Variable.create("?X");
//		
//		AtomicConcept[] C = new AtomicConcept[n];
//		AtomicRole[] R = new AtomicRole[n];
//		AtomicRole[] S = new AtomicRole[n];
//		AtomicRole[] T = new AtomicRole[n];
//		Variable[] Y = new Variable[n];
//		Variable[] Z = new Variable[n];
//		Variable[] W = new Variable[n];
//		for (int i = 0 ; i<n ; i++){
//			C[i] = AtomicConcept.create("C"+i);
//			R[i] = AtomicRole.create("R"+i);
//			S[i] = AtomicRole.create("S"+i);
//			T[i] = AtomicRole.create("T"+i);
//			Y[i] = Variable.create("?Y"+i);
//			Z[i] = Variable.create("?Z"+i);
//			W[i] = Variable.create("?W"+i);
//		}
//		
//		Atom[] head = new Atom[]{Atom.create(A, new Variable[]{X})};
//		Atom[] bodyAtoms = new Atom[4*n+1];
//		bodyAtoms[0] = Atom.create(B, new Variable[]{X});
//		int counter = 1;
//		for (int i = 0 ; i<n ; i++){
//			bodyAtoms[counter++] = Atom.create(C[i], new Variable[]{Y[i]});
//			bodyAtoms[counter++] = Atom.create(R[i], new Variable[]{X,Y[i]});
//			bodyAtoms[counter++] = Atom.create(R[i], new Variable[]{Y[i],Z[i]});
//			bodyAtoms[counter++] = Atom.create(R[i], new Variable[]{Z[i],W[i]});
//		}
//		
//		
//		DLClause clause = DLClause.create(head, bodyAtoms);
//		Set<DLClause> set = new HashSet<DLClause>();
//		set.add(clause);
//		System.out.println(clause.toString());
//		
//		OverApproxForTailoredModuleExtraction approx = new OverApproxForTailoredModuleExtraction(null);
//		
//		for (DLClause c : approx.avoidLongBodies(set))
//			System.out.println(c.toString());
//
//	}
	
//	@Test
//	public void avoidLongBodiesTest3() {
//		
//		int n = 10;
//		
//		AtomicConcept A = AtomicConcept.create("A");
//		Variable X = Variable.create("?X");
//		Individual[] j = new Individual[]{Individual.create("j1"), Individual.create("j2")};
//		
//		AtomicConcept[] B = new AtomicConcept[n];
//		AtomicRole[] R = new AtomicRole[n];
//		AtomicRole S = AtomicRole.create("S");
//
//		Variable[] Y = new Variable[n];
//		
//		for (int i = 0 ; i<n ; i++){
//			B[i] = AtomicConcept.create("B"+i);
//			R[i] = AtomicRole.create("R"+i);
//			Y[i] = Variable.create("?Y"+i);
//		}
//		
//		Atom[] head = new Atom[]{Atom.create(S, j)};
//		Atom[] bodyAtoms = new Atom[2*n+1];
//		bodyAtoms[0] = Atom.create(A, new Variable[]{X});
//		int counter = 1;
//		for (int i = 0 ; i<n ; i++){
//			bodyAtoms[counter++] = Atom.create(B[i], new Variable[]{Y[i]});
//			bodyAtoms[counter++] = Atom.create(R[i], new Variable[]{X,Y[i]});
//		}
//		
//		
//		DLClause clause = DLClause.create(head, bodyAtoms);
//		Set<DLClause> set = new HashSet<DLClause>();
//		set.add(clause);
//		System.out.println(clause.toString());
//		
//		OverApproxForTailoredModuleExtraction approx = new OverApproxForTailoredModuleExtraction(null);
//		
//		for (DLClause c : approx.avoidLongBodies(set))
//			System.out.println(c.toString());
//
//	}
	
//	@Test
//	public void avoidLongBodiesTest4() {
//		
//		int n = 10;
//		
//		AtomicRole R = AtomicRole.create("R");
//
//		Variable[] Y = new Variable[n];
//		
//		for (int i = 0 ; i<n ; i++)
//			Y[i] = Variable.create("?Y"+i);
//		
//		Atom[] head = new Atom[]{Atom.create(R, new Variable[]{Y[0],Y[n-1]})};
//		Atom[] bodyAtoms = new Atom[n-1];
//		int counter = 0;
//		for (int i = 0 ; i<(n-1) ; i++)
//			bodyAtoms[counter++] = Atom.create(R, new Variable[]{Y[i],Y[i+1]});
//		
//		DLClause clause = DLClause.create(head, bodyAtoms);
//		Set<DLClause> set = new HashSet<DLClause>();
//		set.add(clause);
//		System.out.println(clause.toString());
//		
//		OverApproxForTailoredModuleExtraction approx = new OverApproxForTailoredModuleExtraction(null);
//		
//		for (DLClause c : approx.avoidLongBodies(set))
//			System.out.println(c.toString());
//
//	}

}
