package se.liu.imt.mi.eee.ehr;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.ext.wadl.WadlApplication;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;

import se.liu.imt.mi.eee.ehr.res.CurrentDatabaseTimeResource;
import se.liu.imt.mi.eee.ehr.res.EHRRoot;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.utils.TraceResource;
 
public final class EHRRouter extends WadlApplication {
			 
	protected String systemID;

		public EHRRouter(Context context) {
			super(context);
			this.setName("EEE EHR Resource");
			this.setAuthor("Medical informatics group, Department of Biomedical Engineering, Linköping University");
			this.setDescription("LiU EEE is an Educational EHR Environment based on openEHR and REST architecture. " +
					"The EHR Resource is used to retrieve and query EHR content and to handle openEHR CONTRIBUTIONs" +
					" Note that the paths indicated as /ehr/:{ehr_id} should be containing /ehr:{ehr_id} instead (this listing error is produced during WADL-auto-generation).");
		}

		@Override
		public Restlet createInboundRoot() {
			Router ehrRootRouter = new Router(getContext());					
			ehrRootRouter.setDefaultMatchingMode(Template.MODE_STARTS_WITH);
			
			TemplateRoute route;

			route = ehrRootRouter.attach("/", EHRRoot.class);
			route.getTemplate().setMatchingMode(Template.MODE_EQUALS);
			
			route = ehrRootRouter.attach("/currentTime/", CurrentDatabaseTimeResource.class);
			route.getTemplate().setMatchingMode(Template.MODE_EQUALS);

			route = ehrRootRouter.attach("/trace", TraceResource.class); 
			route.getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
	
			return ehrRootRouter;
		}
	}