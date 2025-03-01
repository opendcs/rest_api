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

import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ilex.util.IDateFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.opendcs.odcsapi.beans.ApiInterval;
import org.opendcs.odcsapi.beans.ApiTimeSeriesData;
import org.opendcs.odcsapi.beans.ApiTimeSeriesIdentifier;
import org.opendcs.odcsapi.beans.ApiTimeSeriesSpec;
import org.opendcs.odcsapi.beans.ApiTsGroup;
import org.opendcs.odcsapi.beans.ApiTsGroupRef;
import org.opendcs.odcsapi.dao.ApiRefListDAO;
import org.opendcs.odcsapi.dao.ApiTsDAO;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.util.ApiConstants;
import org.opendcs.odcsapi.util.ApiHttpUtil;

/**
 * HTTP resources relating to Time Series data and descriptors
 * @author mmaloney
 *
 */
@Path("/")
public final class TimeSeriesResources
{
	@Context HttpHeaders httpHeaders;

	@GET
	@Path("tsrefs")
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Time Series Methods")
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	@Operation(
			summary = "The tsrefs method returns a list of time series defined in the database.",
			description = "You have the option to filter out inactive time series by passing the 'active=true' query parameter.\n\n"
					+ "Examples: \n - http://localhost:8080/odcsapi/tsrefs\n - http://localhost:8080/odcsapi/tsrefs?active=true \n\n"
					+ "This returns an array of Time Series Identifiers. The Key of a time series identifier may be used "
					+ "in subsequent calls to get the complete specification for the time series (GET tsspec) or to retrieve "
					+ "time series data (GET tsdata).",
			responses = {
					@ApiResponse(responseCode = "200", description = "Success",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
								array = @ArraySchema(schema = @Schema(type = "array",
										implementation = ApiTimeSeriesIdentifier.class)))),
					@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
			},
			tags = {"Time Series Methods"}
	)
	public Response getTimeSeriesRefs(@Parameter(description = "Include only active time series", required = true,
			schema = @Schema(implementation = Boolean.class, example = "true"))
		@QueryParam("active") Boolean activeOnly)
			throws DbException
	{
		Logger.getLogger(ApiConstants.loggerName).fine("getTimeSeriesRefs");
		try (DbInterface dbi = new DbInterface();
			ApiTsDAO dao = new ApiTsDAO(dbi))
		{
			return ApiHttpUtil.createResponse(dao.getTsRefs(activeOnly != null && activeOnly));
		}
	}

	@GET
	@Path("tsspec")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	@Operation(
			summary = "The tsspec method returns a complete specification for a time series "
					+ "identified by the 'key' parameter.",
			description = "Example: \n\n    http://localhost:8080/odcsapi/tsspec?key=532",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved time series specification",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
								schema = @Schema(implementation = ApiTimeSeriesSpec.class))),
					@ApiResponse(responseCode = "400", description = "Missing or invalid key", content = @Content),
					@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
			},
			tags = {"Time Series Methods"}
	)
	public Response getTimeSeriesSpec(@Parameter(description = "Numeric key identifying the time series.",
			required = true, example = "532", schema = @Schema(implementation = Long.class))
		@QueryParam("key") Long tsKey)
			throws WebAppException, DbException
	{
		if (tsKey == null)
			throw new WebAppException(ErrorCodes.MISSING_ID, 
				"Missing required tskey parameter.");
		
		Logger.getLogger(ApiConstants.loggerName).fine("getTimeSeriesSpec");
		try (DbInterface dbi = new DbInterface();
			ApiTsDAO dao = new ApiTsDAO(dbi))
		{
			return ApiHttpUtil.createResponse(dao.getSpec(tsKey));
		}
	}

	@GET
	@Path("tsdata")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	@Operation(
			summary = "The tsdata method returns data for a time series over a specified time range.",
			description = "The method takes 3 arguments:\n" +
					"* **tkey (required)** – the numeric key identifying the time series. " +
					"It is contained within a Time Series Identifier described above.\n" +
					"* **tstart** – Optionally specifies the start of the time range for retrieval. " +
					"If omitted, the oldest data in the database is returned. See below for time format.\n" +
					"* **tend** – Optionally specifies the end of the time range for retrieval. " +
					"If omitted, the newest data in the database is returned. See below for time format.  \n\n" +
					"The since and until arguments may have any of the following formats:\n" +
					"*\t**now-1day**\tThe word 'now' minus an increment times a unit. " +
					"Examples: now-1day, now-5hours, now-1week, etc.\n" +
					"*\t**now**\tThe current time that the web service call was made.\n" +
					"*\t**YYYY/DDD/HH:MM:SS**\tA complete Julian Year, Day-of-Year, and Time\n" +
					"*\t**YYYY/DDD/HH:MM**\tSeconds omitted means zero.\n" +
					"*\t**DDD/HH:MM:SS**\tAssume current year\n*\t**DDD/HH:MM**\t\n" +
					"*\t**HH:MM:SS**\tAssume current day\n*\t**HH:MM**  \n\n" +
					"Examples:  \n```http://localhost:8080/odcsapi/tsdata?key=12```",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved time series data",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = ApiTimeSeriesData.class))),
					@ApiResponse(responseCode = "400", description = "Invalid input parameters", content = @Content),
					@ApiResponse(responseCode = "500", description = "Database error occurred", content = @Content)
			},
			tags = {"Time Series Methods"}
	)
	public Response getTimeSeriesData(@Parameter(description = "Timeseries key", required = true,
				schema = @Schema(implementation = Long.class), example = "532")
		@QueryParam("key") Long tsKey,
			@Parameter(description = "Start time of the time range", schema = @Schema(implementation = String.class))
		@QueryParam("start") String start,
			@Parameter(description = "End time of the time range", schema = @Schema(implementation = String.class))
		@QueryParam("end") String end)
			throws WebAppException, DbException
	{
		Logger.getLogger(ApiConstants.loggerName).fine("getTimeSeriesData key=" + tsKey 
			+ ", start=" + start + ", end=" + end);
		
		if (tsKey == null)
			throw new WebAppException(ErrorCodes.MISSING_ID, 
				"Missing required tskey parameter.");
		
		Date dStart = null, dEnd = null;
		if (start != null)
			try { dStart = IDateFormat.parse(start); }
			catch(IllegalArgumentException ex)
			{
				throw new WebAppException(ErrorCodes.MISSING_ID, 
					"Invalid start time. Use [[[CC]YY]/DDD]/HH:MM[:SS] or relative time.");
			}
		if (end != null)
		{
			try { dEnd = IDateFormat.parse(end); }
			catch(IllegalArgumentException ex)
			{
				throw new WebAppException(ErrorCodes.MISSING_ID, 
					"Invalid end time. Use [[[CC]YY]/DDD]/HH:MM[:SS] or relative time.");
			}

		}

		Logger.getLogger(ApiConstants.loggerName).fine("getTimeSeriesData start=" + dStart + ", end=" + dEnd);

		try (DbInterface dbi = new DbInterface();
			ApiTsDAO dao = new ApiTsDAO(dbi))
		{
			return ApiHttpUtil.createResponse(dao.getTsData(tsKey, dStart, dEnd));
		}
	}
	
	@GET
	@Path("intervals")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	@Tag(name = "Time Series Methods - Interval Methods", description = "Time Intervals are stored in the database "
			+ "for OpenTSDB. They are hardcoded for CWMS and HDB.")
	@Operation(
			summary = "Returns a list of time intervals defined in the database.",
			description = "Example: \n\n    http://localhost:8080/odcsapi/intervals\n\n" +
					"* The token argument is optional. If supplied it will reset the timer on the token.  \n\n" +
					"An array of data structures representing all known time intervals will be returned as shown below.\n" +
					"```\n[\n  {\n    \"intervalId\": 1,\n    \"name\": \"irregular\",\n    \"calConstant\": \"minute\"," +
					"\n    \"calMultilier\": 0\n  },\n  {\n    \"intervalId\": 2,\n    \"name\": \"2Minutes\"," +
					"\n    \"calConstant\": \"minute\",\n    \"calMultilier\": 2\n  },\n. . .\n]\n```\n\n" +
					"For each interval the system stores a numeric ID, a name, a Java Calendar Constant " +
					"(one of second, minute, hour, day, week, month, year), and a multiplier for the constant.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved intervals",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
									array = @ArraySchema(schema = @Schema(implementation = ApiInterval.class)))),
					@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
			},
			tags = {"Time Series Methods - Interval Methods"}
	)
	public Response getIntervals()
		throws DbException
	{
		Logger.getLogger(ApiConstants.loggerName).fine("getIntervals");
		try (DbInterface dbi = new DbInterface();
			ApiRefListDAO rlDAO = new ApiRefListDAO(dbi))
		{
			HashMap<Long,ApiInterval> imap = rlDAO.getIntervals();
			return ApiHttpUtil.createResponse(imap.values());
		}
	}

	@POST
	@Path("interval")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(
			summary = "Create a new, or update an existing Time Interval",
			description = "Example URL for POST:  \n\n    " +
					"http://localhost:8080/odcsapi/interval\n\n\n" +
					"The POST data should contain a single time interval record as described " +
					"above for the 'intervals' list.   \n\n" +
					"As with other POST methods, to create a new record, omit the numeric ID.  \n\n" +
					"To update an existing record, include the 'intervalId'.  \n\n" +
					"For example, to create a interval 'fortnight', the data could be:\n  " +
					"```\n  {\n    \"name\": \"fortnight\",\n    \"calConstant\": \"day\",\n    " +
					"\"calMultilier\": 14\n  }\n  ```\n\nThe returned data structure will be the " +
					"same as the data passed, except that if this is a new interval the " +
					"intervalId member will be added.",
			requestBody = @RequestBody(
					description = "Engineering Unit Conversion",
					required = true,
					content = @Content(mediaType = MediaType.APPLICATION_JSON,
							schema = @Schema(implementation = ApiInterval.class))),
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved time series data",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = ApiInterval.class))),
					@ApiResponse(responseCode = "400", description = "Invalid input parameters", content = @Content),
					@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
			},
			tags = {"Time Series Methods - Interval Methods"}
	)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	public Response postInterval(ApiInterval intv)
		throws DbException
	{
		Logger.getLogger(ApiConstants.loggerName).fine("postInterval");
		
		try (DbInterface dbi = new DbInterface();
			ApiRefListDAO rlDAO = new ApiRefListDAO(dbi))
		{
			rlDAO.writeInterval(intv);
			return ApiHttpUtil.createResponse(intv);
		}
	}

	@DELETE
	@Path("interval")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	@Operation(
			summary = "Delete an existing Time Interval record.",
			description = "Example URL for DELETE:  \n\n    "
					+ "http://localhost:8080/odcsapi/interval?intervalid=1459\n\n\n"
					+ "This deletes the Time Interval with ID 1459.  \n\n"
					+ "**Use care with this method**. The system needs to know about all of the 'interval' "
					+ "and 'duration' specifiers used for time series IDs.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully deleted the interval"),
					@ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content),
					@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
			},
			tags = {"Time Series Methods - Interval Methods"}
	)
	public Response deleteInterval(@Parameter(description = "ID of the interval to delete.", required = true,
				schema = @Schema(implementation = Long.class), example = "1459")
		@QueryParam("intvid") Long intvId)
			throws DbException
	{
		Logger.getLogger(ApiConstants.loggerName).fine("deleteInterval id=" + intvId);
		
		try (DbInterface dbi = new DbInterface();
			ApiRefListDAO rlDAO = new ApiRefListDAO(dbi))
		{
			rlDAO.deleteInterval(intvId);
			return ApiHttpUtil.createResponse("interval with ID=" + intvId + " deleted");

		}
	}

		@GET
		@Path("tsgrouprefs")
		@Produces(MediaType.APPLICATION_JSON)
		@RolesAllowed({ApiConstants.ODCS_API_GUEST})
		@Tag(name = "Time Series Methods - Groups", description = "Time Series Groups are used to define a "
				+ "set of time series identifiers")
		@Operation(
				summary = "Provide a list of all groups defined in the database.",
				description = "Time Series Groups are used to define a set of time series identifiers. "
						+ "Groups can contain:\n  \n*  Explicit list of time series identifiers  \n"
						+ "*  A list of attributes to flexibly define a set of time series identifiers, "
						+ "E.g. All time series at a particular with interval '30minutes'.  \n"
						+ "*  A list of sub-groups that can be included, excluded, "
						+ "or intersected with the group being defined.\n  \n"
						+ "***\n  \nExample URL:  \n\n    http://localhost:8080/odcsapi-0-7/tsgrouprefs\n\n"
						+ "A security token may be supplied in the header or in the URL, but it is not required. "
						+ "The returned list has the following structure:\n  \n```\n  [\n    {\n      "
						+ "\"groupId\": 8,\n      \"groupName\": \"topgroup\",\n      \"groupType\": \"basin\",\n      "
						+ "\"description\": \"\"\n    },\n    {\n      \"groupId\": 7,\n      "
						+ "\"groupName\": \"subgroup-x\",\n      \"groupType\": \"data type\",\n      "
						+ "\"description\": \"testing for OPENDCS-15 issue\"\n    },\n    {\n      "
						+ "\"groupId\": 2,\n      \"groupName\": \"regtest_017\",\n      "
						+ "\"groupType\": \"data-type\",\n      \"description\": \"Group for regression test 017\"\n    },"
						+ "\n    {\n      \"groupId\": 3,\n      \"groupName\": \"stageRate1Var\",\n      "
						+ "\"groupType\": \"basin\",\n      \"description\": \"Collection of TS IDs with stage "
						+ "to flow ratings\"\n    }\n  ]\n\n```\n\n  ",
				responses = {
						@ApiResponse(responseCode = "200", description = "Successfully retrieved time series group references",
								content = @Content(mediaType = MediaType.APPLICATION_JSON,
										array = @ArraySchema(schema = @Schema(implementation = ApiTsGroupRef.class)))),
						@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
				},
				tags = {"Time Series Methods - Groups"}
		)
		public Response getTsGroupRefs () throws DbException
		{
			Logger.getLogger(ApiConstants.loggerName).fine("getTsGroupRefs");
		try (DbInterface dbi = new DbInterface();
			ApiTsDAO dao = new ApiTsDAO(dbi))
		{
			return ApiHttpUtil.createResponse(dao.getTsGroupRefs());
		}
	}

		@GET
		@Path("tsgroup")
		@Produces(MediaType.APPLICATION_JSON)
		@RolesAllowed({ApiConstants.ODCS_API_GUEST})
		@Operation(
				summary = "Provide a complete definition of a single group.",
				description = "Example URL:  \n\n    http://localhost:8080/odcsapi-0-7/tsgroup?groupid=9\n\n"
						+
						"A security token may be supplied in the header or in the URL, but it is not required.  \n  \n  \n"
						+
						"The returned list has the following structure:  \n  \n```\n  {\n    \"groupId\": 9,\n    "
						+
						"\"groupName\": \"junk\",\n    \"groupType\": \"basin\",\n    \"description\": \"\",\n    "
						+
						"\"tsIds\": [\n      {\n        \"uniqueString\": \"OKVI4.Stage.Inst.15Minutes.0.raw\",\n        "
						+
						"\"key\": 1,\n        \"description\": null,\n        \"storageUnits\": \"ft\",\n        "
						+
						"\"active\": true\n      },\n      {\n        \"uniqueString\": \"OKVI4.Stage.Ave.1Day.1Day.CO\","
						+
						"\n        \"key\": 2,\n        \"description\": null,\n        \"storageUnits\": \"ft\",\n        "
						+
						"\"active\": true\n      }\n    ],\n    \"includeGroups\": [\n      {\n        \"groupId\": 1,"
						+
						"\n        \"groupName\": \"MROI4-ROWI4-HG\",\n        \"groupType\": \"basin\",\n        "
						+
						"\"description\": \"This is a group for the MROI4-ROWI4-HG Regression Test\"\n      }\n    ],"
						+
						"\n    \"excludeGroups\": [\n      {\n        \"groupId\": 2,\n        "
						+
						"\"groupName\": \"regtest_017\",\n        \"groupType\": \"data-type\",\n        "
						+
						"\"description\": \"Group for regression test 017\"\n      }\n    ],\n    "
						+
						"\"intersectGroups\": [\n      {\n        \"groupId\": 7,\n        "
						+
						"\"groupName\": \"subgroup-x\",\n        \"groupType\": \"data type\",\n        "
						+
						"\"description\": \"testing for OPENDCS-15 issue\"\n      }\n    ],\n    " +
						"\"groupAttrs\": [\n      \"BaseLocation=TESTSITE2\",\n      \"BaseParam=ELEV\",\n      " +
						"\"BaseVersion=DCP\",\n      \"Duration=0\",\n      \"Interval=1Hour\",\n      " +
						"\"ParamType=Inst\",\n      \"SubLocation=Spillway2-Gate1\",\n      \"SubParam=PZ1B\",\n      " +
						"\"SubVersion=Raw\",\n      \"Version=DCP-Raw\"\n    ],\n    \"groupSites\": [\n      " +
						"{\n        \"siteId\": 2,\n        \"sitenames\": {\n          " +
						"\"CWMS\": \"ROWI4\",\n          \"USGS\": \"05449500\"\n        },\n        " +
						"\"publicName\": \"IOWA RIVER NEAR ROWAN\",\n        " +
						"\"description\": \"IOWA RIVER NEAR ROWAN 4NW\"\n      }\n    ],\n    " +
						"\"groupDataTypes\": [\n      {\n        \"id\": 224,\n        " +
						"\"standard\": \"CWMS\",\n        \"code\": \"ELEV-PZ2A\",\n        " +
						"\"displayName\": \"CWMS:ELEV-PZ2A\"\n      }\n    ]\n  }\n\n```\n  \n" +
						"**Notes**:  \n*  **tsIds** is a list of explicit time series identifiers " +
						"that are considered part of the group.  \n*  **includedGroups** is a list of " +
						"subgroups to be included in this group.  \n*  **excludedGroups** is a list of subgroups. " +
						"The TSIDs in the subgroup will be excluded from this group.  \n" +
						"*  **intersectedGroups** is a list of subgroups to be intersected with this group. " +
						"Only TSIDs in both groups are considered part of this group.  \n" +
						"*  **groupSites** is a list of Site records. TSIDs in these Sites are " +
						"considered members of this group.  \n*  **groupDataTypes** is a list of fully-specified " +
						"data types (a.k.a. 'Param' in CWMS and OpenTSDB databases). TSIDs with a matching data " +
						"type will be included in the group.  \n*  **groupAttrs** is a list of attributes " +
						"that are used to define the group. These are presented in 'name=value' pairs where " +
						"the name is one of the following:  \n\n    *  **BaseLocation** – only the first " +
						"part of Site (Location) before first hyphen  \n    *  **SubLocation** – only trailing " +
						"part of Site after first hyphen.  \n    *  **BaseParam** – only first part of data " +
						"type (Param) before first hyphen  \n    *  **SubParam** – only trailing part of data " +
						"type (Param) after first hyphen\n    *  **ParamType**  \n    *  **Interval**\n    " +
						"*  **Duration**  \n    *  **Version**  \n    *  **BaseVersion**\n    *  **SubVersion**",
				responses = {
						@ApiResponse(responseCode = "200", description = "Successfully retrieved time series group details"),
						@ApiResponse(responseCode = "400", description = "Invalid or missing group ID", content = @Content),
						@ApiResponse(responseCode = "500", description = "Database error occurred", content = @Content)
				},
				tags = {"Time Series Methods - Groups"}
		)
		public Response getTsGroupRefs (@Parameter(description = "Requested group id", required = true,
				schema = @Schema(implementation = Long.class), example = "9")
			@QueryParam("groupid") Long groupId)
				throws WebAppException, DbException
		{
			Logger.getLogger(ApiConstants.loggerName).fine("getTsGroup");
		try (DbInterface dbi = new DbInterface();
			ApiTsDAO dao = new ApiTsDAO(dbi))
		{
			return ApiHttpUtil.createResponse(dao.getTsGroup(groupId));
		}
	}

		@POST
		@Path("tsgroup")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
		@Operation(
				summary = "Create a new, or update an existing time series group",
				description = "Example URL for POST:  \n\n    http://localhost:8080/odcsapi/tsgroup?token=6b994be905e1fddf\n\n" +
						"This method requires a valid session token. The POST data is as described above for GET tsgroup",
				requestBody = @RequestBody(
						description = "Time Series Group",
						required = true,
						content = @Content(mediaType = MediaType.APPLICATION_JSON,
								schema = @Schema(implementation = ApiTsGroup.class))
				),
				responses = {
						@ApiResponse(responseCode = "200", description = "Successfully created the time series group",
								content = @Content(mediaType = MediaType.APPLICATION_JSON,
										schema = @Schema(implementation = ApiTsGroup.class))),
						@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
				},
				tags = {"Time Series Methods - Groups"}
		)
		public Response postTsGroup (ApiTsGroup grp) throws WebAppException, DbException
		{
			Logger.getLogger(ApiConstants.loggerName).fine("postTsGroup");
		
		try (DbInterface dbi = new DbInterface();
			ApiTsDAO dao = new ApiTsDAO(dbi))
		{
			dao.writeGroup(grp);
			return ApiHttpUtil.createResponse(grp);
		}
	}

		@DELETE
		@Path("tsgroup")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
		@Operation(
				summary = "Delete Time Series Group",
				description = "Example URL for DELETE:  \n\n    "
						+ "http://localhost:8080/odcsapi/delete?token=6b994be905e1fddf&groupid=9\n\n"
						+ "This example deletes the Time series group with ID 9.  \n  \n"
						+ "This method requires a valid session token.",
				responses = {
						@ApiResponse(responseCode = "200", description = "Successfully deleted time series group",
							content = @Content),
						@ApiResponse(responseCode = "400", description = "Missing or invalid group ID", content = @Content),
						@ApiResponse(responseCode = "500", description = "Database error occurred", content = @Content)
				},
				tags = {"Time Series Methods - Groups"}
		)
		public Response deleteTsGroup (@Parameter(description = "Group Id to delete", required = true, example = "9",
				schema = @Schema(implementation = Long.class))
			@QueryParam("groupid") Long groupId)
				throws WebAppException, DbException
		{
			Logger.getLogger(ApiConstants.loggerName).fine("delete tsgroup id=" + groupId);
		
		try (DbInterface dbi = new DbInterface();
			ApiTsDAO dao = new ApiTsDAO(dbi))
		{
			dao.deleteGroup(groupId);
			return ApiHttpUtil.createResponse("tsgroup with ID=" + groupId + " deleted");
		}
	}
}
