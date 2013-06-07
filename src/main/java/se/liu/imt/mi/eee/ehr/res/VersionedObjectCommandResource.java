package se.liu.imt.mi.eee.ehr.res;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.ext.xml.Transformer;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import se.liu.imt.mi.eee.db.EHRDatabaseReadInterface;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.utils.FreemarkerSupportResource;

public class VersionedObjectCommandResource extends FreemarkerSupportResource implements EEEConstants {

	public static final String LATEST_VERSION = "latest_version";
	public static final String LATEST_TRUNK_VERSION = "latest_trunk_version";
	public static final String REVISION_HISTORY = "revision_history";
	public static final String ALL_VERSIONS = "all_versions";
	public static final String ALL_VERSION_IDS = "all_version_ids";
	// See also constants defined in EEEConstants.java (OBJECT_ID, CREATING_SYSTEM_ID, VERSION_TREE_ID etc.)

	private enum MimeHelper {XML("xml", MediaType.APPLICATION_ALL_XML), 
							 HTML("html", MediaType.TEXT_HTML), 
							 TXT("txt", MediaType.TEXT_PLAIN),
							 JSON("json", MediaType.APPLICATION_JSON);		
		private final String fileSuffix;
		private final MediaType mediaType;
		MimeHelper(String fileSuffix, MediaType mediaType){
			this.fileSuffix=fileSuffix;
			this.mediaType=mediaType;
		}
		public String getFileSuffix() {
			return fileSuffix;
		}
		public MediaType getMediaType() {
			return mediaType;
		}
	};

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

		// TODO: Inactivate trace printout:
		System.out.println("# "+VersionedObjectCommandResource.class.getSimpleName()+".doInit() called with http method: "+getRequest().getMethod().getName());
		System.out.println("# Entity available: "+getRequest().isEntityAvailable());
		System.out.println("# Attributes in call: "+getRequestAttributes());

		// Store request parameters in local variables usable e.g. for GET & POST hansling
		this.ehrID = (String) getRequestAttributes().get(EHR_ID);
		this.objectID = (String) getRequestAttributes().get(OBJECT_ID);
		this.systemID = (String) getRequestAttributes().get(CREATING_SYSTEM_ID);
		this.treeID = (String) getRequestAttributes().get(VERSION_TREE_ID);
		this.lookupString = (String) getRequestAttributes().get(VERSION_LOOKUP);
		this.command = (String) getRequestAttributes().get(COMMAND);
	}

	@Get("xml")
	public Representation serveXMLResult() {						
		selectedMime = MimeHelper.XML;
		System.out.println("VersionedObjectResource.getRepresentation() returning XML");
		try {
			return callDatabaseAndReturnRepresentationViaFreemarker();
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
			return new StringRepresentation(e.toString());
		}
	}						

	@Get("html")
	public Representation serveHTMLResult() {						
		selectedMime = MimeHelper.HTML;
		System.out.println("VersionedObjectResource.getRepresentation() returning HTML");
		return callDatabaseAndReturnRepresentationViaFreemarker();
	}						

	@Get("txt")
	public Representation serveTXTResult() {
		selectedMime = MimeHelper.TXT;
		System.out.println("VersionedObjectResource.getRepresentation() returning TXT");
		return callDatabaseAndReturnRepresentationViaFreemarker();
	}

	@Get("json")
	public Representation serveJSONResult() {
		selectedMime = MimeHelper.TXT;
		System.out.println("VersionedObjectResource.getRepresentation() returning JSON");
		return callDatabaseAndReturnRepresentationViaFreemarker();
	}

	public Representation callDatabaseAndReturnRepresentationViaFreemarker() throws ResourceException {
		Node dbResultObject = null;
		try{

			// Check that we actually got an ehrID else scream...
			if (ehrID==null || ehrID.length()==0 ) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Error: No "+EEEConstants.EHR_ID+" supplied in request");
			}

			// Check that we actually got an objectID else scream...
			if (objectID==null || objectID.length()==0 ) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Error: No "+EEEConstants.OBJECT_ID+" supplied in request");
			}

			// TODO: Do real lookup here instead of this mockup		
			if (command != null) {
				// Command examples
				//			 * ---- /ehr/{ehrId}/{versionedObject}/{command} ---
				//			 * http://localhost:8182/ehr/1234000/56780007/
				//			 * http://localhost:8182/ehr/1234000/56780007/all_version_ids
				//			 * http://localhost:8182/ehr/1234000/56780007/all_versions
				//			 * http://localhost:8182/ehr/1234000/56780007/revision_history

				if (command.equals(ALL_VERSION_IDS)){
					// TODO: Implement
					List<String> idList;
					try {
						idList = dbReader.getVersionedObject_all_version_ids(ehrID, objectID);
					} catch (Exception e) {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, 
								"Error, could not access database when asking all version IDs of the object "+objectID+" in the EHR "+ehrID, e);
					}
						fileNameBase = this.getClass().getSimpleName()+"-all_version_ids";
						Map<String, Object> variablesForTemplate = new HashMap<String, Object>();
						variablesForTemplate.put("all_version_ids", idList);
						try {
							return handleResponseViaFreemarkerTemplate(selectedMime.getFileSuffix(), selectedMime.getMediaType(), variablesForTemplate );
						} catch (IOException e) {
							throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
						}
					
				} else if (command.equals(ALL_VERSIONS)){
					
					List<Node> versionList;
					try {
						versionList = dbReader.getVersionedObject_all_versions(ehrID, objectID);
					} catch (Exception e) {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, 
								"Error, could not access database when asking all versions of the object "+objectID+" in the EHR "+ehrID, e);
					}
						fileNameBase = this.getClass().getSimpleName()+"-all_versions";
						Map<String, Object> variablesForTemplate = new HashMap<String, Object>();
						variablesForTemplate.put("all_versions", versionList);
						try {
							return handleResponseViaFreemarkerTemplate(selectedMime.getFileSuffix(), selectedMime.getMediaType(), variablesForTemplate );
						} catch (IOException e) {
							throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
						}
				} else if (command.equals(REVISION_HISTORY)){
					// TODO: Implement
					throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED, 
							"When implemented this would return the revision history of the object "+objectID+" in the EHR "+ehrID);
				} else {
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Error: Illegal command string found in URL. " +
							"Allowed ones are - "+ALL_VERSION_IDS+", "+ALL_VERSIONS+", "+REVISION_HISTORY);
				}
				// TODO: Cater for the "blank" command here or elsewhere.

			} else if (lookupString != null) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, 
						"Error: lookupString was available, perhaps Restlet routning was set up wrong, instead try rerouting to "+VersionedObjectResource.class.getCanonicalName());
			} else if (treeID != null && systemID != null) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, 
						"Error: treeID and systemID were available, perhaps Restlet routning was set up wrong, instead try rerouting to "+VersionedObjectResource.class.getCanonicalName());
			} else if (systemID==null && treeID==null && command==null && lookupString==null) {
				// Now the resource was called with only ehrId and objectID, so return same as for command=ALL_VERSION_IDS
				// TODO: Implement
				throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED, 
						"When implemented this would return all version IDs of the object "+objectID+" in the EHR "+ehrID);
			} else {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "This system did not recognize this combination of parameters " +
						"(Parameters found; ehrID:"+ehrID+", objectID:"+objectID+", systemID:"+systemID+", treeID:"+treeID+
						", command:"+command+", lookupString:"+lookupString );
			}

		} catch (ResourceException e) {
			// Set error HTTP status and send an explaning String 
			getResponse().setStatus(e.getStatus());
			return new StringRepresentation(e.toString() + "\nTechnical error details for administrators if available:\n"+e.getCause());
			// FIXME: Create and call a set of freemarker templates to display ResourceExceptions for different Media types
		}
		
	}



}