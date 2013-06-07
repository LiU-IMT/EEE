package se.liu.imt.mi.eee.bm;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Bookmark implements Serializable {

	/**
	 * serialVersionUID should be incremented when breaking changes are
	 * introduced
	 */
	private static final long serialVersionUID = 1L;

	String uri;

	String id = null;

	String committer;

	/**
	 * Creation (or later update) time as ISO8601 datetime string on a format
	 * like CCYY-MM-DDThh:mm:ssZ
	 */
	String dt;

	/**
	 * Bookmarks can be deactivated but are still persisted for audit-purposes.
	 * A deactivated bookmark should not be resolved (a http '403 Forbidden'
	 * response is suitable when active==false in http based applications)
	 */
	boolean active = true;

	/**
	 * A title or description used for human readability in lists etc. Usually
	 * the derived from the <TITLE> tag in a bookmarked html page
	 */
	String title;

	/**
	 * Optional tags
	 */
	String tags;

	public Bookmark(String uri, String id, String committer, String dt,
			String title, String tags) {
		super();
		this.uri = uri;
		this.id = id;
		this.committer = committer;
		this.dt = dt;
		this.title = title;
		this.tags = tags;
	}

	public Bookmark(JSONObject jo) throws JSONException {
		this(
			jo.getString("uri"), 
			jo.getString("id"), 
			jo.getString("committer"),
			jo.getString("dt"),
			jo.getString("title"),
			jo.getString("tags")
		);
	}

	/**
	 * Constructor with DOM Node object.
	 * 
	 * @param node
	 *            - Is assumed to be a Document node, ie the node itself won't
	 *            contain information but we have to search the child nodes.
	 */
	public Bookmark(Node node) {
		super();
		Node child = node.getFirstChild();// bookmark
		HashMap<String, String> map = new HashMap<String, String>();
		NodeList list = child.getChildNodes();
		for (int i = list.getLength() - 1; i >= 0; i--) {
			String name = list.item(i).getNodeName();
			String value = list.item(i).getTextContent();
			map.put(name, value);
		}

		this.uri = map.get("uri");
		this.id = map.get("id");
		this.committer = map.get("committer");
		this.dt = map.get("dt");
		this.title = map.get("title");
		this.tags = map.get("tags");
	}

	public JSONObject toJson() {
		return new JSONObject(this);
	}

	public Node toXML() {
		Node node;
		try {

			String bookmarkAsXML = XML.toString(this.toJson(), "bookmark");
			node = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new ByteArrayInputStream(bookmarkAsXML.getBytes()))
					.getDocumentElement();
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		return node;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCommitter() {
		return committer;
	}

	public void setCommitter(String committer) {
		this.committer = committer;
	}

	public String getDt() {
		return dt;
	}

	public void setDt(String dt) {
		this.dt = dt;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	// TODO: Possibly add versioning/history

}
