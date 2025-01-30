package org.opendcs.odcsapi.res.it;

import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

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
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration-opentsdb-only")
@ExtendWith(DatabaseContextProvider.class)
final class ReflistResourcesIT extends BaseIT
{
	private static SessionFilter sessionFilter;
	private static Long refListId;
	private static String reflistName;
	private static String seasonId;

	@BeforeEach
	void setUp() throws Exception
	{
		setUpCreds();
		sessionFilter = new SessionFilter();
		authenticate(sessionFilter);

		String refListJson = getJsonFromResource("reflist_insert_data.json");

		// Store reflist
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.contentType(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.body(refListJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("reflist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		refListId = response.jsonPath().getLong("reflistId");

		reflistName = response.jsonPath().getString("reflistName");

		String seasonJson = getJsonFromResource("reflist_season_insert_data.json");

		// Store season
		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.contentType(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.body(seasonJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("season")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		seasonId = response.jsonPath().getString("abbr");
	}

	@AfterEach
	void tearDown()
	{
		// Delete Season
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("abbr", seasonId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("season")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// Delete RefList
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("reflistid", refListId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("reflist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		logout(sessionFilter);
	}

	@TestTemplate
	void testGetRefLists()
	{
		JsonPath expected = getJsonPathFromResource("reflist_get_refs_expected.json");
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("reflists")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();
		Map<String, Object> actualMap = actual.getMap("");
		Map<String, Object> expectedMap = expected.getMap("");

		for (Map.Entry<String, Object> item : actualMap.entrySet())
		{
			boolean found = false;
			for (Map.Entry<String, Object> expItem : expectedMap.entrySet())
			{
				Map<String, Object> expectedObj = (Map<String, Object>) expItem.getValue();
				Map<String, Object> actualObj = (Map<String, Object>) item.getValue();
				if (item.getKey().equalsIgnoreCase(expectedObj.get("enumName").toString()))
				{
					assertEquals(expectedObj.get("defaultValue"), actualObj.get("defaultValue"));
					assertEquals(expectedObj.get("description"), actualObj.get("description"));
					Map<String, Object> actualItem = (Map<String, Object>) actualObj.get("items");
					Map<String, Object> expectedItem = (Map<String, Object>) expectedObj.get("items");
					assertEquals(expectedItem.size(), actualItem.size());
					if (!actualItem.isEmpty())
					{
						for (Map.Entry<String, Object> entry : actualItem.entrySet())
						{
							Map<String, Object> actualVal = (Map<String, Object>) entry.getValue();
							Map<String, Object> expectedVal = (Map<String, Object>) expectedItem.get(entry.getKey());
							assertEquals(expectedVal.get("value"), actualVal.get("value"));
							assertEquals(expectedVal.get("description"), actualVal.get("description"));
							assertEquals(expectedVal.get("execClassName"), actualVal.get("execClassName"));
							assertEquals(expectedVal.get("editClassName"), actualVal.get("editClassName"));
							assertEquals(expectedVal.get("sortNumber"), actualVal.get("sortNumber"));
						}
					}
					found = true;
				}
			}
			assertTrue(found);
		}
	}

	@TestTemplate
	void testGetRefList()
	{
		JsonPath expected = getJsonPathFromResource("reflist_insert_data.json");

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("name", reflistName)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("reflists")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();
		Map<String, Object> actualMap = actual.getMap("");

		for (Map.Entry<String, Object> item : actualMap.entrySet())
		{
			if (item.getKey().equalsIgnoreCase(expected.getString("enumName")))
			{
				Map<String, Object> actualObj = (Map<String, Object>) item.getValue();
				Map<String, Object> expectedObj = expected.getMap("");
				assertEquals(expectedObj.get("defaultValue"), actualObj.get("defaultValue"));
				Map<String, Object> actualItem = (Map<String, Object>) actualObj.get("items");
				Map<String, Object> expectedItem = (Map<String, Object>) expectedObj.get("items");
				for(Map.Entry<String, Object> actualVal : actualItem.entrySet())
				{
					Map<String, Object> expectedItemVal = (Map<String, Object>) expectedItem.get(actualVal.getKey());
					assertEquals(expectedItemVal.get("value"), ((Map<String, Object>) actualVal.getValue()).get("value"));
					assertEquals(expectedItemVal.get("description"), ((Map<String, Object>) actualVal.getValue()).get("description"));
					assertEquals(expectedItemVal.get("execClassName"), ((Map<String, Object>) actualVal.getValue()).get("execClassName"));
					assertEquals(expectedItemVal.get("editClassName"), ((Map<String, Object>) actualVal.getValue()).get("editClassName"));
					assertEquals(expectedItemVal.get("sortNumber"), ((Map<String, Object>) actualVal.getValue()).get("sortNumber"));
				}
			}
		}
	}

	@TestTemplate
	void testPostAndDeleteRefList() throws Exception
	{
		String refListJson = getJsonFromResource("reflist_post_delete_insert_data.json");
		JsonPath expected = new JsonPath(refListJson);

		// Store RefList
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL,  true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.contentType(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.body(refListJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("reflist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		Long newRefListId = response.jsonPath().getLong("reflistId");
		String newRefListName = response.jsonPath().getString("enumName");

		// Retrieve RefList and assert that it matches stored data
		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("name", newRefListName)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("reflists")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();
		Map<String, Object> actualMap = actual.getMap("");

		boolean found = false;
		for (Map.Entry<String, Object> item : actualMap.entrySet())
		{
			if (item.getKey().equalsIgnoreCase(expected.getString("enumName")))
			{
				Map<String, Object> actualObj = (Map<String, Object>) item.getValue();
				Map<String, Object> expectedObj = expected.get("");
				assertEquals(expectedObj.get("defaultValue"), actualObj.get("defaultValue"));
				assertEquals(expectedObj.get("description"), actualObj.get("description"));
				Map<String, Object> itemsList = (Map<String, Object>) actualObj.get("items");
				Map<String, Object> expectedItemsList = (Map<String, Object>) expectedObj.get("items");
				for (Map.Entry<String, Object> actualItem : itemsList.entrySet())
				{
					Map<String, Object> actualVal = (Map<String, Object>) actualItem.getValue();
					Map<String, Object> expectedVal = (Map<String, Object>) expectedItemsList.get(actualItem.getKey());
					assertEquals(expectedVal.get("value"), actualVal.get("value"));
					assertEquals(expectedVal.get("description"), actualVal.get("description"));
					assertEquals(expectedVal.get("execClassName"), actualVal.get("execClassName"));
					assertEquals(expectedVal.get("editClassName"), actualVal.get("editClassName"));
					assertEquals(expectedVal.get("sortNumber"), actualVal.get("sortNumber"));
				}
				found = true;
			}
		}
		assertTrue(found);

		// Delete the reflist
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.contentType(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.queryParam("reflistid", newRefListId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("reflist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// Retrieve RefList and assert that it is not found
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("name", newRefListName)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("reflists")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NOT_FOUND))
		;

	}

	@TestTemplate
	void testGetSeason()
	{
		JsonPath expected = getJsonPathFromResource("reflist_season_insert_data.json");
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("abbr", seasonId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("season")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();
		Map<String, Object> actualItem = actual.get("");
		Map<String, Object> expectedItem = expected.get("");
		assertEquals(expectedItem.get("abbr"), actualItem.get("abbr"));
		assertEquals(expectedItem.get("name"), actualItem.get("name"));
		assertEquals(expectedItem.get("start"), actualItem.get("start"));
		assertEquals(expectedItem.get("end"), actualItem.get("end"));
		assertEquals(expectedItem.get("tz"), actualItem.get("tz"));
	}

	@TestTemplate
	void testPostAndDeleteSeason() throws Exception
	{
		String seasonJson = getJsonFromResource("reflist_season_insert_data.json");

		// Store Season
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.contentType(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.body(seasonJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("season")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		String newSeasonId = response.jsonPath().getString("abbr");

		// Retrieve Season and assert that it matches stored data
		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("abbr", newSeasonId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("season")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();
		JsonPath expected = new JsonPath(seasonJson);
		assertEquals(expected.getString("abbr"), actual.getString("abbr"));
		assertEquals(expected.getString("name"), actual.getString("name"));
		assertEquals(expected.getString("start"), actual.getString("start"));
		assertEquals(expected.getString("end"), actual.getString("end"));
		assertEquals(expected.getString("tz"), actual.getString("tz"));

		// Delete Season
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("abbr", newSeasonId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("season")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// Retrieve Season and assert that it is not found
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("abbr", newSeasonId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("season")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NOT_FOUND))
		;
	}

	@TestTemplate
	void testPostAndDeleteSeasonWithFromAbbr() throws Exception
	{
		String seasonJson = getJsonFromResource("reflist_season_from_abbr_insert_data.json");

		// Store Season
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.contentType(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.body(seasonJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("season")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		String newSeasonId = response.jsonPath().getString("abbr");

		// Retrieve Season and assert that it matches stored data
		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("abbr", newSeasonId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("season")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();
		JsonPath expected = new JsonPath(seasonJson);
		assertEquals(expected.getString("abbr"), actual.getString("abbr"));
		assertEquals(expected.getString("name"), actual.getString("name"));
		assertEquals(expected.getString("start"), actual.getString("start"));
		assertEquals(expected.getString("end"), actual.getString("end"));
		assertEquals(expected.getString("tz"), actual.getString("tz"));
		assertEquals(expected.getString("fromAbbr"), actual.getString("fromAbbr"));

		// Delete Season
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("abbr", newSeasonId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("season")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// Retrieve Season and assert that it is not found
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("abbr", newSeasonId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("season")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NOT_FOUND))
		;
	}
}
