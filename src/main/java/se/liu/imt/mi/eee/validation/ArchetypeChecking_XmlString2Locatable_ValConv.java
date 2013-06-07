package se.liu.imt.mi.eee.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import openEHR.v1.template.TEMPLATE;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openehr.am.archetype.Archetype;
import org.openehr.binding.XMLBinding;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.composition.Composition;
import org.openehr.schemas.v1.COMPOSITION;
import org.openehr.schemas.v1.CompositionDocument;
import org.openehr.validation.DataValidator;
import org.openehr.validation.DataValidatorImpl;
import org.openehr.validation.ValidationError;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import se.liu.imt.mi.eee.ArchetypeAndTemplateRepository;
import se.liu.imt.mi.eee.structure.ContibutionBuilderItem;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.structure.XmlHelper;
import se.liu.imt.mi.eee.utils.Util;

public class ArchetypeChecking_XmlString2Locatable_ValConv extends ValidatorAndConverterAdapter<ContibutionBuilderItem<String>, Locatable>{

	protected XMLBinding xmlBinding;
	protected ArchetypeAndTemplateRepository atRepo;

	public ArchetypeChecking_XmlString2Locatable_ValConv(XMLBinding xmlBinding, ArchetypeAndTemplateRepository atRepo) {
		super();
		this.xmlBinding = xmlBinding;
		if (xmlBinding == null) throw new NullPointerException("xmlBinding was null!");
		this.atRepo = atRepo;
		if (atRepo == null) throw new NullPointerException("atRepo was null!");
	}

	
	@Override
	public ValidationAndConversionResult<Locatable> validateAndConvert(ContibutionBuilderItem<String> input) {

		// TODO: Remove the two booleans intended for debugging 
		boolean validateXml = false;
		boolean validateTypedXml = true;
		
		ValidationAndConversionResult<Locatable> valRes = new ValidationAndConversionResult<Locatable>();
		
		String outerArchetypeId = null;
		String outerTemplateId = null;
		Locatable convertedLocatableObject = null;
		
		// TODO: Do template+archetype based listing and validation here 
		Set<String> containedArchetypes = new HashSet<String>();
		Set<String> containedTemplates	 = new HashSet<String>();			

		
		try {

			// Do we have data?
			if (input.getData() == null || input.getData().isEmpty()) valRes.addError(new Exception("The data field was empty!"));
			
			if (input.getMediaType() == null) valRes.addError(new Exception("The mediaType field was empty!")); 
			Metadata mediaType = MediaType.valueOf(input.getMediaType());
			// Is it marked as XML?
			if (!(mediaType.isCompatible(MediaType.APPLICATION_ALL_XML) || mediaType.isCompatible(MediaType.TEXT_XML))) {
				valRes.addError(new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED, "In this validator implementation only XML formatted data can be validated. Your item identified as "+input.getTempID()+" was marked as having format "+input.getMediaType()));
			}
			
			// Do we know what kind of openEHR object it is supposed to be?
			if (input.getVersionableObjectType() == null) valRes.addError(new Exception("The versionableObjectType field was empty!")); 
			SchemaType schemaTypeInCBItem = XmlHelper.convertEHRTypeEnumToSchemaType(input.getVersionableObjectType());
			
			if (!valRes.valid) return valRes; // No point continuing without the right data or metadata...
						
			valRes.addPassedStep("Basic CB item data and metadata present");

			// Check if it parses as XML at all
			XmlObject dataNode = null;

			ArrayList xoptErrList1 = new ArrayList();				
			XmlOptions xopts = setupXopts(xoptErrList1);
			//xopts.setLoadUseXMLReader( SAXParserFactory.newInstance().newSAXParser().getXMLReader() );
			xopts.setLoadReplaceDocumentElement(schemaTypeInCBItem.getName());
			dataNode = XmlObject.Factory.parse((String) input.getData(), xopts);
			
			valRes.addPassedStep("XML parsed");
			//System.out.println("ContributionBuilderValidateAndCommit analyzed type:"+dataNode.schemaType().getSourceName() +" ---- "+dataNode.getDomNode().getNodeName());			 
			
			// Validate XML (without schema)
			if (validateXml) {
				//dataNode.changeType(schemaTypeInCBItem);
				boolean valid2 = dataNode.validate(xopts);		
				if (!valid2) {
					valRes.addError(new Exception("The data could not be validated as XML;\n<br/>"+prettyPrintXmlErrors(xoptErrList1)));
					return valRes; // No point continuing without valid XML
				}
				valRes.addPassedStep("XML validated");
			}
						
			switch (input.getVersionableObjectType()) {
			case COMPOSITION:
				System.out.println("ContributionBuilderValidateAndCommit.handlePost() will verify Composition");
				ArrayList xoptErrList2 = new ArrayList();
				XmlOptions xopts2 = setupXopts(xoptErrList2);
				xopts2.setLoadReplaceDocumentElement(new QName(EEEConstants.SCHEMA_OPENEHR_ORG_V1, "composition"));
				xopts2.setDocumentType(CompositionDocument.type);

				// COMPOSITION xmlComp = CompositionDocument.Factory.parse(dataNode.newDomNode(), xopts2).getComposition();
				CompositionDocument compDoc = CompositionDocument.Factory.parse((String) input.getData(), xopts2);
				COMPOSITION xmlComp = compDoc.getComposition();
				if (validateTypedXml) {
					boolean valid = xmlComp.validate(xopts2);
					if (!valid){
						valRes.addError(new Exception("The data could not validated as a COMPOSITION according to the openEHR XML Schema;\n"+prettyPrintXmlErrors(xoptErrList2)));
						return valRes; // No point continuing if not valid
					}
					valRes.addPassedStep("Valid COMPOSITION XML");
				}	
				
				// Now extract archetype details from XML (since recursive traversal is harder in the java-RM)
//				String xmlText = xmlComp.xmlText();
//				extractArchetypeAndTemplateIds(containedArchetypes, containedTemplates, xmlText);
				extractArchetypeAndTemplateIds(containedArchetypes, containedTemplates, xmlComp, xopts2);
				
				if (containedArchetypes.size() > 0) valRes.addPassedStep("Found archetypes - "+containedArchetypes);				
				if (containedTemplates.size() > 0) valRes.addPassedStep("Found templates - "+containedTemplates);
				
				// Convert to Java RM object 
				// TODO: possibly replace with Jackson-based implementation				
				Object rmObject = xmlBinding.bindToRM(xmlComp);
				if (rmObject == null) throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "The data could not be converted from XML to openEHR RM");
				Composition comp = (Composition) rmObject;
				valRes.setConverted(comp);
				
				convertedLocatableObject = comp;
								
				break;

//			case FOLDER:
//				Folder folder = (Folder) rmObject;
//				break;
			// case ...	continue with other types

			default:
				throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED, "In the current implementation only openEHR formatted COMPOSITIONs can be committed. Your item identified as "+input.getTempID()+" was marked as being of type "+input.getVersionableObjectType().name());
				// break; //
			} // end switch

			
			valRes.addPassedStep("XML->RM conversion OK");
			
			if (convertedLocatableObject.getArchetypeDetails() == null) throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "The data did not contain any archetype_details node;\n<br/>");

			outerArchetypeId = convertedLocatableObject.getArchetypeDetails().getArchetypeId().getValue();
			valRes.addPassedStep("Outer archetype: "+outerArchetypeId);
			if (convertedLocatableObject.getArchetypeDetails().getTemplateId() != null) {
				outerTemplateId = convertedLocatableObject.getArchetypeDetails().getTemplateId().getValue();
				valRes.addPassedStep("Outer template: "+outerTemplateId);
			}
			
			Map<String, Archetype> archMap = atRepo.getArchetypeMap();
			Map<String, TEMPLATE> templMap = atRepo.getTemplateMap();
				
			Archetype archToValidateFrom = null;
			// TODO: Add template flattening 
			if (outerTemplateId == null) {
				// No template, so use archetype
				archToValidateFrom = archMap.get(outerArchetypeId);
				if (archToValidateFrom == null) throw new Exception("Could not find archetype '"+outerArchetypeId+"' in the repository");
			} else {
				// Template present, so flatten it and use the resulting archetype object.
				TEMPLATE template = templMap.get(outerTemplateId);
				if (template == null) throw new Exception("Could not find template '"+outerTemplateId+"' in the repository");
				archToValidateFrom = atRepo.flattenTemplate(template);
				if (archToValidateFrom == null) throw new Exception("Could notflatten template '"+outerTemplateId+"' from the repository");
			}
					
			DataValidator openEhrValidator = new DataValidatorImpl();
			List<ValidationError> oeValErrorList = openEhrValidator.validate(convertedLocatableObject, archToValidateFrom, archMap);
			
			if (oeValErrorList.size() == 0) {
				valRes.addPassedStep("Valid in accordance with "+archToValidateFrom.getArchetypeId().getValue());
			} else {
				for (ValidationError validationError : oeValErrorList) {
					// TODO: if/when org.openehr.validation.ValidationError inherits from Error
					valRes.addError(new Error(validationError.toString()));
				}
			}
			
		
			// recurseAndFetch(convertedLocatableObject, containedArchetypes, containedTemplates);
			
			valRes.setContainedArchetypes(new ArrayList<String>(containedArchetypes));
			valRes.setContainedTemplates(new ArrayList<String>(containedTemplates));
			
		} catch (Exception e) { 
			e.printStackTrace();	
			valRes.addError(new Exception ("Caught "+e.getClass().getSimpleName()+" in validateAndConvert(); "+e.getMessage(), e));
		}
		return valRes;
		
	}

	protected void extractArchetypeAndTemplateIds(
			Set<String> containedArchetypes, Set<String> containedTemplates,
			COMPOSITION xmlComp, XmlOptions xopts2) {

		// This version uses xpath and xquery library dependencies 

		XmlObject[] archetypeIDarray = xmlComp.selectPath(XmlHelper.XQUERY_NAMESPACE_DECLARATIONS + "//v1:archetype_details/v1:archetype_id/v1:value/text()");
		for (XmlObject xmlObject : archetypeIDarray) {
			String archetypeIDstring = xmlObject.newCursor().getTextValue();
			containedArchetypes.add(archetypeIDstring);
		}
		
		XmlObject[] templateIDarray = xmlComp.selectPath(XmlHelper.XQUERY_NAMESPACE_DECLARATIONS + "//v1:archetype_details/v1:template_id/v1:value/text()");
		for (XmlObject xmlObject : templateIDarray) {
			String templateIDstring = xmlObject.newCursor().getTextValue();
			containedTemplates.add(templateIDstring);
		}
	}
	

//	protected void OLDextractArchetypeAndTemplateIds(
//			Set<String> containedArchetypes, Set<String> containedTemplates,
//			String xmlText) {
//		// Since xpath and xquery library dependencies don't work in all environments currently
//		// let's resort to string processing				
//		int pos = 0;
//		do {
//			// ff to first archetype_id
//			pos = xmlText.indexOf("archetype_id",pos);
//			if (pos < 0) break;
//			pos = xmlText.indexOf("value",pos);
//			pos = xmlText.indexOf(">",pos)+1;
//			int endPos =  xmlText.indexOf("<", pos);
//			// now we are inside the tag
//			containedArchetypes.add(xmlText.substring(pos, endPos));
//			// ff to end of closing archetype_id tag
//			pos = xmlText.indexOf("archetype_id",endPos)+"archetype_id".length();
//		} while (pos > 0 && pos < xmlText.length());
//		
//		// Then again, but for templates
//		pos = 0;
//		do {
//			// ff to first archetype_id
//			pos = xmlText.indexOf("template_id",pos);
//			if (pos < 0) break;
//			pos = xmlText.indexOf("value",pos);
//			pos = xmlText.indexOf(">",pos)+1;
//			int endPos =  xmlText.indexOf("<", pos);
//			// now we are inside the tag
//			containedTemplates.add(xmlText.substring(pos, endPos));
//			// ff to end of closing archetype_id tag
//			pos = xmlText.indexOf("template_id",endPos)+"template_id".length();
//		} while (pos > 0 && pos < xmlText.length());
//	}
	
	
// /* The following is useless since the current RM-implementation only contains a
//	  stub with "return null;" for the .itemsAtPath() method :-( */
//	
//	boolean recurseAndFetch(Pathable pathableObj, Set<String> archetypeSet, Set<String> templateSet){
//		List<Object> children = pathableObj.itemsAtPath("/");
//		if (children.size()<1) {
//			return false;
//		} else {
//			for (Object object : children) {
//				System.out.println("ArchetypeChecking_XmlString2Locatable_ValConv.recurseAndFetch() object:"+object);			
//				if (object instanceof Locatable) {
//					Locatable loc = (Locatable) object;
//					Archetyped details = loc.getArchetypeDetails();
//					if (details != null) {
//						if (details.getArchetypeId() != null) archetypeSet.add(details.getArchetypeId().getValue());
//						if (details.getTemplateId() != null) templateSet.add(details.getTemplateId().getValue());
//					}
//				}
//				// Recurse
//				if (object instanceof Pathable) recurseAndFetch((Pathable) object, archetypeSet, templateSet);
//				
//			}
//			return true;
//		}
//
//	}
	
	//	protected void OLDextractArchetypeAndTemplateIds(
	//			Set<String> containedArchetypes, Set<String> containedTemplates,
	//			String xmlText) {
	//		// Since xpath and xquery library dependencies don't work in all environments currently
	//		// let's resort to string processing				
	//		int pos = 0;
	//		do {
	//			// ff to first archetype_id
	//			pos = xmlText.indexOf("archetype_id",pos);
	//			if (pos < 0) break;
	//			pos = xmlText.indexOf("value",pos);
	//			pos = xmlText.indexOf(">",pos)+1;
	//			int endPos =  xmlText.indexOf("<", pos);
	//			// now we are inside the tag
	//			containedArchetypes.add(xmlText.substring(pos, endPos));
	//			// ff to end of closing archetype_id tag
	//			pos = xmlText.indexOf("archetype_id",endPos)+"archetype_id".length();
	//		} while (pos > 0 && pos < xmlText.length());
	//		
	//		// Then again, but for templates
	//		pos = 0;
	//		do {
	//			// ff to first archetype_id
	//			pos = xmlText.indexOf("template_id",pos);
	//			if (pos < 0) break;
	//			pos = xmlText.indexOf("value",pos);
	//			pos = xmlText.indexOf(">",pos)+1;
	//			int endPos =  xmlText.indexOf("<", pos);
	//			// now we are inside the tag
	//			containedTemplates.add(xmlText.substring(pos, endPos));
	//			// ff to end of closing archetype_id tag
	//			pos = xmlText.indexOf("template_id",endPos)+"template_id".length();
	//		} while (pos > 0 && pos < xmlText.length());
	//	}
		
		
	// /* The following is useless since the current RM-implementation only contains a
	//	  stub with "return null;" for the .itemsAtPath() method :-( */
	//	
	//	boolean recurseAndFetch(Pathable pathableObj, Set<String> archetypeSet, Set<String> templateSet){
	//		List<Object> children = pathableObj.itemsAtPath("/");
	//		if (children.size()<1) {
	//			return false;
	//		} else {
	//			for (Object object : children) {
	//				System.out.println("ArchetypeChecking_XmlString2Locatable_ValConv.recurseAndFetch() object:"+object);			
	//				if (object instanceof Locatable) {
	//					Locatable loc = (Locatable) object;
	//					Archetyped details = loc.getArchetypeDetails();
	//					if (details != null) {
	//						if (details.getArchetypeId() != null) archetypeSet.add(details.getArchetypeId().getValue());
	//						if (details.getTemplateId() != null) templateSet.add(details.getTemplateId().getValue());
	//					}
	//				}
	//				// Recurse
	//				if (object instanceof Pathable) recurseAndFetch((Pathable) object, archetypeSet, templateSet);
	//				
	//			}
	//			return true;
	//		}
	//
	//	}
		
		private String prettyPrintXmlErrors(Collection collection) {
			String prettyString = "";
			for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
				XmlError err = (XmlError) iterator.next();
				prettyString = prettyString + "At Line "+err.getLine()+" column "+err.getColumn()+" : "+err.getMessage()+" ("+err.getErrorCode()+") </br>";
			}
			return prettyString;
		}


//	private String prettyPrintXmlErrors(Collection collection) {
//		String prettyString = "";
//		for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
//			XmlError err = (XmlError) iterator.next();
//			prettyString = prettyString + "At Line "+err.getLine()+" column "+err.getColumn()+" : "+err.getMessage()+" ("+err.getErrorCode()+") </br>";
//		}
//		return prettyString;
//	}

	public XmlOptions setupXopts(List xoptErrList) {
		XmlOptions xopts = new XmlOptions(XmlHelper.getXMLoptionsWithV1asDefault()); 		
		//XmlOptions xopts = new XmlOptions(XmlHelper.getXMLoptions2()); 
		xopts.setErrorListener(xoptErrList);
		xopts.setLoadLineNumbers(); //XmlOptions.LOAD_LINE_NUMBERS_END_ELEMENT);
		return xopts;
	}
	


}
