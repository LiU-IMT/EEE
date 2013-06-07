package se.liu.imt.mi.eee.validation;

import java.util.List;
import java.util.Map;

public interface ValidatorAndConverter<INPUT_TYPE, OUTPUT_TYPE> extends Validator<INPUT_TYPE>{
	
	/**
	 * @return the valid input converted to the OUTPUT_TYPE format if valid. returns null if validation failed. 
	 */
	ValidationAndConversionResult<OUTPUT_TYPE> validateAndConvert(INPUT_TYPE input);
	
	Map<INPUT_TYPE, ValidationAndConversionResult<OUTPUT_TYPE>> validateAndConvertList(List<INPUT_TYPE> input);

}
