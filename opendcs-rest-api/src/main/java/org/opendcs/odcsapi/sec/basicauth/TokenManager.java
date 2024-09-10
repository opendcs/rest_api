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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;

import org.opendcs.odcsapi.appmon.ApiEventClient;
import org.opendcs.odcsapi.beans.ApiAppStatus;
import org.opendcs.odcsapi.dao.ApiAppDAO;
import org.opendcs.odcsapi.dao.ApiDaoBase;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.lrgsclient.ApiLddsClient;
import org.opendcs.odcsapi.util.ApiConstants;
import org.opendcs.odcsapi.util.Base64;

public class TokenManager
{
	private static final String module = "TokenManager";
	private final SecureRandom rand = new SecureRandom();
	
	public synchronized UserToken makeToken(Credentials creds, DbInterface dbi, HttpHeaders hdrs)
		throws WebAppException
	{
		// If creds not provided in POST body, attempt to get from http header.
		if (creds == null)
		{
			String authString = hdrs.getHeaderString(HttpHeaders.AUTHORIZATION);
			if (authString != null && authString.length() > 0)
			{
				String authHdrs[] = authString.split(",");
				for(int idx = 0; idx < authHdrs.length; idx++)
				{
					String x = authHdrs[idx].trim();
					Logger.getLogger(ApiConstants.loggerName).info(module + ".makeToken authHdrs[" + idx + "] = " + x);
					if (x.startsWith("Basic"))
					{
						int sp = x.indexOf(' ');
						if (sp > 0)
						{
							x = x.substring(sp).trim();
							String up = new String(Base64.decodeBase64(x.getBytes()));
							String ups[] = up.split(":");
							if (ups == null || ups.length < 2 || ups[0] == null || ups[1] == null)
								throw new WebAppException(ErrorCodes.AUTH_FAILED, "Credentials not provided.");
							creds = new Credentials();
							creds.setUsername(ups[0]);
							creds.setPassword(ups[1]);
							Logger.getLogger(ApiConstants.loggerName).info(module 
								+ ".checkToken found tokstr in header.");
						}
					}
				}
			}
		}
		
		// get URL from the dbi
		Connection poolCon = dbi.getConnection();
		Connection userCon = null;

		try (ApiDaoBase daoBase = new ApiDaoBase(dbi, module))
		{
			// The only way to verify that user/pw is valid is to attempt to establish a connection:
			DatabaseMetaData metaData = poolCon.getMetaData();
			String url = metaData.getURL();
			
			// Mmake a new db connection using passed credentials
			// This validates the username & password.
			// This will throw SQLException if user/pw is not valid.
			userCon = DriverManager.getConnection(url, creds.getUsername(), creds.getPassword());
			
			if (!DbInterface.isOracle)
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
					Logger.getLogger(ApiConstants.loggerName).info("User '" + creds.getUsername() 
						+ "' has role " + roleid + "=" + role);
					if (role.equalsIgnoreCase("OTSDB_ADMIN") || role.equalsIgnoreCase("OTSDB_MGR"))
						hasPerm = true;
				}
				
				if (!hasPerm)
					throw new WebAppException(ErrorCodes.AUTH_FAILED, 
						"User does not have OTSDB_ADMIN or OTSDB_MGR privilege - Not Authorized.");
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(ApiConstants.loggerName).warning("isUserValid - Authentication failed: " + e);
			throw new WebAppException(ErrorCodes.AUTH_FAILED, "DB connection failed with passed credentials."); 
		}
		finally
		{
			if (userCon != null)
				try { userCon.close(); } catch(Exception ex) {}
		}

		// Make a UserToken and token string. Place in Hashmap & return string.
		String tokstr = Long.toHexString(rand.nextLong());
		UserToken userTok = new UserToken(tokstr, creds.getUsername());
		Logger.getLogger(ApiConstants.loggerName).fine("Added new token for user '" 
			+ creds.getUsername() + "'=" + tokstr);
		
		return userTok;
	}
	
	/**
	 * Check token from header or URL. If one is found, update its lastUsed time.
	 * @param headers the HTTP header
	 * @return true if token is valid, false if not.
	 * @throws WebAppException if an invalid token is provided.
	 */
	boolean checkToken(HttpHeaders headers, UserToken userToken)
	{
		String token = getToken(headers);
		return Objects.equals(userToken.getToken(), token);
	}
	
	/**
	 * Performs the check operation, but also returns the UserToken being
	 * cached for the current session.
	 * @param headers HTTP Request headers
	 * @return UserToken with client identifying information for the session
	 * @throws WebAppException
	 */
	private String getToken(HttpHeaders headers)
	{
		String tokstr = null;

		// try to get tokstr from header Authentication - Bearer
		String authString = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
		if (authString != null && !authString.isEmpty())
		{
			String[] authHeaders = authString.split(",");
			for(int idx = 0; idx < authHeaders.length; idx++)
			{
				String x = authHeaders[idx].trim();
				Logger.getLogger(ApiConstants.loggerName).info(module + ".checkToken authHdrs[" + idx + "] = " + x);
				if (x.startsWith("Bearer"))
				{
					int sp = x.indexOf(' ');
					if (sp > 0)
					{
						tokstr = x.substring(sp).trim();
						Logger.getLogger(ApiConstants.loggerName).info(module 
							+ ".checkToken found tokstr in header: " + tokstr);
					}
				}
			}
		}
		return tokstr;
	}
}
