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

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

import decodes.db.DataSource;
import decodes.db.DatabaseException;
import decodes.db.DatabaseIO;
import decodes.db.RoutingSpec;
import decodes.db.RoutingSpecList;
import decodes.db.ScheduleEntry;
import decodes.db.ScheduleEntryStatus;
import decodes.polling.DacqEvent;
import decodes.sql.DbKey;
import decodes.tsdb.DbIoException;
import opendcs.dai.DacqEventDAI;
import opendcs.dai.ScheduleEntryDAI;
import org.opendcs.odcsapi.beans.ApiRouting;
import org.opendcs.odcsapi.beans.ApiRoutingExecStatus;
import org.opendcs.odcsapi.beans.ApiRoutingRef;
import org.opendcs.odcsapi.beans.ApiScheduleEntry;
import org.opendcs.odcsapi.beans.ApiScheduleEntryRef;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.util.ApiConstants;

@Path("/")
public final class RoutingResources extends OpenDcsResource
{
	@Context private HttpServletRequest request;
	@Context private HttpHeaders httpHeaders;

	@GET
	@Path("routingrefs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	public Response getRoutingRefs() throws DbException
	{
		try
		{
			//TODO: Fix this endpoint. Currently, it is returning an empty list.
			DatabaseIO dbio = getLegacyDatabase();
			RoutingSpecList rsList = new RoutingSpecList();
			dbio.readRoutingSpecList(rsList);
			return Response.status(HttpServletResponse.SC_OK)
					.entity(map(rsList)).build();
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to retrieve routing reference list", e);
		}
	}

	static List<ApiRoutingRef> map(RoutingSpecList rsList)
	{
		List<ApiRoutingRef> refs = new ArrayList<>();
		rsList.getList().forEach(rs -> {
			ApiRoutingRef ref = new ApiRoutingRef();
			if (rs.getId() != null)
			{
				ref.setRoutingId(rs.getId().getValue());
			}
			else
			{
				ref.setRoutingId(DbKey.NullKey.getValue());
			}
			ref.setName(rs.getName());
			refs.add(ref);
		});
		return refs;
	}

	@GET
	@Path("routing")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	public Response getRouting(@QueryParam("routingid") Long routingId)
			throws WebAppException, DbException
	{

		if (routingId == null)
		{
			throw new WebAppException(ErrorCodes.MISSING_ID,
					"Missing required routingid parameter.");
		}

		try
		{
			DatabaseIO dbio = getLegacyDatabase();
			RoutingSpec spec = new RoutingSpec();
			spec.setId(DbKey.createDbKey(routingId));
			dbio.readRoutingSpec(spec);
			return Response.status(HttpServletResponse.SC_OK)
					.entity(map(spec)).build();
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to retrieve routing spec by ID", e);
		}
	}

	static ApiRouting map(RoutingSpec spec) {
		ApiRouting routing = new ApiRouting();
		if (spec.getId() != null)
		{
			routing.setRoutingId(spec.getId().getValue());
		}
		else
		{
			routing.setRoutingId(DbKey.NullKey.getValue());
		}
		routing.setName(spec.getName());
		routing.setLastModified(spec.lastModifyTime);
		if (spec.outputTimeZone != null)
		{
			routing.setOutputTZ(spec.outputTimeZone.toZoneId().getId());
		}
		routing.setNetlistNames(new ArrayList<>(spec.networkListNames));
		routing.setOutputFormat(spec.outputFormat);
		routing.setEnableEquations(spec.enableEquations);
		return routing;
	}

	@POST
	@Path("routing")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	public Response postRouting(ApiRouting routing)
			throws DbException
	{
		try
		{
			DatabaseIO dbio = getLegacyDatabase();
			RoutingSpec spec = map(routing);
			dbio.writeRoutingSpec(spec);
			return Response.status(HttpServletResponse.SC_OK).entity(map(spec)).build();
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to store routing spec", e);
		}
	}

	static RoutingSpec map(ApiRouting routing) throws DbException
	{
		try
		{
			RoutingSpec spec = new RoutingSpec();
			if (routing.getRoutingId() != null)
			{
				spec.setId(DbKey.createDbKey(routing.getRoutingId()));
			}
			else
			{
				spec.setId(DbKey.NullKey);
			}
			spec.setName(routing.getName());
			spec.usePerformanceMeasurements = false;
			spec.lastModifyTime = routing.getLastModified();
			if (routing.getOutputTZ() != null)
			{
				spec.outputTimeZoneAbbr = routing.getOutputTZ();
				spec.outputTimeZone = TimeZone.getTimeZone(ZoneId.of(routing.getOutputTZ()));
			}
			routing.getNetlistNames().forEach(spec::addNetworkListName);
			spec.outputFormat = routing.getOutputFormat();
			spec.enableEquations = routing.isEnableEquations();
			spec.presentationGroupName = routing.getPresGroupName();
			spec.isProduction = routing.isProduction();
			spec.consumerArg = routing.getDestinationArg();
			spec.consumerType = routing.getDestinationType();
			if (routing.getSince() != null)
			{
				spec.sinceTime = routing.getSince();
			}
			if (routing.getUntil() != null)
			{
				spec.untilTime = routing.getUntil();
			}
			spec.setProperties(routing.getProperties());
			if (routing.getDataSourceId() != null)
			{
				DataSource dataSource = new DataSource();
				if (routing.getDataSourceId() != null)
				{
					dataSource.setId(DbKey.createDbKey(routing.getDataSourceId()));
				}
				else
				{
					dataSource.setId(DbKey.NullKey);
				}
				dataSource.setName(routing.getDataSourceName());
				spec.dataSource = dataSource;
			}
			spec.networkListNames = new Vector<>(routing.getNetlistNames());
			return spec;
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to map routing spec", e);
		}

	}

	@DELETE
	@Path("routing")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteRouting(@QueryParam("routingid") Long routingId)
			throws DbException, WebAppException
	{
		if (routingId == null)
		{
			throw new WebAppException(HttpServletResponse.SC_BAD_REQUEST,
					"Missing required routingid parameter.");
		}

		try
		{
			DatabaseIO dbio = getLegacyDatabase();
			RoutingSpec spec = new RoutingSpec();
			spec.setId(DbKey.createDbKey(routingId));
			dbio.deleteRoutingSpec(spec);
			return Response.status(HttpServletResponse.SC_OK)
					.entity("RoutingSpec with ID " + routingId + " deleted").build();
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to delete routing spec", e);
		}
	}

	@GET
	@Path("schedulerefs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	public Response getScheduleRefs()
			throws DbException
	{

		try (ScheduleEntryDAI dai = getLegacyDatabase().makeScheduleEntryDAO())
		{
			List<ScheduleEntry> entries = dai.listScheduleEntries(null);
			return Response.status(HttpServletResponse.SC_OK)
					.entity(map(entries)).build();
		}
		catch(DbIoException e)
		{
			throw new DbException("Unable to retrieve schedule entry ref list", e);
		}
	}

	static List<ApiScheduleEntryRef> map(List<ScheduleEntry> entries)
	{
		List<ApiScheduleEntryRef> refs = new ArrayList<>();
		entries.forEach(entry -> {
			ApiScheduleEntryRef ref = new ApiScheduleEntryRef();
			if (entry.getId() != null)
			{
				ref.setSchedEntryId(entry.getId().getValue());
			}
			else
			{
				ref.setSchedEntryId(DbKey.NullKey.getValue());
			}
			ref.setEnabled(entry.isEnabled());
			ref.setAppName(entry.getLoadingAppName());
			ref.setName(entry.getName());
			ref.setLastModified(entry.getLastModified());
			ref.setRoutingSpecName(entry.getRoutingSpecName());
			refs.add(ref);
		});
		return refs;
	}

	@GET
	@Path("schedule")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	public Response getSchedule(@QueryParam("scheduleid") Long scheduleId)
			throws WebAppException, DbException
	{
		if (scheduleId == null)
			throw new WebAppException(ErrorCodes.MISSING_ID,
					"Missing required scheduleid parameter.");
		try (ScheduleEntryDAI dai = getLegacyDatabase().makeScheduleEntryDAO())
		{
			ScheduleEntry entry = new ScheduleEntry(DbKey.createDbKey(scheduleId));

			// TODO: Add support for schedule retrieval by id in OpenDCS,
			//  which currently only supports retrieval by name

			//		try (DbInterface dbi = new DbInterface();
			//				ApiRoutingDAO dao = new ApiRoutingDAO(dbi))
			//		{
			//			return ApiHttpUtil.createResponse(dao.getSchedule(scheduleId));
			//		}
		}
		//		catch(DbIoException e)
		//		{
		//			throw new DbException("Unable to retrieve schedule entry by ID", e);
		//		}

		return Response.status(HttpServletResponse.SC_NOT_IMPLEMENTED).build();
	}

	@POST
	@Path("schedule")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	public Response postSchedule(ApiScheduleEntry schedule)
			throws DbException
	{
		try (ScheduleEntryDAI dai = getLegacyDatabase().makeScheduleEntryDAO())
		{
			ScheduleEntry entry = map(schedule);
			dai.writeScheduleEntry(entry);
			return Response.status(HttpServletResponse.SC_OK)
					.entity(map(entry))
					.build();
		}
		catch (DbIoException e)
		{
			throw new DbException("Unable to store schedule entry", e);
		}
	}

	static ScheduleEntry map(ApiScheduleEntry schedule) throws DbException
	{
		try
		{
			ScheduleEntry entry = new ScheduleEntry(schedule.getName());
			if (schedule.getSchedEntryId() != null)
			{
				entry.setId(DbKey.createDbKey(schedule.getSchedEntryId()));
			}
			else
			{
				entry.setId(DbKey.NullKey);
			}
			entry.setStartTime(schedule.getStartTime());
			entry.setTimezone(schedule.getTimeZone());
			if (schedule.getAppId() != null)
			{
				entry.setLoadingAppId(DbKey.createDbKey(schedule.getAppId()));
				entry.setLoadingAppName(schedule.getAppName());
			}
			if (schedule.getRoutingSpecId() != null)
			{
				entry.setRoutingSpecId(DbKey.createDbKey(schedule.getRoutingSpecId()));
				entry.setRoutingSpecName(schedule.getRoutingSpecName());
			}
			entry.setRunInterval(schedule.getRunInterval());
			entry.setLastModified(schedule.getLastModified());
			return entry;
		}
		catch (DatabaseException e)
		{
			throw new DbException("Unable to map schedule entry", e);
		}
	}

	static ApiScheduleEntry map(ScheduleEntry entry)
	{
		ApiScheduleEntry schedule = new ApiScheduleEntry();
		if (entry.getId() != null)
		{
			schedule.setSchedEntryId(entry.getId().getValue());
		}
		else
		{
			schedule.setSchedEntryId(DbKey.NullKey.getValue());
		}
		schedule.setName(entry.getName());
		schedule.setStartTime(entry.getStartTime());
		schedule.setTimeZone(entry.getTimezone());
		schedule.setEnabled(entry.isEnabled());
		if (entry.getLoadingAppId() != null)
		{
			schedule.setAppId(entry.getLoadingAppId().getValue());
			schedule.setAppName(entry.getLoadingAppName());
		}
		if (entry.getRoutingSpecId() != null)
		{
			schedule.setRoutingSpecId(entry.getRoutingSpecId().getValue());
			schedule.setRoutingSpecName(entry.getRoutingSpecName());
		}
		schedule.setRunInterval(entry.getRunInterval());
		schedule.setLastModified(entry.getLastModified());
		return schedule;
	}

	@DELETE
	@Path("schedule")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	public Response deleteSchedule(@QueryParam("scheduleid") Long scheduleId)
			throws DbException, WebAppException
	{
		if (scheduleId == null)
		{
			throw new WebAppException(HttpServletResponse.SC_BAD_REQUEST, "missing required scheduleid argument.");
		}
		try (ScheduleEntryDAI dai = getLegacyDatabase().makeScheduleEntryDAO())
		{
			ScheduleEntry entry = new ScheduleEntry(DbKey.createDbKey(scheduleId));
			dai.deleteScheduleEntry(entry);
			return Response.status(HttpServletResponse.SC_OK)
					.entity("Schedule entry with ID " + scheduleId + " deleted").build();
		}
		catch (DbIoException e)
		{
			throw new DbException(String.format("Unable to delete schedule entry by ID: %s", scheduleId), e);
		}
	}


	@GET
	@Path("routingstatus")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	public Response getRoutingStats()
		throws DbException
	{
		try (ScheduleEntryDAI dai = getLegacyDatabase().makeScheduleEntryDAO())
		{
			return Response.status(HttpServletResponse.SC_OK)
					.entity(map(dai.listScheduleEntries(null))).build();
		}
		catch (DbIoException e)
		{
			throw new DbException("Unable to retrieve routing status", e);
		}
	}

	static ArrayList<ApiScheduleEntryRef> map(ArrayList<ScheduleEntry> entries)
	{
		ArrayList<ApiScheduleEntryRef> refs = new ArrayList<>();
		for (ScheduleEntry entry : entries)
		{
			ApiScheduleEntryRef ref = new ApiScheduleEntryRef();
			ref.setSchedEntryId(entry.getId().getValue());
			ref.setEnabled(entry.isEnabled());
			ref.setAppName(entry.getLoadingAppName());
			ref.setName(entry.getName());
			ref.setLastModified(entry.getLastModified());
			ref.setRoutingSpecName(entry.getRoutingSpecName());
			refs.add(ref);
		}

		return refs;
	}

	@GET
	@Path("routingexecstatus")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	public Response getRoutingExecStatus(@QueryParam("scheduleentryid") Long scheduleEntryId)
			throws WebAppException, DbException
	{
		if (scheduleEntryId == null)
		{
			throw new WebAppException(HttpServletResponse.SC_BAD_REQUEST, "missing required scheduleentryid argument.");
		}

		try (ScheduleEntryDAI dai = getLegacyDatabase().makeScheduleEntryDAO())
		{
			ScheduleEntry entry = new ScheduleEntry(DbKey.createDbKey(scheduleEntryId));
			return Response.status(HttpServletResponse.SC_OK)
					.entity(statusMap(dai.readScheduleStatus(entry))).build();
		}
		catch (DbIoException e)
		{
			throw new DbException("Unable to retrieve routing exec status", e);
		}
	}

	static ArrayList<ApiRoutingExecStatus> statusMap(ArrayList<ScheduleEntryStatus> statuses)
	{
		ArrayList<ApiRoutingExecStatus> execStatuses = new ArrayList<>();
		for (ScheduleEntryStatus status : statuses)
		{
			ApiRoutingExecStatus execStatus = new ApiRoutingExecStatus();
			if (status.getScheduleEntryId() != null)
			{
				execStatus.setScheduleEntryId(status.getScheduleEntryId().getValue());
			}
			else
			{
				execStatus.setScheduleEntryId(DbKey.NullKey.getValue());
			}
			execStatus.setHostname(status.getHostname());
			execStatus.setNumErrors(status.getNumDecodesErrors());
			execStatus.setRunStatus(status.getRunStatus());
			execStatus.setRunStop(status.getRunStop());
			execStatus.setRunStart(status.getRunStart());
			execStatus.setNumPlatforms(status.getNumPlatforms());
			execStatus.setNumMessages(status.getNumMessages());
			execStatus.setLastActivity(status.getLastModified());
			execStatuses.add(execStatus);
		}
		return execStatuses;
	}

	@GET
	@Path("dacqevents")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	public Response getDacqEvents(@QueryParam("appid") Long appId, @QueryParam("routingexecid") Long routingExecId,
			@QueryParam("platformid") Long platformId, @QueryParam("backlog") String backlog)
			throws DbException
	{
		// TODO: Add support for dacq events retrieval in OpenDCS, which currently only supports retrieval by a few
		// 		of the above parameters independently

		//		try (DbInterface dbi = new DbInterface();
		//			ApiRoutingDAO dao = new ApiRoutingDAO(dbi))
		//		{
		//			HttpSession session = request.getSession(true);
		//			return ApiHttpUtil.createResponse(dao.getDacqEvents(appId, routingExecId, platformId, backlog, session));
		//		}

		try (DacqEventDAI dai = getLegacyTimeseriesDB().makeDacqEventDAO())
		{
			ArrayList<DacqEvent> platformEvents = new ArrayList<>();
			if (platformId != null) {
				dai.readEventsForPlatform(DbKey.createDbKey(platformId), platformEvents);
			}
			ArrayList<DacqEvent> routingExecEvents = new ArrayList<>();
			if (routingExecId != null) {
				dai.readEventsForScheduleStatus(DbKey.createDbKey(routingExecId), routingExecEvents);
			}



			//			dai.getDacqEvents(appId, routingExecId, platformId, backlog);
			//			return Response.status(HttpServletResponse.SC_OK)
			//					.entity(events).build();
		}
		catch (DbIoException e)
		{
			throw new DbException("Unable to retrieve dacq events", e);
		}

		return Response.status(HttpServletResponse.SC_NOT_IMPLEMENTED).build();
	}
}
