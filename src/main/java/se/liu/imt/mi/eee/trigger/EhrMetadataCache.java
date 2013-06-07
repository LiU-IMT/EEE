package se.liu.imt.mi.eee.trigger;

import java.util.Date;

public interface EhrMetadataCache {

	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.trigger.EHRMetadataCache#getLastModified(java.lang.String)
	 */
	public abstract Date getLastModified(String ehr_id);

	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.trigger.EHRMetadataCache#putLastModified(java.lang.String, java.util.Date)
	 */
	public abstract Date putLastModified(String ehr_id, Date modTime);

	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.trigger.EHRMetadataCache#getEtag(java.lang.String)
	 */
	public abstract String getEtag(String ehr_id);

	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.trigger.EHRMetadataCache#putEtag(java.lang.String, java.lang.String)
	 */
	public abstract void putEtag(String ehr_id, String eTag);

}