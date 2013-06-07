package se.liu.imt.mi.eee.validation;

import java.util.List;
import java.util.Map;

public interface Validator<INPUT_TYPE> {
	
	ValidationResult validate(INPUT_TYPE input);

	/**
	 * @param errorList a modifiable list that potentially can get filled with validation errors.
	 * @return true if input is valid
	 */
	Map<INPUT_TYPE, ValidationResult> validateList(List<INPUT_TYPE> input);

}
