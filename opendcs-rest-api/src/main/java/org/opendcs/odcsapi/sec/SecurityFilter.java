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

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import javax.annotation.Priority;
import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.opendcs.odcsapi.sec.basicauth.BasicAuthCheck;
import org.opendcs.odcsapi.sec.cwms.ServletSsoAuthCheck;
import org.opendcs.odcsapi.sec.openid.OidcAuthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Priority(Priorities.AUTHENTICATION)
public final class SecurityFilter implements ContainerRequestFilter
{

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityFilter.class);
	static final String LAST_AUTHORIZATION_CHECK = "opendcs-last-authorization-check";
	@Context private ResourceInfo resourceInfo;
	@Context private HttpHeaders httpHeaders;
	@Context private HttpServletRequest httpServletRequest;
	@Context private ServletContext servletContext;

	@Override
	public void filter(ContainerRequestContext requestContext)
	{
		if(isPublicEndpoint())
		{
			setupGuestContext(requestContext);
		}
		else
		{
			if(LOGGER.isDebugEnabled())
			{
				LOGGER.debug("Secured endpoint identified: {}", resourceInfo.getResourceMethod().toGenericString());
			}
			HttpSession session = httpServletRequest.getSession(true);
			Object sessionPrincipal = session.getAttribute(OpenDcsPrincipal.USER_PRINCIPAL_SESSION_ATTRIBUTE);
			if(isAuthorizationExpired(session) || !(sessionPrincipal instanceof OpenDcsPrincipal))
			{
				authorizeSession(requestContext, session);
			}
			else
			{
				OpenDcsPrincipal principal = (OpenDcsPrincipal) sessionPrincipal;
				requestContext.setSecurityContext(new OpenDcsSecurityContext(principal,
						httpServletRequest.isSecure(), SecurityContext.BASIC_AUTH));
			}
			verifyRoles(requestContext);
		}
	}

	private void setupGuestContext(ContainerRequestContext requestContext)
	{
		if(LOGGER.isDebugEnabled())
		{
			LOGGER.debug("Public endpoint identified: {}", resourceInfo.getResourceMethod().toGenericString());
		}
		OpenDcsPrincipal principal = new OpenDcsPrincipal("guest", Collections.singleton(OpenDcsApiRoles.ODCS_API_GUEST));
		requestContext.setSecurityContext(new OpenDcsSecurityContext(principal,
				httpServletRequest.isSecure(), ""));
	}

	private void verifyRoles(ContainerRequestContext requestContext)
	{
		SecurityContext securityContext = requestContext.getSecurityContext();
		RolesAllowed annotation = resourceInfo.getResourceMethod().getAnnotation(RolesAllowed.class);
		String endpoint = requestContext.getMethod() + " " + requestContext.getUriInfo().getPath();
		if(annotation == null)
		{
			throw new InternalServerErrorException("Endpoint " + endpoint + " does not have the @RolesAllowed annotation");
		}
		String[] value = annotation.value();
		for(String role : value)
		{
			if(securityContext.isUserInRole(role))
			{
				return;
			}
		}
		throw new ForbiddenException("User does not have the correct roles for endpoint: " + endpoint);
	}

	private void authorizeSession(ContainerRequestContext requestContext, HttpSession session)
	{
		SecurityContext securityContext = lookupAuthCheck().authorize(requestContext, httpServletRequest);
		requestContext.setSecurityContext(securityContext);
		Principal principal = securityContext.getUserPrincipal();
		session.setAttribute(OpenDcsPrincipal.USER_PRINCIPAL_SESSION_ATTRIBUTE, principal);//NOSONAR impl is Serializable
		session.setAttribute(LAST_AUTHORIZATION_CHECK, Instant.now());
	}

	private boolean isAuthorizationExpired(HttpSession session)
	{
		Instant lastAuthorizationCheck = (Instant) session.getAttribute(LAST_AUTHORIZATION_CHECK);
		String expirationMinutes = servletContext.getInitParameter("opendcs.rest.api.authorization.expiration.duration");
		if(expirationMinutes == null)
		{
			expirationMinutes = "PT15M";
		}
		long expirationSeconds = Duration.parse(expirationMinutes).get(ChronoUnit.SECONDS);
		return lastAuthorizationCheck == null
				|| Duration.between(lastAuthorizationCheck, Instant.now()).abs().get(ChronoUnit.SECONDS) >= expirationSeconds;
	}

	private boolean isPublicEndpoint()
	{
		boolean retval = false;
		RolesAllowed annotation = resourceInfo.getResourceMethod().getAnnotation(RolesAllowed.class);
		if(annotation != null)
		{
			String[] roles = annotation.value();
			if(roles != null
					&& Arrays.asList(roles).contains(OpenDcsApiRoles.ODCS_API_GUEST.getRole()))
			{
				retval = true;
			}
		}
		return retval;
	}

	private AuthorizationCheck lookupAuthCheck()
	{
		AuthorizationCheck check;
		String authorizationType = servletContext.getInitParameter("opendcs.rest.api.authorization.type");
		LOGGER.info("Initializing odcsapi RestServices with authorization type '{}'.", authorizationType);
		if("basic".equals(authorizationType))
		{
			check = new BasicAuthCheck();
		}
		else if("openid".equals(authorizationType))
		{
			check = new OidcAuthCheck();
		}
		else if("sso".equals(authorizationType))
		{
			check = new ServletSsoAuthCheck();
		}
		else
		{
			throw new IllegalStateException("Property opendcs.rest.api.authorization must be configured to one of 'basic', 'openid', or 'sso'.");
		}
		return check;
	}
}
