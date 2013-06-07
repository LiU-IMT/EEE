/**
 * 
 */
package se.liu.imt.mi.eee.ehr;

import java.util.Map;

import org.openehr.binding.XMLBinding;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.ext.wadl.WadlApplication;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;

import se.liu.imt.mi.eee.DemoStarter;
import se.liu.imt.mi.eee.db.EHRDatabaseReadInterface;
import se.liu.imt.mi.eee.db.EHRDatabaseWriteInterface;
import se.liu.imt.mi.eee.db.xmldb.ContributionBuilderStorageInXMLDB;
import se.liu.imt.mi.eee.db.xmldb.XMLDBHelper;
import se.liu.imt.mi.eee.ehr.res.ContributionLatestResource;
import se.liu.imt.mi.eee.ehr.res.ContributionListingResource;
import se.liu.imt.mi.eee.ehr.res.ContributionResource;
import se.liu.imt.mi.eee.ehr.res.IdentifiedEHR;
import se.liu.imt.mi.eee.ehr.res.Query;
import se.liu.imt.mi.eee.ehr.res.StoredQueryAQL;
import se.liu.imt.mi.eee.ehr.res.StoredQueryInfo;
import se.liu.imt.mi.eee.ehr.res.StoredQueryXQueryAQLHybrid;
import se.liu.imt.mi.eee.ehr.res.VersionedObjectCommandResource;
import se.liu.imt.mi.eee.ehr.res.VersionedObjectResource;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.translators.QueryTranslator;
import se.liu.imt.mi.eee.utils.InfoRestlet;
import se.liu.imt.mi.eee.utils.NotImplementedResource;
import se.liu.imt.mi.eee.utils.TraceResource;
/**
 * <pre>
 * ******** READING EXISTING EHR CONTENT *********
 * 
 * TODO: Add routes with ehr: (colon sign) like http://localhost:8182/ehr:1234000/56780007@latest_trunk_version 
 * (in order to easily match openEHR EHR-URIs, see {@link http://www.openehr.org/releases/1.0.2/html/architecture/overview/Output/paths_and_locators.html#1123890
 * 
 * ---- /ehr/{ehrId}/{versionedObject}@{versionLookup}  ----                        
 * http://localhost:8182/ehr/1234000/56780007@latest_trunk_version
 * http://localhost:8182/ehr/1234000/56780007@latest_version
 * http://localhost:8182/ehr/1234000/56780007@2005-08-02T04:30:00
 * 
 * ---- /ehr/{ehrId}/{versionedObject}::{version} ---
 * http://localhost:8182/ehr/1234000/56780007::ehr.us.lio.se::2
 * http://localhost:8182/ehr/1234000/87284370-2D4B-4e3d-A3F3-F303D2F4F34B::rmh.nhs.net::2
 * http://localhost:8182/ehr/1234000/87284370-2D4B-4e3d-A3F3-F303D2F4F34B::F7C5C7B7-75DB-4b39-9A1E-C0BA9BFDBDEC::2
 *
 * ---- /ehr/{ehrId}/{versionedObject}/{command} ---
 * http://localhost:8182/ehr/1234000/56780007/
 * http://localhost:8182/ehr/1234000/56780007/all_version_ids
 * http://localhost:8182/ehr/1234000/56780007/all_versions
 * http://localhost:8182/ehr/1234000/56780007/revision_history
 *  
 * http://localhost:8182/ehr/1234000/contributions/
 * 
 *  ---- /ehr/{ehrId}/{command} ---
 * http://localhost:8182/ehr/1234000/AdHocQuery
 * </pre>
 */
public final class IdentifiedEHRRouter extends WadlApplication implements EEEConstants{
			 
		protected String systemID;
		protected XMLDBHelper dbHelper;
		protected EHRDatabaseReadInterface basicDBRead;
		protected EHRDatabaseWriteInterface basicDBWrite;
		protected Map<String, QueryTranslator> queryTranslatorMap;
		private XMLBinding xmlBinding;

		public IdentifiedEHRRouter(Context context, String systemID,
				EHRDatabaseReadInterface basicDBRead, EHRDatabaseWriteInterface basicDBWrite, XMLDBHelper dbHelper, Map<String, QueryTranslator> queryTranslatorMap, XMLBinding xmlBinding) {
			super(context);
			this.systemID = systemID;
			this.basicDBRead = basicDBRead;
			this.basicDBWrite = basicDBWrite;
			this.dbHelper = dbHelper;
			this.queryTranslatorMap = queryTranslatorMap; 
			this.setName("EEE Identified EHR Router/Resource");
			this.setAuthor("Medical informatics group, Department of Biomedical Engineering, Linköping University");
			this.setDescription("LiU EEE is an Educational EHR Environment based on openEHR and REST architecture. " +
					"The EHR Resource is used to retrieve and query EHR content and to handle openEHR CONTRIBUTIONs for a specific EHR identified by EHR ID" +
					" Note that the paths indicated as /ehr/:{ehr_id} should be containing /ehr:{ehr_id} instead (this listing error is produced during WADL-auto-generation).");
			this.xmlBinding = xmlBinding;
		}

		@Override
		public Restlet createInboundRoot() {
			
			// Create routers
			Router identifiedEhrRouter = new Router(getContext());
			
			// Put a references to them in the ehr router context (to be used by e.g. Resource classes)			
			identifiedEhrRouter.getContext().getAttributes().put(DemoStarter.KEY_TO_BASIC_DB_READER, basicDBRead);
			identifiedEhrRouter.getContext().getAttributes().put(DemoStarter.KEY_TO_BASIC_DB_WRITER, basicDBWrite);

			// TODO: Move CB stuff out of EHRRouter
			ContributionBuilderStorageInXMLDB contrBuilderDBHandler;
			try {
				contrBuilderDBHandler = new ContributionBuilderStorageInXMLDB(dbHelper, xmlBinding);
			} catch (Exception e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getLocalizedMessage(), e);
			}
			identifiedEhrRouter.getContext().getAttributes().put(EEEConstants.KEY_TO_CONTRIBUTION_BUILDER_DB_INSTANCE, contrBuilderDBHandler);	
			identifiedEhrRouter.getContext().getAttributes().put(EEEConstants.KEY_TO_XMLDBHELPER_INSTANCE, dbHelper);	
			identifiedEhrRouter.getContext().getAttributes().put(EEEConstants.SYSTEM_ID, systemID);	

			identifiedEhrRouter.getContext().getAttributes().put(KEY_TO_QUERY_TRANSLATOR_MAP, queryTranslatorMap);	
			
			identifiedEhrRouter.setDefaultMatchingMode(Template.MODE_STARTS_WITH);			
			
			// ********* IDENTIFIED EHR ROUTER part 1 (for URIs starting with an EHR ID) *********

			identifiedEhrRouter.attach("", IdentifiedEHR.class, Template.MODE_EQUALS);
			identifiedEhrRouter.attach("/", IdentifiedEHR.class, Template.MODE_EQUALS);

			identifiedEhrRouter.attach("trace", TraceResource.class, Template.MODE_STARTS_WITH);

//			route = identifiedEhrRouter.attach("/adHocQuery", AdHocQueryResource.class); 
//			route.getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);

//			identifiedEhrRouter.attach("q/XQuery/", XQueryQueryResource.class, Template.MODE_STARTS_WITH); 			
//			identifiedEhrRouter.attach("q/XQuery/", XQueryQueryResource.class, Template.MODE_STARTS_WITH); 

//			identifiedEhrRouter.attach("q/AdHocQuery/", AdHocQueryResource.class, Template.MODE_STARTS_WITH);
			identifiedEhrRouter.attach("q/", Query.class, Template.MODE_EQUALS);			
			identifiedEhrRouter.attach("q/{"+QUERY_LANGUAGE+"}/", Query.class, Template.MODE_EQUALS);
			identifiedEhrRouter.attach("q/{"+QUERY_LANGUAGE+"}/{"+QUERY_SHA+"}/info/", StoredQueryInfo.class, Template.MODE_EQUALS);
			identifiedEhrRouter.attach("q/AQL/{"+QUERY_SHA+"}/", StoredQueryAQL.class, Template.MODE_EQUALS);
			identifiedEhrRouter.attach("q/XQuery/{"+QUERY_SHA+"}/", StoredQueryXQueryAQLHybrid.class, Template.MODE_EQUALS);

			// identifiedEhrRouter.attach("q/{"+QUERY_LANGUAGE+"}/{"+QUERY_SHA+"}/", StoredQuery--some-subclass--.class, Template.MODE_EQUALS);

			identifiedEhrRouter.attach("timeline-1", new InfoRestlet("timeline-1", identifiedEhrRouter.getContext()), Template.MODE_STARTS_WITH);
			identifiedEhrRouter.attach("timeline-2", new InfoRestlet("timeline-2", identifiedEhrRouter.getContext()), Template.MODE_STARTS_WITH);
			identifiedEhrRouter.attach("frame-1",    new InfoRestlet("frame-1", identifiedEhrRouter.getContext()), Template.MODE_STARTS_WITH);
			identifiedEhrRouter.attach("bodymap-1",  new InfoRestlet("bodymap-1", identifiedEhrRouter.getContext()), Template.MODE_STARTS_WITH);
			
			
			// ********* IDENTIFIED EHR ROUTER part 2: CONTRIBUTIONS *********
		
			// TODO: Discuss: Should such a complete contribution listing really be available? 
			// Paging ?s=40&n=20&direction=backward or time-boundaries or /latest
			
			TemplateRoute route;
			route = identifiedEhrRouter.attach("contributions/", 
					ContributionListingResource.class);
			route.getTemplate().setMatchingMode(Template.MODE_EQUALS);

			route = identifiedEhrRouter.attach("contributions/latest/", 
					ContributionLatestResource.class);
			route.getTemplate().setMatchingMode(Template.MODE_EQUALS);

			route = identifiedEhrRouter.attach("contributions/{"+CONTRIBUTION_ID+"}/", 
					ContributionResource.class);
			route.getTemplate().setMatchingMode(Template.MODE_EQUALS);

			
			// ********* IDENTIFIED EHR ROUTER part 3: VERSIONED OBJECTS *********
			// Note that trailing slashes (/) after full OBJECT_ID::CREATING_SYSTEM_ID::VERSION_TREE_ID version ID
			// are not required by the spec at  
			// http://www.openehr.org/releases/1.0.2/html/architecture/overview/Output/paths_and_locators.html#1123890
			// in order to retrieve a full VERSION
			// also a trailing slash indicates that further refinement within the VERSION is sought for
			// (but that refinment is not yet implemented)
			
			// Trace printouts
			identifiedEhrRouter.attach("{"+OBJECT_ID+"}/trace", TraceResource.class, Template.MODE_EQUALS);
			
			// If a complete ID is encountered, then dispatch to the VersionedObjectResource class:
			identifiedEhrRouter.attach("{"+OBJECT_ID+"}::{"+CREATING_SYSTEM_ID+"}::{"+VERSION_TREE_ID+"}",
					VersionedObjectResource.class, Template.MODE_EQUALS);
			// TODO: This below doesn't help, probably because colons are between, not in variables: 
			//    route.getTemplate().setEncodingVariables(true);
			// Thus resorting to solution below
			identifiedEhrRouter.attach("{"+OBJECT_ID+"}%3A%3A{"+CREATING_SYSTEM_ID+"}%3A%3A{"+VERSION_TREE_ID+"}",
					VersionedObjectResource.class, Template.MODE_EQUALS);
			
			// If a complete ID with trailing slash is encountered, then only a part of the 
			// version is requested according to the openehr document linked above. 
			// Note that we use MODE_STARTS_WITH so that all trailing paths are sent 
			// on to the implementing resource 
			identifiedEhrRouter.attach("{"+OBJECT_ID+"}::{"+CREATING_SYSTEM_ID+"}::{"+VERSION_TREE_ID+"}/", NotImplementedResource.class, Template.MODE_STARTS_WITH);
			identifiedEhrRouter.attach("{"+OBJECT_ID+"}%3A%3A{"+CREATING_SYSTEM_ID+"}%3A%3A{"+VERSION_TREE_ID+"}/",
					NotImplementedResource.class, Template.MODE_STARTS_WITH);
			
			// If @-style lookup is encountered, then dispatch to versionedObjAtLookup
			route = identifiedEhrRouter.attach("{"+OBJECT_ID+"}@{"+VERSION_LOOKUP+"}",
					VersionedObjectResource.class);
			route.getTemplate().setMatchingMode(Template.MODE_EQUALS);
			
			// If /command is encountered, then dispatch to versionedObjCommand
			route = identifiedEhrRouter.attach("{"+OBJECT_ID+"}/{"+COMMAND+"}",
					VersionedObjectCommandResource.class); // commands for getting specific details
			route.getTemplate().setMatchingMode(Template.MODE_EQUALS);								

			// If / is encountered after OBJECT_ID
			route = identifiedEhrRouter.attach("{"+OBJECT_ID+"}/",
					VersionedObjectResource.class); 
			route.getTemplate().setMatchingMode(Template.MODE_EQUALS);										
			// TODO: We might need to exclude routes containing :: and %3A%3A in the above "{"+OBJECT_ID+"}/" route, but only if it eats the wrong stuff
			
//				// TODO: Check command handling (versionedObjCommand does not do much)
//				route = versionedObjectRouter.attach("/",
//						versionedObjCommand); // attach version "root-uri" as a command too...
//				route.getTemplate().setMatchingMode(Template.MODE_EQUALS);
			
										
//				router.attach("/ehr/{"+EHR_ID+"}/{"+COMMAND+"}",
//						VersionedObjectResource.class); // commands for getting specific details of an entire EHR			
							
//				System.out.println("Done initiating routers (in EHRTestRestStarter.main)");
			
			return identifiedEhrRouter;
		}
	}