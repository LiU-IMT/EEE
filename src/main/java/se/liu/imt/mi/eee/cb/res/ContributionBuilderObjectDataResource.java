package se.liu.imt.mi.eee.cb.res;

import java.util.HashMap;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

import se.liu.imt.mi.eee.db.xmldb.ContributionBuilderStorageInXMLDB;
import se.liu.imt.mi.eee.structure.ContibutionBuilderItem;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.utils.FreemarkerSupportResource;

public class ContributionBuilderObjectDataResource extends FreemarkerSupportResource implements EEEConstants {

	protected ContributionBuilderStorageInXMLDB contributionBuildDBHandler;
	protected String committer;
	protected String ehrId;
	protected String contributionID;
	protected String tempID;
	protected HashMap<String, Object> varMap;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		System.out.println("ContributionBuilderObjectDataResource.doInit()");
		
		// Set up a Map to hold variables
		varMap = new HashMap<String, Object>();
		
		// Then possibly overwrite map values with values from request attributes
		varMap.putAll(getRequestAttributes());
		
		// Extract useful strings from the varMap request
		committer = (String) varMap.get(COMMITTER_ID);
		ehrId = (String) varMap.get(EHR_ID);
		contributionID = (String) varMap.get(CONTRIBUTION_BUILD_ID);
		tempID = (String) varMap.get(TEMP_ID);
		
		// Get the db handle from context (used both for POST & GET handling)
		contributionBuildDBHandler = (ContributionBuilderStorageInXMLDB) getContext().getAttributes().get(EEEConstants.KEY_TO_CONTRIBUTION_BUILDER_DB_INSTANCE);
	}	

	protected Representation return404 (String message) {
		if (message == null) message="No such resource found, check your URL (HTTP error 404)";
		setStatus(Status.CLIENT_ERROR_NOT_FOUND, message);
		return new StringRepresentation(message);
	}
	
	@Get("html")
	public Representation handleGetHTML() throws Exception {
		ContibutionBuilderItem cbi = contributionBuildDBHandler.getObjectInContributionBuild(committer, ehrId, contributionID, tempID);
		if (cbi==null) return return404(null);
		HashMap<String, Object> variablesForTemplate = new HashMap<String, Object>();
		variablesForTemplate.put(EEEConstants.DATA, cbi);
		return handleResponseViaFreemarkerTemplate("html", MediaType.TEXT_HTML, variablesForTemplate);
	}

	@Get("xml")
	public Representation handleGetXML() throws Exception {
		ContibutionBuilderItem cbi = contributionBuildDBHandler.getObjectInContributionBuild(committer, ehrId, contributionID, tempID);
		if (cbi==null) return return404(null);
		return new StringRepresentation((CharSequence) cbi.getData(), MediaType.TEXT_XML);
	}
	
	@Put("xml")
	@Post("xml")	
	public Representation handlePutXML(Representation incomingRepresentation) throws Exception {
		ContibutionBuilderItem<String> cbi = contributionBuildDBHandler.getObjectInContributionBuild(committer, ehrId, contributionID, tempID);
		if (cbi==null) return return404(null);
		cbi.setData(incomingRepresentation.getText());
		cbi.mediaTypeHiddenSetter(incomingRepresentation.getMediaType());
		contributionBuildDBHandler.store(committer, ehrId, contributionID, tempID, cbi, sysId);
		return new StringRepresentation("successful post of:\n"+(CharSequence) cbi.getData(), MediaType.TEXT_PLAIN);
	}
	
//	@Post("form")	
//	public Representation handlePost(Representation incomingRepresentation) throws Exception {
//		// FIXME: Complete proper @Post("form") handling
//		return new StringRepresentation("You posted: "+incomingRepresentation.getText(), MediaType.TEXT_PLAIN);
//		
////		ContibutionBuilderItem cbi = contributionBuildDBHandler.getObjectInContributionBuild(committer, ehrId, contributionID, tempID);
////		cbi.setData(incomingRepresentation.getText());
////		cbi.mediaTypeHiddenSetter(incomingRepresentation.getMediaType());
////		contributionBuildDBHandler.store(committer, ehrId, contributionID, tempID, cbi);
////		return new StringRepresentation("successful post of:\n"+(CharSequence) cbi.getData(), MediaType.TEXT_PLAIN);
//	}

	
		
}
