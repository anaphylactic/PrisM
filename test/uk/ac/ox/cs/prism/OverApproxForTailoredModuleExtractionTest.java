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
//		System.out.println(clause.toString());
		
		OverApproxForTailoredModuleExtraction approx = new OverApproxForTailoredModuleExtraction(null, null);
		
		for (DLClause c : approx.avoidLongBodies(set)){
//			System.out.println(c.toString());
		}
		
//		Provided as reference - output varies in each run!
//		<internal:def_shortenBodies#6>(?X) :- <internal:def_shortenBodies#3>(?X), <internal:def_shortenBodies#4>(?X)
//		<A>(?X) :- <internal:def_shortenBodies#6>(?X), <internal:def_shortenBodies#7>(?Y3,?X)
//		<internal:def_shortenBodies#1>(?X) :- <R8>(?X,?Y8), <C5>(?Y5), <R5>(?X,?Y5), <C8>(?Y8)
//		<internal:def_shortenBodies#4>(?X) :- <C6>(?Y6), <R6>(?X,?Y6)
//		<internal:def_shortenBodies#7>(?Y3,?X) :- <internal:def_shortenBodies#2>(?Y3,?X), <internal:def_shortenBodies#5>(?X), <internal:def_shortenBodies#1>(?X), <internal:def_shortenBodies#0>(?X)
//		<internal:def_shortenBodies#0>(?X) :- <R1>(?X,?Y1), <C0>(?Y0), <C1>(?Y1), <R0>(?X,?Y0)
//		<internal:def_shortenBodies#5>(?X) :- <C2>(?Y2), <C4>(?Y4), <R4>(?X,?Y4), <R2>(?X,?Y2)
//		<internal:def_shortenBodies#3>(?X) :- <R7>(?X,?Y7), <C7>(?Y7), <C9>(?Y9), <R9>(?X,?Y9)
//		<internal:def_shortenBodies#2>(?Y3,?X) :- <R3>(?X,?Y3), <B>(?X), <C3>(?Y3)

	}
	
	@Test
	public void avoidLongBodiesTest2() {
		
		int n = 10;
		
		AtomicConcept A = AtomicConcept.create("A");
		AtomicConcept B = AtomicConcept.create("B");
		Variable X = Variable.create("?X");
		
		AtomicConcept[] C = new AtomicConcept[n];
		AtomicRole[] R = new AtomicRole[n];
		AtomicRole[] S = new AtomicRole[n];
		AtomicRole[] T = new AtomicRole[n];
		Variable[] Y = new Variable[n];
		Variable[] Z = new Variable[n];
		Variable[] W = new Variable[n];
		for (int i = 0 ; i<n ; i++){
			C[i] = AtomicConcept.create("C"+i);
			R[i] = AtomicRole.create("R"+i);
			S[i] = AtomicRole.create("S"+i);
			T[i] = AtomicRole.create("T"+i);
			Y[i] = Variable.create("?Y"+i);
			Z[i] = Variable.create("?Z"+i);
			W[i] = Variable.create("?W"+i);
		}
		
		Atom[] head = new Atom[]{Atom.create(A, new Variable[]{X})};
		Atom[] bodyAtoms = new Atom[4*n+1];
		bodyAtoms[0] = Atom.create(B, new Variable[]{X});
		int counter = 1;
		for (int i = 0 ; i<n ; i++){
			bodyAtoms[counter++] = Atom.create(C[i], new Variable[]{Y[i]});
			bodyAtoms[counter++] = Atom.create(R[i], new Variable[]{X,Y[i]});
			bodyAtoms[counter++] = Atom.create(R[i], new Variable[]{Y[i],Z[i]});
			bodyAtoms[counter++] = Atom.create(R[i], new Variable[]{Z[i],W[i]});
		}
		
		
		DLClause clause = DLClause.create(head, bodyAtoms);
		Set<DLClause> set = new HashSet<DLClause>();
		set.add(clause);
//		System.out.println(clause.toString());
		
		OverApproxForTailoredModuleExtraction approx = new OverApproxForTailoredModuleExtraction(null, null);
		
		for (DLClause c : approx.avoidLongBodies(set)){
//			System.out.println(c.toString());
		}
		
//		Provided as reference - output varies in each run!
//		<internal:def_shortenBodies#19>(?X) :- <internal:def_shortenBodies#16>(?X), <internal:def_shortenBodies#9>(?X), <internal:def_shortenBodies#18>(?X), <internal:def_shortenBodies#13>(?X)
//		<internal:def_shortenBodies#10>(?X) :- <R7>(?X,?Y7), <C7>(?Y7), <R7>(?Z7,?W7), <R7>(?Y7,?Z7)
//		<A>(?X) :- <internal:def_shortenBodies#19>(?X), <internal:def_shortenBodies#21>(?Y6,?X), <internal:def_shortenBodies#20>(?Y6,?X)
//		<internal:def_shortenBodies#16>(?X) :- <R4>(?X,?Y4), <C4>(?Y4), <R4>(?Y4,?Z4), <R4>(?Z4,?W4)
//		<internal:def_shortenBodies#17>(?X) :- <C5>(?Y5), <R5>(?Z5,?W5), <R5>(?X,?Y5), <R5>(?Y5,?Z5)
//		<internal:def_shortenBodies#15>(?X) :- <R9>(?X,?Y9), <R9>(?Z9,?W9), <C9>(?Y9), <R9>(?Y9,?Z9)
//		<internal:def_shortenBodies#21>(?Y6,?X) :- <internal:def_shortenBodies#11>(?X), <internal:def_shortenBodies#14>(?Y6), <internal:def_shortenBodies#12>(?X), <internal:def_shortenBodies#15>(?X)
//		<internal:def_shortenBodies#11>(?X) :- <R8>(?X,?Y8), <R8>(?Z8,?W8), <R8>(?Y8,?Z8), <C8>(?Y8)
//		<internal:def_shortenBodies#14>(?Y6) :- <R6>(?Y6,?Z6), <R6>(?Z6,?W6)
//		<internal:def_shortenBodies#20>(?Y6,?X) :- <internal:def_shortenBodies#10>(?X), <internal:def_shortenBodies#17>(?X), <internal:def_shortenBodies#8>(?Y6,?X)
//		<internal:def_shortenBodies#13>(?X) :- <C1>(?Y1), <R1>(?Z1,?W1), <R1>(?Y1,?Z1), <R1>(?X,?Y1)
//		<internal:def_shortenBodies#18>(?X) :- <R2>(?Z2,?W2), <C2>(?Y2), <R2>(?Y2,?Z2), <R2>(?X,?Y2)
//		<internal:def_shortenBodies#8>(?Y6,?X) :- <B>(?X), <R6>(?X,?Y6), <C6>(?Y6)
//		<internal:def_shortenBodies#12>(?X) :- <R3>(?Y3,?Z3), <R3>(?X,?Y3), <C3>(?Y3), <R3>(?Z3,?W3)
//		<internal:def_shortenBodies#9>(?X) :- <R0>(?Y0,?Z0), <C0>(?Y0), <R0>(?X,?Y0), <R0>(?Z0,?W0)

		
	}
	
	@Test
	public void avoidLongBodiesTest3() {
		
		int n = 10;
		
		AtomicConcept A = AtomicConcept.create("A");
		Variable X = Variable.create("?X");
		Individual[] j = new Individual[]{Individual.create("j1"), Individual.create("j2")};
		
		AtomicConcept[] B = new AtomicConcept[n];
		AtomicRole[] R = new AtomicRole[n];
		AtomicRole S = AtomicRole.create("S");

		Variable[] Y = new Variable[n];
		
		for (int i = 0 ; i<n ; i++){
			B[i] = AtomicConcept.create("B"+i);
			R[i] = AtomicRole.create("R"+i);
			Y[i] = Variable.create("?Y"+i);
		}
		
		Atom[] head = new Atom[]{Atom.create(S, j)};
		Atom[] bodyAtoms = new Atom[2*n+1];
		bodyAtoms[0] = Atom.create(A, new Variable[]{X});
		int counter = 1;
		for (int i = 0 ; i<n ; i++){
			bodyAtoms[counter++] = Atom.create(B[i], new Variable[]{Y[i]});
			bodyAtoms[counter++] = Atom.create(R[i], new Variable[]{X,Y[i]});
		}
		
		
		DLClause clause = DLClause.create(head, bodyAtoms);
		Set<DLClause> set = new HashSet<DLClause>();
		set.add(clause);
//		System.out.println(clause.toString());
		
		OverApproxForTailoredModuleExtraction approx = new OverApproxForTailoredModuleExtraction(null, null);
		
		for (DLClause c : approx.avoidLongBodies(set)){
//			System.out.println(c.toString());
		}

//		Provided as reference - output varies in each run!
//		<internal:def_shortenBodies#25>(?X) :- <B1>(?Y1), <B4>(?Y4), <R1>(?X,?Y1), <R4>(?X,?Y4)
//		<internal:def_shortenBodies#28>(?X,?Y5) :- <internal:def_shortenBodies#24>(?X), <internal:def_shortenBodies#25>(?X), <internal:def_shortenBodies#27>(?X), <internal:def_shortenBodies#22>(?Y5,?X)
//		<internal:def_shortenBodies#23>(?X) :- <R7>(?X,?Y7), <B7>(?Y7), <R0>(?X,?Y0), <B0>(?Y0)
//		<internal:def_shortenBodies#26>(?X) :- <B3>(?Y3), <R3>(?X,?Y3)
//		<internal:def_shortenBodies#29>(?X) :- <internal:def_shortenBodies#26>(?X), <internal:def_shortenBodies#23>(?X)
//		<internal:def_shortenBodies#24>(?X) :- <R6>(?X,?Y6), <B8>(?Y8), <R8>(?X,?Y8), <B6>(?Y6)
//		<internal:def_shortenBodies#22>(?Y5,?X) :- <B5>(?Y5), <A>(?X), <R5>(?X,?Y5)
//		<internal:def_shortenBodies#27>(?X) :- <B9>(?Y9), <R9>(?X,?Y9), <R2>(?X,?Y2), <B2>(?Y2)
//		<S>(<j1>,<j2>) :- <internal:def_shortenBodies#28>(?X,?Y5), <internal:def_shortenBodies#29>(?X)
//		<internal:def_shortenBodies#30>(?Y0,?Y9) :- <R>(?Y6,?Y7), <R>(?Y1,?Y2), <R>(?Y3,?Y4), <R>(?Y7,?Y8), <R>(?Y0,?Y1), <R>(?Y5,?Y6), <R>(?Y4,?Y5), <R>(?Y2,?Y3), <R>(?Y8,?Y9)
//		<R>(?Y0,?Y9) :- <internal:def_shortenBodies#30>(?Y0,?Y9)

	}
	
	@Test
	public void avoidLongBodiesTest4() {
		
		int n = 10;
		
		AtomicRole R = AtomicRole.create("R");

		Variable[] Y = new Variable[n];
		
		for (int i = 0 ; i<n ; i++)
			Y[i] = Variable.create("?Y"+i);
		
		Atom[] head = new Atom[]{Atom.create(R, new Variable[]{Y[0],Y[n-1]})};
		Atom[] bodyAtoms = new Atom[n-1];
		int counter = 0;
		for (int i = 0 ; i<(n-1) ; i++)
			bodyAtoms[counter++] = Atom.create(R, new Variable[]{Y[i],Y[i+1]});
		
		DLClause clause = DLClause.create(head, bodyAtoms);
		Set<DLClause> set = new HashSet<DLClause>();
		set.add(clause);
//		System.out.println(clause.toString());
		
		OverApproxForTailoredModuleExtraction approx = new OverApproxForTailoredModuleExtraction(null, null);
		
		for (DLClause c : approx.avoidLongBodies(set)){
//			System.out.println(c.toString());
		}
		
//		Provided as reference - output varies in each run!
//		<R>(?Y0,?Y9) :- <internal:def_shortenBodies#30>(?Y0,?Y9)
//		<internal:def_shortenBodies#30>(?Y0,?Y9) :- <R>(?Y1,?Y2), <R>(?Y2,?Y3), <R>(?Y8,?Y9), <R>(?Y5,?Y6), <R>(?Y3,?Y4), <R>(?Y6,?Y7), <R>(?Y7,?Y8), <R>(?Y0,?Y1), <R>(?Y4,?Y5)


	}

}
