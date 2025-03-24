package org.opendcs.odcsapi.res.it;

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
import org.opendcs.odcsapi.beans.ApiUnit;
import org.opendcs.odcsapi.beans.ApiUnitConverter;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration-opentsdb-only")
@ExtendWith(DatabaseContextProvider.class)
final class DatatypeUnitResourcesIT extends BaseIT
{
	private static SessionFilter sessionFilter;
	private static Long converterId;
	private static String euAbbr;
	private static String euAbbr2;

	@BeforeEach
	public void setUp() throws Exception
	{
		setUpCreds();
		sessionFilter = new SessionFilter();
		authenticate(sessionFilter);

		ObjectMapper mapper = new ObjectMapper();
		ApiUnit eu = getDtoFromResource("datatypeunit_engineering_unit_insert_data.json",
				ApiUnit.class);
		euAbbr = eu.getAbbr();
		String euJson = mapper.writeValueAsString(eu);

		// Store the EU
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(euJson)
			.queryParam("fromabbr", euAbbr)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("eu")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
		;

		ApiUnit eu2 = getDtoFromResource("datatypeunit_engineering_unit_2_insert_data.json",
				ApiUnit.class);
		euAbbr2 = eu2.getAbbr();
		euJson = mapper.writeValueAsString(eu2);

		// Store the EU
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(euJson)
			.queryParam("fromabbr", euAbbr2)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("eu")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
		;

		// retrieve the EU List and assert it contains the EUs
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(euJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("unitlist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();
		List<Map<String, Object>> actualList = actual.getList("");
		boolean foundFirst = false;
		boolean foundSecond = false;
		for (Map<String, Object> item : actualList)
		{
			if (item.get("abbr").equals(euAbbr))
			{
				assertEquals(euAbbr, item.get("abbr"));
				assertEquals(eu.getName(), item.get("name"));
				assertEquals(eu.getFamily(), item.get("family"));
				assertEquals(eu.getMeasures(), item.get("measures"));
				foundFirst = true;
			}
			else if (item.get("abbr").equals(euAbbr2))
			{
				assertEquals(euAbbr2, item.get("abbr"));
				assertEquals(eu2.getName(), item.get("name"));
				assertEquals(eu2.getFamily(), item.get("family"));
				assertEquals(eu2.getMeasures(), item.get("measures"));
				foundSecond = true;
			}
		}
		assertTrue(foundFirst);
		assertTrue(foundSecond);


		String unitConverterJson = getJsonFromResource("datatypeunit_insert_euconv_data.json");

		// Store the EU Converter
		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(unitConverterJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("euconv")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		converterId = response.body().jsonPath().getLong("ucId");
	}

	@AfterEach
	public void tearDown()
	{
		// Delete the EU Converter
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("euconvid", converterId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("euconv")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// Delete the EU
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("abbr", euAbbr2)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("eu")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// Delete the EU
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("abbr", euAbbr)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("eu")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		logout(sessionFilter);
	}

	@TestTemplate
	void testGetDataTypeList()
	{
		JsonPath expected = getJsonPathFromResource("datatypeunit_get_type_list_expected.json");

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("datatypelist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();
		List<Map<String, Object>> actualList = actual.getList("");
		List<Map<String, Object>> expectedList = expected.getList("");

		int numFound = 0;
		for (Map<String, Object> expectedItem : expectedList)
		{
			for (Map<String, Object> actualItem : actualList)
			{
				if (expectedItem.get("displayName").equals(actualItem.get("displayName")))
				{
					assertEquals(expectedItem.get("displayName"), actualItem.get("displayName"));
					assertEquals(expectedItem.get("standard"), actualItem.get("standard"));
					numFound++;
				}
			}
		}
		assertEquals(expectedList.size(), numFound);
	}

	@TestTemplate
	void testGetDataTypeListWithFilter()
	{
		JsonPath expected = getJsonPathFromResource("datatypeunit_get_type_list_expected_filtered.json");

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("standard", "CWMS")
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("datatypelist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();
		List<Map<String, Object>> actualList = actual.getList("");
		List<Map<String, Object>> expectedList = expected.getList("");

		assertEquals(expectedList.size(), actualList.size());

		for(Map<String, Object> expectedItem : expectedList)
		{
			boolean found = false;
			for(Map<String, Object> actualItem : actualList)
			{
				if(expectedItem.get("displayName").equals(actualItem.get("displayName")))
				{
					assertEquals(expectedItem.get("code"), actualItem.get("code"));
					assertEquals(expectedItem.get("displayName"), actualItem.get("displayName"));
					assertEquals(expectedItem.get("unit"), actualItem.get("unit"));
					assertEquals(expectedItem.get("unitAbbr"), actualItem.get("unitAbbr"));
					found = true;
				}
			}
			assertTrue(found);
		}
	}

	@TestTemplate
	void testGetUnitList() throws Exception
	{
		ApiUnit expected = getDtoFromResource("datatypeunit_engineering_unit_insert_data.json",
				ApiUnit.class);

		ExtractableResponse<Response> response = given()
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
			.extract()
		;

		JsonPath actual = response.body().jsonPath();
		List<Map<String, Object>> actualList = actual.getList("");
		boolean found = false;
		for (Map<String, Object> item : actualList)
		{
			if (item.get("abbr").equals(expected.getAbbr()))
			{
				assertEquals(expected.getAbbr(), item.get("abbr"));
				assertEquals(expected.getName(), item.get("name"));
				assertEquals(expected.getFamily(), item.get("family"));
				assertEquals(expected.getMeasures(), item.get("measures"));
				found = true;
			}
		}
		assertTrue(found);
	}

	@TestTemplate
	void testPostAndDeleteEngineeringUnit() throws Exception
	{
		ApiUnit engineeringUnit = getDtoFromResource("datatypeunit_eu_post_delete_data.json",
				ApiUnit.class);
		String fromAbbr = engineeringUnit.getAbbr();
		ObjectMapper mapper = new ObjectMapper();
		String euJson = mapper.writeValueAsString(engineeringUnit);

		// Store the EU
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(euJson)
			.queryParam("fromabbr", fromAbbr)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("eu")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
		;

		JsonPath expected = getJsonPathFromResource("datatypeunit_eu_post_delete_data.json");

		// Retrieve the EU and assert it matches expected JSON
		ExtractableResponse<Response> response = given()
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
			.extract()
		;

		JsonPath actual = response.body().jsonPath();
		List<Map<String, Object>> actualList = actual.getList("");
		boolean found = false;
		for (Map<String, Object> item : actualList)
		{
			if (item.get("abbr").equals(fromAbbr))
			{
				assertEquals(expected.getString("abbr"), item.get("abbr"));
				assertEquals(expected.getString("name"), item.get("name"));
				assertEquals(expected.getString("family"), item.get("family"));
				assertEquals(expected.getString("measures"), item.get("measures"));
				found = true;
			}
		}
		assertTrue(found);

		// Delete the EU
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("abbr", fromAbbr)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("eu")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// Retrieve the EU and assert it is not found
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("eulist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NOT_FOUND))
		;
	}

	@TestTemplate
	void testGetUnitConverterList() throws Exception
	{
		ApiUnitConverter expected = getDtoFromResource("datatypeunit_insert_euconv_data.json",
				ApiUnitConverter.class);

		ExtractableResponse<Response> response = given()
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
			.extract()
		;

		JsonPath actual = response.body().jsonPath();
		List<Map<String, Object>> expectedMap = actual.getList("");
		boolean found = false;
		for (Map<String, Object> item : expectedMap)
		{
			if (item.get("fromAbbr").equals(expected.getFromAbbr()) && item.get("toAbbr").equals(expected.getToAbbr()))
			{
				assertEquals(expected.getFromAbbr(), item.get("fromAbbr"));
				assertEquals(expected.getToAbbr(), item.get("toAbbr"));
				assertEquals(expected.getAlgorithm(), item.get("algorithm"));
				assertEquals(expected.getA(), item.get("a") == null ? null : Double.parseDouble(item.get("a").toString()));
				assertEquals(expected.getB(), item.get("b") == null ? null : Double.parseDouble(item.get("b").toString()));
				assertEquals(expected.getC(), item.get("c") == null ? null : Double.parseDouble(item.get("c").toString()));
				assertEquals(expected.getD(), item.get("d") == null ? null : Double.parseDouble(item.get("d").toString()));
				assertEquals(expected.getE(), item.get("e") == null ? null : Double.parseDouble(item.get("e").toString()));
				assertEquals(expected.getF(), item.get("f") == null ? null : Double.parseDouble(item.get("f").toString()));
				found = true;
			}
		}
		assertTrue(found);
	}

	@TestTemplate
	void testPostAndDeleteUnitConverter() throws Exception
	{
		ApiUnit engineeringUnit = getDtoFromResource("datatypeunit_eu_post_delete_data.json",
				ApiUnit.class);
		ApiUnit engineeringToUnit = getDtoFromResource("datatypeunit_euconv_post_delete_new_unit_data.json",
				ApiUnit.class);
		String fromAbbr = engineeringUnit.getAbbr();
		String newFromAbbr = engineeringToUnit.getAbbr();
		ObjectMapper mapper = new ObjectMapper();
		String unitJson = mapper.writeValueAsString(engineeringUnit);
		String newUnitJson = mapper.writeValueAsString(engineeringToUnit);
		String converterJson = getJsonFromResource("datatypeunit_euconv_post_delete_data.json");

		// Store the EU
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(unitJson)
			.queryParam("fromabbr", fromAbbr)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("eu")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
		;

		// Store the EU
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(newUnitJson)
			.queryParam("fromabbr", newFromAbbr)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("eu")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
		;

		// Store the EU Converter
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(converterJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("euconv")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		Long convId = response.body().jsonPath().getLong("ucId");

		ApiUnitConverter expected = getDtoFromResource("datatypeunit_euconv_post_delete_data.json",
			ApiUnitConverter.class);

		// Retrieve the EU Converter and assert it matches expected JSON
		response = given()
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
			.extract()
		;

		JsonPath actual = response.body().jsonPath();
		List<Map<String, Object>> items = actual.getList("");
		boolean found = false;
		for (Map<String, Object> item : items)
		{
			if (item.get("fromAbbr").equals(expected.getFromAbbr()) && item.get("toAbbr").equals(expected.getToAbbr()))
			{
				assertEquals(expected.getFromAbbr(), item.get("fromAbbr"));
				assertEquals(expected.getToAbbr(), item.get("toAbbr"));
				found = true;
			}
		}
		assertTrue(found);

		// Delete the EU Converter
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("euconvid", convId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("euconv")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// Delete the EU
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("abbr", fromAbbr)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("eu")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// Delete the EU
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("abbr", newFromAbbr)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("eu")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// Retrieve the EU Converter and assert it is not found
		response = given()
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
			.extract()
		;

		actual = response.body().jsonPath();
		items = actual.getList("");
		found = false;
		for (Map<String, Object> item : items)
		{
			if (item.get("fromAbbr").equals(expected.getFromAbbr()) && item.get("toAbbr").equals(expected.getToAbbr()))
			{
				assertEquals(expected.getFromAbbr(), item.get("fromAbbr"));
				assertEquals(expected.getToAbbr(), item.get("toAbbr"));
				found = true;
			}
		}
		assertFalse(found);
	}
}
