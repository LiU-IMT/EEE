package se.liu.imt.mi.eee.utils;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CacheDirective;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import se.liu.imt.mi.eee.DemoStarter;
import se.liu.imt.mi.eee.structure.EEEConstants;
import freemarker.template.Configuration;

/**
 * A helper class that generates a page based on freemarker templates by spawning a {@link FreemarkerSupportResource} 
 * @author erisu
 */
public class InfoRestlet extends Restlet {

	protected String sysId;
	protected Configuration freemarkerConfiguration;
	protected String fileNameBase;
	private int expirationInSeconds;

	// NOTE: This is a Restlet not a Resource and thus (at least the handle()
	// method) probably needs to be multi-thread safe!

	// FIXME: Add caching timeout directives to constructor and use them to produce http-headers in handle(...)
	
	/*
	 * Use this constructor to manually 
	 */
	
	/**
	 * @param fileNameBase assigns a base (prefix) name for freemarker template files
	 */
	public InfoRestlet(String fileNameBase, Context context) {
		super(context);
		this.fileNameBase = fileNameBase;
		// Extract strings from request
		sysId = (String) getContext().getAttributes().get(
				EEEConstants.SYSTEM_ID);
		freemarkerConfiguration = (Configuration) getContext().getAttributes()
				.get(EEEConstants.KEY_TO_FREEMARKER_CONFIGURATION);
		//setName("Fremarker file '" + fileNameBase + ".*.ftl' based response");
		// TODO: Add wadl description generation via setDescription(description);
		this.setName("InfoRestlet: "+fileNameBase);
		this.setDescription("Implemented using InfoRestlet that calls the freemarker files prefixed with: "+fileNameBase);
		// TODO: Check available template file extensions and allow the right
		// content types (disallow others)
		
		expirationInSeconds = DemoStarter.getConfig().getInt(DemoStarter.IDENTIFIED_VERSIONS_MAXAGE);
	}

//	/**
//	 * @param fileNameBase assigns a base (prefix) name for freemarker template files
//	 * @param expirationInSeconds sets the number of seconds from now when the returned representation expires. Default is 120 seconds (only suitable for development).
//	 */
//	public InfoRestlet(String fileNameBase, Context context, int expirationInSeconds) {
//		this(fileNameBase, context);
//		this.expirationInSeconds = expirationInSeconds;
//	}

	@Override
	public void handle(Request req, Response resp) {
		super.handle(req, resp);

		// TODO: add support for proper return type negotiation etc here (see Restlet Metadata class);
				
		MediaType mediaTypeToReturn = req.getClientInfo().getPreferredMediaType(Util.getSupportedMediaTypeList());

		String fileInfix = Util.getMimeHelperFromMediaType(mediaTypeToReturn).getFileSuffix();

		Map<String, Object> variablesForTemplate = new HashMap<String, Object>();

//		System.out.println("InfoRestlet.handle() ======= "+getContext());
		FreemarkerSupportResource fsr = new FreemarkerSupportResource(
				fileNameBase, getContext());
		fsr.setRequest(req);
		fsr.setResponse(resp);
		try {
			Representation repr = fsr.handleResponseViaFreemarkerTemplate(fileInfix, mediaTypeToReturn, variablesForTemplate);
			CacheDirective cd = CacheDirective.maxAge(expirationInSeconds);
			fsr.getResponse().getCacheDirectives().add(cd);
			repr.setExpirationDate(new Date(System.currentTimeMillis()+expirationInSeconds*1000));   
			resp.setEntity(repr);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
					"Could not return freemarker template based on fileNameBase='"
							+ fileNameBase + "'", e);
		}
		return;
	}

}
