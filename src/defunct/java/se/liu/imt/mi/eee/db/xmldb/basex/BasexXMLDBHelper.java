/**
 * 
 */
package se.liu.imt.mi.eee.db.xmldb.basex;

import java.io.IOException;

import javax.xml.xquery.XQException;

import org.basex.api.xmldb.BXCollection;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.Service;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.TransactionService;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XQueryService;

import se.liu.imt.mi.eee.db.xmldb.CombinedXQueryXpathService;
import se.liu.imt.mi.eee.db.xmldb.XMLDBHelper;
import se.liu.imt.mi.eee.db.xmldb.mockup.MockupTransactionService;
import se.liu.imt.mi.eee.db.xmldb.mockup.MockupXMLDBHelper;
import se.liu.imt.mi.eee.utils.Util;

/**
 * @author Erik Sundvall
 *
 * Note that http://docs.basex.org/wiki/Java_Examples#XML:DB_API_Examples
 * says "the older XML:DB API can only be used in embedded mode."
 */
public class BasexXMLDBHelper extends XMLDBHelper {

	private BXCollection rootCollection;
	private DateTimeFormatter dateFormatter;

	public BasexXMLDBHelper(String databaseName, DatabaseMode databaseMode) throws InstantiationException, IllegalAccessException, ClassNotFoundException, XMLDBException, XQException {
//		this.databaseHost = databaseHost;
//		this.databaseName = databaseName;
//		this.databaseUsername = databaseUsername; 
//		this.databasePassword = databasePassword;
		this.databaseMode = databaseMode;
		dateFormatter = ISODateTimeFormat.dateTime();
		
//		String databaseURI = "xmldb:exist://"+databaseHost+"/exist/xmlrpc"+databaseName;
		
		Database dbDriver = (Database) Class.forName("org.basex.api.xmldb.BXDatabase").newInstance();
//		DatabaseManager.registerDatabase(dbDriver);
		
		rootCollection = new BXCollection(databaseName, true, dbDriver);
		//rootCollection = DatabaseManager.getCollection(databaseURI); //, databaseUsername, databasePassword);
		
		Service[] services = rootCollection.getServices();
		for (Service service : services) {
			System.out.println("BasexXMLDBHelper.BasexXMLDBHelper() service:" + service.getName() + " = " +service.toString());		
		}
	}


	
	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.XMLDBHelper#getRootCollection()
	 */
	public Collection getRootCollection() {
		return rootCollection;
	}


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
	

	
	
	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.db.xmldb.XMLDBHelper#createTransaction(org.xmldb.api.base.Collection)
	 */
	@Override
	public TransactionService createTransaction(Collection collection)
			throws XMLDBException {
		TransactionService ts = (TransactionService) collection.getService("TransactionService", "1.0");
		if (ts == null) {
			return (TransactionService) new MockupTransactionService(collection);
		} else {
			ts.setCollection(collection);
			return ts;	
		}
	}



	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.db.xmldb.XMLDBHelper#getCurrentDatabaseTimeAsISODateTimeString()
	 */
	@Override
	public String getCurrentDatabaseTimeAsISODateTimeString() throws IOException {
//		try {
//			// result needs to be valid XML node thus adding surrounding tags
//			XPathQueryService xPathQueryService = getXPathQueryService(rootCollection);
//			
//			ResourceSet rs = xPathQueryService.query("<t>{current-dateTime()}</t>");	
//			String result = rs.getResource(0).toString();		
//			// Trim beginning and end and return trimmed string
//			return result.substring(3, result.length()-4);
//		} catch (XMLDBException e) {
//			throw new IOException(e.getMessage());
//		}

		String time = dateFormatter.print(System.currentTimeMillis());
		System.out
				.println("BasexXMLDBHelper.getCurrentDatabaseTimeAsISODateTimeString() ------ "+time);
		return time;
	
	}

	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.db.xmldb.XMLDBHelper#getXPathQueryService(org.xmldb.api.base.Collection)
	 */
	@Override
	public XPathQueryService getXPathQueryService(Collection collection)
			throws XMLDBException {
		return (XPathQueryService) collection.getService("XPathQueryService","1.0");
	}

	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.db.xmldb.XMLDBHelper#getXQueryService(org.xmldb.api.base.Collection)
	 */
	@Override
	public XQueryService getXQueryService(Collection collection)
			throws XMLDBException {
		return new CombinedXQueryXpathService((XPathQueryService) collection.getService("XQueryQueryService","1.0"));
	}


}
