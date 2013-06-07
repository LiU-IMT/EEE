package se.liu.imt.mi.eee.utils;

public class LoadExample {
	
		/**
		 * @param args
		 */
		public static void main(String[] args) throws Exception {
			LoadPatientData patientDataLoader = new LoadPatientData();
			// Create two test patients (with same input data - but different IDs will be used for each entry)
			patientDataLoader.loadPatientData("example-ehr-id", "src/main/resources/www-file-root/example/ehr-content/compositions/Anna");
			patientDataLoader.loadPatientData("example-ehr-id-2", "src/main/resources/www-file-root/example/ehr-content/compositions/Anna");
			}
	}

