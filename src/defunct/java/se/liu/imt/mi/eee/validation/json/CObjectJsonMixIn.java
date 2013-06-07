package se.liu.imt.mi.eee.validation.json;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.openehr.am.archetype.constraintmodel.ArchetypeConstraint;
import org.openehr.am.archetype.constraintmodel.CAttribute;
import org.openehr.am.archetype.constraintmodel.CObject;
import org.openehr.rm.support.basic.Interval;

@JsonPropertyOrder({ "rm_type_name", "rm_attribute_name", "node_id" })
public class CObjectJsonMixIn extends CObject {

	protected CObjectJsonMixIn(boolean anyAllowed, String path,
			String rmTypeName, Interval<Integer> occurrences, String nodeID,
			CAttribute parent) {
		super(anyAllowed, path, rmTypeName, occurrences, nodeID, parent);
		// TODO Auto-generated constructor stub
	}

	@Override @JsonIgnore
	protected CObject copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override @JsonIgnore
	public boolean hasPath(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override @JsonIgnore
	public boolean isSubsetOf(ArchetypeConstraint arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override @JsonIgnore
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override 
	public String getNodeId() {
		// TODO Auto-generated method stub
		return super.getNodeId();
	}

	@Override
	public Interval<Integer> getOccurrences() {
		// TODO Auto-generated method stub
		return super.getOccurrences();
	}

	@Override @JsonIgnore
	public CAttribute getParent() {
		// TODO Auto-generated method stub
		return super.getParent();
	}

	@Override
	public String getRmTypeName() {
		// TODO Auto-generated method stub
		return super.getRmTypeName();
	}
	

}
