package se.liu.imt.mi.eee.db.xqj;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Databases;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.CreateDB;
import org.basex.util.list.StringList;
import org.json.JSONObject;
import org.openehr.rm.common.changecontrol.Contribution;
import org.openehr.rm.common.generic.PartyProxy;
import org.openehr.rm.datatypes.text.DvText;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Node;

import se.liu.imt.mi.eee.db.EHRDatabaseReadInterface;
import se.liu.imt.mi.eee.db.EHRDatabaseWriteInterface;
import se.liu.imt.mi.eee.db.SharedXQueries;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.structure.VersionedObjectListItem;
import se.liu.imt.mi.eee.structure.XmlHelper;
import se.liu.imt.mi.eee.utils.Util;


public class EhrXqjHandler extends SharedXQueries implements EEEConstants, EHRDatabaseReadInterface<Node, Node>, EHRDatabaseWriteInterface<Node>{
	
	protected XQConnection readXQJconnection;
	protected XQConnection writeXQJconnection;
	protected XQPreparedExpression xqprep_CurrentTime;
	protected XQPreparedExpression xqprep_getContribution;
	protected XQPreparedExpression xqprep_getContributionLatest;
	protected String ehrDatabaseName;
	protected String contributionDatabaseName;
	private Context dbContext;

	public EhrXqjHandler(XQConnection readXQJconnection, XQConnection writeXqConnection, String ehrDatabaseName, String contributionDatabaseName) throws Exception {
		this.readXQJconnection = readXQJconnection;
		this.writeXQJconnection = writeXqConnection;
		this.ehrDatabaseName = ehrDatabaseName;
		this.contributionDatabaseName = contributionDatabaseName;
		
		xqprep_CurrentTime = readXQJconnection.prepareExpression("current-dateTime()");
		xqprep_getContribution = readXQJconnection.prepareExpression(XQ_GET_CONTRIBUTION);
		xqprep_getContributionLatest = readXQJconnection.prepareExpression(XQ_LATEST_CONTRIBUTION_TIME_AND_ID_AS_JSON);
		
		System.out.println("EhrXqjHandler.EhrXqjHandler() writeXQJconnection.getAutoCommit() = "+writeXQJconnection.getAutoCommit());
		
	    dbContext = new Context();
	    Databases dbs = dbContext.databases;
	    StringList dbList = dbs.listDBs();
	    createDbIfMissing(ehrDatabaseName, dbContext, dbList);
	    createDbIfMissing(contributionDatabaseName, dbContext, dbList);    	
	    
	    System.out.println("EhrXqjHandler.EhrXqjHandler() context.databases() = "+dbs.listDBs().toArray().toString());
	    // "src/main/resources/xml/input.xml"
	    //System.out.println("EhrXqjHandler.EhrXqjHandler() Info = "+Info.info(context));
	    String createDBresult = new CreateDB(ehrDatabaseName, null).execute(dbContext);
	    System.out.println("EhrXqjHandler.EhrXqjHandler() createDBresult = "+createDBresult);

	}

	protected void createDbIfMissing(String ehrDatabaseName, Context context,
			StringList dbList) throws BaseXException {
		if (!dbList.contains(ehrDatabaseName)) {
	    	CreateDB cdb = new CreateDB(ehrDatabaseName);
	    	cdb.execute(context);
	    	System.out.println("EhrXqjHandler.EhrXqjHandler() - Created new EHR database named: "+ehrDatabaseName);
		} else {
	    	System.out.println("EhrXqjHandler.EhrXqjHandler() - Reusing EHR database named: "+ehrDatabaseName);
		}
	}
	
	public void loadExampleData(String ehrDir, String contribDir) throws BaseXException{
		Add add = new Add(ehrDatabaseName, ehrDir);
		add.execute(dbContext);
		Add add2 = new Add(contributionDatabaseName, contribDir);
		add2.execute(dbContext);	
	}
	
	public String testConfig() throws XQException {
		XQExpression xqe = readXQJconnection.createExpression(); 		
		XQResultSequence rs = xqe.executeQuery("for $x in 1 to 4 return $x");
		String stringValue = null;
		while(rs.next()) {
			stringValue = rs.getItemAsString(null); 
			System.out.println(stringValue);
		}; 
		// readXQJconnection.close();
		return stringValue;
	}




	@SuppressWarnings("unused")
	public void createEHR(String ehrId, String systemId) throws IllegalArgumentException, Exception {
		System.out.println("EhrXqjHandler.createEHR("+ehrId+", "+systemId+")");
		//throw new NotImplementedException();	
		
//		<eee:EHR xmlns:eee="http://www.imt.liu.se/mi/ehr/2010/EEE-v1.xsd" xmlns:v1="http://schemas.openehr.org/v1">
//		  <eee:system_id>
//		    <v1:value>test1.eee.mi.imt.liu.se</v1:value>
//		  </eee:system_id>
//		  <eee:ehr_id>
//		    <v1:value>100415-1144</v1:value>
//		  </eee:ehr_id>
//		  <eee:time_created>
//		    <v1:value>2012-05-21T06:06:21.716+02:00</v1:value>
//		  </eee:time_created>
//		</eee:EHR>
		
		// Start transaction
		// writeXQJconnection.setAutoCommit(false); //client side transactions are unsupported in current xqj interface...

		// Check if EHR already exists
		XQExpression xqe = readXQJconnection.createExpression();
		xqe.bindString(new QName("ehrID"), ehrId, null);		
//		XQResultSequence rs = xqe.executeQuery("doc('"+ehrId+"')"); // FIXME: 
		String exists = null; //returnFirstString(rs);	// FIXME: add real existence check
		System.out.println("EhrXqjHandler.createEHR() exists = "+exists);
		if (exists != null) {
			writeXQJconnection.rollback();
			throw new IllegalArgumentException("The ehr_id "+ehrId+" is already in use, try another one");
		} else {
	// Create EHR root XML object
	se.liu.imt.mi.ehr.x2010.eeeV1.EHR ehrXmlObject = constructEhrRootXMLObject(ehrId, systemId);

	// Todo: check if timestamping can be done more efficiently if neccesary
	ehrXmlObject.addNewTimeCreated().setValue(getCurrentDatabaseTimeAsISODateTimeString());
//
//	// Store EHR root XML object
//	XMLResource newEhrRes = (XMLResource) ehrRoot.createResource(ehrId, XMLResource.RESOURCE_TYPE);
//	// Later possibly update timestamp here instead
	Node newDomNode = ehrXmlObject.newDomNode(XmlHelper.getXMLoptionsForEHRRootDocument());
	
	System.out.println("EhrXqjHandler.createEHR() -- "+ehrXmlObject.xmlText(XmlHelper.getXMLoptionsForEHRRootDocument()));

	XQExpression xqeWrite = writeXQJconnection.createExpression(); 
	xqeWrite.bindNode(new QName("contentAsXml"), newDomNode, null);	
	xqeWrite.bindString(new QName("ehrID"), ehrId, null);		

	XQResultSequence rs2 = xqeWrite.executeQuery("declare variable $ehrID as xs:string external; " +
			"declare variable $contentAsXml external; " +			
			//"update insert $contentAsXml into /eee:contributions[@ehr_id=$ehrID] "+
			"insert $contentAsXml into /"); // FIXME: 
	System.out.println("EhrXqjHandler.createEHR() rs2 = "+returnFirstString(rs2));
	
	//CommandBuilder cb = new CommandBuilder(Command)
	//	newEhrRes.setContentAsDOM(newDomNode);
//	System.out.println("EHRXMLDBHandler.createEHR() --- will store");
//	ehrRoot.storeResource(newEhrRes);
//	
//	// Create a contribution root for this EHR (TODO: maybe opened an inner transaction for this)
//	XMLResource newContribRes = (XMLResource) contributionsRoot.createResource(ehrId, XMLResource.RESOURCE_TYPE);			
//	// TODO: Move this to published XML schema (and use xmlBeans?)
//	Element contribRoot = new Element("contributions", EEEConstants.SCHEMA_EEE_OPENEHR_EXTENSION);
//	contribRoot.setAttribute("ehr_id", ehrId);
//	Document contribRootDoc = new Document(contribRoot);
//	DOMOutputter domOut = new DOMOutputter();
//	newContribRes.setContentAsDOM(domOut.output(contribRootDoc));
//	contributionsRoot.storeResource(newContribRes);
//	
//	// Commit the entire EHR creation
//	trans.commit();
	}
//} catch (Exception e) {
//	e.printStackTrace();
//	// Roll back on errors
//	try {
//		trans.rollback();
//		// Oh my, the rollback can also throw exceptions!
//	} catch (Exception e2) {
//		throw e2;
//	}			
//	throw e;
//}		
}

	public String createEHR(String systemId) throws IOException {
		// TODO Auto-generated method stub
		return null; //createEHR(null, systemId);
	}

	public Date getCurrentDatabaseTime() throws Exception {
		return Util.convertIsoDateTimeStringToJavaDate(getCurrentDatabaseTimeAsISODateTimeString());
	}

	public String getCurrentDatabaseTimeAsISODateTimeString() throws Exception {
		XQResultSequence rs = xqprep_CurrentTime.executeQuery();
		return returnFirstString(rs);
	}

	protected String returnFirstString(XQResultSequence rs) throws XQException {
		if(!rs.next()) throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "could not get string from DB");
		String itemAsString = rs.getItemAsString(null);
		rs.close();
		return itemAsString;
	}
	
	protected Node returnFirstNode(XQResultSequence rs) throws XQException {
		if(!rs.next()) throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "could not get Node from DB");
		Node itemAsNode = rs.getNode();
		rs.close();
		return itemAsNode;
	}
	

	public Node getVersionedObject(String ehrID, String objectID,
			String systemID, String treeID) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getVersionedObject_all_version_ids(String ehrID,
			String objectID) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Node getContribution(String ehrID, String contributionID) {
		try {
			xqprep_getContribution.bindString(new QName("ehrID"), ehrID, null);
			xqprep_getContribution.bindString(new QName("contributionID"), contributionID, null);
			XQResultSequence rs = xqprep_getContribution.executeQuery();
			return returnFirstNode(rs);
		} catch (XQException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "could not get contribution from DB");
		}
	}

	public List<Node> listContributionsDescending(String ehrID, int start, int end) {
		// TODO Auto-generated method stub
		return null;
	}

	public JSONObject getContributionsLatest(String ehrID) {
		try {
			xqprep_getContributionLatest.bindString(new QName("ehrID"), ehrID, null);
			XQResultSequence rs = xqprep_getContributionLatest.executeQuery();
			return new JSONObject(returnFirstString(rs));
		} catch (Exception e) {
			e.printStackTrace(); // TODO: remove
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "could not get latest contribution from DB (check if the EHR with ID "+ehrID+" really exists and has any contributions) ");
		}
	}

	public Contribution commitContributionOfOriginalVersions(
			PartyProxy committer,
			String ehrId,
			String systemId,
			List<? extends VersionedObjectListItem<Node>> objectList,
			DvText optionalContributionDescription,
			org.openehr.rm.support.identification.UID optionalSuggestedContributionID)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
