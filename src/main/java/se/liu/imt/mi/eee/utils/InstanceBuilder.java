package se.liu.imt.mi.eee.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import openEHR.v1.template.TEMPLATE;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.template.Flattener;
import org.openehr.am.template.FlatteningException;
import org.openehr.am.template.OETParser;
import org.openehr.binding.XMLBinding;
import org.openehr.binding.XMLBindingException;
import org.openehr.rm.util.GenerationStrategy;
import org.openehr.rm.util.SkeletonGenerator;
import org.openehr.schemas.v1.COMPOSITION;
import org.openehr.schemas.v1.CompositionDocument;

import se.acode.openehr.parser.ADLParser;
import se.liu.imt.mi.eee.ArchetypeAndTemplateRepository;

/**
 * Class is used to generate a skeleton XML RM instance from a template file in
 * OET-format and template and archetype repositories.
 * 
 * main() arguments: LoadTemplateAndGenerateXML <oet-file> -a <archetype repo>
 * -t <template repo> -o <output file>
 * 
 * The output file is named "output.xml" is no other name is given.
 * 
 * Example from command line :
 * 
 * LoadTemplateAndGenerateXML
 *   resources/www-file-root/LoadAndGenerateTestResources/templates/episode.oet
 *   -a resources/www-file-root/LoadAndGenerateTestResources/archetypes
 *   -t resources/www-file-root/LoadAndGenerateTestResources/templates
 *   
 * Example arguments in Eclipse Run Configuration:
 * 
 *   	"resources/www-file-root/aom/templates/GTT_m10_medication.oet" 
 *       -a "resources/www-file-root/aom/archetypes" 
 *       -t "resources/www-file-root/aom/templates" 
 *       -o "resources/www-file-root/example/ehr-content/generatedXMLRMInstanceSkeletons/output-test.xml"
 *       
 * @author danka, erisu
 * 
 */
public class InstanceBuilder {

	protected static final String adlExtension = "adl";
	protected static final String oetExtension = "oet";

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// configure logging
		BasicConfigurator.configure();

		// create Options object for CLI parsing
		Options options = new Options();

		// add options
		options.addOption("a", "archetypes", true, "archetype repository");
		options.addOption("t", "templates", true, "template repository");
		options.addOption("o", "output", true,
				"output file (default output.xml)");
		options.addOption("g", "gen-strategy", true,
				"generation strategy (default value "+ GenerationStrategy.MAXIMUM.name()
				+" other permitted values: "+ GenerationStrategy.MINIMUM.name()+", "+
				GenerationStrategy.MAXIMUM_EMPTY.name()+")"
				);
		options.addOption("h", "help", false, "print help statement");

		// create CLI parser
		CommandLineParser cliParser = new GnuParser();
		CommandLine line = null;

		File file = null;
		try {
			try {
				// parse the command line arguments
				line = cliParser.parse(options, args);
			} catch (ParseException exp) {
				// oops, something went wrong
				System.err.println("Parsing failed.  Reason: " + exp.getMessage());
				System.exit(-1);
			}

			if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("InstanceBuilder", options);
				System.exit(0);
			}

			// --- Check parameter availability ---
			if (!line.hasOption("a")) {
				throw new Exception("Archetype repository path missing");
			}
			if (!line.hasOption("t")) {
				throw new Exception("Template repository path missing");
			}
			
			// --- Assign variables ---
			
			String templateFilename = line.getArgs()[0]; // Reads template filename from arguments
			String archetypeRepoPath = line.getOptionValue("a");
			String templateRepoPath = line.getOptionValue("t");
			GenerationStrategy generationStrategy = GenerationStrategy.MAXIMUM;
			if (line.hasOption("g")) {
				String strategyAsString = line.getOptionValue("g");
				try {
					generationStrategy = GenerationStrategy.valueOf(strategyAsString);
				} catch (Exception e) {
					throw new Exception("Illegal value for generation strategy");
				}
			}	
					
			XmlObject xmlObj = generate(templateFilename, archetypeRepoPath,
					templateRepoPath, generationStrategy);

			// Assign filename
			String filename = line.hasOption("o") ? line.getOptionValue("o")
					: new String("output.xml");
			file = new File(filename);
			
			// Set XML generation options
			XmlOptions xmlOptions = new XmlOptions();
			xmlOptions.setSavePrettyPrint();
			xmlOptions.setSaveAggressiveNamespaces();
			xmlOptions.setSaveOuter();
			xmlOptions.setSaveInner();
			
			if (xmlObj instanceof COMPOSITION) {
				CompositionDocument doc = CompositionDocument.Factory.newInstance();
				doc.setComposition((COMPOSITION) xmlObj);
				doc.save(file, xmlOptions);
			} else {
				xmlObj.save(file, xmlOptions);
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.println("Processing failed.  Reason: " + e.getMessage());
			System.err.println("Use the parameter -help to get usage instructions");		
			System.exit(-1);
		}
		System.out.println("Successfully created output file:\n"+file.getAbsolutePath());

	}

	public static XmlObject generate(String templateFilename,
			String archetypeRepoPath, String templateRepoPath,
			GenerationStrategy generationStrategy)
			throws FileNotFoundException, Exception, FlatteningException,
			XMLBindingException {
		OETParser parser = new OETParser();
		InputStream templateIS = new FileInputStream(templateFilename);

		TEMPLATE template = parser.parseTemplate(templateIS).getTemplate();

		return generateFromTemplateObjectToXML(archetypeRepoPath,
				templateRepoPath, generationStrategy, template);
	}

	public static XmlObject generateFromTemplateObjectToXML(
			String archetypeRepoPath, String templateRepoPath,
			GenerationStrategy generationStrategy, TEMPLATE template)
			throws FlatteningException, Exception, XMLBindingException {

		ArchetypeAndTemplateRepository arTeRep = new ArchetypeAndTemplateRepository(
				new File(archetypeRepoPath),
				adlExtension, 
				new File(templateRepoPath), 
				oetExtension);
		
		// Load archetypes
		Map<String, Archetype> archetypeMap = arTeRep.getArchetypeMap();
		// Load templates
		Map<String, TEMPLATE> templateMap = arTeRep.getTemplateMap();
		
		Flattener flattener = new Flattener();
		Archetype flattened = flattener.toFlattenedArchetype(template,
				archetypeMap, templateMap);

		SkeletonGenerator generator = SkeletonGenerator.getInstance();

		// Old code: 
		// Object obj = generator.create(flattened, archetypeMap, generationStrategy);
		// The signature of .create has been changed in a recent openEHR java-libs release
		Object obj = generator.create(flattened, template.getId(), archetypeMap, generationStrategy);
		
		XMLBinding xmlBinding = new XMLBinding();
		Object value = xmlBinding.bindToXML(obj);
		XmlObject xmlObj = (XmlObject) value;
		return xmlObj;
	}

}
