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

import java.io.IOException;
import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.sec.Public;

@Provider
@Priority(Priorities.AUTHENTICATION)
public final class TokenAuthenticatorFilter implements ContainerRequestFilter
{
	@Context
	private ResourceInfo resourceInfo;
	@Context
	private HttpHeaders httpHeaders;
	@Context
	private HttpServletRequest httpServletRequest;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException
	{
		if(isPublicEndpoint())
		{
			return;
		}
		HttpSession session = httpServletRequest.getSession(false);
		if(session == null)
		{
			requestContext.abortWith(Response.status(ErrorCodes.TOKEN_REQUIRED,
					"Valid token is required for this operation. No client session found.").build());
		}
		else
		{
			Object attribute = session.getAttribute(UserToken.USER_TOKEN_ATTRIBUTE);
			if(attribute instanceof UserToken)
			{
				boolean validToken = DbInterface.getTokenManager().checkToken(httpHeaders, (UserToken) attribute);
				if(validToken)
				{
					return;
				}
			}
			requestContext.abortWith(Response.status(ErrorCodes.TOKEN_REQUIRED,
					"Valid token is required for this operation.").build());
		}
	}

	private boolean isPublicEndpoint()
	{
		if(resourceInfo.getResourceClass().isAnnotationPresent(Public.class))
		{
			return true;
		}
		return resourceInfo.getResourceMethod().isAnnotationPresent(Public.class);
	}
}
