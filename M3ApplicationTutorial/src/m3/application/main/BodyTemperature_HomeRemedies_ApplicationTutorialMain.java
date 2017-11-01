package m3.application.main;

import java.io.FileWriter;
import java.util.ArrayList;

import m3.application.skeleton.ExecuteQueryEngine;
import m3.application.skeleton.GenericApplication;
import m3.application.skeleton.ReadFile;
import m3.application.skeleton.VariableSparql;
import semantic.data.annotator.SemanticAnnotator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * 
 * @author Amelie Gyrard, <br/>
 * In collaboration with Insight/NUIG-DERI, Ireland <br/>
 * Thanks to Pankesh Patel for the fruitful discussions leading to this tutorial Java code <br/><br/>
 * 
 * Designed for Demo paper ISWC 2016, ISWC 2016 Tutorial, and future tutorials, summer school, etc.<br/>
 * To encourage the reusability and understandability of our M3 framework and our research<br/><br/>
 * 
 * IoT application: Deducing meaningful information from a Body Thermometer and suggests home remedies.
 *  
 * Updated: July 2016: Add semantic annotation part missing <br/>
 * Created June 2016 <br/>
 * 
 * TODO: show that we are loading the rules for semantic annotation
 * TODO: restructure the java skeleton to clearly explain which files are provided by the template?
 * 
 */
public class BodyTemperature_HomeRemedies_ApplicationTutorialMain {

	// RAW DATA (SEML/XML)
	public static final String HEALTH_RAW_SENSOR_DATA = "./rawSensorDataset/senMLHealthData.xml";

	// SEMANTIC SENSOR DATASETS
	public static final String GENERATED_SEMANTIC_SENSOR_DATA = "./semanticSensorDataset/generated_semantic_sensor_data.rdf";

	// useful in case of issues with the semantic annotator
	public static final String HEALTH_SEMANTIC_SENSOR_DATA = "./semanticSensorDataset/senml_m3_health_data.rdf";
	public static final String WEATHER_SEMANTIC_SENSOR_DATA = "./semanticSensorDataset/weatherData_8KB_17Septembre2014.rdf";
	
	// ONTOLOGIES
	public static final String M3_ONTOLOGY = "./ontologies/m3.owl";
	public static final String NATUROPATHY_ONTOLOGY = "./ontologies/naturopathy.owl";
	public static final String HEALTH_ONTOLOGY = "./ontologies/health.owl";

	// DOMAIN DATASETS
	public static final String NATUROPATHY_DATASET = "./semanticDomainDataset/naturopathy-dataset.rdf";
	public static final String HEALTH_DATASET = "./semanticDomainDataset/health-dataset.rdf";

	// SPARQL QUERY
	public static final String SPARQL_QUERY_NATUROPATHY = "./sparql/m3SparqlNaturopathyScenario.sparql";
	public static final String SPARQL_QUERY_NATUROPATHY_MINIMAL = "./sparql/m3SparqlNaturopathyScenarioMinimal.sparql";

	// NAMESPACE FOR ONTOLOGIES
	public static final String NAMESPACE_M3= "http://sensormeasurement.appspot.com/m3#";

	// RULES FOR THE SEMANTIC ANNOTATOR
	public static String RULES_M3_SEMANTIC_ANNOTATION = "./ruleSemanticAnnotation/rulesM3SemanticAnnotation.txt";
	
	// RULES TO DEDUCE MEANINGFUL KNOWLEDGE
	public static final String LINKED_OPEN_RULES_HEALTH = "./ruleDeduceMeaningfulKnowledge/LinkedOpenRulesHealth.txt";

	public static void main(String[] args) {

		// USER TO DO: add the jena library to the project build path (in eclipse)
		
		// same scenario than http://sensormeasurement.appspot.com/naturopathy/sick
		// To design another scenario or application
		// download another template http://sensormeasurement.appspot.com/?p=m3api
		// you can contact us for a new template as well
		

		try {

			// STEP: LOAD RAW DATA (SENML/XML) in this tutorial
			System.out.println("0");
			String sensorMeasurements = ReadFile.readContentFile(HEALTH_RAW_SENSOR_DATA);
					
			// STEP: SEMANTIC ANNOTATION
			
			//TODO: show that we are loading the rules for semantic annotation
			System.out.println("1");
			SemanticAnnotator semanticAnnotator = new SemanticAnnotator();
			System.out.println("2");
			semanticAnnotator.convertXMLSenMLIntoRDF(sensorMeasurements);
					
			// WRITE SEMANTIC SENSOR DATA IN A FILE
			String fileName = GENERATED_SEMANTIC_SENSOR_DATA;
			
			FileWriter out = new FileWriter(fileName);
			System.out.println("semanticAnnotator.model.write(out,RDF/XML-ABBREV)");
			semanticAnnotator.model.write(out,"RDF/XML-ABBREV");
									
			// STEP: LOAD SEMANTIC SENSOR DATA
			Model model = ModelFactory.createDefaultModel();
			
			ReadFile.enrichJenaModelOntologyDataset(model, GENERATED_SEMANTIC_SENSOR_DATA);
									
			// works with semantic sensor data 
			// (file already generated, useful to use in case of some issues with the semantic annotation)
			// ReadFile.enrichJenaModelOntologyDataset(model, HEALTH_SEMANTIC_SENSOR_DATA);
			
			// GENERIC APPLICATION
			GenericApplication generic_appli = new GenericApplication(model);

			// STEP: SPECIFIC DOMAIN ONTOLOGIES AND DATASETS
			ReadFile.enrichJenaModelOntologyDataset(generic_appli.model, M3_ONTOLOGY);
			ReadFile.enrichJenaModelOntologyDataset(generic_appli.model, NATUROPATHY_ONTOLOGY);
			ReadFile.enrichJenaModelOntologyDataset(generic_appli.model, NATUROPATHY_DATASET);
			ReadFile.enrichJenaModelOntologyDataset(generic_appli.model, HEALTH_ONTOLOGY);
			ReadFile.enrichJenaModelOntologyDataset(generic_appli.model, HEALTH_DATASET);

		
			// STEP: EXECUTING REASONING ENGINE
			Model deduceMeaningfulInformationFromSensorData = 
					generic_appli.executeReasoningEngine(LINKED_OPEN_RULES_HEALTH);

			//deduceMeaningfulInformationFromSensorData.write(System.out);

			
			// STEP: TO MODIFY THE GENERIC SPARQL QUERY BY ASKING SPECIFIC INFORMATION
			ArrayList<VariableSparql> var = new ArrayList<VariableSparql>();
			var.add(new VariableSparql("inferTypeUri", NAMESPACE_M3 + "BodyTemperature", false));// we look for body temperature measurement

			// STEP: EXECUTING QUERY ENGINE
			ExecuteQueryEngine resultQueryEngine = new ExecuteQueryEngine(
					deduceMeaningfulInformationFromSensorData, SPARQL_QUERY_NATUROPATHY);
			String result = resultQueryEngine.getSelectResultAsXML(var);

			// DISPLAY SMARTER DATA 
			// USER TO DO: DISPLAY THE RESULT IN A USER FRIENDLY INTERFACE
			System.out.println("PRINTING RESULT");
			System.out.println(result);	

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
