package se.liu.imt.mi.eee.cb.res;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.ObjectVersionID;
import org.openehr.schemas.v1.IMPORTEDVERSION;
import org.openehr.schemas.v1.ORIGINALVERSION;
import org.openehr.schemas.v1.VERSION;
import org.openehr.schemas.v1.impl.VERSIONImpl;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.ext.xml.XmlRepresentation;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Node;

import se.liu.imt.mi.eee.db.EHRDatabaseReadInterface;
import se.liu.imt.mi.eee.db.xmldb.ContributionBuilderStorageInXMLDB;
import se.liu.imt.mi.eee.structure.ContibutionBuilderItem;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.utils.FreemarkerSupportResource;
import se.liu.imt.mi.eee.utils.Util;
import se.liu.imt.mi.ehr.x2010.eeeV1.VERSIONEDCOMPOSITION;
import se.liu.imt.mi.ehr.x2010.eeeV1.VERSIONEDFOLDER;


public class ContributionBuilderObjectResource extends FreemarkerSupportResource implements EEEConstants{

	protected ContributionBuilderStorageInXMLDB contributionBuilderDBHandler;
	
	protected String committer;
	protected String ehrId;
	protected String contributionID;
	protected String objectId;	
	protected String tempID;	
	protected String command;
	protected String changeType;
	protected String objectType;
	protected String lifecycleState;
	protected String precedingVersionUidAsString;
	protected String otherInputVersionUidsAsSpaceSeparatedString;
	protected String uri;
	
	protected AuditChangeType auditChangeType = null;
	protected VersionLifecycleState versionLifecycleState = null;
	protected VersionableObjectType versionableObjectType = null;
	protected ObjectVersionID precedingVersionUid = null;
	protected List<String> otherInputVersionUids = null;
	protected MediaType dataFieldMediaType = null;
	
	protected Map<String,Object> varMap;

	protected String sysID;

	protected EHRDatabaseReadInterface<Node, Node> dbReader;

	
	@Override
	protected void doInit() throws ResourceException {
		super.doInit();	
		
		// Get the db handle from context (used both for POST & GET handling)
		contributionBuilderDBHandler = (ContributionBuilderStorageInXMLDB) getContext().getAttributes().get(EEEConstants.KEY_TO_CONTRIBUTION_BUILDER_DB_INSTANCE);
		sysID = (String) getContext().getAttributes().get(EEEConstants.SYSTEM_ID);
		
		// Get the db handle from context (used both for POST & GET handling)
		dbReader = (EHRDatabaseReadInterface<Node, Node>) getContext().getAttributes().get(EEEConstants.KEY_TO_BASIC_DB_READER);

	
		// Set up a Map to hold variables
		varMap = new HashMap<String, Object>();
	}

	protected void fillVarMap(Map<String, Object> varMap) {
		// First load it with values from the URI query string (?varName1=varValue&varName2=varValue)
		varMap.putAll(getQuery().getValuesMap());
		// Then possibly overwrite map values with values from matrix (;varName1=varValue;)
		varMap.putAll(getMatrix().getValuesMap());
		// Then possibly overwrite map values with values from request attributes
		varMap.putAll(getRequestAttributes());
		// TODO: decide on precedence order of RequestAttributes vs query vs matrix vs document content (and document it)		
		// TODO: document content (first read=default) - matrix - query - ReqestAttributes
	}

	public Representation handleEmptyPost() {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return new StringRepresentation("Body missing in your HTTP call, you sent:"+Util.requestInfoToString(getRequest()));
	}
	
	
	@Post("form")
	public Representation handleFormPost(Representation incomingRepresentation) throws Exception {
//		System.out.println("ContributionBuilderObjectResource.handleFormPost  Remaining part: " + getRequest().getResourceRef().getRemainingPart());
		System.out.println("ContributionBuilderObjectResource.handleFormPost incoming mediatype: "+ incomingRepresentation.getMediaType());
		if (incomingRepresentation == null) return handleEmptyPost();
			Form form = new Form(incomingRepresentation);
			
			fillVarMap(varMap);
			
			// Put values from form into varMap
			varMap.putAll(form.getValuesMap());
			
//			System.out.println("ContributionBuilderObjectResource.handleFormPost(1) "+tempID+"\nVarmap:\n"+varMap);
			convertImportantPostVariablesOrSetDefaultsForInputIfMissing();
//			System.out.println("ContributionBuilderObjectResource.handleFormPost(2) "+tempID);

			String dataAsString = null;
			// TODO:insert command switch here
			// /new/update-version/ + /new/copy-version/ + /new/from-form/ + 
			// /new/from-url/ Security? Avsändare avslöjar user & EHR-ID			
			// /new/from-instance-template/ + /new/from-ehr-path/ + /new/from-xpath/			

			//     /data-from-url/ 
			//     /data-from-freemarker-prototype/  Security? Avsändare avslöjar user & EHR-ID
			
			if (command == null) {
				// Handle update (command == null) EEEConstants.TEMP_ID probably provided in path
				dataAsString = form.getFirstValue(DATA, true);
				System.out.println("ContributionBuilderObjectResource.handleFormPost() - update of existing cb (no command) ");
			} else if (command.equalsIgnoreCase("from-form")) {
				// Handle /new/from-form
				dataAsString = form.getFirstValue(DATA, true);
				System.out.println("ContributionBuilderObjectResource.handleFormPost() - from-form");
			} else if (command.equalsIgnoreCase("update-version")) {
				// Handle /new/update-version
				if (precedingVersionUid == null) {
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "A correct "+PRECEDING_VERSION_ID+" parameter must be supplied when using /update-version/");
				} else {
					String fullXMLAsString=null; 
					
					 ClientResource clRes = new ClientResource(getContext(), "riap://host/ehr:"+ehrId+"/"+precedingVersionUid.getValue());
					 
					 // Reuse the current login when making the call
					 clRes.setChallengeResponse(getChallengeResponse());
					 DomRepresentation repr =  (DomRepresentation) clRes.get(MediaType.APPLICATION_ALL_XML);
					 					 				 
					 VERSION ver = VERSION.Factory.parse(repr.getDocument());
					 fullXMLAsString = ver.xmlText();
					 
					 
//					 ORIGINALVERSION origVer = (ORIGINALVERSION) ver.changeType(ORIGINALVERSION.type);
//					 if (origVer != null) {
//						 dataAsString = origVer.getData().xmlText();
//					 } else {
//						 IMPORTEDVERSION impVer = (IMPORTEDVERSION) ver.changeType(IMPORTEDVERSION.type);
//						 if (impVer != null){						 
//							 dataAsString = impVer.getItem().getData().xmlText();
//						 } else {
//							 throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "The format of the VERSION you tried to update is unknown or unimplemented");
//						 }						 
//					 }
					 
					 // TODO: Consider MERGED_VERSIONs
					 
					 dataAsString = cropXmlDataPart(fullXMLAsString);
					 
				}			
			} else {
				throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED, "The command '"+command+"' is not implemented in this server");
			}
						
			DvText auditDetailsDescription = null; // new DvText("ContributionBuilderObjectResource.handleFormPost");
			System.out.println("ContributionBuilderObjectResource.handleFormPost(3) ");

			ContibutionBuilderItem<String> item = new ContibutionBuilderItem<String>(
						tempID, versionableObjectType , versionLifecycleState, auditChangeType, 
						dataAsString, precedingVersionUidAsString, otherInputVersionUids,
						auditDetailsDescription, dataFieldMediaType); 

			System.out.println("ContributionBuilderObjectResource.handleFormPost(3+) "+item.toString());
			
			contributionBuilderDBHandler.store(committer, ehrId, contributionID, tempID, item, sysID);
			
			System.out.println("ContributionBuilderObjectResource.handleFormPost(4) "+item);
			
			return new StringRepresentation(
					"Succesful post!\n If you submitted from a listing page and use the 'back' function of your browswer, then you might need to reload the page to see the newly added post." + Util.requestInfoToString(getRequest()));
	}

	protected String cropXmlDataPart(String fullXMLAsString) {
		String dataAsString;
		// Get the data field by ugly means...
		 // Now extract the data-part (composition etc) only (since stuff like commit_audit etc are not needed)
		 // FIXME: Do this with proper XML library, not raw string manipulation.				 
		 int start = fullXMLAsString.indexOf("<data");
		 int end = fullXMLAsString.lastIndexOf("</data>")+7;
		 dataAsString = fullXMLAsString.substring(start, end);
		 // Add xsi type URI
		 dataAsString = dataAsString.replaceFirst("xsi:type", "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type");
		return dataAsString;
	}

	protected String fetchData(Reference ref, boolean localCall) {
		String dataAsString;
		if (ref == null) throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "The form field "+URI+" is missing");
		
		// TODO: this is where URI filtering/validation could be inserted (eg restrict to system-local URIs)
		// if (ref.getHostIdentifier() != ... )
		
		Request newRequest = new Request(Method.GET, ref);
		if(!localCall) {
			newRequest.setReferrerRef(getRequest().getHostRef()+"/static/restricted.txt");
			// TODO: do not transfer credentials (if available)
		}
		//if(local) newRequest.

		// Handle it using an HTTP client connector
		Client client = new Client(Protocol.HTTP);
		client.setContext(getContext());
		Response responseFromRemote = client.handle(newRequest);
		dataAsString = responseFromRemote.getEntityAsText();
		// FIXME: Comment out row below 
		System.out.println("ContributionBuilderObjectResource.fetchData("+ref.toString()+") responseFromRemote.getEntityAsText:\n"+dataAsString);
		return dataAsString;
	}
	
//	@Put("xml")
//	@Post("xml")
//	public Representation handleXMLPost(Representation incomingRepresentation) throws IOException{
//		System.out.println("ContributionBuilderObjectResource.handleXMLPost(debug)\n"+
//				Util.requestInfoToString(getRequest()));
//		fillVarMap(varMap);
//		convertImportantVariablesOrSetDefaultsIfMissing();
//		DomRepresentation drep = new DomRepresentation(incomingRepresentation);
//		Request req = getRequest();
//		try {
//			contributionBuilderDBHandler.store(committer, ehrId, contributionID, tempID, drep.getDocument()); 
//			return new StringRepresentation(
//					"Succesful (xml) post!\n" + Util.requestInfoToString(req));
//		} catch (Exception e) {
//			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e);
//			return new StringRepresentation("Failed to store post... -- req attr:"+Util.requestInfoToString(req));
//		}
//	}
	
	@Delete
	public Representation deleteObject() throws Exception {
		fillVarMap(varMap);
		convertIdentifyingVariables();
		contributionBuilderDBHandler.deleteObjectFromContributionBuild(committer, ehrId, contributionID, tempID);
		String message = "Successfully deleted object '"+tempID+ "' from contribution build '"+contributionID+"'.";
		setStatus(Status.SUCCESS_OK, message);
		return new StringRepresentation(message);
		
	}
	
	@Get("html")
	public Representation handleGetHTML() throws Exception {
		fillVarMap(varMap);
		convertIdentifyingVariables();
		ContibutionBuilderItem cbi = contributionBuilderDBHandler.getObjectInContributionBuild(committer, ehrId, contributionID, tempID);
		if (cbi==null) return return404("You asked for a non-existing resource");

		HashMap<String, Object> variablesForTemplate = new HashMap<String, Object>();

		// Make cbi available for freemarker		
		variablesForTemplate.put(EEEConstants.CB_ITEM, cbi);
		
//		// Redundant extras:
//		variablesForTemplate.put(EEEConstants.DATA, cbi.getData());
//		variablesForTemplate.put(EEEConstants.OBJECT_TYPE, cbi.getVersionableObjectType().name());
//		variablesForTemplate.put(EEEConstants.CHANGE_TYPE, cbi.getAuditChangeType().name());
//		variablesForTemplate.put(EEEConstants.LIFECYCLE_STATE, cbi.getVersionLifecycleState().name());
//		variablesForTemplate.put(EEEConstants.PRECEDING_VERSION_ID, cbi.getPreceding_version_uid());
		
		return handleResponseViaFreemarkerTemplate("html", MediaType.TEXT_HTML, variablesForTemplate);
	}
	
	@Get("xml")
	public Representation getRepresentation() {
		// TODO Auto-generated method stub
		//System.out.println("ContributionBuilderObjectResource.getRepresentation()"+ " Remaining part: " + getRequest().getResourceRef().getRemainingPart());
		System.out.println("ContributionBuilderObjectResource.getRepresentation(), request attributes: "+getRequestAttributes().toString());
		System.out.println("ContributionBuilderObjectResource.getRepresentation(--0--)");
		try {
			fillVarMap(varMap);
//		System.out.println("ContributionBuilderObjectResource.getRepresentation(--1a--)");
			convertIdentifyingVariables();
			System.out.println("ContributionBuilderObjectResource.getRepresentation(--1b--)");
			ContibutionBuilderItem cbi = contributionBuilderDBHandler.getObjectInContributionBuild(committer, ehrId, contributionID, tempID);
			System.out.println("ContributionBuilderObjectResource.getRepresentation(--2--)");

			if (cbi != null) {
				System.out.println("ContributionBuilderObjectResource.getRepresentation(--x--)");
				System.out.println("ContributionBuilderObjectResource.getRepresentation() :: returning ::\n 2str= "+cbi.toString() + " mt="+cbi.getMediaType());
				System.out.println("ContributionBuilderObjectResource.getRepresentation(--y--)");
				return new ObjectRepresentation<Serializable>((Serializable) cbi.getData(), cbi.mediaTypeHiddenGetter()) ;				
			} else {
				return return404("You asked for a non-existing resource");
			}
		} catch (ResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			getResponse().setStatus(e.getStatus(), e);		
			return new StringRepresentation(e.toString());
		}		
	}

	protected void convertIdentifyingVariables() {
		// Extract useful strings from the varMap request
		committer = (String) varMap.get(COMMITTER_ID);
		ehrId = (String) varMap.get(EHR_ID);
		contributionID = (String) varMap.get(CONTRIBUTION_BUILD_ID);
		tempID = (String) varMap.get(TEMP_ID);
	}

	// TODO: Push some of this out to router (setting defaults, what's mandatory etc) and document that in WADL?
	protected void convertImportantPostVariablesOrSetDefaultsForInputIfMissing() {
		
		convertIdentifyingVariables();
		
		command = (String) varMap.get(COMMAND);
		
		System.out.println("ContributionBuilderObjectResource.fillVarMap() tempID="+tempID);
		objectType = (String) varMap.get(OBJECT_TYPE);				
		changeType = (String) varMap.get(CHANGE_TYPE);
		lifecycleState = (String) varMap.get(LIFECYCLE_STATE);
		precedingVersionUidAsString = (String) varMap.get(PRECEDING_VERSION_ID);
		otherInputVersionUidsAsSpaceSeparatedString = (String) varMap.get(OTHER_INPUT_VERSION_UIDS);
		uri = (String) varMap.get(URI);
		String mt_in_varmap = (String) varMap.get(DATA_FIELD_MEDIA_TYPE);
	
		if (mt_in_varmap==null) {
			dataFieldMediaType = MediaType.TEXT_XML;
		} else {
			dataFieldMediaType = MediaType.valueOf(mt_in_varmap);
		}
	
		if (tempID==null) {
			tempID = "no-name-"+System.currentTimeMillis();
		}
		
		// TODO: Decide on defaults and implement as null detection followed by default
		if (changeType==null) {
			auditChangeType = AuditChangeType.creation;
		} else {
			auditChangeType = AuditChangeType.valueOf(changeType); // Default
		}
	
		if (objectType==null) {
			// Possibly analyze Entity content to figure out type later,
			// but for now, throw error instead. Clients need to send type!
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, 
					"You (currently) need to supply a versionable openEHR " +
					"object type (e.g. COMPOSITION) in your call. (Error produced " +
					"in ContributionBuilderObjectResource.convertImportantPostVariablesOrSetDefaultsIfMissing)");
		} else {
			versionableObjectType = VersionableObjectType.valueOf(objectType); // Default
		}
		
		if (lifecycleState==null) {
			versionLifecycleState = VersionLifecycleState.complete; // Default
		} else {
			versionLifecycleState = VersionLifecycleState.valueOf(lifecycleState);
		}
		
		if (precedingVersionUidAsString != null && precedingVersionUidAsString.length()>0) {
			try {
				precedingVersionUid = new ObjectVersionID(precedingVersionUidAsString);
			} catch (Exception e) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "the value of"+PRECEDING_VERSION_ID+" could not be parsed correctly");
			}
		}
		
		// Split and convert space-separated IDs
		if (otherInputVersionUidsAsSpaceSeparatedString != null && otherInputVersionUidsAsSpaceSeparatedString.length()>0){
			String[] splitsArray = otherInputVersionUidsAsSpaceSeparatedString.split(" ");
			otherInputVersionUids = new ArrayList<String>(); 
			for (int i = 0; i < splitsArray.length; i++) {
				ObjectVersionID ovid = new ObjectVersionID(splitsArray[i]); // This checks that ovid strings are valid
				otherInputVersionUids.add(ovid.getValue());
			}
		}	
	}

	// TODO: Remove and replace by ResourceException
	protected Representation return404 (String message) {
		if (message == null) message="No such resource found, check your URL (HTTP error 404)";
		setStatus(Status.CLIENT_ERROR_NOT_FOUND, message);
		return new StringRepresentation(message);
	}
	
//	@Put
//	@Post
//	public Representation handleDefaultPost(Representation incomingRepresentation) {
//		System.out.println("ContributionBuilderObjectResource.handleDefaultPost incoming mediatype: "+ incomingRepresentation.getMediaType());
//		if (incomingRepresentation == null) return handleEmptyPost();
//		try {
//			fillVarMap(varMap);
//			convertImportantVariablesOrSetDefaultsIfMissing();
//			ContibutionBuilderItem item = new ContibutionBuilderItem(
//					tempID, versionableObjectType , versionLifecycleState, auditChangeType, 
//					incomingRepresentation, precedingVersionUid, otherInputVersionUids, 
//					null, incomingRepresentation.getMediaType());
//			contributionBuilderDBHandler.store(committer, ehrId, contributionID, tempID, item);
//			
//			return new StringRepresentation(
//					"Succesful post!\n" + Util.requestInfoToString(getRequest()));
//		} catch (Exception e) {
//			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e);
//			return new StringRepresentation("Failed to store post... \n" +
//					"Error: " +e.getMessage() + "\n"+
//					"Request debug info:\n"+Util.requestInfoToString(getRequest()));
//		}
//	}

	
}
