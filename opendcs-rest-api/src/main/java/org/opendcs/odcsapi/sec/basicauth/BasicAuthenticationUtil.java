/*
 *  Copyright 2024 OpenDCS Consortium
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

package org.opendcs.odcsapi.sec.basicauth;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;

import org.opendcs.odcsapi.dao.ApiDaoBase;
import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.sec.OpenDcsPrincipal;
import org.opendcs.odcsapi.sec.OpenDcsApiRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class BasicAuthenticationUtil
{
	private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthenticationUtil.class);
	private static final String MODULE = "TokenManager";

	private BasicAuthenticationUtil()
	{
		throw new AssertionError("Utility class cannot be instantiated.");
	}

	static OpenDcsPrincipal makeUserPrincipal(Credentials postBody, String authorizationHeader)
			throws WebAppException
	{
		//If creds not provided in POST body, attempt to get from http header.
		Credentials credentials = getCredentials(postBody, authorizationHeader);
		Set<OpenDcsApiRoles> roles = verifyAuthentication(credentials);
		return new OpenDcsPrincipal(credentials.getUsername(), Collections.unmodifiableSet(roles));
	}

	private static Credentials getCredentials(Credentials postBody, String authorizationHeader)
			throws WebAppException
	{
		if(postBody != null)
		{
			return postBody;
		}
		if(authorizationHeader == null || authorizationHeader.isEmpty())
		{
			throw newAuthException();
		}
		return parseAuthorizationHeader(authorizationHeader);
	}

	private static WebAppException newAuthException()
	{
		return new WebAppException(ErrorCodes.AUTH_FAILED, "Credentials not provided.");
	}

	private static Credentials parseAuthorizationHeader(String authString) throws WebAppException
	{
		String[] authHeaders = authString.split(",");
		for(String header : authHeaders)
		{
			String trimmedHeader = header.trim();
			LOGGER.info(MODULE + ".makeToken authHdr = {}", trimmedHeader);
			if(trimmedHeader.startsWith("Basic"))
			{
				return extractCredentials(trimmedHeader.substring(6).trim());
			}
		}
		throw newAuthException();
	}

	private static Credentials extractCredentials(String base64Credentials) throws WebAppException
	{
		String decodedCredentials = new String(Base64.getDecoder().decode(base64Credentials.getBytes()));
		String[] parts = decodedCredentials.split(":", 2);

		if(parts.length < 2 || parts[0] == null || parts[1] == null)
		{
			throw newAuthException();
		}

		Credentials credentials = new Credentials();
		credentials.setUsername(parts[0]);
		credentials.setPassword(parts[1]);

		LOGGER.info(MODULE + ".checkToken found tokstr in header.");
		return credentials;
	}

	private static Set<OpenDcsApiRoles> verifyAuthentication(Credentials creds) throws WebAppException
	{
		// Use username and password to attempt to connect to the database
		try (DbInterface dbi = new DbInterface())
		{
			String url = getDatabaseUrl(dbi);
			/*
			 noinspection
			 Intentional unused connection. Makes a new db connection using passed credentials
			 This validates the username & password and will throw SQLException if user/pw is not valid.
			*/
			try(Connection ignored = DriverManager.getConnection(url, creds.getUsername(), creds.getPassword());
				ApiDaoBase daoBase = new ApiDaoBase(dbi, MODULE))
			{
				Set<OpenDcsApiRoles> roles = new HashSet<>();
				roles.add(OpenDcsApiRoles.ODCS_API_GUEST);
				// Now verify that user has appropriate privilege. This only works on Postgress currently:
				String q = "select pm.roleid, pr.rolname from pg_auth_members pm, pg_roles pr "
						+ "where pm.member = (select oid from pg_roles where rolname = '" + creds.getUsername() + "') "
						+ "and pm.roleid = pr.oid";
				ResultSet rs = daoBase.doQuery(q);
				boolean hasPerm = false;
				while(rs.next() && !hasPerm)
				{
					int roleid = rs.getInt(1);
					String role = rs.getString(2);
					LOGGER.info("User '{}' has role {}={}", creds.getUsername(), roleid, role);
					if("OTSDB_ADMIN".equalsIgnoreCase(role))
					{
						hasPerm = true;
						roles.add(OpenDcsApiRoles.ODCS_API_ADMIN);
					}
					if("OTSDB_MGR".equalsIgnoreCase(role))
					{
						hasPerm = true;
						roles.add(OpenDcsApiRoles.ODCS_API_USER);
					}
				}
				if(!hasPerm)
				{
					throw new WebAppException(ErrorCodes.AUTH_FAILED,
							"User does not have OTSDB_ADMIN or OTSDB_MGR privilege - Not Authorized.");
				}
				return roles;
			}
		}
		catch(Exception e)
		{
			LOGGER.atWarn().setCause(e).log("isUserValid - Authentication failed");
			throw new WebAppException(ErrorCodes.AUTH_FAILED, "DB connection failed with passed credentials.");
		}
	}

	private static String getDatabaseUrl(DbInterface dbi) throws WebAppException
	{
		try
		{
			Connection poolCon = dbi.getConnection();
			// The only way to verify that user/pw is valid is to attempt to establish a connection:
			DatabaseMetaData metaData = poolCon.getMetaData();
			return metaData.getURL();
		}
		catch(SQLException e)
		{
			throw new WebAppException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Failed to obtain database URL.", e);
		}
	}
}
