package org.opendcs.odcsapi.res.it;

import java.sql.Date;
import java.time.Instant;
import java.util.Base64;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.session.SessionFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendcs.odcsapi.beans.ApiLoadingApp;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.sec.basicauth.Credentials;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@Tag("integration")
@ExtendWith(DatabaseContextProvider.class)
final class AppResourcesIT
{
	private static final long APPID = 29965L;
	private static String credentialsJson = null;
	private static final String AUTH_HEADER_PREFIX = "Basic ";
	private static SessionFilter sessionFilter;

	@BeforeEach
	void setUp() throws Exception
	{
		DbInterface.decodesProperties.setProperty("opendcs.rest.api.authorization.type", "basic");
		ObjectMapper objectMapper = new ObjectMapper();

		Credentials credentials = new Credentials();
		credentials.setUsername("tsdbadm");
		credentials.setPassword("postgres_pass");

		credentialsJson = Base64.getEncoder()
				.encodeToString(String.format("%s:%s", credentials.getUsername(), credentials.getPassword()).getBytes());

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

		ApiLoadingApp app = new ApiLoadingApp();
		app.setAppType("loading");
		app.setAppName("TestApp");
		app.setAppId(APPID);
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
	}

	@AfterEach
	void tearDown()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.queryParam("appid", APPID)
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
			.queryParam("appid", "1")
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("app")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testPostAppRoundTrip() throws Exception
	{
		ApiLoadingApp app = new ApiLoadingApp();
		app.setAppType("loading");
		app.setAppName("TestApp");
		app.setAppId(9965L);
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
	}

	@TestTemplate
	void testDeleteAppRoundTrip() throws Exception
	{
		ApiLoadingApp app = new ApiLoadingApp();
		app.setAppType("loading");
		app.setAppName("TestApp");
		app.setAppId(9965L);
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

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.queryParam("appid", app.getAppId())
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
		app.setAppName("TestApp");
		app.setAppId(9965L);
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

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.filter(sessionFilter)
			.queryParam("appid", app.getAppId())
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
			.queryParam("appid", app.getAppId())
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
			.queryParam("appid", APPID)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("appstart")
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
			.queryParam("appid", APPID)
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

}
