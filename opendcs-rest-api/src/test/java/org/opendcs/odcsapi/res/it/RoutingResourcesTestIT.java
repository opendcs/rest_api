package org.opendcs.odcsapi.res.it;

import java.sql.Date;
import java.util.Base64;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.session.SessionFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendcs.odcsapi.beans.ApiRouting;
import org.opendcs.odcsapi.beans.ApiScheduleEntry;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.sec.basicauth.Credentials;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@Tag("integration")
@ExtendWith(DatabaseContextProvider.class)
final class RoutingResourcesTestIT
{

	private static String credentialsJson = null;
	private static final String AUTH_HEADER_PREFIX = "Basic ";
	private static SessionFilter sessionFilter;
	private static final long ROUTING_ID = 18892151L;
	private static final long SCHEDULE_ID = 71829L;

	@BeforeAll
	static void setUp()
	{
		DbInterface.decodesProperties.setProperty("opendcs.rest.api.authorization.type", "basic");
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
	}

	@BeforeEach
	void setUpEach() throws Exception
	{

		ObjectMapper objectMapper = new ObjectMapper();
		ApiRouting apiRouting = new ApiRouting();
		apiRouting.setRoutingId(ROUTING_ID);
		apiRouting.setName("Test Routing");
		apiRouting.setDestinationType("TSDB");
		apiRouting.setLastModified(Date.valueOf("2021-01-01T12:15:34Z"));

		String routingJson = objectMapper.writeValueAsString(apiRouting);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.queryParam("routingid", ROUTING_ID)
			.filter(sessionFilter)
			.body(routingJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("routing")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		ApiScheduleEntry apiScheduleEntry = new ApiScheduleEntry();
		apiScheduleEntry.setRunInterval("60");
		apiScheduleEntry.setEnabled(true);
		apiScheduleEntry.setTimeZone("UTC");
		apiScheduleEntry.setAppId(55112L);
		apiScheduleEntry.setAppName("Test App");
		apiScheduleEntry.setLastModified(Date.valueOf("2021-01-01T12:15:34Z"));
		apiScheduleEntry.setRoutingSpecName("Test Routing");
		apiScheduleEntry.setStartTime(Date.valueOf("2021-01-01T12:15:34Z"));

		String scheduleJson = objectMapper.writeValueAsString(apiScheduleEntry);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.queryParam("schedule", SCHEDULE_ID)
			.filter(sessionFilter)
			.body(scheduleJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("schedule")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@AfterEach
	void tearDownEach()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.queryParam("routingid", ROUTING_ID)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("routing")
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
			.queryParam("scheduleid", SCHEDULE_ID)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("schedule")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testGetRoutingRefs()
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
			.get("routingrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testGetRouting()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.queryParam("routingid", ROUTING_ID)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("routing")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testPostAndDeleteRouting() throws Exception
	{
		ObjectMapper objectMapper = new ObjectMapper();
		ApiRouting apiRouting = new ApiRouting();
		apiRouting.setRoutingId(66635L);
		apiRouting.setName("Test Routing 2");
		apiRouting.setDestinationType("TSDB 2");
		apiRouting.setLastModified(Date.valueOf("2021-01-02T12:15:34Z"));

		String routingJson = objectMapper.writeValueAsString(apiRouting);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.queryParam("routingid", apiRouting.getRoutingId())
			.filter(sessionFilter)
			.body(routingJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("routing")
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
			.queryParam("routingid", apiRouting.getRoutingId())
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("routing")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testGetSchedule()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.queryParam("scheduleid", SCHEDULE_ID)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("schedule")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testGetRoutingStatus()
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
			.get("routingstatus")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testGetRoutingExecStatus()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.queryParam("scheduleentryid", SCHEDULE_ID)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("routingexecstatus")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testGetDACQEvents()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
		// TODO: Add query parameter values
			.queryParam("appid", "1")
			.queryParam("routingexecid", "1")
			.queryParam("platformid", "1")
			.queryParam("backlog", "1")
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("dacqevents")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}
}
