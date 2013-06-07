package se.liu.imt.mi.eee.bm;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.ext.wadl.ApplicationInfo;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.WadlApplication;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;

import se.liu.imt.mi.eee.bm.res.BookmarkInfo;
import se.liu.imt.mi.eee.bm.res.BookmarkListOfUser;
import se.liu.imt.mi.eee.bm.res.BookmarkPrefixedRoot;
import se.liu.imt.mi.eee.bm.res.BookmarkQR;
import se.liu.imt.mi.eee.bm.res.BookmarkResolveOrEdit;
import se.liu.imt.mi.eee.ehr.res.CurrentDatabaseTimeResource;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.utils.InfoRestlet;
import se.liu.imt.mi.eee.utils.TraceResource;

public class BookmarkRouter extends WadlApplication {

	protected String systemID;

	public BookmarkRouter(Context context, String systemID) {
		super(context);
		this.systemID = systemID;
		this.setName("EEE Bookmark service");
	}

	@Override
	public ApplicationInfo getApplicationInfo(Request request, Response response) {
		ApplicationInfo result = super.getApplicationInfo(request, response);
		DocumentationInfo docInfo = new DocumentationInfo(
				"This service can bookmark links to both EHR " +
				"overviews (including focus points within them) and to specific EHR content items. " +
				"The service addresses privacy concerns and handles warnings when " +
				"sharing and retrieving bookmarked EHR content. The service can be used for " +
				"sending links between different presentation devices, groups and users, including patients. " +
				"A bookmark targets a point in the live EHR and does not constitute a static screenshot. " +
				"EHR content and access restrictions may thus change between the moment of " +
				"bookmark creation and bookmark retrieval");
		//docInfo.setTitle("EEE Bookmark service");
		result.setDocumentation(docInfo);
		return result;
	}

	@Override
	public Restlet createInboundRoot() {

		// Create routers
		Router bookmarkListRouter = new Router(getContext());			
		//Router identifiedBookmarkRouter = new Router(getContext());			

		bookmarkListRouter.getContext().getAttributes().put(EEEConstants.SYSTEM_ID, systemID);	
		bookmarkListRouter.setDefaultMatchingMode(Template.MODE_STARTS_WITH);
		
		Router prefixedRouter = new Router(bookmarkListRouter.getContext());			
		prefixedRouter.setDefaultMatchingMode(Template.MODE_STARTS_WITH);

		TemplateRoute route;
		InfoRestlet bmRootPage = new InfoRestlet("BookmarkRoot", getContext());
		route = bookmarkListRouter.attach("/", bmRootPage);
		route.getTemplate().setMatchingMode(Template.MODE_EQUALS);

		route = bookmarkListRouter.attach("/currentTime", CurrentDatabaseTimeResource.class);
		route.getTemplate().setMatchingMode(Template.MODE_EQUALS);

		route = bookmarkListRouter.attach("/trace", TraceResource.class); 
		route.getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);

		// List bookmarks for specific user
		route = bookmarkListRouter.attach("/u/{"+EEEConstants.USER_ID+"}/", BookmarkListOfUser.class); // Take POST too?			
		route.getTemplate().setMatchingMode(Template.MODE_EQUALS);
		
//		// List tags used by this user (useful e.g. for ajax tag completions)
//		route = bookmarkListRouter.attach("/u/{"+EEEConstants.USER_ID+"}/tags", UserBookmarkTagListResource.class);			
//		route.getTemplate().setMatchingMode(Template.MODE_EQUALS);

		route = bookmarkListRouter.attach("/u/{"+EEEConstants.USER_ID+"}/trace", TraceResource.class);			
		route.getTemplate().setMatchingMode(Template.MODE_EQUALS);
		
		bookmarkListRouter.attach("/{"+EEEConstants.PREFIX+"}", prefixedRouter, Template.MODE_STARTS_WITH);
		
		prefixedRouter.attach("/", BookmarkPrefixedRoot.class, Template.MODE_EQUALS); // For POSTing new bookmarks etc			
		prefixedRouter.attach("/{"+EEEConstants.BOOKMARK_ID+"}/trace", TraceResource.class, Template.MODE_STARTS_WITH);			
		prefixedRouter.attach("/{"+EEEConstants.BOOKMARK_ID+"}/info/", BookmarkInfo.class, Template.MODE_EQUALS);			
		prefixedRouter.attach("/{"+EEEConstants.BOOKMARK_ID+"}/qr/", BookmarkQR.class, Template.MODE_STARTS_WITH);			
		prefixedRouter.attach("/{"+EEEConstants.BOOKMARK_ID+"}/", BookmarkResolveOrEdit.class, Template.MODE_EQUALS);			

		/* optional later implementation...
		route = bookmarkListRouter.attach("/ehr/{"+EEEConstants.EHR_ID+"}", BookmarkListResource.class);
		route.getTemplate().setMatchingMode(Template.MODE_EQUALS);

		route = bookmarkListRouter.attach("/ehr/{"+EEEConstants.EHR_ID+"}/", BookmarkListResource.class);			
		route.getTemplate().setMatchingMode(Template.MODE_EQUALS);
		 */

		// TODO: check for /{"+EEEConstants.BOOKMARK_ID+"} (without trailing "/" slash) ?

		return bookmarkListRouter;
	}

}
