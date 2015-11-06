package uk.ac.ox.cs.prism.util;

import java.io.File;
import java.io.PrintWriter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.store.DataStore;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;
import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.util.Namespace;

public class Utility_PrisM{

	protected static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

	public static void print(PrintWriter output, String objectIRI) {
		if (!objectIRI.startsWith("<"))
			objectIRI = "<" + objectIRI;
		if (!objectIRI.endsWith(">"))
			objectIRI = objectIRI + ">";
		output.print(objectIRI);
	}
	public static void print(PrintWriter output, OWLClass owlClass) {
		print(output, owlClass.getIRI().toString());
	}
	public static void print(PrintWriter output, Individual i) {
		print(output, i.getIRI().toString());
	}
	public static void print(PrintWriter output, OWLObjectPropertyExpression propertyExpression, Individual argument1, Individual argument2) throws Exception {
		if (propertyExpression instanceof OWLObjectProperty) {
			print(output, argument1);
			output.print(' ');
			print(output, ((OWLObjectProperty)propertyExpression).getIRI().toString());
			output.print(' ');
			print(output, argument2);
			output.println(" .");
		}
		else if (propertyExpression instanceof OWLObjectInverseOf)
			print(output, ((OWLObjectInverseOf)propertyExpression).getInverseProperty().getSimplified(), argument2, argument1);
		else
			throw new Exception("Unsupported property.");
	}
	public static void print(PrintWriter output, OWLClass owlClass, Individual argument) throws Exception {
		print(output, argument);
		output.print(' ');
		print(output, RDF_TYPE);
		output.print(' ');
		print(output, (owlClass).getIRI().toString());
		output.println(" .");
	}
	public static void print(PrintWriter output, String propertyIRI, Individual argument1, Individual argument2) throws Exception {
		print(output, argument1);
		output.print(' ');
		print(output, propertyIRI);
		output.print(' ');
		print(output, argument2);
		output.println(" .");
	}
	public static void print(PrintWriter output, String classIRI, Individual argument) throws Exception {
		print(output, argument);
		output.print(' ');
		print(output, RDF_TYPE);
		output.print(' ');
		print(output, classIRI);
		output.println(" .");
	}
	public static void print(PrintWriter output, String propertyIRI, String argument1, String argument2) throws Exception {
		print(output, argument1);
		output.print(' ');
		print(output, propertyIRI);
		output.print(' ');
		print(output, argument2);
		output.println(" .");
	}
	public static void print(PrintWriter output, String classIRI, String argument) throws Exception {
		print(output, argument);
		output.print(' ');
		print(output, RDF_TYPE);
		output.print(' ');
		print(output, classIRI);
		output.println(" .");
	}


	/////////////////
	public static String print(String objectIRI) {
		if (!objectIRI.startsWith("<"))
			objectIRI = "<" + objectIRI;
		if (!objectIRI.endsWith(">"))
			objectIRI = objectIRI + ">";
		return objectIRI;
	}
	public static String print(OWLClass owlClass) {
		return print(owlClass.getIRI().toString());
	}
	public static String print(Individual i) {
		return print(i.getIRI().toString());
	}
	public static String print(Constant c) {
		StringBuffer sb = new StringBuffer(c.toString());
		String datatype = sb.substring(sb.lastIndexOf("^")+1,sb.length());
		String ret = sb.toString().replace(datatype, "<" + MyPrefixes.PAGOdAPrefixes.expandIRI(datatype) + ">");
		return ret;
	}
	public static String print(OWLObjectPropertyExpression propertyExpression, Individual argument1, Individual argument2){
		if (propertyExpression instanceof OWLObjectProperty) {
			String ret = print(argument1);
			ret = ret + ' ';
			ret = ret + print(((OWLObjectProperty)propertyExpression).getIRI().toString());
			ret = ret + ' ';
			ret = ret + print(argument2);
			ret = ret + " .\n";
			return ret;
		}
		else if (propertyExpression instanceof OWLObjectInverseOf)
			return print(((OWLObjectInverseOf)propertyExpression).getInverseProperty().getSimplified(), argument2, argument1);
		else
			return "";
//			throw new Exception("Unsupported property.");
	}
	public static String print(OWLClass owlClass, Individual argument){
		String ret = print(argument);
		ret = ret + ' ';
		ret = ret + print(RDF_TYPE);
		ret = ret + ' ';
		ret = ret + print((owlClass).getIRI().toString());
		ret = ret + " .\n";
		return ret;
	}
	public static String print(String propertyIRI, Individual argument1, Individual argument2){
		String ret = print(argument1);
		ret = ret + ' ';
		ret = ret + print(propertyIRI);
		ret = ret + ' ';
		ret = ret + print(argument2);
		ret = ret + " .\n";
		return ret;
	}
	public static String print(String propertyIRI, Constant argument1, Constant argument2){
		String ret = print(argument1);
		ret = ret + ' ';
		ret = ret + print(propertyIRI);
		ret = ret + ' ';
		ret = ret + print(argument2);
		ret = ret + " .\n";
		return ret;
	}
	public static String print(String classIRI, Individual argument){
		String ret = print(argument);
		ret = ret + ' ';
		ret = ret + print(RDF_TYPE);
		ret = ret + ' ';
		ret = ret + print(classIRI);
		ret = ret + " .\n";
		return ret;
	}
	public static String print(String propertyIRI, String argument1, String argument2){
		String ret = print(argument1);
		ret = ret + ' ';
		ret = ret + print(propertyIRI);
		ret = ret + ' ';
		ret = ret + print(argument2);
		ret = ret + " .\n";
		return ret;
	}
	public static String print(String classIRI, String argument){
		String ret = print(argument);
		ret = ret + ' ';
		ret = ret + print(RDF_TYPE);
		ret = ret + ' ';
		ret = ret + print(classIRI);
		ret = ret + " .\n";
		return ret;
	}
	
	
	
	
	protected static final Logger logger = Logger.getLogger("org.semanticweb.tmextractor");
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	private static String getLogMessage(Object[] messages) {
		StringBuilder logMessage = new StringBuilder();
		if (messages.length == 1) return messages[0].toString(); 
		else {
			logMessage.setLength(0);
			for (int i = 0; i < messages.length; ++i) { 
				if (logMessage.length() != 0)
					logMessage.append(LINE_SEPARATOR); 
				logMessage.append(messages[i]); 
			}
			return logMessage.toString(); 		
		}
	}
	public static void logInfo(Object... messages){
		logger.info(getLogMessage(messages));
	}	
	public static void logDebug(Object... messages){
		logger.debug(getLogMessage(messages));
	}
	public static void logError(Object... messages){
		logger.error(getLogMessage(messages));
	}
	public static void logWarn(Object... messages){
		logger.warn(getLogMessage(messages));
	}
	public static void logTrace(Object... messages){
		logger.trace(getLogMessage(messages));
	}
	public static Level getLoggerLevel(){
		return logger.getLevel();
	}
	public static void setLoggerLevel(Level level){
		logger.setLevel(level);
	}

	
	public static void printAllTriples(DataStore store){
		TupleIterator iter = null;
		try{
			String query = "select ?x ?y ?z where { ?x ?y ?z }";
			iter = store.compileQuery(query);
			for (long multi = iter.open() ; multi != 0 ; multi = iter.getNext()){
				String t1 = iter.getGroundTerm(0).toString();
				String t2 = iter.getGroundTerm(1).toString();
				String t3 = iter.getGroundTerm(2).toString();
//				if (!(t2.equals(MyPrefixes.PAGOdAPrefixes.expandText("owl:sameAs")) && t1.equals(t3)))
					logger.info(t1 + " " + t2 + " " + t3 + " .");
			}
		}
		catch (JRDFStoreException e){
			e.printStackTrace();
		}
		finally{
			if (iter != null) iter.dispose();
		}
	}
	
	public static String removeAngleBrackets(String s){
		return s.replace("<", "").replace(">", "");
	}
	
	public static void loadDatasetAsABox(String ttlFile, OWLOntologyManager manager, OWLOntology o){
		OWLDataFactory factory = new OWLDataFactoryImpl(); 
		try {
			DataStore store = new DataStore(DataStore.StoreType.SequentialHead);
			store.importFiles(new File[]{new File(ttlFile)});
			TupleIterator iter = null;
			try{
				iter = store.compileQuery("select ?x ?y ?z where { ?x ?y ?z }");
				for (long multi = iter.open() ; multi != 0 ; multi = iter.getNext()){
					String t1 = iter.getGroundTerm(0).toString();
					String t2 = iter.getGroundTerm(1).toString();
					String t3 = iter.getGroundTerm(2).toString();
					if (Utility_PrisM.removeAngleBrackets(t2).equals(Namespace.RDF_TYPE)){
						OWLClass c = factory.getOWLClass(IRI.create(Utility_PrisM.removeAngleBrackets(t3)));
						OWLIndividual i = factory.getOWLNamedIndividual(IRI.create(Utility_PrisM.removeAngleBrackets(t1)));
						manager.addAxiom(o, factory.getOWLClassAssertionAxiom(c, i));
					}
					else if (Utility_PrisM.removeAngleBrackets(t2).equals(Namespace.EQUALITY)){
						OWLIndividual i = factory.getOWLNamedIndividual(IRI.create(Utility_PrisM.removeAngleBrackets(t1)));
						OWLIndividual j = factory.getOWLNamedIndividual(IRI.create(Utility_PrisM.removeAngleBrackets(t3)));
						System.out.println("Equality Assertions in the dataset..." + t1+t2+t3);
						manager.addAxiom(o, factory.getOWLSameIndividualAxiom(i,j));
					}
					else if (Utility_PrisM.removeAngleBrackets(t2).equals(Namespace.INEQUALITY)){
						OWLIndividual i = factory.getOWLNamedIndividual(IRI.create(Utility_PrisM.removeAngleBrackets(t1)));
						OWLIndividual j = factory.getOWLNamedIndividual(IRI.create(Utility_PrisM.removeAngleBrackets(t3)));
						System.out.println("Inequality Assertions in the dataset..." + t1+t2+t3);
						manager.addAxiom(o, factory.getOWLDifferentIndividualsAxiom(i,j));
					}
					else {
						OWLIndividual i = factory.getOWLNamedIndividual(IRI.create(Utility_PrisM.removeAngleBrackets(t1)));
						OWLIndividual j = factory.getOWLNamedIndividual(IRI.create(Utility_PrisM.removeAngleBrackets(t3)));
						OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create(Utility_PrisM.removeAngleBrackets(t2)));
						manager.addAxiom(o, factory.getOWLObjectPropertyAssertionAxiom(r, i, j));
					}
				}
			}
			catch (JRDFStoreException e){
				e.printStackTrace();
			}
			finally{
				if (iter != null) iter.dispose();
			}
			
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		}
	}
}
