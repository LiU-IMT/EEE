package se.liu.imt.mi.eee.ehr.res;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.w3c.dom.Document;

public class StoredQueryInfo extends StoredQuery {
	
	@Get("json")
	public Representation serveJSON() throws Exception {	
		return new JsonRepresentation(storedQuery.toJson());
	}
	
	@Get("xml")
	public Representation serveXML() throws Exception {	
		DomRepresentation dr = new DomRepresentation();
		dr.setDocument((Document) storedQuery.toXML());
		return dr;
	}
	
	
}
