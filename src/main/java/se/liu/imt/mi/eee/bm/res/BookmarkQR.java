package se.liu.imt.mi.eee.bm.res;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import se.liu.imt.mi.eee.bm.Bookmark;
import se.liu.imt.mi.eee.bm.BookmarkStorageInterface;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.utils.FreemarkerSupportResource;

public class BookmarkQR extends FreemarkerSupportResource implements EEEConstants{
	
	protected String bookmarkID;
	protected String command;
	protected BookmarkStorageInterface bookmarkStorage;
	protected Bookmark bookmark;
	
	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		
		// TODO: Change to internal zXing QR generator
		
		// Extract strings from request
		bookmarkStorage = (BookmarkStorageInterface) getContext().getAttributes().get(
				EEEConstants.KEY_TO_BOOKMARK_STORAGE);
		bookmarkID = (String) getRequest().getAttributes().get(EEEConstants.BOOKMARK_ID);

		if (bookmarkID == null) throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, 
			"No bookmark id provided. Is your routing configuration really correct?");

		bookmark = bookmarkStorage.getBookmark(bookmarkID);		
		if (bookmark == null) throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, 
				"Could not find any bookmark with ID "+bookmarkID);
	}
		
	@Get("html")
	public Representation handleGetHTML() throws Exception {
			getResponse().redirectPermanent("http://chart.apis.google.com/chart?cht=qr&chs=200x200&choe=UTF-8&chld=Q&chl="+getReference().getParentRef()); 
			return new StringRepresentation("This would create a QR-code for Bookmark ID: "
					+ bookmarkID + " encoding it's stored target URI: "+bookmark.getUri(), MediaType.TEXT_PLAIN);

	}

}
