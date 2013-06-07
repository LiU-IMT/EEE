package se.liu.imt.mi.eee.db;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.openehr.rm.datatypes.encapsulated.DvMultimedia;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.datatypes.uri.DvEHRURI;
import org.openehr.rm.support.identification.ObjectVersionID;
import org.openehr.rm.support.identification.UID;
import org.w3c.dom.Node;
import se.liu.imt.mi.eee.structure.XmlHelper;
import se.liu.imt.mi.eee.utils.Util;


public class SharedXQueries {

	public static final String XQ_WRITE_VERSION = " declare variable $ehrID as xs:string external; " +
				" declare variable $objectID as xs:string external; " +
				" declare variable $versionAsXml external; " +
				" for $ehr in //eee:EHR[eee:ehr_id/v1:value/text()=$ehrID]" +
				" let $v_obj := $ehr//*[eee:uid/v1:value/text()=$objectID]" +
				" return update insert $versionAsXml into $v_obj";
	
	public static final String XQ_VERSIONED_OBJECT_ALL_VERSION_IDS = " declare variable $ehrID as xs:string external; " +
					" declare variable $objectID as xs:string external; " +
					" for $ehr in //eee:EHR[eee:ehr_id/v1:value/text()=$ehrID] " +
					" let $v_obj := $ehr//*[eee:uid/v1:value/text()=$objectID] " +
					" return $v_obj/eee:versions/v1:uid/v1:value/text()";
	
	public static final String XQ_VERSIONED_OBJECT = " declare variable $ehrID as xs:string external; " +
					" declare variable $fullVersionID as xs:string external; " +
					" for $ehr in //eee:EHR[eee:ehr_id/v1:value/text()=$ehrID] " +
					" let $version := $ehr//eee:versions[v1:uid/v1:value/text()=$fullVersionID] " +
					" return $version ";
	
	public static final String XQ_WRITE_CONTRIBUTION = "declare variable $ehrID as xs:string external; " +
				"declare variable $contributionAsXml external; " +			
				"update insert $contributionAsXml into /eee:contributions[@ehr_id=$ehrID] ";
	
	protected static final String XQ_GET_CONTRIBUTION = " declare variable $ehrID as xs:string external; " +
						" declare variable $contributionID as xs:string external; " +
						" for $contrib in /eee:contributions[@ehr_id=$ehrID]/eee:contribution " +
						" let $id := $contrib/eee:uid/v1:value/text() " +				
						" where ($id eq $contributionID) " +
						" return $contrib ";
	
	protected static final String XQ_CONTRIBUTIONS_DESCENDING_LIST = " declare variable $ehrID as xs:string external; " +
					" declare variable $start as xs:integer external; " +
					" declare variable $end as xs:integer external; " +
					" let $result := for $contrib in /eee:contributions[@ehr_id=$ehrID]/eee:contribution " +
					" let $time := $contrib/eee:audit/v1:time_committed/v1:value/text() " +
					" let $modtime := xs:dateTime($time) " +
					" order by $modtime descending " +
					" return $contrib " +
					" for $y at $pos in $result " +
					" where ($pos ge $start) and ($pos le $end) " + 
					" return $y";
	
	public static final String XQ_LATEST_CONTRIBUTION_TIME_AND_ID_AS_JSON = " declare variable $ehrID as xs:string external; "
								+ " let $result := for $contrib in /eee:contributions[@ehr_id=$ehrID]/eee:contribution "
								+ " let $time := $contrib/eee:audit/v1:time_committed/v1:value/text() "
								+ " let $modtime := xs:dateTime($time) "
								+ " let $uid := $contrib/eee:uid/v1:value/text() "
								+ " order by $modtime descending "
								+ " return concat('{\"" + EHRDatabaseReadInterface.TIME_COMMITTED
								+ "\": \"', $modtime, '\", \"" +EHRDatabaseReadInterface.UID
								+ "\": \"', $uid,'\"}') "
								+ "for $y at $pos in $result " 
								+ " where ($pos eq 1) "
								+ "return $y";
	
	public static final String QX_FETCH_TRANSACTION = "//eee:CONTRIBUTION[eee:uid/v1:value/text()=$contrib_id]";

	public SharedXQueries() {
		super();
	}
	
	protected se.liu.imt.mi.ehr.x2010.eeeV1.EHR constructEhrRootXMLObject(String ehrId, String systemId) throws IOException {
		se.liu.imt.mi.ehr.x2010.eeeV1.EHR ehrXmlObject = se.liu.imt.mi.ehr.x2010.eeeV1.EHR.Factory.newInstance(XmlHelper.getXMLoptionsForEHRRootDocument());
		ehrXmlObject.addNewEhrId().setValue(ehrId);
		ehrXmlObject.addNewSystemId().setValue(systemId);
		
		return ehrXmlObject;
	}
	
	// TODO: Check if the  methods below are really used or just happened to be left from a copy-paste
	// ******************************************************************************************
	
	public UID generateUID() {
		// TODO: Figure out if using XML databases built in UID generation would be more suitable
		return Util.generateUID();
	}
	
	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.db.xmldb.EHRDatabaseReadInterface#getVersionedObject_all_versions(java.lang.String, java.lang.String)
	 */
	public List<Node> getVersionedObject_all_versions(String ehrID, String objectID) throws Exception {
		throw new NotImplementedException();

	}

	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.db.xmldb.EHRDatabaseReadInterface#getVersionedObject_at_time(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Node getVersionedObject_at_time(String ehrID, String objectID, String iso_time) throws Exception {
		throw new NotImplementedException();
	}
	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.db.xmldb.EHRDatabaseReadInterface#getVersionedObject_latest_trunk_version(java.lang.String, java.lang.String)
	 */
	public Node getVersionedObject_latest_trunk_version(String ehrID, String objectID) throws Exception {
		throw new NotImplementedException();
	}

	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.db.xmldb.EHRDatabaseReadInterface#getVersionedObject_latest_version(java.lang.String, java.lang.String)
	 */
	public Node getVersionedObject_latest_version(String ehrID, String objectID) throws Exception {
		throw new NotImplementedException();
	}

	public void attestObject(String committer, String ehrId, String systemId,
			ObjectVersionID ObjectVersionID, DvText reason, boolean is_pending, DvText auditDetailsDescription, DvMultimedia attested_view,
			String proof, Set<DvEHRURI> items) {
				// AuditChangeType change_type = AuditChangeType.attestation;		
				// TODO : implement
				throw new org.apache.commons.lang.NotImplementedException();
			}


}