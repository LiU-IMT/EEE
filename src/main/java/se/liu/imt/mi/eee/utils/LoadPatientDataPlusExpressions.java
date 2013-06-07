package se.liu.imt.mi.eee.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.NotImplementedException;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openehr.rm.common.changecontrol.Contribution;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.schemas.v1.COMPOSITION;
import org.restlet.resource.ClientResource;
import org.w3c.dom.Node;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;

import se.liu.imt.mi.eee.db.DatabaseInterface.DatabaseMode;
import se.liu.imt.mi.eee.db.xmldb.EHRXMLDBHandler;
import se.liu.imt.mi.eee.db.xmldb.XMLDBHelper;
import se.liu.imt.mi.eee.db.xmldb.exist.ExistXMLDBHelper;
import se.liu.imt.mi.eee.structure.EEEConstants.AuditChangeType;
import se.liu.imt.mi.eee.structure.EEEConstants.VersionLifecycleState;
import se.liu.imt.mi.eee.structure.EEEConstants.VersionableObjectType;
import se.liu.imt.mi.eee.structure.VersionedObjectListItem;
//import se.liu.imt.mi.eee.db.xmldb.sedna.SednaXMLDBHelper;

/**
 * This class should be used to load patient data into EEE. If you run this file
 * in Eclipse you will get a prompt where you should enter two arguments to the
 * method: ehrId and directoryWithPatientData. Use the following syntax:
 * "GTT_c1" "resources/www-file-root/GTT/c1_transfusion" (white space between
 * arguments) where "GTT_c1" is the ehrId and the rest is the
 * directoryWithPatientData.
 * 
 * See also example usage of this file in LoadGTT.java.
 * 
 * TODO: Everything works fine when running with eXist database, but when using
 * Sedna, a thread is not terminated.
 * 
 * @author Marie, Daniel, Erik
 */
public class LoadPatientDataPlusExpressions {
	private static String COMMITTER_NAME = "EEE Testcase Import Service";
	private static String SYSTEM_ID = "test1.eee.mi.imt.liu.se";
	private static PartyIdentified COMMITTER = new PartyIdentified(null,
			COMMITTER_NAME, null);

	// If ignoreBadFiles is true, we will skip bad files and try the next one,
	// only works if also createSeparateContributionForEachFile = true
	private static boolean IGNORE_BAD_FILES = false;
	private static boolean CREATE_SEPARATE_CONTRIBUTION_FOR_EACH_FILE = true;

	private static EHRXMLDBHandler ehrDatabaseHandler;

	public static void main(String args[]) throws Exception {
		if (args.length != 2) {
			throw new Exception(
					"Wrong number of arguments. Two input arguments are required;"
							+ " ehrId and directory to files. Separate the arguments with a colon in the input dialog box.");
		}

		String ehrId = args[0];
		String directoryWithPatientData = args[1];

		LoadPatientDataPlusExpressions patientDataLoader = new LoadPatientDataPlusExpressions();
		patientDataLoader.loadPatientData(ehrId, directoryWithPatientData);
	}

	/**
	 * See comment above for entire class.
	 * 
	 * @param ehrId
	 *            e.g. "GTT_c1"
	 * @param directoryWithPatientData
	 *            e.g. "resources/www-file-root/GTT/c1_transfusion"
	 */
	public void loadPatientData(String ehrId, String directoryWithPatientData)
			throws Exception {

		// TODO: This code is also used in EHRTestRestStarter.main(). Refactor
		// so
		// the code is not duplicated!
		// TODO: Add to the config file a possibility to use this Mockup:
		//
		// To just test XML validity etc, uncomment line below...
		// ehrDatabaseHandler = new EHRXMLDBHandler(true,
		// new MockupXMLDBHelper(),
		// DatabaseMode.SINGLE_RECORD);

		// Load properties from configuration file using Commons Configuration
		Configuration config = new PropertiesConfiguration(
				"restserver.properties");
		String database = config.getString("database");
		
		if (!database.equals("eXist")) {
			throw new NotImplementedException("The only presently verified database is eXist. Other databases might get reenabled or added later.");
		}
		
		final XMLDBHelper dbHelper = new ExistXMLDBHelper(
				config.getString("databaseHost"),
				config.getString("databaseName"),
				config.getString("databaseUser"),
				config.getString("databasePass"), DatabaseMode.SINGLE_RECORD);
		
//		final XMLDBHelper dbHelper = (database.equals("eXist") ? new ExistXMLDBHelper(
//				config.getString("databaseHost"),
//				config.getString("databaseName"),
//				config.getString("databaseUser"),
//				config.getString("databasePass"), DatabaseMode.SINGLE_RECORD)
//				: (database.equals("Sedna") ? new SednaXMLDBHelper(
//						config.getString("databaseHost"),
//						config.getString("databaseName"),
//						config.getString("databaseUser"),
//						config.getString("databasePass"),
//						DatabaseMode.SINGLE_RECORD) : null));

		// TODO: Split to reader and writer according to the new interfaces...
		ehrDatabaseHandler = new EHRXMLDBHandler(true, dbHelper, "EHR",
				"Contributions", null, Util.setUpXMLBinding("sv"));

		cleanEhrForPatientAndLoadFiles(dbHelper, ehrId,
				directoryWithPatientData);
	}

	private void cleanEhrForPatientAndLoadFiles(XMLDBHelper dbHelper,
			String ehrId, String directoryWithPatientData) throws Exception {
		System.out.println("EhrId: " + ehrId);
		System.out.println("Directory: " + directoryWithPatientData);
		removeOldEhrAndCreateNewEmptyEhrForPatient(dbHelper, ehrId);
		File c1 = new File(directoryWithPatientData);
		loadFilesInDirectory(c1, ehrId);
		System.out.println("Finished loading files!");
		System.out.println("-------");
	}

	private void removeOldEhrAndCreateNewEmptyEhrForPatient(
			XMLDBHelper dbHelper, String ehrId) throws Exception {
		Collection EHR = dbHelper.getRootCollection().getChildCollection("EHR");
		Resource res = EHR.getResource(ehrId);
		if (res != null) {
			EHR.removeResource(res);
		}
		ehrDatabaseHandler.createEHR(ehrId, SYSTEM_ID);
	}

	private void loadFilesInDirectory(File directory, String ehrId)
			throws Exception {
		if (!directory.isDirectory()) {
			throw new Exception(
					"The path given is not a directory. Directory given: "
							+ directory.toString());
		}
		File[] fileList = findFiles(directory);

		if (!CREATE_SEPARATE_CONTRIBUTION_FOR_EACH_FILE) {
			loadFilesNotOneContributionPerDocument(fileList, ehrId);
		} else {
			loadFilesOneContributionPerDocument(fileList, ehrId);
		}
	}

	private File[] findFiles(File directory) {
		File[] fileList = directory.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith("xml")
						|| pathname.getName().endsWith("XML");
			}
		});
		return fileList;
	}

	private void loadFilesNotOneContributionPerDocument(File[] fileList,
			String ehrId) throws Exception {
		List<VersionedObjectListItem<Node>> objects = createListForSingleContributionOfAllDocuments(fileList);
		Contribution returnedContribution = commitSingleContribution(objects,
				ehrId);
		if (fileList.length != returnedContribution.getVersions().size()) {
			throw new Exception(
					"Filelist length does not correspond to the returnedContribution version size.");
		}
		System.out
				.println("PatientDataLoader.loadPatientData() finished a combined contribution: "
						+ returnedContribution.toString());
	}

	private Contribution commitSingleContribution(
			List<VersionedObjectListItem<Node>> objects, String ehrId)
			throws Exception {
		Contribution returnedContribution = ehrDatabaseHandler
				.commitContributionOfOriginalVersions(COMMITTER, ehrId,
						SYSTEM_ID, objects, null, null);
		return returnedContribution;
	}

	private List<VersionedObjectListItem<Node>> createListForSingleContributionOfAllDocuments(
			File[] fileList) throws Exception {
		List<VersionedObjectListItem<Node>> objects = new ArrayList<VersionedObjectListItem<Node>>();
		for (int i = 0; i < fileList.length; i++) {
			System.out.println("PatientDataLoader.loadPatientData() Filename: "
					+ fileList[i].getAbsolutePath());
			objects.add(generateCompositionItem_creation_complete(fileList[i]));
		}
		return objects;
	}

	private void loadFilesOneContributionPerDocument(File[] fileList,
			String ehrId) throws Exception {
		List<VersionedObjectListItem<Node>> objects = new ArrayList<VersionedObjectListItem<Node>>();
		for (int i = 0; i < fileList.length; i++) {
			System.out.println("PatientDataLoader.loadPatientData() Filename: "
					+ fileList[i].getAbsolutePath());
			objects.clear();
			objects.add(generateCompositionItem_creation_complete(fileList[i]));
			try {
				Contribution returnedContribution = commitSingleContribution(
						objects, ehrId);
				if (returnedContribution.getVersions().size() != 1) {
					throw new Exception(
							"Size of returnedContribution versions is not 1.");
				}
				// assertEquals(1, returnedContribution.getVersions().size());
				System.out
						.println("PatientDataLoader.loadPatientData() SUCCESSFUL STORAGE: "
								+ fileList[i]);
			} catch (Exception e) {
				if (IGNORE_BAD_FILES) {
					System.out
							.println("PatientDataLoader.loadPatientData() FAILED TO STORE: "
									+ fileList[i] + "\nSkipping to next file");
				} else {
					e.printStackTrace();
					throw e;
				}
			}
		}
	}

	private VersionedObjectListItem<Node> generateCompositionItem_creation_complete(
			File file) throws Exception {
		COMPOSITION comp = LoadComposition.loadCompositionFromFile(file);
		String namespace = "declare namespace v1=\"http://schemas.openehr.org/v1\";\n" + 
				"declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\";\n";
		XmlObject[] codes = comp.selectPath(namespace + "//v1:defining_code");
		
		for(XmlObject code : codes) {
			String terminology = null;
			XmlObject[] terminologyObject = code.selectPath(namespace + "v1:terminology_id/v1:value/text()");
			if(terminologyObject != null) {
				// according to the RM there is only one terminology_id entry
				terminology = terminologyObject[0].newCursor().getTextValue();
				if(!terminology.equals("SNOMED-CT"))
					continue;
			}
			
			XmlObject[] codeStringObject = code.selectPath(namespace + "v1:code_string/text()");
			if(codeStringObject != null) {
				XmlCursor codeStringCursor = codeStringObject[0].newCursor();
				String codeString = codeStringCursor.getTextValue();
				System.out.println("LoadPatientDataPlusExpressions.generateCompositionItem_creation_complete() " + terminology + " : " + codeString);
				
				ClientResource uriResource = new ClientResource(
						"http://localhost:8183/getExpressionID?exp="
								+ codeString);
				try {
					uriResource.get();
					if (uriResource.getStatus().isSuccess()
							&& uriResource.getResponseEntity().isAvailable()) {
						String result = uriResource.getResponseEntity().getText();
						codeStringCursor.setTextValue(result);
					}
						
				} catch (Exception e) {
					System.out
							.println("URIProcessor.processURI(): Exception caught");
					continue;
				}
						
			}
			
			
		
		}
		
//		while(cursor.hasNextSelection()) {
//			cursor.toNextSelection();
//			
//			XmlObject object = cursor.getObject();
//			object.
////			c.removeXml();
//			AqlParser parser = new AqlParser(new StringReader(aql));
//			parser.setReturnType(EEEConstants.AQLParserReturnType.XQuery_EEE_0_1);
//			c.insertChars(parser.Query());
//		}
		
//		NodeList nodeList = (NodeList) defCodeExpr.evaluate(data,
//				XPathConstants.NODESET);
//		for (int i = 0; i < nodeList.getLength(); i++) {
//			Node item = nodeList.item(i);
//			if (terminologyExpr.evaluate(item).equals("SNOMED-CT")) {
//				Node n = (Node) codeExpr.evaluate(item, XPathConstants.NODE);
//				// Node cs = n.getChildNodes().item(0);
//				String code_string = n.getNodeValue();
//				System.out.println(code_string);
//
//				ClientResource uriResource = new ClientResource(
//						"http://localhost:8183/getExpressionID?exp="
//								+ code_string);
//				try {
//					uriResource.get();
//					if (uriResource.getStatus().isSuccess()
//							&& uriResource.getResponseEntity().isAvailable())
//						n.setNodeValue(uriResource.getResponseEntity()
//								.getText());
//				} catch (Exception e) {
//					System.out
//							.println("URIProcessor.processURI(): Exception caught");
//					continue;
//				}
//
//			}

//		}

		return new VersionedObjectListItem<Node>(
				"test2-id-will-be-replaced-anyway-due-to-creation",
				VersionableObjectType.COMPOSITION,
				VersionLifecycleState.complete, AuditChangeType.creation, comp.getDomNode());
	}

//	private VersionedObjectListItem<Node> generateCompositionItem_creation_complete_backup(
//			File file) throws Exception {
//		Node data = LoadComposition.loadCompositionFromFile(file).getDomNode();
//
//		javax.xml.namespace.NamespaceContext nsc = new NamespaceContext() {
//
//			public String getNamespaceURI(String prefix) {
//				if (prefix.equals("v1"))
//					return "http://schemas.openehr.org/v1";
//				else if (prefix.equals("xsi"))
//					return "http://www.w3.org/2001/XMLSchema-instance";
//				else
//					return XMLConstants.XML_NS_URI;
//			}
//
//			public String getPrefix(String namespace) {
//				if (namespace.equals("http://apache.org/foo"))
//					return "foo";
//				else if (namespace.equals("http://apache.org/bar"))
//					return "bar";
//				else
//					return null;
//			}
//
//			public void reset() {
//				// TODO Auto-generated method stub
//
//			}
//
//			public void pushContext() {
//				// TODO Auto-generated method stub
//
//			}
//
//			public void popContext() {
//				// TODO Auto-generated method stub
//
//			}
//
//			public String getURI(String arg0) {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//			public int getDeclaredPrefixCount() {
//				// TODO Auto-generated method stub
//				return 0;
//			}
//
//			public String getDeclaredPrefixAt(int arg0) {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//			public Enumeration getAllPrefixes() {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//			public boolean declarePrefix(String arg0, String arg1) {
//				// TODO Auto-generated method stub
//				return false;
//			}
//
//			public Iterator getPrefixes(String arg0) {
//				// TODO Auto-generated method stub
//				return null;
//			}
//		};
//		javax.xml.xpath.XPathFactory factory = javax.xml.xpath.XPathFactory
//				.newInstance();
//		XPath xpath = factory.newXPath();
//		xpath.setNamespaceContext((javax.xml.namespace.NamespaceContext) nsc);
//		XPathExpression defCodeExpr = xpath.compile("//v1:defining_code");
//		XPathExpression terminologyExpr = xpath
//				.compile("v1:terminology_id/v1:value");
//		XPathExpression codeExpr = xpath.compile("v1:code_string/text()");
//
//		NodeList nodeList = (NodeList) defCodeExpr.evaluate(data,
//				XPathConstants.NODESET);
//		for (int i = 0; i < nodeList.getLength(); i++) {
//			Node item = nodeList.item(i);
//			if (terminologyExpr.evaluate(item).equals("SNOMED-CT")) {
//				Node n = (Node) codeExpr.evaluate(item, XPathConstants.NODE);
//				// Node cs = n.getChildNodes().item(0);
//				String code_string = n.getNodeValue();
//				System.out.println(code_string);
//
//				ClientResource uriResource = new ClientResource(
//						"http://localhost:8183/getExpressionID?exp="
//								+ code_string);
//				try {
//					uriResource.get();
//					if (uriResource.getStatus().isSuccess()
//							&& uriResource.getResponseEntity().isAvailable())
//						n.setNodeValue(uriResource.getResponseEntity()
//								.getText());
//				} catch (Exception e) {
//					System.out
//							.println("URIProcessor.processURI(): Exception caught");
//					continue;
//				}
//
//			}
//
//		}
//
//		return new VersionedObjectListItem<Node>(
//				"test2-id-will-be-replaced-anyway-due-to-creation",
//				VersionableObjectType.COMPOSITION,
//				VersionLifecycleState.complete, AuditChangeType.creation, data);
//	}

}