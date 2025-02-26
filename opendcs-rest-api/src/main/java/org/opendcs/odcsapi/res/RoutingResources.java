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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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

import org.opendcs.odcsapi.beans.ApiRouting;
import org.opendcs.odcsapi.beans.ApiScheduleEntry;
import org.opendcs.odcsapi.dao.ApiRoutingDAO;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.util.ApiConstants;
import org.opendcs.odcsapi.util.ApiHttpUtil;

@Path("/")
public final class RoutingResources
{
	@Context private HttpServletRequest request;
	@Context private HttpHeaders httpHeaders;

	@GET
	@Path("routingrefs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	@Operation(
			summary = "Get Routing References",
			description = "Retrieves a list of all routing references.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved routing references"),
					@ApiResponse(responseCode = "500", description = "Database error occurred", content = @Content)
			}
	)
	public Response getRoutingRefs() throws DbException
	{
		Logger.getLogger(ApiConstants.loggerName).fine("getRoutingRefs");
		try (DbInterface dbi = new DbInterface();
			ApiRoutingDAO dao = new ApiRoutingDAO(dbi))
		{
			return ApiHttpUtil.createResponse(dao.getRoutingRefs());
		}
	}

	@GET
	@Path("routing")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	@Operation(
			summary = "Get Routing",
			description = "Fetches a specific routing object based on the provided routing ID.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved routing object"),
					@ApiResponse(responseCode = "400", description = "Missing or invalid routing ID parameter", content = @Content),
					@ApiResponse(responseCode = "500", description = "Database error occurred", content = @Content)
			}
	)
	public Response getRouting(@QueryParam("routingid") Long routingId)
			throws WebAppException, DbException, SQLException
	{
		
		if (routingId == null)
			throw new WebAppException(ErrorCodes.MISSING_ID, 
				"Missing required routingid parameter.");
		
		Logger.getLogger(ApiConstants.loggerName).fine("getRouting id=" + routingId);
		try (DbInterface dbi = new DbInterface();
			ApiRoutingDAO dao = new ApiRoutingDAO(dbi))
		{
			return ApiHttpUtil.createResponse(dao.getRouting(routingId));
		}
	}

	@POST
	@Path("routing")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	@Operation(
			summary = "Post Routing",
			description = "Creates or updates a routing using the provided routing object.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully created or updated the routing"),
					@ApiResponse(responseCode = "400", description = "Invalid routing data provided", content = @Content),
					@ApiResponse(responseCode = "500", description = "Database error occurred", content = @Content)
			}
	)
	public Response postRouting(ApiRouting routing)
		throws WebAppException, DbException, SQLException
	{
		Logger.getLogger(ApiConstants.loggerName).fine("post routing received routing " + routing.getName() 
			+ " with ID=" + routing.getRoutingId());
		
		try (DbInterface dbi = new DbInterface();
				ApiRoutingDAO dao = new ApiRoutingDAO(dbi))
		{
			dao.writeRouting(routing);
			return ApiHttpUtil.createResponse(routing);
		}
	}

	@DELETE
	@Path("routing")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(
			summary = "Delete Routing",
			description = "Deletes a specific routing based on the provided routing ID.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully deleted routing"),
					@ApiResponse(responseCode = "400", description = "Invalid routing ID provided", content = @Content),
					@ApiResponse(responseCode = "500", description = "Database error occurred", content = @Content)
			}
	)
	public Response deleteRouting(@QueryParam("routingid") Long routingId)
		throws WebAppException, DbException, SQLException
	{
		Logger.getLogger(ApiConstants.loggerName)
				.fine("DELETE routing received routingId=" + routingId);
		
		// Use username and password to attempt to connect to the database
		try (DbInterface dbi = new DbInterface();
			ApiRoutingDAO dao = new ApiRoutingDAO(dbi))
		{
			dao.deleteRouting(routingId);
			return ApiHttpUtil.createResponse("RoutingSpec with ID " + routingId + " deleted");
		}
	}

	@GET
	@Path("schedulerefs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	@Operation(
			summary = "Get Schedule References",
			description = "Retrieves a list of all schedule references.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved schedule references"),
					@ApiResponse(responseCode = "500", description = "Database error occurred", content = @Content)
			}
	)
	public Response getScheduleRefs()
		throws DbException
	{
		Logger.getLogger(ApiConstants.loggerName).fine("getScheduleRefs");
		try (DbInterface dbi = new DbInterface();
			ApiRoutingDAO dao = new ApiRoutingDAO(dbi))
		{
			return ApiHttpUtil.createResponse(dao.getScheduleRefs());
		}
	}

	@GET
	@Path("schedule")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	@Operation(
			summary = "Get Schedule",
			description = "Fetches a specific schedule object based on the provided schedule ID.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved schedule object"),
					@ApiResponse(responseCode = "400", description = "Missing or invalid schedule ID parameter", content = @Content),
					@ApiResponse(responseCode = "500", description = "Database error occurred", content = @Content)
			}
	)
	public Response getSchedule(@QueryParam("scheduleid") Long scheduleId)
		throws WebAppException, DbException, SQLException
	{
		if (scheduleId == null)
			throw new WebAppException(ErrorCodes.MISSING_ID, 
				"Missing required scheduleid parameter.");
		
		Logger.getLogger(ApiConstants.loggerName).fine("getSchedule id=" + scheduleId);
		try (DbInterface dbi = new DbInterface();
				ApiRoutingDAO dao = new ApiRoutingDAO(dbi))
		{
			return ApiHttpUtil.createResponse(dao.getSchedule(scheduleId));
		}
	}

	@POST
	@Path("schedule")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	@Operation(
			summary = "Post Schedule",
			description = "Creates or updates a schedule using the provided schedule object.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully created or updated the schedule"),
					@ApiResponse(responseCode = "400", description = "Invalid schedule data provided", content = @Content),
					@ApiResponse(responseCode = "500", description = "Database error occurred", content = @Content)
			}
	)
	public Response postSchedule(ApiScheduleEntry schedule)
		throws WebAppException, DbException, SQLException
	{
		Logger.getLogger(ApiConstants.loggerName).fine("post schedule received sched " + schedule.getName() 
			+ " with ID=" + schedule.getSchedEntryId());
		
		try (DbInterface dbi = new DbInterface();
				ApiRoutingDAO dao = new ApiRoutingDAO(dbi))
		{
			dao.writeSchedule(schedule);
			return ApiHttpUtil.createResponse(schedule);
		}
	}

	@DELETE
	@Path("schedule")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	@Operation(
			summary = "Delete Schedule",
			description = "Deletes a specific schedule based on the provided schedule ID.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully deleted schedule"),
					@ApiResponse(responseCode = "400", description = "Invalid schedule ID provided", content = @Content),
					@ApiResponse(responseCode = "500", description = "Database error occurred", content = @Content)
			}
	)
	public Response deleteSchedule(@QueryParam("scheduleid") Long scheduleId)
		throws WebAppException, DbException
	{
		Logger.getLogger(ApiConstants.loggerName)
				.fine("DELETE schedule received scheduleId=" + scheduleId);
		
		// Use username and password to attempt to connect to the database
		try (DbInterface dbi = new DbInterface();
				ApiRoutingDAO dao = new ApiRoutingDAO(dbi))
		{
			dao.deleteSchedule(scheduleId);
			return ApiHttpUtil.createResponse("schedulec with ID " + scheduleId + " deleted");
		}
	}


	@GET
	@Path("routingstatus")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	@Operation(
			summary = "Get Routing Status",
			description = "Retrieves the status of various routings.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved routing statistics"),
					@ApiResponse(responseCode = "500", description = "Database error occurred", content = @Content)
			}
	)
	public Response getRoutingStats()
		throws WebAppException, DbException
	{
		Logger.getLogger(ApiConstants.loggerName).fine("getRoutingStats");
		try (DbInterface dbi = new DbInterface();
			ApiRoutingDAO dao = new ApiRoutingDAO(dbi))
		{
			return ApiHttpUtil.createResponse(dao.getRsStatus());
		}
	}

	@GET
	@Path("routingexecstatus")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	@Operation(
			summary = "Get Routing Execution Status",
			description = "Fetches the execution status of a specific routing based on the provided schedule entry ID.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved routing execution status"),
					@ApiResponse(responseCode = "400", description = "Missing or invalid schedule entry ID parameter", content = @Content),
					@ApiResponse(responseCode = "500", description = "Database error occurred", content = @Content)
			}
	)
	public Response getRoutingExecStatus(@QueryParam("scheduleentryid") Long scheduleEntryId)
		throws WebAppException, DbException
	{
		if (scheduleEntryId == null)
			throw new WebAppException(ErrorCodes.MISSING_ID, "missing required scheduleentryid argument.");
		
		Logger.getLogger(ApiConstants.loggerName).fine("getRoutingExecStatus");
		try (DbInterface dbi = new DbInterface();
			ApiRoutingDAO dao = new ApiRoutingDAO(dbi))
		{
			return ApiHttpUtil.createResponse(dao.getRoutingExecStatus(scheduleEntryId));
		}
	}

	@GET
	@Path("dacqevents")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	@Operation(
			summary = "Get Data Acquisition Events",
			description = "Retrieves data acquisition events based on various filter criteria, such as app ID, routing execution ID, and platform ID.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved data acquisition events"),
					@ApiResponse(responseCode = "400", description = "Invalid input parameters", content = @Content),
					@ApiResponse(responseCode = "500", description = "Database error occurred", content = @Content)
			}
	)
	public Response getDacqEvents(@QueryParam("appid") Long appId, @QueryParam("routingexecid") Long routingExecId,
		@QueryParam("platformid") Long platformId, @QueryParam("backlog") String backlog)
		throws DbException
	{
		Logger.getLogger(ApiConstants.loggerName).fine("getDacqEvents");
		try (DbInterface dbi = new DbInterface();
			ApiRoutingDAO dao = new ApiRoutingDAO(dbi))
		{
			HttpSession session = request.getSession(true);
			return ApiHttpUtil.createResponse(dao.getDacqEvents(appId, routingExecId, platformId, backlog, session));
		}
	}
}
