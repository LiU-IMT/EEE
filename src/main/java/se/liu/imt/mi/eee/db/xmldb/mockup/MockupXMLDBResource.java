package se.liu.imt.mi.eee.db.xmldb.mockup;

import org.apache.commons.lang.NotImplementedException;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.BinaryResource;
import org.xmldb.api.modules.XMLResource;

public class MockupXMLDBResource implements Resource, XMLResource, BinaryResource {

	String id;
	Object content = null;
	String type;
	Collection parent;
	//boolean contentIsDom = false;
		
	public MockupXMLDBResource(String id, String type, Collection parent) {
		super();
		this.id = id;
		this.type = type;
		this.parent = parent;
	}
	
	public MockupXMLDBResource(String id, Object content, String type, Collection parent) {
		this(id, type, parent);
		this.content = content;
	}

	public Object getContent() throws XMLDBException {
		return content;
	}

	public String getId() throws XMLDBException {
		return id;
	}

	public Collection getParentCollection() throws XMLDBException {
		return parent;
	}

	public String getResourceType() throws XMLDBException {
		return type;
	}

	public void setContent(Object cont) throws XMLDBException {
		this.content = cont;
	}

	public Node getContentAsDOM() throws XMLDBException {
		try {
			return (Node) content;
		} catch (ClassCastException e) {
			throw new XMLDBException();
		}
	}

	public void getContentAsSAX(ContentHandler arg0) throws XMLDBException {
		throw new NotImplementedException();
	}

	public String getDocumentId() throws XMLDBException {
		throw new NotImplementedException();
	}

	public boolean getSAXFeature(String arg0) throws SAXNotRecognizedException,
			SAXNotSupportedException {
		throw new NotImplementedException();
	}

	public void setContentAsDOM(Node domNode) throws XMLDBException {
		content = domNode;
		//System.out.println("MockupXMLDBResource.setContentAsDOM( >>>>> )"+domNode.toString());
		//contentIsDom = true;
	}

	public ContentHandler setContentAsSAX() throws XMLDBException {
		throw new NotImplementedException();
	}

	public void setSAXFeature(String arg0, boolean arg1)
			throws SAXNotRecognizedException, SAXNotSupportedException {
		throw new NotImplementedException();
	}

}
