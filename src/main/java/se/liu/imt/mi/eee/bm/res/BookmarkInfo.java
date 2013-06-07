package se.liu.imt.mi.eee.bm.res;

import java.util.HashMap;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;

import se.liu.imt.mi.eee.bm.Bookmark;
import se.liu.imt.mi.eee.bm.BookmarkStorageInterface;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.utils.FreemarkerSupportResource;

public class BookmarkInfo extends FreemarkerSupportResource implements EEEConstants{
	
	protected String bookmarkID;
	protected String command;
	protected BookmarkStorageInterface bookmarkStorage;
	protected Bookmark bookmark;
	
	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		// Extract strings from request
		bookmarkStorage = (BookmarkStorageInterface) getContext().getAttributes().get(
				EEEConstants.KEY_TO_BOOKMARK_STORAGE);
		bookmarkID = (String) getRequest().getAttributes().get(EEEConstants.BOOKMARK_ID);
//		command = (String) getRequest().getAttributes().get(EEEConstants.COMMAND);

		if (bookmarkID == null) throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, 
			"No bookmark id provided. Is your routing configuration really correct?");

		bookmark = bookmarkStorage.getBookmark(bookmarkID);		
		if (bookmark == null) throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, 
				"Could not find any bookmark with ID "+bookmarkID);
	}
	
	/**
	 * Takes care of url-encoded forms sent with POST using the
	 * application/x-www-form-urlencoded mediatype, e.g. forms from HTML like
	 * <FORM METHOD=POST ENCTYPE="application/x-www-form-urlencoded" ...
	 * 
	 * @param incomingPostedRepresentation
	 * @return
	 * @throws Exception
	 */
//	@Post("form")
//	public Representation handleFormPost(Representation incomingPostedRepresentation) throws Exception {
//		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED, 
//				"POST to identified bookmark not supported yet (might be allowed later to " +
//			    "change tags etc)");
//	}
	
	@Get("html")
	public Representation handleGetHTML() throws Exception {
		Map<String, Object> variablesForTemplate = new HashMap<String, Object>();
		variablesForTemplate.put("bookmark", bookmark);
		variablesForTemplate.put("bookmarkID", bookmarkID);
		variablesForTemplate.put("bookmarkURI", getReference().getParentRef());
		return handleResponseViaFreemarkerTemplate("html", MediaType.TEXT_HTML, variablesForTemplate);
	}

	
//	@Get("json")
//	public Representation handleGetJSON() {
//			return new JsonRepresentation(bookmark.toJson());
//	}
	
	// Testing xstream auto-stuff
	@Get("json") 
	public Bookmark retrieve() {
			return bookmark;
	}

	@Get("xml")
	public Representation handleGetXML() {
			return new DomRepresentation(MediaType.APPLICATION_ALL_XML, (Document) bookmark.toXML());
	}
	
//	@Get("xml")
//	public Representation handleGetXML() {
//			return new JaxbRepresentation<T>(MediaType.APPLICATION_ALL_XML, (Document) bookmark.toXML());
//	}

}
