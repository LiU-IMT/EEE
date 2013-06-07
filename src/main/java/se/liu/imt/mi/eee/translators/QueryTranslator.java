package se.liu.imt.mi.eee.translators;

import org.restlet.data.Form;

import se.liu.imt.mi.eee.db.QueryContainer;

public interface QueryTranslator {

	/**
	 * This method should do it best to pre-parse and validate the query. 
	 * It should not store the query or execute anything towards EHR data.
	 * @return Should return an informative debug string containing successful translation etc or meaningful error messages. 
	 */
	public abstract String debugQuery(Form staticQueryParametersAsForm)
			throws Exception;
	
	/**
	 * This method should do it best to pre-parse and validate the query  
	 * It should store the query or execute anything towards EHR data.
	 * @return Returns the a modified query object containing added staticParameters
	 * the convention is to prefix translated parameter names with "translated_",
	 * the most (so far only?) added parameter is "translated_query"
	 */
	public abstract QueryContainer translateQuery(QueryContainer query)
			throws Exception;
	
}
