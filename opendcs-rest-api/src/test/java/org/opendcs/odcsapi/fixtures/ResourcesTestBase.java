package org.opendcs.odcsapi.fixtures;

import java.util.Base64;

import org.opendcs.odcsapi.sec.basicauth.Credentials;

public class ResourcesTestBase
{
	public static String credentialsJson = null;

	public static void setUpCreds()
	{
		Credentials adminCreds = new Credentials();
		adminCreds.setPassword(System.getProperty("DB_PASSWORD"));
		adminCreds.setUsername(System.getProperty("DB_USERNAME"));
		credentialsJson = Base64.getEncoder()
				.encodeToString(String.format("%s:%s", adminCreds.getUsername(), adminCreds.getPassword()).getBytes());
	}
}
