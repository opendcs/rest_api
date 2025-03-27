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

package org.opendcs.odcsapi.fixtures;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import javax.servlet.http.HttpServletResponse;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.PreconditionViolationException;
import org.opendcs.fixtures.spi.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;

public class DatabaseSetupExtension implements BeforeEachCallback
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSetupExtension.class);
	private static DbType currentDbType;
	private static Configuration currentConfig;
	private static TomcatServer currentTomcat;
	private final DbType dbType;
	private TomcatServer tomcatServer;
	private Configuration configuration;

	public DatabaseSetupExtension(DbType dbType)
	{
		this.dbType = dbType;
	}

	public static DbType getCurrentDbType()
	{
		return currentDbType;
	}

	public static Configuration getCurrentConfig()
	{
		return currentConfig;
	}

	public static TomcatServer getCurrentTomcat()
	{
		return currentTomcat;
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception
	{
		if(tomcatServer == null)
		{
			tomcatServer = startTomcat();
		}
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = tomcatServer.getPort();
		RestAssured.basePath = "odcsapi";
		healthCheck();
		currentDbType = dbType;
		currentTomcat = tomcatServer;
		currentConfig = configuration;
	}

	private TomcatServer startTomcat() throws Exception
	{
		configuration = TomcatServer.setupDb(dbType.toString());
		setupClientUser();
		String restWarFile = Objects.requireNonNull(System.getProperty("opendcs.restapi.warfile"), "opendcs.restapi.warfile is not set");
		String guiWarFile = Objects.requireNonNull(System.getProperty("opendcs.gui.warfile"), "opendcs.gui.warfile is not set");
		TomcatServer tomcat = new TomcatServer("build/tomcat", 0, restWarFile, guiWarFile);
		tomcat.start();
		return tomcat;
	}

	private static void healthCheck() throws InterruptedException
	{
		int attempts = 0;
		int maxAttempts = 15;
		for(; attempts < maxAttempts; attempts++)
		{
			try
			{
				given()
						.when()
						.delete("/logout")
						.then()
						.assertThat()
						.statusCode(Matchers.is(HttpServletResponse.SC_NO_CONTENT));
				LOGGER.atDebug().log("Server is up!");
				break;
			}
			catch(Throwable e)
			{
				LOGGER.atDebug().log("Waiting for the server to start...");
				Thread.sleep(100);//NOSONAR
			}
		}
		if(attempts == maxAttempts)
		{
			throw new PreconditionViolationException("Server didn't start in time...");
		}
	}

	private void setupClientUser()
	{
		if(dbType == DbType.CWMS)
		{
			String userPermissions = "begin execute immediate 'grant web_user to " + System.getProperty("DB_USERNAME") + "'; end;";
			String dbOffice = System.getProperty("DB_OFFICE");
			String setWebUserPermissions = "begin\n" +
					"   cwms_sec.add_user_to_group(?, 'CWMS User Admins',?) ;\n" +
					"   commit;\n" +
					"end;";
			try(Connection connection = DriverManager.getConnection(System.getProperty("DB_URL"), "CWMS_20",
					System.getProperty("DB_PASSWORD"));
				PreparedStatement stmt1 = connection.prepareStatement(userPermissions);
				PreparedStatement stmt2 = connection.prepareStatement(setWebUserPermissions))
			{
				stmt1.executeQuery();
				stmt2.setString(1, System.getProperty("DB_USERNAME"));
				stmt2.setString(2, dbOffice);
				stmt2.executeQuery();
			}
			catch(SQLException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}