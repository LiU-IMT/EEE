package se.liu.imt.mi.eee.validation;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.restlet.resource.ResourceException;


public class ValidationResult  {

	protected boolean valid;
	protected List<Throwable> errList = null;
	protected List<String> passList = null;
	private List<String> containedArchetypes;
	private List<String> containedTemplates;

	public List<String> getContainedArchetypes() {
		return containedArchetypes;
	}

	public void setContainedArchetypes(List<String> containedArchetypes) {
		this.containedArchetypes = containedArchetypes;
	}

	public List<String> getContainedTemplates() {
		return containedTemplates;
	}

	public void setContainedTemplates(List<String> containedTemplates) {
		this.containedTemplates = containedTemplates;
	}
	
	/**
	 * Creates a result that is initially valid (can switch to invalid if erors are added)
	 */
	public ValidationResult() {
		this.valid = true;
	}
	
	/**
	 * Creates a result that is initially valid (can switch to invalid if erors are added)
	 * @param pass An optional string describing the step that passed vaildation
	 */
	public ValidationResult(String pass) {
		this.valid = true;
		addPassedStep(pass);
	}

	/**
	 * Creates a result that is invalid (meaning that isValid() returns false)
	 * @param pass
	 */
	public ValidationResult(Throwable err) {		
		addError(err);
	}

	public void addPassedStep(String pass) {
		if (passList == null) passList = new ArrayList<String>();
		passList.add(pass);
	}
	
	/**
	 * After adding any error, isValid() will always return false.
	 * @param err
	 */
	public void addError(Throwable err) {
		this.valid = false;
		if (errList == null) errList = new ArrayList<Throwable>();
		errList.add(err);
	}
	
	public boolean isValid() {
		return valid;
	}

	public String getResultString() {
		String result = new String();

		if (passList != null) {
			for (String pass : passList) {
				result = result + "Passed: "+pass+ "\r\n";
			}
		}

		if (errList != null) {
			for (Throwable err : errList) {
				result = result + "FAILED: ";
				if (err instanceof ResourceException) {
					result = result + ((ResourceException)err).toString();
				} else {
					result = result + err.getMessage();
				}
				result = result +"\r\n";
			}			
		}
		return result;		
	}

	public String getResultHTMLString() {
		// TODO: Make nicer HTML format
		String result = new String();
		for (String pass : passList) {
			result = result + "<strong>PASSED:</strong> "+pass+ "<br/>\r\n";
		}
		for (Throwable err : errList) {
			result = result + "<strong>FAILED:</strong> "+err.getMessage()+ "<br/>\r\n";
		}
		return result;	
	}	

	public List<Throwable> getErrList() {
		return errList;
	}

	public List<String> getPassList() {
		return passList;
	}
	
	@Override
	public String toString() {
		return getResultString();
	}

}
