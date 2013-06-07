package se.liu.imt.mi.eee.validation.json;

import java.util.List;

import org.json.JSONObject;
import org.openehr.rm.RMObject;
import org.openehr.rm.common.archetyped.Locatable;

import se.liu.imt.mi.eee.validation.ValidatorAndConverter;
/**
 * Takes openEHR java RM objects as input and returns Strings with 
 * compact JSON representations of the object.
 * @author Erik Sundvall
 */
public abstract class JavaRM_to_CompactJSON_Converter implements
		ValidatorAndConverter<Locatable, JSONObject> {


	
}
