package se.liu.imt.mi.eee.db.xmldb;

import java.io.IOException;
import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;

import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.TransactionService;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XQueryService;
import org.xmldb.api.modules.XUpdateQueryService;

import se.liu.imt.mi.eee.db.DatabaseInterface;
import se.liu.imt.mi.eee.utils.Util;

public abstract class XMLDBHelper implements DatabaseInterface {
	
	public abstract Collection getRootCollection();
	public abstract Collection createChildCollection(Collection collection, String collectionName) throws XMLDBException;
	public abstract void deleteCollection(Collection collection, String collectionName) throws XMLDBException;
	public abstract TransactionService createTransaction(Collection collection) throws XMLDBException;	
	public abstract XPathQueryService getXPathQueryService(Collection collection) throws XMLDBException;
	public abstract XQueryService getXQueryService(Collection collection) throws XMLDBException;
	public abstract String getCurrentDatabaseTimeAsISODateTimeString() throws IOException;

	protected DatabaseMode databaseMode = DatabaseMode.SINGLE_RECORD;
		
	/**
	 * Warning: a recursive function that cleans the collection, all it's child collections and so on...
	 * @param collectionToBecleaned
	 * @throws XMLDBException 
	 */
	public boolean cleanCollectionAndRemoveSubCollections(Collection collectionToBecleaned)
			throws XMLDBException {
					//Delete all
					String[] childCollections = collectionToBecleaned.listChildCollections();
					if (childCollections.length > 0) {
						for (String collectionName : childCollections) {
							boolean deleteMe = cleanCollectionAndRemoveSubCollections(collectionToBecleaned.getChildCollection(collectionName));
							if (deleteMe) {
								deleteCollection(collectionToBecleaned, collectionName);
							}
						}				
					} else {
						// TODO: check if this really is necessary...
						String[] childResources = collectionToBecleaned.listResources();
						for (String childName : childResources) {
							collectionToBecleaned.removeResource(collectionToBecleaned.getResource(childName));
						}
						return true;
					}
					return false;
			}
	
	public Date getCurrentDatabaseTime() throws IOException {
		try {
			return Util.convertIsoDateTimeStringToJavaDate(getCurrentDatabaseTimeAsISODateTimeString());
		} catch (DatatypeConfigurationException e) {
			throw new IOException(e.getMessage());
		}
	}
		
	public DatabaseMode getDatabaseMode() {
		return databaseMode;
	}
	
	

}