package se.liu.imt.mi.eee.ehr.res;

import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.jdom.JDOMException;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Node;

import se.liu.imt.mi.eee.db.EHRDatabaseReadInterface;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.trigger.EhrMetadataCache;
import se.liu.imt.mi.eee.utils.Util;
import se.liu.imt.mi.ehr.x2010.eeeV1.CONTRIBUTION;

public class ContributionListingResource extends WadlServerResource {
	
	EHRDatabaseReadInterface<Node, Node> dbConnection;
	protected String ehrId;
	protected int start;
	protected int end;
	protected List<Node> contribList;
	protected String etag;
	
	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		System.out.println("ContributionListingResource.doInit()");
		
		// Set WADL metadata
		this.setName("Contribution List");
		this.setDescription("This resource lists the contributions of an EHR");
		// TODO: add WADL description of parameters

		// Get the db handle from context (used both for POST & GET handling)
		dbConnection = (EHRDatabaseReadInterface<Node, Node>) getContext().getAttributes().get(EEEConstants.KEY_TO_BASIC_DB_READER);
		ehrId = (String) getRequestAttributes().get(EEEConstants.EHR_ID);
		EhrMetadataCache ehrMetadataCache = (EhrMetadataCache) getContext().getAttributes().get(EEEConstants.KEY_TO_EHR_METADATA_CACHE);		

		etag = Util.checkOrPopulateCacheThenReturn304EarlyIfETagsMatch(this, ehrMetadataCache, dbConnection, ehrId);
		
		// TODO: document and formalize start and end as constants in EEE-constants or this class
		String startAsObj = getQuery().getFirstValue("start", "1");
		String endAsObj = getQuery().getFirstValue("end", "20");

		// TODO: Catch and convert format errors to correct ResourceException
		start = Integer.parseInt((String) startAsObj);
		end = Integer.parseInt((String) endAsObj);
		
	}	

	
//	@Get("txt")
//	public String getRepresentation() { 
//		System.out.println("ContributionListingResource.getRepresentation(txt)!!!");
//		// TODO: use freemarker or other methods to make nice html, xml etc
//		return "You called ContributionListingResource.getRepresentation for ehr "+ehrId+" UGLY LISTING FOLLOWS:"+contribList.toString();
//	}
	
	@Get("xml")
	public Representation getXmlRepresentation() throws JDOMException, XmlException { 
		
		contribList = dbConnection.listContributionsDescending(ehrId, start, end);		
		if (contribList == null) throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No contributions (between position "+start+" and "+end+") were found for the EHR with id="+ehrId);
		
		// TODO: perhaps use real xml handling instead of strings...
		System.out.println("ContributionListingResource.getRepresentation(xml)!!!");
		String theList ="";
		for (Node contr : contribList){
			CONTRIBUTION co = CONTRIBUTION.Factory.parse(contr);			
			//System.out.println("ContributionListingResource.getXmlRepresentation(co.xmlText()) "+co.xmlText());
			theList = theList+co.xmlText();
		}
		StringRepresentation reprToReturn = new StringRepresentation("<contributions start=\""+start+"\" end=\""+end+"\">"+theList+"</contributions>", MediaType.APPLICATION_ALL_XML);
		if (etag != null) {
			reprToReturn.setTag(new Tag(etag)); 
			// TODO: Add last modified later
		}	
		return reprToReturn;
	}

	// TODO: Add Java and JSON as return formats!
}
