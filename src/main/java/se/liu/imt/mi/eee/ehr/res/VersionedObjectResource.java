package se.liu.imt.mi.eee.ehr.res;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.restlet.data.CacheDirective;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.ext.xml.Transformer;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import se.liu.imt.mi.eee.DemoStarter;
import se.liu.imt.mi.eee.db.EHRDatabaseReadInterface;
import se.liu.imt.mi.eee.ehr.EHRRouter;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.utils.FreemarkerSupportResource;

public class VersionedObjectResource extends FreemarkerSupportResource implements EEEConstants {

	// TODO: Move MAX_AGE control to settings file (also allows simple cache comparison testing) 
	Integer identifiedVersionsMaxage = 60*60*24*7; // Allow a week of caching
	
	public static final String LATEST_VERSION = "latest_version";
	public static final String LATEST_TRUNK_VERSION = "latest_trunk_version";
	// See also constants defined in EEEConstants.java (OBJECT_ID, CREATING_SYSTEM_ID, VERSION_TREE_ID etc.)

	String ehrID;
	String objectID;
	String systemID;
	String treeID;
	EHRDatabaseReadInterface<Node, Node> dbReader;
	String lookupString;
	String command;
	MimeHelper selectedMime;
	// Response response;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();

		// Get the db handle from context (used both for POST & GET handling)
		dbReader = (EHRDatabaseReadInterface<Node, Node>) getContext().getAttributes().get(EEEConstants.KEY_TO_BASIC_DB_READER);
		//identifiedVersionsMaxage = (Integer) getContext().getAttributes().get(EHRTestRestStarter.IDENTIFIED_VERSIONS_MAXAGE);
		identifiedVersionsMaxage = DemoStarter.getConfig().getInteger(DemoStarter.IDENTIFIED_VERSIONS_MAXAGE, new Integer(60*60*24*7));
		// TODO: Inactivate trace printout:
		getContext().getLogger().fine("VersionedObjectResource.doInit() called with http method: "+getRequest().getMethod().getName()+"\n"+
		"Entity available: "+getRequest().isEntityAvailable()+"\n"+
		"Attributes in call: "+getRequestAttributes());

		// Store request parameters in local variables usable e.g. for GET & POST handling
		this.ehrID = (String) getRequestAttributes().get(EHR_ID);
		this.objectID = (String) getRequestAttributes().get(OBJECT_ID);
		this.systemID = (String) getRequestAttributes().get(CREATING_SYSTEM_ID);
		this.treeID = (String) getRequestAttributes().get(VERSION_TREE_ID);
		this.lookupString = (String) getRequestAttributes().get(VERSION_LOOKUP);
		this.command = (String) getRequestAttributes().get(COMMAND);

	}

// FIXME: Enable again (disabled due to ipad content negotiation trouble during mtd2011 demo)
	@Get("xml")
	public Representation serveXMLResult() {						
		selectedMime = MimeHelper.XML;
//		System.out.println("VersionedObjectResource.getRepresentation() returning XML");
		try {
			return getVersionAsXMLRepresentatioFromDatabase();
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
			return new StringRepresentation(e.toString());
		}
	}						

	@Get("html")
	public Representation serveHTMLResult() {						
		selectedMime = MimeHelper.HTML;
//		System.out.println("VersionedObjectResource.getRepresentation() returning HTML");
		Map<String, Object> variablesForTemplate = new HashMap<String, Object>();

		String div = transformVersionToHTMLdiv((DomRepresentation) getVersionAsXMLRepresentatioFromDatabase());
		variablesForTemplate.put("composition_div", div);

		try {
			return handleResponseViaFreemarkerTemplate(selectedMime.getFileSuffix(), selectedMime.getMediaType(), variablesForTemplate );
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	
	}						

	@Get("txt")
	public Representation serveTXTResult() {
		selectedMime = MimeHelper.TXT;
		// TODO: replace hack below with a xsltTransformer that converts xmlformat to nice plaintext. 
		String textresult = "This would return plain text "
			+ " for EHR:" + ehrID + " Versioned object :" + objectID
			+ " Creating System ID:" + systemID + " Version tree ID:"
			+ treeID + "\n Content (XML now, later will be txt): " + getVersionAsXMLRepresentatioFromDatabase().toString();
//		System.out.println("VersionedObjectResource.getRepresentation() returning Plain text");
		return new StringRepresentation(textresult);
	}

	@Get("json")
	public Representation serveJSONResult() {
		selectedMime = MimeHelper.JSON;
		// TODO: replace hack below with a xsltTransformer that converts xmlformat to nice json. 
		String textresult = "This would return JSON "
			+ " for EHR:" + ehrID + " Versioned object :" + objectID
			+ " Creating System ID:" + systemID + " Version tree ID:"
			+ treeID;
//		System.out.println("VersionedObjectResource.getRepresentation() returning JSON");
		return new StringRepresentation(textresult);
	}

	public String transformVersionToHTMLdiv(DomRepresentation dbResultAsXML)  {		    		
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

		Representation transformedRepresentation = xsltTransformer.transform(dbResultAsXML);
		
		// TODO: Consider more efficient things than stringifying to get bookmarking functionality
		
		String transformed = null;
		try {
			transformed = transformedRepresentation.getText();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not convert xml to html", e);
		}
		transformed = transformed.replace("replace-with-server-and-ehrID/", getRequest().getHostRef().toString()+"/ehr:"+ehrID+"/");
		return transformed;
	}

	/**
	 * This method is only for experimental testing purposes. 
	 * Normally version objects can only be created via contributions
	 * @see {@link ContributionBuilderObjectResource}
	 */
	
	// TODO: Comment out or block access to this method in production
	// environments since addition of VERSION_OBJECTS should only be allowed 
	// via contributions (e.g. using the ContributionBuilder classes).
	/*
		@Post("xml")
		public void handlePost() {

			//				try {
	//					System.out.println("POST ATTRIBUTES: " + this.getRequest().getAttributes() + "ENTITY:\n"
	//							+ getRequest().getEntity().getText()); // getEntityAsDom().getDocument().toString());
	//				} catch (IOException e1) {
	//					// TODO Auto-generated catch block
	//					e1.printStackTrace();
	//				}

			try {

				Request request = getRequest();		
				DomRepresentation domRep = new DomRepresentation(request.getEntity());
				Document domDoc = domRep.getDocument();
				dbReader.writeVersionedObject(ehrID, objectID, systemID, treeID, VersionableObjectType.COMPOSITION, domDoc);
			} catch (Exception e1) {

				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			String message = "(VersionedObjectResource: post)"+"Succesful POST to" + "EHR:" + ehrID + " Versioned object :" + objectID
			+ " Creating System ID:" + systemID + " Version tree ID:" + treeID;
			try {
				message = message + "\n Content as text: " + getRequest().getEntity().getText();
			} catch (IOException e) {
				message = message + "\n Content: " + e.getLocalizedMessage();
			}

			getResponse().setEntity(message, MediaType.TEXT_PLAIN);
		}

	 */

//    @Override
//    protected void describeGet(MethodInfo info) {
//        info.setIdentifier("items");
//        info.setDocumentation("Retrieve the list of current items.");
//        RepresentationInfo repInfo = new RepresentationInfo(MediaType.TEXT_XML);
//        repInfo.setXmlElement("items");
//        repInfo.setDocumentation("List of items as XML file");
//        info.getResponse().getRepresentations().add(repInfo);
//    }
	
	/**
	 * Analyzes incoming variables and returns suitable XML representation, 
	 * the method also sets suitable cache directives.
	 */
	public Representation getVersionAsXMLRepresentatioFromDatabase() throws ResourceException {
		Node dbResultObject = null;


			// Check that we actually got an ehrID else scream...
			if (ehrID==null || ehrID.length()==0 ) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Error: No "+EEEConstants.EHR_ID+" supplied in request");
			}

			// Check that we actually got an objectID else scream...
			if (objectID==null || objectID.length()==0 ) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Error: No "+EEEConstants.OBJECT_ID+" supplied in request");
			}

			if (command != null) {

				// Command examples
				//			 * ---- /ehr/{ehrId}/{versionedObject}/{command} ---
				//			 * http://localhost:8182/ehr/1234000/56780007/all_version_ids
				//			 * http://localhost:8182/ehr/1234000/56780007/all_versions
				//			 * http://localhost:8182/ehr/1234000/56780007/revision_history

				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, 
						"Broken URI routing. Commands should be routed by "+EHRRouter.class.getName() + 
						" to "+ VersionedObjectCommandResource.class.getCanonicalName()+" instead of " +
						this.getClass().getCanonicalName() +
						" (You tried to send a /command to the object "+objectID+" in the EHR "+ehrID+ ")");
				
			} else if (lookupString != null) {
				
				// Enters here only if command was null and lookupString != null
				// @Command examples
				//			 * ---- /ehr/{ehrId}/{versionedObject}@{versionLookup}  ----                        
				//			 * http://localhost:8182/ehr/1234000/56780007@latest_trunk_version
				//			 * http://localhost:8182/ehr/1234000/56780007@latest_version
				//			 * http://localhost:8182/ehr/1234000/56780007@2005-08-02T04:30:00

				// TODO: Possibly insert a better string matcher for allowed @commands below
				
				try {
					if (lookupString.startsWith(LATEST_TRUNK_VERSION)) {
						dbResultObject = dbReader
								.getVersionedObject_latest_trunk_version(ehrID,
										objectID);
						getResponseCacheDirectives().add(CacheDirective.noCache());
					} else if (lookupString.startsWith(LATEST_VERSION)) {
						dbResultObject = dbReader
								.getVersionedObject_latest_version(ehrID,
										objectID);
						getResponseCacheDirectives().add(CacheDirective.noCache());
					}
					// TODO: Insert a better string matcher for timestamps below
					else if (lookupString
							.matches("(\\d{2,4})(?:\\-(\\d{2}))?(?:\\-(\\d{2}))?.*")) {
						// OK, matched a date go on do a date lookup
						dbResultObject = dbReader.getVersionedObject_at_time(ehrID, objectID, lookupString);
						getResponseCacheDirectives().add(CacheDirective.noCache()); // FIXME: Consider if we actually could allow (private) caching of timestamped lookup resources
					} else {
						// @parameter unknown...
						throw new ResourceException(
								Status.CLIENT_ERROR_BAD_REQUEST,
								"The parameter after @ was illegal. Try suffixing with a timestamp or '"
										+ LATEST_TRUNK_VERSION + "' or '"
										+ LATEST_VERSION + "' instead");
					}
				} catch (Exception e) {
					// - internal error, DB not reached etc
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}

			} else if (treeID != null && systemID != null) {

				// The commonly used DB lookup when we have ehrID, objectID, systemID
				// and treeID is done here
				
				// These fully identified version never change and can thus be cached for long time
				getResponseCacheDirectives().add(CacheDirective.maxAge(identifiedVersionsMaxage.intValue()));
				getResponseCacheDirectives().add(CacheDirective.privateInfo());				
				
				try {
					dbResultObject = dbReader.getVersionedObject(ehrID, objectID, systemID, treeID);
					//System.out.println("VersionedObjectResource ---> db OK");
				} catch (Exception e) {
					// - internal error, DB not reached etc
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}

			} else if (systemID==null && treeID==null && command==null && lookupString==null) {
				// Now the resource was called with only ehrId and objectID, so return same as for command=ALL_VERSION_IDS
				// TODO: Implement
				getResponseCacheDirectives().add(CacheDirective.noCache());
				throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED, 
						"When implemented this would return all version IDs of the object "+objectID+" in the EHR "+ehrID);
			} else {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "This system did not recognize this combination of parameters " +
						"(Parameters found; ehrID:"+ehrID+", objectID:"+objectID+", systemID:"+systemID+", treeID:"+treeID+
						", command:"+command+", lookupString:"+lookupString );
			}
			
			if (dbResultObject==null){
				//System.out.println("VersionedObjectResource ---> dbResultObject==null");
				// Http error 404 if version not found
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Correct query format, but there was no such versioned object stored here. (You asked for "+ehrID+"::"+objectID+":"+systemID+":"+treeID+" )");
			}

			// ***** Now, if we got all the way here, then return (a version) in XML *******
			return new DomRepresentation(MediaType.TEXT_XML, (Document) dbResultObject);

	}

//	public ResourceInfo getResourceInfo(ApplicationInfo ai) {
//		ai.getResources().
//		ResourceInfo ri = new ResourceInfo("Versioned Object");
//		ri.setIdentifier("Versioned Object!");
//		ri.setPath("/");
//		// WADL info
////		this.setName("Versioned Object");
////		this.setDescription("This resource returns identified openEHR VERSION objects. Depending on context the VERSIONs can be different things, COMPOSITIONs, FOLDERs etc.");
//
//		return ri;
//	}



}