package se.liu.imt.mi.eee.validation.json;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;
import java.util.Set;

import org.openehr.am.archetype.assertion.Assertion;
import org.openehr.am.archetype.constraintmodel.ArchetypeConstraint;
import org.openehr.am.archetype.constraintmodel.CComplexObject;
import org.openehr.am.archetype.constraintmodel.CObject;
import org.openehr.am.archetype.ontology.ArchetypeOntology;
import org.openehr.rm.support.identification.ArchetypeID;
import org.openehr.rm.support.identification.ObjectID;

abstract class ArchetypeJacksonMixIn {
	ArchetypeJacksonMixIn(@JsonProperty("width") int w, @JsonProperty("height") int h) { } // TODO: remove w & h example

	// Examples:
    // note: could alternatively annotate fields "w" and "h" as well -- if so, would need to @JsonIgnore getters
    //  @JsonProperty("width") abstract int getW(); // rename property

    @JsonIgnore abstract void reloadNodeMaps();

    @JsonIgnore abstract Set<String> physicalPaths();

    @JsonIgnore abstract Set<String> logicalPaths(String language);

    @JsonIgnore abstract Map<String, CObject> getPathNodeMap();

    @JsonIgnore abstract String getAdlVersion();

    @JsonIgnore abstract ArchetypeID getArchetypeId();

  	@JsonProperty("width") abstract ObjectID getUid(); // TODO: change w & h example

  	@JsonIgnore abstract String getConcept();

  	@JsonIgnore abstract String getConceptName(String language);

  	@JsonIgnore abstract ArchetypeID getParentArchetypeId();

  	@JsonIgnore abstract CComplexObject getDefinition();

  	@JsonIgnore abstract Set<Assertion> getInvariants();

  	@JsonIgnore abstract ArchetypeOntology getOntology();

  	@JsonIgnore abstract String version();

  	@JsonIgnore abstract String previousVersion();

  	@JsonIgnore abstract ArchetypeConstraint node(String path);

  	@JsonIgnore abstract void updatePathNodeMap(CObject cobj);
  	@JsonIgnore abstract void updatePathNodeMap(String path, CObject cobj);
  	@JsonIgnore abstract void updatePathNodeMapRecursively(CObject cobj);
  	@JsonIgnore public abstract String toString();
  	@JsonIgnore abstract String inputByPath(String path);
  	@JsonIgnore abstract String pathByInput(String input);
  
}
