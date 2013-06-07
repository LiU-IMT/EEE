package se.liu.imt.mi.eee.db.xmldb.mockup;

import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.TransactionService;

public class MockupTransactionService implements TransactionService {
	public Collection collection;

	public MockupTransactionService(Collection arg0) {
		this.collection = arg0;
	}
	
	public void setProperty(String arg0, String arg1) throws XMLDBException {
		// TODO Auto-generated method stub
		
	}
	
	public String getProperty(String arg0) throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setCollection(Collection arg0) throws XMLDBException {
		this.collection = arg0;		
	}
	
	public String getVersion() throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getName() throws XMLDBException {
		// TODO Auto-generated method stub
		return "MockupTransactionService for collection: "+collection.getName();
	}
	
	public void rollback() throws XMLDBException {
		System.out.println("MockupTransactionService...transaction...rollback()" +
						" called for collection:"+collection.getName());
	}
	
	public void commit() throws XMLDBException {
		System.out.println("MockupTransactionService...transaction...commit()" +
				" called for collection:"+collection.getName());
	}
	
	public void begin() throws XMLDBException {
		System.out.println("MockupTransactionService...transaction...begin()" +
				" called for collection:"+collection.getName());
	}
}