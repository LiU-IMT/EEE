package se.liu.imt.mi.eee;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import openEHR.v1.template.TEMPLATE;

import org.openehr.am.archetype.Archetype;
import org.openehr.am.template.OETParser;
import org.openehr.am.template.Flattener;

import se.acode.openehr.parser.ADLParser;
import se.acode.openehr.parser.ParseException;
import se.liu.imt.mi.eee.utils.Util;

public class ArchetypeAndTemplateRepository {

	private Map<String, Archetype> archetypeMap = null;
	private Map<String, TEMPLATE> templateMap = null;
	private Flattener flattener;
	
	public ArchetypeAndTemplateRepository(File archetypeDirectory, final String archetypeFileExt) throws ParseException, FileNotFoundException, Exception {
		super();
		
		System.out.println("Will open archetype repository:  "+archetypeDirectory.getAbsolutePath());
		
		flattener = new Flattener();
		archetypeMap = new HashMap<String, Archetype>();
			Collection<File> fileList = Util.listFiles(
					archetypeDirectory, 
					new FileFilter() {
						public boolean accept(File pathname) {
							return pathname.getName().toLowerCase().endsWith(archetypeFileExt);
						}
					},
					true);
			for (File aFile : fileList) {
				ADLParser adlParser = new ADLParser(new FileInputStream(aFile),"UTF-8");
				Archetype archetype = null;
				archetype = adlParser.parse();
				if (archetype != null)
					archetypeMap.put(archetype.getArchetypeId().toString(),
							archetype);
			}
	}
	
	public ArchetypeAndTemplateRepository(File archetypeDirectory, final String archetypeFileExt, File templateDirectory, final String templateFileExt) throws ParseException, FileNotFoundException, Exception {
		this(archetypeDirectory, archetypeFileExt);
		
		System.out.println("Will open oetTemplate repository:  "+templateDirectory.getAbsolutePath());
		
		templateMap = new HashMap<String, TEMPLATE>();

			Collection<File> fileList = Util.listFiles(templateDirectory, new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.getName().toLowerCase().endsWith(
							templateFileExt);
				}
			}, true);
			for (File tFile : fileList) {
				OETParser oetParser = new OETParser();
				TEMPLATE templ = null;
				try {
					templ = oetParser.parseTemplate(new FileInputStream(tFile))
							.getTemplate();
				} catch (Exception e) {
					System.out
							.println("InstanceBuilder.main(): Parse error for template file "
									+ tFile.getName());
					e.printStackTrace(System.out);
				}
				if (templ != null)
					templateMap.put(templ.getId(), templ);
			}

	}
	
	public Archetype flattenTemplate(TEMPLATE template) throws Exception {
		Archetype flattened = flattener.toFlattenedArchetype(template, 
				archetypeMap, null);
		return flattened;
	}

	public Map<String, Archetype> getArchetypeMap() {
		return archetypeMap;
	}

	public Map<String, TEMPLATE> getTemplateMap() {
		return templateMap;
	}
	
	public TEMPLATE getTemplate(String templateId){
		return getTemplateMap().get(templateId);
	}
	
	public Archetype getArchetype(String archetypeId){
		return getArchetypeMap().get(archetypeId);
	}
	
}
