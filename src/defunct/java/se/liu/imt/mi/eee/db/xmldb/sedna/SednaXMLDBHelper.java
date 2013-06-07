package se.liu.imt.mi.eee.db.xmldb.sedna;

// Sedna handling
//Start and Shutdown Sedna
//To start Sedna server go to SEDNA_INSTALL_DIR/bin and run 
//se_gov
//To shutdown Sedna server run 
//se_stop
//Create and Run a Database
//To create a database named testdb: 
//se_cdb testdb
//To run the testdb database: 
//se_sm testdb
//To shutdown the testdb database: 
//se_smsd testdb

import java.io.IOException;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;

import net.cfoster.sedna.xqj.SednaXQDataSource;

import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.TransactionService;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XQueryService;
import org.xmldb.api.modules.XUpdateQueryService;

import se.liu.imt.mi.eee.db.xmldb.XMLDBHelper;
import se.liu.imt.mi.eee.structure.EEEConstants;

public class SednaXMLDBHelper extends XMLDBHelper implements EEEConstants {

	// TODO: Possibly refactor to create and throw a generic DB exception that can be reused by different DB providers
	// Useful:
	// http://www.jdom.org/docs/apidocs/org/jdom/output/DOMOutputter.html

	protected String databaseHost ;
	protected String databaseName;		

	protected String databaseUsername;
	protected String databasePassword;
	
	protected String databaseURI;
	protected Collection rootCollection;
	protected XQDataSource xqs;
	protected XQConnection xqConnection;
	
	public SednaXMLDBHelper(String databaseHost, String databaseName,
			String databaseUsername, String databasePassword, DatabaseMode databaseMode) throws InstantiationException, IllegalAccessException, ClassNotFoundException, XMLDBException, XQException {
		this.databaseHost = databaseHost;
		this.databaseName = databaseName;
		this.databaseUsername = databaseUsername; 
		this.databasePassword = databasePassword;
		this.databaseMode = databaseMode;
		
		databaseURI = "xmldb:sedna://"+databaseHost+"/"+databaseName;
		Database dbDriver = (Database) Class.forName("net.cfoster.sedna.DatabaseImpl").newInstance();
		DatabaseManager.registerDatabase(dbDriver);
		rootCollection = DatabaseManager.getCollection(databaseURI, databaseUsername, databasePassword);
		
		// Adding xquery java support:
		xqs = new SednaXQDataSource();
	    XQDataSource xqs = new SednaXQDataSource();
	    xqs.setProperty("serverName", databaseHost);
	    xqs.setProperty("databaseName", databaseName);
	    
		//xqConnection = xqs.getConnection(databaseUsername, databasePassword);

	}

	// FIXME: Remove this constructor
	public SednaXMLDBHelper() throws Exception {
		this("localhost","EEE_EHR","SYSTEM", "MANAGER", DatabaseMode.SINGLE_RECORD);
	}

	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.XMLDBHelper#getRootCollection()
	 */
	public Collection getRootCollection() {
		return rootCollection;
	}

//	public void closeSedna() throws Exception {
//		//xqs.getConnection().close();
//		//rootCollection.close();
//	}


	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.XMLDBHelper#createChildCollection(org.xmldb.api.base.Collection, java.lang.String)
	 */
	public Collection createChildCollection(Collection collection, String collectionName) throws XMLDBException {
		CollectionManagementService cms = (CollectionManagementService) collection.getService(
				"CollectionManagementService", "1.0");

		return cms.createCollection(collectionName);
	}

	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.XMLDBHelper#deleteCollection(org.xmldb.api.base.Collection, java.lang.String)
	 */
	public void deleteCollection(Collection collection, String collectionName) throws XMLDBException {
		CollectionManagementService cms = (CollectionManagementService) collection.getService(
				"CollectionManagementService", "1.0");

		cms.removeCollection(collectionName);
	}
	
	public TransactionService createTransaction(Collection collection) throws XMLDBException
	{
		return (TransactionService) collection.getService("TransactionService", "1.0");
	}
	
	public XPathQueryService getXPathQueryService(Collection collection) throws XMLDBException
	{
		return (XPathQueryService) collection.getService("XPathQueryService","1.0");
	}

	public XQueryService getXQueryService(Collection collection) throws XMLDBException
	{
		return (XQueryService) collection.getService("XQueryService","1.0");
	}
	
	
	/**
	 * Sedna-specific way of getting time.
	 */
	public String getCurrentDatabaseTimeAsISODateTimeString() throws IOException {
		try {
			// result needs to be valid XML node thus adding surrounding tags
			XPathQueryService xPathQueryService = getXPathQueryService(rootCollection);
			
			ResourceSet rs = xPathQueryService.query("" +
					"<t>{fn:current-dateTime()}</t>");	
			String result = rs.getResource(0).toString();		
			// Trim beginning and end and return trimmed string
			return result.substring(3, result.length()-4);
		} catch (XMLDBException e) {
			throw new IOException(e.getMessage());
		}
	}

}