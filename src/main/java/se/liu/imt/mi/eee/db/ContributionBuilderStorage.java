package se.liu.imt.mi.eee.db;

import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Node;
import org.xmldb.api.base.Collection;


import se.liu.imt.mi.eee.structure.ContibutionBuilderItem;

public interface ContributionBuilderStorage<DATA> {

	/**
	 * @param committer
	 * @param ehrId
	 * @param contributionID 
	 * @param system_id
	 * @return Returns a new collection populated at start with AUDIT_DETAILS
	 */
	abstract Collection createNewEmptyContributionBuild(String committer,
			String ehrId, String contributionID, String system_id,
			String descriptionText) throws Exception;
	// TODO: Rethink return type of above creation - we don't want to be bound to only XMLDB-implementations
	

	abstract void deleteContributionBuild(String committer, String ehrId,
			String contributionID);

	abstract void deleteObjectFromContributionBuild(String committer,
			String ehrId, String contributionID, String tempID)
			throws Exception;

	/*
		public void storeXMLInContributionBuild(
				String committer, String ehrId, String contributionID, 
				String tempID, Document XMLFormattedData) throws XMLDBException{	
			storeAnyObjectInContributionBuild(committer, ehrId, contributionID,
					tempID, XMLFormattedData, XMLResource.RESOURCE_TYPE);
		}
	
		// TODO: Contemplate if it's best to store Object, or Entity/Representation
		public void storeBinaryObjectInContributionBuild(
				String committer, String ehrId, String contributionID, 
				String tempID, Object binaryData) throws XMLDBException{	
			storeAnyObjectInContributionBuild(committer, ehrId, contributionID,
					tempID, binaryData, BinaryResource.RESOURCE_TYPE);
		}
	 */

	/**
	 * This implementation simply runs the ContibutionBuilderItem through 
	 * a java.beans.XMLEncoder before storing it in an XML database.
	 * TODO: Explore alternative serializations
	 */
	abstract void store(String committer, String ehrId, String contributionID,
			String tempID, ContibutionBuilderItem<DATA> cbi, String system_id)
			throws Exception;

	/**
	 * 
	 * @param committer
	 * @return Returns a list of EHR id's with ongoing contributions
	 * @throws XMLDBException 
	 */
	abstract List<String> getactiveEhrIds(String committer)
			throws ResourceException;

	/**
	 * 
	 * @param committer
	 * @param ehrId
	 * @return Returns a list of triples containting strings<contribution id>, <>'s pertinent to the indicated EHR
	 * @throws XmlException 
	 * @throws XMLDBException 
	 */
	abstract Map<String, Node> getActiveBuildsMetadataForEhr(String committer,
			String ehrId) throws ResourceException, XmlException;

	/**
	 * 
	 * @param committer
	 * @param ehrId
	 * @return Returns a list of contribution id's pertinent to the indicated EHR
	 * @throws XmlException 
	 * @throws XMLDBException 
	 */
	abstract List<String> getActiveBuildsIDsForEhr(String committer,
			String ehrId) throws ResourceException, XmlException;

	abstract List<String> getTempObjectIdsInContributionBuild(String committer,
			String ehrId, String contributionID) throws ResourceException;

	abstract ContibutionBuilderItem<DATA> getObjectInContributionBuild(
			String committer, String ehrId, String contributionID, String tempID)
			throws ResourceException;

	/**
	 * 
	 * @param committer
	 * @param ehrId
	 * @param contributionUUID
	 * @param change_type
	 * @return
	 */
	abstract List<ContibutionBuilderItem<DATA>> assembleContributionObjectList(
			String committer, String ehrId, String contributionID);

}