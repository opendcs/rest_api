package org.opendcs.odcsapi.res.it;

import java.util.Base64;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.session.SessionFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendcs.odcsapi.beans.ApiRefList;
import org.opendcs.odcsapi.beans.ApiRefListItem;
import org.opendcs.odcsapi.beans.ApiSeason;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.sec.basicauth.Credentials;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@Tag("integration")
@ExtendWith(DatabaseContextProvider.class)
final class ReflistResourcesIT
{
	private static String credentialsJson = null;
	private static final String AUTH_HEADER_PREFIX = "Basic ";
	private static SessionFilter sessionFilter;
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final long REF_LIST_ID = 668720L;

	@BeforeAll
	static void setUp()
	{
		DbInterface.decodesProperties.setProperty("opendcs.rest.api.authorization.type", "basic");


		Credentials credentials = new Credentials();
		credentials.setUsername("dcs_admin");
		credentials.setPassword("dcs_admin_password");

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

	@BeforeEach
	void setup() throws Exception
	{
		ApiRefList refList = new ApiRefList();
		refList.setReflistId(REF_LIST_ID);
		refList.setDescription("test");
		refList.setDefaultValue("test");
		refList.setEnumName("test");
		HashMap<String, ApiRefListItem> items = new HashMap<>();
		ApiRefListItem item = new ApiRefListItem();
		item.setSortNumber(1);
		item.setValue("test");
		item.setDescription("test");
		items.put("test", item);
		refList.setItems(items);

		String refListJson = OBJECT_MAPPER.writeValueAsString(refList);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.body(refListJson)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("reflist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@AfterEach
	void tearDown()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.filter(sessionFilter)
			.queryParam("reflistid", REF_LIST_ID)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("reflist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testGetRefList()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.queryParam("name", "test") // TODO: Replace with actual ref list name
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("reflists")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testPostAndDeleteRefList() throws Exception
	{
		ApiRefList refList = new ApiRefList();
		refList.setReflistId(55682L);
		refList.setDescription("test1");
		refList.setDefaultValue("test1");
		refList.setEnumName("test1");
		HashMap<String, ApiRefListItem> items = new HashMap<>();
		ApiRefListItem item = new ApiRefListItem();
		item.setSortNumber(1);
		item.setValue("test1");
		item.setDescription("test1");
		items.put("test1", item);
		refList.setItems(items);

		String refListJson = OBJECT_MAPPER.writeValueAsString(refList);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.filter(sessionFilter)
			.body(refListJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("reflist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.filter(sessionFilter)
			.queryParam("reflistid", refList.getReflistId())
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("reflist")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testGetSeasons()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("seasons")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testPostAndDeleteSeason() throws Exception
	{
		ApiSeason season = new ApiSeason();
		season.setName("test2");
		season.setAbbr("test2");
		season.setStart("test2");
		season.setEnd("test2");
		season.setSortNumber(3);
		season.setTz("UTC");

		String seasonJson = OBJECT_MAPPER.writeValueAsString(season);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.filter(sessionFilter)
			.queryParam("fromabbr", season.getFromabbr())
			.body(seasonJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("season")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", AUTH_HEADER_PREFIX + credentialsJson)
			.filter(sessionFilter)
			.queryParam("abbr", season.getAbbr())
			.body(seasonJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("season")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}
}
