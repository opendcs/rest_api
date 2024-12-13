package org.opendcs.odcsapi.fixtures;

import java.util.Base64;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import io.restassured.filter.log.LogDetail;
import io.restassured.filter.session.SessionFilter;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.opendcs.odcsapi.sec.basicauth.Credentials;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public abstract class ResourcesTestBase
{
	protected static String authHeader = null;

	public static void setUpCreds()
	{
		String authHeaderPrefix = "Basic ";
		Credentials adminCreds = new Credentials();
		adminCreds.setPassword(System.getProperty("DB_PASSWORD"));
		adminCreds.setUsername(System.getProperty("DB_USERNAME"));
		String credentialsJson = Base64.getEncoder()
				.encodeToString(String.format("%s:%s", adminCreds.getUsername(), adminCreds.getPassword()).getBytes());
		authHeader = authHeaderPrefix + credentialsJson;
	}

	public Long getSiteId(String name)
	{
		SessionFilter sessionFilter = new SessionFilter();
		ExtractableResponse<Response> response = given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", authHeader)
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("siterefs")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
			.extract()
		;

		for (int i = 0; i < response.body().jsonPath().getList("").size(); i++)
		{

			if ((response.body().jsonPath().getString("[" + i + "].siteName")).equalsIgnoreCase(name))
			{
				return response.body().jsonPath().getLong("[" + i + "].siteId");
			}
		}
		return null;
	}
}
