package uk.ac.ox.cs.prism.clausification;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.ConstantEnumeration;
import org.semanticweb.HermiT.model.LiteralDataRange;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;

import uk.ac.ox.cs.JRDFox.model.Datatype;
import uk.ac.ox.cs.pagoda.MyPrefixes;

public class DatatypeManager {

	protected Set<Constant> literalConstants = new HashSet<Constant>();

	public boolean isSupported(OWLDataRange range) {
		if (range instanceof OWLDatatype) {
			if (range.isTopDatatype())
				return true;
			String s = ((OWLDatatype) range).toString(); 
			for (Datatype dt : Datatype.values()) {
				if (s.equals(dt.getIRI()))
					return true;
			}
		}
		else if (range instanceof OWLDataOneOf) {
			for (OWLLiteral lit : ((OWLDataOneOf) range).getValues())
				if (!isSupported(lit)) return false;
			return true;
		}
		return false;
	}

	public boolean isSupported(OWLLiteral lit) {
		String datatype = lit.getDatatype().toString();
		for (Datatype dt : Datatype.values()) {
			if (datatype.equals(dt.getIRI())) {
				literalConstants.add(Constant.create(lit.getLiteral(), lit.getDatatype().toString()));
				return true;
			}
		}
		return false;
	}
	
	public Set<Constant> getUsedConstants() {
		return literalConstants;
	}
	
	public Constant[] getConstantsForDataType(LiteralDataRange dataRange, int nLiterals) {
		//if one of the positions in the array is null then we think asking for that many literals 
		//of this particular datatype is a problem and it should propagate a contradiction
		if (dataRange instanceof ConstantEnumeration) {
			if (nLiterals > ((ConstantEnumeration) dataRange).getNumberOfConstants())
				return new Constant[]{((ConstantEnumeration) dataRange).getConstant(0), null};
			else if (nLiterals == 1)
				return new Constant[]{((ConstantEnumeration) dataRange).getConstant(0)};
			else 
				return new Constant[]{
					((ConstantEnumeration) dataRange).getConstant(0), 
					((ConstantEnumeration) dataRange).getConstant(1)};
		}
		else {
			if (notEnoughLiterals(dataRange, nLiterals))
				return new Constant[]{getConstant(dataRange,1), null};
			else if (nLiterals == 1) {
				return new Constant[]{getConstant(dataRange,1)};
			}
			else 
				return new Constant[]{getConstant(dataRange,1), getConstant(dataRange,2)};
		}
	}

	
	
	protected Constant getConstant(LiteralDataRange datatype, int which) {
		String s = MyPrefixes.PAGOdAPrefixes.expandIRI(datatype.toString());
		Datatype dt = Datatype.value(s);
		Constant c;
		switch (dt) {
		case XSD_STRING: //("http://www.w3.org/2001/XMLSchema#string", 3)
			if (which == 1)
				c = Constant.create("a","http://www.w3.org/2001/XMLSchema#string");
			else 
				c = Constant.create("b", "http://www.w3.org/2001/XMLSchema#string");
			break;
		case RDF_PLAIN_LITERAL: //("http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral", 4)
			if (which == 1)
				c = Constant.create("a","http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral");
			else 
				c = Constant.create("b", "http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral");
			break;
		case XSD_INTEGER: //("http://www.w3.org/2001/XMLSchema#integer", 5)
			if (which == 1)
				c = Constant.create("1", "http://www.w3.org/2001/XMLSchema#integer");
			else 
				c = Constant.create("2", "http://www.w3.org/2001/XMLSchema#integer");
			break;
		case XSD_FLOAT: //("http://www.w3.org/2001/XMLSchema#float", 6)
			if (which == 1)
				c = Constant.create("1", "http://www.w3.org/2001/XMLSchema#float");
			else 
				c = Constant.create("2", "http://www.w3.org/2001/XMLSchema#float");
			break;
		case XSD_DOUBLE: //("http://www.w3.org/2001/XMLSchema#double", 7)
			if (which == 1)
				c = Constant.create("1", "http://www.w3.org/2001/XMLSchema#double");
			else 
				c = Constant.create("2", "http://www.w3.org/2001/XMLSchema#double");
			break;
		case XSD_BOOLEAN: //("http://www.w3.org/2001/XMLSchema#boolean", 8)
			if (which == 1)
				c = Constant.create("true", "http://www.w3.org/2001/XMLSchema#boolean");
			else 
				c = Constant.create("false", "http://www.w3.org/2001/XMLSchema#boolean");
			break;
		case XSD_DATE_TIME: //("http://www.w3.org/2001/XMLSchema#dateTime", 9)
			if (which == 1)
				c = Constant.create("2001-10-10T12:00:00", "http://www.w3.org/2001/XMLSchema#dateTime");
			else 
				c = Constant.create("2002-10-10T12:00:00", "http://www.w3.org/2001/XMLSchema#dateTime");
			break;
		case XSD_DATE_TIME_STAMP: //("http://www.w3.org/2001/XMLSchema#dateTimeStamp", XSD_DATE_TIME.value()),
			if (which == 1)
				c = Constant.create("2001-10-10T12:00:00", "http://www.w3.org/2001/XMLSchema#dateTime");
			else 
				c = Constant.create("2002-10-10T12:00:00", "http://www.w3.org/2001/XMLSchema#dateTime");
			break;
		case XSD_TIME: //("http://www.w3.org/2001/XMLSchema#time", 10)
			if (which == 1)
				c = Constant.create("00:00:01", "http://www.w3.org/2001/XMLSchema#time");
			else 
				c = Constant.create("00:00:02", "http://www.w3.org/2001/XMLSchema#time");
			break;
		case XSD_DATE: //("http://www.w3.org/2001/XMLSchema#date", 11)
			if (which == 1)
				c = Constant.create("2001-10-10", "http://www.w3.org/2001/XMLSchema#date");
			else 
				c = Constant.create("2002-10-10", "http://www.w3.org/2001/XMLSchema#date");
			break;
		case XSD_G_YEAR_MONTH: //("http://www.w3.org/2001/XMLSchema#gYearMonth", 12)
			if (which == 1)
				c = Constant.create("2001-10", "http://www.w3.org/2001/XMLSchema#gYearMonth");
			else 
				c = Constant.create("2002-10", "http://www.w3.org/2001/XMLSchema#gYearMonth");
			break;
		case XSD_G_YEAR: //("http://www.w3.org/2001/XMLSchema#gYear", 13),
			if (which == 1)
				c = Constant.create("2001", "http://www.w3.org/2001/XMLSchema#gYear");
			else 
				c = Constant.create("2002", "http://www.w3.org/2001/XMLSchema#gYear");
			break;
		case XSD_G_MONTH_DAY: //("http://www.w3.org/2001/XMLSchema#gMonthDay", 14),
			if (which == 1)
				c = Constant.create("--10-10", "http://www.w3.org/2001/XMLSchema#gMonthDay");
			else 
				c = Constant.create("--11-10", "http://www.w3.org/2001/XMLSchema#gMonthDay");
			break;
		case XSD_G_DAY: //("http://www.w3.org/2001/XMLSchema#gDay", 15),
			if (which == 1)
				c = Constant.create("---10", "http://www.w3.org/2001/XMLSchema#gDay");
			else 
				c = Constant.create("---11", "http://www.w3.org/2001/XMLSchema#gDay");
			break;
		case XSD_G_MONTH: //("http://www.w3.org/2001/XMLSchema#gMonth", 16),
			if (which == 1)
				c = Constant.create("--10", "http://www.w3.org/2001/XMLSchema#gMonth");
			else 
				c = Constant.create("--11", "http://www.w3.org/2001/XMLSchema#gMonth");
			break;
		case XSD_DURATION: //("http://www.w3.org/2001/XMLSchema#duration", 17),
			if (which == 1)
				c = Constant.create("P1Y", "http://www.w3.org/2001/XMLSchema#duration");
			else 
				c = Constant.create("P2Y", "http://www.w3.org/2001/XMLSchema#duration");
			break;
		case XSD_YEAR_MONTH_DURATION: //("http://www.w3.org/2001/XMLSchema#yearMonthDuration", XSD_DURATION.value()),
			if (which == 1)
				c = Constant.create("P1Y", "http://www.w3.org/2001/XMLSchema#duration");
			else 
				c = Constant.create("P2Y", "http://www.w3.org/2001/XMLSchema#duration");
			break;
		case XSD_DAY_TIME_DURATION: //("http://www.w3.org/2001/XMLSchema#dayTimeDuration", XSD_DURATION.value()),
			if (which == 1)
				c = Constant.create("P1D", "http://www.w3.org/2001/XMLSchema#duration");
			else 
				c = Constant.create("P2D", "http://www.w3.org/2001/XMLSchema#duration");
			break;
		case RDF_XML_LITERAL: //("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral", XSD_STRING.value()),
			if (which == 1)
				c = Constant.create("a","http://www.w3.org/2001/XMLSchema#string");
			else 
				c = Constant.create("b", "http://www.w3.org/2001/XMLSchema#string");
			break;		
		case RDFS_LITERAL: //("http://www.w3.org/2000/01/rdf-schema#Literal", XSD_STRING.value()),
			if (which == 1)
				c = Constant.create("a","http://www.w3.org/2001/XMLSchema#string");
			else 
				c = Constant.create("b", "http://www.w3.org/2001/XMLSchema#string");
			break;
		case OWL_REAL: //("http://www.w3.org/2002/07/owl#real", XSD_DOUBLE.value()),
			if (which == 1)
				return Constant.create("1", "http://www.w3.org/2001/XMLSchema#double");
			else 
				return Constant.create("2", "http://www.w3.org/2001/XMLSchema#double");		
		case OWL_RATIONAL: //("http://www.w3.org/2002/07/owl#rational", XSD_DOUBLE.value()),
			if (which == 1)
				c = Constant.create("1/1", "http://www.w3.org/2001/XMLSchema#double");
			else 
				c = Constant.create("2/1", "http://www.w3.org/2001/XMLSchema#double");
			break;		
		case XSD_DECIMAL: //("http://www.w3.org/2001/XMLSchema#decimal", XSD_DOUBLE.value()),
			if (which == 1)
				c = Constant.create("1", "http://www.w3.org/2001/XMLSchema#double");
			else 
				c = Constant.create("2", "http://www.w3.org/2001/XMLSchema#double");
			break;				
		case XSD_NON_NEGATIVE_INTEGER: //("http://www.w3.org/2001/XMLSchema#nonNegativeInteger", XSD_INTEGER.value()),
			if (which == 1)
				c = Constant.create("1", "http://www.w3.org/2001/XMLSchema#integer");
			else 
				c = Constant.create("2", "http://www.w3.org/2001/XMLSchema#integer");
			break;
		case XSD_NON_POSITIVE_INTEGER: //("http://www.w3.org/2001/XMLSchema#nonPositiveInteger", XSD_INTEGER.value()),
			if (which == 1)
				c = Constant.create("-1", "http://www.w3.org/2001/XMLSchema#integer");
			else 
				c = Constant.create("-2", "http://www.w3.org/2001/XMLSchema#integer");
			break;
		case XSD_POSITIVE_INTEGER: //("http://www.w3.org/2001/XMLSchema#positiveInteger", XSD_INTEGER.value()),
			if (which == 1)
				c = Constant.create("1", "http://www.w3.org/2001/XMLSchema#integer");
			else 
				c = Constant.create("2", "http://www.w3.org/2001/XMLSchema#integer");
			break;
		case XSD_NEGATIVE_INTEGER: //("http://www.w3.org/2001/XMLSchema#negativeInteger", XSD_INTEGER.value()),
			if (which == 1)
				c = Constant.create("-1", "http://www.w3.org/2001/XMLSchema#integer");
			else 
				c = Constant.create("-2", "http://www.w3.org/2001/XMLSchema#integer");
			break;
		case XSD_LONG: //("http://www.w3.org/2001/XMLSchema#long", XSD_INTEGER.value()),
			if (which == 1)
				c = Constant.create("1", "http://www.w3.org/2001/XMLSchema#integer");
			else 
				c = Constant.create("2", "http://www.w3.org/2001/XMLSchema#integer");
			break;
		case XSD_INT: //("http://www.w3.org/2001/XMLSchema#int", XSD_INTEGER.value()),
			if (which == 1)
				c = Constant.create("1", "http://www.w3.org/2001/XMLSchema#integer");
			else 
				c = Constant.create("2", "http://www.w3.org/2001/XMLSchema#integer");
			break;
		case XSD_SHORT: //("http://www.w3.org/2001/XMLSchema#short", XSD_INTEGER.value()),
			if (which == 1)
				c = Constant.create("1","http://www.w3.org/2001/XMLSchema#integer");
			else 
				c = Constant.create("2", "http://www.w3.org/2001/XMLSchema#integer");
			break;
		case XSD_BYTE: //("http://www.w3.org/2001/XMLSchema#byte", XSD_INTEGER.value()),
			if (which == 1)
				c = Constant.create("1","http://www.w3.org/2001/XMLSchema#integer");
			else 
				c = Constant.create("2", "http://www.w3.org/2001/XMLSchema#integer");
			break;
		case XSD_UNSIGNED_LONG: //("http://www.w3.org/2001/XMLSchema#unsignedLong", XSD_INTEGER.value()),
			if (which == 1)
				c = Constant.create("1","http://www.w3.org/2001/XMLSchema#integer");
			else 
				c = Constant.create("2", "http://www.w3.org/2001/XMLSchema#integer");
			break;
		case XSD_UNSIGNED_INT: //("http://www.w3.org/2001/XMLSchema#unsignedInt", XSD_INTEGER.value()),
			if (which == 1)
				c = Constant.create("1","http://www.w3.org/2001/XMLSchema#integer");
			else 
				c = Constant.create("2", "http://www.w3.org/2001/XMLSchema#integer");
			break;
		case XSD_UNSIGNED_SHORT: //("http://www.w3.org/2001/XMLSchema#unsignedShort", XSD_INTEGER.value()),
			if (which == 1)
				c = Constant.create("1","http://www.w3.org/2001/XMLSchema#integer");
			else 
				c = Constant.create("2", "http://www.w3.org/2001/XMLSchema#integer");
			break;
		case XSD_UNSIGNED_BYTE: //("http://www.w3.org/2001/XMLSchema#unsignedByte", XSD_INTEGER.value()),
			if (which == 1)
				c = Constant.create("1","http://www.w3.org/2001/XMLSchema#integer");
			else 
				c = Constant.create("2", "http://www.w3.org/2001/XMLSchema#integer");
			break;
		case XSD_NORMALIZED_STRING: //("http://www.w3.org/2001/XMLSchema#normalizedString", XSD_STRING.value()),
			if (which == 1)
				c = Constant.create("a","http://www.w3.org/2001/XMLSchema#string");
			else 
				c = Constant.create("b", "http://www.w3.org/2001/XMLSchema#string");
			break;
		case XSD_TOKEN: //("http://www.w3.org/2001/XMLSchema#token", XSD_STRING.value()),
			if (which == 1)
				c = Constant.create("a","http://www.w3.org/2001/XMLSchema#string");
			else 
				c = Constant.create("b", "http://www.w3.org/2001/XMLSchema#string");
			break;
		case XSD_LANGUAGE: //("http://www.w3.org/2001/XMLSchema#language", XSD_STRING.value()),
			if (which == 1)
				c = Constant.create("en","http://www.w3.org/2001/XMLSchema#string");
			else 
				c = Constant.create("es", "http://www.w3.org/2001/XMLSchema#string");
			break;
		case XSD_NAME: //("http://www.w3.org/2001/XMLSchema#Name", XSD_STRING.value()),
			if (which == 1)
				c = Constant.create("a","http://www.w3.org/2001/XMLSchema#string");
			else 
				c = Constant.create("b", "http://www.w3.org/2001/XMLSchema#string");
			break;
		case XSD_NCNAME: //("http://www.w3.org/2001/XMLSchema#NCName", XSD_STRING.value()),
			if (which == 1)
				c = Constant.create("a","http://www.w3.org/2001/XMLSchema#string");
			else 
				c = Constant.create("b", "http://www.w3.org/2001/XMLSchema#string");
			break;
		case XSD_NMTOKEN: //("http://www.w3.org/2001/XMLSchema#NMTOKEN", XSD_STRING.value()),
			if (which == 1)
				c = Constant.create("a","http://www.w3.org/2001/XMLSchema#string");
			else 
				c = Constant.create("b", "http://www.w3.org/2001/XMLSchema#string");
			break;
		case XSD_HEX_BINARY: //("http://www.w3.org/2001/XMLSchema#hexBinary", XSD_STRING.value()),
			if (which == 1)
				c = Constant.create("0FB7","http://www.w3.org/2001/XMLSchema#string");
			else 
				c = Constant.create("0FB8", "http://www.w3.org/2001/XMLSchema#string");
			break;
		case XSD_BASE_64_BINARY: //("http://www.w3.org/2001/XMLSchema#base64Binary", XSD_STRING.value()),
			if (which == 1)
				c = Constant.create("0FB7","http://www.w3.org/2001/XMLSchema#string");
			else 
				c = Constant.create("0FB8", "http://www.w3.org/2001/XMLSchema#string");
			break;
		case XSD_ANY_URI: //("http://www.w3.org/2001/XMLSchema#anyURI", XSD_STRING.value());
			if (which == 1)
				c = Constant.create("http://www.w3.org/TR/owl2-overview/","http://www.w3.org/2001/XMLSchema#string");
			else 
				c = Constant.create("http://www.w3.org/TR/xmlschema-2/", "http://www.w3.org/2001/XMLSchema#string");
			break;
		default:
			throw new IllegalAccessError("unsupported datatype: " + datatype.toString());
		}
		literalConstants.add(c);
		return c;
	}
	
	protected boolean notEnoughLiterals(LiteralDataRange datatype, int nLiterals) {
		String s = MyPrefixes.PAGOdAPrefixes.expandIRI(datatype.toString());
		Datatype dt = Datatype.value(s); 
		switch (dt) {
		case XSD_BOOLEAN: //("http://www.w3.org/2001/XMLSchema#boolean", 8)
			return (nLiterals > 2);
//		case XSD_STRING: //("http://www.w3.org/2001/XMLSchema#string", 3)
//			return false;
//		case RDF_PLAIN_LITERAL: //("http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral", 4)
//			return false;
//		case XSD_INTEGER: //("http://www.w3.org/2001/XMLSchema#integer", 5)
//			return false;
//		case XSD_FLOAT: //("http://www.w3.org/2001/XMLSchema#float", 6)
//			return false;
//		case XSD_DOUBLE: //("http://www.w3.org/2001/XMLSchema#double", 7)
//			return false;
//		case XSD_DATE_TIME: //("http://www.w3.org/2001/XMLSchema#dateTime", 9)
//			return false;
//		case XSD_DATE_TIME_STAMP: //("http://www.w3.org/2001/XMLSchema#dateTimeStamp", XSD_DATE_TIME.value()),
//		case XSD_TIME: //("http://www.w3.org/2001/XMLSchema#time", 10)
//		case XSD_DATE: //("http://www.w3.org/2001/XMLSchema#date", 11)
//		case XSD_G_YEAR_MONTH: //("http://www.w3.org/2001/XMLSchema#gYearMonth", 12)
//		case XSD_G_YEAR: //("http://www.w3.org/2001/XMLSchema#gYear", 13),
//		case XSD_G_MONTH_DAY: //("http://www.w3.org/2001/XMLSchema#gMonthDay", 14),
//		case XSD_G_DAY: //("http://www.w3.org/2001/XMLSchema#gDay", 15),
//		case XSD_G_MONTH: //("http://www.w3.org/2001/XMLSchema#gMonth", 16),
//		case XSD_DURATION: //("http://www.w3.org/2001/XMLSchema#duration", 17),
//		case XSD_YEAR_MONTH_DURATION: //("http://www.w3.org/2001/XMLSchema#yearMonthDuration", XSD_DURATION.value()),
//		case XSD_DAY_TIME_DURATION: //("http://www.w3.org/2001/XMLSchema#dayTimeDuration", XSD_DURATION.value()),
//		case RDF_XML_LITERAL: //("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral", XSD_STRING.value()),
//		case RDFS_LITERAL: //("http://www.w3.org/2000/01/rdf-schema#Literal", XSD_STRING.value()),
//		case OWL_REAL: //("http://www.w3.org/2002/07/owl#real", XSD_DOUBLE.value()),
//		case OWL_RATIONAL: //("http://www.w3.org/2002/07/owl#rational", XSD_DOUBLE.value()),
//		case XSD_DECIMAL: //("http://www.w3.org/2001/XMLSchema#decimal", XSD_DOUBLE.value()),
//		case XSD_NON_NEGATIVE_INTEGER: //("http://www.w3.org/2001/XMLSchema#nonNegativeInteger", XSD_INTEGER.value()),
//		case XSD_NON_POSITIVE_INTEGER: //("http://www.w3.org/2001/XMLSchema#nonPositiveInteger", XSD_INTEGER.value()),
//		case XSD_POSITIVE_INTEGER: //("http://www.w3.org/2001/XMLSchema#positiveInteger", XSD_INTEGER.value()),
//		case XSD_NEGATIVE_INTEGER: //("http://www.w3.org/2001/XMLSchema#negativeInteger", XSD_INTEGER.value()),
//		case XSD_LONG: //("http://www.w3.org/2001/XMLSchema#long", XSD_INTEGER.value()),
//		case XSD_INT: //("http://www.w3.org/2001/XMLSchema#int", XSD_INTEGER.value()),
//		case XSD_SHORT: //("http://www.w3.org/2001/XMLSchema#short", XSD_INTEGER.value()),
//		case XSD_BYTE: //("http://www.w3.org/2001/XMLSchema#byte", XSD_INTEGER.value()),
//		case XSD_UNSIGNED_LONG: //("http://www.w3.org/2001/XMLSchema#unsignedLong", XSD_INTEGER.value()),
//		case XSD_UNSIGNED_INT: //("http://www.w3.org/2001/XMLSchema#unsignedInt", XSD_INTEGER.value()),
//		case XSD_UNSIGNED_SHORT: //("http://www.w3.org/2001/XMLSchema#unsignedShort", XSD_INTEGER.value()),
//		case XSD_UNSIGNED_BYTE: //("http://www.w3.org/2001/XMLSchema#unsignedByte", XSD_INTEGER.value()),
//		case XSD_NORMALIZED_STRING: //("http://www.w3.org/2001/XMLSchema#normalizedString", XSD_STRING.value()),
//		case XSD_TOKEN: //("http://www.w3.org/2001/XMLSchema#token", XSD_STRING.value()),
//		case XSD_LANGUAGE: //("http://www.w3.org/2001/XMLSchema#language", XSD_STRING.value()),
//		case XSD_NAME: //("http://www.w3.org/2001/XMLSchema#Name", XSD_STRING.value()),
//		case XSD_NCNAME: //("http://www.w3.org/2001/XMLSchema#NCName", XSD_STRING.value()),
//		case XSD_NMTOKEN: //("http://www.w3.org/2001/XMLSchema#NMTOKEN", XSD_STRING.value()),
//		case XSD_HEX_BINARY: //("http://www.w3.org/2001/XMLSchema#hexBinary", XSD_STRING.value()),
//		case XSD_BASE_64_BINARY: //("http://www.w3.org/2001/XMLSchema#base64Binary", XSD_STRING.value()),
//		case XSD_ANY_URI: //("http://www.w3.org/2001/XMLSchema#anyURI", XSD_STRING.value());
		default:
			return false;
		}
	}

}
