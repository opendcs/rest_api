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
import org.opendcs.odcsapi.beans.ApiAlgorithm;
import org.opendcs.odcsapi.beans.ApiCompParm;
import org.opendcs.odcsapi.beans.ApiComputation;
import org.opendcs.odcsapi.beans.ApiLoadingApp;
import org.opendcs.odcsapi.beans.ApiSite;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Tag("integration")
@ExtendWith(DatabaseContextProvider.class)
final class ComputationResourcesIT extends BaseIT
{
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static SessionFilter sessionFilter;
	private Long compId;
	private Long siteId;
	private Long appId;
	private Long algId;

	@BeforeEach
	void init() throws Exception
	{
		setUpCreds();
		sessionFilter = new SessionFilter();
		authenticate(sessionFilter);

		ApiComputation comp = getDtoFromResource("computation_insert_data.json", ApiComputation.class);

		ApiSite site = getDtoFromResource("computation_site_data.json", ApiSite.class);

		ApiLoadingApp app = getDtoFromResource("computation_app_data.json", ApiLoadingApp.class);
		String appJson = MAPPER.writeValueAsString(app);

		// Insert the app
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.body(appJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("app")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		appId = response.body().jsonPath().getLong("appId");

		// Insert Algorithm
		ApiAlgorithm alg = getDtoFromResource("computation_algorithm_data.json", ApiAlgorithm.class);

		String algJson = MAPPER.writeValueAsString(alg);

		// Insert the algorithm
		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.body(algJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("algorithm")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		algId = response.body().jsonPath().getLong("algorithmId");

		String siteJson = MAPPER.writeValueAsString(site);

		// Insert the site
		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.body(siteJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("site")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		siteId = response.body().jsonPath().getLong("siteId");

		comp.getParmList().get(0).setSiteName(site.getPublicName());
		comp.getParmList().get(0).setSiteId(siteId);
		comp.setApplicationName(app.getAppName());
		comp.setAppId(appId);
		comp.setAlgorithmName(alg.getName());
		comp.setAlgorithmId(algId);

		String compJson = MAPPER.writeValueAsString(comp);

		// Insert the computation
		 response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.body(compJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("computation")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		compId = response.body().jsonPath().getLong("computationId");
	}

	@AfterEach
	void cleanup()
	{
		// Delete the computation
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("computationid", compId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("computation")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// Delete the app
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
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// Delete the algorithm
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("algorithmid", algId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("algorithm")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// Delete the site
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("siteid", siteId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("site")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		logout(sessionFilter);
	}

	@TestTemplate
	void testGetComputationRefs()
	{
		JsonPath expectedJson = getJsonPathFromResource("computation_refs_expected.json");

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("computationrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;
		List<Map<String, Object>> actualList = response.body().jsonPath().getList("");
		assertFalse(actualList.isEmpty());
		Map<String, Object> actualItem = actualList.get(0);
		assertEquals(expectedJson.getString("name"), actualItem.get("name"));
		assertEquals(expectedJson.getString("description"), actualItem.get("description"));
		assertEquals(expectedJson.getBoolean("enabled"), actualItem.get("enabled"));
		assertEquals(expectedJson.getString("groupName"), actualItem.get("groupName"));
		assertEquals(expectedJson.getString("algorithmName"), actualItem.get("algorithmName"));
		assertEquals(expectedJson.getString("processName"), actualItem.get("processName"));
	}

	@TestTemplate
	void testGetComputationRefsWithFilters() throws Exception
	{
		JsonPath expected = getJsonPathFromResource("computation_refs_expected.json");

		ApiComputation comp = getDtoFromResource("computation_insert_data.json", ApiComputation.class);

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("site", siteId)
			.queryParam("datatype", comp.getParmList().get(0).getDataTypeId())
			.queryParam("group", comp.getGroupId())
			.queryParam("algorithm", algId)
			.queryParam("process", appId)
			.queryParam("enabled", comp.isEnabled())
			.queryParam("interval", comp.getParmList().get(0).getInterval())
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("computationrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		List<Map<String, Object>> actualList = response.body().jsonPath().getList("");
		assertFalse(actualList.isEmpty());
		Map<String, Object> actualItem = actualList.get(0);
		assertEquals(expected.getString("name"), actualItem.get("name"));
		assertEquals(expected.getString("description"), actualItem.get("description"));
		assertEquals(expected.getBoolean("enabled"), actualItem.get("enabled"));
		assertEquals(expected.getString("groupName"), actualItem.get("groupName"));
		assertEquals(expected.getString("algorithmName"), actualItem.get("algorithmName"));
		assertEquals(expected.getString("processName"), actualItem.get("processName"));
	}

	@TestTemplate
	void testGetComputationRefsWithNoMatchingFilters() throws Exception
	{
		ApiComputation comp = getDtoFromResource("computation_insert_data.json", ApiComputation.class);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("site", comp.getParmList().get(0).getSiteId())
			.queryParam("datatype", comp.getParmList().get(0).getDataTypeId())
			.queryParam("group", "test group")
			.queryParam("algorithm", comp.getAlgorithmId())
			.queryParam("process", comp.getAppId())
			.queryParam("enabled", false)
			.queryParam("interval", comp.getParmList().get(0).getInterval())
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("computationrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NOT_FOUND))
		;

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("site", comp.getParmList().get(0).getSiteId())
			.queryParam("datatype", comp.getParmList().get(0).getDataTypeId())
			.queryParam("group", comp.getGroupId())
			.queryParam("algorithm", comp.getAlgorithmId())
			.queryParam("process", comp.getAppId())
			.queryParam("enabled", comp.isEnabled())
			.queryParam("interval", "bi-annual")
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("computationrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.body("size()", is(0))
		;

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("site", comp.getParmList().get(0).getSiteId())
			.queryParam("datatype", comp.getParmList().get(0).getDataTypeId())
			.queryParam("group", comp.getGroupId())
			.queryParam("algorithm", "25")
			.queryParam("process", comp.getAppId())
			.queryParam("enabled", comp.isEnabled())
			.queryParam("interval", comp.getParmList().get(0).getInterval())
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("computationrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testGetComputation()
	{
		JsonPath expected = getJsonPathFromResource("computation_insert_data.json");

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("computationid", compId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("computation")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();
		assertEquals(expected.getString("name"), actual.getString("name"));
		assertEquals(expected.getString("description"), actual.getString("description"));
		assertEquals(expected.getString("enabled"), actual.getString("enabled"));
		assertEquals(expected.getString("groupName"), actual.getString("groupName"));
		assertEquals(expected.getString("applicationName"), actual.getString("applicationName"));
		assertEquals("Test Algorithm", actual.getString("algorithmName"));
		assertEquals(algId, actual.getLong("algorithmId"));
		assertEquals(appId, actual.getLong("appId"));
		assertEquals(siteId, actual.getLong("parmList[0].siteId"));
		assertEquals(expected.getString("parmList[0].siteName"), actual.getString("parmList[0].siteName"));
		assertEquals(expected.getString("parmList[0].dataType"), actual.getString("parmList[0].dataType"));
		assertEquals(expected.getString("parmList[0].interval"), actual.getString("parmList[0].interval"));
		assertEquals(expected.getString("comment"), actual.getString("comment"));
	}

	@TestTemplate
	void testPostAndDeleteComputation() throws Exception
	{
		ApiComputation comp = getDtoFromResource("computation_post_delete_insert_data.json",
				ApiComputation.class);

		String compJson = MAPPER.writeValueAsString(comp);

		// Store the new computation
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.body(compJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("computation")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_CREATED))
			.extract()
		;

		Long newCompId = response.body().jsonPath().getLong("computationId");

		// Get the new computation and assert it matches the expected data
		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("computationid", newCompId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("computation")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		JsonPath actual = response.body().jsonPath();
		assertEquals(comp.getName(), actual.getString("name"));
		assertEquals(comp.isEnabled(), actual.getBoolean("enabled"));
		assertEquals(comp.getGroupName(), actual.getString("groupName"));
		assertEquals(comp.getApplicationName(), actual.getString("applicationName"));
		assertEquals(comp.getAlgorithmName(), actual.getString("algorithmName"));
		assertEquals(comp.getComment(), actual.getString("comment"));
		List<Map<String, Object>> actualParmList = actual.getList("parmList");
		int i = 0;
		for (ApiCompParm parm : comp.getParmList())
		{
			Map<String, Object> actualItem = actualParmList.get(i);
			assertEquals(parm.getSiteName(), actualItem.get("siteName"));
			assertEquals(parm.getDataType(), actualItem.get("dataType"));
			assertEquals(parm.getInterval(), actualItem.get("interval"));
			i++;
		}

		// Delete the new computation
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("computationid", newCompId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("computation")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NO_CONTENT))
		;

		// Get the new computation and assert it is not found
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.filter(sessionFilter)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("computationid", newCompId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("computation")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_NOT_FOUND))
		;
	}
}
