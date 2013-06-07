package se.liu.imt.mi.eee.cb.res;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Node;

import se.liu.imt.mi.eee.db.xmldb.ContributionBuilderStorageInXMLDB;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.utils.FreemarkerSupportResource;

public class ContributionBuilderListResource extends FreemarkerSupportResource implements EEEConstants {

	private static final String KEY_TO_CB_LIST = "keyToCBList";
	
	private ContributionBuilderStorageInXMLDB contributionBuildDBHandler;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		System.out.println("ContributionBuilderListResource.doInit()");
		// Get the db handle from context (used both for POST & GET handling)
		contributionBuildDBHandler = (ContributionBuilderStorageInXMLDB) getContext().getAttributes().get(EEEConstants.KEY_TO_CONTRIBUTION_BUILDER_DB_INSTANCE);
	}	

	protected List populateListAndSetFileNameBase() throws Exception{
		String committer = (String) getRequestAttributes().get(COMMITTER_ID);
		if (committer== null || committer.length()==0) {			
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Missing request parameter: "+COMMITTER_ID);
			this.fileNameBase = "ContributionBuilder-root";
			return null;
		}
		String ehrId = (String) getRequestAttributes().get(EHR_ID);
		if (ehrId == null || ehrId.length()==0) {
			// No EHR_ID supplied, then list all EHRs that the 
			// committer has active contribution builders for.
			this.fileNameBase = this.getClass().getSimpleName() + "-activeEhrIds";
			//System.out.println("1. ContributionBuilderListResource.populateListAndSetFileNameBase() c="+committer+" e="+ehrId);
			return contributionBuildDBHandler.getactiveEhrIds(committer);
		}			
		String contribId = (String) getRequestAttributes().get(CONTRIBUTION_BUILD_ID);
		if (contribId == null || contribId.length()==0) {
			// No contribution supplied, then list all contributions 
			// that the user has active contribution builders for in this EHR.
			// Note: a List<Couple<String, Node>> returned, see contributionBuildDBHandler.getActiveBuildsMetadataForEhr()
			this.fileNameBase = this.getClass().getSimpleName()+"-activeBuildsForEhr";
			//System.out.println("2. ContributionBuilderListResource.populateListAndSetFileNameBase() c="+committer+" e="+ehrId);
			return new ArrayList<Entry<String,Node>>(contributionBuildDBHandler.getActiveBuildsMetadataForEhr(committer, ehrId).entrySet());
		}		
		// Contribution, committer and EHR supplied, then list the objects
		this.fileNameBase = this.getClass().getSimpleName()+"-temporaryObjectIDs";
		return contributionBuildDBHandler.getTempObjectIdsInContributionBuild(committer, ehrId, contribId);		
	}

//	// extract temp-id-name from post and redirect to correct resource
//	@Post("form")
//	public Representation handleFormPost(Representation incomingRepresentation) {
//		System.out.println("ContributionBuilderListResource.handleFormPost()" +
//			"\n  Remaining part: " + getRequest().getResourceRef().getRemainingPart()); // +
////				"\n  Incoming mediatype: "+ incomingRepresentation.getMediaType());
//		if (incomingRepresentation == null) {
//			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Body missing in your HTTP call, you sent:"+Util.requestInfoToString(getRequest()));
//		}
//		Form form = new Form(incomingRepresentation);
//		String idFromForm = form.getFirstValue(EEEConstants.TEMP_ID, true);
//		if (idFromForm == null || idFromForm.equalsIgnoreCase("")){
//			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, EEEConstants.TEMP_ID+" missing in your sent form, you sent:"+Util.requestInfoToString(getRequest()));
//		}
//		String datafield = form.getFirstValue(EEEConstants.DATA, true);
//		if (datafield == null || datafield.equalsIgnoreCase("")){
//			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, EEEConstants.DATA+" missing in your sent form, you sent:"+Util.requestInfoToString(getRequest()));
//		}
//		String targetString = ""+idFromForm+"/";
//		
//		//		String target = "./{idFromForm}/";
////		Redirector redirector = new Redirector(getContext(), target, Redirector.MODE_SERVER_INBOUND);
////		return redirector.start()
//		
////		// Prepare the request
////		Request request = new Request(Method.POST, postToUrl);
////		request.setReferrerRef("http://www.mysite-rererrer.org");
////		DomRepresentation drep = new DomRepresentation(MediaType.APPLICATION_XML, d);		
////		request.setEntity(drep);
//		
////		Create the resource if missing...
//		
//		WrapperRequest wreq = new WrapperRequest(getRequest());
//		wreq.setResourceRef(targetString);
//		String text;
//		try {
//			text = incomingRepresentation.getText();
//		} catch (IOException e) {
//			throw new ResourceException(e);
//		}
//		System.out.println("ContributionBuilderListResource.handleFormPost() datafield "+datafield);
//		wreq.setEntity(datafield, MediaType.APPLICATION_WWW_FORM);
//
//		// Handle it using an HTTP client connector
//		Client client = new Client(Protocol.HTTP);
//		Response response = client.handle(wreq);
//
//		return response.getEntity();
////		org.restlet.data.Reference ref = new 
////		getResponse().redirectPermanent(targetString); // TODO: remove /data/ once target fixed
////		return new StringRepresentation("ContributionBuilderListResource.handleFormPost() returning", MediaType.TEXT_PLAIN);
//	}


	@Get("html")
	public Representation handleGetHTML() throws Exception {
		List<String> list = populateListAndSetFileNameBase();
		HashMap<String, Object> variablesForTemplate = new HashMap<String, Object>();
		if (list !=null) variablesForTemplate.put(KEY_TO_CB_LIST, list);
		return handleResponseViaFreemarkerTemplate("html", MediaType.TEXT_HTML, variablesForTemplate);		
	}

	@Get("json")
	public Representation handleGetJSON() throws Exception {
		List<String> list = populateListAndSetFileNameBase();
		if (list == null) {
			// TODO: consider cleaner handling in router instead?
			throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED, "No JSON information file implemented yet, see HTML-version instead");
		} else {
			return new JsonRepresentation(new JSONArray(list, false));			
		}
	}
		
}
