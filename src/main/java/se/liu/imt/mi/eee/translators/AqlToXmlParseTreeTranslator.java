package se.liu.imt.mi.eee.translators;

import java.io.StringReader;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import se.liu.imt.mi.eee.AQL_Parser.ParseException;
import se.liu.imt.mi.eee.AQL_Parser.AqlParser;
import se.liu.imt.mi.eee.db.QueryContainer;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.structure.EEEConstants.AQLParserReturnType;

public class AqlToXmlParseTreeTranslator implements QueryTranslator {

	public String debugQuery(Form staticQueryParametersAsForm) throws Exception {
		return "\nThe query translated by "+this.getClass().getCanonicalName()+":\n"+
		translateQueryAQL(staticQueryParametersAsForm.getFirstValue(EEEConstants.QUERY))+
		"\nThe incoming static variables:\n"+staticQueryParametersAsForm.toString();
	}

	public QueryContainer translateQuery(QueryContainer query) throws Exception {
		throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "only debug mode is currently allowed...");
//		query.put(EEEConstants.TRANSLATED_QUERY, translateQueryAQL(query.get("query")));
//		return query;
	}
	
	// TODO: Change to protected AFTER REMOVING EXTERNAL CALLS
	protected String translateQueryAQL(String rawText) throws ParseException {
		if (rawText == null || rawText.length()==0) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Failed to process since the query was empty...");
		}
		AqlParser parser = new AqlParser(new StringReader(rawText));
		parser.setReturnType(AQLParserReturnType.XML);
		// parser.setReturnType(AQLParserReturnType.XQuery_EEE_0_1);
		parser.ReInit(new StringReader(rawText));
		return parser.Query();
	}

}
