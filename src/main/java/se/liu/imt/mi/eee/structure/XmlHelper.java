package se.liu.imt.mi.eee.structure;

import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.output.DOMOutputter;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.schemas.v1.DVTEXT;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import se.liu.imt.mi.ehr.x2010.eeeV1.*;

public class XmlHelper implements EEEConstants {
	
	public static final String XQUERY_NAMESPACE_DECLARATIONS = 
		"declare namespace v1 = 'http://schemas.openehr.org/v1';\n"+
		"declare namespace eee = 'http://www.imt.liu.se/mi/ehr/2010/EEE-v1.xsd';\n" +
		"declare namespace xsi = 'http://www.w3.org/2001/XMLSchema-instance';\n";
	
	public static final String XQUERY_NAMESPACE_DECLARATIONS_AS_XML_ATTRIBUTES = 
		"xmlns:v1 = 'http://schemas.openehr.org/v1' "+
		"xmlns:eee = 'http://www.imt.liu.se/mi/ehr/2010/EEE-v1.xsd' " +
		"xmlns:xsi = 'http://www.w3.org/2001/XMLSchema-instance' ";
	
	protected static BidiMap bidi = new DualHashBidiMap();
	protected static XmlOptions xopt;
	protected static XmlOptions xopt2;
	protected static XmlOptions xoptEHR;
	protected static XmlOptions xoptCONTRIBUTION;
	protected static XmlOptions xoptVERSION;
	
    static {
    	bidi.put(VersionableObjectType.ACTOR, VERSIONEDCOMPOSITION.type);
    	bidi.put(VersionableObjectType.AGENT, VERSIONEDAGENT.type);
    	bidi.put(VersionableObjectType.COMPOSITION, VERSIONEDCOMPOSITION.type);
    	bidi.put(VersionableObjectType.EHR_ACCESS, VERSIONEDEHRACCESS.type);
    	bidi.put(VersionableObjectType.EHR_STATUS, VERSIONEDEHRSTATUS.type);
    	bidi.put(VersionableObjectType.FOLDER, VERSIONEDFOLDER.type);
    	bidi.put(VersionableObjectType.GROUP, VERSIONEDGROUP.type);
    	bidi.put(VersionableObjectType.ORGANISATION, VERSIONEDORGANISATION.type);
    	bidi.put(VersionableObjectType.PARTY, VERSIONEDPARTY.type);
    	bidi.put(VersionableObjectType.PERSON, VERSIONEDPERSON.type);
    	bidi.put(VersionableObjectType.ROLE, VERSIONEDROLE.type);
    	xopt = _generateXMLoptions();
    	xopt2 = _generateXMLoptions2();
    	xoptEHR = _generateXMLoptionsForEHRRootDocument();
    	xoptCONTRIBUTION = _generateXMLoptionsForCONTRIBUTIONRootDocument();
    	xoptVERSION = _generateXMLoptionsForVERSION();
    }
    
	public static SchemaType convertEHRTypeEnumToSchemaType(VersionableObjectType versionableObjectType) {
		return (SchemaType) bidi.get(versionableObjectType);		
	}

	public static VersionableObjectType convertSchemaTypeToVersionableObjectType(SchemaType schemaType){
		return (VersionableObjectType) bidi.getKey(schemaType);
	}
	
	// TODO: rename generate... to get...
	
	/**
	 * Creates an XML-snippet containing AUDIT_DETAILS using the supplied parameters. 
	 * The timestamp is invalid and needs to be replaced by an update durinig or before
	 * database commit.
	 * @param committer
	 * @param systemId
	 * @param change_type
	 * @return
	 */
	public static Node formatAuditDetails(String committer, String systemId,
			AuditChangeType change_type) {
		/*		
				<commit_audit>
				<system_id>10aec661-5458-4ff6-8e63-c2265537196d</system_id>
				<committer xsi:type="PARTY_IDENTIFIED">
					<name>emi-mca</name> <!-- this should be the username of the comitter etc).
				</committer>
				<time_committed>
					<value>2008-01-18T15:40:41Z</value>
				</time_committed>
				<change_type>
					<value>creation</value>
					<defining_code>
						<terminology_id>
							<value>openehr</value>
						</terminology_id>
						<code_string>249</code_string>
		
					</defining_code>
				</change_type>
			</commit_audit>
		*/
				
				//Create an AUDIT_DETAILS stub
				Namespace ns = Namespace.getNamespace("OE", "http://schemas.openehr.org/v1");
				Element feeder_audit = new Element("AUDIT_DETAILS", ns);
				feeder_audit.addContent(new Element("system_id").addContent(systemId));
				feeder_audit.addContent(new Element("committer").addContent(new Element("external_ref").addContent(committer)));
				feeder_audit.addContent(new Element("time_committed").addContent(new Element("value").addContent("incomplete-time-placeholder-to-be-deleted")));
					
				Element changeTypeNode = feeder_audit.addContent(new Element("change_type").addContent(new Element("value").addContent(change_type.name())));
				Element definingCodeNode = changeTypeNode.addContent(new Element("defining_code")
					.addContent(new Element("terminology_id")
						.addContent(new Element("value").addContent("openehr"))
					));
				definingCodeNode.addContent(new Element("code_string").addContent(Integer.toString(change_type.getNumericCode())));
				
				feeder_audit.addContent(new Comment("This is an incomplete stub created by the method formatAuditDetails, the external_ref above is not complete either."));
				org.jdom.Document jdoc = new org.jdom.Document(feeder_audit);
				
				DOMOutputter dop = new DOMOutputter();
				Document doc = null;
				
				try {
					doc = dop.output(jdoc);
				} catch (JDOMException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				return doc;
	}

	public static String formatDvText(DvText dvText){
		DVTEXT dvt = DVTEXT.Factory.newInstance();
		dvt.setValue(dvText.getValue());
		return dvt.xmlText();
	}

	public static XmlOptions getXMLoptionsWithV1asDefault() {return xopt;}
	
	protected static XmlOptions _generateXMLoptions() {
		XmlOptions xopt = new XmlOptions();

		HashMap<String, String> nsMap = new HashMap<String, String>();
		nsMap.put(SCHEMA_XSI, "xsi");
		nsMap.put(SCHEMA_EEE_OPENEHR_EXTENSION, "eee");
		nsMap.put(SCHEMA_OPENEHR_ORG_V1, "v1");
		nsMap.put(SCHEMA_OPENEHR_ORG_V1, "");
		
		xopt.setSaveImplicitNamespaces(nsMap);
		xopt.setSavePrettyPrint(); 
		xopt.setSaveAggressiveNamespaces();
		xopt.setUseDefaultNamespace();
		xopt.setSaveNamespacesFirst();
		xopt.setLoadAdditionalNamespaces(nsMap);
		xopt.setCharacterEncoding("UTF-8");
		return xopt;
	}

	// TODO: figure out if both the method above and below are necessary, try merging them
	
	public static XmlOptions getXMLoptions2() {return xopt2;}
			
	protected static XmlOptions _generateXMLoptions2() {
			XmlOptions xopt = new XmlOptions();
	
	//		HashMap<String, String> prefixToUriMap = new HashMap<String, String>();
	//		//prefixToUriMap.put("", "http://schemas.openehr.org/v1");
	//		prefixToUriMap.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
	//		prefixToUriMap.put("eee", SCHEMA_EEE_OPENEHR_EXTENSION);		
	//		// nsMap.put("oe", SCHEMA_OPENEHR_ORG_V1);
	//		xopt.setSaveImplicitNamespaces(prefixToUriMap);// TODO: Check differences between impl/sugg
			
			HashMap<String, String> uriToPrefixMap = new HashMap<String, String>();
			uriToPrefixMap.put(SCHEMA_XSI, "xsi");
			uriToPrefixMap.put(SCHEMA_EEE_OPENEHR_EXTENSION, "eee");
			uriToPrefixMap.put(SCHEMA_OPENEHR_ORG_V1, "v1");
			xopt.setSaveSuggestedPrefixes(uriToPrefixMap); // TODO: Check differences between impl/sugg
	
			xopt.setSavePrettyPrint(); 
			xopt.setSaveAggressiveNamespaces();
	//		xopt.setUseDefaultNamespace();
			xopt.setSaveNamespacesFirst();
			xopt.setCharacterEncoding("UTF-8");
			return xopt;
		}

	// ***** EHR root *****
	protected static XmlOptions _generateXMLoptionsForEHRRootDocument() {
		XmlOptions xopt =  new XmlOptions(getXMLoptions2());
		xopt.setSaveOuter();
		xopt.setSaveSyntheticDocumentElement(new QName(SCHEMA_EEE_OPENEHR_EXTENSION, "EHR"));	
		return xopt;
	}

	public static XmlOptions generateXMLoptionsForEHRRootDocumentWithErrorList(Collection<XmlError> coll) {
		XmlOptions xopt = new XmlOptions(getXMLoptionsForEHRRootDocument());
		xopt.setErrorListener(coll);
		return xopt;
	}
	
	public static XmlOptions getXMLoptionsForEHRRootDocument() {return xoptEHR;}

	
	// ***** CONTRIBUTION root *****
	
	protected static XmlOptions _generateXMLoptionsForCONTRIBUTIONRootDocument() {
		XmlOptions xopt =  new XmlOptions(getXMLoptions2());
		xopt.setSaveOuter();
		xopt.setSaveSyntheticDocumentElement(new QName(SCHEMA_EEE_OPENEHR_EXTENSION, "CONTRIBUTION"));	
		return xopt;
	}
	
	public static XmlOptions generateXMLoptionsForContributionRootDocumentWithErrorList(Collection<XmlError> coll) {
		XmlOptions xopt = new XmlOptions(getXMLoptionsForContributionRootDocument());
		xopt.setErrorListener(coll);
		return xopt;
	}

	public static XmlOptions getXMLoptionsForContributionRootDocument() {
		return xoptCONTRIBUTION;
	}
	
	// ***** VERSIONED_OBJECT root *****
	
	public static XmlOptions getXMLoptionsForVERSION() {return xoptVERSION;}
	
	protected static XmlOptions _generateXMLoptionsForVERSION() {
		XmlOptions xopt =  new XmlOptions(getXMLoptions2());
		xopt.setSaveOuter();
		xopt.setSaveSyntheticDocumentElement(new QName(SCHEMA_EEE_OPENEHR_EXTENSION, "versions"));	
		return xopt;
	}

	
}
