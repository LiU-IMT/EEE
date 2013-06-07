package se.liu.imt.mi.eee.ehr.res;

import java.util.HashMap;
import java.util.Map;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

public class StoredQueryXQueryAQLHybrid extends StoredQueryAQL {
	
	// This XQuery/AQL hybrid uses the same execution as StoredQueryAQL, that is likely to be different for other storage/DB solutions 
	
	public Representation serveGet(MimeHelper m) throws Exception {

		Map<String, Object> variablesForTemplate = new HashMap<String, Object>();

		// DOUBLE CHECK QUERY LANGUAGE COMPATIBILITY
		if (!storedQuery.getQueryLanguageID().equals("XQuery")) { //TODO: avoid hardcoded string value
			final String errorString = "You are trying to use "
					+ this.getClass().getCanonicalName()
					+ " to process a query that is not in a compatible AQL format. That is not allowed...";
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
					errorString);
		}

		// Execute query...
		//Dev/Debug: return new StringRepresentation("This should return the results from executing the query: "+storedQuery.toJson().toString(5));
		return processQueryAndReturnResult(storedQuery.get(TRANSLATED_QUERY), false);
	}

}
