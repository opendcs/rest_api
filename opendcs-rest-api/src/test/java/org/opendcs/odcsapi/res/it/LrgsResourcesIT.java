package org.opendcs.odcsapi.res.it;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.opendcs.odcsapi.beans.ApiDataSource;
import org.opendcs.odcsapi.beans.ApiPlatform;
import org.opendcs.odcsapi.beans.ApiSearchCrit;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration-opentsdb-only")
@ExtendWith(DatabaseContextProvider.class)
final class LrgsResourcesIT extends BaseIT
{
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static SessionFilter sessionFilter;
	private Long siteId;
	private Long platformId;
	private Long datasourceId;
	private String dataSource;

	@BeforeEach
	void setUp() throws Exception
	{
		setUpCreds();
		sessionFilter = new SessionFilter();
		authenticate(sessionFilter);

		platformId = storePlatform();

		datasourceId = storeDataSource();

		ApiSearchCrit crit = getDtoFromResource("lrgs_search_crit_insert_data.json", ApiSearchCrit.class);

		List<String> platformIds = new ArrayList<>();
		platformIds.add(platformId.toString());
		crit.setPlatformIds(platformIds);

		List<String> platformNames = new ArrayList<>();
		platformNames.add("TEST-PLATFORM");
		crit.setPlatformNames(platformNames);

		String criteriaJson = MAPPER.writeValueAsString(crit);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.body(criteriaJson)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("searchcrit")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
		;
	}

	@AfterEach
	void tearDown()
	{
		deletePlatform(platformId);

		deleteDataSource(datasourceId);

		deleteSite(siteId);

		logout(sessionFilter);
	}

	@TestTemplate
	void testGetSearchCriteria()
	{
		JsonPath expected = getJsonPathFromResource("lrgs_search_crit_insert_data.json");

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("searchcrit")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();

		// platform IDs are not checked, since those are generated DB keys
		assertEquals(expected.getString("since"), actual.getString("since"));
		assertEquals(expected.getString("until"), actual.getString("until"));
		assertEquals(expected.getString("platformNames[0]"), actual.getString("platformNames[0]"));
		assertEquals(expected.getString("netlistNames[0]"), actual.getString("netlistNames[0]"));
		assertEquals(expected.getString("goesChannels[0]"), actual.getString("goesChannels[0]"));
		assertEquals(expected.getBoolean("goesSelfTimed"), actual.getBoolean("goesSelfTimed"));
		assertEquals(expected.getBoolean("goesRandom"), actual.getBoolean("goesRandom"));
		assertEquals(expected.getBoolean("networkDCP"), actual.getBoolean("networkDCP"));
		assertEquals(expected.getBoolean("iridium"), actual.getBoolean("iridium"));
		assertEquals(expected.getBoolean("qualityNotifications"), actual.getBoolean("qualityNotifications"));
		assertEquals(expected.getBoolean("goesSpacecraftCheck"), actual.getBoolean("goesSpacecraftCheck"));
		assertEquals(expected.getString("goesSpacecraftSelection"), actual.getString("goesSpacecraftSelection"));
		assertEquals(expected.getBoolean("parityCheck"), actual.getBoolean("parityCheck"));
		assertEquals(expected.getString("paritySelection"), actual.getString("paritySelection"));
	}

	@TestTemplate
	void testGetMessages()
	{
//		JsonPath expected = getJsonPathFromResource("lrgs_messages_expected.json");

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("messages")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();

		// TODO: Fix this comparison
//		assertEquals(expected, actual);
	}

	@TestTemplate
	void testGetMessage()
	{
//		JsonPath expected = getJsonPathFromResource("lrgs_message_expected.json");

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("tmid", "LRGS-TEST")
			.queryParam("tmtype", "Platform")
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("message")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();

		// TODO: Fix this comparison
//		assertEquals(expected, actual);
	}

	@TestTemplate
	void testGetLrgsStatus()
	{
//		JsonPath expected = getJsonPathFromResource("lrgs_status_expected.json");

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("source", dataSource)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("lrgsstatus")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();

		// TODO: Fix this comparison
//		assertEquals(expected, actual);
	}

	private Long storePlatform() throws Exception
	{
		ApiPlatform platform = getDtoFromResource("lrgs_platform_insert_data.json",
				ApiPlatform.class);

		siteId = storeSite();
		platform.setSiteId(siteId);

		String platformJson = MAPPER.writeValueAsString(platform);

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(platformJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("platform")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		return response.body().jsonPath().getLong("platformId");
	}

	private void deletePlatform(Long platformId)
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("platformid", platformId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("platform")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	private Long storeSite() throws Exception
	{
		String siteJson = getJsonFromResource("lrgs_site_insert_data.json");

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(siteJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("site")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		return response.body().jsonPath().getLong("siteId");
	}

	private void deleteSite(Long siteId)
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("siteid", siteId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("site")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	private Long storeDataSource() throws Exception
	{
		ApiDataSource source = getDtoFromResource("lrgs_datasource_insert_data.json", ApiDataSource.class);

		String dsJson = MAPPER.writeValueAsString(source);

		// Create a new data source member
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(dsJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("datasource")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		dataSource = response.body().jsonPath().getString("name");

		Properties props = new Properties();
		props.setProperty("api.datasource", source.getName());

		String propJson = MAPPER.writeValueAsString(props);

		// Store properties
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(propJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("tsdb_properties")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		return response.body().jsonPath().getLong("dataSourceId");
	}

	private void deleteDataSource(Long datasourceId)
	{
		// Delete the data source
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("datasourceid", datasourceId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("datasource")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		// TODO: Fix this verification once the updated DatasourceResource controller is merged in
//		// Verify that the data source was deleted
//		given()
//			.log().ifValidationFails(LogDetail.ALL, true)
//			.accept(MediaType.APPLICATION_JSON)
//			.header("Authorization", authHeader)
//			.filter(sessionFilter)
//			.queryParam("datasourceid", datasourceId)
//		.when()
//			.redirects().follow(true)
//			.redirects().max(3)
//			.get("datasource")
//		.then()
//			.log().ifValidationFails(LogDetail.ALL, true)
//		.assertThat()
//			.statusCode(is(HttpServletResponse.SC_NOT_FOUND))
//		;
	}
}
