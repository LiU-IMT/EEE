package se.liu.imt.mi.eee.utils;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.structure.EEEConstants.MimeHelper;
import sun.security.util.BigInt;

public class HashUtilResource extends FreemarkerSupportResource{
	
	String command, data;
	
	protected void doInit() throws org.restlet.resource.ResourceException {
		command = getQuery().getFirstValue(EEEConstants.COMMAND);
		data = getQuery().getFirstValue(EEEConstants.DATA);
	};
		
	// FIXME: Add fetching and local caching of images here instead of only redirecting, also hide referring page when making external calls (to avoid information leakage)
	@Get
	public Representation getHash(Variant variant) throws ResourceException, IOException {		
		if (command == null || command.isEmpty()) {
			// Return help page with form
			Map<String, Object> variablesForTemplate = new HashMap<String, Object>();
			if(data != null){
				variablesForTemplate.put("MD5", DigestUtils.md5Hex(data));
				
				byte[] md5byteArray = DigestUtils.md5(data);

				BigInteger bint = new BigInteger(md5byteArray);

				String javaConversions = "BigInteger bint = new BigInteger(md5byteArray);" +"\r\n"+
				"bint.toString() = "+bint.toString()+"\r\n"+
				"bint.intValue() = "+bint.intValue()+"\r\n"+
				"bint.longValue() = "+bint.longValue()+"\r\n"+
				"bint.floatValue() = "+bint.floatValue()+"\r\n"+
				"ByteBuffer.wrap(md5byteArray).getInt() = "+ByteBuffer.wrap(md5byteArray).getInt();
				
				variablesForTemplate.put("java_conversions", javaConversions);
				variablesForTemplate.put("MD5_dec_bigint", bint.toString());
				variablesForTemplate.put("MD5_dec_int", String.valueOf(bint.intValue()));
				variablesForTemplate.put("SHA1", DigestUtils.shaHex(data));
				variablesForTemplate.put("data", data);				
			}
			return handleResponseViaFreemarkerTemplate(MimeHelper.HTML.getFileSuffix(), MimeHelper.HTML.getMediaType(), variablesForTemplate );			
		} else if (command.equals("MD5")) {
			return new StringRepresentation(DigestUtils.md5Hex(data), MediaType.TEXT_PLAIN);
		} else if (command.equals("SHA1")) {
			return new StringRepresentation(DigestUtils.shaHex(data), MediaType.TEXT_PLAIN);
//		} else if (command.equals("gravatar")) {
//			String md5Hex = DigestUtils.md5Hex(data);
//			Form q = getPurgedQuery();
//			// Use identicon as default fallback
//			if (q.getFirst("d") == null) q.add("d", "identicon");
//			getResponse().redirectTemporary("http://www.gravatar.com/avatar/"+md5Hex+"?"+q.getQueryString());
//			return new StringRepresentation(DigestUtils.shaHex(data), MediaType.TEXT_PLAIN);
//		} else if (command.equals("robohash")) {
//			String md5Hex = DigestUtils.md5Hex(data);
//			Form q = getPurgedQuery();
//			String s = q.getFirstValue("s");
//			if (s != null) {
//				q.add("size", s+"x"+s);
//				q.remove(q.indexOf(q.getFirst("s")));
//			}
//			// q.add("gravatar", "hashed");
//			getResponse().redirectTemporary("http://robohash.org/"+md5Hex+"?"+q.getQueryString());
//			return new StringRepresentation(DigestUtils.shaHex(data), MediaType.TEXT_PLAIN);			
		} else {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "The command '"+command+"' is unknown by this implementation");
		}
	}

//	protected Form getPurgedQuery() {
//		Form q = getQuery();
//		int indexOf = q.indexOf(q.getFirst(EEEConstants.COMMAND));
//		q.remove(indexOf);
//		q.remove(q.indexOf(q.getFirst(EEEConstants.DATA)));
//		return q;
//	}

/* Examples of auto-generated images
 * http://robohash.org/
 * https://github.com/thevash/vash and http://www.thevash.com/
 * Identicons:
 * 	http://en.wikipedia.org/wiki/Identicon
 * 
 * Many of these can be fetched via gravatar parameters d=... and f=y 
 * see explanation on http://en.gravatar.com/site/implement/images/
 *  
 * Strange alternative: http://unicornify.appspot.com/use-it
 */
	
}
