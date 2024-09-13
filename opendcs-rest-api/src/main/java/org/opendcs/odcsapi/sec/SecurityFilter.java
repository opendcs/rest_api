/*
 *  Copyright 2024 OpenDCS Consortium and its Contributors
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

package org.opendcs.odcsapi.sec;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import javax.annotation.Priority;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.opendcs.odcsapi.sec.cwms.CwmsAuthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Priority(Priorities.AUTHENTICATION)
public final class SecurityFilter implements ContainerRequestFilter
{

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityFilter.class);
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
			if(LOGGER.isDebugEnabled())
			{
				LOGGER.debug("Public endpoint identified: {}", resourceInfo.getResourceMethod().toGenericString());
			}
			OpenDcsPrincipal principal = new OpenDcsPrincipal("guest", Collections.singleton(OpenDcsApiRoles.ODCS_API_GUEST));
			requestContext.setSecurityContext(new OpenDcsSecurityContext(principal,
					httpServletRequest.isSecure(), ""));
		}
		else
		{
			if(LOGGER.isDebugEnabled())
			{
				LOGGER.debug("Secured endpoint identified: {}", resourceInfo.getResourceMethod().toGenericString());
			}
			HttpSession session = httpServletRequest.getSession(false);
			if(session == null)
			{
				throw new NotAuthorizedException("No session found");
			}
			Object attribute = session.getAttribute(OpenDcsPrincipal.USER_PRINCIPAL_SESSION_ATTRIBUTE);
			if(attribute instanceof OpenDcsPrincipal)
			{
				OpenDcsPrincipal principal = (OpenDcsPrincipal) attribute;
				requestContext.setSecurityContext(new OpenDcsSecurityContext(principal,
						httpServletRequest.isSecure(), SecurityContext.BASIC_AUTH));
			}
			else
			{
				AuthorizationCheck authorizationCheck = new CwmsAuthCheck();
				authorizationCheck.authorize(requestContext, httpServletRequest);
			}
			SecurityContext securityContext = requestContext.getSecurityContext();
			if(!securityContext.isUserInRole(OpenDcsApiRoles.ODCS_API_USER.name())
					|| !securityContext.isUserInRole(OpenDcsApiRoles.ODCS_API_ADMIN.name()))
			{
				throw new ForbiddenException("User does not have the correct roles");
			}
		}
	}

	private boolean isPublicEndpoint()
	{
		boolean retval = false;
		RolesAllowed annotation = resourceInfo.getResourceMethod().getAnnotation(RolesAllowed.class);
		if(annotation != null)
		{
			String[] roles = annotation.value();
			if(roles != null
					&& Arrays.asList(roles).contains(OpenDcsApiRoles.ODCS_API_GUEST.name()))
			{
				retval = true;
			}
		}
		return retval;
	}
}
