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
public class AqlToXqueryTranslator implements QueryTranslator {
	
	protected static final String NAMESPACE_PREFIX_ETC = new String("declare namespace v1 = 'http://schemas.openehr.org/v1'; \n" +
	"declare default element namespace 'http://schemas.openehr.org/v1'; \n" +
	"declare namespace xsi = 'http://www.w3.org/2001/XMLSchema-instance'; \n" +
	"declare namespace eee = 'http://www.imt.liu.se/mi/ehr/2010/EEE-v1.xsd'; \n" +
	"declare namespace res = 'http://www.imt.liu.se/mi/ehr/2010/xml-result-v1#'; \n");

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
		
		String rawText  = staticParameters.getFirstValue(EEEConstants.QUERY);
		
		if (rawText == null || rawText.length()==0) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Failed to process since the query was empty...");
		}
		
		String xqString = ""+NAMESPACE_PREFIX_ETC;
		
		for (Parameter parameter : staticParameters) {
			// Don't repeat the query 
			if (!parameter.getName().equals(EEEConstants.QUERY)) {
				// Usually a Parameter list can contain duplicate keys and that would
				// likely mess up the produced xQuery (or at least it's predictability)
				// In this case QueryContainer itself disallows duplicate keys so it 
				// should not be a problem.
				xqString = xqString + "declare variable $"+parameter.getName()+" :='"+parameter.getValue()+"';\n";
			}			
		}
		
		AqlParser parser = new AqlParser(new StringReader(rawText));
		parser.setReturnType(AQLParserReturnType.XQuery_EEE_0_1);
		String temp_query = parser.Query();
		
		// FIXME: Replace hack below with proper parser correction
		temp_query = temp_query.replace("//*eee:versioned_objects/eee:versions", "/eee:versioned_objects/eee:versions");
		xqString += temp_query;
		return xqString;
	}

}
