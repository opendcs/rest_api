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
import java.sql.SQLException;

import javax.sql.DataSource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.opendcs.database.SimpleTransaction;
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
	public Response getOrganizations() throws DbException
	{
		DataSource dataSource = getDataSource();
		String databaseType = getDatabaseType(dataSource);
		if("cwms".equalsIgnoreCase(databaseType))
		{
			try(Connection connection = dataSource.getConnection())
			{

				CwmsOrganizationDao cwmsOrganizationDao = new CwmsOrganizationDao();
				return Response.status(HttpServletResponse.SC_OK)
						.entity(cwmsOrganizationDao.retrieveOrganizationIds(new SimpleTransaction(connection)))
						.build();
			}
			catch(SQLException e)
			{
				throw new DbException("Error establishing connection to the database.", e);
			}
		}
		return Response.status(HttpServletResponse.SC_NOT_FOUND)
				.entity(new Status("Session Valid"))
				.build();
	}
}
