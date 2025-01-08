package org.opendcs.odcsapi.res.it;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.session.SessionFilter;
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
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integration-opentsdb-only")
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

		ApiPlatform platform = getDtoFromResource("netlist_platform_insert_data.json", ApiPlatform.class);
		platform.setSiteId(siteId);
		String platformJson = mapper.writeValueAsString(platform);

		ExtractableResponse<Response> response = given()
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
			.statusCode(is(HttpServletResponse.SC_OK))
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
			.statusCode(is(HttpServletResponse.SC_OK))
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
			.queryParam("netlistId", netlistId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("netlist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
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
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		tearDownSite(siteId);

		logout(sessionFilter);
	}

	@TestTemplate
	void testGetNetlistRefs()
	{
		given()
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
			.body(hasItem(getJsonPathFromResource("netlist_get_refs_expected.json")))
		;

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
			.queryParam("tmtype", "GOES")
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("netlistrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.body(hasItem(getJsonPathFromResource("netlist_get_refs_filtered_expected.json")))
		;

	}

	@TestTemplate
	void testGetNetlist()
	{
		given()
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
			.body(hasItem(getJsonPathFromResource("netlist_get_expected.json")))
		;
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
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		Long newNetlistId = response.body().jsonPath().getLong("netlistId");

		// Get the new netlist and assert it is as expected
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
			.statusCode(is(HttpServletResponse.SC_OK))
			.body(hasItem(getJsonPathFromResource("netlist_post_delete_expected.json")))
		;

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
			.statusCode(is(HttpServletResponse.SC_OK))
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
		String inputJson = getJsonFromResource("netlist_cnvtanl_input_data.json");

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.filter(sessionFilter)
			.header("Authorization", authHeader)
			.body(inputJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("netlist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.body(hasItem(getJsonPathFromResource("netlist_cnvtanl_expected.json")))
		;
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
