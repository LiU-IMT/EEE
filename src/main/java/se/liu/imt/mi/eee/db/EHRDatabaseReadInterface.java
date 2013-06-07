package se.liu.imt.mi.eee.db;

import java.util.Date;
import java.util.List;

import org.json.JSONObject;

public interface EHRDatabaseReadInterface<VOBJ,CONTR> {

	/**
	 * fieldname for contribution uid used in e.g. JSON objects returned by {@link #getContributionsLatest(String)}
	 */
	public static final String UID = "uid";
	/**
	 * fieldname for contribution time_committed used in e.g. JSON objects returned by {@link #getContributionsLatest(String)}
	 */
	public static final String TIME_COMMITTED = "time_committed";

	public abstract Date getCurrentDatabaseTime() throws Exception;

	public abstract String getCurrentDatabaseTimeAsISODateTimeString()
			throws Exception;

	/**
	 * 
	 * @param ehrID
	 * @param objectID
	 * @param systemID
	 * @param treeID
	 * @return Returns a org.w3c.dom.Node containing the versioned object if found, else returns NULL if the requested composition was not found.
	 * @throws Throws an exception if the Database could not be reached etc.
	 */
	public abstract VOBJ getVersionedObject(String ehrID, String objectID,
			String systemID, String treeID) throws Exception;

	/**
	 * 
	 * @param ehrID
	 * @param objectID
	 * @return Returns a List<String> containing (complete) version IDs if found, else returns NULL if the requested object was not found.
	 * @throws Throws an exception if the Database could not be reached etc.
	 */
	public abstract List<String> getVersionedObject_all_version_ids(
			String ehrID, String objectID) throws Exception;

	/**
	 * 
	 * @param ehrID
	 * @param objectID
	 * @return Returns a <T> if found, else returns NULL if the requested Object was not found.
	 * @throws Throws an exception if the Database could not be reached etc.
	 */
	public abstract List<VOBJ> getVersionedObject_all_versions(String ehrID,
			String objectID) throws Exception;

	/**
	 * 
	 * @param ehrID
	 * @param objectID
	 * @param systemID
	 * @param iso_time (ISO 8601 time string eg. 2008-01-18T15:40:41Z)
	 * @throws Throws an exception if the Database could not be reached etc.
	 */
	public abstract VOBJ getVersionedObject_at_time(String ehrID, String objectID,
			String iso_time) throws Exception;
	
//		
//	/**
//	 * Get metadata for an object. The use case in mind is e.g. when a 
//	 * client only requests HTTP HEAD with e.g. "Last-Modified" 
//	 * @param ehrID
//	 * @param objectID
//	 * @param systemID
//	 * @param treeID
//	 * @return
//	 * @throws Exception
//	 */
//	public abstract T getVersionedObjectMetadata(String ehrID, String objectID) throws Exception;
//	// TODO: figure out if any other metadata than "Last-Modified" is interesting (if not rename to e.g. getVersionedObjectLastModified)
	
//	/**
//	 * Get metadata for a version. The use case in mind is e.g. when a 
//	 * client only requests HTTP HEAD with e.g. "Last-Modified" 
//	 * @param ehrID
//	 * @param objectID
//	 * @param systemID
//	 * @param treeID
//	 * @return
//	 * @throws Exception
//	 */
//	public abstract T getVersionMetadata(String ehrID, String objectID,
//			String systemID, String treeID) throws Exception;
//	// TODO: figure out if getVersionMetadata is only interesting for @latest_version (if so rename) or for any version...	
	
	/**
	 * 
	 * @param ehrID
	 * @param objectID
	 * @return Returns a <T> if found, else returns NULL if the requested Object was not found.
	 * @throws Throws an exception if the Database could not be reached etc.
	 */
	public abstract VOBJ getVersionedObject_latest_trunk_version(String ehrID,
			String objectID) throws Exception;

	/**
	 * 
	 * @param ehrID
	 * @param objectID
	 * @return Returns a <T> if found, else returns NULL if the requested Object was not found.
	 * @throws Throws an exception if the Database could not be reached etc.
	 */
	public abstract VOBJ getVersionedObject_latest_version(String ehrID,
			String objectID) throws Exception;
	
	public abstract CONTR getContribution(String ehrID, String contributionID);

	public abstract List<CONTR> listContributionsDescending(String ehrID,
			int start, int end);

	public abstract JSONObject getContributionsLatest(String ehrID);

}