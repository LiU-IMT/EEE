package se.liu.imt.mi.eee.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.NotImplementedException;
import org.openehr.rm.common.changecontrol.Contribution;
import org.openehr.rm.common.generic.PartyIdentified;
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
 * This class should be used to load patient data into EEE.
 * If you run this file in Eclipse you will get a prompt where you should enter two arguments to the method:
 * ehrId and directoryWithPatientData. Use the following syntax:
 * "GTT_c1" "resources/www-file-root/GTT/c1_transfusion" (white space between arguments)
 * where "GTT_c1" is the ehrId and the rest is the directoryWithPatientData.
 * 
 * See also example usage of this file in LoadGTT.java.
 *
 * TODO: Everything works fine when running with eXist database, but when using
 * 		 Sedna, a thread is not terminated.
 * 
 * @author Marie, Daniel, Erik
 */
public class LoadPatientData {
	private static String COMMITTER_NAME = "EEE Testcase Import Service";
	private static String SYSTEM_ID = "test1.eee.mi.imt.liu.se";
	private static PartyIdentified COMMITTER
		= new PartyIdentified(null, COMMITTER_NAME, null);

	// If ignoreBadFiles is true, we will skip bad files and try the next one, 
	//only works if also createSeparateContributionForEachFile = true
	private static boolean IGNORE_BAD_FILES = false;
	private static boolean CREATE_SEPARATE_CONTRIBUTION_FOR_EACH_FILE = true;	

	private static EHRXMLDBHandler ehrDatabaseHandler;

	public static void main(String args[]) throws Exception {
		if (args.length != 2) {
			throw new Exception("Wrong number of arguments. Two input arguments are required;" +
					" ehrId and directory to files. Separate the arguments with a colon in the input dialog box.");
		}

		String ehrId = args[0];
		String directoryWithPatientData = args[1];
		
		LoadPatientData patientDataLoader = new LoadPatientData();
		patientDataLoader.loadPatientData(ehrId, directoryWithPatientData);
	}
	
	/**
	 * See comment above for entire class.
	 * 
	 * @param ehrId							e.g. "GTT_c1"
	 * @param directoryWithPatientData		e.g. "resources/www-file-root/GTT/c1_transfusion"
	 */
	public void loadPatientData(String ehrId, String directoryWithPatientData) throws Exception{

		// TODO: This code is also used in EHRTestRestStarter.main(). Refactor so
		// 		 the code is not duplicated!
		// TODO: Add to the config file a possibility to use this Mockup:
		//
		// To just test XML validity etc, uncomment line below...
		//		ehrDatabaseHandler = new EHRXMLDBHandler(true,
		//												 new MockupXMLDBHelper(),
		//												 DatabaseMode.SINGLE_RECORD);

		// Load properties from configuration file using Commons Configuration
		Configuration config = new PropertiesConfiguration("restserver.properties");
		String database = config.getString("database");
		
		if (!database.equals("eXist")) {
			throw new NotImplementedException("The only presently verified database is eXist. Other databases might get reenabled or added later.");
		}
			
		final XMLDBHelper dbHelper = new ExistXMLDBHelper(config.getString("databaseHost"), config.getString("databaseName"), config.getString("databaseUser"), config.getString("databasePass"), DatabaseMode.SINGLE_RECORD);

// TODO: Create parametric DBHelper classloading based on reusable config file settings instead later...
//		final XMLDBHelper dbHelper = (database.equals("eXist") ?
//				new ExistXMLDBHelper(config.getString("databaseHost"), config.getString("databaseName"), config.getString("databaseUser"), config.getString("databasePass"), DatabaseMode.SINGLE_RECORD) :
//					(database.equals("Sedna") ?
//							new SednaXMLDBHelper(config.getString("databaseHost"), config.getString("databaseName"), config.getString("databaseUser"), config.getString("databasePass"), DatabaseMode.SINGLE_RECORD) :
//								null));
//		
		//TODO: Split to reader and writer according to the new interfaces...
		ehrDatabaseHandler = new EHRXMLDBHandler(true, dbHelper, "EHR", "Contributions", null, Util.setUpXMLBinding("sv"));
		
		
		cleanEhrForPatientAndLoadFiles(dbHelper, ehrId, directoryWithPatientData);
	}

	private void cleanEhrForPatientAndLoadFiles(XMLDBHelper dbHelper,
						   						String ehrId,
						   						String directoryWithPatientData)
	throws Exception {
		System.out.println("EhrId: " + ehrId);
		System.out.println("Directory: " + directoryWithPatientData);
		removeOldEhrAndCreateNewEmptyEhrForPatient(dbHelper, ehrId);
		File c1 = new File(directoryWithPatientData);
		loadFilesInDirectory(c1, ehrId);
		System.out.println("Finished loading files!");
		System.out.println("-------");
	}

	private void removeOldEhrAndCreateNewEmptyEhrForPatient(XMLDBHelper dbHelper, String ehrId) throws Exception {
		Collection EHR = dbHelper.getRootCollection().getChildCollection("EHR");
		Resource res = EHR.getResource(ehrId);
		if (res != null) {
			EHR.removeResource(res);		
		}
		ehrDatabaseHandler.createEHR(ehrId, SYSTEM_ID);
	}

	private void loadFilesInDirectory(File directory, String ehrId) throws Exception {
		if (!directory.isDirectory()) {
			throw new Exception("The path given is not a directory. Directory given: " + directory.toString());
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
				return pathname.getName().endsWith("xml") || pathname.getName().endsWith("XML");
			}
		});	
		return fileList;
	}
		
	private void loadFilesNotOneContributionPerDocument(File[] fileList, String ehrId) throws Exception {
		List<VersionedObjectListItem<Node>> objects
			= createListForSingleContributionOfAllDocuments(fileList);
		Contribution returnedContribution = commitSingleContribution(objects, ehrId);
		if (fileList.length != returnedContribution.getVersions().size()){
			throw new Exception("Filelist length does not correspond to the returnedContribution version size.");
		}
		System.out.println("PatientDataLoader.loadPatientData() finished a combined contribution: "
							+ returnedContribution.toString());
	}

	private Contribution commitSingleContribution(
			List<VersionedObjectListItem<Node>> objects, 
			String ehrId)
	throws Exception {
		Contribution returnedContribution
		=  ehrDatabaseHandler.commitContributionOfOriginalVersions(COMMITTER,
																   ehrId,
																   SYSTEM_ID,
																   objects,
																   null, null);
		return returnedContribution;
	}

	private List<VersionedObjectListItem<Node>>
		createListForSingleContributionOfAllDocuments(File[] fileList) throws Exception {
		List<VersionedObjectListItem<Node>> objects
			= new ArrayList<VersionedObjectListItem<Node>>();		
		for (int i = 0; i < fileList.length; i++) {
			System.out.println("PatientDataLoader.loadPatientData() Filename: "
								+ fileList[i].getAbsolutePath());
			objects.add(generateCompositionItem_creation_complete(fileList[i]));
		}
		return objects;
	}

	
	private void loadFilesOneContributionPerDocument(File[] fileList, String ehrId)
	throws Exception {
		List<VersionedObjectListItem<Node>> objects
			= new ArrayList<VersionedObjectListItem<Node>>();
		for (int i = 0; i < fileList.length; i++) {
			System.out.println("PatientDataLoader.loadPatientData() Filename: "
								+ fileList[i].getAbsolutePath());
			objects.clear();
			objects.add(generateCompositionItem_creation_complete(fileList[i]));
			try {
				Contribution returnedContribution = commitSingleContribution(objects, ehrId);
				if (returnedContribution.getVersions().size() != 1) {
					throw new Exception("Size of returnedContribution versions is not 1.");
				}
				//assertEquals(1, returnedContribution.getVersions().size());
				System.out.println("PatientDataLoader.loadPatientData() SUCCESSFUL STORAGE: "
									+ fileList[i]);
			} catch (Exception e) {
				if (IGNORE_BAD_FILES) {
					System.out
							.println("PatientDataLoader.loadPatientData() FAILED TO STORE: "
									+ fileList[i]
									+ "\nSkipping to next file");
				} else {
					e.printStackTrace();
					throw e;
				}
			}
		}
	}
	
	private VersionedObjectListItem<Node> generateCompositionItem_creation_complete(File file)
	throws Exception {
		Node data = LoadComposition.loadCompositionFromFile(file).getDomNode();
		return new VersionedObjectListItem<Node>(
				"test2-id-will-be-replaced-anyway-due-to-creation", 
				VersionableObjectType.COMPOSITION, 
				VersionLifecycleState.complete, 
				AuditChangeType.creation, 
				data);
	}

}