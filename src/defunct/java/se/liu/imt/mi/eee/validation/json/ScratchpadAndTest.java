package se.liu.imt.mi.eee.validation.json;

import java.io.File;
import java.io.IOException;

import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.constraintmodel.CComplexObject;
import org.openehr.am.openehrprofile.datatypes.text.CCodePhrase;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.restlet.data.Status;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import se.acode.openehr.parser.ADLParser;

public class ScratchpadAndTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
//		System.out.println("ScratchpadAndTest.main() SAFE_ALPHABET_LENGTH = "+Util.SAFE_ALPHABET_LENGTH);
		new ScratchpadAndTest();
	}

	public ScratchpadAndTest() throws Exception {
		ADLParser adlParser = new ADLParser(new File("resources/www-file-root/aom/archetypes/openEHR-DEMOGRAPHIC-PERSON.person.v1.adl"));
		Archetype archetype = adlParser.parse();
		
		//AOMtoJSONandYAMLSerializer a2yG = new AOMtoJSONandYAMLSerializer();
		JacksonBasedAOMtoJSONandYAML a2yJ = new JacksonBasedAOMtoJSONandYAML();
		String output = a2yJ.output(archetype, false, FlowStyle.AUTO);
		System.out.println(output);
		// CComplexObject ccobj = archetype.getDefinition();
		//System.out.println("Archetype:"+archetype.toString());
//		Yaml yaml = new Yaml(new CCORepresenter());
//		Tag tag = new Tag("org.openehr.am.archetype.ARCHETYPE");
//		yaml.setBeanAccess(BeanAccess.FIELD);
//		System.out.println(yaml.dumpAsMap(archetype));
//		//System.out.println(yaml.dumpAs(archetype.getOntology(), tag , FlowStyle.BLOCK));			
	}
	
	

}
