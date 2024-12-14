package org.opendcs.odcsapi.res.it;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Properties;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.session.SessionFilter;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendcs.odcsapi.beans.ApiRouting;
import org.opendcs.odcsapi.beans.ApiScheduleEntry;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;
import org.opendcs.odcsapi.fixtures.ResourcesTestBase;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@Tag("integration")
@ExtendWith(DatabaseContextProvider.class)
final class RoutingResourcesTestIT extends ResourcesTestBase
{

	private static SessionFilter sessionFilter;
	private static Long routingId;
	private static Long scheduleId;
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeAll
	static void setUp()
	{
		sessionFilter = new SessionFilter();
	}

	@BeforeEach
	void setUpEach() throws Exception
	{
		setUpCreds();
		
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
		
		ApiRouting apiRouting = new ApiRouting();
		apiRouting.setName("Test Routing");
		apiRouting.setDestinationType("TSDB");
		apiRouting.setDataSourceName("Test DS");
		apiRouting.setDataSourceId(1L);
		apiRouting.setEnableEquations(true);
		apiRouting.setOutputFormat("JSON");
		apiRouting.setOutputTZ("UTC");
		apiRouting.setPresGroupName("Test Pres Group");
		apiRouting.setProduction(false);
		apiRouting.setDestinationArg("Test Arg");
		apiRouting.setDestinationType("TSDB");
		apiRouting.setLastModified(Date.from(Instant.parse("2021-01-01T12:15:34Z")));
		apiRouting.setRoutingId(1L);
		apiRouting.setSince("2021-01-01T12:15:34Z");
		apiRouting.setUntil("2021-01-01T12:15:34Z");

		String routingJson = objectMapper.writeValueAsString(apiRouting);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
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

		routingId = getRoutingId(apiRouting.getName());

		ApiScheduleEntry apiScheduleEntry = new ApiScheduleEntry();
		apiScheduleEntry.setRunInterval("60");
		apiScheduleEntry.setName("Test Schedule");
		apiScheduleEntry.setRoutingSpecName("Test Routing");
		apiScheduleEntry.setRoutingSpecId(routingId);
		apiScheduleEntry.setEnabled(true);
		apiScheduleEntry.setTimeZone("UTC");
		apiScheduleEntry.setAppId(55112L);
		apiScheduleEntry.setAppName("Test App");
		apiScheduleEntry.setLastModified(Date.from(Instant.parse("2021-01-01T12:15:34Z")));
		apiScheduleEntry.setStartTime(Date.from(Instant.parse("2021-01-01T12:15:34Z")));

		String scheduleJson = objectMapper.writeValueAsString(apiScheduleEntry);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
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

		scheduleId = getScheduleId(apiScheduleEntry.getAppName());
	}

	@AfterEach
	void tearDownEach()
	{
//		given()
//			.log().ifValidationFails(LogDetail.ALL, true)
//			.accept(MediaType.APPLICATION_JSON)
//			.contentType(MediaType.APPLICATION_JSON)
//			.header("Authorization", authHeader)
//			.queryParam("routingid", routingId)
//			.filter(sessionFilter)
//		.when()
//			.redirects().follow(true)
//			.redirects().max(3)
//			.delete("routing")
//		.then()
//			.log().ifValidationFails(LogDetail.ALL, true)
//		.assertThat()
//			.statusCode(is(HttpServletResponse.SC_OK))
//		;
//
//		given()
//			.log().ifValidationFails(LogDetail.ALL, true)
//			.accept(MediaType.APPLICATION_JSON)
//			.contentType(MediaType.APPLICATION_JSON)
//			.header("Authorization", authHeader)
//			.queryParam("scheduleid", scheduleId)
//			.filter(sessionFilter)
//		.when()
//			.redirects().follow(true)
//			.redirects().max(3)
//			.delete("schedule")
//		.then()
//			.log().ifValidationFails(LogDetail.ALL, true)
//		.assertThat()
//			.statusCode(is(HttpServletResponse.SC_OK))
//		;
	}

	@TestTemplate
	void testGetRoutingRefs()
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
			.header("Authorization", authHeader)
			.queryParam("routingid", routingId)
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
		ApiRouting apiRouting = new ApiRouting();
		apiRouting.setName("Test Routing 2");
		apiRouting.setDestinationType("TSDB 2");
		apiRouting.setDataSourceName("Test DS");
		apiRouting.setLastModified(Date.from(Instant.parse("2021-01-02T12:15:34Z")));
		apiRouting.setEnableEquations(true);
		apiRouting.setOutputFormat("JSON");
		apiRouting.setOutputTZ("UTC");
		apiRouting.setPresGroupName("Test Pres Group");
		apiRouting.setProduction(false);
		apiRouting.setDestinationArg("Test Arg");
		apiRouting.setDestinationType("TSDB");
		apiRouting.setSince("2021-01-01T12:15:34Z");
		apiRouting.setUntil("2021-01-01T12:15:34Z");
		Properties properties = new Properties();
		properties.setProperty("test", "test1");
		apiRouting.setProperties(properties);
		ArrayList<Integer> goesChannels = new ArrayList<>();
		goesChannels.add(1);
		goesChannels.add(2);
		goesChannels.add(3);
		apiRouting.setGoesChannels(goesChannels);
		apiRouting.setGoesRandom(true);
		ArrayList<String> netlistNames = new ArrayList<>();
		netlistNames.add("Test Netlist 1");
		netlistNames.add("Test Netlist 2");
		netlistNames.add("Test Netlist 3");
		apiRouting.setNetlistNames(netlistNames);
		apiRouting.setGoesSelfTimed(false);
		apiRouting.setQualityNotifications(true);

		String routingJson = objectMapper.writeValueAsString(apiRouting);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
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

		Long newRoutingId = getRoutingId(apiRouting.getName());

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("routingid", newRoutingId)
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
			.header("Authorization", authHeader)
			.queryParam("scheduleid", scheduleId)
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
			.header("Authorization", authHeader)
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
			.header("Authorization", authHeader)
			.queryParam("scheduleentryid", scheduleId)
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
			.header("Authorization", authHeader)
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

	@TestTemplate
	void testPostAndDeleteSchedule() throws Exception
	{
		ApiScheduleEntry apiScheduleEntry = new ApiScheduleEntry();
		apiScheduleEntry.setRunInterval("62");
		apiScheduleEntry.setName("Test Schedule 2");
		apiScheduleEntry.setRoutingSpecName("Test Routing 2");
		apiScheduleEntry.setRoutingSpecId(routingId);
		apiScheduleEntry.setEnabled(true);
		apiScheduleEntry.setTimeZone("UTC");
		apiScheduleEntry.setAppId(55113L);
		apiScheduleEntry.setAppName("Test App 2");
		apiScheduleEntry.setLastModified(Date.from(Instant.parse("2021-02-01T12:15:34Z")));
		apiScheduleEntry.setStartTime(Date.from(Instant.parse("2021-01-02T12:15:34Z")));

		String scheduleJson = objectMapper.writeValueAsString(apiScheduleEntry);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
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

		Long newScheduleId = getScheduleId(apiScheduleEntry.getAppName());

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("scheduleid", newScheduleId)
			.filter(sessionFilter)
			.body(scheduleJson)
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

	private Long getRoutingId(String routName)
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
			.get("routingrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;
		if (response.body().jsonPath().getList("").isEmpty())
		{
			return null;
		}


		for (int i = 0; i < response.body().jsonPath().getList("").size(); i++)
		{

			if ((response.body().jsonPath().getString("[" + i + "].name")).equalsIgnoreCase(routName))
			{
				return response.body().jsonPath().getLong("[" + i + "].routingId");
			}
		}
		return null;
	}

	private Long getScheduleId(String scheduleName)
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
			.get("schedulerefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		for (int i = 0; i < response.body().jsonPath().getList("").size(); i++)
		{

			if ((response.body().jsonPath().getString("[" + i + "].name")).equalsIgnoreCase(scheduleName))
			{
				return response.body().jsonPath().getLong("[" + i + "].scheduleId");
			}
		}
		return null;
	}
}
