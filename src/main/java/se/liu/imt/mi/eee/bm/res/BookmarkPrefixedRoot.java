package se.liu.imt.mi.eee.bm.res;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;

import se.liu.imt.mi.eee.bm.Bookmark;
import se.liu.imt.mi.eee.bm.BookmarkStorageInterface;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.utils.FreemarkerSupportResource;

/**
 * Creates new bookmarks from POSTed forms or shows form via GET
 * TODO: This class does not yet make use of the prefix (e.g. test in /bm/test/...) 
 *  
 * @author erisu
 *
 */
public class BookmarkPrefixedRoot extends FreemarkerSupportResource implements EEEConstants{
	
	protected BookmarkStorageInterface bookmarkStorage;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		// Extract strings from request
		bookmarkStorage = (BookmarkStorageInterface) getContext().getAttributes().get(
				EEEConstants.KEY_TO_BOOKMARK_STORAGE);

		//this.setAutoDescribing(true);
		//this.setName("TEST EEE");
	}
	
	/**
	 * Creating bookmark from form
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

		
		try {
			String uri = decodedForm.getFirstValue(URI);
			// TODO: add extra checks here (see TODO.txt)
			if (uri == null || uri.equalsIgnoreCase("")) throw new ResourceException(
					Status.CLIENT_ERROR_BAD_REQUEST, 
					"A URI must be supplied in order to create a bookmark");
			
			String title = decodedForm.getFirstValue(TITLE);
			if (title == null || title.trim().equalsIgnoreCase("")) throw new ResourceException(
					Status.CLIENT_ERROR_BAD_REQUEST, 
					"A title (not only containing whitespace) must be supplied in order to create a bookmark");
			title = title.trim();
			
			// TODO: remove default empty string, allow null instead
			String tags = decodedForm.getFirstValue(TAGS);
			if (tags != null) tags = tags.trim();
			
			GregorianCalendar gc = new GregorianCalendar();
			DatatypeFactory dtf = DatatypeFactory.newInstance();
			XMLGregorianCalendar xgc = dtf.newXMLGregorianCalendar(gc);
			String currentDateTime = xgc.toXMLFormat();
			
			// TODO: Add more detailed user handling
			User user = getRequest().getClientInfo().getUser();;
			String committer = user.getName();
			
			Bookmark bookmark = new Bookmark( uri, //uri
					null, // id - the database will assign bookmark id upon creation
					committer, // committer
					currentDateTime, //dt (datetime) 
				    title, tags);
			String bookmarkId = bookmarkStorage.createAndStoreBookmark(committer, bookmark);
			// Override form with value from context if available

			// TODO: Consider if "201 Created" should be used instead of "301 See other" - maybe not since we redirect to /info
			//   getResponse().setLocationRef(bookmarkId + "/info/");
			//   getResponse().setStatus(Status.SUCCESS_CREATED, "Successful creation of bookmark with ID: "+ bookmarkId );

			getResponse().redirectSeeOther(bookmarkId + "/info/");
			// FIXME: According to HTTP spec "the response SHOULD contain a short hypertext note with a hyperlink to the new URI(s)."
			return new StringRepresentation("Successful creation of EHR with ID: "
					+ bookmarkId + " for URI "+bookmark.getUri(), MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			// Do a debug logging...
			getContext().getLogger().throwing(this.getClass().getCanonicalName(), "handleFormPost", e);
			// ...then throw the exception again
			throw e;
		}
	}
	
	@Get("html")
	public Representation handleGetHTML() throws Exception {
		Map<String, Object> variablesForTemplate = new HashMap<String, Object>();
		return handleResponseViaFreemarkerTemplate("html", MediaType.TEXT_HTML, variablesForTemplate);
	}
	
}
