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

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.WebApplicationException;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.opendcs.odcsapi.dao.ApiDaoBase;
import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TokenManager
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TokenManager.class);
	private static final String MODULE = "TokenManager";
	private static final String SECRET_KEY;

	static
	{
		byte[] key = new byte[32];
		new SecureRandom().nextBytes(key);
		SECRET_KEY = Base64.getEncoder().encodeToString(key);
	}

	private TokenManager()
	{
		throw new AssertionError("Utility class cannot be instantiated.");
	}

	static UserToken makeToken(Credentials postBody, DbInterface dbi, String authorizationHeader)
			throws WebAppException
	{
		// If creds not provided in POST body, attempt to get from http header.
		Credentials credentials = getCredentials(postBody, authorizationHeader);
		verifyAuthentication(credentials, dbi);
		return createToken(credentials);
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

	private static void verifyAuthentication(Credentials creds, DbInterface dbi) throws WebAppException
	{
		String url = getDatabaseUrl(dbi);
		//noinspection unused connection. only validating that the user can log in
		try(Connection userCon = DriverManager.getConnection(url, creds.getUsername(), creds.getPassword());
			ApiDaoBase daoBase = new ApiDaoBase(dbi, MODULE))
		{

			// Make a new db connection using passed credentials
			// This validates the username & password.
			// This will throw SQLException if user/pw is not valid.


			if(!DbInterface.isOracle)
			{
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
					if("OTSDB_ADMIN".equalsIgnoreCase(role)
							|| role.equalsIgnoreCase(DbInterface.getAuthenticatedRole()))
					{
						hasPerm = true;
					}
				}

				if(!hasPerm)
				{
					throw new WebAppException(ErrorCodes.AUTH_FAILED,
							"User does not have OTSDB_ADMIN or OTSDB_MGR privilege - Not Authorized.");
				}
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

	private static UserToken createToken(Credentials creds) throws WebAppException
	{
		try
		{
			JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
					.subject(creds.getUsername())
					.claim("role", DbInterface.getAuthenticatedRole())
					.issueTime(new Date())
					.build();
			SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
			signedJWT.sign(new MACSigner(SECRET_KEY.getBytes()));
			String token = signedJWT.serialize();
			UserToken userTok = new UserToken(token, creds.getUsername());
			LOGGER.debug("Added new token for user '{}'={}", creds.getUsername(), token);
			return userTok;
		}
		catch(JOSEException e)
		{
			LOGGER.atWarn().setCause(e).log("JWT generation failed.");
			throw new WebAppException(ErrorCodes.AUTH_FAILED, "DB connection failed with passed credentials.");
		}
	}

	/**
	 * Check token from header or URL. If one is found, update its lastUsed time.
	 *
	 * @param authorizationHeader string obtained from Authorization header
	 * @throws WebApplicationException if the token is invalid
	 */
	static void checkToken(String authorizationHeader, UserToken userToken)
			throws WebApplicationException
	{
		String token = getToken(authorizationHeader);
		if(token == null)
		{
			throw new NotAuthorizedException("Invalid authorization header");
		}
		verifyToken(token, userToken);
	}

	private static String getToken(String authorizationHeader)
	{
		String tokstr = null;
		// try to get tokstr from header Authentication - Bearer
		if(authorizationHeader != null && !authorizationHeader.isEmpty())
		{
			String[] authHeaders = authorizationHeader.split(",");
			for(int idx = 0; idx < authHeaders.length; idx++)
			{
				String x = authHeaders[idx].trim();
				LOGGER.info(MODULE + ".checkToken authHdrs[{}] = {}", idx, x);
				if(x.startsWith("Bearer"))
				{
					int sp = x.indexOf(' ');
					if(sp > 0)
					{
						tokstr = x.substring(sp).trim();
						LOGGER.info(MODULE + ".checkToken found tokstr in header: {}", tokstr);
					}
				}
			}
		}
		return tokstr;
	}

	private static void verifyToken(String token, UserToken userToken)
			throws WebApplicationException
	{
		try
		{
			SignedJWT signedJWT = SignedJWT.parse(token);
			JWSVerifier verifier = new MACVerifier(SECRET_KEY.getBytes());
			if (!signedJWT.verify(verifier))
			{
				throw new NotAuthorizedException("Bad JWT");
			}

			JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
			String username = claims.getSubject();
			String role = (String) claims.getClaim("role");

			boolean isValid = Objects.equals(userToken.getToken(), token) &&
					Objects.equals(userToken.getUsername(), username) &&
					Objects.equals(DbInterface.getAuthenticatedRole(), role);

			if(!isValid)
			{
				throw new NotAuthorizedException("Invalid JWT");
			}
		}
		catch (ParseException | JOSEException e)
		{
			LOGGER.debug(MODULE + ".verifyToken: Error verifying JWT token.", e);
			throw new NotAuthorizedException("Bad JWT");
		}
	}
}
