package ca.uhn.fhir.example;

import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Patient.PatientCommunicationComponent;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IIdType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Enumerations;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class Example01_CreateAPatient {
	public static void main(String[] theArgs) {

//      // Create a resource instance
//      Patient pat = new Patient();
//
//      // Add a "name" element
//      HumanName name = pat.addName();
//      name.setFamily("Simpson").addGiven("Homer").addGiven("J");
//
//      // Add an "identifier" element
//      Identifier identifier = pat.addIdentifier();
//      identifier.setSystem("http://acme.org/MRNs").setValue("7000135");
//
//      // Model is designed to be chained
//      pat.addIdentifier().setSystem("http://acme.org/MRNs").setValue("12345");

//		step2_search_patient("4952");

//		step3_create_patient();
		// Create a client
		FhirContext ctx = FhirContext.forDstu3();
		IGenericClient client = ctx.newRestfulGenericClient("http://superbugai.erc.monash.edu:8081/baseDstu3");
//		IGenericClient client = ctx.newRestfulGenericClient("http://fhirtest.uhn.ca/baseDstu3");
//
//		try {
//			List<String[]> patients = Files.lines(Paths.get("/home/monash/temp/patient.csv"))
//			                .map(line -> line.split(","))
//			                .collect(Collectors.toList());
//			for (int i=1; i<patients.size(); i++) {
//				step3_create_patient(client, patients.get(i)[0], patients.get(i)[1], patients.get(i)[2]);
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		step3_create_encounter(client, "", "", "", "");
		
		extractPeriod("2165-06-05 11:30:00", "2165-06-05 11:40:00");
	}

	public static void step2_search_patient(String id){
		// Create a context
		FhirContext ctx = FhirContext.forDstu3();

		// Create a client
		IGenericClient client = ctx.newRestfulGenericClient("http://superbugai.erc.monash.edu:8081/baseDstu3");

		// Read a patient with the given ID
		Patient patient = client.read().resource(Patient.class).withId(id).execute();

		// Print the output
		String string = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
		System.out.println(string);
	}

	public static void step3_create_patient(IGenericClient client, String id, String gender, String birthDate) {
		// Create a patient
		Patient newPatient = new Patient();

		// Populate the patient with fake information
		newPatient
			.addIdentifier()
				.setSystem("http://superbugai.erc.monash.edu:8081/Patient")
				.setValue(id);
		if (gender.equalsIgnoreCase("M")) {
			newPatient.setGender(Enumerations.AdministrativeGender.MALE);
		} else {
			newPatient.setGender(Enumerations.AdministrativeGender.FEMALE);
		}
		
		newPatient.setBirthDateElement(new DateType(birthDate));

		// Create the resource on the server
		MethodOutcome outcome = client
			.create()
			.resource(newPatient)
			.execute();

		// Log the ID that the server assigned
		IIdType patientId = outcome.getId();
		System.out.println("Created patient, got ID: " + patientId);
	}

	public static void step3_create_observation(IGenericClient client, String id) {
		// Create a patient
		Observation newObservation = new Observation();

		// Populate the patient with fake information
		newObservation
			.addIdentifier()
				.setSystem("http://superbugai.erc.monash.edu:8081/Observation")
				.setValue(id);

		// Create the resource on the server
		MethodOutcome outcome = client
			.create()
			.resource(newObservation)
			.execute();

		// Log the ID that the server assigned
		IIdType observationId = outcome.getId();
		System.out.println("Created observation, got ID: " + observationId);
	}

	public static void step3_create_encounter(IGenericClient client, String id, String subject, String startPeriod, String endPeriod) {
		// Create a patient
		Encounter newEncounter = new Encounter();

		// Populate the patient with fake information
		newEncounter
			.addIdentifier()
				.setSystem("http://superbugai.erc.monash.edu:8081/Encounter")
				.setValue(id);
		
	     Bundle results = client
	                .search()
	                .forResource(Patient.class)
	                .where(Patient.IDENTIFIER.exactly().systemAndCode("http://superbugai.erc.monash.edu:8081/Patient", "98813"))
	                .returnBundle(Bundle.class)
	                .execute();
	     results.getEntry().forEach((entry) -> {
             // within each entry is a resource - print its logical ID
	    	 if (entry.getResource() instanceof Patient) {
	    		 Patient patient = (Patient) entry.getResource();
	    		 newEncounter.setSubject(new Reference().setReferenceElement(patient.getIdElement()));
	    	 }
         });

	     newEncounter.setPeriod(extractPeriod(startPeriod, endPeriod));

		// Create the resource on the server
//		MethodOutcome outcome = client
//			.create()
//			.resource(newEncounter)
//			.execute();
//
//		// Log the ID that the server assigned
//		IIdType encounterId = outcome.getId();
//		System.out.println("Created encounter, got ID: " + encounterId);
	}

	public static Period extractPeriod(String startPeriod, String endPeriod) {
		Date startDate = new GregorianCalendar(
				Integer.parseInt(startPeriod.substring(0, 4)),
				Integer.parseInt(startPeriod.substring(5, 7)),
				Integer.parseInt(startPeriod.substring(8, 10)),
				Integer.parseInt(startPeriod.substring(11, 13)),
				Integer.parseInt(startPeriod.substring(14, 16)),
				Integer.parseInt(startPeriod.substring(17))
				).getTime();
		Date endDate = new GregorianCalendar(
				Integer.parseInt(endPeriod.substring(0, 4)),
				Integer.parseInt(endPeriod.substring(5, 7)),
				Integer.parseInt(endPeriod.substring(8, 10)),
				Integer.parseInt(endPeriod.substring(11, 13)),
				Integer.parseInt(endPeriod.substring(14, 16)),
				Integer.parseInt(endPeriod.substring(17))
				).getTime();
		Period period = new Period().setStart(startDate).setEnd(endDate);
		return period;
	}

}
