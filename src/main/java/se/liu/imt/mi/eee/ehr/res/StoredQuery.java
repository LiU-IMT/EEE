package se.liu.imt.mi.eee.ehr.res;

import java.util.concurrent.ConcurrentMap;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.engine.header.Header;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import se.liu.imt.mi.eee.db.DatabaseInterface.DatabaseMode;
import se.liu.imt.mi.eee.db.EHRDatabaseReadInterface;
import se.liu.imt.mi.eee.db.QueryContainer;
import se.liu.imt.mi.eee.db.QueryStorage;
import se.liu.imt.mi.eee.db.xmldb.XMLDBHelper;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.trigger.EhrMetadataCache;
import se.liu.imt.mi.eee.utils.FreemarkerSupportResource;
import se.liu.imt.mi.eee.utils.Util;

public abstract class StoredQuery extends FreemarkerSupportResource implements EEEConstants{
	
	protected String ehrId;
	protected XMLDBHelper dbHelper;	
	protected String command;
	protected MediaType returnMediaType;
	protected String querySha;
	protected QueryStorage queryStorage;
	protected QueryContainer storedQuery;
	protected String etag = null; // This needs to be set in the response 
	
	@Override
	protected void doInit() throws ResourceException {
		super.doInit();

		// Get the db handle from context (used both for executing the stored queries)
		queryStorage = (QueryStorage) getContext().getAttributes().get(KEY_TO_QUERY_STORAGE);

		// Get the db handle from context (used both for executing the stored queries)
		dbHelper = (XMLDBHelper) getContext().getAttributes().get(KEY_TO_XMLDBHELPER_INSTANCE);		
		
		// Extract strings from request
		ehrId = (String) getRequestAttributes().get(EHR_ID);
		if (ehrId == null && dbHelper.getDatabaseMode().equals(DatabaseMode.SINGLE_RECORD)) throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "You must supply an EHR ID in the request when running in single EHR mode.");

		querySha = (String) getRequestAttributes().get(QUERY_SHA);
		if (querySha == null) throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "You must supply a query ID (SHA) in the request");

		String returnMediaTypeName = getQuery().getFirstValue(RETURN_MEDIA_TYPE, true);
		// XML is default return media type
		returnMediaType = MediaType.TEXT_XML;
		if (returnMediaTypeName != null) {
			returnMediaType = MediaType.valueOf(returnMediaTypeName);
		}

		// Http caching reduces DB load upon repeated requests for unchanged EHRs
		if (dbHelper.getDatabaseMode().equals(DatabaseMode.SINGLE_RECORD)) {
			EHRDatabaseReadInterface<?, ?> dbReader = (EHRDatabaseReadInterface<?,?>) getContext().getAttributes().get(EEEConstants.KEY_TO_BASIC_DB_READER);
			EhrMetadataCache cache = (EhrMetadataCache) getContext().getAttributes().get(EEEConstants.KEY_TO_EHR_METADATA_CACHE);		
			etag = Util.checkOrPopulateCacheThenReturn304EarlyIfETagsMatch(this, cache, dbReader, ehrId);
		}

		// System.out.println("StoredQuery.doInit() still here... ++++++++++++++ etag="+ etag);

		// TODO: should this be moved out of doInit
		storedQuery = queryStorage.viewQuery(querySha); // This is where we hit the QueryStorage.
		
		if(storedQuery == null) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No query with this query ID (SHA-1) can be found. Perhaps this is the first time it is run or somebody forgot to store it on this server. (Posting the original query that you expected to have this SHA-1 will likely help.)");
		}
		
	}
	
	@Override
	protected void doRelease() throws ResourceException {
		if (etag != null && dbHelper.getDatabaseMode().equals(DatabaseMode.SINGLE_RECORD)) {
			// System.out.println("StoredQuery.doRelease() called by "+this.getClass().getCanonicalName());
			Representation responseEntity = getResponseEntity();
			// If 304 status and etag already set then there will be no entity sent (and thus nothing to tag)
			if (responseEntity != null && getResponse().getStatus().isSuccess()) responseEntity.setTag(new Tag(etag)); 	
		}
		
		// Now add an extra "Content-Location" response header to help e.g. AJAX clients
		// can discover the SHA even if the browser hides that a POST was redirected
		ConcurrentMap<String, Object> attr = getResponse().getAttributes();
		@SuppressWarnings("unchecked")
		Series<Header> headers = (Series<Header>) attr.get("org.restlet.http.headers");			
		if(headers == null){
			headers = new Series<Header>(Header.class);
			attr.put("org.restlet.http.headers", headers);
		}
		headers.add(new Header("Content-Location", getReference().toString()));
		
		super.doRelease();
	}

	
}
