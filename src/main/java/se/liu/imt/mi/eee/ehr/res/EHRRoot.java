package se.liu.imt.mi.eee.ehr.res;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.restlet.data.CacheDirective;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import se.liu.imt.mi.eee.db.EHRDatabaseWriteInterface;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.utils.FreemarkerSupportResource;

public class EHRRoot extends FreemarkerSupportResource {

	private static final int EXPIRATION_IN_SECONDS = 120; // TODO: Make longer
	protected EHRDatabaseWriteInterface<?> dbWriter;
	
	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		dbWriter = (EHRDatabaseWriteInterface<?>) getContext().getAttributes()
				.get(EEEConstants.KEY_TO_BASIC_DB_WRITER);
	}

	@Get("html")
	public Representation handleGetHTML() throws Exception {
		Map<String, Object> variablesForTemplate = new HashMap<String, Object>();		
		Representation reprToReturn;
			reprToReturn = handleResponseViaFreemarkerTemplate("html", MediaType.TEXT_HTML, variablesForTemplate);
			CacheDirective cd = CacheDirective.maxAge(EXPIRATION_IN_SECONDS);
			getResponse().getCacheDirectives().add(cd);
			reprToReturn.setExpirationDate(new Date(System.currentTimeMillis()+EXPIRATION_IN_SECONDS*1000)); // Allow static caching for a while  
		return reprToReturn;
	}
	
	/**
	 * Creates a new EHR ready for writing.
	 * Takes care of url-encoded forms sent with POST using the
	 * application/x-www-form-urlencoded mediatype, e.g. forms from HTML like
	 * <FORM METHOD=POST ENCTYPE="application/x-www-form-urlencoded" ...
	 * 
	 * @param incomingPostedRepresenation
	 * @throws Exception
	 */
	@Post("form")
	public Representation handleFormPost(Representation incomingPostedRepresentation) throws Exception {
		// TODO: Change to throw ResourceException
		Form decodedForm = new Form(getRequestEntity());
		// Extract form value
		String suggestedEHR_ID = decodedForm.getFirstValue(EEEConstants.EHR_ID, true);
		try {
			// TODO: consider auto-assigning EHR IDs if suggestedEHR_ID = null/blank by calling dbWriter.createEHR(systemId) instead
			dbWriter.createEHR(suggestedEHR_ID, sysId);
			
			// FIXME: Also prime ehr metadata cache for ETag with something like: suggestedEHR_ID+"-created-"+IsoDateTime
			System.out.println("EHRRootResource.handleFormPost() created EHR: " + suggestedEHR_ID);
		} catch (Exception e) {
			return new StringRepresentation("Creation of EHR with ID "
					+ suggestedEHR_ID + " failed. Details: " + e.getMessage(),
					MediaType.TEXT_PLAIN);
		}
		//getResponse().redirectSeeOther(suggestedEHR_ID + "/");
		getResponse().setLocationRef(suggestedEHR_ID + "/");
		getResponse().setStatus(Status.SUCCESS_CREATED, "Successful creation of EHR with ID: "+ suggestedEHR_ID);
		return new StringRepresentation("Successful creation of EHR with ID: <a href=\"../ehr:"+suggestedEHR_ID+"/\">"+
				 suggestedEHR_ID +"</a>", MediaType.TEXT_HTML);
	}

}
