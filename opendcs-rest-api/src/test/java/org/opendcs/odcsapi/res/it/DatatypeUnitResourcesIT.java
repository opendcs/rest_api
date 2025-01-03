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

@Tag("integration")
@ExtendWith(DatabaseContextProvider.class)
final class DatatypeUnitResourcesIT extends BaseIT
{
	private static SessionFilter sessionFilter;
	private static Long converterId;
	private static String euAbbr;

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


		String unitConverterJson = getJsonFromResource("datatypeunit_insert_euconv_data.json");

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
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		// Store the EU Converter
		ExtractableResponse<Response> response = given()
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
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		converterId = Long.parseLong(response.body().jsonPath().getList("ucId").get(0).toString());
	}

	@AfterEach
	public void tearDown()
	{
		// Delete the EU Converter
		// TODO: Determine the source of the deletion error, appears to be at the SQL query level
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
			.statusCode(is(HttpServletResponse.SC_OK))
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
			.statusCode(is(HttpServletResponse.SC_OK))
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

		for (int i = 0; i < expectedList.size(); i++)
		{
			Map<String, Object> expectedItem = expectedList.get(i);
			Map<String, Object> actualItem = actualList.get(i);
			assertEquals(expectedItem.get("code"), actualItem.get("code"));
			assertEquals(expectedItem.get("name"), actualItem.get("name"));
			assertEquals(expectedItem.get("unit"), actualItem.get("unit"));
			assertEquals(expectedItem.get("unitAbbr"), actualItem.get("unitAbbr"));
		}
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
			.statusCode(is(HttpServletResponse.SC_OK))
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
			.statusCode(is(HttpServletResponse.SC_OK))
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
			.statusCode(is(HttpServletResponse.SC_OK))
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
			.statusCode(is(HttpServletResponse.SC_OK))
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
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		Long convId = Long.parseLong(response.body().jsonPath().getList("ucId").get(0).toString());

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
			.statusCode(is(HttpServletResponse.SC_OK))
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
			.statusCode(is(HttpServletResponse.SC_OK))
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
			.statusCode(is(HttpServletResponse.SC_OK))
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
