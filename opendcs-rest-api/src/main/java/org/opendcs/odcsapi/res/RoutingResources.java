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
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.Schema;

import org.opendcs.odcsapi.beans.ApiDacqEvent;
import org.opendcs.odcsapi.beans.ApiRouting;
import org.opendcs.odcsapi.beans.ApiRoutingExecStatus;
import org.opendcs.odcsapi.beans.ApiRoutingRef;
import org.opendcs.odcsapi.beans.ApiRoutingStatus;
import org.opendcs.odcsapi.beans.ApiScheduleEntry;
import org.opendcs.odcsapi.beans.ApiScheduleEntryRef;
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
			tags = {"REST - DECODES Routing Spec Records"},
			responses = {
					@ApiResponse(responseCode = "200", description = "Success",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
									array = @ArraySchema(schema = @Schema(implementation = ApiRoutingRef.class)))),
					@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
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
			summary = "This method returns a JSON representation of a single routing spec",
			description = "This method returns a JSON representation of a single routing spec. " +
					"Example: \n\n    http://localhost:8080/odcsapi/routing?routingid=20",
			tags = {"REST - DECODES Routing Spec Records"},
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved routing spec",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = ApiRouting.class))),
					@ApiResponse(responseCode = "400", description = "Missing or invalid routing ID parameter", content = @Content),
					@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
			}
	)
	public Response getRouting(@Parameter(description = "routing spec id", required = true, example = "20",
			schema = @Schema(implementation = Long.class))
		@QueryParam("routingid") Long routingId)
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
			summary = "Create or Overwrite Existing Routing Spec",
			description = "It takes a single DECODES Routing Spec in JSON format. " +
					"For creating a new record, leave routingId out of the passed structure. " +
					"For overwriting, include the routingId.",
			tags = {"REST - DECODES Routing Spec Records"},
			requestBody = @RequestBody(
					description = "Decodes Routing Spec Object",
					required = true,
					content = @Content(mediaType = MediaType.APPLICATION_JSON,
							schema = @Schema(implementation = ApiRouting.class))
			),
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully created or updated the routing",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = ApiRouting.class))),
					@ApiResponse(responseCode = "400", description = "Invalid routing data provided", content = @Content),
					@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
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
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	@Operation(
			summary = "Delete Existing Decodes Routing Spec",
			description = "Required argument routingid must be passed in the URL.",
			tags = {"REST - DECODES Routing Spec Records"},
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully deleted routing"),
					@ApiResponse(responseCode = "400", description = "Invalid routing ID provided", content = @Content),
					@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
			}
	)
	public Response deleteRouting(@Parameter(description = "routing spec id", required = true, example = "20",
			schema = @Schema(implementation = Long.class))
		@QueryParam("routingid") Long routingId)
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
			summary = "Retrieve all schedule references",
			description = "Example:\n\n    http://localhost:8080/odcsapi/schedulerefs\n\n" +
					"The returned structure is:\n```\n[\n  {\n    \"appName\": \"RoutingScheduler\",\n    " +
					"\"enabled\": false,\n    \"lastModified\": \"2020-12-15T17:52:13.934Z[UTC]\",\n    " +
					"\"name\": \"goes1\",\n    \"routingSpecName\": \"goes1\",\n    " +
					"\"schedEntryId\": 9\n  },\n  ...\n]\n```",
			tags = {"REST - Schedule Entry Methods"},
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "success",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
									array = @ArraySchema(schema = @Schema(implementation = ApiScheduleEntryRef.class)))
					),
					@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
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
			summary = "This method returns a JSON representation of a single schedule entry",
			description = "Fetches a specific schedule object based on the provided schedule ID.\n\n" +
					"Example: \n\n    http://localhost:8080/odcsapi/schedule?scheduleid=21",
			tags = {"REST - Schedule Entry Methods"},
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Success",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = ApiScheduleEntry.class))
					),
					@ApiResponse(responseCode = "400", description = "Missing or invalid schedule ID parameter", content = @Content),
					@ApiResponse(responseCode = "404", description = "Requested schedule entry not found", content = @Content),
					@ApiResponse(responseCode = "500", description = "Default error sample response", content = @Content)
			}
	)
	public Response getSchedule(@Parameter(description = "Schedule ID", required = true,
			schema = @Schema(implementation = Long.class, example = "21"))
		@QueryParam("scheduleid") Long scheduleId)
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
			summary = "Create or Overwrite Existing Schedule",
			description = "It takes a single DECODES Schedule Entry " +
					"in JSON format, as described above for GET.\n\n" +
					"For creating a new record, leave schedEntryId out of the passed data structure.\n\n" +
					"For overwriting an existing one, provide the schedEntryId that was previously returned.",
			tags = {"REST - Schedule Entry Methods"},
			requestBody = @RequestBody(
					description = "Schedule Object",
					required = true,
					content = @Content(mediaType = MediaType.APPLICATION_JSON,
							schema = @Schema(implementation = ApiScheduleEntry.class)
					)
			),
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully created or updated the schedule",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = ApiScheduleEntry.class))),
					@ApiResponse(responseCode = "400", description = "Invalid schedule data provided", content = @Content),
					@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
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
			summary = "Delete Existing Schedule",
			description = "Required argument scheduleid must be passed in the URL.",
			tags = {"REST - Schedule Entry Methods"},
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully deleted schedule"),
					@ApiResponse(responseCode = "400", description = "Invalid schedule ID provided", content = @Content),
					@ApiResponse(responseCode = "500", description = "Default error sample response", content = @Content)
			}
	)
	public Response deleteSchedule(@Parameter(description = "Schedule ID", required = true,
			schema = @Schema(implementation = Long.class, example = "21"))
		@QueryParam("scheduleid") Long scheduleId)
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
			summary =  "This method allows a developer to implement a web version of the OpenDCS Routing Monitor screen.",
			description = "Sample URL:\n  \n    http://localhost:8080/odcsapi/routingstatus\n  \n" +
					"The returned data structure is shown below. Note the following:\n  \n" +
					"* All routing specs are contained in the list regardless of whether they have a " +
					"schedule entry assigned. No schedule entry is indicated by scheduleEntryId = null.\n  \n" +
					"* Routing specs with a suffix of '-manual' and the 'manual' attribute set to true indicate " +
					"that the routing spec was run throught the 'rs' command. Otherwise they were run by a " +
					"Routing Scheduler from a schedule entry.\n  \n* A routing spec may be run either way. " +
					"Note that the entry for 'test' and 'test-manual' are the same routing spec. " +
					"'test' was run from the routing scheduler with scheduleEntryId=43, and " +
					"'test-manual' was run from the command line 'rs'.\n  \n```\n  [\n    {\n      " +
					"\"routingSpecId\": 44,\n      \"name\": \"rs-MROI4-ROWI4\",\n      " +
					"\"scheduleEntryId\": null,\n      \"appId\": null,\n      \"appName\": null,\n      " +
					"\"runInterval\": null,\n      \"lastActivity\": null,\n      \"lastMsgTime\": null,\n      " +
					"\"numMessages\": 0,\n      \"numErrors\": 0,\n      \"enabled\": false,\n      " +
					"\"manual\": false\n    },\n    {\n      \"routingSpecId\": 58,\n      \"name\": \"test\",\n      " +
					"\"scheduleEntryId\": 43,\n      \"appId\": 26,\n      \"appName\": \"RoutingScheduler\",\n      " +
					"\"runInterval\": \"5 minute\",\n      \"lastActivity\": \"2023-05-31T18:56:54.364Z[UTC]\",\n      " +
					"\"lastMsgTime\": \"2023-05-31T18:56:53.099Z[UTC]\",\n      \"numMessages\": 3362,\n      " +
					"\"numErrors\": 3362,\n      \"enabled\": true,\n      \"manual\": false\n    },\n    {\n      " +
					"\"routingSpecId\": 58,\n      \"name\": \"test-manual\",\n      \"scheduleEntryId\": 40,\n      " +
					"\"appId\": 0,\n      \"appName\": null,\n      \"runInterval\": null,\n      " +
					"\"lastActivity\": \"2023-05-31T18:37:02.490Z[UTC]\",\n      " +
					"\"lastMsgTime\": \"2023-05-31T18:37:02.458Z[UTC]\",\n      \"numMessages\": 5700,\n      " +
					"\"numErrors\": 5699,\n      \"enabled\": true,\n      \"manual\": true\n    },\n    {\n      " +
					"\"routingSpecId\": 59,\n      \"name\": \"goes1-manual\",\n      \"scheduleEntryId\": 39,\n      " +
					"\"appId\": 0,\n      \"appName\": null,\n      \"runInterval\": null,\n      " +
					"\"lastActivity\": \"2022-12-01T22:19:06.024Z[UTC]\",\n      " +
					"\"lastMsgTime\": \"2022-12-01T22:19:05.939Z[UTC]\",\n      \"numMessages\": 9,\n      " +
					"\"numErrors\": 0,\n      \"enabled\": true,\n      \"manual\": true\n    },\n    {\n      " +
					"\"routingSpecId\": 63,\n      \"name\": \"periodic-10-min\",\n      " +
					"\"scheduleEntryId\": 38,\n      \"appId\": 26,\n      \"appName\": \"RoutingScheduler\",\n      " +
					"\"runInterval\": \"10 minute\",\n      \"lastActivity\": \"2023-05-22T13:28:17.631Z[UTC]\",\n      " +
					"\"lastMsgTime\": \"2023-05-22T13:28:12.825Z[UTC]\",\n      \"numMessages\": 8,\n      " +
					"\"numErrors\": 0,\n      \"enabled\": true,\n      \"manual\": false\n    },\n    {\n      " +
					"\"routingSpecId\": 63,\n      \"name\": \"periodic-10-minute-manual\",\n      " +
					"\"scheduleEntryId\": 37,\n      \"appId\": 0,\n      \"appName\": null,\n      " +
					"\"runInterval\": null,\n      \"lastActivity\": \"2022-12-01T20:37:50.811Z[UTC]\",\n      " +
					"\"lastMsgTime\": \"2022-12-01T20:37:50.710Z[UTC]\",\n      \"numMessages\": 6,\n      " +
					"\"numErrors\": 0,\n      \"enabled\": true,\n      \"manual\": true\n    },\n    {\n      " +
					"\"routingSpecId\": 65,\n      \"name\": \"last-5-min\",\n      \"scheduleEntryId\": null,\n      " +
					"\"appId\": null,\n      \"appName\": null,\n      \"runInterval\": null,\n      " +
					"\"lastActivity\": null,\n      \"lastMsgTime\": null,\n      \"numMessages\": 0,\n      " +
					"\"numErrors\": 0,\n      \"enabled\": false,\n      \"manual\": false\n    },\n    {\n      " +
					"\"routingSpecId\": 65,\n      \"name\": \"last-5-min-manual\",\n      " +
					"\"scheduleEntryId\": 42,\n      \"appId\": 0,\n      \"appName\": null,\n      " +
					"\"runInterval\": null,\n      \"lastActivity\": \"2023-05-31T18:49:49.453Z[UTC]\",\n      " +
					"\"lastMsgTime\": \"2023-05-31T18:49:47.902Z[UTC]\",\n      \"numMessages\": 3179,\n      " +
					"\"numErrors\": 0,\n      \"enabled\": true,\n      \"manual\": true\n    }\n  ]\n\n```",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved routing statistics",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
									array = @ArraySchema(schema = @Schema(implementation = ApiRoutingStatus.class)))
					),
					@ApiResponse(responseCode = "500", description = "Database error occurred", content = @Content)
			},
			tags = {"OpenDCS Process Monitor and Control (Routing)"}
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
			summary = "It returns all of the executions for the specified schedule entry",
			description = "Sample URL\n  \n      http://localhost:8080/odcsapi/routingexecstatus?scheduleentryid=38\n  \n" +
					"Note the 'GET routingstatus' method returns a list of routing specs showing a unique " +
					"scheduleEntryId for each entry. There may be more than one entry for each routing spec because:\n    \n" +
					"* The same routing spec may be run in multiple schedule entries.\n  \n* A 'manual' routing spec " +
					"(i.e. run with the 'rs' command) will appear as a separate schedule entry with the 'manual' " +
					"Boolean set to true.\n  \nThus this method, GET routingexecstatus, takes a scheduleentryid " +
					"as its argument. It returns all of the executions for the specified schedule entry. " +
					"The returned data structure appears as follows:\n  \n```\n  [\n    {\n      " +
					"\"routingExecId\": 568,\n      \"scheduleEntryId\": 38,\n      \"routingSpecId\": 63,\n      " +
					"\"runStart\": \"2023-06-01T17:20:00.516Z[UTC]\",\n      " +
					"\"runStop\": \"2023-06-01T17:20:00.526Z[UTC]\",\n      \"numMessages\": 0,\n      " +
					"\"numErrors\": 0,\n      \"numPlatforms\": 0,\n      \"lastMsgTime\": null,\n      " +
					"\"lastActivity\": \"2023-06-01T17:20:00.527Z[UTC]\",\n      \"runStatus\": \"ERR-OutputInit\",\n      " +
					"\"hostname\": \"mmaloney3.local\",\n      \"lastInput\": null,\n      \"lastOutput\": null\n    " +
					"},\n    {\n      \"routingExecId\": 565,\n      \"scheduleEntryId\": 38,\n      " +
					"\"routingSpecId\": 63,\n      \"runStart\": \"2023-06-01T17:10:00.841Z[UTC]\",\n      " +
					"\"runStop\": \"2023-06-01T17:10:00.855Z[UTC]\",\n      \"numMessages\": 0,\n      " +
					"\"numErrors\": 0,\n      \"numPlatforms\": 0,\n      \"lastMsgTime\": null,\n      " +
					"\"lastActivity\": \"2023-06-01T17:10:00.855Z[UTC]\",\n      " +
					"\"runStatus\": \"ERR-OutputInit\",\n      \"hostname\": \"mmaloney3.local\",\n      " +
					"\"lastInput\": null,\n      \"lastOutput\": null\n    },\n    {\n      " +
					"\"routingExecId\": 562,\n      \"scheduleEntryId\": 38,\n      \"routingSpecId\": 63,\n      " +
					"\"runStart\": \"2023-06-01T17:00:00.259Z[UTC]\",\n      " +
					"\"runStop\": \"2023-06-01T17:00:00.269Z[UTC]\",\n      \"numMessages\": 0,\n      " +
					"\"numErrors\": 0,\n      \"numPlatforms\": 0,\n      \"lastMsgTime\": null,\n      " +
					"\"lastActivity\": \"2023-06-01T17:00:00.270Z[UTC]\",\n      " +
					"\"runStatus\": \"ERR-OutputInit\",\n      \"hostname\": \"mmaloney3.local\",\n      " +
					"\"lastInput\": null,\n      \"lastOutput\": null\n    }\n  ]\n```\n  \n" +
					"The entries are sorted in descending order by the runStart time. " +
					"'runStop' may be null if the execution was halted abnormally or if it is still running. " +
					"If any messages were processed, the num Messages/Errors/Platforms will be non-zero.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved routing execution status",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
									array = @ArraySchema(schema = @Schema(implementation = ApiRoutingExecStatus.class)))
					),
					@ApiResponse(responseCode = "400", description = "Missing or invalid schedule entry ID parameter", content = @Content),
					@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
			},
			tags = {"OpenDCS Process Monitor and Control (Routing)"}
	)
	public Response getRoutingExecStatus(@Parameter(description = "Schedule entry identifier", required = true,
			schema = @Schema(implementation = Long.class), example = "38")
		@QueryParam("scheduleentryid") Long scheduleEntryId)
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
			summary = "Returns data acquisition events stored in the DACQ_EVENT database table",
			description = "Sample URL:\n  \n    " +
					"http://localhost:8080/odcsapi/dacqevents?appid=26\n  \n" +
					"The 'GET dacqevents' method returns events stored in the DACQ_EVENT database table. " +
					"These are events having to do with data acquisition (DACQ) events. " +
					"That can be associated with …\n  \n" +
					"* An execution of a routing spec (pass argument routingexecid)\n  \n" +
					"* An application (pass argument appid)\n  \n" +
					"*  A specific platform (pass argument platformid)\n  \n" +
					"It may contain any of the following additional argument. " +
					"Each argument refines a filter that determines which events are to be returned:\n  \n" +
					"*  **appid** (*long integer*): only return events generated by a specific app.\n  \n" +
					"*  **routingexecid** (*long integer*): only return events generated during a specific " +
					"execution of a routing spec. (The 'GET routingexecstatus' method will return a list of " +
					"executions, each with a unique ID.)\n  \n*  **platformid** (*long integer*): only return " +
					"events generated during the processing of a specific platform.\n  \n" +
					"*  **backlog** (*string*): either the word 'last' or one of the valid interval names " +
					"returned in GET intervals (see section 3.4.1). Only events generated since the specified " +
					"interval are returned. The word 'last' means only return events generated since the last " +
					"'GET dacqevents' call within this session. It can be used to approximate a real-time stream. \n  \n" +
					"The returned data looks like this:\n  \n```\n  [\n    {\n      \"eventId\": 181646,\n      " +
					"\"routingExecId\": 607,\n      \"platformId\": null,\n      " +
					"\"eventTime\": \"2023-06-08T19:21:15.255Z[UTC]\",\n      " +
					"\"priority\": \"INFO\",\n      \"appId\": 26,\n      \"appName\": \"RoutingScheduler\",\n      " +
					"\"subsystem\": null,\n      \"msgRecvTime\": null,\n      " +
					"\"eventText\": \"RoutingSpec(test) Connected to DDS server at www.covesw.com:-1, " +
					"username='covetest'\"\n    },\n    {\n      \"eventId\": 181647,\n      " +
					"\"routingExecId\": 606,\n      \"platformId\": null,\n      " +
					"\"eventTime\": \"2023-06-08T19:21:15.281Z[UTC]\",\n      \"priority\": \"INFO\",\n      " +
					"\"appId\": 26,\n      \"appName\": \"RoutingScheduler\",\n      \"subsystem\": null,\n      " +
					"\"msgRecvTime\": null,\n      \"eventText\": \"RoutingSpec(periodic-10-minute) " +
					"Connected to DDS server at www.covesw.com:-1, username='covetest'\"\n    },\n    " +
					"{\n      \"eventId\": 181648,\n      \"routingExecId\": 607,\n      \"platformId\": null,\n      " +
					"\"eventTime\": \"2023-06-08T19:21:15.284Z[UTC]\",\n      \"priority\": \"INFO\",\n      " +
					"\"appId\": 26,\n      \"appName\": \"RoutingScheduler\",\n      \"subsystem\": null,\n      " +
					"\"msgRecvTime\": null,\n      \"eventText\": \"RoutingSpec(test) Purging old DACQ_EVENTs " +
					"before Sat Jun 03 15:21:15 EDT 2023\"\n    }\n  ]\n\n```",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved data acquisition events",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
									array = @ArraySchema(schema = @Schema(implementation = ApiDacqEvent.class)))),
					@ApiResponse(responseCode = "400", description = "Invalid input parameters", content = @Content),
					@ApiResponse(responseCode = "500", description = "Database error occurred", content = @Content)
			},
			tags = {"OpenDCS Process Monitor and Control (Routing)"}
	)
	public Response getDacqEvents(@Parameter(description = "Only return events generated by a specific app.", example = "26",
			schema = @Schema(implementation = Long.class))
		@QueryParam("appid") Long appId,
			@Parameter(description = "Only return events generated during a specific execution of a routing spec. " +
					"(The 'GET routingexecstatus' method will return a list of executions, each with a unique ID.)",
					example = "64", schema = @Schema(implementation = Long.class))
		@QueryParam("routingexecid") Long routingExecId,
			@Parameter(description = "Only return events generated during the processing of a specific platform.",
					example = "45", schema = @Schema(implementation = Long.class))
		@QueryParam("platformid") Long platformId,
			@Parameter(description = "Either the word 'last' or one of the valid interval names returned in " +
					"GET intervals (see section 3.4.1). Only events generated since the specified interval " +
					"are returned. The word 'last' means only return events generated since the last " +
					"'GET dacqevents' call within this session. It can be used to approximate a real-time stream.",
					example = "9fe6390676c7dca9", schema = @Schema(implementation = String.class))
		@QueryParam("backlog") String backlog)
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
