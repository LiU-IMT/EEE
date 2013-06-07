package se.liu.imt.mi.eee.db.xmldb;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openehr.binding.XMLBinding;
import org.openehr.binding.XMLBindingException;
import org.openehr.rm.common.generic.AuditDetails;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.common.generic.PartyProxy;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.HierObjectID;
import org.openehr.rm.support.identification.ObjectID;
import org.openehr.rm.support.identification.PartyRef;
import org.openehr.rm.support.terminology.TerminologyService;
import org.openehr.schemas.v1.AUDITDETAILS;
import org.openehr.terminology.SimpleTerminologyService;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

import se.liu.imt.mi.eee.db.ContributionBuilderStorage;
import se.liu.imt.mi.eee.structure.ContibutionBuilderItem;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.utils.Util;

public class ContributionBuilderStorageInXMLDB implements ContributionBuilderStorage<String> {
	
	// TODO: Extract ContributionBuilderStorage<T> as interface andmake this class a ContributionBuilderStorage<Node>

	private static final String DEFAULT = "default";
	// TODO: Check if URI-encoding/decoding can be removed now when tempID is 
	//       restricted to URI-unreserved characters 

	private Collection contributionBuilderRoot;
//	private Namespace xmlNamespace = Namespace.getNamespace("", EEEConstants.SCHEMA_OPENEHR_ORG_V1); 
	protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	protected XMLDBHelper dbHelper;
	protected XMLBinding xmlBinding;

	public ContributionBuilderStorageInXMLDB(XMLDBHelper dbHelper, XMLBinding xmlBinding) throws Exception {	
		this.dbHelper = dbHelper;
		this.xmlBinding = xmlBinding;
		contributionBuilderRoot = dbHelper.createChildCollection(dbHelper.getRootCollection(), "ContributionBuilder");
	}

	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.db.xmldb.ContributionBuilderStorage#createNewEmptyContributionBuild(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public Collection createNewEmptyContributionBuild (String committer, String ehrId, String contributionID, String system_id, String descriptionText) throws Exception {
		// Setup the ongoing contribution tree path
		Collection composerCollection = 
			dbHelper.createChildCollection(contributionBuilderRoot, committer);
		Collection ehrCollection = 
			dbHelper.createChildCollection(composerCollection, ehrId);
		
		// FIXME: check if contributionBuilder with desired name exists already - if so, then return it instead of creating a new one
		
		Collection contributionBuilder = 
			dbHelper.createChildCollection(ehrCollection, contributionID);

		DvDateTime timeCommitted = new DvDateTime(dbHelper.getCurrentDatabaseTimeAsISODateTimeString()); // Now
		DvCodedText changeType = EEEConstants.AuditChangeType.unknown.getAsDvCodedText(); // Unknown at creatio will get calculated and changed on committ
		DvText description = new DvText(descriptionText);
		TerminologyService terminologyService = SimpleTerminologyService.getInstance();
		ObjectID id = new HierObjectID(system_id, committer); // Globally unique id of the committer
		PartyRef externalRef = new PartyRef(id , "PARTY");
		
		// TODO: Use real identifiers in a list (fetch from demographics?)
		//List<DvIdentifier> id_list = new ArrayList<DvIdentifier>();
		//PartyProxy committerAsPartyProxy = new PartyIdentified(externalRef, "Should be Name of User "+committer, id_list);
		
		PartyProxy committerAsPartyProxy = new PartyIdentified(externalRef, "Should be real name of the user: "+committer, null);
		AuditDetails auDet = new AuditDetails(system_id, committerAsPartyProxy , timeCommitted, changeType, description, terminologyService);
		//Create an AUDIT_DETAILS stub
		Node AUDIT_DETAILS = createAuditDetailsDocument(auDet ); // committer, system_id

		XMLResource resource = (XMLResource) contributionBuilder.createResource("AUDIT_DETAILS", XMLResource.RESOURCE_TYPE);
		resource.setContentAsDOM(AUDIT_DETAILS);

		contributionBuilder.storeResource(resource);
		return contributionBuilder;
	}

	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.db.xmldb.ContributionBuilderStorage#deleteContributionBuild(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void deleteContributionBuild(String committer, String ehrId, 
			String contributionID){
		Collection collection;
		try {
			collection = getEHRCollection(committer, ehrId, false);
			dbHelper.deleteCollection(collection, contributionID);	
		} catch (XMLDBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not delete Contribution Build");
		}
	}

	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.db.xmldb.ContributionBuilderStorage#deleteObjectFromContributionBuild(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void deleteObjectFromContributionBuild(String committer, String ehrId, 
			String contributionID, String tempID) throws XMLDBException{
		 Collection contributionBuilder = getContributionBuildCollection(committer, ehrId, contributionID);
		 contributionBuilder.removeResource(contributionBuilder.getResource(urlEncode(tempID)));
	}
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
	
	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.db.xmldb.ContributionBuilderStorage#store(java.lang.String, java.lang.String, java.lang.String, java.lang.String, se.liu.imt.mi.eee.structure.ContibutionBuilderItem, java.lang.String)
	 */
	public void store(String committer, String ehrId, 
			String contributionID, String tempID, ContibutionBuilderItem<String> cbi, String system_id) throws Exception {
			
//		// Temp hack to convert incoming XML-strings in cbi to DOM Documents
//		if (cbi.getData() instanceof String) {
//			String dataString = (String) cbi.getData();
//			DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();
//	        DocumentBuilder builder1 = factory1.newDocumentBuilder();
//	        Document data = builder1.parse( new ByteArrayInputStream(dataString.getBytes()) );
//	        cbi.setData(data);
//		}      

		ByteArrayOutputStream os = new ByteArrayOutputStream(); 

		// Encode the whole java bean to xml
		XMLEncoder encoder = new XMLEncoder(os);
		//System.out.println("ContributionBuilderStorageInXMLDB.store() before encoding. cbi getPreceding_version_uid: \n"+cbi.getPreceding_version_uid());		
		//System.out.println("ContributionBuilderStorageInXMLDB.store() before encoding. cbi getOther_input_version_uids: \n"+cbi.getOther_input_version_uids());		
		//System.out.println("ContributionBuilderStorageInXMLDB.store() before encoding. cbi detData: \n"+cbi.getData());		
		encoder.writeObject(cbi);
		//System.out.println("ContributionBuilderStorageInXMLDB.store() after writeObject");		
        encoder.close();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();              
		Document d = builder.parse(new ByteArrayInputStream(os.toByteArray()));
	      
		//System.out.println("ContributionBuilderStorageInXMLDB.store(boz.toString())"+bozString);
		
		Collection contributionBuilder = null;
		if (contributionID.trim().equalsIgnoreCase(DEFAULT)){
			 contributionBuilder = getDefaultContributionBuildCollection(committer, ehrId, true, system_id);			
		} else {
			 contributionBuilder = getContributionBuildCollection(committer, ehrId, contributionID);			
		}
		
		
//        // TODO: open existing or create new?
//		Collection composerCollection = 
//			dbHelper.createChildCollection(contributionBuilderRoot, committer);
//		Collection ehrCollection = 
//			dbHelper.createChildCollection(composerCollection, ehrId);
//		Collection contributionBuilder = 
//			dbHelper.createChildCollection(ehrCollection, checkLatest(contributionID));

		//Store the composition data document

		String urlEncodedTempID = urlEncode(tempID);

		Resource resource;
		resource = contributionBuilder.createResource(
				urlEncodedTempID, XMLResource.RESOURCE_TYPE);
	
			((XMLResource) resource).setContentAsDOM(d);	
		contributionBuilder.storeResource(resource);
		System.out.println("ContributionBuilderStorageInXMLDB.store() stored: "+urlEncodedTempID);
		//updateAuditDetailsTime(committer, ehrId, contributionID);
        
	}

protected String urlEncode(String tempID) {
	String urlEncodedTempID;
	try {
		urlEncodedTempID = URLEncoder.encode(tempID,"UTF-8");
	} catch (UnsupportedEncodingException e) {
		throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "The temporary id '"+tempID+"' could not be URLEncoded before usage in storage.");
	}
	return urlEncodedTempID;
}

/*	private void storeAnyObjectInContributionBuild(String committer,
			String ehrId, String contributionID, String tempID,
			Object dataObject, String resourceType) throws XMLDBException {
		//Setup the contribution path

		Collection composerCollection = 
			dbHelper.createChildCollection(contributionBuilderRoot, committer);
		Collection ehrCollection = 
			dbHelper.createChildCollection(composerCollection, ehrId);
		Collection contributionBuilder = 
			dbHelper.createChildCollection(ehrCollection, contributionID);

		//Store the composition data document

		String urlEncodedTempID;

		try {
			urlEncodedTempID = URLEncoder.encode(tempID,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Throw better exception later...
			e.printStackTrace();
			throw new XMLDBException();
		}

		Resource resource;
		resource = contributionBuilder.createResource(
				urlEncodedTempID, resourceType);
		if (resourceType == XMLResource.RESOURCE_TYPE){	
			((XMLResource) resource).setContentAsDOM((Document) dataObject);	
		} else {
			((BinaryResource) resource).setContent(dataObject);
		}
		contributionBuilder.storeResource(resource);
		updateAuditDetailsTime(committer, ehrId, contributionID);
	}*/

	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.db.xmldb.ContributionBuilderStorage#getactiveEhrIds(java.lang.String)
	 */
	public List<String>	getactiveEhrIds(String committer) throws ResourceException
	{
		//System.out.println("ContributionBuilderStorageInXMLDB.getactiveEhrIds() c="+committer);
			Collection committerCollection;
			List<String> ehrs;
			try {
				committerCollection = getCommitterCollection(committer, false);
				ehrs = Arrays.asList(committerCollection.listChildCollections());
			} catch (XMLDBException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Error accessing contribution build database when listing active EHRs for this committer ("+committer+").");
			}
			return ehrs;
	}

	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.db.xmldb.ContributionBuilderStorage#getActiveBuildsMetadataForEhr(java.lang.String, java.lang.String)
	 */
	public Map<String, Node> getActiveBuildsMetadataForEhr(String committer, String ehrId)  throws ResourceException, XmlException
	{
		try {			
			Collection ehrCollection = getEHRCollection(committer, ehrId, false);  			
			List<String> contributionBuildIDs = Arrays.asList(ehrCollection.listChildCollections());
			Map<String, Node> coupleList = new HashMap<String, Node>();
			for (String contribBuildID : contributionBuildIDs) {
				XMLResource xres = (XMLResource) ehrCollection.getChildCollection(contribBuildID).getResource("AUDIT_DETAILS");
				Node n = xres.getContentAsDOM();
				coupleList.put(contribBuildID, n);
			}
			return coupleList ;
		} catch (XMLDBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

	}
	
	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.db.xmldb.ContributionBuilderStorage#getActiveBuildsIDsForEhr(java.lang.String, java.lang.String)
	 */
	public List<String>	getActiveBuildsIDsForEhr(String committer, String ehrId)  throws ResourceException, XmlException
	{
		try {			
			Collection ehrCollection = getEHRCollection(committer, ehrId, false);  			
			return Arrays.asList(ehrCollection.listChildCollections());
		} catch (XMLDBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

	}

	private Collection getEHRCollection(String committer, String ehrId, boolean createIfMissing)
	throws XMLDBException {
		//System.out.println("ContributionBuilderStorageInXMLDB.getEHRCollection() c="+committer+ " e="+ehrId);
		Collection committerCollection = getCommitterCollection(committer, createIfMissing); 
				
		Collection ehrCollection = committerCollection.getChildCollection(ehrId);
		if (ehrCollection == null) {
			if (createIfMissing) {
				ehrCollection = dbHelper.createChildCollection(committerCollection, ehrId);
			} else {
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No contribution builds active for this EHR ("+ehrId+") by this committer ("+committer+") .");
			}
		}
		return ehrCollection;
	}

	private Collection getCommitterCollection(String committer, boolean createIfMissing) throws XMLDBException {
		//System.out.println("ContributionBuilderStorageInXMLDB.getCommitterCollection() : c=="+committer);
		Collection committerCollection = null;
		try {
			committerCollection = contributionBuilderRoot.getChildCollection(committer);
		} catch (XMLDBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Error accessing contribution build database for this committer ("+committer+").");
		}
		if (committerCollection == null) {
			if (createIfMissing) {
				committerCollection = dbHelper.createChildCollection(contributionBuilderRoot, committer);
			} else {	
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No contribution builds active by this committer ("+committer+").");
			}
		}
		return committerCollection;
	}

	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.db.xmldb.ContributionBuilderStorage#getTempObjectIdsInContributionBuild(java.lang.String, java.lang.String, java.lang.String)
	 */
	public List<String>	getTempObjectIdsInContributionBuild(String committer, String ehrId, String contributionID) throws ResourceException
	{
		try{	
			Collection contributionBuilder = getContributionBuildCollection(
					committer, ehrId, contributionID);

			String[] idArray = contributionBuilder.listResources();
			ArrayList<String> objectsInContribution = new ArrayList<String>();
			for (int i = 0; i < idArray.length; i++) {
				String array_element = idArray[i];
				try {
					String decodedID = URLDecoder.decode(array_element,"UTF-8");
					if (!decodedID.equals("AUDIT_DETAILS")) objectsInContribution.add(decodedID);
				} catch (UnsupportedEncodingException e) {
					// TODO Throw better exception later...
					e.printStackTrace();
					throw new XMLDBException();
				}
			}										
			return objectsInContribution;
		} catch (XMLDBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

	}

	private Collection getContributionBuildCollection(String committer,
			String ehrId, String contributionID) throws XMLDBException {
		
		Collection ehrCollection = getEHRCollection(committer, ehrId, false);
		
		Collection contributionBuilder = ehrCollection.getChildCollection(contributionID);
		if (contributionBuilder == null) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No active contribution build with this ID ("+contributionID+") found for this EHR ("+ehrId+") by this committer ("+committer+") .");
		}
		return contributionBuilder;
	}
	
	private Collection getDefaultContributionBuildCollection(String committer,
			String ehrId, boolean createIfMissing, String system_id) throws Exception {
		
		Collection ehrCollection = getEHRCollection(committer, ehrId, createIfMissing);
		
		String[] listOfChildCollectionNames = ehrCollection.listChildCollections();
				
		for (int i = 0; i < listOfChildCollectionNames.length; i++) {
			if (listOfChildCollectionNames[i].equalsIgnoreCase(DEFAULT)) {
				// return default if found
				return ehrCollection.getChildCollection(listOfChildCollectionNames[i]);
			}
		}
		
		// goes here if default is missing
		if (createIfMissing) {
			// Check
			
			// Create a new collection and return it
			return createNewEmptyContributionBuild(committer, ehrId, DEFAULT, system_id, "Auto-created default contribution build.");
		} else {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No active contribution build found for this EHR ("+ehrId+") by this committer ("+committer+") .");
		}			
		
	}


	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.db.xmldb.ContributionBuilderStorage#getObjectInContributionBuild(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public ContibutionBuilderItem<String> getObjectInContributionBuild (String committer, String ehrId, 
			String contributionID, String tempID) throws ResourceException {
		try{
			Collection contributionBuilder = 
				getContributionBuildCollection(committer, ehrId, contributionID);
			
			// TODO: GO ON FIXING FROM HERE
			
			XMLResource resource = (XMLResource) contributionBuilder.getResource(urlEncode(tempID));
			System.out
					.println(">>>>> ContributionBuilderStorageInXMLDB.getObjectInContributionBuild() contributionBuilder.getResource:");
			
			if (resource != null){
				Node resultNode = null;
				resultNode = resource.getContentAsDOM();
				
				// Convert dom node to InputStream
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				Source xmlSource = new DOMSource(resultNode);
				Result outputTarget = new StreamResult(outputStream);
				TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
				InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
				
				// Reconstruct ContibutionBuilderItem from storage
			    XMLDecoder decoder = new XMLDecoder(is);

					    // MyClass is declared in Serializing a Bean to XML
			    ContibutionBuilderItem<String> cbi = (ContibutionBuilderItem<String>)decoder.readObject();
			    decoder.close();

				//System.out.println("ContributionBuilderStorageInXMLDB.getObjectInContributionBuild( cbi after ) "+cbi.getMediaType());
			    
				return cbi;
			} else {
				return null; // returning null means "no such object found"
			}

		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}

	
			
	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.db.xmldb.ContributionBuilderStorage#assembleContributionObjectList(java.lang.String, java.lang.String, java.lang.String)
	 */
	public List<ContibutionBuilderItem<String>> assembleContributionObjectList(String committer, String ehrId, String contributionID){
		
		// TODO: Possibly encapsulate the code of this method in a DB transaction or better: rewrite to a single xQuery that executes atomically 
		
		// First check if the contrib exists and can be fetched
		List<String> idList = getTempObjectIdsInContributionBuild(committer, ehrId, contributionID);
		if (idList.size()<1) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "The contribution "+contributionID+" does not contain any objects!");
		}
		
//		AUDITDETAILS auDetXMLobj = null;
//		try {
//			XMLResource auDetXML = (XMLResource) contributionBuilderRoot.getChildCollection(ehrId).getChildCollection(contributionID).getResource("AUDIT_DETAILS");
//			auDetXMLobj = AUDITDETAILS.Factory.parse(auDetXML.getContentAsDOM());
//		} catch (Exception e) {
//			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "The contribution "+contributionID+" could not be assembled: "+e.getMessage());
//		}
		
//		ContributionBuild contributionNode = ContributionBuild.Factory.newInstance();
//		if (audit_details_description != null && audit_details_description.length() > 0) {
//			contributionNode.addNewDescription().setValue(audit_details_description);
//		} else {
//			// Reuse existing stored descritpion (possibly given at creation time)
//			contributionNode.addNewDescription().set(auDetXMLobj.getDescription());
//		}

		List<ContibutionBuilderItem<String>> resultlist = new ArrayList<ContibutionBuilderItem<String>>();
		
		for (String tempId : idList) {
			ContibutionBuilderItem<String> cbi = getObjectInContributionBuild(committer, ehrId, contributionID, tempId);
//			if ((cbi instanceof VersionedObjectListItem || cbi instanceof ContibutionBuilderItem) && (cbi.getData() instanceof Node)) {
//				VersionedObjectListItem<Node> voli = (VersionedObjectListItem)cbi;
				resultlist.add(cbi);				
//			} else {
//				throw new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED, "-The datatypes in the Contribution Builder ("
//						+cbi.getClass().getCanonicalName()+"&lt;"+cbi.getData().getClass().getCanonicalName()+"&gt;) did not match the destination"); 
//				//TODO: Check HTTP spec if the above errorcode is appropriate here
//			}
			System.out
					.println("ContributionBuilderStorageInXMLDB.assembleContributionObjectList()");
		}
		
		return resultlist;
	}
		
//	commitContributionOfOriginalVersions(PartyProxy committer, String ehrId, String systemId,
//	List<VersionedObjectListItem<Node>> objectList, DvText optionalContributionDescription, UID optionalSuggestedContributionID)

		//Time committed
		//Change type
		//
		//Create an AUDIT_DETAILS stub
		//		Namespace ns = Namespace.getNamespace("OE", "http://schemas.openehr.org/v1");
		//		Element feeder_audit = new Element("AUDIT_DETAILS", ns);
		//		feeder_audit.addContent(new Element("system_id").addContent(system_id));
		//		feeder_audit.addContent(new Element("committer").addContent(new Element("external_ref").addContent(committer)));
		//		feeder_audit.addContent(new Comment("This is an incomplete stub created in the class ContributionBuilderInitiatorResource, the external_ref above is not complete either."));
		//		org.jdom.Document jdoc = new org.jdom.Document(feeder_audit);
		//		
		//		DOMOutputter dop = new DOMOutputter();
		//		Document doc = null;
		//		
		//		try {
		//			doc = dop.output(jdoc);
		//		} catch (JDOMException e1) {
		//			// TODO Auto-generated catch block
		//			e1.printStackTrace();
		//		}
		//		
		//		XMLResource resource = (XMLResource) contributionBuilder.createResource("AUDIT_DETAILS", XMLResource.RESOURCE_TYPE);
		//		resource.setContentAsDOM(doc);
		//		
		//		contributionBuilder.storeResource(resource);
		//TODO: Add lock code for the objects contained in the ongoing contributions
		//TransactionService transaction = (TransactionService)creatingSystem.getService("TransactionService", "1.0");
		//transaction.begin();

		//TODO: Add the various objects to the DB
		//TODO: Commit the objects to the database
		//transaction.commit();
		//TODO: Add the contribution to the contributionDB
	

	
	
	
	//private Document createAuditDetailsDocument(String committer, String system_id) {
	private Node createAuditDetailsDocument(AuditDetails auDet) throws XMLBindingException {
		// TODO: Possibly update using Java-ref.impl instead (as in EHRXMLDBHandler)
		AUDITDETAILS auDetAsXML = (AUDITDETAILS) xmlBinding.bindToXML(auDet);
//		System.out
//				.println("ContributionBuilderStorageInXMLDB.createAuditDetailsDocument()\n"+
//						auDetAsXML.xmlText());
		XmlOptions xopt = new XmlOptions();
		xopt.setSaveOuter();
		xopt.setSaveSyntheticDocumentElement(new QName(XMLBinding.SCHEMA_OPENEHR_ORG_V1, "AUDIT_DETAILS"));
		return auDetAsXML.newDomNode(xopt);
//		
//		Element feeder_audit = new Element("AUDIT_DETAILS", xmlNamespace);
//		feeder_audit.addContent(new Element("system_id").addContent(auDet.getSystemId()));
//		Element partyRef = new Element("external_ref");
//		PartyRef externalRef = auDet.getCommitter().getExternalRef();
//		partyRef.addContent(new Element("id").addContent(externalRef.getId()));
//		partyRef.addContent(new Element("namespace").addContent(externalRef.system_id));
//		partyRef.addContent(new Element("type").addContent("PARTY"));
//		feeder_audit.addContent(new Element("committer").addContent(partyRef));
//		feeder_audit.addContent(new Element("time_committed").addContent(new Element("value").addContent(sdf.format( new Date() ))));
//		feeder_audit.addContent(new Comment("This is an incomplete stub created in the class ContributionBuilderStorageInXMLDB. " +
//				"Date and possibly other things are likely to be updated, and change_type should be added, upon committing contribution to backing storage system. " +
//		"A proper storage implementation may also delete this comment since it is not part of the openEHR semantics declared in the specifications :-)"));
//		org.jdom.Document jdoc = new org.jdom.Document(feeder_audit);
//
//		DOMOutputter dop = new DOMOutputter();
//		Document doc = null;
//
//		try {
//			doc = dop.output(jdoc);
//		} catch (JDOMException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		return doc;
	}

//	private void updateAuditDetailsTime(String committer, String ehrId, String contributionID)
//	{
//		Collection composerCollection, ehrCollection, contributionBuilder;
//
//		try {		
//			composerCollection = dbHelper.createChildCollection(contributionBuilderRoot, committer);
//			ehrCollection = dbHelper.createChildCollection(composerCollection, ehrId);
//			contributionBuilder =  dbHelper.createChildCollection(ehrCollection, contributionID);
//
//			XUpdateQueryService updateService = dbHelper.getXUpdateQueryService(contributionBuilder);
//
//			String xqueryString = 
//				"UPDATE" +
//				" replace $x in doc('AUDIT_DETAILS', 'OngoingContributions/"+committer+"/"+ehrId+"/"+contributionID+"')//time_committed/value" +
//				" with" +
//				" <value>{current-dateTime()}</value>";
//
//			updateService.update(xqueryString);	
//
//			/* Example:
//	UPDATE  
//	replace $x in doc('AUDIT_DETAILS', 'contributionBuilder/dr_who/1234567/dd3035a7-d0bb-4723-85de-8749b09c5ef7')//time_committed/value
//	with 
//	<value>{current-dateTime()}</value>		
//			 */
//
//			// The QXJ beta implementation does not support update yet, thus commented out example below.				
//
//			//			try {
//			//				XQExpression xqe = xqConnection.createExpression();
//			//				// the document location is on the format: doc('document-name', 'collection-name')
//			//				
//			// Another example returning time found in AUDIT_DETAILS (useful for garbage collect later?) 
//			// String xqueryString = "for $x in doc('AUDIT_DETAILS', 'OngoingContributions/"+committer+"/"+ehrId+"/"+contributionID+"')//time_committed/value/text() return $x";				
//			//				XQResultSequence rs = xqe.executeQuery(xqueryString);
//			//				// Print entire result set
//			//				while (rs.next())
//			//					System.out.println("Result set: "+rs.getItemAsString(null));
//			//			} catch (Exception e) {
//			//				// TODO: handle exception
//			//				e.printStackTrace();
//			//			}
//
//
//
//		} catch (XMLDBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
