package se.liu.imt.mi.eee.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A somewhat lazyness-encouraging abstract adapter class that only requires
 * subclasses to implement the validateAndConvert(INPUT_TYPE input) method 
 * in order to implement the entire ValidatorAndConverter interface.
 * NOTE: An implementation of the "validate" and validateList methods that 
 * does not also convert during validation may be more efficient at runtime 
 * than the current code that always calls the validateAndConvert* methods 
 * under the hood.
 * @author erisu
 *
 * @param <INPUT_TYPE>
 * @param <OUTPUT_TYPE>
 */
public abstract  class ValidatorAndConverterAdapter<INPUT_TYPE, OUTPUT_TYPE> 
	implements ValidatorAndConverter<INPUT_TYPE, OUTPUT_TYPE> {
		
	public ValidationResult validate(INPUT_TYPE input){
		return validateAndConvert(input);
	}

	public Map<INPUT_TYPE, ValidationResult> validateList(List<INPUT_TYPE> input) {
		
		Map<INPUT_TYPE, ValidationResult> valMap = new HashMap<INPUT_TYPE, ValidationResult>();
		Map<INPUT_TYPE, ValidationAndConversionResult<OUTPUT_TYPE>> valConvMap2 = validateAndConvertList(input);
		for (Entry<INPUT_TYPE, ValidationAndConversionResult<OUTPUT_TYPE>> ent : valConvMap2.entrySet()) {
			valMap.put(ent.getKey(), ent.getValue());
		}
		return valMap;
	}

	// This needs to be implemented by concrete classed
	public abstract ValidationAndConversionResult<OUTPUT_TYPE> validateAndConvert(INPUT_TYPE input) ;

	public Map<INPUT_TYPE, ValidationAndConversionResult<OUTPUT_TYPE>> validateAndConvertList(List<INPUT_TYPE> input) {
		Map<INPUT_TYPE, ValidationAndConversionResult<OUTPUT_TYPE>> valConvMap 
			= new HashMap<INPUT_TYPE, ValidationAndConversionResult<OUTPUT_TYPE>>();
		for (INPUT_TYPE inputItem : input) {
			valConvMap.put(inputItem, validateAndConvert(inputItem));
		}
		return valConvMap;
	}


}
