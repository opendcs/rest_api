package org.opendcs.odcsapi.res.it;


import java.util.ArrayList;
import java.util.Properties;
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
import org.opendcs.odcsapi.beans.ApiDataSource;
import org.opendcs.odcsapi.beans.ApiDataSourceGroupMember;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;

@Tag("integration-opentsdb-only")
@ExtendWith(DatabaseContextProvider.class)
final class DataSourceResourcesIT extends BaseIT
{
	private static SessionFilter sessionFilter;
	private static Long sourceId;
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@BeforeEach
	void setUp() throws Exception
	{
		setUpCreds();
		sessionFilter = new SessionFilter();

		authenticate(sessionFilter);

		ApiDataSource dsGroupMem = new ApiDataSource();
		dsGroupMem.setName("Sensor Data Value");
		dsGroupMem.setUsedBy(2);
		dsGroupMem.setType("Sensor");
		Properties props = new Properties();
		props.setProperty("country", "USA");
		dsGroupMem.setProps(props);

		String dsJson = OBJECT_MAPPER.writeValueAsString(dsGroupMem);

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(dsJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("datasource")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		Long memberSourceId = response.body().jsonPath().getLong("dataSourceId");

		ApiDataSource ds = new ApiDataSource();
		ds.setName("Sensor Data");
		ds.setUsedBy(12);
		ds.setType("Sensor");
		Properties properties = new Properties();
		properties.setProperty("country", "USA");
		ds.setProps(properties);
		ArrayList<ApiDataSourceGroupMember> groupMembers = new ArrayList<>();
		ApiDataSourceGroupMember groupMember = new ApiDataSourceGroupMember();
		groupMember.setDataSourceName(dsGroupMem.getName());
		groupMember.setDataSourceId(memberSourceId);
		groupMembers.add(groupMember);
		ds.setGroupMembers(groupMembers);

		dsJson = OBJECT_MAPPER.writeValueAsString(ds);

		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(dsJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("datasource")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		sourceId = response.body().jsonPath().getLong("dataSourceId");
	}

	@AfterEach
	void tearDown()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("datasourceid", sourceId)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("datasource")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		logout(sessionFilter);
	}

	@TestTemplate
	void testDataSourceRefs()
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
			.get("datasourcerefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.body("size()", greaterThan(0))
		;
	}

	@TestTemplate
	void testGetDataSource()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.queryParam("datasourceid", sourceId)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("datasource")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	@TestTemplate
	void testPostAndDeleteDataSource() throws Exception
	{
		ApiDataSource dsGroupMem = new ApiDataSource();
		dsGroupMem.setName("Satellite Data Value");
		dsGroupMem.setUsedBy(2);
		dsGroupMem.setType("Satellite");
		Properties props = new Properties();
		props.setProperty("country", "USA");
		dsGroupMem.setProps(props);

		String dsJson = OBJECT_MAPPER.writeValueAsString(dsGroupMem);

		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(dsJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("datasource")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		Long memberSourceId = response.body().jsonPath().getLong("dataSourceId");

		ApiDataSource ds = new ApiDataSource();
		ds.setName("Satellite Data");
		ds.setUsedBy(12);
		ds.setType("Satellite");
		Properties properties = new Properties();
		properties.setProperty("location", "low Earth orbit");
		ds.setProps(properties);
		ArrayList<ApiDataSourceGroupMember> groupMembers = new ArrayList<>();
		ApiDataSourceGroupMember groupMember = new ApiDataSourceGroupMember();
		groupMember.setDataSourceName(dsGroupMem.getName());
		groupMember.setDataSourceId(memberSourceId);
		groupMembers.add(groupMember);
		ds.setGroupMembers(groupMembers);

		dsJson = OBJECT_MAPPER.writeValueAsString(ds);

		response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(dsJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("datasource")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		Long newSourceId = response.body().jsonPath().getLong("dataSourceId");

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("datasourceid", newSourceId)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("datasource")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}
}
