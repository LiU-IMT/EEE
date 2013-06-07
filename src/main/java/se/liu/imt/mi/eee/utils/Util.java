package se.liu.imt.mi.eee.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.openehr.binding.XMLBinding;
import org.openehr.binding.XMLBindingException;
import org.openehr.build.SystemValue;
import org.openehr.rm.common.changecontrol.Contribution;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.support.identification.ObjectRef;
import org.openehr.rm.support.identification.ObjectVersionID;
import org.openehr.rm.support.identification.UID;
import org.openehr.rm.support.identification.UUID;
import org.openehr.rm.support.measurement.SimpleMeasurementService;
import org.openehr.schemas.v1.AUDITDETAILS;
import org.openehr.schemas.v1.HIEROBJECTID;
import org.openehr.schemas.v1.OBJECTREF;
import org.openehr.terminology.SimpleTerminologyService;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;

import se.liu.imt.mi.eee.DemoStarter;
import se.liu.imt.mi.eee.db.EHRDatabaseReadInterface;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.structure.EEEConstants.MimeHelper;
import se.liu.imt.mi.eee.trigger.EhrMetadataCache;
import se.liu.imt.mi.ehr.x2010.eeeV1.CONTRIBUTION;

public class Util {
	
	
	/**
	 * Characters that are both URL-safe and easily distinguishable from each other
	 * by human readers (where we don't know what font they'll be using).
	 * Lower case letters a-z except l (lower case L) sometimes mistaken for 1
	 * Upper case letters A-Z except I and O (upper case i and o) easily mistaken for l, 1 and 0 
	 * Numbers 2-9 (thus excluding one and zero). 
	 * 0 (zero) is used as null
	 * (The hyphen sign '-' and underscore '_' would also be URL-safe, but might cause 
	 * line breaks or confuse human readers/listeners so they are not included.)
	 * This custom "Base-57" encoding (57 character long safe alphabet) was originally
	 * intended for URL-shortening. 
	 */
	public final static char SAFE_ALPHABET[] = { 
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 
			'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
			'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 
			'2', '3', '4', '5', '6', '7', '8', '9',
			  };
	public final static int SAFE_ALPHABET_LENGTH = Array.getLength(SAFE_ALPHABET);
	public final static BigInteger SAFE_ALPHABET_LENGTH_AS_BIGINT = BigInteger.valueOf((long)SAFE_ALPHABET_LENGTH); // (new Integer(SAFE_ALPHABET_LENGTH)).longValue()
	
	private final static String SAFE_ALPHABET_STRING = new String(SAFE_ALPHABET);

	protected static final BigInteger LONG_MAX_AS_BIG_INTEGER = new BigInteger((new Long(Long.MAX_VALUE)).toString());
	protected static final BigInteger LONG_MIN_AS_BIG_INTEGER = new BigInteger((new Long(Long.MIN_VALUE)).toString());

	private static ArrayList<MediaType> supported;
	private static Map<MediaType, MimeHelper> mediaMap;

	static{
		supported = new ArrayList<MediaType>();
		mediaMap = new HashMap<MediaType, EEEConstants.MimeHelper>();
		MimeHelper[] valArray = EEEConstants.MimeHelper.values();
		for (MimeHelper mimeHelper : valArray) {
			supported.add(mimeHelper.getMediaType());
			mediaMap.put(mimeHelper.getMediaType(), mimeHelper);
		}
	}
	
	public static List<MediaType> getSupportedMediaTypeList() {
		return supported;
	}

	public static MimeHelper getMimeHelperFromMediaType(MediaType mt) {
		return mediaMap.get(mt);
	}

	
	// Code inspired by: http://stackoverflow.com/questions/2938482/encode-decode-a-long-to-a-string-using-a-fixed-set-of-letters-in-java
		
	public static long decodeSafeCharsToLong(String s) {
		BigInteger bint = decodeSafeCharsToBigInteger(s);
		if (bint.compareTo(LONG_MAX_AS_BIG_INTEGER) > 0) throw new IllegalArgumentException("Input string contains a too big positive number that won't fit into a Java long");
		if (bint.compareTo(LONG_MIN_AS_BIG_INTEGER) < 0) throw new IllegalArgumentException("Input string contains a too big negative number that won't fit into a Java long");
		return bint.longValue();
		//	    long num = 0;
//	    for (char ch : s.toCharArray()) {
//	        num *= SAFE_ALPHABET_LENGTH;
//	        int index = SAFE_ALPHABET_STRING.indexOf(ch);
//	        if (index < 0) throw new IllegalArgumentException("Input contained illegal charachters. Allowed ones are: "+SAFE_ALPHABET_STRING);
//			num += index;
//	    }
//	    return num;
	}
	
	public static BigInteger decodeSafeCharsToBigInteger(String s) {
	    BigInteger num = new BigInteger("0");
	    for (char ch : s.toCharArray()) {
	        num = num.multiply(SAFE_ALPHABET_LENGTH_AS_BIGINT); //num *= SAFE_ALPHABET_LENGTH;
	        int index = SAFE_ALPHABET_STRING.indexOf(ch);
	        if (index < 0) throw new IllegalArgumentException("Input contined illegal charachters. Allowed ones are: "+SAFE_ALPHABET_STRING);
			num = num.add(BigInteger.valueOf(index)); //num += index;
	    }
	    if (s.startsWith("a")) {
	    	return num.negate();
	    } else {
	    	return num;
	    }	    
	}
	
	
	/**
	 * Encodes a positive long (larger than zero) to a fairly compact string of characters 
	 * @param a long larger than zero
	 * @return a string built from characters occuring in {@link #SAFE_ALPHABET}
	 */
	public static String encodeLongToSafeChars(long num) {
		boolean positive = true;
		if (num == 0) return "a";
		if (num < 0) {
			positive = false;  // throw new IllegalArgumentException("Only numbers larger than zero (>0) allowed.");
			num = -num;
		}
	    StringBuilder sb = new StringBuilder();
	    while (num != 0) {
	        sb.append(SAFE_ALPHABET[((int) (num % SAFE_ALPHABET_LENGTH))]);
	        num /= SAFE_ALPHABET_LENGTH;
	    }
	    if (positive) {
	    	return sb.reverse().toString();
	    } else {
	    	return "a"+sb.reverse().toString();
	    }
	}
	
	/**
	 * Encodes a positive long (larger than zero) to a fairly compact string of characters 
	 * @param a long larger than zero
	 * @return a string built from characters occuring in {@link #SAFE_ALPHABET}
	 */
	public static String encodeBigIntegerToSafeChars(BigInteger num) {
		int compare = num.compareTo(BigInteger.ZERO);
		boolean positive = true;
		if (compare == -1) {
			// num was negative
			positive = false;
			num = num.abs(); // make it positive
			//throw new IllegalArgumentException("Only numbers larger than zero (>0) allowed."); 		
		} else if (compare == 0 ) {
			// num == 0
	    	return "a"; 
	    } 
		StringBuilder sb = new StringBuilder();
	    while (num.compareTo(BigInteger.ZERO) != 0) {
	        sb.append(SAFE_ALPHABET[(num.mod(SAFE_ALPHABET_LENGTH_AS_BIGINT).intValue())]);
	        num = num.divide(SAFE_ALPHABET_LENGTH_AS_BIGINT);
	    }
	    if (positive) {
		    return sb.reverse().toString();	    	
	    } else {
	    	return "a"+sb.reverse().toString();
	    }
	}
	
	
	protected static DatatypeFactory dtFactory;

	public static Date convertIsoDateTimeStringToJavaDate(String timeAsISODateTimeString)
			throws DatatypeConfigurationException {
		if (dtFactory == null) dtFactory = DatatypeFactory.newInstance();
		XMLGregorianCalendar xcal = dtFactory.newXMLGregorianCalendar(timeAsISODateTimeString);
		return xcal.toGregorianCalendar().getTime();
	}

	public static UID generateUID() {
		// Using UUIDs in this implementation, but other ways are of course possible too.
		return generateUUID();
	}
	
	public static UUID generateUUID() {
		return new UUID(java.util.UUID.randomUUID().toString());
	}

	public static ObjectVersionID generateNextVersionUID(ObjectVersionID precedingVersionUid, String currentSystemId)
	throws Exception {
	
		
		// FIXME: TODO: Consider imports, check branching etc
		
		if (!precedingVersionUid.creatingSystemID().getValue().equals(currentSystemId)) {
			throw new Exception("Branching is not implemented yet and your creatingSystemID (" +
					precedingVersionUid.creatingSystemID().getValue()+ ") does not match currentSystemId (" +
					currentSystemId+")");
		}
	
		System.out
		.println("Warning: EHRXMLDBHandler.generateNextVersionUID() called - probably not complelely implemented yet");
	
		return new ObjectVersionID(
				precedingVersionUid.objectID(),
				precedingVersionUid.creatingSystemID(),
				precedingVersionUid.versionTreeID().next());
	}

	public static CONTRIBUTION convertJavaRefImplContributionToXmlObject(XMLBinding binding,
			Contribution contribution) throws XMLBindingException {
		CONTRIBUTION contrib = CONTRIBUTION.Factory.newInstance();
		contrib.setUid((HIEROBJECTID) binding.bindToXML(contribution.getUid()));
		//contrib.setVersionsArray(  (OBJECTREF[]) binding.bindToXML(contribution.getVersions().toArray()));
		OBJECTREF[] objRefArray = new OBJECTREF[contribution.getVersions().size()];
		Iterator<ObjectRef> iter = contribution.getVersions().iterator();
		int i = 0;
		while (iter.hasNext()) {
			ObjectRef objectRef = (ObjectRef) iter.next();
			objRefArray[i] = (OBJECTREF) binding.bindToXML(objectRef);
			i++;
		}
		contrib.setVersionsArray(objRefArray);
		contrib.setAudit((AUDITDETAILS) binding.bindToXML(contribution.getAudit()));
		return contrib;
	}
	
	public static String requestInfoToString(Request req) {
		String resultString = 
				"\n Query: "+req.getResourceRef().getQuery() +
				"\n Matrix: "+req.getResourceRef().getMatrix() +"\n" +
				"\n All request attributes: "+req.getAttributes() +"\n";
		
		Representation reqEntity = req.getEntity();
		if (reqEntity != null){
			resultString = resultString + " Body, type: " + reqEntity.getMediaType() +", size:"+reqEntity.getSize();			
		} else {
			resultString = resultString + " No body/entity was found in the request";			
		}
		return resultString;
	}

	/**
	 * Checks for cached info and if necessary checks last DB modification for the requested EHR 
	 * (and stores it in cache). After that it compares
	 * info to etag in request to figure out if it should 
	 * return early with 304 or go on with rest of processing.
	 * This method is intended to be called in the doInit() methods
	 * of suitable resources before doing expensive operations like
	 * some database queries.
	 * Note that if this method does not return null, it is the responsibility 
	 * of the calling class to set the Etag (and last modified date) on it's returned representation
	 * if the "304 Not Modified" early return does not occur.
	 * @param callingResource
	 * @param ehrMetadataCache
	 * @param callingEhrId
	 * @return the ETag string (=latest contribution UID) or null if there are no contributions for this EHR ID  
	 */
	@SuppressWarnings("unused") // **** only needed during Cache performance experiments **** FIXME: COMMENT OUT THIS LINE
	public static String checkOrPopulateCacheThenReturn304EarlyIfETagsMatch(ServerResource callingResource, EhrMetadataCache ehrMetadataCache, EHRDatabaseReadInterface dbReader, String callingEhrId) {
		// FIXME: Change cache and it's callers to store & handle the entire JSONObject instead!
		// TODO: add ResourceException throwing (500) if callingResource or ehrMetadataCache are missing
		
		// ***************** Enable the lines below only during Cache performance experiments ****************** FIXME: COMMENT OUT LINES BELOW
		//boolean useETagForIndividualData = (Boolean) callingResource.getContext().getAttributes().get(EHRTestRestStarter.USE_ETAG_FOR_INDIVIDUAL_DATA);
		boolean useEtag = DemoStarter.getConfig().getBoolean(DemoStarter.USE_ETAG_FOR_INDIVIDUAL_DATA, true);
		//System.out.println("Util.checkOrPopulateCacheThenReturn304EarlyIfETagsMatch() useEtag="+useEtag);
		if (useEtag==false) return null; // Stop tag creation here if useEtag is false
		// ***************** Enable the lines above only during Cache performance experiments ******************
		
		if (callingEhrId == null) throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Server-side implementation error, cache should not be called with null as callingEhrId");
	
		List<Tag> noneMatchList = callingResource.getRequest().getConditions().getNoneMatch();
		//System.out.println("Util.checkOrPopulateCacheThenReturn304EarlyIfETagsMatch() got the noneMatchList ="+noneMatchList.toString());
		// if noneMatchList is empty, then we should just go on?
	
		String cachedTag = ehrMetadataCache.getEtag(callingEhrId);
		if (cachedTag == null) {
			// Nothing found in cache for this ehr id, so go look in DB and populate cache if possible (then return and go on)
			JSONObject metadataAsJson = dbReader.getContributionsLatest(callingEhrId);
			if (metadataAsJson == null) {
				// No contributions found for this EHR, then no etag should be set and nothing should be cached, thus return with null
				return null; // Ends method processing here
			}
			try {
				cachedTag = metadataAsJson.getString(EHRDatabaseReadInterface.UID);
			} catch (JSONException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Internal JSON fieldname error", e);
			}
			Date modTime;
			try {
				modTime = Util.convertIsoDateTimeStringToJavaDate(metadataAsJson.getString(EHRDatabaseReadInterface.TIME_COMMITTED));
			} catch (JSONException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Internal JSON fieldname error", e);
			} catch (DatatypeConfigurationException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Internal date format error", e);
			}
			// if we got here we have fetched the latest contribution id from the database 
			ehrMetadataCache.putEtag(callingEhrId, cachedTag);
			ehrMetadataCache.putLastModified(callingEhrId, modTime);
		} else {
			// Found entry for this ehr in cache (cachedTag was not null)
			// Since noneMatchList.contains(...) uses identity rather than equality we need to loop instead...
			boolean found = false;
			for (Tag tag : noneMatchList) {
//				System.out.println("Util.checkOrPopulateCacheThenReturn304EarlyIfETagsMatch() testing if "+tag.getName()+" equals "+cachedTag);
				if (tag.getName().equals(cachedTag)) found = true;
			}
			if (found){
				// Matched! Return early  with 304
				//callingResource.getResponse().getEntity().setTag(new Tag(cachedTag)); // Set response tag. TODO: Check if this can be left out
			    String msg = "No modifications have been made to the EHR "+callingEhrId+" since last time you checked";
				//System.out.println("Util.checkOrPopulateCacheThenReturn304EarlyIfETagsMatch() returning early, msg = "+msg);
				callingResource.setStatus(Status.REDIRECTION_NOT_MODIFIED, msg);
				callingResource.commit(); // Does this end the processing as it should?
			} else {				
				// System.out.println("Util.checkOrPopulateCacheThenReturn304EarlyIfETagsMatch() updating response etag to: "+cachedTag);
			}
		}
		return cachedTag;
	}

	/**
		 * Optional utility test function...
		 * @param postToUrl The URL e.g. within an EHR to send a POST to
		 * @param fetchXMLFromURL URL of an XML document we want to us as content in the post
		 * @throws IOException
		 */
		private static void postSomething(String postToUrl, String fetchXMLFromURL) throws IOException {
	
			// prep doc
			org.jdom.Document doc = null; 
			
			if (postToUrl==null || postToUrl.equalsIgnoreCase("")){
				postToUrl = "http://localhost:8182/ehr/1234567/56780007::ehr.us.lio.se::2";
			}
			System.out.println("EHRTestRestStarter.postSomething() postToUrl="+postToUrl);
			
			if (fetchXMLFromURL == null || fetchXMLFromURL.equalsIgnoreCase("")) {
				//Create a little test doc if no doc was given as input
				doc = new org.jdom.Document();
				org.jdom.Namespace defaultNamespace = Namespace
						.getNamespace("http://schemas.openehr.org/v1");
				org.jdom.Element element = new Element("ID33", defaultNamespace);
				element.addContent(new Element("Sub", defaultNamespace)
						.setAttribute("Det", "funkar").setAttribute("Tjo", "Ho")
						.setAttribute("Med", "DOM"));
				doc.setRootElement(element);
			} else {
				// An url for fetching was given
				SAXBuilder sb = new SAXBuilder();
				try {
					doc = sb.build(new URL(fetchXMLFromURL));
				} catch (JDOMException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			org.jdom.output.DOMOutputter outputter = new org.jdom.output.DOMOutputter();
			Document d = null;
			try {
				d = outputter.output(doc);
			} catch (JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			// Prepare the request
			Request request = new Request(Method.POST, postToUrl);
			request.setReferrerRef("http://www.mysite-rererrer.org");
			DomRepresentation drep = new DomRepresentation(MediaType.APPLICATION_XML, d);		
			request.setEntity(drep);
			
			// Handle it using an HTTP client connector
			Client client = new Client(Protocol.HTTP);
			Response response = client.handle(request);
	
	//		// Write the response entity on the console
			// NO, don't since response.getEntity() can only be called once
	//		Representation output = response.getEntity();
	//		output.write(System.out);
	
			// Outputting the content of a Web page
	//		Client client = new Client(Protocol.HTTP);
	//		client.get(urlString).getEntity().write(System.out);
		}

	/**
	 * The XMLBinding is used to convert back and forth between XML and openEHR Java-ref-impl objects  
	 */
	public static XMLBinding setUpXMLBinding(String language) throws Exception, XMLBindingException {
		Map<SystemValue, Object> systemValues = new HashMap<SystemValue, Object>();
		systemValues.put(SystemValue.TERMINOLOGY_SERVICE, SimpleTerminologyService.getInstance());
		systemValues.put(SystemValue.MEASUREMENT_SERVICE, SimpleMeasurementService.getInstance());
		systemValues.put(SystemValue.CHARSET, new CodePhrase("IANA_character-sets", "UTF-8"));
		systemValues.put(SystemValue.LANGUAGE, new CodePhrase("ISO_639-1", language));			
		return new XMLBinding(systemValues);			
	}

	public static Collection<File> listFiles(File directory, FileFilter filter,
			boolean recurse) {
		// List of files / directories
		Vector<File> files = new Vector<File>();
	
		// Get files / directories in the directory
		File[] entries = directory.listFiles();
	
		// Go over entries
		for (File entry : entries) {
			// If there is no filter or the filter accepts the
			// file / directory, add it to the list
			if (filter == null || filter.accept(entry)) {
				files.add(entry);
			}
	
			// If the file is a directory and the recurse flag
			// is set, recurse into the directory
			if (recurse && entry.isDirectory()) {
				files.addAll(listFiles(entry, filter, recurse));
			}
		}
	
		// Return collection of files
		return files;
	}
	
	

}
