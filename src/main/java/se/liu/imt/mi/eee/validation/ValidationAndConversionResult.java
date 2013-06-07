package se.liu.imt.mi.eee.validation;

public class ValidationAndConversionResult<OUTPUT_TYPE> extends ValidationResult {

	OUTPUT_TYPE converted;

	public OUTPUT_TYPE getConverted() throws IllegalStateException {
		// if (!isValid()) throw new IllegalStateException("The object did not pass validation, an thus does not contain a converted value."); 
		return converted;
	}

	public void setConverted(OUTPUT_TYPE converted) {
		this.converted = converted;
	}
	

}
