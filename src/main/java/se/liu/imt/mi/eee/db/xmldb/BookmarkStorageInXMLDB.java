package se.liu.imt.mi.eee.db.xmldb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.TransactionService;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XQueryService;

import se.liu.imt.mi.eee.bm.Bookmark;
import se.liu.imt.mi.eee.bm.BookmarkStorageInterface;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.utils.Util;

public class BookmarkStorageInXMLDB implements BookmarkStorageInterface {

	private Collection bookmarkRootCollection;
	protected SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss");
	protected XMLDBHelper dbHelper;
	protected XQueryService bookmarkXQueryService;

	public BookmarkStorageInXMLDB(XMLDBHelper dbHelper) throws XMLDBException {
		this.dbHelper = dbHelper;
		bookmarkRootCollection = dbHelper.createChildCollection(
				dbHelper.getRootCollection(), "BookmarkStorage");

		bookmarkXQueryService = dbHelper
				.getXQueryService(bookmarkRootCollection);
		bookmarkXQueryService.setNamespace("v1",
				EEEConstants.SCHEMA_OPENEHR_ORG_V1);
		bookmarkXQueryService.setNamespace("eee",
				EEEConstants.SCHEMA_EEE_OPENEHR_EXTENSION);
		bookmarkXQueryService.setNamespace("xsi", EEEConstants.SCHEMA_XSI);

	}

	public Bookmark getBookmark(String bookmark_id) {
		Bookmark bookmark = null;
		XMLResource n;
		try {
			n = (XMLResource) bookmarkRootCollection.getResource(bookmark_id);
			if (n == null)
				return null;
			bookmark = new Bookmark(n.getContentAsDOM());
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		// TODO: Maybe add public/private check here (and add user parameter to
		// method signature)
		return bookmark;
	}

	public String createAndStoreBookmark(String committer, Bookmark bookmark) {
		if (!committer.equals(bookmark.getCommitter())) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
					"committers must match");
		}
		// One way to produce an ID-string
		String bookmarkId = null;
		TransactionService trans = null;
		try {
			trans = dbHelper.createTransaction(bookmarkRootCollection);
			trans.begin();
			
			// Repeat until unused bookmarkId is found
			do {
				bookmarkId = Util.encodeLongToSafeChars(System
						.currentTimeMillis());
			} while (bookmarkRootCollection.getResource(bookmarkId) != null);

			bookmark.setId(bookmarkId);
			// Set up bookmark
			XMLResource newBookmarkRes = (XMLResource) bookmarkRootCollection
					.createResource(bookmarkId, XMLResource.RESOURCE_TYPE);
			newBookmarkRes.setContentAsDOM(bookmark.toXML());

			// eXist-specific:
			// ((RemoteBinaryResource)newBookmarkRes).setMimeType("application/x-java-bean");
			bookmarkRootCollection.storeResource(newBookmarkRes);
			trans.commit();
		} catch (XMLDBException e) {
			// Roll back on errors
			try {
				trans.rollback();
				// Oh my, the rollback can also throw exceptions!
			} catch (Exception e2) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e2);
			}
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		return bookmarkId;
	}

	public Bookmark deactivateBookmark(String committer, String bookmark_id) {
		// TODO: Push validation up to superclass!
		Bookmark bookmark = getBookmark(bookmark_id);
		if (!committer.equals(bookmark.getCommitter())) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
					"committers must match, only the creator of a bookmark can deactivate it");
		}
		bookmark.setActive(false);
		updateBookmark(committer, bookmark_id, bookmark);
		return bookmark;
	}

	public void updateBookmark(String committer, String bookmarkID,
			Bookmark bookmark) {
		XMLResource bookmarkRes;
		try {
			bookmarkRes = (XMLResource) bookmarkRootCollection
					.getResource(bookmarkID);
			bookmarkRes.setContentAsDOM(bookmark.toXML());
			bookmarkRootCollection.storeResource(bookmarkRes);
		} catch (XMLDBException e) {
			// TODO: improve error description & use status code differentiation
			// depending on root cause
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not update bookmark in database", e);
		}
	}

	public List<Bookmark> listUserBookmarks(String user_id) {
		List<Bookmark> bookmarklist = null;

		String query = "for $bookmark in //bookmark "
				+ "where $bookmark/committer/text()='" + user_id + "' " 
				+ "order by $bookmark/dt/text()	descending "
				+ "return $bookmark ";
		try {
			ResourceIterator rit = bookmarkXQueryService.query(query)
					.getIterator();
			if (rit.hasMoreResources()) {
				// The answer set was not empty, so initiate a list to replace the null
				bookmarklist = new ArrayList<Bookmark>();
			} 
			while (rit.hasMoreResources()) {
				XMLResource r = (XMLResource) rit.nextResource();
				bookmarklist.add(new Bookmark(r.getContentAsDOM()));
			}
		} catch (Exception e) {
			// TODO: improve error description & use status code differentiation
			// depending on root cause
			throw new ResourceException(e);
		}
		return bookmarklist;
	}

}
