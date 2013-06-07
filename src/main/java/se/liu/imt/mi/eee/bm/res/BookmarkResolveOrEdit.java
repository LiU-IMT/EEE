package se.liu.imt.mi.eee.bm.res;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import se.liu.imt.mi.eee.bm.Bookmark;
import se.liu.imt.mi.eee.bm.BookmarkStorageInterface;
import se.liu.imt.mi.eee.structure.EEEConstants;

public class BookmarkResolveOrEdit extends ServerResource implements EEEConstants{
	
	protected String bookmarkID;
	protected BookmarkStorageInterface bookmarkStorage;
	protected String prefix;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		// Extract strings from request
		bookmarkStorage = (BookmarkStorageInterface) getContext().getAttributes().get(
				EEEConstants.KEY_TO_BOOKMARK_STORAGE);
		bookmarkID = (String) getRequest().getAttributes().get(EEEConstants.BOOKMARK_ID);
		prefix = (String) getRequest().getAttributes().get(EEEConstants.PREFIX);

		//this.setAutoDescribing(true);
		//this.setName("TEST EEE");
		//System.out.println("FreemarkerSupportResource.doInit() -> " + this.isAutoDescribing());
	}
	
	@Get
	public Representation handleGetHTML() throws Exception {

		// Fetch existing bookmark
		Bookmark bookmark = bookmarkStorage.getBookmark(bookmarkID);
		if (bookmark == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation(bookmarkID + " is an unknown bookmark ID", MediaType.TEXT_PLAIN);
		}	
		if (!bookmark.isActive()) {
			String message = "The bookmark with ID: "
				+ bookmarkID + " has been deactivated by it's creator or by administrators and can no longer be resolved";
			getResponse().setStatus(Status.CLIENT_ERROR_GONE, message);
			return new StringRepresentation(message, MediaType.TEXT_PLAIN);
		}
		
		getResponse().redirectSeeOther(bookmark.getUri()); // FIXME: remove stringrepr
		return new StringRepresentation("This would resolve Bookmark ID: "
				+ bookmarkID + " to it's stored target URI: "+bookmark.getUri(), MediaType.TEXT_PLAIN);
	}


	
//	if (bookmarkID != null) throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED, 
//			"POST to identified bookmark not supported yet (might be allowed later to " +
//		    "change tags etc)");
	
	/**
	 * Updates title and tag fields of a given bookmark
	 * Takes care of url-encoded forms sent with POST using the
	 * application/x-www-form-urlencoded mediatype, e.g. forms from HTML like
	 * <FORM METHOD=POST ENCTYPE="application/x-www-form-urlencoded" ...
	 * 
	 * @param incomingPostedRepresentation
	 * @return
	 * @throws Exception
	 */
	@Post("form")
	public Representation handleFormPost(Representation incomingPostedRepresentation) throws Exception {
		Form decodedForm = new Form(getRequestEntity());
		
		// TODO: Refactor duplicated cut&paste-code from handleGetHTML(...)
		// Fetch existing bookmark 	
		Bookmark bookmark = bookmarkStorage.getBookmark(bookmarkID);		
		if (bookmark == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation(bookmarkID + " is an unknown bookmark ID", MediaType.TEXT_PLAIN);
		}	
		if (!bookmark.isActive()) {
			String message = "The bookmark with ID: "
				+ bookmarkID + " has been deactivated by it's creator or by administrators and can no longer be resolved";
			getResponse().setStatus(Status.CLIENT_ERROR_GONE, message);
			return new StringRepresentation(message, MediaType.TEXT_PLAIN);
		}
		
		// Check that the current user is the original creator of the bookmark.
		String username = getRequest().getClientInfo().getUser().getName();
		if (!bookmark.getCommitter().equalsIgnoreCase(username)) {
			getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			return new StringRepresentation("This bookmark can only be edited by it's creator, not by you", MediaType.TEXT_PLAIN);
		}	
		
		String uri = decodedForm.getFirstValue(URI);
		if (uri != null && !uri.equalsIgnoreCase("") && uri.equalsIgnoreCase("test")) { // FIXME: refactor to configurable list of prefixes
			// URI was given, and if we are in a editable prefix then allow uri change
			bookmark.setUri(uri);
		}
		
		String title = decodedForm.getFirstValue(TITLE);
		if (title == null || title.trim().equalsIgnoreCase("")) throw new ResourceException(
				Status.CLIENT_ERROR_BAD_REQUEST, 
				"A title (not only containing whitespace) must be supplied in order to create a bookmark");
		bookmark.setTitle(title.trim());
		
		String tags = decodedForm.getFirstValue(TAGS, "");
		bookmark.setTags(tags.trim());
		
//		GregorianCalendar gc = new GregorianCalendar();
//		DatatypeFactory dtf = DatatypeFactory.newInstance();
//		XMLGregorianCalendar xgc = dtf.newXMLGregorianCalendar(gc);
//		String currentDateTime = xgc.toXMLFormat();
//		System.out.println("BookmarkResource.handleFormPost() -- "+currentDateTime);
		
// TODO: Possibly update to modification date instead of creation date, or record two dates.
		
		bookmarkStorage.updateBookmark(username, bookmarkID, bookmark);

		return new StringRepresentation("Successful update of bookmark with ID: "
				+ bookmarkID, MediaType.TEXT_PLAIN);
	}


}
