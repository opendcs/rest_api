/*
 *  Copyright 2024 OpenDCS Consortium and its Contributors
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

package org.opendcs.odcsapi.sec.cwms;

import java.util.EnumSet;
import javax.servlet.http.HttpServletResponse;

import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import org.apache.catalina.session.StandardSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.sec.BaseIT;
import org.opendcs.odcsapi.sec.OpenDcsApiRoles;
import org.opendcs.odcsapi.sec.OpenDcsPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@Tag("integration")
final class ServletSsoAuthIT extends BaseIT
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ServletSsoAuthIT.class);
	private RestAssuredConfig oldConfig;
	private static final String COOKIE = "HelloWorld";

	private static void setupSession()
	{
		String username = System.getProperty("opendcs.db.username");
		StandardSession session = (StandardSession) opendcsInstance.getTestSessionManager()
				.createSession(COOKIE);
		if(session == null) {
			throw new RuntimeException("Test Session Manager is unusable.");
		}
		OpenDcsPrincipal mcup = new OpenDcsPrincipal(username, EnumSet.allOf(OpenDcsApiRoles.class));
		session.setAuthType("CLIENT-CERT");
		session.setPrincipal(mcup);
		session.activate();
		session.addSessionListener(event ->
		{
			LOGGER.atInfo().log("Got event of type: {}",event.getType());
			LOGGER.atInfo().log("Session is: {}",event.getSession().toString());
		});
		opendcsInstance.getSsoValve()
				.wrappedRegister(COOKIE, mcup, "CLIENT-CERT", null,null);
	}

	@AfterEach
	public void resetTls()
	{
		RestAssured.config = oldConfig;
	}

	@Test
	void testSsoAuthFlow()
	{
		DbInterface.decodesProperties.setProperty("opendcs.rest.api.authorization.type", "sso");
		//Initial session should be unauthorized
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept("application/json")
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("check")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_UNAUTHORIZED))
		;

		setupSession();

		//Check while passing in cookie
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept("application/json")
			.filter(sessionFilter)
			.cookie("JSESSIONIDSSO", COOKIE)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("check")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		//Session should be cached even without cookie
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept("application/json")
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("check")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_OK))
		;

		//Logout and clear session
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept("application/json")
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

		//Session should now be cleared
		given()
			.log().ifValidationFails(LogDetail.ALL, true)
			.accept("application/json")
			.filter(sessionFilter)
		.when()
			.redirects().follow(true)
			.redirects().max(3)
			.get("check")
		.then()
			.log().ifValidationFails(LogDetail.ALL, true)
		.assertThat()
			.statusCode(is(HttpServletResponse.SC_UNAUTHORIZED))
		;
	}
}