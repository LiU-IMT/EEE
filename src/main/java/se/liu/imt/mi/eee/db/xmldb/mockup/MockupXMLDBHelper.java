package se.liu.imt.mi.eee.db.xmldb.mockup;

import java.io.IOException;

import org.apache.commons.lang.NotImplementedException;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.CompiledExpression;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.TransactionService;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XQueryService;
import org.xmldb.api.modules.XUpdateQueryService;

import se.liu.imt.mi.eee.db.xmldb.XMLDBHelper;

public class MockupXMLDBHelper extends XMLDBHelper {
	
	Collection rootCollection;
	private DateTimeFormatter dateFormatter;
	
	public MockupXMLDBHelper() {
		super();
		dateFormatter = ISODateTimeFormat.dateTime();
		rootCollection = new MockupXMLDBCollection("ROOT", null);
	}

	public Collection createChildCollection(Collection collection,
		String collectionName) throws XMLDBException {
		return ((MockupXMLDBCollection)collection).createChildCollection(collectionName);
	}

	public void deleteCollection(Collection collection, String collectionName)
			throws XMLDBException {
		((MockupXMLDBCollection)collection).deleteCollection(collectionName);
	}
	

	public Collection getRootCollection() {
		return rootCollection;
	}

	/**
	 * @return Returns a dummy (non-functional) TransactionService
	 */
	public TransactionService createTransaction(final Collection collection)
			throws XMLDBException {		
		return (TransactionService) new MockupTransactionService(collection);
	}

	public XPathQueryService getXPathQueryService(Collection collection)
			throws XMLDBException {
		throw new NotImplementedException();
	}

	public XQueryService getXQueryService(Collection collection) throws XMLDBException {
		return new XQueryService() {	
			public void setProperty(String arg0, String arg1) throws XMLDBException {
				// TODO Auto-generated method stub
			}
			
			public String getProperty(String arg0) throws XMLDBException {
				// TODO Auto-generated method stub
				return null;
			}
			
			public void setCollection(Collection arg0) throws XMLDBException {
				// TODO Auto-generated method stub
			}
			
			public String getVersion() throws XMLDBException {
				// TODO Auto-generated method stub
				return null;
			}
			
			public String getName() throws XMLDBException {
				// TODO Auto-generated method stub
				return null;
			}
			
			public void setXPathCompatibility(boolean arg0) {
				// TODO Auto-generated method stub			
			}
			
			public void setNamespace(String arg0, String arg1) throws XMLDBException {
				// TODO Auto-generated method stub
			}
			
			public void setModuleLoadPath(String arg0) {
				// TODO Auto-generated method stub
			}
			
			public void removeNamespace(String arg0) throws XMLDBException {
				// TODO Auto-generated method stub				
			}
			
			public ResourceSet queryResource(String arg0, String arg1)
					throws XMLDBException {
				// TODO Auto-generated method stub
				return null;
			}
			
			public ResourceSet query(String arg0) throws XMLDBException {
				// TODO Auto-generated method stub
				return null;
			}
			
			public String getNamespace(String arg0) throws XMLDBException {
				// TODO Auto-generated method stub
				return null;
			}
			
			public ResourceSet execute(CompiledExpression arg0) throws XMLDBException {
				// TODO Auto-generated method stub
				return null;
			}
			
			public void declareVariable(String arg0, Object arg1) throws XMLDBException {
				// TODO Auto-generated method stub				
			}
			
			public CompiledExpression compile(String arg0) throws XMLDBException {
				// TODO Auto-generated method stub
				return null;
			}
			
			public void clearNamespaces() throws XMLDBException {
				// TODO Auto-generated method stub
				
			}
		};
		//throw new NotImplementedException();

	}

	public XUpdateQueryService getXUpdateQueryService(Collection collection)
			throws XMLDBException {
		throw new NotImplementedException();
	}	
	

	public String getCurrentDatabaseTimeAsISODateTimeString() throws IOException {	
		// TODO: FIXME: Temp workaround for Mockup DB ... remove later
		//eturn "2010-04-10T14:08:17+10:30";
		return dateFormatter.print(System.currentTimeMillis());

	}
	

}
