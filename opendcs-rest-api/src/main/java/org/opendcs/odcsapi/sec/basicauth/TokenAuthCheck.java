/*
 *  Copyright 2024 OpenDCS Consortium
 *
 *  Licensed under the Apache License, Version 2.0 (the "License")
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opendcs.odcsapi.sec.basicauth;

import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;

import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.sec.OpenDcsPrincipal;
import org.opendcs.odcsapi.sec.OpenDcsSecurityContext;
import org.opendcs.odcsapi.sec.SecurityCheck;

public final class TokenAuthCheck implements SecurityCheck
{
	private static final String AUTHORIZATION_HEADER = "Authorization";

	@Override
	public void authenticate(ContainerRequestContext requestContext, HttpServletRequest httpServletRequest)
	{
		HttpSession session = httpServletRequest.getSession(false);
		if(session == null)
		{
			throw new NotAuthorizedException("No session found");
		}
		Object attribute = session.getAttribute(UserToken.USER_TOKEN_ATTRIBUTE);
		if(!(attribute instanceof UserToken))
		{
			throw new NotAuthorizedException("Invalid session");
		}
		String authorizationHeader = requestContext.getHeaderString(AUTHORIZATION_HEADER);
		if(authorizationHeader == null)
		{
			throw new NotAuthorizedException("Invalid authorization header");
		}
		UserToken userToken = (UserToken) attribute;
		TokenManager.checkToken(authorizationHeader, userToken);
		requestContext.setSecurityContext(new OpenDcsSecurityContext(new OpenDcsPrincipal(userToken.getToken()),
				Collections.singleton(DbInterface.getAuthenticatedRole()), httpServletRequest.isSecure(),
				"basic-auth-token"));
	}
}
