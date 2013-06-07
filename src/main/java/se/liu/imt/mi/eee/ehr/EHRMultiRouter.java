package se.liu.imt.mi.eee.ehr;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.ext.wadl.WadlApplication;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;

import se.liu.imt.mi.eee.db.xmldb.XMLDBHelper;
import se.liu.imt.mi.eee.ehr.res.CurrentDatabaseTimeResource;
import se.liu.imt.mi.eee.ehr.res.MultiQuery;
import se.liu.imt.mi.eee.ehr.res.Query;
import se.liu.imt.mi.eee.ehr.res.StoredQueryAQL;
import se.liu.imt.mi.eee.ehr.res.StoredQueryInfo;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.utils.InfoRestlet;
import se.liu.imt.mi.eee.utils.TraceResource;
 
public final class EHRMultiRouter extends WadlApplication implements EEEConstants{
			 
	protected XMLDBHelper dbHelper;


		public EHRMultiRouter(Context context, XMLDBHelper dbHelper) {
			super(context);
			this.dbHelper = dbHelper;
			this.setName("EEE Multi EHR Resource");
			this.setAuthor("Medical informatics group, Department of Biomedical Engineering, Linköping University");
			this.setDescription("LiU EEE is an Educational EHR Environment based on openEHR and REST architecture. " +
					"The EHR Resource is used by a user to retrieve and query EHR content for multiple patients at the same time. " +
					"This is intended to be accesible primarily to users that need to do epidemiology queries etc.");
		}

		@Override
		public Restlet createInboundRoot() {			
			Router ehrMultiRootRouter = new Router(getContext());					
			ehrMultiRootRouter.setDefaultMatchingMode(Template.MODE_STARTS_WITH);			
			ehrMultiRootRouter.getContext().getAttributes().put(KEY_TO_XMLDBHELPER_INSTANCE, dbHelper);	
			
			TemplateRoute route;

			InfoRestlet rootInfo = new InfoRestlet(EHRMultiRouter.class.getSimpleName(), getContext());			
			route = ehrMultiRootRouter.attach("/", rootInfo);
			route.getTemplate().setMatchingMode(Template.MODE_EQUALS);
			
			route = ehrMultiRootRouter.attach("/currentTime/", CurrentDatabaseTimeResource.class);
			route.getTemplate().setMatchingMode(Template.MODE_EQUALS);

			route = ehrMultiRootRouter.attach("/trace", TraceResource.class); 
			route.getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
	
			ehrMultiRootRouter.attach("/q/", MultiQuery.class, Template.MODE_EQUALS);			
			ehrMultiRootRouter.attach("/q/{"+QUERY_LANGUAGE+"}/", MultiQuery.class, Template.MODE_EQUALS);
			ehrMultiRootRouter.attach("/q/{"+QUERY_LANGUAGE+"}/{"+QUERY_SHA+"}/info/", StoredQueryInfo.class, Template.MODE_EQUALS);
			ehrMultiRootRouter.attach("/q/AQL/{"+QUERY_SHA+"}/", StoredQueryAQL.class, Template.MODE_EQUALS);

			return ehrMultiRootRouter;
		}
	}