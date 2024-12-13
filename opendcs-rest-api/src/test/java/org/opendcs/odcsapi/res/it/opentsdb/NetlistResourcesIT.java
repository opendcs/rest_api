package org.opendcs.odcsapi.res.it.opentsdb;

import java.util.Date;
import java.util.HashMap;
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
import org.opendcs.odcsapi.beans.ApiNetList;
import org.opendcs.odcsapi.beans.ApiNetListItem;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;
import org.opendcs.odcsapi.fixtures.ResourcesTestBase;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;


@Tag("integration")
@ExtendWith(DatabaseContextProvider.class)
final class NetlistResourcesIT extends ResourcesTestBase
{
	private static SessionFilter sessionFilter;
	private static Long netId;

	@BeforeAll
	static void setup()
	{
		sessionFilter = new SessionFilter();
	}

	@BeforeEach
	void config() throws Exception
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

		ApiNetList netList = new ApiNetList();
		netList.setSiteNameTypePref("Test");
		netList.setName("Test name");
		netList.setLastModifyTime(new Date());
		netList.setTransportMediumType("Test");
		HashMap<String, ApiNetListItem> itemList = new HashMap<>();
		ApiNetListItem item = new ApiNetListItem();
		item.setTransportId("Test transport id");
		item.setPlatformName("Test platform name");
		item.setDescription("Test description");
		itemList.put("Test transport id", item);
		netList.setItems(itemList);

		ObjectMapper mapper = new ObjectMapper();
		String netListJson = mapper.writeValueAsString(netList);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(netListJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("netlist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		netId = getNetId(netList.getName());
	}

	@AfterEach
	void tearDown()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("netlistid", netId)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("netlist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@AfterAll
	static void logout()
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
	void testGetNetworkListRefs()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("tmtype", "Test") // TODO: Change to actual value once implemented in endpoint
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("netlistrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.body("size()", greaterThan(0))
		;
	}

	@TestTemplate
	void testNetworkList()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("netlistid", netId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("netlist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testPostAndDeleteNetworkList() throws Exception
	{
		ApiNetList netList = new ApiNetList();
		netList.setSiteNameTypePref("Test 2");
		netList.setName("Test name 2");
		netList.setTransportMediumType("Test 2");
		netList.setLastModifyTime(new Date());
		HashMap<String, ApiNetListItem> itemList = new HashMap<>();
		ApiNetListItem item = new ApiNetListItem();
		item.setTransportId("Test transport id 2");
		item.setPlatformName("Test platform name 2");
		item.setDescription("Test description 2");
		itemList.put("Test transport id 2", item);
		netList.setItems(itemList);

		ObjectMapper mapper = new ObjectMapper();
		String netListJson = mapper.writeValueAsString(netList);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(netListJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("netlist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		Long newNetId = getNetId(netList.getName());

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("netlistid", newNetId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("netlist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testNLFileParser()
	{
		String nlData = "Test data";

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.TEXT_PLAIN)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(nlData)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("cnvtnl")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}


	private Long getNetId(String netName)
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
			.get("netlistrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		for (int i = 0; i < response.body().jsonPath().getList("").size(); i++)
		{
			if ((response.body().jsonPath().getString("[" + i + "].name")).equalsIgnoreCase(netName))
			{
				return response.body().jsonPath().getLong("[" + i + "].netId");
			}
		}
		return null;
	}
}
