package se.liu.imt.mi.eee.structure;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.ObjectVersionID;

import se.liu.imt.mi.eee.structure.EEEConstants.AuditChangeType;
import se.liu.imt.mi.eee.structure.EEEConstants.VersionLifecycleState;
import se.liu.imt.mi.eee.structure.EEEConstants.VersionableObjectType;


/**
 * This class stores structured typesafe (Enum) metadata for handling VERSIONED_OBJECTs
 * It uses java generics (<T>) to cater for xml, json, java etc. in the data field
 *
 * In many ways it resembles org.openehr.rm.common.changecontrol.OriginalVersion
 * but it allows data to be in other formats  and has fewer dependencies on
 * openEHR java ref impl packages (e.g. no need for TerminologyService)
 *
 * @author Erik Sundvall
 */
public class VersionedObjectListItem<T> implements Serializable{
 
	private static final long serialVersionUID = 4037186634686122605L;
	protected String tempID;
	protected T data;
	protected VersionableObjectType versionableObjectType;
	protected VersionLifecycleState versionLifecycleState;
	protected AuditChangeType auditChangeType;
	protected String preceding_version_uid = null;
	protected List<String> other_input_version_uids = null;
	protected DvText auditDetailsDescription = null;

	/**
	 * Used internally by e.g. EHRXMLDBHandler for ID suggestion generation
	 */
	protected ObjectVersionID suggested_version_uid = null;
	
	public VersionedObjectListItem() {
		super();
	}

	/**
	 * Constructor containing all mandatory fields. 
	 */
	public VersionedObjectListItem(String tempID,
			VersionableObjectType versionableObjectType,
			VersionLifecycleState versionLifecycleState,
			AuditChangeType auditChangeType, T data) {
		this(tempID, versionableObjectType, versionLifecycleState, auditChangeType, data, null, null);
	}
	
	/**
	 * Constructor containing all mandatory fields plus the optional fields precedingVersionUid and otherInputVersionUids
	 */
	public VersionedObjectListItem(String tempID,
			VersionableObjectType versionableObjectType,
			VersionLifecycleState versionLifecycleState,
			AuditChangeType auditChangeType, T data, String precedingVersionUid, List<String> otherInputVersionUids) {
		this(tempID, versionableObjectType, versionLifecycleState, auditChangeType, data, precedingVersionUid, otherInputVersionUids, null);
	}
		
	/**
	 * Constructor also containing optional auditDetailsDescription in addition to all mandatory fields.
	 */
	public VersionedObjectListItem(String tempID, 
			VersionableObjectType versionableObjectType,
			VersionLifecycleState versionLifecycleState,
			AuditChangeType auditChangeType, T data,
			String precedingVersionUid,
			List<String> otherInputVersionUids,
			DvText auditDetailsDescription) {
		super();
		setTempID(tempID);
		this.versionableObjectType = versionableObjectType;
		this.versionLifecycleState = versionLifecycleState;
		this.auditChangeType = auditChangeType;
		this.data = data;
		this.preceding_version_uid = precedingVersionUid;
		this.auditDetailsDescription = auditDetailsDescription;
	}

	public String getTempID() { 
		return tempID; 
	}

	/**
	 * A temporary id used for uidentification until the system assigns an id upon commit.
	 * @param tempID mandatory, must be unique within a VersionedObjectList and must not be null 
	 */
	public void setTempID(String tempID) {
		this.tempID = tempID; // TODO: maybe enforce non null...
	}
	
	public String getPreceding_version_uid() {
		return preceding_version_uid;
	}

	public void setPreceding_version_uid(String precedingVersionUid) {
			preceding_version_uid = precedingVersionUid;
	}

	public VersionableObjectType getVersionableObjectType() {
		return versionableObjectType;
	}

	public void setVersionableObjectType(VersionableObjectType versionableObjectType) {
		this.versionableObjectType = versionableObjectType;
	}

	public VersionLifecycleState getVersionLifecycleState() {
		return versionLifecycleState;
	}

	public void setVersionLifecycleState(VersionLifecycleState versionLifecycleState) {
		this.versionLifecycleState = versionLifecycleState;
	}

	public AuditChangeType getAuditChangeType() {
		return auditChangeType;
	}

	public void setAuditChangeType(AuditChangeType auditChangeType) {
		this.auditChangeType = auditChangeType;
	}
	
	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public DvText getAuditDetailsDescription() {
		return auditDetailsDescription;
	}

	public void setAuditDetailsDescription(DvText auditDetailsDescription) {
		this.auditDetailsDescription = auditDetailsDescription;
	}

	public ObjectVersionID getSuggested_version_uid() {
		return suggested_version_uid;
	}

	public void setSuggested_version_uid(ObjectVersionID suggestedVersionUid) {
		suggested_version_uid = suggestedVersionUid;
	}	
	
	public List<String> getOther_input_version_uids() {
		return other_input_version_uids;
//		if (other_input_version_uids == null) return null;
//		List<ObjectVersionID> returnList = new ArrayList<ObjectVersionID>();
//		for (String id : other_input_version_uids) {
//			returnList.add(new ObjectVersionID(id));
//		}
//		return returnList;
	}

	public void setOther_input_version_uids(
			List<String> otherInputVersionUids) {
		other_input_version_uids = otherInputVersionUids;
	}
	
	public void addOther_input_version_uid(String ovid) {
		other_input_version_uids.add(ovid);
	}
	
//	public JSONObject asJson(){
//		return new JSONObject(this);
//	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() +" with tempID: "+getTempID();
	}
	
//	@Override
//	public String toString() {
//		String result = super.toString() +"\nRunning the toString() method of "+this.getClass().getCanonicalName()+"\n";
//		Field[] fields = this.getClass().getFields();
//		for (int i = 0; i < fields.length; i++) {
//			try {
//				String name = fields[i].getName();
//				if (!name.equals("serialVersionUID")) {
//					result = result+ name + ": "+ fields[i].get(this).toString()+"\n";					
//				}
//			} catch (Exception e) {
//				result = result+e.getMessage();
//				e.printStackTrace();
//			}			
//		}
//		return result +"\n";
//	}
	
}
