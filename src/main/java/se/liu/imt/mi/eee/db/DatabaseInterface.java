package se.liu.imt.mi.eee.db;

/**
 * This interface contains a METHOD named 'getDatabaseMode' that acts 
 * as a marker if the interface is allowed to access: <br/> 
 * 1. only one specified EHR at a time (SINGLE_RECORD) <br/>
 * or <br/>
 * 2. multiple records (MULTI_RECORD) for population queries etc.<br/>
 * <br/>
 * The intention of this is the possibility for implementers to 
 * add restrictions that disallow access (eg. via malicious queries)
 * to other records than the one being requested and logged.
 *    
 * @author Erik Sundvall
 */
public interface DatabaseInterface{

	public enum DatabaseMode {
		SINGLE_RECORD, MULTI_RECORD
	}
	
	/**
	 * The mode should not be allowed to change after initialization
	 * of the DatabaseInterface object, thus no setter is provided. 
	 * The a variable should be set (and locked) in the constructor 
	 * of an implementing class if it can handle both kinds, and this
	 * getter should return that variable. <br/>
	 * <br/> 
	 * Otherwise this method can be implemented to always return
	 * SINGLE_RECORD or MULTI_RECORD depending on intended use.
	 */
	public DatabaseMode getDatabaseMode();
	
}