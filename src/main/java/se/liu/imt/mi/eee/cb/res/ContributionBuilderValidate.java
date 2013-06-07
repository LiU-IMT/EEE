package se.liu.imt.mi.eee.cb.res;

public class ContributionBuilderValidate extends
		ContributionBuilderValidateAndCommit {
	
	public ContributionBuilderValidate() {
		super();
		setName("Contribution Builder Validator");
		setDescription("Validates a Contribution Build without commmitting it to the EHR backend storage");
	}

}
