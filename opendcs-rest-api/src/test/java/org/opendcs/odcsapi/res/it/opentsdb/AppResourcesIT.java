package org.opendcs.odcsapi.res.it.opentsdb;

import java.sql.Date;
import java.time.Instant;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendcs.odcsapi.beans.ApiLoadingApp;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;
import org.opendcs.odcsapi.fixtures.ResourcesTestBase;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@Tag("integration")
@ExtendWith(DatabaseContextProvider.class)
final class AppResourcesIT extends ResourcesTestBase
{
	private static Long appid;
	private static final String AUTH_HEADER_PREFIX = "Basic ";
	private static SessionFilter sessionFilter;
	private static Properties properties;

	@BeforeAll
	static void setUpAll()
	{
		properties = new Properties();
		properties.setProperty("startCmd", "$DCSTOOL_HOME\\bin\\compproc.bat");
	}

	@BeforeEach
	void setUp() throws Exception
	{
		setUpCreds();
		appid = null;
		sessionFilter = new SessionFilter();

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
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

		ObjectMapper objectMapper = new ObjectMapper();

		ApiLoadingApp app = new ApiLoadingApp();
		app.setAppType("loading");
		app.setAppName("TestApp1");
		app.setAppId(9965L);
		app.setProperties(properties);
		app.setLastModified(Date.from(Instant.parse("2021-02-01T00:00:00Z")));
		app.setComment("Test comment");

		String appJson = objectMapper.writeValueAsString(app);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.body(appJson)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("app")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		appid = getAppId(app.getAppName());
	}

	@AfterEach
	void tearDown()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.queryParam("appid", appid)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("app")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@AfterAll
	static void tearDownAll()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
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
	void testGetAppRefsRoundTrip()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("apprefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testGetAppRoundTrip()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.filter(sessionFilter)
			.queryParam("appid", appid)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("app")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.body("appName", is("TestApp1"))
		;
	}

	@TestTemplate
	void testPostAppRoundTrip() throws Exception
	{
		ApiLoadingApp app = new ApiLoadingApp();
		app.setAppType("loading");
		app.setAppName("TestApp2");
		app.setLastModified(Date.from(Instant.parse("2021-01-01T00:00:00Z")));
		app.setComment("Test comment");

		ObjectMapper objectMapper = new ObjectMapper();

		String appJson = objectMapper.writeValueAsString(app);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.body(appJson)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("app")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		Long appId = getAppId(app.getAppName());

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.queryParam("appid", appId)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("app")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testDeleteAppRoundTrip() throws Exception
	{
		ApiLoadingApp app = new ApiLoadingApp();
		app.setAppType("loading");
		app.setAppName("TestApp3");
		app.setProperties(properties);
		app.setLastModified(Date.from(Instant.parse("2021-01-01T00:00:00Z")));
		app.setComment("Test comment");

		ObjectMapper objectMapper = new ObjectMapper();

		String appJson = objectMapper.writeValueAsString(app);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.body(appJson)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("app")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		Long appId = getAppId(app.getAppName());

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.queryParam("appid", appId)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("app")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testGetAppStatRoundTrip()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("appstat")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testGetAppEventsRoundTrip() throws Exception
	{
		ApiLoadingApp app = new ApiLoadingApp();
		app.setAppType("loading");
		app.setAppName("TestApp4");
		app.setProperties(properties);
		app.setLastModified(Date.from(Instant.parse("2021-01-01T00:00:00Z")));
		app.setComment("Test comment");

		ObjectMapper objectMapper = new ObjectMapper();

		String appJson = objectMapper.writeValueAsString(app);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.contentType(MediaType.APPLICATION_JSON)
			.body(appJson)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("app")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		Long appId = getAppId(app.getAppName());

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.contentType(MediaType.APPLICATION_JSON)
			.queryParam("appid", appId)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("appstart")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.filter(sessionFilter)
			.queryParam("appid", appId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("appevents")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.contentType(MediaType.APPLICATION_JSON)
			.queryParam("appid", appId)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("appstop")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.queryParam("appid", appId)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("app")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testPostAppStartRoundTrip()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.contentType(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.queryParam("appid", appid)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("appstart")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.contentType(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.queryParam("appid", appid)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("appstop")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
	;
	}

	@TestTemplate
	void testPostAppStopRoundTrip()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.filter(sessionFilter)
			.queryParam("appid", appid)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("appstart")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.filter(sessionFilter)
			.queryParam("appid", appid)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("appstop")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	private Long getAppId(String appName)
	{
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("apprefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		for (int i = 0; i < response.body().jsonPath().getList("").size(); i++)
		{

			if ((response.body().jsonPath().getString("[" + i + "].appName")).equalsIgnoreCase(appName))
			{
				return response.body().jsonPath().getLong("[" + i + "].appId");
			}
		}
		return null;
	}

}
