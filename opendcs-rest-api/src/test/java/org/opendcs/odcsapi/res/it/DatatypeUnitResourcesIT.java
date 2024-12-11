package org.opendcs.odcsapi.res.it;

import java.util.Base64;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.session.SessionFilter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendcs.odcsapi.beans.ApiUnit;
import org.opendcs.odcsapi.beans.ApiUnitConverter;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.sec.basicauth.Credentials;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@Tag("integration")
@ExtendWith(DatabaseContextProvider.class)
final class DatatypeUnitResourcesIT
{
	private static String credentialsJson = null;
	private static final String AUTH_HEADER_PREFIX = "Basic ";
	private static SessionFilter sessionFilter;
	private static final ObjectMapper object = new ObjectMapper();

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

	@TestTemplate
	void testDataTypeList()
	{
		// TODO: add appropriate query parameter values
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
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
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
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
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
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
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
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
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
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
		unitConv.setE(20.0);
		unitConv.setF(10.0);

		String unitJson = object.writeValueAsString(unitConv);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
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
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.queryParam("euconvid", unitConv.getUcId())
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
}
