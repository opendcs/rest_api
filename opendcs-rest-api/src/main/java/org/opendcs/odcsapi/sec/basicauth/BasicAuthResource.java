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

import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opendcs.odcsapi.beans.TokenBean;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.sec.Public;
import org.opendcs.odcsapi.util.ApiHttpUtil;

@Path("/")
public class BasicAuthResource
{

	@Context private HttpServletRequest request;
	@Context private HttpHeaders httpHeaders;

	@GET
	@Path("check")
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkToken()
	{
		//Security filters will ensure this method is only accessible via an authenticated client
		return ApiHttpUtil.createResponse("Token Valid");
	}

	@POST
	@Path("credentials")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Public
	public Response postCredentials(Credentials credentials)
			throws WebAppException, DbException
	{
		if(credentials == null)
		{
			throw new WebAppException(HttpServletResponse.SC_NOT_ACCEPTABLE,
					"Credentials may not be null.");
		}
		String u = credentials.getUsername();
		String p = credentials.getPassword();
		if (u == null || u.trim().isEmpty() || p == null || p.trim().isEmpty())
		{
			throw new WebAppException(HttpServletResponse.SC_NOT_ACCEPTABLE,
					"Neither username nor password may be null.");
		}
		for(int i=0; i<u.length(); i++)
		{
			char c = u.charAt(i);
			if (!Character.isLetterOrDigit(c) && c != '_' && c != '.')
				throw new WebAppException(ErrorCodes.AUTH_FAILED,
						"Username may only contain alphanumeric, underscore, or period.");
		}
		for(int i=0; i<p.length(); i++)
		{
			char c = p.charAt(i);
			if (Character.isWhitespace(c) || c == '\'')
				throw new WebAppException(ErrorCodes.AUTH_FAILED,
						"Password may not contain whitespace or quote.");
		}

		// Use username and password to attempt to connect to the database
		try (DbInterface dbi = new DbInterface())
		{
			String authorizationHeader = httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION);
			UserToken userToken = TokenManager.makeToken(credentials, dbi, authorizationHeader);
			HttpSession session = request.getSession(true);
			session.setAttribute(UserToken.USER_TOKEN_ATTRIBUTE, userToken);
			TokenBean ret = new TokenBean();
			ret.setUsername(userToken.getUsername());
			ret.setToken(userToken.getToken());
			return ApiHttpUtil.createResponse(ret);
		}
	}

	@GET
	@Path("credentials")
	@Produces(MediaType.APPLICATION_JSON)
	@Public
	public Response postCredentials() throws WebAppException, DbException
	{
		// Use username and password to attempt to connect to the database
		try (DbInterface dbi = new DbInterface())
		{
			String authorizationHeader = httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION);
			UserToken userToken = TokenManager.makeToken(null, dbi, authorizationHeader);
			HttpSession session = request.getSession(true);
			session.setAttribute(UserToken.USER_TOKEN_ATTRIBUTE, userToken);
			TokenBean ret = new TokenBean();
			ret.setUsername(userToken.getUsername());
			ret.setToken(userToken.getToken());

			// Place the new token in the return JSON and in the header.
			String[] tokenHeader = new String[]{HttpHeaders.AUTHORIZATION, "Bearer " + userToken.getToken()};
			ArrayList<String[]> hdrs = new ArrayList<>();
			hdrs.add(tokenHeader);
			return ApiHttpUtil.createResponseWithHeaders(ret, HttpServletResponse.SC_OK, hdrs);
		}
	}

}
