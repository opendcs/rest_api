package org.opendcs.odcsapi.res.it;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.session.SessionFilter;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendcs.odcsapi.beans.ApiUnit;
import org.opendcs.odcsapi.beans.ApiUnitConverter;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;
import org.opendcs.odcsapi.fixtures.ResourcesTestBase;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@Tag("integration")
@ExtendWith(DatabaseContextProvider.class)
final class DatatypeUnitResourcesIT extends ResourcesTestBase
{
	private static SessionFilter sessionFilter;
	private static final ObjectMapper object = new ObjectMapper();

	@BeforeEach
	void setUp()
	{
		setUpCreds();

		sessionFilter = new SessionFilter();
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
	}

	@AfterAll
	static void tearDown()
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
			.delete("logout")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;
	}

	@TestTemplate
	void testDataTypeList()
	{
		// TODO: add appropriate query parameter values
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.header("Authorization", authHeader)
			.accept(MediaType.APPLICATION_JSON)
			.queryParam("standard", "CWMS")
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("datatypelist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testUnitList()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("unitlist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testPostAndDeleteEngineeringUnit() throws Exception
	{
		ApiUnit unit = new ApiUnit();
		unit.setAbbr("m");
		unit.setFamily("length");
		unit.setName("Meter");
		unit.setMeasures("m");
		String fromUnit = "ft";

		String unitJson = object.writeValueAsString(unit);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("fromabbr", fromUnit)
			.body(unitJson)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("eu")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("abbr", unit.getAbbr())
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("eu")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testUnitConverterList()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("euconvlist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@Disabled("This test is failing because the unit converter is not saved in the database")
	@TestTemplate
	void testPostAndDeleteUnitConv() throws Exception
	{
		ApiUnitConverter unitConv = new ApiUnitConverter();
		unitConv.setFromAbbr("ft");
		unitConv.setToAbbr("m");
		unitConv.setUcId(88532L);
		unitConv.setAlgorithm("ft * 0.3048");
		unitConv.setA(15.3048);
		unitConv.setB(21.0);
		unitConv.setC(40.0);
		unitConv.setD(30.0);

		String unitJson = object.writeValueAsString(unitConv);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.contentType(MediaType.APPLICATION_JSON)
			.body(unitJson)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("euconv")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		Long dataId = getDataId(unitConv.getFromAbbr());

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("euconvid", dataId)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("euconv")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	private Long getDataId(String fromAbbr)
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
			.get("euconvlist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		for (int i = 0; i < response.body().jsonPath().getList("").size(); i++)
		{

			if ((response.body().jsonPath().getString("[" + i + "].fromAbbr")).equalsIgnoreCase(fromAbbr))
			{
				return response.body().jsonPath().getLong("[" + i + "].UcId");
			}
		}
		return null;
	}
}
