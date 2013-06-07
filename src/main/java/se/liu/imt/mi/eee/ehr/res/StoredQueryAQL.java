package se.liu.imt.mi.eee.ehr.res;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.ext.xml.Transformer;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.modules.XQueryService;

import se.liu.imt.mi.eee.DemoStarter;
import se.liu.imt.mi.eee.db.DatabaseInterface.DatabaseMode;

public class StoredQueryAQL extends StoredQuery {

	//	@Get("json")
	//	public Representation serveJSON() throws Exception {
	//		return new JsonRepresentation(...);
	//	}

	 //@Get("html") // TODO: un-comment this annotation to activate the method (some future time when html-transform works)
	 public Representation serveHTML() throws Exception {
		 
		    // Quick and dirty copy-paste from VersionedObjectResource
			String filename = DemoStarter.getWwwFileDir()+"xslt/openEHR_RMtoHTML-v6.xsl";
			filename = filename.substring(5);
			try {
				filename = java.net.URLDecoder.decode(filename, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, 
						"Suspected configuration problem, couldn't access "+filename, e);
			}
			Representation xsltSheet = new FileRepresentation(filename,
					MediaType.APPLICATION_XML); //TODO: Check expirationTime, Move to proper location
			
			//TODO: The Transformer should perhaps be created in a more accessible location 
			//      - in the application context perhaps (if there are no multithread issues - check that first...)
			Transformer xsltTransformer = new Transformer(Transformer.MODE_RESPONSE, xsltSheet);

			return xsltTransformer.transform(serveGet());
			
//			// TODO: Consider more efficient things than stringifying to get bookmarking functionality
//			
//			String transformed = null;
//			try {
//				transformed = transformedRepresentation.getText();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not convert xml to html", e);
//			}
//			transformed = transformed.replace("replace-with-server-and-ehrID/", getRequest().getHostRef().toString()+"/ehr:"+ehrID+"/");
//			return transformed;
//		 return "HTML!"; //serveGet(MimeHelper.HTML);
	 }

	@Get("xml")
	public Representation serveXML() throws Exception {
		return serveGet();
	}

	public Representation serveGet() throws Exception {

		@SuppressWarnings("unused")
		Map<String, Object> variablesForTemplate = new HashMap<String, Object>();

		// DOUBLE CHECK QUERY LANGUAGE COMPATIBILITY
		if (!storedQuery.getQueryLanguageID().equals("AQL")) { //TODO: avoid hardcoded string value
			final String errorString = "You are trying to use "
					+ this.getClass().getCanonicalName()
					+ " to process a query that is not in a compatible AQL format. That is not allowed...";
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
					errorString);
		}

		// Execute query...

		//return new StringRepresentation("This should return the results from executing the query: "+storedQuery.toJson().toString(5));

		return processQueryAndReturnResult(storedQuery.get(TRANSLATED_QUERY), true);

	}

	protected Representation processQueryAndReturnResult(String translQuery, boolean addXmlProlog) {
		
		//System.out.println("StoredQueryAQL.processQueryAndReturnResult() translQuery: "+translQuery);
		//System.out.println("StoredQueryAQL.processQueryAndReturnResult() translQuery type: "+translQuery.getClass().getCanonicalName());

		// TODO: This is a possible place to filter and stop *QL-injection attacks 		

//		if (dbHelper.getDatabaseMode().equals(DatabaseMode.SINGLE_RECORD))
//			// TODO: check ehrId existence & not empty
//			xqString += "declare variable $current_ehr_uid :=\"" + ehrId
//					+ "\" ; \n";

		// Get XQuery Service object and 
		XQueryService xqService;
		try {
			xqService = dbHelper.getXQueryService(dbHelper.getRootCollection()
					.getChildCollection("EHR"));
			
			// provide xqService with dynamic parameters given in URI query
			Form uriQueryParameters = getQuery(); // this "getQuery" calls a built in Restlet function
			for (String parameterName : uriQueryParameters.getNames()) {
				if (uriQueryParameters.getValuesArray(parameterName).length > 1) {
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
							"Duplicate keys detected. The LiU EEE framework may allow duplicate keys for URI query parameters, but this particular query executor implementation ("+this.getClass().getCanonicalName()+") does not.");
				}
				xqService.declareVariable(parameterName, uriQueryParameters.getFirstValue(parameterName));			
			}
			
			// TODO: Possibly add support for some static parameters too.
			// xqService.declareVariable(arg0, arg1);
			
			ResourceSet rset = null;
			// FIXME: Enable better security here
			if (dbHelper.getDatabaseMode().equals(DatabaseMode.MULTI_RECORD)) {
				// Query can access all EHRs:
				rset = xqService.query(translQuery);
//				System.out.println("StoredQueryAQL.processQueryAndReturnResult() running in DatabaseMode.MULTI_RECORD");
			} else {
				// Only query this specific EHR:
				xqService.declareVariable("current_ehr_uid", ehrId);
				rset = xqService.queryResource(ehrId, translQuery);
//				System.out.println("StoredQueryAQL.processQueryAndReturnResult() running in DatabaseMode.SINGLE_RECORD for EHR ID: "+ehrId);
			}

			// TODO: document return format specification(s) better?
			String dbOutput = "";
			if (addXmlProlog) dbOutput = dbOutput + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
			ResourceIterator rit = rset.getIterator();
			while (rit.hasMoreResources()) {
				Resource res = rit.nextResource();
				dbOutput += res.getContent().toString();
			}

			// System.out.println("AQLQueryResource.handlePost() listing result:\n"
			// + dbOutput);
			

			
			return new StringRepresentation(dbOutput, returnMediaType);
		} catch (Exception e) {
			System.out.println(this.getClass().getCanonicalName()+" stack trace:");
			e.printStackTrace();
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
					"Failed to execute query; " + e.getMessage(), e);
		}
	}

}
