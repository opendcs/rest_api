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

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opendcs.odcsapi.util.ApiConstants;
import org.opendcs.odcsapi.util.ApiHttpUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;

@Path("/")
public final class SessionResource
{

	@Context
	HttpServletRequest request;

	@GET
	@Path("check")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	@Operation(
			summary = "Check Session Authorization",
			description = "Verifies if the current session token is valid and authorized.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Token is valid"),
					@ApiResponse(responseCode = "403", description = "Forbidden - User is not authorized", content = @Content),
					@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
			}
	)
	public Response checkSessionAuthorization()
	{
		//Security filters will ensure this method is only accessible via an authenticated client
		return ApiHttpUtil.createResponse("Token Valid");
	}

	@DELETE
	@Path("logout")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	@Operation(
			summary = "Logout",
			description = "Logs the user out by invalidating the current session.",
			responses = {
					@ApiResponse(responseCode = "204", description = "Logout successful"),
			}
	)
	public Response logout()
	{
		HttpSession session = request.getSession(false);
		if(session != null)
		{
			session.invalidate();
		}
		return Response.status(HttpServletResponse.SC_NO_CONTENT).build();
	}
}
