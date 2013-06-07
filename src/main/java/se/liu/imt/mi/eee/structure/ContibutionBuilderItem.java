package se.liu.imt.mi.eee.structure;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

import org.openehr.rm.datatypes.text.DvText;
import org.restlet.data.MediaType;

import se.liu.imt.mi.eee.structure.EEEConstants.AuditChangeType;
import se.liu.imt.mi.eee.structure.EEEConstants.VersionLifecycleState;
import se.liu.imt.mi.eee.structure.EEEConstants.VersionableObjectType;

public class ContibutionBuilderItem<T> extends VersionedObjectListItem<T> implements Serializable{

	private static final long serialVersionUID = -7135071251964329264L;
	
	/**
	 * Media type of the data parameter (Stored as string instead of MediaType object, in order to be easily serializable)
	 */
	protected String mediaTypeAsString;	
	
	public ContibutionBuilderItem() {
		super();
	}
	

//	public ContibutionBuilderItem(String tempID,
//			VersionableObjectType versionableObjectType,
//			VersionLifecycleState versionLifecycleState,
//			AuditChangeType auditChangeType, T data,
//			String precedingVersionUid,
//			List<String> otherInputVersionUids,
//			DvText auditDetailsDescription) {
//		super(tempID, versionableObjectType, versionLifecycleState, auditChangeType,
//				data, precedingVersionUid, otherInputVersionUids,
//				auditDetailsDescription, );
//		// TODO Auto-generated constructor stub
//	}
	

	public ContibutionBuilderItem(String tempID, VersionableObjectType versionableObjectType,
			VersionLifecycleState versionLifecycleState,
			AuditChangeType auditChangeType, 
			T data,
			String precedingVersionUid, 
			List<String> otherInputVersionUids, 
			DvText auditDetailsDescription, 
			MediaType mediaType
//			List<String> containedArchetypes, 
//			List<String> containedTemplates
			) {
		super(tempID, versionableObjectType, versionLifecycleState, auditChangeType, 
				data, precedingVersionUid, otherInputVersionUids, auditDetailsDescription);
		this.mediaTypeHiddenSetter(mediaType);
//		this.containedArchetypes = containedArchetypes;
//		this.containedTemplates = containedTemplates;
	}
	

	/** 
	 * Convenience constructor only containing the mandatory fields.
	 */
	public ContibutionBuilderItem(String tempID, VersionableObjectType versionableObjectType,
			VersionLifecycleState versionLifecycleState,
			AuditChangeType auditChangeType, T data, MediaType mediaType) {
		super(tempID, versionableObjectType, versionLifecycleState, auditChangeType, data);
		this.mediaTypeHiddenSetter(mediaType);
	}


	public void setMediaType(String mediaTypeAsString) {
		this.mediaTypeAsString = mediaTypeAsString;
	}

	public String getMediaType() {
		return mediaTypeAsString;
	}
	
	public void mediaTypeHiddenSetter(MediaType mediaType) {
		this.mediaTypeAsString = mediaType.getName();
	}
	
	public MediaType mediaTypeHiddenGetter() {
		return MediaType.valueOf(this.mediaTypeAsString);
	}

	
}
