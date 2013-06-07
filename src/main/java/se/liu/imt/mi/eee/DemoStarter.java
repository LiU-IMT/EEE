package se.liu.imt.mi.eee;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.ser1.stomp.Stomp;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.NotImplementedException;
import org.openehr.binding.XMLBinding;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.CacheDirective;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.log.AccessLogFormatter;
import org.restlet.resource.Directory;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MapVerifier;

import se.liu.imt.mi.eee.bm.BookmarkRouter;
import se.liu.imt.mi.eee.cb.ContributionBuilderRouter;
import se.liu.imt.mi.eee.db.DatabaseInterface.DatabaseMode;
import se.liu.imt.mi.eee.db.EHRDatabaseReadInterface;
import se.liu.imt.mi.eee.db.EHRDatabaseWriteInterface;
import se.liu.imt.mi.eee.db.xmldb.BookmarkStorageInXMLDB;
import se.liu.imt.mi.eee.db.xmldb.ContributionBuilderStorageInXMLDB;
import se.liu.imt.mi.eee.db.xmldb.EHRXMLDBHandler;
import se.liu.imt.mi.eee.db.xmldb.QueryStorageInXMLDB;
import se.liu.imt.mi.eee.db.xmldb.XMLDBHelper;
import se.liu.imt.mi.eee.db.xmldb.exist.ExistXMLDBHelper;
//import se.liu.imt.mi.eee.db.xmldb.sedna.SednaXMLDBHelper;
import se.liu.imt.mi.eee.ehr.EHRMultiRouter;
import se.liu.imt.mi.eee.ehr.EHRRouter;
import se.liu.imt.mi.eee.ehr.IdentifiedEHRRouter;
import se.liu.imt.mi.eee.structure.EEEConstants;
import se.liu.imt.mi.eee.translators.AqlToXqueryTranslator;
import se.liu.imt.mi.eee.translators.QueryTranslator;
import se.liu.imt.mi.eee.translators.XqueryAqlHybridToXqueryTranslator;
import se.liu.imt.mi.eee.trigger.ContributionTrigger;
import se.liu.imt.mi.eee.trigger.ContributionTriggerImplementationJSONviaSTOMP;
import se.liu.imt.mi.eee.utils.CacheControlSettingsFilter;
import se.liu.imt.mi.eee.utils.HashUtilResource;
import se.liu.imt.mi.eee.utils.InfoRestlet;
import se.liu.imt.mi.eee.utils.TraceResource;
import se.liu.imt.mi.eee.utils.Util;
import freemarker.template.DefaultObjectWrapper;

/**
 * Regarding paths, see ch 11 of AO: http://www.openehr.org/releases/1.0.1/html/architecture/overview/Output/paths_and_locators.html#1123890
 * Regarding versions, see ch 6 of Common IM: http://www.openehr.org/releases/1.0.1/architecture/rm/common_im.pdf
 * 
 * You can start this class via MAven by issuing the command:
 * mvn exec:java -Dexec.mainClass="se.liu.imt.mi.eee.DemoStarter" 
 * 
 * @author Erik Sundvall, Martin Eneling, Daniel Karlsson, Mikael Nyström, Marie Sandström, Department of Biomedical Engineering, Linköping University, Sweden 
 */
public class DemoStarter implements EEEConstants {
	
	public static final String USE_ETAG_FOR_INDIVIDUAL_DATA = "useETagForIndividualData";
	public static final String IDENTIFIED_VERSIONS_MAXAGE = "identifiedVersionsMaxage";
	
	private static int staticDirectroyMaxage = 8640000; // 60*60*24*100=8640000; // 100 Days
	private static int defaultDirectoryMaxage = 60; // 60 Seconds
	private static Integer identifiedVersionsMaxage;
	private static boolean useETagForIndividualData;

	final public static String DEFAULT_HTTP_REALM = "LiU EEE"; 
	
	final private static String defaultPropertiesFile = "restserver.properties"; 

	static String wwwFileDir, wwwStaticFileDir, freemarkerTemplateFileDir;
	static String appLogFile, accessLogFile; 
	
	private static Level appLogLevel;
	private static Configuration config;

	public static Configuration getConfig(){
		return config;
	}

	public static void main(String[] args) throws Exception {		
		
		// *********** CONFIG FILE HANDLING **********************

		String settingsFileName = null;
		// create Options object
		Options options = new Options();
	
		// add t option
		options.addOption("p", true, "provide properties file");
		options.addOption("h", "help", false, "print help statement");
		
		// create CLI parser
		CommandLineParser parser = new GnuParser();
		CommandLine line = null;
	    try {
	        // parse the command line arguments
	        line = parser.parse(options, args);
	    }
	    catch(ParseException exp) {
	        // oops, something went wrong	    	
	        System.err.println("Parsing of commandline arguments failed. Reason: " + exp.getMessage());
	        System.exit(-1);
	    }
		
	    if(line.hasOption("help")) {
	    	HelpFormatter formatter = new HelpFormatter();
	    	formatter.printHelp("EHRTestRestStarter", options);
	    	System.exit(0);
	    }
	    
		int defaultPortNumber = 0;
		String systemID = "not initialized";
		net.ser1.stomp.Server contributionTriggerHandlerServer = null;
		Stomp stompContributionTriggerSenderClient = null;

		try {
			// Load properties from configuration file using Commons Configuration
			settingsFileName = line.hasOption("p")?line.getOptionValue("p"):defaultPropertiesFile;
			config = new PropertiesConfiguration(settingsFileName);

			// Set port number from config file
			defaultPortNumber = config.getInt("defaultPortNumber");

			// Set logs from config file using absolute or relative path
			appLogFile = config.getString("appLogFile");
			appLogLevel = Level.parse(config.getString("appLogLevel"));
			accessLogFile = config.getString("accessLogFile");

			// Set wwwFileDir from config file using absolute or relative path
			wwwFileDir = new File(config.getString("wwwFileDir")).toURI().toString();

			// Set wwwFileDir from config file using absolute or relative path
			wwwStaticFileDir = new File(config.getString("wwwStaticFileDir")).toURI().toString();
		
			// Start up simple Gozirra STOMP server to be used as contribution trigger handler
			if(config.getBoolean("triggerServer_EnableBuiltin")) {
				int port = config.getInt("triggerServer_port");
				contributionTriggerHandlerServer = new net.ser1.stomp.Server(port);				
				System.out.println("EHRTestRestStarter.main reports: \n" +
						"  Done starting built in STOMP server (Gozirra) on tcp port "+port+"\n"+
						"  See specification at http://stomp.github.com/stomp-specification-1.0.html \n"+
						"  Can be tested in many operating systems by running: telnet localhost "+port);						
			}
			
			String database = config.getString("database");
	
			if (!database.equals("eXist")) {
				throw new NotImplementedException("The only presently verified database is eXist. Other databases might get reenabled or added later.");
			}
			
			final XMLDBHelper dbHelperSingleRec = new ExistXMLDBHelper(config.getString("databaseHost"), config.getString("databaseName"), config.getString("databaseUser"), config.getString("databasePass"), DatabaseMode.SINGLE_RECORD);
			final XMLDBHelper dbHelperMultiRec = new ExistXMLDBHelper(config.getString("databaseHost"), config.getString("databaseName"), config.getString("databaseUser"), config.getString("databasePass"), DatabaseMode.MULTI_RECORD) ;

//			// TODO: Check if there is more readable construct for this than the ?:-operator (nicer for beginners)
//			final XMLDBHelper dbHelperSingleRec = (database.equals("eXist") ?
//					new ExistXMLDBHelper(config.getString("databaseHost"), config.getString("databaseName"), config.getString("databaseUser"), config.getString("databasePass"), DatabaseMode.SINGLE_RECORD) :
//						(database.equals("Sedna") ?
//								new SednaXMLDBHelper(config.getString("databaseHost"), config.getString("databaseName"), config.getString("databaseUser"), config.getString("databasePass"), DatabaseMode.SINGLE_RECORD) :
//									null));
//			
//			final XMLDBHelper dbHelperMultiRec = (database.equals("eXist") ?
//					new ExistXMLDBHelper(config.getString("databaseHost"), config.getString("databaseName"), config.getString("databaseUser"), config.getString("databasePass"), DatabaseMode.MULTI_RECORD) :
//						(database.equals("Sedna") ?
//								new SednaXMLDBHelper(config.getString("databaseHost"), config.getString("databaseName"), config.getString("databaseUser"), config.getString("databasePass"), DatabaseMode.MULTI_RECORD) :
//									null));
//			
			systemID = config.getString("systemID");
	
			String oetTemplateFileDir = config.getString("oetTemplateFileDir");
			String archetypeFileDir = config.getString("archetypeFileDir");

			ArchetypeAndTemplateRepository atRepo = new ArchetypeAndTemplateRepository(new File(archetypeFileDir), "adl", new File(oetTemplateFileDir), "oet");

			freemarkerTemplateFileDir = config.getString("freemarkerTemplateFileDir");
			
			// HTTP-cache-related
			staticDirectroyMaxage = config.getInt("staticDirectroyMaxage");
			defaultDirectoryMaxage = config.getInt("defaultDirectoryMaxage");
			identifiedVersionsMaxage = config.getInteger(IDENTIFIED_VERSIONS_MAXAGE, 604800); // 0*60*24*7=604800 // A week
			useETagForIndividualData = config.getBoolean(USE_ETAG_FOR_INDIVIDUAL_DATA, true);
			
			// *********** SET UP MAIN RESTLET **********************
			
			// Create a new Restlet component and add a HTTP server connector to it
			// By making it a WADL component it can produce a WADL service description too
			Component component = new Component();
			
			// TODO: check how to resolve the connection shortage (e.g. by using later restlet version), then remove the "maxTotalConnections" stuff in this file
			component.getContext().getParameters().add("maxTotalConnections", "100");
			
//			// Http cache related
//			component.getContext().getAttributes().put(IDENTIFIED_VERSIONS_MAXAGE, identifiedVersionsMaxage);
//			component.getContext().getAttributes().put(USE_ETAG_FOR_INDIVIDUAL_DATA, useETagForIndividualData);

			// allow the calling using the HTTP protocol 
			component.getClients().add(Protocol.HTTP);
			
			// ************ LOGGING *****************
			Logger logger = component.getContext().getLogger();
			int LIMIT_BYTES = 10*1000*1000; // 10MB
			int COUNT = 99; boolean APPEND = true; // TODO move to config file?

			FileHandler appFH = new FileHandler(appLogFile, LIMIT_BYTES, COUNT, APPEND);

		    appFH.setLevel(appLogLevel);

		    logger.addHandler(appFH);

			FileHandler accessFH = new FileHandler(accessLogFile, LIMIT_BYTES, COUNT, APPEND);
			accessFH.setLevel(Level.INFO);
		    AccessLogFormatter alf = new AccessLogFormatter(); 
		    accessFH.setFormatter(alf);
		    logger.addHandler(accessFH);

		    logger.fine("Starts logging using logger in "+logger.getName());


			Context sharedContext = component.getContext().createChildContext();
			
//			WadlComponent component = new WadlComponent();			   
//			component.setAuthor("Medical informatics group, Department of Biomedical Engineering, Linköping University");
//			component.setName("LiU EEE");
//			component.setDescription("LiU EEE is an Educational EHR Environment based on openEHR and REST architecture.");
			
			Context initialServerContext = new Context();
			initialServerContext.getParameters().add("maxTotalConnections", "100");
			Server server = new Server(initialServerContext, Protocol.HTTP, defaultPortNumber, new Restlet() {}); // FIXME: check if new Restlet() in constructor can be avoided
			component.getServers().add(server);			
			component.getClients().add(Protocol.FILE);
			
			// *********** SET UP SOME GLOBAL OBJECT INSTANCES AND PUT IN CONTEXT ***********
			freemarker.template.Configuration freemarkerConfig = new freemarker.template.Configuration();
			// Directory where the template files come from.
			freemarkerConfig.setDirectoryForTemplateLoading(new File(freemarkerTemplateFileDir));
			// How templates will see the data model.
			freemarkerConfig.setObjectWrapper(new DefaultObjectWrapper());
			sharedContext.getAttributes().put(EEEConstants.KEY_TO_FREEMARKER_CONFIGURATION, freemarkerConfig);

			XMLBinding xmlBinding = Util.setUpXMLBinding("sv");
			sharedContext.getAttributes().put(EEEConstants.KEY_TO_XML_BINDING, xmlBinding);
			
			sharedContext.getAttributes().put(EEEConstants.KEY_TO_AT_REPO, atRepo );
			

//			// Http cache related
//			sharedContext.getAttributes().put(IDENTIFIED_VERSIONS_MAXAGE, identifiedVersionsMaxage);
//			sharedContext.getAttributes().put(USE_ETAG_FOR_INDIVIDUAL_DATA, useETagForIndividualData);
			
			// *********** TRIGGER HANDLING **********************
			ContributionTrigger contributionTrigger = null;
			
			if (config.getBoolean("ContributionTrigger_active")) {
				// Start up simple Gozirra STOMP client (used for sending triggers)
				String destination = config.getString("ContributionTrigger_destination");
				int	timeOutInMilliseconds = config.getInt("ContributionTrigger_timeOutInMilliseconds");
				Stomp stompDebugListeningClient = null;
				
				if (config.getBoolean("ContributionTrigger_EnableInProccesCommunication")) {
					// Using in process communication
					if (contributionTriggerHandlerServer == null)
						throw new ResourceException(
								Status.SERVER_ERROR_INTERNAL,
								"Error in EEE configuration: you must set triggerServer_EnableBuiltin to true if you want to set ContributionTrigger_EnableInProccesCommunication to true.");
					stompContributionTriggerSenderClient = contributionTriggerHandlerServer.getClient();
					contributionTrigger = new ContributionTriggerImplementationJSONviaSTOMP(stompContributionTriggerSenderClient, timeOutInMilliseconds, destination);
					System.out.println("Done starting in process STOMP client (Gozirra) for Contribution trigger handler\n");
					logger.fine("Done starting in process STOMP client (Gozirra) for Contribution trigger handler\n");
					
					if (config.getBoolean("enableDebugTriggerListener")) {
						stompDebugListeningClient = contributionTriggerHandlerServer.getClient();
					}
					
				} else {
					// Using network communication
					String servername = config
							.getString("ContributionTrigger_server");
					int port = config.getInt("ContributionTrigger_serverPort");
					stompContributionTriggerSenderClient = new net.ser1.stomp.Client("localhost", 61626,
							"ser", "ser"); // TODO: Make login/pw for sender configurable
					contributionTrigger = new ContributionTriggerImplementationJSONviaSTOMP(
							config.getString("ContributionTrigger_server"), //server
							config.getInt("ContributionTrigger_serverPort"), //port
							config.getString("ContributionTrigger_login"), //login
							config.getString("ContributionTrigger_passcode"), //passcode
							timeOutInMilliseconds, 
							destination
					);
					logger.info("EHRTestRestStarter.main reports: \n"
							+ " Done starting networked STOMP client (Gozirra) for Contribution trigger handler available at server "
							+ servername + " on tcp port " + port
							+ "\n");
					if (config.getBoolean("enableDebugTriggerListener")) {
						stompDebugListeningClient = new net.ser1.stomp.Client(
								config.getString("ContributionTrigger_server"), //server
								config.getInt("ContributionTrigger_serverPort"), //port
								"ser", "ser"); // TODO: Make login/pw for debug listener configurable
					}

				}
				
				// Start up simple Gozirra STOMP listening debug client
				if (config.getBoolean("enableDebugTriggerListener")) {
				stompDebugListeningClient.subscribe( config.getString("ContributionTrigger_destination"), new net.ser1.stomp.Listener() {
				    public void message( Map header, String body ) {
				       System.out.println("Stomp client recieved headers:"+header.toString());
				       System.out.println("Stomp client recieved body:"+body.toString());
				      }
				    }  );
				stompDebugListeningClient.commit();
				System.out.println("EHRTestRestStarter.main() the enableDebugTriggerListener was set to 'true' in the config file");
				System.out.println(	"thus a debug STOMP listener will print stuff to this console. Have fun! "+stompDebugListeningClient.toString());
				System.out.println();
				}

			}		
			
			// *********** EHR HANDLING (shared stuff) **********************
			
			Map<String, QueryTranslator> queryTranslatorMap;
			queryTranslatorMap = new TreeMap<String, QueryTranslator>();
			queryTranslatorMap.put("AQL", new AqlToXqueryTranslator());
			//queryTranslatorMap.put("AQL-XML-debug", new AqlToXmlParseTreeTranslator());
			queryTranslatorMap.put("XQuery", new XqueryAqlHybridToXqueryTranslator());

			// TODO: ??? Change constructor call of ehrDBHandlerSingle below
		
			
			
			// *********** SINGLE EHR HANDLING **********************

			// *** Simple demo user authentication (not intended for production systems) ***
			// Create a simple password verifier for "normal" day to day clinical usage
			MapVerifier verifier = new MapVerifier();
			setupMapVerifier(verifier, "user.r");
			setupMapVerifier(verifier, "user.rw");

			
			// Set up an application for working with records one at a time (normal clinical use)
			final EHRXMLDBHandler ehrDBHandlerSingle = new EHRXMLDBHandler(true, dbHelperSingleRec, "EHR", "Contributions", contributionTrigger, xmlBinding);		
			EHRDatabaseReadInterface<?,?> basicDBReadSingle = ehrDBHandlerSingle;
			EHRDatabaseWriteInterface<?> basicDBWriteSingle = ehrDBHandlerSingle;
			Context singleContext = component.getContext().createChildContext();
			singleContext.getAttributes().put(EEEConstants.KEY_TO_FREEMARKER_CONFIGURATION, freemarkerConfig);
			Application ehrSingleRecordApplication = new EHRRouter(singleContext);

			// TODO: check if "systemID" really needs to be passed to the following or if it 
			//       is in the context and childcontext already
			Application ehrSingleIdentifiedRecordApplication = new IdentifiedEHRRouter(singleContext, systemID, basicDBReadSingle, basicDBWriteSingle, dbHelperSingleRec, queryTranslatorMap, xmlBinding);

			// TODO: perhaps put stompCLient (or null) into server context KEY_TO_CONTRIBUTION_TRIGGER if needed by other things than ehrDBHandlerSingle (that already got it in the call)
			//singleContext.getAttributes().put(EEEConstants.KEY_TO_CONTRIBUTION_TRIGGER, contributionTrigger);
			// For now contributionTrigger is the cache itself... TODO: Change if/when cache has been separated from trigger
			singleContext.getAttributes().put(EEEConstants.KEY_TO_EHR_METADATA_CACHE, contributionTrigger); // TODO: possibly add for multiContext too...


			// **** Single query storage ***
			singleContext.getAttributes().put(EEEConstants.KEY_TO_QUERY_STORAGE, new QueryStorageInXMLDB(dbHelperSingleRec));
		
			// Attach the applications to the component and start them
			component.getDefaultHost().attach("/ehr:{"+EEEConstants.EHR_ID+"}/", guardThis(ehrSingleIdentifiedRecordApplication, verifier, singleContext), Template.MODE_STARTS_WITH);
			component.getDefaultHost().attach("/ehr", guardThis(ehrSingleRecordApplication, verifier, singleContext), Template.MODE_STARTS_WITH);

			
			// *********** MULTI EHR HANDLING **********************

			// Create a simple password verifier for epidemiology/research users that 
			// need to query multiple records.	
			MapVerifier multiVerifier = new MapVerifier();
			setupMapVerifier(multiVerifier, "user.rw");

			// Set up an application for working with multiple records at once (epidemilogy, statistical use etc)
			final EHRXMLDBHandler ehrDBHandlerMulti = new EHRXMLDBHandler(true, dbHelperMultiRec, "EHR", "Contributions", null, xmlBinding);		
			EHRDatabaseReadInterface<?,?> basicDBReadMulti = ehrDBHandlerMulti;
			EHRDatabaseWriteInterface<?> basicDBWriteMulti = ehrDBHandlerMulti;
			
			Context multiContext = component.getContext().createChildContext();
			multiContext.getAttributes().put(EEEConstants.KEY_TO_FREEMARKER_CONFIGURATION, freemarkerConfig);
			multiContext.getAttributes().put(EEEConstants.KEY_TO_EHR_METADATA_CACHE, contributionTrigger); // TODO: don't reuse for multiContext if not same DB as single!
			Application ehrMultiRecordApplication = new EHRMultiRouter(multiContext, dbHelperMultiRec);
			Application ehrMultiIdentifiedRecordApplication = new IdentifiedEHRRouter(multiContext, systemID, basicDBReadMulti, basicDBWriteMulti, dbHelperMultiRec, queryTranslatorMap, xmlBinding);

			// **** Multi query storage ***
			// Currently the same storage as for single query, but could be separated in some deployments.
			multiContext.getAttributes().put(EEEConstants.KEY_TO_QUERY_STORAGE, new QueryStorageInXMLDB(dbHelperSingleRec));
			
			component.getDefaultHost().attach("/multi/ehr:{"+EEEConstants.EHR_ID+"}/", guardThis(ehrMultiIdentifiedRecordApplication,  multiVerifier, multiContext), Template.MODE_STARTS_WITH);
			component.getDefaultHost().attach("/multi/ehr", guardThis(ehrMultiRecordApplication,  multiVerifier, multiContext));

			// *********** CONTRIBUTION BUILDER HANDLING ***		
			MapVerifier cbVerifier = new MapVerifier();
			setupMapVerifier(cbVerifier, "user.rw");
			
			ContributionBuilderStorageInXMLDB contrBuilderStorage = new ContributionBuilderStorageInXMLDB(dbHelperSingleRec, xmlBinding);
			Application contributionBuilderApplication = new ContributionBuilderRouter(singleContext, systemID, contrBuilderStorage);
			contributionBuilderApplication.getContext().getAttributes().put(EEEConstants.KEY_TO_AT_REPO, atRepo );

			// Allow http GET tunneling of other http methods TODO: re-enable?
//			contributionBuilderApplication.getTunnelService().setEnabled(true);
//			contributionBuilderApplication.getTunnelService().setMethodTunnel(true);
//			component.getDefaultHost().attach("/cb", guardThis(contributionBuilderApplication, cbVerifier, sharedContext, "EEE Contribution Builder"));
			component.getDefaultHost().attach("/cb", guardThis(contributionBuilderApplication, cbVerifier, sharedContext));


			// *********** BOOKMARK HANDLING **********************
			sharedContext.getAttributes().put(EEEConstants.KEY_TO_BOOKMARK_STORAGE, new BookmarkStorageInXMLDB(dbHelperSingleRec));
			BookmarkRouter bookmarkApplication = new BookmarkRouter(sharedContext, systemID);
			component.getDefaultHost().attach("/bm", guardThis(bookmarkApplication, verifier, sharedContext));

			// *********** USER PAGE HANDLING **********************
			InfoRestlet userPage = new InfoRestlet("UserPage", sharedContext);
			component.getDefaultHost().attach("/user/", guardThis(userPage, verifier, sharedContext));
			InfoRestlet userIdentifiedPage = new InfoRestlet("UserIdentifiedPage", sharedContext);
			component.getDefaultHost().attach("/user/{"+USER_ID+"}/", guardThis(userIdentifiedPage, verifier, sharedContext));

			// *********** INSTANCE BUILDER HANDLING **********************
			// FIXME: Create and connect Instance Builder/generator using config variables oetTemplateFileDir & archetypeFileDir
			InfoRestlet ibPage = new InfoRestlet("InstanceBuilder", sharedContext);
			component.getDefaultHost().attach("/ib/", guardThis(ibPage, verifier, sharedContext));
			
			// component.getDefaultHost().attach("/ib" or "/util/ib", ... ); ../ib/out/SHA-1 (skapa bara ny om det inte redan finns annars fel-status)

			// *********** Utility resources  **********************	
			Router utilRouter = new Router(singleContext);
			//utilRouter.attach("/hash/{"+EEEConstants.COMMAND+"}/{"+EEEConstants.DATA+"}/", HashUtilResource.class);			
			utilRouter.attach("/hash/", HashUtilResource.class);
			utilRouter.attach("/trace", TraceResource.class);
			component.getDefaultHost().attach("/util", utilRouter);		
			//System.out.println("DemoStarter.main(2) utilRouter="+utilRouter);		

			// *********** STATIC FILE DIRECTORY **********************

			// Create a directory able to expose a hierarchy of static files that are allowed to be cached in public using a "Cache-Control: public" header
			
			Directory fileServingDirectory = new Directory(singleContext, wwwStaticFileDir);
			fileServingDirectory.setDescription("Directory containing static (non changing) files. Browsers are via HTTP headers encouraged to cache these files for "+staticDirectroyMaxage+" seconds.");
			fileServingDirectory.setName("Static File Directory");
			fileServingDirectory.setListingAllowed(true);

			List<CacheDirective> cacheDirectives = new ArrayList<CacheDirective>();
			cacheDirectives.add(CacheDirective.maxAge(staticDirectroyMaxage)); 
			cacheDirectives.add(CacheDirective.sharedMaxAge(staticDirectroyMaxage));
			cacheDirectives.add(CacheDirective.publicInfo()); 
			CacheControlSettingsFilter cacheFilteredfileServingDirectory = new CacheControlSettingsFilter(sharedContext, fileServingDirectory, cacheDirectives, staticDirectroyMaxage);
			component.getDefaultHost().attach("/static", cacheFilteredfileServingDirectory);

			
			// *********** ROOT FILE DIRECTORY **********************

			// Create a directory able to expose a hierarchy of files
			Directory rootFileDirectory = new Directory(singleContext, wwwFileDir);
			rootFileDirectory.setDescription("Directory containing files");
			rootFileDirectory.setName("Default Root File Directory");
			rootFileDirectory.setListingAllowed(true);

			//List<CacheDirective> 
			cacheDirectives = new ArrayList<CacheDirective>();
			cacheDirectives.add(CacheDirective.maxAge(defaultDirectoryMaxage));
			cacheFilteredfileServingDirectory = new CacheControlSettingsFilter(sharedContext, rootFileDirectory, cacheDirectives, defaultDirectoryMaxage);
			component.getDefaultHost().attach("", cacheFilteredfileServingDirectory);

			// *********** TRACE (for tests) **********************			
			component.getDefaultHost().attach("/trace", TraceResource.class);

			// The fileServingDirectory and /trace should not require user authentication 
			// in a demo setup, but authentication may be suitable in some production setups
			
			component.start();
			
		} catch (Exception e) {
		       System.err.println("Startup of EEE failed: \n" +e.getMessage());
		       System.err.println("  Aborting EEE startup. \n" +
		       		"  (Hint: Check your settings and that your chosen database engine has been started prior to starting EEE)\n"+
				"  Provided Settings file name: "+ settingsFileName);
		       System.exit(-1);
		}
		
		System.out.println("EHRTestRestStarter.main reports: \n" +
				"  Done attaching restlet routers. \n"+				
				"  Unique openEHR system ID: "+systemID+ "\n" +
				"  Settings were fetched from the file named: "+ settingsFileName + "\n" +
				"  Web server started on port "+defaultPortNumber+"\n" +
			    "  Try accessing the server on any of the following URIs: " + "\n" +
			    "     http://localhost:"+ defaultPortNumber + "/"
				);
		
	    Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

	         while(e.hasMoreElements()) {
	            NetworkInterface ni = (NetworkInterface) e.nextElement();
	            System.out.print("     Net i/f "+ni.getName()+"  >>");

	            Enumeration<InetAddress> e2 = ni.getInetAddresses();

	            while (e2.hasMoreElements()){
	               InetAddress ip = (InetAddress) e2.nextElement();
	               System.out.print("  http:/"+ ip.toString()+":"+ defaultPortNumber +"/");
	            }
	            System.out.println();
	         } 
		
		
	}

	protected static void setupMapVerifier(MapVerifier verifier, String configPrefix) {
		// Users with write access are read from the configuration file (default filename: restserver.properties)
		Configuration userAndPw = getConfig().subset(configPrefix);
		Iterator<String> usernames = userAndPw.getKeys();
		while (usernames.hasNext()) {
			String username = (String) usernames.next();
			//System.out.println("setupMapVerifier()"+username+"="+userAndPw.getString(username));
			verifier.getLocalSecrets().put(username, userAndPw.getString(username).toCharArray());
		}
	}


	private static ChallengeAuthenticator guardThis(Class theThingToGuard,
			MapVerifier verifier, Context context) {
		// Create a guard
		ChallengeAuthenticator guard = new ChallengeAuthenticator(
		    context, ChallengeScheme.HTTP_BASIC, DEFAULT_HTTP_REALM);
		guard.setVerifier(verifier);
		guard.setNext(theThingToGuard);
		return guard;
	}
	
	private static ChallengeAuthenticator guardThis(Restlet theThingToGuard,
			MapVerifier verifier, Context context) {
		// Create a guard
		ChallengeAuthenticator guard = new ChallengeAuthenticator(
		    context, ChallengeScheme.HTTP_BASIC, DEFAULT_HTTP_REALM);
		guard.setVerifier(verifier);
		guard.setNext(theThingToGuard);
		return guard;
	}

	public static String getWwwFileDir() {
		return wwwFileDir;
	}

}
