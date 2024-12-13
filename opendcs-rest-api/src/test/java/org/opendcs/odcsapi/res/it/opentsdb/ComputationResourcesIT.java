package org.opendcs.odcsapi.res.it.opentsdb;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
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
import org.opendcs.odcsapi.beans.ApiCompParm;
import org.opendcs.odcsapi.beans.ApiComputation;
import org.opendcs.odcsapi.fixtures.DatabaseContextProvider;
import org.opendcs.odcsapi.fixtures.ResourcesTestBase;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;

@Tag("integration")
@ExtendWith(DatabaseContextProvider.class)
final class ComputationResourcesIT extends ResourcesTestBase
{
	private static SessionFilter sessionFilter;
	private static Long compId;

	@BeforeAll
	public static void setUp()
	{
		sessionFilter = new SessionFilter();
	}

	@BeforeEach
	public void setUpEach() throws Exception
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

		//TODO: Store site and group in DB and use them here

		ApiComputation comp = new ApiComputation();
		comp.setName("TestComputation");
		comp.setApplicationName("TestApplication");
		comp.setGroupName("TestGroup");
		comp.setEnabled(true);
		comp.setComputationId(889651L);
		comp.setLastModified(new Date());
		comp.setEffectiveStartDate(new Date());
		Properties properties = new Properties();
		properties.put("key1", "value1");
//		comp.setProps(properties);
		comp.setAlgorithmName("TestAlgorithm");
		comp.setEffectiveStartType("TestStartType");
		comp.setEffectiveStartInterval("1month");
		comp.setEffectiveEndType("TestEndType");
		comp.setEffectiveEndInterval("1month");
		comp.setComment("TestComment");

		//TODO: Store site and use it here
		ArrayList<ApiCompParm> parmList = new ArrayList<>();
		ApiCompParm parm = new ApiCompParm();
		parm.setParamType("TestParamType");
		parm.setVersion("1.2.3");
		parm.setTableSelector("TestTableSelector");
		parm.setInterval("1week");
		parm.setDuration("1month");
		parm.setAlgoParmType("TestAlgoParmType");
		parm.setUnitsAbbr("m");
		parm.setSiteId(12345L);
		parm.setSiteName("TestSiteName");
		parm.setAlgoRoleName("TestAlgoRoleName");
		parmList.add(parm);
//		comp.setParmList(parmList);


		ObjectMapper mapper = new ObjectMapper();
		String compJson = mapper.writeValueAsString(comp);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(compJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("computation")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		compId = getCompId(comp.getName());
	}

	@AfterEach
	void cleanup()
	{
//		given()
//			.log().ifValidationFails(LogDetail.ALL, true)
//			.accept(MediaType.APPLICATION_JSON)
//			.contentType(MediaType.APPLICATION_JSON)
//			.header("Authorization", authHeader)
//			.queryParam("compId", compId)
//			.filter(sessionFilter)
//		.when()
//			.redirects().follow(true)
//			.redirects().max(3)
//			.delete("computation")
//		.then()
//			.log().ifValidationFails(LogDetail.ALL, true)
//		.assertThat()
//			.statusCode(is(HttpServletResponse.SC_OK))
//		;
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
	void testGetComputationRefs()
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
			.get("computationrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
				.body("size()", greaterThan(0))
		;
	}

	@TestTemplate
	void testGetComputation()
	{
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("computationid", compId)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("computation")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.body("size()", greaterThan(0))
		;
	}

	@TestTemplate
	void testPostAndDeleteComputation() throws Exception
	{
		//TODO: Store site and computation parameters in DB and use them here
		ApiComputation comp = new ApiComputation();
		comp.setName("TestComputation1");
		comp.setApplicationName("TestApplication1");
		comp.setAlgorithmId(11241L);
		comp.setGroupName("TestGroup1");
		comp.setGroupId(5574151L);
		comp.setComputationId(889651L);
		comp.setEnabled(true);
		comp.setLastModified(new Date());
		comp.setEffectiveStartDate(new Date());
		Properties properties = new Properties();
		properties.put("key2", "value2");
//		comp.setProps(properties);
		comp.setAlgorithmName("TestAlgorithm1");
		comp.setEffectiveStartType("TestStartType1");
		comp.setEffectiveStartInterval("1year");
		comp.setEffectiveEndType("TestEndType1");
		comp.setEffectiveEndInterval("1year");
		comp.setComment("TestComment1");
		ArrayList<ApiCompParm> parmList = new ArrayList<>();
		ApiCompParm parm = new ApiCompParm();
		parm.setParamType("TestParamType1");
		parm.setVersion("1.2.34");
		parm.setTableSelector("TestTableSelector1");
		parm.setInterval("1month");
		parm.setDuration("1year");
		parm.setAlgoParmType("TestAlgoParmType1");
		parm.setUnitsAbbr("ft");
		parm.setSiteName("TestSiteName1");
		parm.setSiteId(123451L);
		parm.setAlgoRoleName("TestAlgoRoleName1");
		parmList.add(parm);
//		comp.setParmList(parmList);


		ObjectMapper mapper = new ObjectMapper();
		String compJson = mapper.writeValueAsString(comp);

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
			.body(compJson)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.post("computation")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		Long newCompId = getCompId(comp.getName());

		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.queryParam("computationid", newCompId)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.delete("computation")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;
	}

	private Long getCompId(String compName)
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
			.get("computationrefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		for (int i = 0; i < response.body().jsonPath().getList("").size(); i++)
		{

			if ((response.body().jsonPath().getString("[" + i + "].name")).equalsIgnoreCase(compName))
			{
				return response.body().jsonPath().getLong("[" + i + "].compId");
			}
		}
		return null;
	}

}
