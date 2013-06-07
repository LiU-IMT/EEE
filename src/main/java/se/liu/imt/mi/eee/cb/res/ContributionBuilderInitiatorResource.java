package se.liu.imt.mi.eee.cb.res;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import se.liu.imt.mi.eee.db.xmldb.ContributionBuilderStorageInXMLDB;
import se.liu.imt.mi.eee.structure.EEEConstants;

/**
 * This class accepts POST requests and creates a new blank Contribution Build (in the DB) and redirects
 * to the newly created resource. (The client is then supposed to POST objects to that created 
 * ContributionResource.) 
 * 
 * If you want to write to the default contribution build, then you don't need to call this resource first, since the contribution 
 * build named "default" will be automatically generated if missing.
 * 
 * @author Erik Sundvall
 */
public class ContributionBuilderInitiatorResource extends WadlServerResource implements EEEConstants{

	ContributionBuilderStorageInXMLDB dbConnection;
	
	public ContributionBuilderInitiatorResource() {
		super();
		setName("Contribution Builder Initiator");
		setDescription("Creates a new empty Contribuition Build and redirects to it. A query parameter 'description' can be used to set a customized name of the contribution");
	}

	@Override
	protected void doInit() throws ResourceException {
		// TODO Auto-generated method stub
		super.doInit();
		dbConnection = (ContributionBuilderStorageInXMLDB) getContext().getAttributes().get(EEEConstants.KEY_TO_CONTRIBUTION_BUILDER_DB_INSTANCE);
	}

	/**
	 * Call this with POST to initiate creation of a new 
	 * @return Returns a StringRepresentation containing a string 
	 * formatted as /contributionBuilder/{composerId}/{ehrId}/{cb-id}
	 */
	@Post
	public Representation handlePost(Representation reprIn) {
		getLogger().entering(this.getClass().getCanonicalName(), "handlePost", getRequestAttributes().toString());

		String committer = (String) getRequestAttributes().get(COMMITTER_ID);
//		// Using IP for now, should change to logged in user later
//		if (getRequest().getClientInfo().getUser() != null){
//			committer = getRequest().getClientInfo().getUser().getIdentifier();	
//		}
//		if (committer == null) { // TODO: also check committer.isEmpty
//			committer = "IP_" + getRequest().getClientInfo().getUpstreamAddress();
//		}

		String ehrId = (String) getRequestAttributes().get(EHR_ID);
		Form decodedForm = new Form(getRequestEntity());
		String descriptionText = decodedForm.getFirstValue("description", true, "Default Contribution Description");
//		System.out.println("ContributionBuilderInitiatorResource.createContributionBuilderID(desc): "+descriptionText);
//		System.out.println("ContributionBuilderInitiatorResource.createContributionBuilderID(q): "+decodedForm);

		
		try {
			String createdCBID = createContributionBuilderID(committer, ehrId, descriptionText);
			String path = "../"+createdCBID+"/";
			getResponse().setStatus(Status.SUCCESS_CREATED);
			getResponse().redirectSeeOther(path);
			return new StringRepresentation("Successful creation of Contribution build with path: "
					+ path, MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			// TODO: add standard error handling / Throw
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			return new StringRepresentation("An error ocurred while trying to initiate a new countribution build: \n"+e.getLocalizedMessage(), MediaType.TEXT_PLAIN);
		}
	}
	
	private String createContributionBuilderID(String committer, String ehrId, String descriptionText) throws Exception {		
		//The openEHR java UUID implementation does not create new UUIDs
		//thus we shift the task to java.util.UUID currently
		String contributionUUID = java.util.UUID.randomUUID().toString();		
		dbConnection.createNewEmptyContributionBuild(committer, ehrId, contributionUUID, (String) getContext().getAttributes().get(SYSTEM_ID), descriptionText );
		return contributionUUID;
	}
	
}
