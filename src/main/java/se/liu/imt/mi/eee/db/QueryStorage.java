package se.liu.imt.mi.eee.db;

import java.util.List;

public interface QueryStorage {
		
	/**
	 * This method should check if a query with the same ID as given within 
	 * the variable query.queryID already exists. If it does exist then an
	 * explaining exception is thrown, otherwise the query is stored.
	 * The query should already have been checked and possibly translated
	 * by other classes before sending it to this method since no
	 * checking of content is done here.
	 */
	public abstract void storeQuery(QueryContainer query);

	/**
	 * 
	 * @param queryID
	 * @return null if no query with that queryID is found otherwise the query is returned
	 */
	public abstract QueryContainer viewQuery(String queryID);

	public abstract List<QueryContainer> listUserQueries(String user_id);

}