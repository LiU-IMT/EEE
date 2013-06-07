package se.liu.imt.mi.eee.bm.res;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import se.liu.imt.mi.eee.bm.Bookmark;
import se.liu.imt.mi.eee.bm.BookmarkStorageInterface;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.utils.FreemarkerSupportResource;

public class BookmarkListOfUser extends FreemarkerSupportResource {

	protected String userID;
	//protected String command;
	protected BookmarkStorageInterface bookmarkStorage;
	protected List<Bookmark> bookmarkList;
		
	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		// Extract strings from request
		bookmarkStorage = (BookmarkStorageInterface) getContext().getAttributes().get(
				EEEConstants.KEY_TO_BOOKMARK_STORAGE);
		userID = (String) getRequest().getAttributes().get(EEEConstants.USER_ID);
		//bookmarkID = (String) getRequest().getAttributes().get(EEEConstants.BOOKMARK_ID);
		//command = (String) getRequest().getAttributes().get(EEEConstants.COMMAND);
		//System.out.println("UserBookmarkListResource.doInit() userID -> " + userID);
		
		// Fetch existing bookmark
		bookmarkList = bookmarkStorage.listUserBookmarks(userID);
		
		if (bookmarkList == null) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find any bookmarks for user "+ userID);
		}
	}


	@Get("html")
	public Representation handleGetHTML() throws Exception {
		
		Map<String, Object> variablesForTemplate = new HashMap<String, Object>();
		
		variablesForTemplate.put("bookmarkList", bookmarkList);
		return handleResponseViaFreemarkerTemplate("html", MediaType.TEXT_HTML, variablesForTemplate);
		
		// TODO: Call a freemarker template instead...
		// return new StringRepresentation("This will become a nicer list later, # of bm for "+userID+" = "+
		//		+ bookmarkList.size() + " as string: "+bookmarkList.toString(), MediaType.TEXT_PLAIN);
	}
	
	// TODO: add json and XML methods (easy since bookmark suppors it...)
	
	@Get("xml")
	public Representation handleGetXML() {
		Document doc;
		Node root;
		try {
			DocumentBuilder db = DocumentBuilderFactory
				    .newInstance()
				    .newDocumentBuilder();
			 root = db.parse(new ByteArrayInputStream("<bookmark-list></bookmark-list>".getBytes()))
				    .getDocumentElement();
			doc = root.getOwnerDocument();
			/*root = doc.importNode(root, true);
			doc.appendChild(root);*/
		}
		catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		Node newnode;
		for (Bookmark bookmark : bookmarkList) {	
			newnode = doc.importNode(bookmark.toXML(), true);
			root.appendChild(newnode);
			}
		return new DomRepresentation(MediaType.APPLICATION_ALL_XML, doc);
	}
	
}
