/*
 *  Copyright 2024 OpenDCS Consortium and its Contributors
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

package org.opendcs.odcsapi.sec.cwms;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;

import org.opendcs.odcsapi.sec.OpenDcsApiRoles;
import org.opendcs.odcsapi.sec.OpenDcsPrincipal;
import org.opendcs.odcsapi.sec.OpenDcsSecurityContext;
import org.opendcs.odcsapi.sec.AuthorizationCheck;

public final class CwmsAuthCheck implements AuthorizationCheck
{
	private static final String SESSION_COOKIE_NAME = "JSESSIONIDSSO";

	@Override
	public void authorize(ContainerRequestContext requestContext, HttpServletRequest httpServletRequest)
	{
		Principal userPrincipal = requestContext.getSecurityContext().getUserPrincipal();
		if(userPrincipal == null)
		{
			throw new NotAuthorizedException("Invalid session");
		}
		Set<OpenDcsApiRoles> roles = new HashSet<>();
		roles.add(OpenDcsApiRoles.ODCS_API_GUEST);
		if(requestContext.getSecurityContext().isUserInRole("CCP Mgr"))
		{
			roles.add(OpenDcsApiRoles.ODCS_API_USER);
		}
		else if(requestContext.getSecurityContext().isUserInRole("CCP Proc"))
		{
			roles.add(OpenDcsApiRoles.ODCS_API_ADMIN);
		}
		OpenDcsPrincipal openDcsPrincipal = new OpenDcsPrincipal(userPrincipal.getName(), roles);
		requestContext.setSecurityContext(new OpenDcsSecurityContext(openDcsPrincipal,
				httpServletRequest.isSecure(), SESSION_COOKIE_NAME));
		httpServletRequest.getSession(true)
				.setAttribute(OpenDcsPrincipal.USER_PRINCIPAL_SESSION_ATTRIBUTE, openDcsPrincipal);
	}
}
