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

package org.opendcs.odcsapi.sec;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;

import io.swagger.v3.oas.models.security.SecurityScheme;


public interface AuthorizationCheck
{

	/**
	 * Authorizes the current session returning the SecurityContext that will check user roles.
	 *
	 * @param requestContext     context for the current session.
	 * @param httpServletRequest context for the current request.
	 */
	SecurityContext authorize(ContainerRequestContext requestContext,
			HttpServletRequest httpServletRequest, ServletContext servletContext);

	boolean supports(String type, ContainerRequestContext requestContext, ServletContext servletContext);

	/**
	 * build the OpenApi SecurityScheme to render into the runtime generated spec.
	 * @return
	 */
	SecurityScheme getOaSecurityScheme();
}
