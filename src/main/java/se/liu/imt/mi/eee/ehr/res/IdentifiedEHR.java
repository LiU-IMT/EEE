package se.liu.imt.mi.eee.ehr.res;

import java.util.HashMap;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Node;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

import se.liu.imt.mi.eee.db.EHRDatabaseReadInterface;
import se.liu.imt.mi.eee.db.xmldb.XMLDBHelper;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.trigger.EhrMetadataCache;
import se.liu.imt.mi.eee.utils.FreemarkerSupportResource;
import se.liu.imt.mi.eee.utils.Util;
import freemarker.ext.dom.NodeModel;

public class IdentifiedEHR extends FreemarkerSupportResource implements EEEConstants {

	protected EHRDatabaseReadInterface<?, ?> dbReader;
	protected XMLDBHelper dbHelper;
	protected String ehrId;
	protected EhrMetadataCache cache;
	protected String etag = null;
	
	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		// Extract strings from request
		ehrId = (String) getRequestAttributes().get(EHR_ID);
		dbReader = (EHRDatabaseReadInterface<?,?>) getContext().getAttributes()
		.get(EEEConstants.KEY_TO_BASIC_DB_READER);		
		dbHelper = (XMLDBHelper) getContext().getAttributes().get(
				KEY_TO_XMLDBHELPER_INSTANCE);
		
		// FIXME: Commented out during development 		
//		cache = (EhrMetadataCache) getContext().getAttributes().get(EEEConstants.KEY_TO_EHR_METADATA_CACHE);		
//		etag = Util.checkOrPopulateCacheThenReturn304EarlyIfETagsMatch(this, cache, dbReader, ehrId);
	}

	@Get("html")
	public Representation handleGetHTML() throws Exception {
		Map<String, Object> variablesForTemplate = new HashMap<String, Object>();		
		Representation reprToReturn;
			addFreemarkerTemplateVariables(variablesForTemplate);
			reprToReturn = handleResponseViaFreemarkerTemplate("html", MediaType.TEXT_HTML, variablesForTemplate);	
				// if available then set ETag according to cache
				if (etag != null) {
					reprToReturn.setTag(new Tag(etag));
					// TODO: Add last modified later
				}	
		return reprToReturn;
	}

	public void addFreemarkerTemplateVariables(Map<String, Object> variablesForTemplate) throws ResourceException {
		// An ehr_id was supplied
		Collection ehrColl;
		XMLResource resource;
		Node ehrNode;
		NodeModel freeMarkerNodeModel;
		try {
			ehrColl = ((XMLDBHelper) dbHelper).getRootCollection() // FIXME: avoid XMLDBHelper by either putting this into EHRDatabaseReadInterface or (better) by modifying the freemarker code to use some other calls to make an ehr frontpage.
			.getChildCollection("EHR");
			resource = (XMLResource) ehrColl.getResource(ehrId);
			if (resource == null) {
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, 
						"The EHR with ID " + ehrId
						+ " does not exist in this system");
			} else {
				ehrNode = resource.getContentAsDOM();
				freeMarkerNodeModel = NodeModel.wrap(ehrNode
						.getFirstChild());
			}
		} catch (XMLDBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, 
					"Could not access EHR database currently.", e);

		}
		variablesForTemplate.put("ehrNode", freeMarkerNodeModel);
		variablesForTemplate.put("ehrId", ehrId);					
	}

}
