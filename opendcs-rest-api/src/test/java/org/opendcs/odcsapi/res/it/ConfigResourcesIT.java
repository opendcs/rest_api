package org.opendcs.odcsapi.res.it;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import io.restassured.filter.log.LogDetail;
import io.restassured.filter.session.SessionFilter;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

@Tag("integration")
@ExtendWith(DatabaseContextProvider.class)
final class ConfigResourcesIT extends BaseIT
{
	private static SessionFilter sessionFilter;
	private static Long configId;

	@BeforeEach
	void setUp() throws Exception
	{
		setUpCreds();
		sessionFilter = new SessionFilter();

		authenticate(sessionFilter);

		String configJson = getJsonFromResource("config_input_data.json");

		ExtractableResponse<Response> response = given()
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
			.extract()
		;

		configId = response.body().jsonPath().getLong("configId");
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

		logout(sessionFilter);
	}

	@TestTemplate
	void testGetConfigRefs()
	{
		JsonPath expected = getJsonPathFromResource("config_refs_expected.json");

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
			.body("[0].name", equalTo(expected.getString("[0].name")))
			.body("[0].description", equalTo(expected.getString("[0].description")))
			.body("[0].numPlatforms", equalTo(expected.getInt("[0].numPlatforms")))
		;
	}

	@TestTemplate
	void testGetConfig()
	{
		JsonPath expected = getJsonPathFromResource("config_get_expected.json");

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
			.body("name", equalTo(expected.getString("name")))
			.body("description", equalTo(expected.getString("description")))
			.body("numPlatforms", equalTo(expected.getLong("numPlatforms")))
			.body("scripts.size()", equalTo(expected.getString("scripts.size()")))
			.body("configSensors.size()", equalTo(expected.getString("configSensors.size()")))
			// TODO: Add script and configSensor assertions
		;
	}

	@TestTemplate
	void testPostAndDeleteConfig() throws Exception
	{
		String configJson = getJsonFromResource("config_post_delete_input_data.json");

		ExtractableResponse<Response> response = given()
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
			.extract()
		;

		Long newConfigId = response.body().jsonPath().getLong("configId");

		JsonPath expected = getJsonPathFromResource("config_post_delete_expected.json");

		// Get the config and assert it matches expected JSON
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
			.get("config")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.body("name", equalTo(expected.getString("name")))
			.body("description", equalTo(expected.getString("description")))
			.body("numPlatforms", equalTo(expected.getLong("numPlatforms")))
			.body("scripts.size()", equalTo(expected.getString("scripts.size()")))
			.body("configSensors.size()", equalTo(expected.getString("configSensors.size()")))
			// TODO: Add script and configSensor assertions
		;

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
}
