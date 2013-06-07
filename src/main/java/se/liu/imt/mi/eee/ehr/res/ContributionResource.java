package se.liu.imt.mi.eee.ehr.res;


import org.openehr.rm.support.identification.HierObjectID;
import org.openehr.rm.support.identification.UID;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.ext.xml.XmlRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import se.liu.imt.mi.eee.db.EHRDatabaseReadInterface;
import se.liu.imt.mi.eee.db.xmldb.EHRXMLDBHandler;
import se.liu.imt.mi.eee.db.xmldb.XMLDBHelper;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.utils.Util;

/**
 * Resource that via GET can serve information about identified
 * contributions. 
 * 
 * It can also to handle validated contribution commits via 
 * PUT or POST (an alternative way to do that is via DB calls
 * directly from ContributionBuilder)
 * 
 * @author Erik Sundvall
 */
public class ContributionResource extends WadlServerResource implements EEEConstants {

	EHRDatabaseReadInterface<Node, Node> dbReader;
	protected String ehrID;
	protected String contributionID;
	
	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		// Get the db handle from context (used both for POST & GET handling)
		dbReader = (EHRDatabaseReadInterface<Node, Node>) getContext().getAttributes().get(EEEConstants.KEY_TO_BASIC_DB_READER);
		this.setName("Contribution");
		this.setDescription("This resource represents an openEHR CONTRIBUTION object " +
				"that contains metadata and identifiers of VERSIONs that were " +
				"created in the CONTRIBUTION.");
		ehrID = (String) getRequestAttributes().get(EHR_ID);
		contributionID = (String) getRequestAttributes().get(CONTRIBUTION_ID);
	}

//	/**
//	 * Validates and stores the content of a ContributionBuild XML document
//	 * (@see http://www.imt.liu.se/mi/ehr/2010/EEE-v1.xsd),
//	 * @return returns a Contribution XML document containing references etc. Returns error message if validation or storage failed.
//	 */
//  FIXME: Make sure contibs are only allowed in single mode 	
//	@Post("xml:xml")
//	public Representation validateAndStoreXMLContribution(XmlRepresentation incomingRepresentation){
//		String errorMessage = "";
//		Node rootDomNode = incomingRepresentation.getNode("/");
//		System.out
//				.println("ContributionResource.validateAndStoreXMLContribution(root): "+
//						rootDomNode.getLocalName());
//		// Loop through items
//		Node currentNode = incomingRepresentation.getNode("/");
//		
//		// Send to conversion/validation
//		// Extract contribution metadata
//		// Send to storage
//		// Return contribution object as XML
//	}
	
//	@Post // Other types than XML
//	public Representation validateAndStoreContribution(Representation incomingRepresentation){
//		if (incomingRepresentation.getMediaType().equals(MediaType.APPLICATION_JAVA_OBJECT)) {
//			// Do java stuff here
//			System.out.println("ContributionResource.validateAndStoreContribution(java)");
//		} else if (incomingRepresentation.getMediaType().equals(MediaType.APPLICATION_JSON)){
//			// Do json stuff here
//			System.out.println("ContributionResource.validateAndStoreContribution(json)");
//		}
//		return new StringRepresentation("Succesful (xml) post!\n" 
//				+ Util.requestInfoToString(getRequest()));
//	}

	@Get("xml")
	public Representation getXMLRepresentation() throws ResourceException{
		String info = "You called ContributionResource.getRepresentation for the contribution with ID: "+contributionID+" from the EHR with ID: "+ehrID;

		// Check that the ID is possible to convert
		HierObjectID contribId = new HierObjectID(contributionID);

		// FIXME: handle XML DB etc returning other kinds
		Document contributionDoc = (Document) dbReader.getContribution(ehrID, contribId.getValue());
		if (contributionDoc == null) throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find any such contribution from database.\n "+info);
		return new DomRepresentation(MediaType.TEXT_XML, contributionDoc);

	}
	
//	@Get("html")
//	public Representation getHtmlRepresentation() throws ResourceException{
//		// TODO Auto-generated method stub
//		String info = "You called ContributionResource.getRepresentation for the contribution with ID: "+contributionID+" from the EHR with ID: "+ehrID;
//
//		try {
//			// Check that the ID is possible to convert
//			HierObjectID contribId = new HierObjectID(contributionID);
//			
//			// FIXME: handle XML DB etc returning other kinds
//			return new DomRepresentation(MediaType.TEXT_XML, (Document) dbReader.getContribution(ehrID, contribId.getValue()));
//		} catch (Exception e) {
//			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
//			return new StringRepresentation("Could not get contribution from database.\n "+info+" \n"+e.toString());
//			//throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not get contribution from database. "+info);
//		}
//		
//	}


}
