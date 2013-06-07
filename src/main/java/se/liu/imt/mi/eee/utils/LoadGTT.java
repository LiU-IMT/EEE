package se.liu.imt.mi.eee.utils;

public class LoadGTT {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		LoadPatientData patientDataLoader = new LoadPatientData();
		patientDataLoader.loadPatientData("GTT_c1", "resources/www-file-root/example/ehr-content/compositions/GTT/c1_transfusion/sameComposition");
		patientDataLoader.loadPatientData("GTT_c10_Patient1", "resources/www-file-root/example/ehr-content/compositions/GTT/c10_readmission/Patient1");
		patientDataLoader.loadPatientData("GTT_c10_Patient2", "resources/www-file-root/example/ehr-content/compositions/GTT/c10_readmission/Patient2");
		patientDataLoader.loadPatientData("GTT_c11_Patient1_diagnosis_T81_4", "resources/www-file-root/example/ehr-content/compositions/GTT/c11_healthcareAssociatedInfections/Patient1_diagnosis_T81_4");
		patientDataLoader.loadPatientData("GTT_c11_Patient2_neonatalUrinaryTractInfection", "resources/www-file-root/example/ehr-content/compositions/GTT/c11_healthcareAssociatedInfections/Patient2_neonatalUrinaryTractInfection/sameComposition");
		patientDataLoader.loadPatientData("GTT_c13", "resources/www-file-root/example/ehr-content/compositions/GTT/c13_procedure");
		patientDataLoader.loadPatientData("GTT_c2", "resources/www-file-root/example/ehr-content/compositions/GTT/c2_Hb");
		patientDataLoader.loadPatientData("GTT_c7_Patient1_DVT", "resources/www-file-root/example/ehr-content/compositions/GTT/c7_DVTorLungemboli/Patient1_DVT");
		patientDataLoader.loadPatientData("GTT_c7_Patient2_PE", "resources/www-file-root/example/ehr-content/compositions/GTT/c7_DVTorLungemboli/Patient2_PE");
		patientDataLoader.loadPatientData("GTT_c7_Patient3_admissionDueToDVT", "resources/www-file-root/example/ehr-content/compositions/GTT/c7_DVTorLungemboli/Patient3_admissionDueToDVT");
		patientDataLoader.loadPatientData("GTT_c7_Patient4_admissionDueToPE", "resources/www-file-root/example/ehr-content/compositions/GTT/c7_DVTorLungemboli/Patient4_admissionDueToPE");
		patientDataLoader.loadPatientData("GTT_c8_Patient1_FallDuringHospitalization", "resources/www-file-root/example/ehr-content/compositions/GTT/c8_fall/Patient1_FallDuringHospitalization");
		patientDataLoader.loadPatientData("GTT_c8_Patient2_AdmissionDueToFall", "resources/www-file-root/example/ehr-content/compositions/GTT/c8_fall/Patient2_AdmissionDueToFall");
		patientDataLoader.loadPatientData("GTT_c9", "resources/www-file-root/example/ehr-content/compositions/GTT/c9_pressureSore");
		patientDataLoader.loadPatientData("GTT_m10", "resources/www-file-root/example/ehr-content/compositions/GTT/m10_antiemetics");	
		patientDataLoader.loadPatientData("GTT_m12_Patient1_diagnosis", "resources/www-file-root/example/ehr-content/compositions/GTT/m12_medicationStop/Patient1_diagnosis");
		patientDataLoader.loadPatientData("GTT_m12_Patient2_medication", "resources/www-file-root/example/ehr-content/compositions/GTT/m12_medicationStop/Patient2_medication");
		patientDataLoader.loadPatientData("GTT_m5_Patient1_increasingCreatinine", "resources/www-file-root/example/ehr-content/compositions/GTT/m5_creatinine/Patient1_increasingCreatinine");
		patientDataLoader.loadPatientData("GTT_m5_Patient2_increasingUrea", "resources/www-file-root/example/ehr-content/compositions/GTT/m5_creatinine/Patient2_increasingUrea");
		patientDataLoader.loadPatientData("GTT_s12_Patient1_clinicalFindingDueToSurgicalProcedure", "resources/www-file-root/example/ehr-content/compositions/GTT/s12_procedureWithComplication/Patient1_clinicalFindingDueToSurgicalProcedure");
		patientDataLoader.loadPatientData("GTT_s12_Patient2_ICD10_T81", "resources/www-file-root/example/ehr-content/compositions/GTT/s12_procedureWithComplication/Patient2_ICD10_T81");
		patientDataLoader.loadPatientData("GTT_s3", "resources/www-file-root/example/ehr-content/compositions/GTT/s3_admissionIVAafterProcedure");
	}
}
