package se.liu.imt.mi.eee.structure;

import java.util.HashMap;
import java.util.Map;

import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.restlet.data.MediaType;

public interface EEEConstants {
	public static final String EHR_ID = "ehr_id";
	
	// Parts of OBJECT_VERSION_ID, see openEHR Support IM, Section 4.3.8 (release 1.0.2)
	public static final String OBJECT_ID = "object_id";
	public static final String CREATING_SYSTEM_ID = "creating_system_id";
	public static final String VERSION_TREE_ID = "version_tree_id";
	
	/**
	 *  Temporary ID-string identifying versioned objects in ContributionBuilder etc
	 */
	public static final String TEMP_ID = "temp-id";
	
	/**
	 *  Key to existing ContibutionBuilderItem used in ContributionBuilder etc
	 *  @see ContibutionBuilderItem
	 */
	public static final String CB_ITEM = "cbItem";	

	public static final String CONTRIBUTION_BUILD_ID = "cb-id";	

	public static final String DATA = "data";
	public static final String DATA_FIELD_MEDIA_TYPE = "data_field_media_type";
	
	
	public static final String PRECEDING_VERSION_ID = "preceding_version_id";
	public static final String OTHER_INPUT_VERSION_UIDS = "other_input_version_uids";
	public static final String CHANGE_TYPE = "change_type";
	public static final String LIFECYCLE_STATE = "lifecycle_state";
	public static final String OBJECT_TYPE = "object_type";
	
	public static final String COMMAND = "command";

	public static final String VERSION_LOOKUP = "versionLookup";
	
	public static final String COMMITTER_ID = "committer_id";
	public static final String CONTRIBUTION_ID = "contribution_id";	
	
	public static final String OPENEHR_TERMINOLOGY_ID = "openehr";

	public static final String DEMOGRAPHIC = "demographic";
	public static final String EHR = "ehr";
	
	public static final String USER_ID = "userID";
	
	// Trigger handler constants
	public static final String KEY_TO_CONTRIBUTION_TRIGGER = "KEY_TO_CONTRIBUTION_TRIGGER";
	public static final String KEY_TO_CB_TRIGGER = "KEY_TO_CB_TRIGGER";
	
	// Cache constants
	public static final String KEY_TO_EHR_METADATA_CACHE = "KEY_TO_EHR_METADATA_CACHE";

	// Bookmark related constants
	public static final String BOOKMARK_ID = "BOOKMARK_ID";
	public static final String URI = "uri"; // Also used in Contribution Builder /cb
	public static final String TITLE = "title";
	public static final String TAGS = "tags";
	public static final String BOOKMARK_COMMAND_QR = "qr";
	public static final String BOOKMARK_COMMAND_INFO = "info";
	public static final String KEY_TO_BOOKMARK_STORAGE = "KEY_TO_BOOKMARK_STORAGE";
	public static final String PREFIX = "prefix";

	// Parameter names for EHR queries
	public static final String QUERY = "query";
	public static final String DEBUG = "debug";	
	public static final String KEY_TO_QUERY_STORAGE = "KEY_TO_QUERY_STORAGE";
	public static final String TRANSLATED_QUERY = "translated_query";
	public static final String QUERY_LANGUAGE = "queryLanguage";
	public static final String STORED_QUERY = "storedQuery";
	public static final String KEY_TO_QUERY_TRANSLATOR_MAP = "KEY_TO_QUERY_TRANSLATOR_MAP";
	
	public static final String QUERY_SHA = "query-SHA";	
	public static final String RETURN_MEDIA_TYPE = "media";		
	public static final String INCOMING_MEDIA_TYPE = "incomingMediaType";

	
	public enum VersionableObjectType {
		COMPOSITION(EHR), FOLDER(EHR), 
		EHR_ACCESS(EHR),EHR_STATUS(EHR),
		
		PARTY(DEMOGRAPHIC), ACTOR(DEMOGRAPHIC), PERSON(DEMOGRAPHIC), 
		ROLE(DEMOGRAPHIC), GROUP(DEMOGRAPHIC), ORGANISATION(DEMOGRAPHIC), 
		AGENT(DEMOGRAPHIC);		
		private final String packageName;
		VersionableObjectType(String packageName){
			this.packageName=packageName;
		}
		public String getPackageName() {return packageName;}
	}
	
	
	// TODO: Create a utility app (or XSLT) that converts openEHR vocabulary XML 
	//       file to enums similar to the ones below.
	
	/**
	 * Audit change types found in http://www.openehr.org/releases/1.0.2/architecture/rm/common_im.pdf 
	 * and http://www.openehr.org/releases/1.0.2/architecture/terminology.pdf
	 * 
	 * This vocabulary codifies the kinds of changes to data which are recorded in audit trails.
	 */
	public enum AuditChangeType {
		creation (249, "Change type was creation."),
		amendment (250, "Change type was amendment, i.e. correction of the previous version."),
		modification (251, "Change type was update of the previous version"),
		synthesis (252, "Change type was creation synthesis of data due to conversion process, typically a data importer."),
		deleted (523, "Change type was logical deletion."),
		attestation (666, "Existing data were attested."),
		unknown (253, "Type of change unknown");
		
	    private final int numericCode;
	    private final String description;
	    AuditChangeType(int numericCode, String description) {
	        this.numericCode=numericCode;
	        this.description=description;
	    }
	    public int getNumericCode()   { return numericCode; }
	    public String getDescription() { return description; }
	    public DvCodedText getAsDvCodedText() { return new DvCodedText(name(), new CodePhrase(OPENEHR_TERMINOLOGY_ID, Integer.toString(numericCode))); }
	}
	
	/**
	 * Attestation Reasons as described in  
	 * http://www.openehr.org/releases/1.0.2/architecture/rm/common_im.pdf 
	 * and http://www.openehr.org/releases/1.0.2/architecture/terminology.pdf
	 * 
	 * This vocabulary codifies attestation statuses of Compositions or 
	 * other elements of the health record
	 */
	public enum AttestationReason {
		signed (240, "The attested information has been signed by its signatory."),
		witnessed (648, "This attested information has been witnessed by the signatory.");

	    private final int numericCode;
	    private final String description;
	    AttestationReason(int numericCode, String description) {
	        this.numericCode=numericCode;
	        this.description=description;
	    }
	    public int numericCode()   { return numericCode; }
	    public String description() { return description; }
	    public DvCodedText asDvCodedText() { return new DvCodedText(name(), new CodePhrase(OPENEHR_TERMINOLOGY_ID, Integer.toString(numericCode))); }
	}
	
	/**
	 * This vocabulary codifies lifecycle states of Compositions or other elements of the health record.
	 * See http://www.openehr.org/releases/1.0.2/architecture/rm/common_im.pdf 
	 * and http://www.openehr.org/releases/1.0.2/architecture/terminology.pdf
	 */
	public enum VersionLifecycleState {
		complete (532, "Item is complete at time of committal."),
		incomplete (553, "Item is incomplete at time of committal, " +
				"in the view of the author. Further editing or review " +
				"needed before its status will be set to �finished�."),
		deleted (523, "Item has been logically deleted.");

	    private final int numericCode;   // in kilograms
	    private final String description; // in meters
	    VersionLifecycleState(int numericCode, String description) {
	        this.numericCode=numericCode;
	        this.description=description;
	    }
	    public int numericCode()   { return numericCode; }
	    public String description() { return description; }
	    public DvCodedText asDvCodedText() { return new DvCodedText(name(), new CodePhrase(OPENEHR_TERMINOLOGY_ID, Integer.toString(numericCode))); }
	}
	
	//  public enum ParserReturnType { XML, XQuery-EEE-0_1; }
	
	public enum AQLParserReturnType {
		XML, XQuery_EEE_0_1
	}

	public enum MimeHelper {
		HTML("html", MediaType.TEXT_HTML), 
		XML("xml", MediaType.APPLICATION_ALL_XML), 
		TXT("txt", MediaType.TEXT_PLAIN), 
		JSON("json", MediaType.APPLICATION_JSON);

		private final String fileSuffix;
		private final MediaType mediaType;

		MimeHelper(String fileSuffix, MediaType mediaType) {
			this.fileSuffix = fileSuffix;
			this.mediaType = mediaType;
		}

		public String getFileSuffix() {
			return fileSuffix;
		}

		public MediaType getMediaType() {
			return mediaType;
		}

	}

	
	/**
	 * A key stored in the restlet context, looks up the EHRXMLDBHandler instance
	 * It should be stored in the context upon start of the application. 
	 */
	public static final String KEY_TO_BASIC_DB_READER = "KEY_TO_BASIC_DB_READER";
	public static final String KEY_TO_BASIC_DB_WRITER = "KEY_TO_BASIC_DB_WRITER";

	/**
	 * A key stored in the restlet context, looks up the ContributionBuilderStorageInXMLDB instance
	 * It should be stored in the context upon start of the application. 
	 */
	public static final String KEY_TO_CONTRIBUTION_BUILDER_DB_INSTANCE = "KEY_TO_CONTRIBUTION_BUILDER_DB_INSTANCE";

	/**
	 * A key stored in the restlet context, looks up the XMLDBHelper instance (used e.g. to get xquery service)
	 * It should be stored in the context upon start of the application. 
	 */
	public static final String KEY_TO_XMLDBHELPER_INSTANCE = "KEY_TO_XMLDBHELPER_INSTANCE";

	/**
	 * A key stored in the restlet context, looks up the Freemarker configuration instance (used e.g. to generate pages from freemarker templates)
	 * It should be stored in the context upon start of the application. 
	 */
	public static final String KEY_TO_FREEMARKER_CONFIGURATION = "KEY_TO_FREEMARKER_CONFIGURATION";

	/**
	 * A key stored in the restlet context, looks up the openEHR java-ref-impl object for (to/from) XML conversion
	 * It should be stored in the context upon start of the application. 
	 */
	public static final String KEY_TO_XML_BINDING = "KEY_TO_XML_BINDING";

	/**
	 * A key stored in the restlet context, looks up Archetype and Templates Repository
	 * It should be stored in the context upon start of the application. 
	 */
	public static final String KEY_TO_AT_REPO = "KEY_TO_AT_REPO";

	
	/**
	 * A key stored in the restlet context, looks up the default system ID (String). 
	 * It should be stored in the context upon start of the application. 
	 */
	public static final String SYSTEM_ID = "system_id";

	public static final String SCHEMA_XSI = "http://www.w3.org/2001/XMLSchema-instance";
	public static final String SCHEMA_OPENEHR_ORG_V1 = "http://schemas.openehr.org/v1";
	public static final String SCHEMA_EEE_OPENEHR_EXTENSION = "http://www.imt.liu.se/mi/ehr/2010/EEE-v1.xsd";

	
}

