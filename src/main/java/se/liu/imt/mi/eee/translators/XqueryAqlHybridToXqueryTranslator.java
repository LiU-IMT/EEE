package se.liu.imt.mi.eee.translators;

import java.io.StringReader;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import se.liu.imt.mi.eee.AQL_Parser.ParseException;
import se.liu.imt.mi.eee.AQL_Parser.AqlParser;
import se.liu.imt.mi.eee.db.QueryContainer;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.structure.EEEConstants.AQLParserReturnType;

/**
 * Translates AQL to XQuery the variable corresponding to {@link EEEConstants.QUERY} 
 * is sent to conversion in {@link se.liu.imt.mi.eee.AQL_Parser.AqlParser} and
 * prefixed with namespace information plus the other variables as inline
 * XQuery variable declarations. The resulting translation is stored in 
 * the {@link QueryContainer} as the variable {@link @EEEConstants.TRANSLATED_QUERY}
 * @author erisu
 */
public class XqueryAqlHybridToXqueryTranslator implements QueryTranslator {
	
	public static final String EEE_AQL_START_TAG = "<eeeq:aql>";
	public static final String EEE_AQL_END_TAG = "</eeeq:aql>";
	private boolean existTweaks = true;
	
	protected static final String NAMESPACE_PREFIX_ETC = new String(
			"declare default element namespace 'http://schemas.openehr.org/v1'; \n" +
			"declare namespace v1 = 'http://schemas.openehr.org/v1'; \n" +
			"declare namespace xsi = 'http://www.w3.org/2001/XMLSchema-instance'; \n" +
			"declare namespace eee = 'http://www.imt.liu.se/mi/ehr/2010/EEE-v1.xsd'; \n" +
			"declare namespace eeeq = 'http://www.imt.liu.se/mi/ehr/2010/EEEQuery'; \n" +
			"declare namespace res = 'http://www.imt.liu.se/mi/ehr/2010/xml-result-v1#'; \n" +
			"declare namespace output = 'http://www.w3.org/2010/xslt-xquery-serialization'; \n" );

	public String debugQuery(Form staticQueryParametersAsForm) throws Exception {
		return "\nThe query translated by "+this.getClass().getCanonicalName()+":\n"+
		
		translateQueryAQL(staticQueryParametersAsForm)+
		"\n\nThe incoming static variables were:\n"+staticQueryParametersAsForm.toString();
	}

	public QueryContainer translateQuery(QueryContainer query) throws Exception {
		query.put(EEEConstants.TRANSLATED_QUERY, translateQueryAQL(query.toForm(true)));
		return query;
	}
	
	// TODO: Change to protected AFTER REMOVING EXTERNAL CALLS
	public String translateQueryAQL(Form staticParameters) throws ParseException {
		
		String rawText = ""; 
		String optionsText = "";
		String xqString = "";

		// see e.g.: http://demo.exist-db.org:8098/exist/xquery.xml#serialization for example
		// declare option exist:serialize "method=xhtml media-type=application/xhtml+html";
		if (existTweaks){
			optionsText = "declare option exist:serialize '";
		}
		
		for (Parameter parameter : staticParameters) {			 
			if (parameter.getName().equals(EEEConstants.QUERY)){
				rawText = parameter.getValue();
				// The main query field from the form
			} else if (parameter.getName().startsWith("x_ser_option_")) {
				// Treat some option parameters in a special way inserting them into the xqString
				if (existTweaks) {
					optionsText = optionsText + parameter.getName().substring(13) +"="+parameter.getValue()+" ";
				} else {
					optionsText = optionsText + "declare option output:"+ parameter.getName().substring(13) +" '"+parameter.getValue()+"';\n";
				} 
			} else {
				// Declare all other parameters as normal static variables in the XQuery
				
				// Usually a Parameter list can contain duplicate keys and that would
				// likely mess up the produced xQuery (or at least it's predictability)
				// In this case QueryContainer itself disallows duplicate keys so it 
				// should not be a problem.
				xqString = xqString + "declare variable $"+parameter.getName()+" :='"+parameter.getValue()+"' ; \n";
			}			
		}
		if (existTweaks) optionsText = optionsText +"';\n";

		xqString = xqString + NAMESPACE_PREFIX_ETC;
		xqString = xqString + optionsText;

		// Add EHR ID if available
		
		if (rawText == null || rawText.length()==0) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Failed to process since the query was empty...");
		}

		// Find and replace AQL sections in the query text until no more such sections are left
		int startPositionOfAQL = -10;
		int lastPositionOfAQL = 0;
		
		do {
			// Find next AQL start
			startPositionOfAQL = rawText.indexOf(EEE_AQL_START_TAG, lastPositionOfAQL);
			System.out.println("XqueryAqlHybridToXqueryTranslator.translateQueryAQL(): startPositionOfAQL="+startPositionOfAQL);
			
			if (startPositionOfAQL < 0) {
				// No more AQL found, so finish by adding the remaining tail of the raw text...
				xqString = xqString + rawText.substring(lastPositionOfAQL);	
			} else {
				// Include raw text between last and this AQL part
				xqString = xqString + rawText.substring(lastPositionOfAQL, startPositionOfAQL);
				
				// Find end tag of this aql
				lastPositionOfAQL = rawText.indexOf(EEE_AQL_END_TAG, startPositionOfAQL + EEE_AQL_START_TAG.length());
				
				String aqlSnippet = rawText.substring(startPositionOfAQL+EEE_AQL_START_TAG.length(), lastPositionOfAQL);
				
				// Step past the closing tag:
				lastPositionOfAQL = lastPositionOfAQL + EEE_AQL_END_TAG.length();
				
				AqlParser parser = new AqlParser(new StringReader(aqlSnippet));
				parser.setReturnType(AQLParserReturnType.XQuery_EEE_0_1);
				String temp_query = parser.Query();
				
				// FIXME: Replace hack below with proper parser correction
				temp_query = temp_query.replace("//*eee:versioned_objects/eee:versions", "/eee:versioned_objects/eee:versions");
				xqString = xqString + temp_query;				
			}
		} while (startPositionOfAQL > 0);
		
		return xqString;
	}

	public void setExistTweaks(boolean existTweaks) {
		this.existTweaks = existTweaks;
	}

	public boolean isExistTweaks() {
		return existTweaks;
	}

}
