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

import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.opendcs.odcsapi.res.SwaggerResources;


@Provider
@Priority(Priorities.USER)
public final class SecurityHeadersFilter implements ContainerResponseFilter
{

	@Override
	public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException
	{
		response.getHeaders().putSingle("Strict-Transport-Security", "max-age=63072000");
		response.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
		response.getHeaders().putSingle("Access-Control-Allow-Headers", "Content-Type");
		response.getHeaders().remove("Server");
		//swagger returns html/js/css/png/etc
		if(request.getUriInfo()
				.getMatchedResources()
				.stream()
				.map(Object::getClass)
				.noneMatch(c -> c == SwaggerResources.class))
		{
			response.getHeaders().putSingle("X-Content-Type-Options", "nosniff");
			response.getHeaders().putSingle("Content-Type", MediaType.APPLICATION_JSON_TYPE);
		}
	}
}
