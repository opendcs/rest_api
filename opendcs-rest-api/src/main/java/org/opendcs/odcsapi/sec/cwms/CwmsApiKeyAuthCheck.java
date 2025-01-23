/*
 *  Copyright 2025 OpenDCS Consortium and its Contributors
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

package org.opendcs.odcsapi.sec.cwms;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

import com.google.auto.service.AutoService;
import org.opendcs.odcsapi.dao.ApiAuthorizationDAI;
import org.opendcs.odcsapi.sec.AuthorizationCheck;
import org.opendcs.odcsapi.sec.OpenDcsApiRoles;
import org.opendcs.odcsapi.sec.OpenDcsPrincipal;
import org.opendcs.odcsapi.sec.OpenDcsSecurityContext;

import static org.opendcs.odcsapi.sec.cwms.ServletSsoAuthCheck.SESSION_COOKIE_NAME;

@AutoService(AuthorizationCheck.class)
public final class CwmsApiKeyAuthCheck extends AuthorizationCheck
{
	private static final String AUTH_HEADER = "Authorization";
	private static final Pattern APIKEY_PATTERN = Pattern.compile("apikey (.*)");

	@Override
	public OpenDcsSecurityContext authorize(ContainerRequestContext requestContext, HttpServletRequest httpServletRequest, ServletContext servletContext)
	{
		try(ApiAuthorizationDAI authorizationDao = getAuthDao(servletContext))
		{
			String apiKey = getApiKey(requestContext).orElseThrow(() -> new ServerErrorException(
					"No apikey found for client authorization.", Response.Status.UNAUTHORIZED.getStatusCode()));
			String username = authorizationDao.getUserForApiKey(apiKey);
			Set<OpenDcsApiRoles> roles = authorizationDao.getRoles(username);
			OpenDcsPrincipal openDcsPrincipal = new OpenDcsPrincipal(username, roles);
			return new OpenDcsSecurityContext(openDcsPrincipal,
					httpServletRequest.isSecure(), SESSION_COOKIE_NAME);
		}
		catch(Exception e)
		{
			throw new ServerErrorException("Error accessing database to determine user roles",
					Response.Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@Override
	public boolean supports(String type, ContainerRequestContext requestContext)
	{
		return "apikey".equals(type) && getApiKey(requestContext).isPresent();
	}

	private Optional<String> getApiKey(ContainerRequestContext request)
	{
		Optional<String> retval = Optional.empty();
		String header = request.getHeaderString(AUTH_HEADER);
		if(header != null)
		{
			Matcher matcher = APIKEY_PATTERN.matcher(header);
			if(matcher.matches())
			{
				retval = Optional.of(matcher.group(1));
			}
		}
		return retval;
	}
}
