package se.liu.imt.mi.eee.cb;


import org.openehr.binding.XMLBindingException;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.ext.wadl.WadlApplication;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Router;
import org.restlet.routing.Template;

import se.liu.imt.mi.eee.cb.res.ContributionBuilderCommit;
import se.liu.imt.mi.eee.cb.res.ContributionBuilderInitiatorResource;
import se.liu.imt.mi.eee.cb.res.ContributionBuilderListResource;
import se.liu.imt.mi.eee.cb.res.ContributionBuilderObjectDataResource;
import se.liu.imt.mi.eee.cb.res.ContributionBuilderObjectResource;
import se.liu.imt.mi.eee.cb.res.ContributionBuilderValidate;
import se.liu.imt.mi.eee.db.xmldb.ContributionBuilderStorageInXMLDB;
import se.liu.imt.mi.eee.db.xmldb.XMLDBHelper;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.utils.TraceResource;
import se.liu.imt.mi.eee.utils.Util;

// TODO: possibly move ContributionBuilderListResource into contributionBuilderRouter as inner class?
/**
 * <pre>
 * ********* CREATING/POPULATING NEW CONTRIBUTIONS *********
 * 
 * Demand creation of a new contribution ID
 * *  /cb/{composerId}/{ehrId}/generateContributionBuildID --- POST
 * Example: http://localhost:8182/cb/dr_who/1234567/GenerateContributionBuildID
 * (Should return the URI to the contribution to write
 *  to something like: 
 *  /cb/{composerId}/{ehrId}/{contributionId}
 * TODO: possibly return (or redirect to) created URI
 *  
 * Add content to the contributionBuilder:
 *   
 * --- /cb/{composerId}/{ehrId} --- 
 *   GET (lists contrib IDs)
 * 
 * --- /cb/{composerId}/{ehrId}/{contributionId} --- 
 *   GET (lists versioned object IDs)
 * 
 * --- /cb/{composerId}/{ehrId}/{contributionId}/{tempObjectId} --- 
 *    POST/PUT adds versioned objects to the ongoing contribution
 *    GET retrieves versioned objects from the ongoing contribution
 * 
 * --- /cb/{composerId}/{ehrId}/{contributionId}/commit ---
 *    POST call must include data needed to create AUDIT_DETAILS 
 *    TODO: Check change_type constraints (e.g. if deleted objects have data=null)
 
 * --- /cb/{composerId}/{ehrId}/{contributionId}/load-prototype/;change_type=creation;lifecycle_state=incomplete};?prototype-uri={prototype-uri}  ---
 * possibly run freemarker substitution for start-time {current-time}, composer {current-user} 
 * TODO: Return (or redirect to) created URI
 * 
 * --- /cb/{composerId}/{ehrId}/{contributionId}/load/56780007::ehr.us.lio.se::2;change_type=creation;lifecycle_state=incomplete}; // change_type=creation not allowed for load
 * --- /cb/{composerId}/{ehrId}/{contributionId}/load/56780007::ehr.us.lio.se::2;change_type=deleted;lifecycle_state=deleted}; // change_type=creation not allowed for load
 * --- /cb/{composerId}/{ehrId}/{contributionId}/commit ---
 * </pre>
 */
public final class ContributionBuilderRouter extends WadlApplication {
		protected String systemID;
		protected XMLDBHelper dbHelper;
		protected Object contrBuilderDBHandler;
	
		public ContributionBuilderRouter(Context context, String systemID, ContributionBuilderStorageInXMLDB contrBuilderDBHandler) throws XMLBindingException, Exception {
			super(context);
			this.systemID = systemID;
			this.contrBuilderDBHandler = contrBuilderDBHandler;
			this.setName("EEE Contribution Builder");
			this.setDescription("The Contribution Builder is a temporary writing space where the contents of " +
					"a new openEHR CONTRIBUTION can be built up step by step and edited before committing it.");	
		}	
		
		@Override
		public Restlet createInboundRoot() {
			
			// Create root router
			Router rootRouter = new Router(getContext());		
			rootRouter.getContext().getAttributes().put(EEEConstants.KEY_TO_CONTRIBUTION_BUILDER_DB_INSTANCE, contrBuilderDBHandler);	
			rootRouter.getContext().getAttributes().put(EEEConstants.SYSTEM_ID, systemID);	
			try {
				rootRouter.getContext().getAttributes().put(EEEConstants.KEY_TO_XML_BINDING, Util.setUpXMLBinding("sv"));
			} catch (Exception e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "ContributionBuilderRouter.createInboundRoot() failed setUpXMLBinding", e);
			}	
			
			Router contributionBuilderRouter = new Router(rootRouter.getContext());			
			contributionBuilderRouter.setDefaultMatchingMode(Template.MODE_EQUALS);

			rootRouter.attach("/trace", TraceResource.class, Template.MODE_STARTS_WITH);
			// TODO: think more about redirection etc
			//rootRouter.attach("/", new Redirector(rootRouter.getContext(),"/cb/info/",Redirector.MODE_CLIENT_SEE_OTHER), Template.MODE_EQUALS);
			rootRouter.attach("/", ContributionBuilderListResource.class, Template.MODE_EQUALS);

			rootRouter.attach("/{"+EEEConstants.COMMITTER_ID+"}/", ContributionBuilderListResource.class, Template.MODE_EQUALS);
			rootRouter.attach("/{"+EEEConstants.COMMITTER_ID+"}/{"+EEEConstants.EHR_ID+"}", contributionBuilderRouter, Template.MODE_STARTS_WITH);						

			
			// **** routes below used when both COMMITTER_ID and EHR_ID are known 				
			// contributionBuilderRouter.setDefaultMatchingQuery(false);

			contributionBuilderRouter.attach("/", ContributionBuilderListResource.class);			
			contributionBuilderRouter.attach("/trace", TraceResource.class, Template.MODE_STARTS_WITH); 
					
			contributionBuilderRouter.attach("/new-cb-id/", ContributionBuilderInitiatorResource.class); 
								
			contributionBuilderRouter.attach("/{"+EEEConstants.CONTRIBUTION_BUILD_ID+"}/", 
					ContributionBuilderListResource.class, Template.MODE_EQUALS); 
			
			contributionBuilderRouter.attach("{"+EEEConstants.CONTRIBUTION_BUILD_ID+"}/trace",
					TraceResource.class, Template.MODE_STARTS_WITH); 

			contributionBuilderRouter.attach("/{"+EEEConstants.CONTRIBUTION_BUILD_ID+"}/validate/",
					ContributionBuilderValidate.class);
			
			contributionBuilderRouter.attach("/{"+EEEConstants.CONTRIBUTION_BUILD_ID+"}/commit/", 
					ContributionBuilderCommit.class);
			
			// /new/update-version/ + /new/copy-version/ + /new/from-form/ + /new/from-url/			
			// /new/from-instance-template/ + /new/from-ehr-path/ + /new/from-xpath/			
			// EEEConstants.TEMP_ID provided in form parameter
			contributionBuilderRouter.attach("/{"+EEEConstants.CONTRIBUTION_BUILD_ID+"}/new/{"+EEEConstants.COMMAND+"}/", 
					ContributionBuilderObjectResource.class);

			// Just the "data" part
			contributionBuilderRouter.attach("/{"+EEEConstants.CONTRIBUTION_BUILD_ID+"}/{"+EEEConstants.TEMP_ID+"}/data/", 
					ContributionBuilderObjectDataResource.class, Template.MODE_STARTS_WITH); 

			// The entire "version" including content under /data, /commit_audit and /lifecycle_state if that is desired
			// EEEConstants.TEMP_ID provided in path
			contributionBuilderRouter.attach("/{"+EEEConstants.CONTRIBUTION_BUILD_ID+"}/{"+EEEConstants.TEMP_ID+"}/", 
					ContributionBuilderObjectResource.class, Template.MODE_STARTS_WITH);
			
			
//			routeVars = route.getTemplate().getVariables();
//			routeVars.put(EEEConstants.TEMP_ID, new Variable(Variable.TYPE_URI_UNRESERVED));
			// TODO: Document the URI_UNRESERVED requirement for temp IDs (motivated by not using / and making it easier to access partial sub-content
			// TYPE_URI_UNRESERVED = ALPHA / DIGIT / "-" / "." / "_" / "~"
			// see: http://tools.ietf.org/html/rfc3986
			
//			// or perhaps:
//			routeVars.put(EEEConstants.TEMP_ID, new Variable(Variable.TYPE_WORD));
//			// TYPE_WORD = Matches all alphabetical and digital characters plus the underscore.
//			// see: http://tools.ietf.org/html/rfc3986 (ALPHA / DIGIT / "_")

			// TODO: Add support for creation with shorter arbitrary object IDs like 
			// contributionBuilder/dr_who/1234567/b734db36-30a7-43d6-b9ec-51cb389871b6/new-1;change_type=creation;lifecycle_state=incomplete
			// ...or better: should we force new temp ids to be on the form xx::yy::0 ?
			
			// alternative using matrix URIs as described in http://www.w3.org/DesignIssues/MatrixURIs.html
			// ...56780007::ehr.us.lio.se::2;change_type=creation;lifecycle_state=incomplete
			// The matrix uri parameters will override content of (possibly) submitted content of change_type or lifecycle_state
//			route = contributionBuilderRouter.attach("{"+EHRTestRestStarter.CONTRIBUTION_ID+"}/{"+EHRTestRestStarter.OBJECT_ID+"}::{"+EHRTestRestStarter.CREATING_SYSTEM_ID+"}::{"+EHRTestRestStarter.VERSION_TREE_ID+"}/data;"+EHRTestRestStarter.CHANGE_TYPE+"={"+EHRTestRestStarter.CHANGE_TYPE+"};"+EHRTestRestStarter.LIFECYCLE_STATE+"={"+EHRTestRestStarter.LIFECYCLE_STATE+"}", 
//					ContributionBuilderResource.class); 
//			route.getTemplate().setMatchingMode(Template.MODE_EQUALS);
//
//			// intended for /commit_audit and /lifecycle_state
//			route = contributionBuilderRouter.attach("{"+EHRTestRestStarter.CONTRIBUTION_ID+"}/{"+EHRTestRestStarter.OBJECT_ID+"}::{"+EHRTestRestStarter.CREATING_SYSTEM_ID+"}::{"+EHRTestRestStarter.VERSION_TREE_ID+"}/{"+EHRTestRestStarter.COMMAND+"}", 
//					ContributionBuilderResource.class); 
//			route.getTemplate().setMatchingMode(Template.MODE_EQUALS);
			
			return rootRouter;
		}
	}