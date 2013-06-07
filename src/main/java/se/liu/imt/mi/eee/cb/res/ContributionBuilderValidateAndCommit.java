package se.liu.imt.mi.eee.cb.res;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.xb.xsdschema.AnyDocument.Any;
import org.openehr.binding.XMLBinding;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.common.changecontrol.Contribution;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.common.generic.PartyProxy;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.HierObjectID;
import org.openehr.rm.support.identification.PartyRef;
import org.openehr.rm.support.identification.UID;
import org.openehr.rm.support.identification.UUID;
import org.openehr.schemas.v1.ARCHETYPED;
import org.openehr.schemas.v1.AUDITDETAILS;
import org.openehr.schemas.v1.COMPOSITION;
import org.openehr.schemas.v1.CompositionDocument;
import org.openehr.schemas.v1.DVTEXT;
import org.openehr.schemas.v1.PARTYIDENTIFIED;
import org.openehr.validation.DataValidator;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Node;

import se.liu.imt.mi.eee.ArchetypeAndTemplateRepository;
import se.liu.imt.mi.eee.db.EHRDatabaseWriteInterface;
import se.liu.imt.mi.eee.db.xmldb.ContributionBuilderStorageInXMLDB;
import se.liu.imt.mi.eee.structure.ContibutionBuilderItem;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.structure.XmlHelper;
import se.liu.imt.mi.eee.utils.Util;
import se.liu.imt.mi.eee.validation.ArchetypeChecking_XmlString2Locatable_ValConv;
import se.liu.imt.mi.eee.validation.ValidationAndConversionResult;

/**
 * This class accepts POST requests and validates or commits a Contribution Build 
 * to the EHR backend storage (TODO: and then redirects to...?)
 * 
 * @author Erik Sundvall
 */
public abstract class ContributionBuilderValidateAndCommit extends WadlServerResource implements EEEConstants{

	protected ContributionBuilderStorageInXMLDB cbStorage;
	protected EHRDatabaseWriteInterface dbWriter;
	protected String systemId;
	protected XMLBinding xmlBinding;
	protected String command;
	protected String ehrId;
	protected String contributionBuildID;
	protected String committer;
	protected List<ContibutionBuilderItem<String>> inList;
	protected ArchetypeAndTemplateRepository atRepo;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		getLogger().entering(this.getClass().getCanonicalName(), "doInit", getContext().getAttributes().toString());
		System.out.println(" >> >> >> ContributionBuilderValidateAndCommit.doInit() attributes: "+getContext().getAttributes().toString());
		
		cbStorage = (ContributionBuilderStorageInXMLDB) getContext().getAttributes().get(EEEConstants.KEY_TO_CONTRIBUTION_BUILDER_DB_INSTANCE);
		systemId = (String) getContext().getAttributes().get(EEEConstants.SYSTEM_ID);
		xmlBinding = (XMLBinding) getContext().getAttributes().get(EEEConstants.KEY_TO_XML_BINDING);
		command = (String) getContext().getAttributes().get(EEEConstants.COMMAND);
		atRepo = (ArchetypeAndTemplateRepository) getContext().getAttributes().get(EEEConstants.KEY_TO_AT_REPO);

		// LOGGED IN USER CHECK - START - The section below is an extra safeguard checkning that committers are logged in.
		if (getRequest().getClientInfo().getUser() == null) {
			throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, "You must be logged in first!");
		}
		
		String useridentifier = getRequest().getClientInfo().getUser().getIdentifier(); // from HTTP authenticated user
		committer = (String) getRequestAttributes().get(COMMITTER_ID);

		if (!committer.equalsIgnoreCase(useridentifier)) { // TODO: Cosider changing to equals(...) instead
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "The logged in user ("+useridentifier
					+") must in the current single-user focused cb-implementation match the committer ("+committer+") in the URI path.");
		}
		// LOGIN CHECK END		

		ehrId = (String) getRequestAttributes().get(EHR_ID);		
		contributionBuildID = (String) getRequestAttributes().get(CONTRIBUTION_BUILD_ID);		
		inList = cbStorage.assembleContributionObjectList(committer, ehrId, contributionBuildID);

	}

	@Post
	public Representation handlePost(Representation reprIn) {
		
		// System.out.println("ContributionBuilderValidateAndCommit.handlePost() class = "+this.getClass().getSimpleName());		
		getLogger().entering(this.getClass().getCanonicalName(), "handlePost", getRequestAttributes().toString());
		
		ArchetypeChecking_XmlString2Locatable_ValConv converter = new ArchetypeChecking_XmlString2Locatable_ValConv(xmlBinding, atRepo);		
		Map<ContibutionBuilderItem<String>, ValidationAndConversionResult<Locatable>> valResMap = converter.validateAndConvertList(inList);

		List<ContibutionBuilderItem> outlist = new ArrayList<ContibutionBuilderItem>();
		boolean valid = true;
		for (Entry<ContibutionBuilderItem<String>,ValidationAndConversionResult<Locatable>> valEntry : valResMap.entrySet()) {
			if (!valEntry.getValue().isValid()){
				valid = false;
			}
			outlist.add(valEntry.getKey());
		}
		
		// Fetch metadata for entire contribution build including description...
		AUDITDETAILS audetAsXML = null;
		//AuditDetails audet = null;
		try {
			Node auditMetaInfo = cbStorage.getActiveBuildsMetadataForEhr(committer, ehrId).get(contributionBuildID);
			System.out.println("ContributionBuilderValidateAndCommit.handlePost(1) "+auditMetaInfo);
			audetAsXML = AUDITDETAILS.Factory.parse(auditMetaInfo);
			//System.out.println("ContributionBuilderValidateAndCommit.handlePost(2) "+audetAsXML.xmlText());
			//audet = (AuditDetails) xmlBinding.bindToRM(audetAsXML);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to fetch or parse contribution build metadata for "+contributionBuildID+" from database.", e);
		}	
		//DvText optionalContributionDescription = audet.getDescription() ;
		DVTEXT descriptionXMLobject = audetAsXML.getDescription();
		String desc = "FIXME: dummy description from ContributionBuilderValidateAndCommit.handlePost()"; // FIXME: Replace with form field or previous content
		if (descriptionXMLobject != null) {
			desc = descriptionXMLobject.getValue();
		}
		System.out.println("ContributionBuilderValidateAndCommit.handlePost(3) "+desc);
		
		
		if (!valid){ 
			// Return error list
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			// TODO: possibly add printout of metadata about CB from audetAsXML
			// TODO: Possibly return HTML, JSON and XML representation of valResMap if requested
			return new StringRepresentation("Failed validation of Contribution build!\r\n"+valResMap, MediaType.TEXT_PLAIN);			
		} // end if(!valid)	
		

		DvText optionalContributionDescription = new DvText(desc);
		//PartyProxy committerProxy = audet.getCommitter();
		PARTYIDENTIFIED committerIdentified = (PARTYIDENTIFIED) audetAsXML.getCommitter();
		PartyProxy committerProxy = 
			new PartyIdentified(
							new PartyRef(
									new HierObjectID(systemId, committer), // id
									"PARTY" // type
							),
							"Should be real name of the user: "+committer, // name FIXME
							null // identifiers
			);
		
		if (this.getClass().equals(ContributionBuilderCommit.class)){

			// ////// Validate Commit ///////

			// Check format of UUID
			UID suggestedContributionUID;
			try {
				// Check if suggested UUID was OK
				suggestedContributionUID = new UUID(contributionBuildID);
				// If not, then generate a new one
				if (suggestedContributionUID == null) suggestedContributionUID = Util.generateUUID();
			} catch (Exception e) {
				// Really, generate a new UUID if we messed up
				suggestedContributionUID = Util.generateUUID();
			}

			// Then store		
			Contribution storedContrib;
			dbWriter = (EHRDatabaseWriteInterface) getContext().getAttributes().get(EEEConstants.KEY_TO_BASIC_DB_WRITER);
			try {
				// Store cb as a real contribution
				storedContrib = dbWriter.commitContributionOfOriginalVersions(committerProxy, ehrId, systemId, outlist, optionalContributionDescription, suggestedContributionUID );

				// Then remove the cb:
				cbStorage.deleteContributionBuild(committer, ehrId, contributionBuildID);
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to store contribution in database.", e1);
			}


			String path = "/ehr:"+ehrId+"/contributions/"+storedContrib.getUid().getValue()+"/";
			getResponse().setStatus(Status.SUCCESS_CREATED);
			getResponse().redirectSeeOther(path);
			return new StringRepresentation("Successful creation of Contribution with path: "
					+ path, MediaType.TEXT_PLAIN);			
		} else { // else from if (this.getClass().equals(ContributionBuilderCommit.class)){
			
			// ////// Just Validate ///////
			
			getResponse().setStatus(Status.SUCCESS_OK);
			// TODO: possibly add printout of metadata about CB from audetAsXML
			return new StringRepresentation("Successful validation of Contribution build!\r\n"+valResMap, MediaType.TEXT_PLAIN);			
		} // end of if-else (this.getClass().equals(ContributionBuilderCommit.class)){
			
			
	}
	
//	// Old code to Prettyprint errors and throw exception containing the error list			
//	StringWriter innerWriter = new StringWriter();
//	PrintWriter errorWriter = new PrintWriter(innerWriter);
//	errorWriter.println("Some of the data did NOT pass validation, See error list below:\n<br/><br/>");
//	for (Entry<ContibutionBuilderItem, String> validationEntry : validationResultMap.entrySet()) {
//		errorWriter.println("\n<br/><br/>\n<strong>"+validationEntry.getKey().getTempID() + "</strong><br/> " +validationEntry.getValue());
//	}
//	throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, innerWriter.toString());
//	// TODO: Later perhaps implement and return a specialized Exception containing the validationResultMap (inherit from ResourceException) 
//} // End of: if (errorsFound)


	
}
