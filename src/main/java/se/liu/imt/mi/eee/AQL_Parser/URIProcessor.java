/**
 * 
 */
package se.liu.imt.mi.eee.AQL_Parser;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.resource.ClientResource;

/**
 * Simple class for handling URIs in AQL queries.
 * 
 * @author Daniel Karlsson, daniel.karlsson@liu.se
 * 
 */
public class URIProcessor {
	/**
	 * Method for handling URIs in AQL queries. The resource in the URI is
	 * called and the result is returned. If any exception is caught, an empty
	 * string is returned.
	 * 
	 * TODO: Consider if exceptions should be thrown further instead of returning empty string upon error //ES
	 * 
	 * @param uri
	 *            The URI of the resource to be called.
	 * @return The result of calling the resource.
	 */
	static String processURI(String uri) {
		
		String newUri = uri.replaceAll("\\\\\\{", "{").replaceAll("\\\\\\}", "}");
		
		System.out.println("URIProcessor.processTerminologyURI(): URI in = "
				+ newUri);

		ClientResource uriResource = new ClientResource(newUri);
		uriResource.getClientInfo().getAcceptedMediaTypes()
				.add(new Preference<MediaType>(MediaType.TEXT_CSV));

		try {
			uriResource.get();
			if (uriResource.getStatus().isSuccess()
					&& uriResource.getResponseEntity().isAvailable())
				return uriResource.getResponseEntity().getText();
		} catch (Exception e) {
			System.out.println("URIProcessor.processURI(): Exception caught");
			e.printStackTrace(System.out);
		}

		return "";

	}
}
