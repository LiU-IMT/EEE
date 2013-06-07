package se.liu.imt.mi.eee.db.xmldb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.TransactionService;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XQueryService;

import se.liu.imt.mi.eee.db.QueryContainer;
import se.liu.imt.mi.eee.db.QueryStorage;
import se.liu.imt.mi.eee.structure.EEEConstants;

public class QueryStorageInXMLDB implements QueryStorage {

	private Collection queryRootCollection;
	protected SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss");
	protected XMLDBHelper dbHelper;
	protected XQueryService queryXQueryService;

	public QueryStorageInXMLDB(XMLDBHelper dbHelper) throws XMLDBException {
		this.dbHelper = dbHelper;
		queryRootCollection = dbHelper.createChildCollection(
				dbHelper.getRootCollection(), "QueryStorage");

		queryXQueryService = dbHelper
				.getXQueryService(queryRootCollection);
		
		// TODO: Check if the following three namespace declarations still are useful 
		queryXQueryService.setNamespace("v1",
				EEEConstants.SCHEMA_OPENEHR_ORG_V1);
		queryXQueryService.setNamespace("eee",
				EEEConstants.SCHEMA_EEE_OPENEHR_EXTENSION);
		queryXQueryService.setNamespace("xsi", EEEConstants.SCHEMA_XSI);
	}

	public QueryContainer viewQuery(String queryID) {		
		QueryContainer query = null;
		XMLResource n;
		try {
			n = (XMLResource) queryRootCollection.getResource(queryID);
			if (n == null)
				return null;
			query = new QueryContainer(n.getContentAsDOM());
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
//		// Debug printout to trace DB calls
//		try {
//			System.out.println("QueryStorageInXMLDB.viewQuery("+queryID+"): "+query.toJson().toString(4));
//		} catch (JSONException e) {
//			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
//		}
		
		return query;
	}

	public void storeQuery(QueryContainer query) {		
		// FIXME: Remember that optimized native query storage (creating stored procedures) etc is allowed here
		
		// Fetch SHA-1 ID-string from query object
		String queryId = query.getQueryID();

		TransactionService trans = null;
		try {
			trans = dbHelper.createTransaction(queryRootCollection);
			trans.begin();
			
			// Check that the SHA-1 has not been used before
			if (queryRootCollection.getResource(queryId) != null) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "A query with this SHA-1 checksum is already exists, so your query can not be stored. The existing query is most likely identical to the one you tried to store, so use that instead of re-posting. " +
						"(In the unlikely event of an unwanted SHA-1 collision for _different_ queries just change some character (e.g. a space) in your query to get a different checksum.)");
			}
			
			// Set up query xml resource
			XMLResource newQueryRes = (XMLResource) queryRootCollection
					.createResource(queryId, XMLResource.RESOURCE_TYPE);
			newQueryRes.setContentAsDOM(query.toXML());
			queryRootCollection.storeResource(newQueryRes);
			trans.commit();
		} catch (XMLDBException e) {
			// Roll back on errors
			try {
				trans.rollback();
				// Oh my, the rollback can also throw exceptions!
			} catch (Exception e2) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e2);
			}
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		return;
	}
	
	public List<QueryContainer> listUserQueries(String user_id) {
		List<QueryContainer> queryList = null;

		String query = "for $query in //query "
				+ "where $query/creator/text()='" + user_id + "' " 
				+ "order by $query/created/text() descending "
				+ "return $query ";
		try {
			ResourceIterator rit = queryXQueryService.query(query)
					.getIterator();
			if (rit.hasMoreResources()) {
				// The answer set was not empty, so initiate a list to replace the null
				queryList = new ArrayList<QueryContainer>();
			} 
			while (rit.hasMoreResources()) {
				XMLResource r = (XMLResource) rit.nextResource();
				queryList.add(new QueryContainer(r.getContentAsDOM()));
			}
		} catch (Exception e) {
			// TODO: improve error description & use status code differentiation
			// depending on root cause
			throw new ResourceException(e);
		}
		return queryList;
	}

}
