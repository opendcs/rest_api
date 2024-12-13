package org.opendcs.odcsapi.res.it.opentsdb;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendcs.odcsapi.beans.ApiPlatform;
import org.opendcs.odcsapi.beans.ApiPlatformSensor;
import org.opendcs.odcsapi.beans.ApiSite;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;
import org.opendcs.odcsapi.fixtures.ResourcesTestBase;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@Tag("integration")
@ExtendWith(DatabaseContextProvider.class)
final class PlatformResourcesIT extends ResourcesTestBase
{
	private static SessionFilter sessionFilter;
	private static final ObjectMapper mapper = new ObjectMapper();
	private static Long platformId;
	private static Long siteId;

	@BeforeAll
	static void setUpAll()
	{
		sessionFilter = new SessionFilter();
	}

	@BeforeEach
	void setUp() throws Exception
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

		ApiSite site = new ApiSite();
		site.setState("CA");
		site.setLocationtype("stream");
		site.setPublicName("Test Site");
		site.setElevation(100.0);
		site.setElevUnits("m");
		site.setActive(true);
		site.setTimezone("UTC");
		HashMap<String, String> siteNames = new HashMap<>();
		siteNames.put("stream", "Test Site");
		site.setSitenames(siteNames);
		site.setDescription("This is a test site");
		site.setCountry("US");
		site.setLongitude("-121.0");
		site.setLatitude("37.0");

		String siteJson = mapper.writeValueAsString(site);

		given()
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
		;

		// TODO: Fix the siterefs endpoint
		siteId = getSiteId(site.getPublicName());

		ApiPlatform platform = new ApiPlatform();
		platform.setLastModified(Date.from(Instant.parse("2021-08-01T00:00:00Z")));
		platform.setProduction(true);
		platform.setDescription("Test Platform");
		platform.setName("USGS-07227448");
		platform.setDesignator("07227448");
		platform.setAgency("USGS");
		Properties props = new Properties();
		props.setProperty("siteName", "Test Site");
		platform.setProperties(props);
		if (siteId != null)
		{
			platform.setSiteId(siteId);
		}

		// TODO: Add sensors, currently causes a NPE due to missing data
		ArrayList<ApiPlatformSensor> sensors = new ArrayList<>();
		ApiPlatformSensor sensor = new ApiPlatformSensor();
		sensor.setSensorNum(889151231);
		sensor.setMax(100.0);
		sensor.setUsgsDdno(892410);
		sensor.setMin(0.0);
		sensors.add(sensor);
		Properties sensorProps = new Properties();
		sensorProps.setProperty("sensorName", "Test Sensor");
		sensor.setSensorProps(sensorProps);
//		platform.setPlatformSensors(sensors);

		String platformJson = mapper.writeValueAsString(platform);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.body(platformJson)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("platform")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		platformId = getPlatformId(platform.getName());
	}

	@AfterEach
	void tearDown()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
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
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		// TODO: Fix the siterefs endpoint
//		given()
//			.log().ifValidationFails(LogDetail.ALL, true)
//			.accept(MediaType.APPLICATION_JSON)
//			.contentType(MediaType.APPLICATION_JSON)
//			.header("Authorization", authHeader)
//			.queryParam("siteid", siteId)
//			.filter(sessionFilter)
//		.when()
//			.redirects().follow(true)
//			.redirects().max(3)
//			.delete("site")
//		.then()
//			.log().ifValidationFails(LogDetail.ALL, true)
//		.assertThat()
//			.statusCode(is(HttpServletResponse.SC_OK))
//		;
	}

	@AfterAll
	static void logout()
	{
		SessionFilter sessionFilter = new SessionFilter();
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
	void testGetPlatformRefs()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("platformrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testGetPlatform()
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
			.get("platform")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testPostAndDeletePlatform() throws Exception
	{
		ApiPlatform platform = new ApiPlatform();
		platform.setLastModified(Date.from(Instant.parse("2021-08-02T00:00:00Z")));
		platform.setProduction(true);
		platform.setDescription("Test Platform 1");
		platform.setName("USGS-07227999");
		platform.setDesignator("07227999");
		platform.setAgency("USGS");
		Properties props = new Properties();
		props.setProperty("siteName", "Test Site 1");
		platform.setProperties(props);

		// TODO: Add sensors, currently causes a NPE due to missing data
		ArrayList<ApiPlatformSensor> sensors = new ArrayList<>();
		ApiPlatformSensor sensor = new ApiPlatformSensor();
		sensor.setSensorNum(1);
		sensor.setMax(1100.0);
		sensor.setUsgsDdno(892420);
		sensor.setMin(10.0);
		sensors.add(sensor);
//		platform.setPlatformSensors(sensors);

		String platformJson = mapper.writeValueAsString(platform);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.body(platformJson)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("platform")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		Long platId = getPlatformId(platform.getName());

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("platformid", platId)
			.filter(sessionFilter)
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

	@TestTemplate
	void testGetPlatformStats()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("netlistid", platformId)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("platformstat")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	private Long getPlatformId(String appName)
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
			.get("platformrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		// TODO: This is a temporary workaround until the siterefs endpoint is fixed
		String expected;
		if (appName.endsWith("07227448"))
		{
			expected = "unknownSite-07227448";
		} else {
			expected = "unknownSite-07227999";
		}

		if ((response.body().jsonPath().getString(expected + ".name")).equalsIgnoreCase(expected))
		{
			return response.body().jsonPath().getLong(expected + ".platformId");
		}

		return null;
	}

}
