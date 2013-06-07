package se.liu.imt.mi.eee.utils;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;


public class TraceResource extends WadlServerResource {
	
	protected Status myStatus = Status.SUCCESS_OK;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		this.setName("Trace-printout helper for debugging.");
		this.setDescription("This resource can be called with any HTTP method and will return a plain text containing debug information. Useful primarily for debugging and development phases.");
	}

	@Override
	public Representation handle() {

		// Print the requested URI path
		String message = "";

		message = message + "\nHTTP Method: "
			+ getRequest().getMethod().getName();

		message = message + "\n------------------------------ \n" +
				"The server believes this response was returned to you with the HTTP status code: "+myStatus.getCode()+"\n"
				+ " ...meaning: "+myStatus+ "\n-------------------------------"; 

		User user = getRequest().getClientInfo().getUser();
		
		message = message
				+ '\n' + "Resource URI  : " + getRequest().getResourceRef()
				+ '\n' + "Root URI      : " + getRequest().getRootRef()
				+ '\n' + "Routed part   : " + getRequest().getResourceRef().getBaseRef() + '\n'
				+ "Remaining part: "
				+ getRequest().getResourceRef().getRemainingPart()
										+ "\nRelative part: "
				+ getRequest().getResourceRef().getRelativePart()
				+ "\n\nAttributes: " 
				+ getRequest().getAttributes().toString()
				+ "\n------------------------------ \n" 
				+ "Logged in user (if any) :" + user; 

		Representation entity = getRequest().getEntity();	
		if (entity != null) {
			try {
				message = message + "\n------------------------------ " + 
				"\nRequest entity content: "
						+ entity.getText();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				message = message + "\n Failed getting entity content"
						+ e.getMessage();
			}
		}
		

		
		//setResponse(new Response(getRequest()).set);
		StringRepresentation repr = new StringRepresentation("Trace printout:\n"+message, MediaType.TEXT_PLAIN);
		getResponse().setEntity(repr);
		getResponse().setStatus(myStatus, message);
		
		return repr;
	}	
	
}
