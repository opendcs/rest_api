/*
 *  Copyright 2025 OpenDCS Consortium and its Contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License")
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
import org.opendcs.odcsapi.beans.ApiNetList;
import org.opendcs.odcsapi.beans.ApiPlatform;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@ExtendWith(DatabaseContextProvider.class)
final class NetlistResourcesIT extends BaseIT
{
	private static SessionFilter sessionFilter;
	private Long netlistId;
	private Long platformId;
	private Long siteId;

	@BeforeEach
	void setUp() throws Exception
	{
		setUpCreds();
		sessionFilter = new SessionFilter();
		authenticate(sessionFilter);

		ObjectMapper mapper = new ObjectMapper();

		siteId = storeSite("netlist_site_insert_data.json");

		String configJson = getJsonFromResource("config_input_data.json");
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
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

		long configId = response.body().jsonPath().getLong("configId");

		ApiPlatform platform = getDtoFromResource("netlist_platform_insert_data.json", ApiPlatform.class);
		platform.setSiteId(siteId);
		platform.setConfigId(configId);
		String platformJson = mapper.writeValueAsString(platform);

		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
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

		platformId = response.body().jsonPath().getLong("platformId");

		ApiNetList netlist = getDtoFromResource("netlist_insert_data.json", ApiNetList.class);
		netlist.getItems().get("6698948").setPlatformName(platform.getName());
		netlist.getItems().get("6698948").setTransportId("6698948");
		netlist.getItems().get("6698948").setDescription(platform.getDescription());

		String netlistJson = mapper.writeValueAsString(netlist);

		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(netlistJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("netlist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract();

		netlistId = response.body().jsonPath().getLong("netlistId");
	}

	@AfterEach
	void tearDown()
	{
		// Delete the netlist
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("netlistid", netlistId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("netlist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// Delete the platform
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("platformid", platformId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("platform")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
			.extract()
		;

		tearDownSite(siteId);

		logout(sessionFilter);
	}

	@TestTemplate
	void testGetNetlistRefs()
	{
		JsonPath expected = getJsonPathFromResource("netlist_insert_data.json");

		assertNotNull(expected);

		Map<String, Object> expectedMap = expected.getMap("");

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("netlistrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.jsonPath();
		List<Map<String, Object>> actualList = actual.getList("");

		assertNotNull(actual);
		assertNotNull(actualList);

		boolean found = false;
		for (Map<String, Object> actualMap : actualList)
		{
			if (actualMap.get("name").equals(expectedMap.get("name")))
			{
				assertEquals(expectedMap.get("name"), actualMap.get("name"));
				assertEquals(expectedMap.get("transportMediumType"), actualMap.get("transportMediumType"));
				assertEquals(expectedMap.get("siteNameTypePref"), actualMap.get("siteNameTypePref"));
				assertEquals(expectedMap.get("items[6698948].transportId"), actualMap.get("items[6698948].transportId"));
				assertEquals(expectedMap.get("items[6698948].description"), actualMap.get("items[6698948].description"));
				assertEquals(expectedMap.get("items[6698948].platformName"), actualMap.get("items[6698948].platformName"));
				found = true;
			}
		}
		assertTrue(found);

		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
			.queryParam("tmtype", "goes")
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("netlistrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		actual = response.jsonPath();
		actualList = actual.getList("");

		assertNotNull(actual);

		found = false;
		for (Map<String, Object> actualMap : actualList)
		{
			if (actualMap.get("name").equals(expectedMap.get("name")))
			{
				assertEquals(expectedMap.get("name"), actualMap.get("name"));
				assertEquals(expectedMap.get("transportMediumType"), actualMap.get("transportMediumType"));
				assertEquals(expectedMap.get("siteNameTypePref"), actualMap.get("siteNameTypePref"));
				assertEquals(expectedMap.get("items[6698948].transportId"), actualMap.get("items[6698948].transportId"));
				assertEquals(expectedMap.get("items[6698948].description"), actualMap.get("items[6698948].description"));
				assertEquals(expectedMap.get("items[6698948].platformName"), actualMap.get("items[6698948].platformName"));
				found = true;
			}
		}

		assertTrue(found);

		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
			.queryParam("tmtype", "goes-random")
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("netlistrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		actual = response.jsonPath();
		actualList = actual.getList("");

		assertNotNull(actual);

		found = false;
		for (Map<String, Object> actualMap : actualList)
		{
			if (actualMap.get("name").equals(expectedMap.get("name")))
			{
				assertEquals(expectedMap.get("name"), actualMap.get("name"));
				assertEquals(expectedMap.get("transportMediumType"), actualMap.get("transportMediumType"));
				assertEquals(expectedMap.get("siteNameTypePref"), actualMap.get("siteNameTypePref"));
				assertEquals(expectedMap.get("items[6698948].transportId"), actualMap.get("items[6698948].transportId"));
				assertEquals(expectedMap.get("items[6698948].description"), actualMap.get("items[6698948].description"));
				assertEquals(expectedMap.get("items[6698948].platformName"), actualMap.get("items[6698948].platformName"));
				found = true;
			}
		}

		assertFalse(found);
	}

	@TestTemplate
	void testGetNetlist()
	{
		JsonPath expected = getJsonPathFromResource("netlist_get_expected.json");

		assertNotNull(expected);

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("netlistid", netlistId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("netlist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.jsonPath();

		assertNotNull(actual);
		assertEquals(expected.getString("name"), actual.getString("name"));
		assertEquals(expected.getString("transportMediumType"), actual.getString("transportMediumType"));
		assertEquals(expected.getString("siteNameTypePref"), actual.getString("siteNameTypePref"));
		assertEquals(expected.getString("items[6698948].transportId"),
				actual.getString("items[6698948].transportId"));
		assertEquals(expected.getString("items[6698948].description"),
				actual.getString("items[6698948].description"));
		assertEquals(expected.getString("items[6698948].platformName"),
				actual.getString("items[6698948].platformName"));
	}

	@TestTemplate
	void testPostAndDeleteNetlist() throws Exception
	{
		String netlistJson = getJsonFromResource("netlist_post_delete_insert_data.json");

		// Insert a new netlist
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(netlistJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("netlist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		Long newNetlistId = response.body().jsonPath().getLong("netlistId");

		JsonPath expected = new JsonPath(netlistJson);

		// Get the new netlist and assert it is as expected
		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
			.queryParam("netlistid", newNetlistId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("netlist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.jsonPath();

		assertNotNull(actual);

		assertEquals(expected.getString("name"), actual.getString("name"));
		assertEquals(expected.getString("transportMediumType"), actual.getString("transportMediumType"));
		assertEquals(expected.getString("siteNameTypePref"), actual.getString("siteNameTypePref"));
		assertEquals(expected.getString("items[6698948].transportId"),
				actual.getString("items[6698948].transportId"));
		assertEquals(expected.getString("items[6698948].description"),
				actual.getString("items[6698948].description"));
		assertEquals(expected.getString("items[6698948].platformName"),
				actual.getString("items[6698948].platformName"));

		// Delete the new netlist
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("netlistid", newNetlistId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("netlist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// Get the new netlist and assert it is not found
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
			.queryParam("netlistid", newNetlistId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("netlist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NOT_FOUND))
		;
	}

	@TestTemplate
	void testCnvtANL() throws Exception
	{
		String inputTxt = getJsonFromResource("netlist_cnvtanl_input_data.txt");

		JsonPath expected = getJsonPathFromResource("netlist_cnvtanl_expected.json");

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.TEXT_PLAIN)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
			.body(inputTxt)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("cnvtnl")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		Map<String, Object> actual = response.jsonPath().getMap("items.6698948");
		assertNotNull(actual);
		assertEquals(expected.get("transportId"), actual.get("transportId"));
		assertEquals(expected.get("platformName"), actual.get("platformName"));
		assertEquals(expected.get("description"), actual.get("description"));
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
}
