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

package org.opendcs.odcsapi.res;

import java.sql.SQLException;
import java.util.ArrayList;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.opendcs.odcsapi.beans.ApiAppRef;
import org.opendcs.odcsapi.beans.ApiAppStatus;
import org.opendcs.odcsapi.beans.ApiLoadingApp;
import org.opendcs.odcsapi.dao.ApiAppDAO;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.util.ApiConstants;
import org.opendcs.odcsapi.util.ApiHttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resources for editing, monitoring, stopping, and starting processes.
 */
@Path("/")
public final class AppResources
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AppResources.class);
	@Context private HttpServletRequest request;
	@Context private HttpHeaders httpHeaders;

	@GET
	@Path("apprefs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	@Operation(
			summary = "Retrieves a list of application references",
			description = "Example:  \n\n    http://localhost:8080/odcsapi/apprefs",
			responses = {
					@ApiResponse(responseCode = "200", description = "Success",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
									array = @ArraySchema(schema = @Schema(implementation = ApiAppRef.class)))),
					@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
			},
			tags = {"REST - Loading Application Records"}
	)
	public Response getAppRefs() throws DbException
	{
		LOGGER.trace("Getting App Refs.");
		try (DbInterface dbi = new DbInterface();
			ApiAppDAO dao = new ApiAppDAO(dbi))
		{
			ArrayList<ApiAppRef> ret = dao.getAppRefs();
			LOGGER.trace("Returning {} apps.", ret.size());
			return ApiHttpUtil.createResponse(ret);
		}
	}

	@GET
	@Path("app")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	@Operation(
			summary = "Retrieve a Single Application by its ID",
			description = "Example: \n\n    http://localhost:8080/odcsapi/app?appid=4  \n" +
					"**Note**: appType may be omitted if it is not defined in the database.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Success",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
									array = @ArraySchema(schema = @Schema(implementation = ApiLoadingApp.class)))),
					@ApiResponse(responseCode = "400", description = "Bad Request - Missing required appId parameter",
							content = @Content),
					@ApiResponse(responseCode = "404", description = "Not Found - No app found with the given ID",
							content = @Content),
					@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
			},
			tags = {"REST - Loading Application Records"}
	)
	public Response getApp(@Parameter(description = "App ID", required = true, example = "4",
			schema = @Schema(implementation = Long.class))
		@QueryParam("appid") Long appId)
			throws WebAppException, DbException, SQLException
	{
		if (appId == null)
			throw new WebAppException(ErrorCodes.MISSING_ID,
				"Missing required appid parameter.");
		LOGGER.debug("Getting app with id {}", appId);
		try (DbInterface dbi = new DbInterface();
			ApiAppDAO dao = new ApiAppDAO(dbi))
		{
			return ApiHttpUtil.createResponse(dao.getApp(appId));
		}
	}

	@POST
	@Path("app")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	@Operation(
			summary = "Create or Overwrite Existing App",
			description = "It takes a single DECODES Loading Application in JSON format, as described above for GET.  \n\n" +
					"For creating a new record, leave appId out of the passed data structure.  \n\n" +
					"For overwriting an existing one, include the appId that was previously returned. " +
					"The app in the database is replaced with the one sent.",
			requestBody = @RequestBody(
					description = "Loading App",
					required = true,
					content = @Content(mediaType = MediaType.APPLICATION_JSON,
							schema = @Schema(implementation = ApiLoadingApp.class))
			),
			responses = {
					@ApiResponse(responseCode = "201", description = "Successfully stored application",
							content = @Content(schema = @Schema(implementation = ApiLoadingApp.class),
									mediaType = MediaType.APPLICATION_JSON)),
					@ApiResponse(responseCode = "500", description = "Database error occurred", content = @Content)
			},
			tags = {"REST - Loading Application Records"}
	)
	public Response postApp(ApiLoadingApp app)
		throws WebAppException, DbException, SQLException
	{
		LOGGER.debug("Post app received app {} with id {}", app.getAppName(), app.getAppId());
		try (DbInterface dbi = new DbInterface();
			ApiAppDAO dao = new ApiAppDAO(dbi))
		{
			dao.writeApp(app);
			return ApiHttpUtil.createResponse(app);
		}
	}

	@DELETE
	@Path("app")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	@Operation(
			summary = "Delete Existing Loading App",
			description = "Required argument appid must be passed in the URL.  \n\n" +
					"This operation will fail if the loading application is currently being used by any " +
					"computations or schedule entries, or if it is currently running and has " +
					"an active CP_COMP_PROC_LOCK record.",
			responses = {
					@ApiResponse(responseCode = "204", description = "Successfully deleted application",
							content = @Content),
					@ApiResponse(responseCode = "400", description = "Bad Request - Missing required appId parameter",
							content = @Content),
					@ApiResponse(responseCode = "404", description = "Not Found - No app found with the given ID",
							content = @Content),
					@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
			},
			tags = {"REST - Loading Application Records"}
	)
	public Response deletApp(@Parameter(description = "App ID", required = true, example = "4",
			schema = @Schema(implementation = Long.class))
		@QueryParam("appid") Long appId)
			throws WebAppException, DbException, SQLException
	{
		LOGGER.debug("Delete app received request to delete app with id {}", appId);

		// Use username and password to attempt to connect to the database
		try (DbInterface dbi = new DbInterface();
			ApiAppDAO dao = new ApiAppDAO(dbi))
		{
			dao.deleteApp(appId);
			return ApiHttpUtil.createResponse("appId with ID " + appId + " deleted");
		}
	}

	@GET
	@Path("appstat")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	@Operation(
			summary = "Returns an array with one element for each application",
			description = "*REST - Loading Application Records* describes API methods for retrieving and " +
					"manipulating 'Loading Application' records. The concept of a 'Loading App' has been generalized " +
					"to include any application that is known by the OpenDCS software.  \\n  " +
					"\\nApplications each have a set of properties. The following properties are relevant to M&C: " +
					"•\tstartCmd – A string containins a command used to start the application on this server. " +
					"Most of the OpenDCS apps use lock records to ensure that only a single instance can run at a time. " +
					"•\tMonitor – A Boolean (true/false) value indicating whether this app should listen " +
					"for 'event clients.' The API can act as an event client. Event clients can connect to the " +
					"app via a socket and pull a list of events generated by the app. " +
					"This is typically used to provide a scrolling event window. " +
					"•\tEventPort – If set, this property determines the port that this app will listen on " +
					"for event clients. If not set (the usual case), the port is determined by the formula: " +
					"port = (pid % 10000) + 20000",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved application statistics",
						content = @Content(mediaType = MediaType.APPLICATION_JSON,
							array = @ArraySchema(schema = @Schema(implementation = ApiAppStatus.class)))),
					@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
			},
			tags = {"OpenDCS Process Monitor and Control (APP)"}
	)
	public Response getAppStat() throws DbException
	{
		LOGGER.debug("Getting app stats");
		try (DbInterface dbi = new DbInterface();
			ApiAppDAO dao = new ApiAppDAO(dbi))
		{
			return ApiHttpUtil.createResponse(dao.getAppStatus());
		}
	}

}
