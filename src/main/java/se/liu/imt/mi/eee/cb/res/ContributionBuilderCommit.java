package se.liu.imt.mi.eee.cb.res;

public class ContributionBuilderCommit extends
		ContributionBuilderValidateAndCommit {
	
	public ContributionBuilderCommit() {
		super();
		setName("Contribution Builder Comitter");
		setDescription("Validates and then (if valid) commits a Contribution Build to the EHR backend storage");
	}	
	
}
