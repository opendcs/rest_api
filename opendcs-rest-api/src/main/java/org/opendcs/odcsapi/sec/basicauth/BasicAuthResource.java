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

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.sec.OpenDcsPrincipal;
import org.opendcs.odcsapi.sec.AuthorizationCheck;
import org.opendcs.odcsapi.util.ApiHttpUtil;

@Path("/")
public class BasicAuthResource
{

	@Context private HttpServletRequest request;
	@Context private HttpHeaders httpHeaders;

	@POST
	@Path("credentials")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response postCredentials(Credentials credentials) throws WebAppException
	{
		//If credentials are null, Authorization header will be checked.
		if(credentials != null)
		{
			validateCredentials(credentials);
		}

		String authorizationHeader = httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION);
		OpenDcsPrincipal principal = BasicAuthenticationUtil.makeUserPrincipal(credentials, authorizationHeader);
		HttpSession oldSession = request.getSession(false);
		if(oldSession != null)
		{
			oldSession.invalidate();
		}
		HttpSession session = request.getSession(true);
		session.setAttribute(OpenDcsPrincipal.USER_PRINCIPAL_SESSION_ATTRIBUTE, principal);
		return ApiHttpUtil.createResponse("Authentication Successful.");
	}

	private static void validateCredentials(Credentials credentials) throws WebAppException
	{
		String u = credentials.getUsername();
		String p = credentials.getPassword();
		if(u == null || u.trim().isEmpty() || p == null || p.trim().isEmpty())
		{
			throw new WebAppException(HttpServletResponse.SC_NOT_ACCEPTABLE,
					"Neither username nor password may be null.");
		}
		for(int i = 0; i < u.length(); i++)
		{
			char c = u.charAt(i);
			if(!Character.isLetterOrDigit(c) && c != '_' && c != '.')
				throw new WebAppException(ErrorCodes.AUTH_FAILED,
						"Username may only contain alphanumeric, underscore, or period.");
		}
		for(int i = 0; i < p.length(); i++)
		{
			char c = p.charAt(i);
			if(Character.isWhitespace(c) || c == '\'')
				throw new WebAppException(ErrorCodes.AUTH_FAILED,
						"Password may not contain whitespace or quote.");
		}
	}

}
