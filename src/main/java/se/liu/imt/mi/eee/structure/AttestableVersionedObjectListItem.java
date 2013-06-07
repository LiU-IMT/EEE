package se.liu.imt.mi.eee.structure;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openehr.rm.datatypes.encapsulated.DvMultimedia;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.datatypes.uri.DvEHRURI;
import org.openehr.rm.support.identification.ObjectVersionID;

import se.liu.imt.mi.eee.structure.EEEConstants.AttestationReason;
import se.liu.imt.mi.eee.structure.EEEConstants.AuditChangeType;
import se.liu.imt.mi.eee.structure.EEEConstants.VersionLifecycleState;
import se.liu.imt.mi.eee.structure.EEEConstants.VersionableObjectType;

public class AttestableVersionedObjectListItem<T> extends
		VersionedObjectListItem<T> {
	
	protected String proof;
	protected Set<DvEHRURI> items;
	protected AttestationReason reason;
	protected boolean is_pending;
	protected DvMultimedia attested_view;

	public AttestableVersionedObjectListItem(String tempID,
			VersionableObjectType versionableObjectType,
			VersionLifecycleState versionLifecycleState,
			AuditChangeType auditChangeType, T data,
			String precedingVersionUid,
			List<String> otherInputVersionUids,
			DvText auditDetailsDescription) {
		super(tempID, versionableObjectType, versionLifecycleState,
				auditChangeType, data, precedingVersionUid,
				otherInputVersionUids, auditDetailsDescription);
		// TODO Auto-generated constructor stub
	}

	public AttestableVersionedObjectListItem(String tempID,
			VersionableObjectType versionableObjectType,
			VersionLifecycleState versionLifecycleState,
			AuditChangeType auditChangeType, T data,
			String precedingVersionUid,
			List<String> otherInputVersionUids,
			DvText auditDetailsDescription,
			String proof,
			Set<DvEHRURI> items, AttestationReason reason, boolean isPending,
			DvMultimedia attestedView) {
		super(tempID, versionableObjectType, versionLifecycleState,
				auditChangeType, data, precedingVersionUid,
				otherInputVersionUids, auditDetailsDescription);
		this.proof = proof;
		this.items = items;
		this.reason = reason;
		is_pending = isPending;
		attested_view = attestedView;
	}

	public String getProof() {
		return proof;
	}

	public void setProof(String proof) {
		this.proof = proof;
	}

	public Set<DvEHRURI> getItems() {
		return items;
	}

	public void setItems(Set<DvEHRURI> items) {
		this.items = items;
	}
	
	public void addItem(DvEHRURI item) {
		if (items == null) {
			items = new HashSet<DvEHRURI>();
		}
		items.add(item);
	}

	public AttestationReason getReason() {
		return reason;
	}

	public void setReason(AttestationReason reason) {
		this.reason = reason;
	}

	public boolean isIs_pending() {
		return is_pending;
	}

	public void setIs_pending(boolean isPending) {
		is_pending = isPending;
	}

	public DvMultimedia getAttested_view() {
		return attested_view;
	}

	public void setAttested_view(DvMultimedia attestedView) {
		attested_view = attestedView;
	}

}
