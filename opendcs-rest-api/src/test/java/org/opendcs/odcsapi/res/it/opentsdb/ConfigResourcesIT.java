package org.opendcs.odcsapi.res.it.opentsdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.session.SessionFilter;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendcs.odcsapi.beans.ApiConfigScript;
import org.opendcs.odcsapi.beans.ApiConfigScriptSensor;
import org.opendcs.odcsapi.beans.ApiConfigSensor;
import org.opendcs.odcsapi.beans.ApiPlatformConfig;
import org.opendcs.odcsapi.beans.ApiUnitConverter;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;
import org.opendcs.odcsapi.fixtures.ResourcesTestBase;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@Tag("integration")
@ExtendWith(DatabaseContextProvider.class)
final class ConfigResourcesIT extends ResourcesTestBase
{
	private static SessionFilter sessionFilter;
	private static Long configId;

	@BeforeEach
	void setUp() throws Exception
	{
		setUpCreds();
		sessionFilter = new SessionFilter();

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("credentials")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		ApiPlatformConfig config = new ApiPlatformConfig();
		config.setDescription("Test Platform");
		config.setNumPlatforms(1);
		config.setName("Test Platform");
		ArrayList<ApiConfigSensor> sensors = new ArrayList<>();
		ApiConfigSensor sensor = new ApiConfigSensor();
		sensor.setSensorName("Test Sensor");
		sensor.setSensorNumber(17833);
		sensor.setAbsoluteMax(100.0);
		sensor.setAbsoluteMin(0.0);
		sensor.setRecordingInterval(900);
		sensor.setTimeOfFirstSample(0);
		Properties properties = new Properties();
		properties.setProperty("site", "Test Site");
		sensor.setProperties(properties);
		HashMap<String, String> dataTypes = new HashMap<>();
		dataTypes.put("Test Type", "Test Units");
		sensor.setDataTypes(dataTypes);
		sensors.add(sensor);
		config.setConfigSensors(sensors);
		ArrayList<ApiConfigScript> scripts = new ArrayList<>();
		ApiConfigScript script = new ApiConfigScript();
		script.setName("Test Script");
		// TODO: Tie this into the unit converter endpoint. Causes NPE if converter with ID is not in DB
		ArrayList<ApiConfigScriptSensor> scriptSensors = new ArrayList<>();
		ApiConfigScriptSensor scriptSensor = new ApiConfigScriptSensor();
		scriptSensor.setSensorNumber(17833);
		ApiUnitConverter unitConverter = new ApiUnitConverter();
		unitConverter.setAlgorithm("x * 100 + 32");
		unitConverter.setFromAbbr("C");
		unitConverter.setToAbbr("F");
		unitConverter.setA(100.0);
		unitConverter.setB(32.0);
		scriptSensor.setUnitConverter(unitConverter);
//		scriptSensors.add(scriptSensor);
		script.setScriptSensors(scriptSensors);
		scripts.add(script);
		config.setScripts(scripts);

		ObjectMapper mapper = new ObjectMapper();
		String configJson = mapper.writeValueAsString(config);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(configJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("config")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		configId = getConfigId(config.getName());
	}

	@AfterEach
	void tearDown()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("configid", configId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("config")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@AfterAll
	static void logout()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("logout")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;
	}

	@TestTemplate
	void testGetConfigRefs()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("configrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testGetConfig()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("configid", configId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("config")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testPostAndDeleteConfig() throws Exception
	{
		ApiPlatformConfig config = new ApiPlatformConfig();
		config.setDescription("Test Platform 1");
		config.setNumPlatforms(1);
		config.setName("Test Platform 2");
		ArrayList<ApiConfigSensor> sensors = new ArrayList<>();
		ApiConfigSensor sensor = new ApiConfigSensor();
		sensor.setSensorName("Test Sensor 1");
		sensor.setSensorNumber(178355);
		sensor.setAbsoluteMax(120.0);
		sensor.setAbsoluteMin(2.0);
		sensor.setRecordingInterval(90);
		sensor.setTimeOfFirstSample(10000000);
		Properties properties = new Properties();
		properties.setProperty("site", "Test Site 1");
		sensor.setProperties(properties);
		HashMap<String, String> dataTypes = new HashMap<>();
		dataTypes.put("Test Type", "Test Units 1");
		sensor.setDataTypes(dataTypes);
		sensors.add(sensor);
		config.setConfigSensors(sensors);
		ArrayList<ApiConfigScript> scripts = new ArrayList<>();
		ApiConfigScript script = new ApiConfigScript();
		script.setName("Test Script");
		ArrayList<ApiConfigScriptSensor> scriptSensors = new ArrayList<>();
		// TODO: Tie this into the unit converter endpoint. Causes NPE if converter with ID is not in DB
		ApiConfigScriptSensor scriptSensor = new ApiConfigScriptSensor();
		scriptSensor.setSensorNumber(178413);
		ApiUnitConverter unitConverter = new ApiUnitConverter();
		unitConverter.setAlgorithm("x / 100 + 885");
		unitConverter.setFromAbbr("F");
		unitConverter.setToAbbr("C");
		unitConverter.setA(110.0);
		unitConverter.setB(38.0);
		scriptSensor.setUnitConverter(unitConverter);
//		scriptSensors.add(scriptSensor);
		script.setScriptSensors(scriptSensors);
		scripts.add(script);
		config.setScripts(scripts);

		ObjectMapper mapper = new ObjectMapper();
		String configJson = mapper.writeValueAsString(config);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(configJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("config")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		Long newConfigId = getConfigId(config.getName());

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("configid", newConfigId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("config")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}


	private Long getConfigId(String configName)
	{
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("configrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		for (int i = 0; i < response.body().jsonPath().getList("").size(); i++)
		{

			if ((response.body().jsonPath().getString("[" + i + "].name")).equalsIgnoreCase(configName))
			{
				return response.body().jsonPath().getLong("[" + i + "].configId");
			}
		}
		return null;
	}

}
