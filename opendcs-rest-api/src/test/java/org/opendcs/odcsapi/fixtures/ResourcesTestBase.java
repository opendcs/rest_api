package org.opendcs.odcsapi.fixtures;

import java.util.Base64;

import org.opendcs.odcsapi.sec.basicauth.Credentials;

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
}