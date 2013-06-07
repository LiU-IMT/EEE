package se.liu.imt.mi.eee.db;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeMap;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Node;

public class QueryContainer extends TreeMap<String, String> implements Serializable{
	
	/**
	 * serialVersionUID should be incremented when breaking changes are introduced
	 */
	private static final long serialVersionUID = 1L;
	
	protected String created;
	/**
	 * URI uniquely identifying query language and version
	 */
	protected String queryLanguageID;
	
	/**
	 * 
	 * @param queryID
	 * @param creator
	 * @param staticQueryParametersAsForm
	 * @param created Creation time as ISO8601 datetime string on a format like CCYY-MM-DDThh:mm:ss+HH:MM, e.g. 2012-02-15T15:04:43.334+01:00
	 * @param queryLanguageID 
	 */
	public QueryContainer(String queryID, String creator,
			Form staticQueryParametersAsForm, String created, String queryLanguageID) {
		super();
		
		// Protect against underscore prefix:
		Set<String> names = staticQueryParametersAsForm.getNames();
		for (String name : names) {
			if (name.startsWith("_")) throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "variable names starting with _ (underscore) are not allowed as inout from forms - they are reserved for query metadata");
		}
		
		// ...then start storing...
		putAll(staticQueryParametersAsForm.getValuesMap());
		put("_queryID", queryID);
		put("_creator", creator);
		put("_created", created);
		put("_queryLanguageID", queryLanguageID);
	}
	
	public QueryContainer(JSONObject jo) throws JSONException {
		if (jo.get("_queryID") == null || jo.getString("_queryID").length() < 1) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "the mandatory parameter _queryID is missing");
		} 
		// TODO:possibly add checks for other mandatory metadata variables too...
		
		for (String name : JSONObject.getNames(jo)) {
			put(name, jo.getString(name));
		}
	}

	public QueryContainer(Node node) throws JSONException, XmlException {
		this(XML.toJSONObject(XmlObject.Factory.parse(node).xmlText()).getJSONObject("QueryContainer"));
	}

    public JSONObject toJson(){
    	return new JSONObject(this);
    }
    
    /**
     * @param removeMetaData if true then the metadata properties (queryID, creator etc) will not be included in the returned form
     */
    public Form toForm(Boolean removeMetaData) {
    	Form qForm = new Form();
    	Set<String> qKeys = this.keySet();
    	for (String key : qKeys) {
    		if(removeMetaData && key.startsWith("_")) {
    			//System.out.println("QueryContainer.toForm() skipping field "+key+" = "+this.get(key)); // TODO: comment out
    		} else {
    			qForm.add(key, this.get(key));
			}			
		}
    	return qForm;
    }
    
    public Node toXML() { // TODO: Consider if static parameters also should be returned as XML
		Node node;
		try {
			String queryAsXML = XML.toString(this.toJson(), "QueryContainer");
			XmlObject xo  = XmlObject.Factory.parse(queryAsXML);
			node = xo.getDomNode();
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		return node;
	}

	public String getQueryID() {
		return get("_queryID");
	}

	public void setQueryID(String queryID) {
		put("_queryID", queryID);
	}

	public String getCreator() {
		return get("_creator");
	}

	public void setCreator(String creator) {
		put("_creator", creator);
	}

	public String getCreated() {
		return get("_created");
	}

	public void setCreated(String created) {
		put("_created", created);
	}

	public String getQueryLanguageID() {
		return get("_queryLanguageID");
	}

	public void setQueryLanguageID(String queryLanguageID) {
		put("_queryLanguageID", queryLanguageID);
	}
	
	// TODO: Possibly add versioning/history pointing back to previous SHA keyed queries
	
}
