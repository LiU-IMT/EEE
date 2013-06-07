/**
 * 
 */
package se.liu.imt.mi.eee.db.xmldb.mockup;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.Service;
import org.xmldb.api.base.XMLDBException;


/**
 * @author Erik Sundvall
 *
 */
public class MockupXMLDBCollection implements Collection {

	HashMap<String, Resource> resourceMap;
	HashMap<String, Collection> collectionMap;
	String name;
	Collection parent;
	Boolean open = false;
	
	/**
	 * 
	 */
	public MockupXMLDBCollection(String name, Collection parent) {
		resourceMap = new HashMap<String, Resource>();
		collectionMap = new HashMap<String, Collection>();
		this.name = name;
		this.parent = parent; // Null is OK for root collection
		open = true;
	}

	/* (non-Javadoc)
	 * @see org.xmldb.api.base.Collection#close()
	 */
	public void close() throws XMLDBException {
		open = false;
	}

	/* (non-Javadoc)
	 * @see org.xmldb.api.base.Collection#createId()
	 */
	public String createId() throws XMLDBException {
		return UUID.randomUUID().toString();
	}

	/* (non-Javadoc)
	 * @see org.xmldb.api.base.Collection#createResource(java.lang.String, java.lang.String)
	 */
	public Resource createResource(String key, String type)
			throws XMLDBException {
		if (key == null || key.length() < 1) key = createId();
		System.out.println("MockupXMLDBCollection.createResource("+key+","+type+") called");
		return new MockupXMLDBResource(key, type, this); //was ReresourceMap.put(key, null);
	}

	/* (non-Javadoc)
	 * @see org.xmldb.api.base.Collection#getChildCollection(java.lang.String)
	 */
	public Collection getChildCollection(String key) throws XMLDBException {
		return collectionMap.get(key);
	}

	/* (non-Javadoc)
	 * @see org.xmldb.api.base.Collection#getChildCollectionCount()
	 */
	public int getChildCollectionCount() throws XMLDBException {
		return collectionMap.size();
	}

	/* (non-Javadoc)
	 * @see org.xmldb.api.base.Collection#getName()
	 */
	public String getName() throws XMLDBException {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.xmldb.api.base.Collection#getParentCollection()
	 */
	public Collection getParentCollection() throws XMLDBException {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.xmldb.api.base.Collection#getResource(java.lang.String)
	 */
	public Resource getResource(String key) throws XMLDBException {
		return resourceMap.get(key);
	}

	/* (non-Javadoc)
	 * @see org.xmldb.api.base.Collection#getResourceCount()
	 */
	public int getResourceCount() throws XMLDBException {
		return resourceMap.size();
	}

	/* (non-Javadoc)
	 * @see org.xmldb.api.base.Collection#getService(java.lang.String, java.lang.String)
	 */
	public Service getService(String arg0, String arg1) throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.xmldb.api.base.Collection#getServices()
	 */
	public Service[] getServices() throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.xmldb.api.base.Collection#isOpen()
	 */
	public boolean isOpen() throws XMLDBException {
		// TODO Auto-generated method stub
		return open;
	}

	/* (non-Javadoc)
	 * @see org.xmldb.api.base.Collection#listChildCollections()
	 */
	public String[] listChildCollections() throws XMLDBException {
		Set<String> keySet = collectionMap.keySet();
		String[] al = new String[keySet.size()] ;
		int i = 0;
		for (String key : keySet) {
			al[i] = key;
		}
		return al;
		//al.addAll(keySet);
		//return (String[]) al.toArray();
		//return (String[]) collectionMap.keySet().toArray();
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmldb.api.base.Collection#listResources()
	 */
	public String[] listResources() throws XMLDBException {
		// String[] res = {};
		// String[] ret = resourceMap.keySet().toArray(res);

		String[] ret = new String[resourceMap.size()];
		Iterator<String> keyIterator = resourceMap.keySet().iterator();
		int i = 0;
		while (keyIterator.hasNext()) {
			ret[i] = (String) keyIterator.next();
			i++;
		}

		System.out.println("MockupXMLDBCollection.listResources(MMMMMMMM) "
				+ ArrayUtils.toString(ret));

		return ret;
	}

	/* (non-Javadoc)
	 * @see org.xmldb.api.base.Collection#removeResource(org.xmldb.api.base.Resource)
	 */
	public void removeResource(Resource res) throws XMLDBException {
		System.out.println("MockupXMLDBCollection.removeResource("+res.getId()+")");
		resourceMap.remove(res.getId());
	}

	/* (non-Javadoc)
	 * @see org.xmldb.api.base.Collection#storeResource(org.xmldb.api.base.Resource)
	 */
	public void storeResource(Resource res) throws XMLDBException {
		System.out.println("MockupXMLDBCollection.storeResource("+res.getId()+")");
		resourceMap.put(res.getId(), res);
	}

	/* (non-Javadoc)
	 * @see org.xmldb.api.base.Configurable#getProperty(java.lang.String)
	 */
	public String getProperty(String arg0) throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.xmldb.api.base.Configurable#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty(String arg0, String arg1) throws XMLDBException {
		// TODO Auto-generated method stub

	}
	
	// default visibility (package private)
	Collection createChildCollection(String name){
		MockupXMLDBCollection mockupXMLDBCollection = new MockupXMLDBCollection(name, this);
		collectionMap.put(name, mockupXMLDBCollection);
		return mockupXMLDBCollection;
	}

	// default visibility (package private)
	void deleteCollection(String collectionName) {
		collectionMap.remove(collectionName);
	}
	
	

}
