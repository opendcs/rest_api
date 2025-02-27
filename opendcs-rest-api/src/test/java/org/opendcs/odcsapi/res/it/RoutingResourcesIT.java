package org.opendcs.odcsapi.res.it;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import decodes.db.ScheduleEntryStatus;
import decodes.polling.DacqEvent;
import decodes.sql.DbKey;
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
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@ExtendWith(DatabaseContextProvider.class)
final class RoutingResourcesIT extends BaseIT
{
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static SessionFilter sessionFilter;
	private static Long routingId;
	private static Long scheduleId;
	private static Long dataSourceId;
	private static Long appId;
	private static Long siteId;
	private static Long configId;
	private static String appName;
	private static Long platformId;
	private static Long scheduleEntryStatusId;
	private static int OFFSET_IN_MILLI;

	@BeforeEach
	void setUp() throws Exception
	{
		// get the timezone offset to account for CWMS times being returned with a local timezone offset
		String timeZone = System.getProperty("user.timezone");
		if (timeZone == null || timeZone.isEmpty())
		{
			timeZone = TimeZone.getDefault().getID();
		}
		OFFSET_IN_MILLI = TimeZone.getTimeZone(timeZone).getRawOffset();

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
			.statusCode(is(HttpServletResponse.SC_CREATED))
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

		ScheduleEntryStatus status = new ScheduleEntryStatus(DbKey.NullKey);
		status.setScheduleEntryId(DbKey.createDbKey(scheduleId));
		status.setHostname("localhost");
		status.setRunStatus("Running");
		status.setLastMessageTime(Date.from(Instant.parse("2025-01-31T00:00:00Z")));
		status.setNumMessages(10);
		status.setNumDecodesErrors(1);
		status.setNumPlatforms(1);
		status.setRunStart(Date.from(Instant.parse("2025-01-29T00:00:00Z")));
		status.setRunStop(Date.from(Instant.parse("2025-01-30T00:00:00Z")));
		status.setScheduleEntryName("Test schedule");

		// Insert the schedule entry status
		storeScheduleEntryStatus(status);

		siteId = storeSite("routing_site_insert.json");

		String configJson = getJsonFromResource("routing_config_insert_data.json");

		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.body(configJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("config")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		configId = response.body().jsonPath().getLong("configId");

		platformId = storePlatform("routing_platform_insert.json", siteId, configId);

		// Retrieve the schedule entry status id
		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
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

		scheduleEntryStatusId = DbKey.NullKey.getValue();
		JsonPath responseJson = response.body().jsonPath();
		List<Map<String, Object>> responseList = responseJson.getList("");
		assertFalse(responseList.isEmpty());
		boolean found = false;
		for (Map<String, Object> responseMap : responseList)
		{
			if (((Integer) responseMap.get("scheduleEntryId")).longValue() == scheduleId)
			{
				scheduleEntryStatusId = ((Integer) responseMap.get("routingExecId")).longValue();
				found = true;
				break;
			}
		}
		assertTrue(found);

		// Insert the dacq event
		DacqEvent event = new DacqEvent();
		event.setAppId(DbKey.createDbKey(appId));
		event.setEventPriority(1);
		event.setEventText("Test event");
		event.setSubsystem("Routing");
		event.setEventTime(Date.from(Instant.parse("2025-01-31T00:00:00Z")));
		event.setMsgRecvTime(Date.from(Instant.parse("2025-01-31T00:30:00Z")));
		event.setPlatformId(DbKey.createDbKey(platformId));
		event.setScheduleEntryStatusId(DbKey.createDbKey(scheduleEntryStatusId));

		storeDacqEvent(event);
	}

	@AfterEach
	void tearDown() throws Exception
	{
		// Delete the dacq event
		deleteEventsForPlatform(DbKey.createDbKey(platformId));

		// Delete the schedule entry status
		deleteScheduleEntryStatus(DbKey.createDbKey(scheduleEntryStatusId));

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
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
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
		;

		tearDownPlatform(platformId);

		// Delete the config
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("configid", configId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("config")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		tearDownSite(siteId);

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
		assertThat(ZonedDateTime.ofInstant(Instant.ofEpochMilli(expected.getLong("startTime")),
						ZoneId.of("UTC")).toString(),
				anyOf(is(ZonedDateTime.parse(actual.getString("startTime")).plus(OFFSET_IN_MILLI, ChronoUnit.MILLIS).toString()),
						is(ZonedDateTime.parse(actual.getString("startTime")).toString()),
						is(ZonedDateTime.parse(actual.getString("startTime")).minus(OFFSET_IN_MILLI, ChronoUnit.MILLIS).toString())));
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

		boolean found = false;
		for (Map<String, Object> actualItem : actualList)
		{
			if (actualItem.get("name").equals(expected.get("name")))
			{
				assertEquals(expected.get("name"), actualItem.get("name"));
				assertEquals(expected.get("appName"), actualItem.get("appName"));
				assertEquals(expected.get("routingSpecName"), actualItem.get("routingSpecName"));
				assertEquals(expected.get("enabled"), actualItem.get("enabled"));
				found = true;
			}
		}
		assertTrue(found);
	}

	@TestTemplate
	void testGetRoutingExecStatus()
	{
		// Note: Routing spec id is not mapped here!
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
		List<Map<String, Object>> actualList = actual.getList("");

		boolean found = false;
		for (Map<String, Object> actualItem : actualList)
		{
			if (((Integer) actualItem.get("scheduleEntryId")).longValue() == scheduleId)
			{
				assertThat(ZonedDateTime.parse(actualItem.get("runStart").toString()).toInstant().toString(),
					anyOf(is(expected.get("runStart").toString()),
							is(ZonedDateTime.parse(expected.get("runStart")).toInstant().toString()),
						is(ZonedDateTime.parse(expected.get("runStart")).toInstant().plusMillis(OFFSET_IN_MILLI).toString())));
				assertThat(ZonedDateTime.parse(actualItem.get("runStop").toString()).toInstant().toString(),
					anyOf(is(expected.get("runStop").toString()),
						is(ZonedDateTime.parse(expected.get("runStop")).toInstant().toString()),
						is(ZonedDateTime.parse(expected.get("runStop")).toInstant().plusMillis(OFFSET_IN_MILLI).toString())));
				assertEquals(expected.getInt("numMessages"), actualItem.get("numMessages"));
				assertEquals(expected.getInt("numErrors"), actualItem.get("numErrors"));
				assertEquals(expected.getInt("numPlatforms"), actualItem.get("numPlatforms"));
				assertThat(ZonedDateTime.parse(actualItem.get("lastMsgTime").toString()).toInstant().toString(),
					anyOf(is(expected.get("lastMsgTime").toString()),
						is(ZonedDateTime.parse(expected.get("lastMsgTime")).toInstant().toString()),
						is(ZonedDateTime.parse(expected.get("lastMsgTime")).toInstant().plusMillis(OFFSET_IN_MILLI).toString())));
				assertEquals(expected.getString("runStatus"), actualItem.get("runStatus"));
				assertEquals(expected.getString("hostname"), actualItem.get("hostname"));
				assertEquals(expected.getString("lastInput"), actualItem.get("lastInput"));
				assertEquals(expected.getString("lastOutput"), actualItem.get("lastOutput"));

				found = true;
			}
		}
		assertTrue(found);
	}

	@TestTemplate
	void testGetDacqEvents() throws Exception
	{
		ApiDacqEvent event = getDtoFromResource("routing_dacq_events_expected.json", ApiDacqEvent.class);
		event.setAppId(appId);
		event.setAppName(appName);
		event.setRoutingExecId(scheduleEntryStatusId);
		event.setSubsystem("Routing");
		JsonPath expected = new JsonPath(MAPPER.writeValueAsString(event));

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
			.queryParam("routingexecid", scheduleEntryStatusId)
			.queryParam("appid", appId)
			.queryParam("platformid", platformId)
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
		List<Map<String, Object>> actualList = actual.getList("");
		assertFalse(actualList.isEmpty());

		boolean found = false;
		for (Map<String, Object> actualItem : actualList)
		{
			if (((Integer) actualItem.get("platformId")).longValue() == platformId
					&& ((Integer) actualItem.get("appId")).longValue() == appId
					&& ((Integer) actualItem.get("routingExecId")).longValue() == scheduleEntryStatusId)
			{
				// The event time updated based on current time, so we can't compare it directly
				// The app name is not present in the Toolkit DTO, so it won't be returned in the response
				assertEquals(expected.get("priority").toString(), actualItem.get("priority").toString());
				assertEquals(expected.get("subsystem"), actualItem.get("subsystem"));
				Instant messageRcvTime = ZonedDateTime.parse(actualItem.get("msgRecvTime").toString()).toInstant();
				// substring date text to remove timezone and milliseconds
				assertThat(Instant.ofEpochMilli(expected.get("msgRecvTime")).toString(),
						anyOf(is(messageRcvTime.minusMillis(OFFSET_IN_MILLI).toString()),
								is(messageRcvTime.toString()),
								is(messageRcvTime.plusMillis(OFFSET_IN_MILLI).toString())));
				assertEquals(expected.get("eventText").toString(), actualItem.get("eventText").toString());
				found = true;
			}
		}
		assertTrue(found);
	}

	private Long storeSite(String jsonPath) throws Exception
	{
		assertNotNull(jsonPath);
		String siteJson = getJsonFromResource(jsonPath);

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.contentType(MediaType.APPLICATION_JSON)
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

	private void tearDownSite(Long siteId)
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("siteid", siteId)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("site")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("siteid", siteId)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("site")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NOT_FOUND))
		;
	}

	private Long storePlatform(String jsonPath, Long siteId, Long configId) throws Exception
	{
		assertNotNull(jsonPath);
		String platformJson = getJsonFromResource(jsonPath);

		platformJson = platformJson.replace("[SITE_ID]", siteId.toString());
		platformJson = platformJson.replace("[CONFIG_ID]", configId.toString());

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.contentType(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.body(platformJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("platform")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		return response.body().jsonPath().getLong("platformId");
	}

	private void tearDownPlatform(Long platformId)
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("platformid", platformId)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("platform")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("platformid", platformId)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("platform")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			// TODO: This is here to support older implementations of the PlatformResources controller and should be removed
			.statusCode(anyOf(is(HttpServletResponse.SC_NOT_FOUND),
					is(HttpServletResponse.SC_GONE)))
		;
	}
}
