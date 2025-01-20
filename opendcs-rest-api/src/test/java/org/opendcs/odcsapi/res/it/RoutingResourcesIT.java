package org.opendcs.odcsapi.res.it;

import java.time.Instant;
import java.util.List;
import java.util.Map;
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
import org.opendcs.odcsapi.beans.ApiDacqEvent;
import org.opendcs.odcsapi.beans.ApiDataSource;
import org.opendcs.odcsapi.beans.ApiLoadingApp;
import org.opendcs.odcsapi.beans.ApiRouting;
import org.opendcs.odcsapi.beans.ApiRoutingStatus;
import org.opendcs.odcsapi.beans.ApiScheduleEntry;
import org.opendcs.odcsapi.beans.ApiScheduleEntryRef;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration-opentsdb-only")
@ExtendWith(DatabaseContextProvider.class)
final class RoutingResourcesIT extends BaseIT
{
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static SessionFilter sessionFilter;
	private static Long routingId;
	private static Long scheduleId;
	private static Long dataSourceId;
	private static Long appId;
	private static String appName;

	@BeforeEach
	void setUp() throws Exception
	{
		setUpCreds();
		sessionFilter = new SessionFilter();
		authenticate(sessionFilter);

		ApiLoadingApp loadingApp = getDtoFromResource("routing_app_insert_data.json", ApiLoadingApp.class);

		appName = loadingApp.getAppName();

		String loadingAppJson = MAPPER.writeValueAsString(loadingApp);

		// Insert the application data
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.body(loadingAppJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("app")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		appId = response.body().jsonPath().getLong("appId");

		ApiRouting route = getDtoFromResource("routing_insert_data.json", ApiRouting.class);

		ApiDataSource dataSource = getDtoFromResource("routing_datasource_insert_data.json", ApiDataSource.class);

		String dataSourceJson = MAPPER.writeValueAsString(dataSource);

		// Insert the data source
		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.body(dataSourceJson)
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

		dataSourceId = response.body().jsonPath().getLong("dataSourceId");

		route.setDataSourceId(dataSourceId);

		String routingJson = MAPPER.writeValueAsString(route);

		// Insert the routing
		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.body(routingJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("routing")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		routingId = response.body().jsonPath().getLong("routingId");

		ApiScheduleEntry sched = getDtoFromResource("routing_schedule_insert_data.json", ApiScheduleEntry.class);

		sched.setAppName(appName);
		sched.setAppId(appId);
		sched.setRoutingSpecId(routingId);

		String scheduleJson = MAPPER.writeValueAsString(sched);

		// Insert the schedule
		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.body(scheduleJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("schedule")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		scheduleId = response.body().jsonPath().getLong("schedEntryId");
	}

	@AfterEach
	void tearDown()
	{
		if (routingId != null)
		{
			// Delete the routing
			given()
				.log().ifValidationFails(LogDetail.ALL, true)
				.filter(sessionFilter)
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", authHeader)
				.queryParam("routingid", routingId)
			.when()
				.redirects().follow(true)
				.redirects().max(3)
				.delete("routing")
			.then()
				.log().ifValidationFails(LogDetail.ALL, true)
			.assertThat()
				.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
			;
		}

		if (scheduleId != null)
		{
			// Delete the schedule
			given()
				.log().ifValidationFails(LogDetail.ALL, true)
				.filter(sessionFilter)
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", authHeader)
				.queryParam("scheduleid", scheduleId)
			.when()
				.redirects().follow(true)
				.redirects().max(3)
				.delete("schedule")
			.then()
				.log().ifValidationFails(LogDetail.ALL, true)
			.assertThat()
				.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
			;
		}

		// Delete the data source
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("datasourceid", dataSourceId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("datasource")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		// Delete the application
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("appid", appId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("app")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		logout(sessionFilter);
	}


	@TestTemplate
	void testGetRoutingRefs()
	{
		JsonPath expected = getJsonPathFromResource("routing_insert_data.json");

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
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

		JsonPath actual = response.body().jsonPath();
		List<Map<String, Object>> actualList = actual.getList("");
		boolean found = false;
		for (Map<String, Object> actualMap : actualList)
		{
			if (actualMap.get("name").equals(expected.getString("name")))
			{
				assertEquals(expected.getString("name"), actualMap.get("name"));
				assertEquals(expected.getString("dataSourceName"), actualMap.get("dataSourceName"));
				assertEquals(expected.getString("lastModified"), actualMap.get("lastModified"));
				found = true;
			}
		}
		assertTrue(found);
	}

	@TestTemplate
	void testGetRouting()
	{
		JsonPath expected = getJsonPathFromResource("routing_insert_data.json");

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
			.queryParam("routingid", routingId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("routing")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();
		assertEquals(expected.getString("name"), actual.getString("name"));
		assertEquals(expected.getString("destination"), actual.getString("destination"));
		assertEquals(expected.getString("dataSourceName"), actual.getString("dataSourceName"));
		assertEquals(expected.getString("lastModified"), actual.getString("lastModified"));
		assertEquals(expected.getString("destinationType"), actual.getString("destinationType"));
		assertEquals(expected.getString("destinationArg"), actual.getString("destinationArg"));
		assertEquals(expected.getString("enableEquations"), actual.getString("enableEquations"));
		assertEquals(expected.getString("outputFormat"), actual.getString("outputFormat"));
		assertEquals(expected.getString("outputTZ"), actual.getString("outputTZ"));
		assertEquals(expected.getString("presGroupName"), actual.getString("presGroupName"));
		assertEquals(expected.getString("since"), actual.getString("since"));
		assertEquals(expected.getString("until"), actual.getString("until"));
		assertEquals(expected.getString("settlingTimeDelay"), actual.getString("settlingTimeDelay"));
		assertEquals(expected.getString("applyTimeTo"), actual.getString("applyTimeTo"));
		assertEquals(expected.getString("ascendingTime"), actual.getString("ascendingTime"));
		assertEquals(expected.getString("platformIds"), actual.getString("platformIds"));
		assertEquals(expected.getString("platformNames"), actual.getString("platformNames"));
		assertEquals(expected.getString("netlistNames"), actual.getString("netlistNames"));
		assertEquals(expected.getString("goesChannels"), actual.getString("goesChannels"));
		assertEquals(expected.getString("properties"), actual.getString("properties"));
		assertEquals(expected.getString("goesSelfTimed"), actual.getString("goesSelfTimed"));
		assertEquals(expected.getString("goesRandom"), actual.getString("goesRandom"));
		assertEquals(expected.getString("networkDCP"), actual.getString("networkDCP"));
		assertEquals(expected.getString("iridium"), actual.getString("iridium"));
		assertEquals(expected.getString("qualityNotifications"), actual.getString("qualityNotifications"));
		assertEquals(expected.getString("goesSpacecraftCheck"), actual.getString("goesSpacecraftCheck"));
		assertEquals(expected.getString("goesSpacecraftSelection"), actual.getString("goesSpacecraftSelection"));
		assertEquals(expected.getString("parityCheck"), actual.getString("parityCheck"));
		assertEquals(expected.getString("paritySelection"), actual.getString("paritySelection"));
		assertEquals(expected.getString("production"), actual.getString("production"));
	}

	@TestTemplate
	void testPostAndDeleteRouting() throws Exception
	{
		ApiRouting routing = getDtoFromResource("routing_post_delete_insert_data.json", ApiRouting.class);
		routing.setDataSourceId(dataSourceId);

		String routingJson = MAPPER.writeValueAsString(routing);

		// Insert the routing
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.body(routingJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("routing")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		Long newRoutingId = response.body().jsonPath().getLong("routingId");

		JsonPath expected = getJsonPathFromResource("routing_post_delete_expected.json");

		// Get the routing and assert it exists
		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
			.queryParam("routingid", newRoutingId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("routing")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();

		assertEquals(expected.getString("name"), actual.getString("name"));
		assertEquals(expected.getString("dataSourceName"), actual.getString("dataSourceName"));
		assertEquals(expected.getString("destinationType"), actual.getString("destinationType"));
		assertEquals(expected.getString("destinationArg"), actual.getString("destinationArg"));
		assertEquals(expected.getString("enableEquations"), actual.getString("enableEquations"));
		assertEquals(expected.getString("outputFormat"), actual.getString("outputFormat"));
		assertEquals(expected.getString("outputTZ"), actual.getString("outputTZ"));
		assertEquals(expected.getString("presGroupName"), actual.getString("presGroupName"));
		assertEquals(expected.getString("since"), actual.getString("since"));
		assertEquals(expected.getString("until"), actual.getString("until"));
		assertEquals(expected.getString("settlingTimeDelay"), actual.getString("settlingTimeDelay"));
		assertEquals(expected.getString("applyTimeTo"), actual.getString("applyTimeTo"));
		assertEquals(expected.getString("ascendingTime"), actual.getString("ascendingTime"));
		assertEquals(expected.getString("platformIds"), actual.getString("platformIds"));
		assertEquals(expected.getString("platformNames"), actual.getString("platformNames"));
		assertEquals(expected.getString("netlistNames"), actual.getString("netlistNames"));
		assertEquals(expected.getString("goesChannels"), actual.getString("goesChannels"));
		assertEquals(expected.getString("properties"), actual.getString("properties"));
		assertEquals(expected.getString("goesSelfTimed"), actual.getString("goesSelfTimed"));
		assertEquals(expected.getString("goesRandom"), actual.getString("goesRandom"));
		assertEquals(expected.getString("networkDCP"), actual.getString("networkDCP"));
		assertEquals(expected.getString("iridium"), actual.getString("iridium"));
		assertEquals(expected.getString("qualityNotifications"), actual.getString("qualityNotifications"));
		assertEquals(expected.getString("goesSpacecraftCheck"), actual.getString("goesSpacecraftCheck"));
		assertEquals(expected.getString("goesSpacecraftSelection"), actual.getString("goesSpacecraftSelection"));
		assertEquals(expected.getString("parityCheck"), actual.getString("parityCheck"));
		assertEquals(expected.getString("paritySelection"), actual.getString("paritySelection"));
		assertEquals(expected.getString("production"), actual.getString("production"));
		assertEquals(expected.getString("lastModified"), actual.getString("lastModified"));

		// Delete the routing
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
			.queryParam("routingid", newRoutingId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("routing")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// Get the routing and assert it does not exist
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
			.queryParam("routingid", newRoutingId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("routing")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NOT_FOUND))
		;
	}

	@TestTemplate
	void testGetScheduleRefs() throws Exception
	{
		ApiScheduleEntryRef schedule = getDtoFromResource("routing_refs_expected.json",
				ApiScheduleEntryRef.class);
		schedule.setAppName(appName);
		JsonPath expected = new JsonPath(MAPPER.writeValueAsString(schedule));

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
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

		JsonPath actual = response.body().jsonPath();
		List<Map<String, Object>> actualList = actual.getList("");
		boolean found = false;
		for (Map<String, Object> actualMap : actualList)
		{
			if (actualMap.get("name").equals(expected.getString("name")))
			{
				assertEquals(expected.getString("name"), actualMap.get("name"));
				assertEquals(expected.getString("appName"), actualMap.get("appName"));
				assertEquals(expected.getString("routingSpecName"), actualMap.get("routingSpecName"));
				assertEquals(expected.get("enabled"), actualMap.get("enabled"));
				found = true;
			}
		}
		assertTrue(found);
	}

	@TestTemplate
	void testGetSchedule() throws Exception
	{
		ApiScheduleEntry entry = getDtoFromResource("routing_schedule_insert_data.json", ApiScheduleEntry.class);
		entry.setAppId(appId);
		entry.setAppName(appName);
		JsonPath expected = new JsonPath(MAPPER.writeValueAsString(entry));

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
			.queryParam("scheduleid", scheduleId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("schedule")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();
		assertEquals(expected.getString("name"), actual.get("name"));
		assertEquals(expected.getString("appName"), actual.get("appName"));
		assertEquals(expected.getString("routingSpecName"), actual.get("routingSpecName"));
		assertEquals(expected.getBoolean("enabled"), actual.getBoolean("enabled"));
	}

	@TestTemplate
	void testPostAndDeleteSchedule() throws Exception
	{
		ApiScheduleEntry schedule = getDtoFromResource("routing_schedule_post_delete_insert_data.json",
				ApiScheduleEntry.class);

		schedule.setAppName(appName);
		schedule.setAppId(appId);
		schedule.setRoutingSpecId(routingId);
		String scheduleJson = MAPPER.writeValueAsString(schedule);

		// Insert the schedule
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.body(scheduleJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("schedule")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		Long newScheduleId = response.body().jsonPath().getLong("schedEntryId");

		ApiScheduleEntry expectedSchedule = getDtoFromResource("routing_schedule_post_delete_expected.json",
				ApiScheduleEntry.class);
		expectedSchedule.setAppId(appId);
		expectedSchedule.setAppName(appName);
		JsonPath expected = new JsonPath(MAPPER.writeValueAsString(expectedSchedule));

		// Get the schedule and assert it exists
		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
			.queryParam("scheduleid", newScheduleId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("schedule")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();

		assertEquals(expected.getString("name"), actual.getString("name"));
		assertEquals(expected.getString("appName"), actual.getString("appName"));
		assertEquals(expected.getString("routingSpecName"), actual.getString("routingSpecName"));
		assertEquals(expected.getString("enabled"), actual.getString("enabled"));
		assertEquals(Instant.ofEpochMilli(expected.getLong("startTime")).toString() + "[UTC]",
				actual.getString("startTime")); // This comparison requires some manipulation to match up
		assertEquals(expected.getString("timeZone"), actual.getString("timeZone"));
		assertEquals(expected.getString("runInterval"), actual.getString("runInterval"));

		// Delete the schedule
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
			.queryParam("scheduleid", newScheduleId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("schedule")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// Get the schedule and assert it does not exist
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
			.queryParam("scheduleid", newScheduleId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("schedule")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NOT_FOUND))
		;
	}

	@TestTemplate
	void testGetRoutingStatus() throws Exception
	{
		// TODO: setup test to return actual data
		ApiRoutingStatus status = getDtoFromResource("routing_status_expected.json", ApiRoutingStatus.class);
		status.setAppId(appId);
		status.setAppName(appName);
		JsonPath expected = new JsonPath(MAPPER.writeValueAsString(status));

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("routingstatus")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		List<Map<String, Object>> actualList = response.body().jsonPath().getList("");

		for (Map<String, Object> actualItem : actualList)
		{
			if (actualItem.get("name").equals(expected.get("name")))
			{
				assertEquals(expected.get("name"), actualItem.get("name"));
				assertEquals(expected.get("appName"), actualItem.get("appName"));
				assertEquals(expected.get("routingSpecName"), actualItem.get("routingSpecName"));
				assertEquals(expected.get("enabled"), actualItem.get("enabled"));
			}
		}
	}

	@TestTemplate
	void testGetRoutingExecStatus()
	{
		// TODO: Setup tests to give an actual response with data, currently null
		JsonPath expected = getJsonPathFromResource("routing_exec_status_expected.json");

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
			.queryParam("scheduleentryid", scheduleId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("routingexecstatus")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();

		assertEquals(expected.getString("routingName"), actual.getString("routingName"));
		assertEquals(expected.getString("destination"), actual.getString("destination"));
		assertEquals(expected.getString("datasourcename"), actual.getString("datasourcename"));
	}

	@TestTemplate
	void testGetDacqEvents() throws Exception
	{
		// TODO: Create expected JSON, make sure to setup the tests to give an actual response with data
		ApiDacqEvent event = getDtoFromResource("routing_dacq_events_expected.json", ApiDacqEvent.class);
		event.setAppId(appId);
		event.setAppName(appName);
		JsonPath expected = new JsonPath(MAPPER.writeValueAsString(event));

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
			.queryParam("routingexecid", routingId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("dacqevents")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();

		assertEquals(expected.get("routingExecId").toString(), actual.get("routingExecId").toString());
		assertEquals(expected.get("platformId").toString(), actual.get("platformId").toString());
		assertEquals(expected.get("eventTime").toString(), actual.get("eventTime").toString());
		assertEquals(expected.get("priority").toString(), actual.get("priority").toString());
		assertEquals(expected.get("subsystem").toString(), actual.get("subsystem").toString());
		assertEquals(expected.get("appName").toString(), actual.get("appName").toString());
		assertEquals(expected.get("msgRecvTime").toString(), actual.get("msgRecvTime").toString());
		assertEquals(expected.get("eventText").toString(), actual.get("eventText").toString());
		assertEquals(expected.get("appId").toString(), actual.get("appId").toString());
	}
}
