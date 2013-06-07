package se.liu.imt.mi.eee.ehr.res;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import se.liu.imt.mi.eee.db.QueryContainer;
import se.liu.imt.mi.eee.db.QueryStorage;
import se.liu.imt.mi.eee.db.xmldb.XMLDBHelper;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.translators.QueryTranslator;
import se.liu.imt.mi.eee.utils.FreemarkerSupportResource;

public class Query extends FreemarkerSupportResource implements EEEConstants{
	
	protected String username;	
	protected String storedQueryURI;
	protected String returnMediaTypeName;
	protected MediaType returnMediaType;
	protected String querySha;
	protected String queryLanguage;
	protected QueryStorage queryStorage;
	
	public Map<String, QueryTranslator> queryTranslatorMap;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
				
		// TODO: Remove the following two lines?
		querySha = (String) getRequestAttributes().get(QUERY_SHA); 
		if (querySha != null) throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Server side routing messed up, you should have been sent to "+StoredQuery.class.getCanonicalName());

		queryLanguage = (String) getRequestAttributes().get(QUERY_LANGUAGE); // FIXME: set up mapping in router and constructor instead and select translator here
		
		username = getRequest().getClientInfo().getUser().getName();
	
		queryStorage = (QueryStorage) getContext().getAttributes().get(KEY_TO_QUERY_STORAGE); 
		queryTranslatorMap =  (Map<String, QueryTranslator>) getContext().getAttributes().get(KEY_TO_QUERY_TRANSLATOR_MAP); 
		
		//System.out.println("Query.doInit() query lang:"+queryLanguage+" queryStorage:"+queryStorage);
		
//		// Extract strings from URL query string	
//		// Not used so far
//		storedQueryURI = getQuery().getFirstValue(STORED_QUERY, true);	
		
		returnMediaTypeName = getQuery().getFirstValue(RETURN_MEDIA_TYPE, true);		
		returnMediaType = MediaType.TEXT_XML; // XML is default return media type
		if (returnMediaTypeName != null) {
			returnMediaType = MediaType.valueOf(returnMediaTypeName);
		}		
	}
	
	
	/**
	 * Takes care of url-encoded forms sent with POST using the
	 * application/x-www-form-urlencoded mediatype, e.g. forms from HTML like
	 * <FORM METHOD=POST ENCTYPE="application/x-www-form-urlencoded" ... 
	 * HTML form POSTed variables starting with _ (underscore) will have their 
	 * first _ removed and be sent further as dynamic variables in the URI query
	 * string together with any possibly already existing URI query variables. 
	 * All other variables (except "debug") will be stored as static variables
	 * together with the query.
	 * 
	 * In this implementation  if either the HTML form POSTed variable "debug"
	 * or a variable named "debug" in the URI query equals "true" then debugging
	 * behaviour will be invoked. Thus a "true" will override "false".
	 * The "debug" variable is then removed and thus not stored or sent further. 
	 * 
	 * (TODO: figure out if above overrides are good design choices)
	 * 
	 * @param incomingPostedRepresenation
	 * @throws Exception
	 */
	@Post("form")
	public Representation handleFormPost(Representation incomingPostedRepresentation) throws Exception{
		Form postedQueryAsForm = new Form(getRequestEntity());		
		String valueOfPostedVariableNamedQuery = postedQueryAsForm.getFirstValue(QUERY, true); // ignore case of parameter
		Form cleanedStaticForm = new Form();
		
		Form uriGetQueryAsForm = getQuery();
		if (uriGetQueryAsForm == null) uriGetQueryAsForm = new Form();

		// Determine debugging requests
		String debugFromURI = uriGetQueryAsForm.getFirstValue(DEBUG, true, "false");		
		String debugFromFormPost = postedQueryAsForm.getFirstValue(DEBUG, true, "false");
		Boolean debug = false;
		if (debugFromURI.equalsIgnoreCase("true")) debug = true;
		if (debugFromFormPost.equalsIgnoreCase("true")) debug = true;	
		// ...then remove debug variable from form
		postedQueryAsForm.removeAll(DEBUG);
		uriGetQueryAsForm.removeAll(DEBUG);
		
		//System.out.println("Query.handleFormPost() POST-ed query: "+valueOfPostedVariableNamedQuery);
		//System.out.println("Query.handleFormPost() URI query-string: "+uriGetQueryAsForm.getQueryString());
		
		// Check Query Language TODO: Call query-translators lookup here instead
		QueryTranslator queryTranslator = null;
		queryTranslator = queryTranslatorMap.get(queryLanguage);
		if (queryTranslator == null) {
			// No match found
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "You requested an unknown (not yet implemented) query language");
		}
				
		// Warning: JSONObject.put messes up ordering of keys thus using an intermediate
		//          sorted map instead of building JSON incrementally
		TreeMap<String, String> sortedMap = new TreeMap<String, String>();
		
		rearrangeParameters(postedQueryAsForm, cleanedStaticForm, uriGetQueryAsForm, sortedMap);

		if(debug){	
			// debugging
			String debugMessage = queryTranslator.debugQuery(cleanedStaticForm);
			return new StringRepresentation(debugMessage);
		} else {
			// not debugging
						
			JSONObject jobj = new JSONObject(sortedMap);
			
			//System.out.println("Query.handleFormPost() -- stringToBeHashed below:");
			//System.out.println(jobj.toString());
			//System.out.println("Query.handleFormPost() -- string to be sent on as dynamic variables query URI below:");
			//System.out.println(uriGetQueryAsForm.getMatrixString() + " -- Done!");
									
			// Calculate SHA on raw query
			// and other static variables (in sorted order as JSON string) 		
			
			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.reset(); // Probably not needed now for new MessageDigest, but later if same MessageDigest is reused for performance reasons
			md.update(jobj.toString().getBytes());
			byte[] shaOutput = md.digest();
			String staticQueryHash = new String(Hex.encodeHex(shaOutput));
			
			// Check if query exists already...
			QueryContainer alreadyStoredQuery = queryStorage.viewQuery(staticQueryHash);
			if (alreadyStoredQuery == null) { 
				// FIXME: add deeper equality check instead of just hash comparison (to avoid accidental hash collisions for differing queries)
				
				// This was a new hash, so let's store it
				//System.out.println("Query.handleFormPost() staticQueryHash = "+staticQueryHash);

				XMLDBHelper dbHelper = (XMLDBHelper) getContext().getAttributes().get(KEY_TO_XMLDBHELPER_INSTANCE); // Get the db handle from context
				String created = dbHelper.getCurrentDatabaseTimeAsISODateTimeString();

				// Build query object
				QueryContainer q = new QueryContainer(staticQueryHash, username, cleanedStaticForm, created, queryLanguage); //TODO: Change cleanedStaticForm to 

				try {
					// Translate  
					QueryContainer qt = queryTranslator.translateQuery(q);

					// If no exceptions thrown, then translation is OK so we store it
					queryStorage.storeQuery(qt);

				} catch (Exception e) {
					System.out.println(this.getClass().getCanonicalName()+" stack trace:");
					e.printStackTrace();
					if (e instanceof ResourceException) {
						throw e;
					} else {
						throw new ResourceException (Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage(), e);
					}
				}	
			} else {
				// Log unwise rePOSTing TODO: Log using proper logging level etc...
				System.out.println("Alert from Query.handleFormPost(): User "+username+" is POSTing an already POSTed query. This may be BAD for performance... The SHA-1 of query is: "+staticQueryHash);
			}
			
			// Redirect to the new stored query
			String redirectUrl = "../"+queryLanguage+"/"+staticQueryHash+"/?"+uriGetQueryAsForm.getQueryString();
			getResponse().redirectPermanent(redirectUrl); 
			return new StringRepresentation("Query sucessfully translated and stored, you will now be redirected to: "+redirectUrl , MediaType.TEXT_PLAIN);
		}
	}

	protected void rearrangeParameters(Form postedQueryAsForm,
			Form cleanedStaticForm, Form uriGetQueryAsForm,
			TreeMap<String, String> sortedMap) {
		// Create string (currently compact JSON) with keys and values sorted alphabetically, allows multiple values with same name			
		Iterator<String> it = postedQueryAsForm.getNames().iterator();

		while (it.hasNext()) {
			String name = (String) it.next();
			String[] valueArray = postedQueryAsForm.getValuesArray(name);
			// TODO: Separate static & dynamic variables,
			if (name.startsWith("_")) {
				// POST-ed variable names starting with _ (underscore) will not be stored, 
				// but instead sent on as dynamic parameters in URI after removing first underscore in the variable name
				for (int i = 0; i < valueArray.length; i++) {
					uriGetQueryAsForm.add(name.substring(1), valueArray[i]);
					// System.out.println("Querysource.handleFormPost() adding entry: "+name.substring(1) +" = "+ valueArray[i]);
				}					
			} else {
				// All other posted (static) variables get stored in the query map
				if (valueArray.length != 1) {
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Keys for static (stored) query variables need to be unique (duplicates are not allowed). Dynamic variables prepended with _ (underscore) will instead be passed on as a URI query and do not need to be unique.");					
				}
				sortedMap.put(name, valueArray[0]);
				cleanedStaticForm.add(name, valueArray[0]);				
			}				
		}
	}
	
	@Get("html")
	public Representation serveHTML() throws IOException {	
		return serveGet(MimeHelper.HTML);
	}
	
//	@Get("xml")
//	public Representation serveXML() throws IOException {	
//		return serveGet(MimeHelper.XML);
//	}
	
	public Representation serveGet(MimeHelper m) throws IOException {
		// TODO: Add error handling instead of throwing further 
		String result;
		Map<String, Object> variablesForTemplate = new HashMap<String, Object>();
		if (queryLanguage == null) {
			return handleResponseViaFreemarkerTemplate(m.getFileSuffix(), m.getMediaType(), variablesForTemplate );			
		} else {
			this.fileNameBase = this.fileNameBase+"-"+queryLanguage;
			return handleResponseViaFreemarkerTemplate(m.getFileSuffix(), m.getMediaType(), variablesForTemplate );
		} 
	}	
	
}
