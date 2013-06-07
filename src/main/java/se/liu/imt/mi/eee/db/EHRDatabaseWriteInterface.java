package se.liu.imt.mi.eee.db;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.openehr.rm.common.changecontrol.Contribution;
import org.openehr.rm.common.generic.PartyProxy;
import org.openehr.rm.datatypes.encapsulated.DvMultimedia;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.datatypes.uri.DvEHRURI;
import org.openehr.rm.support.identification.ObjectVersionID;
import org.openehr.rm.support.identification.UID;

import se.liu.imt.mi.eee.structure.VersionedObjectListItem;
import se.liu.imt.mi.eee.structure.EEEConstants.AuditChangeType;

public interface EHRDatabaseWriteInterface<T> {

	/**
	 * This method takes care of committing a list of objects to the storage
	 * as an atomic contribution. Intended for ORIGINAL_VERSION<T> objects only.
	 * 	
	 * The change_type for the entire contribution will be inferred from the 
	 * contents of the list of objects. 
	 * {@link AuditChangeType#unknown AuditChangeType.unknown} will be used 
	 * for mixed contributions. If the type of all the objects are the same 
	 * (e.g. {@link AuditChangeType#creation AuditChangeType.creation} then
	 * that will be used as contribution change_type here too. See 
	 * <a href="http://www.openehr.org/mailarchives/openehr-technical/msg04904.html">
	 * this openEHR mailinglist discussion</a> for details
	 */
	public abstract Contribution commitContributionOfOriginalVersions(
			PartyProxy committer, String ehrId, String systemId,
			List<? extends VersionedObjectListItem<T>> objectList,
			DvText optionalContributionDescription, 
			UID optionalSuggestedContributionID) throws Exception;

	/**
	 * Intended for post-committal signing as described in the third 
	 * bullet point of section 6.2.8 in 
	 * http://www.openehr.org/releases/1.0.2/architecture/rm/common_im.pdf
	 * @param committer
	 * @param ehrId
	 * @param systemId
	 * @param ObjectVersionID
	 * @param reason
	 * @param is_pending
	 * @param auditDetailsDescription
	 * @param attested_view
	 * @param proof
	 * @param items
	 */
	public abstract void attestObject(String committer, String ehrId,
			String systemId, ObjectVersionID ObjectVersionID, DvText reason,
			boolean is_pending, DvText auditDetailsDescription,
			DvMultimedia attested_view, String proof, Set<DvEHRURI> items);
		
	public abstract UID generateUID(); 
	
	/**
	 * Creates an EHR root object based on supplied ehrId.
	 * @param ehrId Suggested EHR id
	 * @param systemId id of the EHR system
	 * @throws IllegalArgumentException thrown if that ehrId already exists
	 * @throws IOException thrown if DB can not be accessed or updated properly
	 * @throws Exception 
	 */
	public abstract void createEHR(String ehrId, String systemId) throws IllegalArgumentException, Exception;
	
	/**
	 * Generates a UID and then creates an EHR root object based on that.
	 * @param systemId id of the EHR system
	 * @return String representation of the EHR ID
	 * @throws IOException thrown if DB can not be accessed or updated properly
	 */
	public abstract String createEHR(String systemId) throws IllegalArgumentException, Exception;
	

}