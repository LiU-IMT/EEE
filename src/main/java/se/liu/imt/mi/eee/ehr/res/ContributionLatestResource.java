package se.liu.imt.mi.eee.ehr.res;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.xmlbeans.XmlException;
import org.jdom.JDOMException;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import se.liu.imt.mi.eee.db.EHRDatabaseReadInterface;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.trigger.EhrMetadataCache;
import se.liu.imt.mi.eee.utils.Util;

public class ContributionLatestResource extends WadlServerResource {
	
	@SuppressWarnings("rawtypes") // Ok since we don't use the generics part of EHRDatabaseReadInterface
	EHRDatabaseReadInterface dbConnection;
	protected String ehrId;
	protected String uid;
	protected String lastModifiedAsISODateString;
	protected JSONObject metadataAsJson;
	protected EhrMetadataCache cache;
	
	@SuppressWarnings({ "unchecked", "rawtypes" }) // Ok since we don't use the generics part of EHRDatabaseReadInterface
	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		
		// Set WADL metadata
		this.setName("Contribution, Get latest");
		this.setDescription("This resource gets some metadata regarding the most recent contribution, so that the latest write to the particular identified EHR can be isentified. It may be useful for warming cache for ETag and last-modified using uid of latest contribution");
		// TODO: add WADL description of parameters

		// Get the db handle from context (used both for POST & GET handling)
		dbConnection = (EHRDatabaseReadInterface) getContext().getAttributes().get(EEEConstants.KEY_TO_BASIC_DB_READER);
		cache = (EhrMetadataCache) getContext().getAttributes().get(EEEConstants.KEY_TO_EHR_METADATA_CACHE);
		ehrId = (String) getRequestAttributes().get(EEEConstants.EHR_ID);
				
		metadataAsJson = dbConnection.getContributionsLatest(ehrId);
		if (metadataAsJson == null) throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find any contributions at all for this EHR ID");

		try {
			uid = metadataAsJson.getString(EHRDatabaseReadInterface.UID);
			lastModifiedAsISODateString = metadataAsJson.getString(EHRDatabaseReadInterface.TIME_COMMITTED);
		} catch (JSONException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not fetch contribution metadata from JSON");
		}
		
		// if cache is missing value for this ehrId then update 
		// (if there already is a value, don't update it since it might have been written 
		//  by other thread recently parallel to this request)
		String currentTag = cache.getEtag(ehrId); // FIXME: should change to atomic cache operation like in memcached
		if(currentTag == null) {
			cache.putEtag(ehrId, uid);
			System.out.println("ContributionLatestResource.doInit() cached etag "+uid+" for ehr "+ehrId);
		} else {
			// TODO: possibly update recence of etag if the cache has a LRU timer
			System.out.println("ContributionLatestResource.doInit() did not touch cached the etag "+currentTag+" for ehr "+ehrId);
			if (!currentTag.equals(uid)) System.out.println("ContributionLatestResource.doInit() --- ALERT: the cached tag did not match latest uid "+uid+" from database. Do consider cache with atomic check+update calls");
		}
	}	
	
	
	@Get("json")
	public Representation getJsonRepresentation() throws JDOMException, XmlException { 
		JsonRepresentation returningRepresentation = new JsonRepresentation(metadataAsJson);		
		returningRepresentation.setTag(new Tag(uid));
		try {
			returningRepresentation.setModificationDate(Util.convertIsoDateTimeStringToJavaDate(lastModifiedAsISODateString));
		} catch (DatatypeConfigurationException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not convert date format");
		}
		return returningRepresentation;
	}

	// TODO: Add Java and HTML as return formats! (+move etag setting etc to common code for those methods
	
}
