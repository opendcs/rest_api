package org.opendcs.odcsapi.res.it;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import decodes.cwms.CwmsTsId;
import decodes.db.DatabaseException;
import decodes.db.Site;
import decodes.sql.DbKey;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.TimeSeriesIdentifier;
import ilex.var.TimedVariable;
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
import org.opendcs.odcsapi.beans.ApiSite;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;
import org.opendcs.odcsapi.res.ObjectMapperContextResolver;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integration-opentsdb-only")
@ExtendWith(DatabaseContextProvider.class)
final class TimeSeriesResourcesIT extends BaseIT
{
	private static SessionFilter sessionFilter;
	private Long intervalId;
	private Long tsGroupId;
	private Long tsId;
	private Long siteId;

	@BeforeEach
	void setUp() throws Exception
	{
		setUpCreds();
		sessionFilter = new SessionFilter();
		authenticate(sessionFilter);

		// create a site
		Site tsSite = map(storeSite("ts_site_insert_data.json"));

		TimeSeriesIdentifier identifier = new CwmsTsId();
		identifier.setUniqueString("test");
		identifier.setSite(tsSite);
		CTimeSeries ts = new CTimeSeries(identifier);

		ArrayList<TimedVariable> tvs = new ArrayList<>();
		tvs.add(new TimedVariable(new Date(), 2.2, 0));
		tvs.add(new TimedVariable(new Date(), 3.3, 0));
		tvs.add(new TimedVariable(new Date(), 18, 0));
		tvs.add(new TimedVariable(new Date(), 20, 0));

		// TODO: Add ts storage here!

		tsId = ts.getTimeSeriesIdentifier().getKey().getValue();

		String intervalJson = getJsonFromResource("ts_interval_insert_data.json");

		// create interval
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.body(intervalJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("interval")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		intervalId = response.body().jsonPath().getLong("intervalId");

		String groupJson = getJsonFromResource("ts_group_insert_data.json");

		// create ts group
		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.body(groupJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("tsgroup")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		tsGroupId = response.body().jsonPath().getLong("groupId");
	}

	@AfterEach
	void tearDown()
	{
		// Delete the ts group
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.queryParam("groupid", tsGroupId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("tsgroup")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// delete interval
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.queryParam("intvid", intervalId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("interval")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		tearDownSite(siteId);

		// TODO: Add ts deletion here!

		logout(sessionFilter);
	}

	@TestTemplate
	void testGetTSRefs()
	{
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("tsrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();

		// TODO: match here

		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.queryParam("active", true)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("tsrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		actual = response.body().jsonPath();

		// TODO: match here
	}

	@TestTemplate
	void testGetTSSpec()
	{
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("tsspec")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();

		// TODO: match here
	}

	@TestTemplate
	void testGetTSData()
	{
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
				.queryParam("key", tsId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("tsdata")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();

		// TODO: match here
	}

	@TestTemplate
	void testGetIntervals()
	{
		JsonPath expected = getJsonPathFromResource("ts_interval_insert_data.json");

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("intervals")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();

		List<Map<String, Object>> actualList = actual.getList("");
		Map<String, Object> expectedMap = expected.getMap("");

		for (Map<String, Object> actualMap : actualList)
		{
			if (actualMap.get("name").equals(expectedMap.get("name")))
			{
				assertEquals(expectedMap.get("name"), actualMap.get("name"));
				assertEquals(expectedMap.get("calConstant").toString().toUpperCase(),
						actualMap.get("calConstant").toString().toUpperCase());
				assertEquals(expectedMap.get("calMultiplier"), actualMap.get("calMultiplier"));
			}
		}
	}

	@TestTemplate
	void testPostAndDeleteInterval() throws Exception
	{
		String intervalJson = getJsonFromResource("ts_interval_insert_data.json");
		JsonPath expected = new JsonPath(intervalJson);

		// create interval
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.body(intervalJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("interval")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		Long newIntervalId = response.body().jsonPath().getLong("intervalId");

		// retrieve intervals and assert it is in the list
		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.queryParam("id", newIntervalId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("intervals")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath(); // this will be a list

		List<Map<String, Object>> actualList = actual.getList("");
		Map<String, Object> expectedMap = expected.getMap("");

		for (Map<String, Object> actualMap : actualList)
		{
			if (actualMap.get("name").equals(expectedMap.get("name")))
			{
				assertEquals(expectedMap.get("name"), actualMap.get("name"));
				assertEquals(expectedMap.get("calConstant").toString().toUpperCase(),
						actualMap.get("calConstant").toString().toUpperCase());
				assertEquals(expectedMap.get("calMultiplier"), actualMap.get("calMultiplier"));
			}
		}

		// delete interval
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.queryParam("intvid", newIntervalId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("interval")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// retrieve intervals, assert not in list
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("intervals")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		actual = response.body().jsonPath();
		actualList = actual.getList("");

		boolean found = false;
		for (Map<String, Object> actualMap : actualList)
		{
			if (actualMap.get("name").equals(expectedMap.get("name"))
					&& expectedMap.get("name").equals(actualMap.get("name"))
					&& expectedMap.get("calConstant").equals(actualMap.get("calConstant"))
					&& expectedMap.get("calMultiplier").equals(actualMap.get("calMultiplier")))
			{
				found = true;
			}
		}
		assertFalse(found);
	}

	@TestTemplate
	void testGetTSGroupRefs()
	{
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("tsgrouprefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();

		// TODO: match here
	}

	@TestTemplate
	void testGetTSGroup()
	{
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.queryParam("groupid", tsGroupId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("tsgroup")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();

		// TODO: match here
	}

	@TestTemplate
	void testPostAndDeleteTSGroup() throws Exception
	{
		String groupJson = getJsonFromResource("ts_group_post_delete_insert_data.json");

		// create ts group
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.body(groupJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("tsgroup")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		Long newTSGroupId = response.body().jsonPath().getLong("groupId");

		// retrieve ts group and assert data matches
		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.queryParam("groupid", newTSGroupId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("tsgroup")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();

		// TODO: match here

		// delete ts group
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.queryParam("groupid", newTSGroupId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("tsgroup")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// retrieve ts group and assert not found
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.queryParam("groupid", newTSGroupId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("tsgroup")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NOT_FOUND))
		;
	}

	private ApiSite storeSite(String jsonPath) throws Exception
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

		siteId = response.body().jsonPath().getLong("siteId");

		ObjectMapper mapper = new ObjectMapperContextResolver().getContext(ApiSite.class);
		ApiSite retVal = mapper.readValue(siteJson, ApiSite.class);
		retVal.setSiteId(siteId);
		return retVal;
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

	private Site map(ApiSite apiSite) throws DatabaseException
	{
		Site site = new Site();
		site.setPublicName(apiSite.getPublicName());
		site.setLocationType(apiSite.getLocationType());
		site.setElevation(apiSite.getElevation());
		site.setElevationUnits(apiSite.getElevUnits());
		site.latitude = apiSite.getLatitude();
		site.longitude = apiSite.getLongitude();
		site.setId(DbKey.createDbKey(apiSite.getSiteId()));
		site.setLastModifyTime(apiSite.getLastModified());
		site.setDescription(apiSite.getDescription());
		site.timeZoneAbbr = apiSite.getTimezone();
		site.nearestCity = apiSite.getNearestCity();
		site.state = apiSite.getState();
		site.country = apiSite.getCountry();
		site.region = apiSite.getRegion();
		site.setActive(apiSite.isActive());
		for (String props : apiSite.getProperties().stringPropertyNames())
		{
			site.setProperty(props, apiSite.getProperties().getProperty(props));
		}
		return site;
	}
}
