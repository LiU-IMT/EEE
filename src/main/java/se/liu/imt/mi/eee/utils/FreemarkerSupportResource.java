package se.liu.imt.mi.eee.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.security.User;

import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.structure.EEEConstantsAsHashMap;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class FreemarkerSupportResource extends ServerResource {

	private static final Object EEEConstantsInstance = new EEEConstantsAsHashMap();

	protected String sysId;
	protected String fileNameBase;

	private Context ctx;
	
	/**
	 * Will assign a base (prefix) name for freemarker template files based on this.getClass().getSimpleName()
	 */
	public FreemarkerSupportResource() {
		super();
		this.fileNameBase = this.getClass().getSimpleName();
		//System.out.println("FreemarkeSupportResource constructor assigned fileName = "+fileNameBase);
	}
	
	/**
	 * Use this constructor to manually assign a base (prefix) name for freemarker template files
	 */
	public FreemarkerSupportResource(String fileName, Context ctx) {
		super();
		this.fileNameBase = fileName;
		this.ctx=ctx; 
		//System.out.println("FreemarkeSupportResource constructor assigned fileName = "+this.fileNameBase +" Context="+ctx);
	}

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		// Extract strings from request
		sysId = (String) getContext().getAttributes().get(
				EEEConstants.SYSTEM_ID);
		//this.setAutoDescribing(true);
		//this.setName("TEST EEE");
		//System.out.println("FreemarkerSupportResource.doInit() -> " + this.isAutoDescribing());
	}
	
	@Override
	protected void doCatch(Throwable arg0) {
		// TODO Auto-generated method stub
		getContext().getLogger().throwing(this.getClass().getCanonicalName(), "doCatch", arg0);
		System.out.println("FreemarkerSupportResource.doCatch() (Called in class: "+this.getClass().getCanonicalName()+") Stacktrace follows: ");
		arg0.printStackTrace();
		super.doCatch(arg0);
		if (arg0 instanceof ResourceException){
			ResourceException rex = (ResourceException) arg0;
			getResponse().setStatus(rex.getStatus());
			String txt = "Description: "+rex.getStatus().getDescription()+"\n"+
						 "HTTP error: "+rex.getStatus().getCode()+" ("+rex.getStatus().getName()+")\n";
			getResponse().setEntity(txt, MediaType.TEXT_PLAIN);
		} else {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			String txt = "Server side java error message: "+arg0.getLocalizedMessage()+"\n"+
			"Server side java error type: "+arg0.getClass().getName()+"\n"+
			"Server side java error printout: "+arg0.toString()+"\n"+
			 "(Generated HTTP error: "+getResponse().getStatus().getCode()+" ("+getResponse().getStatus().getName()+")\n";
			getResponse().setEntity(txt, MediaType.TEXT_PLAIN);
		}
	}

	public Representation handleResponseViaFreemarkerTemplate(String fileInfix, MediaType mediaTypeToReturn, Map<String, Object> variablesForTemplate)
	throws IOException {
		
		String freemarkerTemplateFilename =  fileNameBase+ "." + fileInfix + ".ftl";

//		System.out.println("FreemarkerSupportResource.handleResponseViaFreemarkerTemplate() getRequest="+getRequest());
		User user = getRequest().getClientInfo().getUser();
		//System.out.println("FreemarkerSupportResource.handleResponseViaFreemarkerTemplate user:"+user+" +user.getFirstName:"+user.getFirstName());
//		char[] secret = user.getSecret();
//		if (secret != null) {
//			System.out.println("FreemarkerSupportResource.handleResponseViaFreemarkerTemplate(user.secret): "+secret.toString());	
//		}
//		assert (secret == null); // secret should not be available to freemarker template authors 		
		variablesForTemplate.put("currentUser", user);
		String ehrId = (String) getRequestAttributes().get(EEEConstants.EHR_ID);
		variablesForTemplate.put("ehrId", ehrId);
		variablesForTemplate.put("EEE_debug_callingClassName", this.getClass().getName());
		variablesForTemplate.put("EEE_debug_freemarkerTemplateFilename", freemarkerTemplateFilename);

		if (ctx == null) {
			ctx = getContext();
		}

		variablesForTemplate.put("contextAttributes", ctx.getAttributes());
		variablesForTemplate.put("requestAttributes", getRequestAttributes());
		variablesForTemplate.put("httpQuery", getQuery());
		variablesForTemplate.put("httpMatrix", getMatrix());
		variablesForTemplate.put("EEEConstants", EEEConstantsInstance);

		Configuration freemarkerConfiguration = (Configuration) ctx.getAttributes().get(
				EEEConstants.KEY_TO_FREEMARKER_CONFIGURATION);
		Template templ = freemarkerConfiguration.getTemplate(freemarkerTemplateFilename); // TODO: Consider if this should be a shared instance instead of creating new.
	
		return new TemplateRepresentation(templ, variablesForTemplate, mediaTypeToReturn);
		
	}

}