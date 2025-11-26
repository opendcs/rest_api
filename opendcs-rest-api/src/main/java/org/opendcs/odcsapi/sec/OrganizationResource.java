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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.opendcs.odcsapi.beans.ApiAlgorithmRef;
import org.opendcs.odcsapi.beans.Status;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.dao.cwms.CwmsOrganizationDao;
import org.opendcs.odcsapi.res.OpenDcsResource;
import org.opendcs.odcsapi.util.ApiConstants;

import static org.opendcs.odcsapi.dao.OpenDcsDatabaseFactory.getDatabaseType;

@Path("/")
public final class OrganizationResource extends OpenDcsResource
{
	@GET
	@Path("organizations")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST, ApiConstants.ODCS_API_USER, ApiConstants.ODCS_API_ADMIN})
	@Operation(
			summary = "Request the list of valid organizations",
			description = "Organizations are used by queries to filter to a subset of data that a user is authorized for.",
			responses = {
					@ApiResponse(responseCode = "200", description = "A list of organizations will be returned."),
					@ApiResponse(
							responseCode = "404",
							description = "If no organizations are available.",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
									array = @ArraySchema(schema = @Schema(implementation = String.class)))
					)
			},
			tags = {"REST - Authentication and Authorization"}
	)
	public Response checkSessionAuthorization() throws DbException
	{
		DataSource dataSource = getDataSource();
		String databaseType = getDatabaseType(dataSource);
		if("cwms".equalsIgnoreCase(databaseType))
		{
			CwmsOrganizationDao cwmsOrganizationDao = new CwmsOrganizationDao(dataSource);
			return Response.status(HttpServletResponse.SC_OK).entity(cwmsOrganizationDao.retrieveOrganizationIds())
					.build();
		}
		return Response.status(HttpServletResponse.SC_NOT_FOUND).entity(new Status("Session Valid"))
				.build();
	}
}
