/*
 *  Copyright 2023 OpenDCS Consortium
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.annotation.security.RolesAllowed;
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

import decodes.cwms.CwmsTsId;
import decodes.sql.DbKey;
import decodes.tsdb.BadTimeSeriesException;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.DbIoException;
import decodes.tsdb.TimeSeriesDb;
import decodes.tsdb.TimeSeriesIdentifier;
import decodes.tsdb.TsGroup;
import opendcs.dai.IntervalDAI;
import opendcs.dai.TimeSeriesDAI;
import opendcs.dai.TsGroupDAI;
import opendcs.opentsdb.Interval;
import org.opendcs.odcsapi.beans.ApiInterval;
import org.opendcs.odcsapi.beans.ApiTimeSeriesData;
import org.opendcs.odcsapi.beans.ApiTimeSeriesIdentifier;
import org.opendcs.odcsapi.beans.ApiTsGroup;
import org.opendcs.odcsapi.beans.ApiTsGroupRef;
import org.opendcs.odcsapi.dao.ApiRefListDAO;
import org.opendcs.odcsapi.dao.ApiTsDAO;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.sec.AuthorizationCheck;

import ilex.util.IDateFormat;

/**
 * HTTP resources relating to Time Series data and descriptors
 * @author mmaloney
 *
 */
@Path("/")
public class TimeSeriesResources extends OpenDcsResource
{
	@Context HttpHeaders httpHeaders;

	@GET
	@Path("tsrefs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getTimeSeriesRefs(@QueryParam("active") Boolean activeOnly) throws DbException
	{
		TimeSeriesDb tsdb = getLegacyTimeseriesDB();
		try (TimeSeriesDAI dai = tsdb.makeTimeSeriesDAO())
		{
			return Response.status(HttpServletResponse.SC_OK)
					.entity(map(dai.listTimeSeries(), activeOnly != null && activeOnly))
					.build();
		}
		catch (DbIoException ex)
		{
			throw new DbException("Unable to retrieve time series", ex);
		}
	}

	// TODO: Add active support to OpenDCS DAIs.
	//  This method only supports active checking for the CWMS DB implementation.
	static ArrayList<ApiTimeSeriesIdentifier> map(ArrayList<TimeSeriesIdentifier> identifiers, boolean activeOnly)
	{
		ArrayList<ApiTimeSeriesIdentifier> ret = new ArrayList<>();
		for(TimeSeriesIdentifier id : identifiers)
		{
			if (activeOnly && id instanceof CwmsTsId)
			{
				CwmsTsId ctsid = (CwmsTsId)id;
				if (ctsid.isActive())
				{
					ApiTimeSeriesIdentifier apiId = new ApiTimeSeriesIdentifier();
					apiId.setKey(id.getKey().getValue());
					apiId.setActive(ctsid.isActive());
					apiId.setDescription(id.getDescription());
					apiId.setStorageUnits(id.getStorageUnits());
					apiId.setUniqueString(id.getUniqueString());
					ret.add(apiId);
				}
			} else {
				ApiTimeSeriesIdentifier apiId = new ApiTimeSeriesIdentifier();
				apiId.setKey(id.getKey().getValue());
				apiId.setDescription(id.getDescription());
				apiId.setStorageUnits(id.getStorageUnits());
				apiId.setUniqueString(id.getUniqueString());
				ret.add(apiId);
			}
		}
		return ret;
	}

	@GET
	@Path("tsspec")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response getTimeSeriesSpec(@QueryParam("key") Long tsKey) throws WebAppException, DbException
	{
		if (tsKey == null)
			throw new WebAppException(ErrorCodes.MISSING_ID, 
				"Missing required tskey parameter.");
		
		try (DbInterface dbi = new DbInterface();
			ApiTsDAO dao = new ApiTsDAO(dbi))
		{

			return Response.status(HttpServletResponse.SC_OK)
					.entity(dao.getSpec(tsKey)).build();
		}
	}

	@GET
	@Path("tsdata")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getTimeSeriesData(@QueryParam("key") Long tsKey, @QueryParam("start") String start,
		@QueryParam("end") String end)
		throws WebAppException, DbException
	{
		if (tsKey == null)
			throw new WebAppException(ErrorCodes.MISSING_ID, 
				"Missing required tskey parameter.");
		
		Date dStart = null, dEnd = null;
		if (start != null)
			try { dStart = IDateFormat.parse(start); }
			catch (IllegalArgumentException ex)
			{
				throw new WebAppException(ErrorCodes.MISSING_ID, 
					"Invalid start time. Use [[[CC]YY]/DDD]/HH:MM[:SS] or relative time.");
			}
		if (end != null)
		{
			try { dEnd = IDateFormat.parse(end); }
			catch (IllegalArgumentException ex)
			{
				throw new WebAppException(ErrorCodes.MISSING_ID, 
					"Invalid end time. Use [[[CC]YY]/DDD]/HH:MM[:SS] or relative time.");
			}
		}

		try (TimeSeriesDAI dai = getLegacyTimeseriesDB().makeTimeSeriesDAO())
		{
			CTimeSeries cts = new CTimeSeries(DbKey.createDbKey(tsKey), null, null);
			dai.fillTimeSeries(cts, dStart, dEnd);
			return Response.status(HttpServletResponse.SC_OK)
					.entity(map(cts)).build();
		}
		catch (DbIoException | BadTimeSeriesException ex)
		{
			throw new DbException("Unable to retrieve time series data", ex);
		}
	}

	static ApiTimeSeriesData map(CTimeSeries cts)
	{
		ApiTimeSeriesData ret = new ApiTimeSeriesData();
		ret.setTsid(map(cts.getTimeSeriesIdentifier()));
		// TODO: Map data values, may require changes to OpenDCS
		return ret;
	}

	static ApiTimeSeriesIdentifier map(TimeSeriesIdentifier tsid)
	{
		ApiTimeSeriesIdentifier ret = new ApiTimeSeriesIdentifier();
		ret.setKey(tsid.getKey().getValue());
		ret.setUniqueString(tsid.getUniqueString());
		ret.setDescription(tsid.getDescription());
		ret.setStorageUnits(tsid.getStorageUnits());
		return ret;
	}
	
	@GET
	@Path("intervals")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getIntervals()
		throws DbException
	{
		try (DbInterface dbi = new DbInterface();
			ApiRefListDAO rlDAO = new ApiRefListDAO(dbi))
		{
			// TODO: Implement this in OpenDCS
			HashMap<Long,ApiInterval> imap = rlDAO.getIntervals();
			return Response.status(HttpServletResponse.SC_NOT_IMPLEMENTED)
					.entity(imap.values()).build();
		}
	}

	@POST
	@Path("interval")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response postInterval(ApiInterval intv)
		throws DbException
	{
		try (IntervalDAI dai = getLegacyTimeseriesDB().makeIntervalDAO())
		{
			dai.writeInterval(map(intv));
			return Response.status(HttpServletResponse.SC_OK)
					.entity(intv).build();
		}
		catch (DbIoException ex)
		{
			throw new DbException("Unable to store interval", ex);
		}
	}

	static Interval map(ApiInterval intv)
	{
		Interval ret = new Interval(intv.getName());
		ret.setKey(DbKey.createDbKey(intv.getIntervalId()));
		ret.setCalConstant(Integer.parseInt(intv.getCalConstant()));
		ret.setCalMultiplier(intv.getCalMultilier());
		return ret;
	}

	@DELETE
	@Path("interval")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response deleteInterval(@QueryParam("intvid") Long intvId) throws DbException
	{
		try (DbInterface dbi = new DbInterface();
			ApiRefListDAO rlDAO = new ApiRefListDAO(dbi))
		{
			// TODO: Implement this in OpenDCS
			rlDAO.deleteInterval(intvId);
			return Response.status(HttpServletResponse.SC_NOT_IMPLEMENTED)
					.entity("interval with ID=" + intvId + " deleted").build();
		}
	}

	@GET
	@Path("tsgrouprefs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getTsGroupRefs() throws DbException
	{
		try (TsGroupDAI dai = getLegacyTimeseriesDB().makeTsGroupDAO())
		{
			return Response.status(HttpServletResponse.SC_OK)
					.entity(mapRef(dai.getTsGroupList(null))).build();
		}
		catch (DbIoException ex)
		{
			throw new DbException("Unable to retrieve time series group references", ex);
		}
	}

	static ArrayList<ApiTsGroupRef> mapRef(ArrayList<TsGroup> groups)
	{
		ArrayList<ApiTsGroupRef> ret = new ArrayList<>();
		for(TsGroup group : groups)
		{
			ApiTsGroupRef ref = new ApiTsGroupRef();
			ref.setGroupId(group.getGroupId().getValue());
			ref.setGroupName(group.getGroupName());
			ref.setDescription(group.getDescription());
			ref.setGroupType(group.getGroupType());
			ret.add(ref);
		}
		return ret;
	}

	@GET
	@Path("tsgroup")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getTsGroupRefs(@QueryParam("groupid") Long groupId) throws DbException
	{
		try (TsGroupDAI dai = getLegacyTimeseriesDB().makeTsGroupDAO())
		{
			return Response.status(HttpServletResponse.SC_OK)
					.entity(map(dai.getTsGroupById(DbKey.createDbKey(groupId)))).build();
		}
		catch (DbIoException ex)
		{
			throw new DbException("Unable to retrieve time series group by ID", ex);
		}
	}

	static ApiTsGroup map(TsGroup group)
	{
		ApiTsGroup ret = new ApiTsGroup();
		ret.setGroupName(group.getGroupName());
		ret.setDescription(group.getDescription());
		ret.setGroupType(group.getGroupType());
		ret.setGroupId(group.getGroupId().getValue());
		ret.getIntersectGroups().addAll(mapRef(group.getIntersectedGroups()));
		return ret;
	}
	
	@POST
	@Path("tsgroup")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response postTsGroup(ApiTsGroup grp) throws DbException
	{
		try (TsGroupDAI dai = getLegacyTimeseriesDB().makeTsGroupDAO())
		{
			dai.writeTsGroup(map(grp));
			return Response.status(HttpServletResponse.SC_OK)
					.entity(grp).build();
		}
		catch (DbIoException ex)
		{
			throw new DbException("Unable to store time series group", ex);
		}
	}

	static TsGroup map(ApiTsGroup grp)
	{
		TsGroup ret = new TsGroup();
		ret.setDescription(grp.getDescription());
		ret.setGroupName(grp.getGroupName());
		ret.setGroupType(grp.getGroupType());
		ret.setIntersectedGroups(map(grp.getIntersectGroups()));
		if (grp.getGroupId() != null)
			ret.setGroupId(DbKey.createDbKey(grp.getGroupId()));
		return ret;
	}

	static ArrayList<TsGroup> map(ArrayList<ApiTsGroupRef> groupRefs)
	{
		ArrayList<TsGroup> ret = new ArrayList<>();
		for(ApiTsGroupRef ref : groupRefs)
		{
			TsGroup group = new TsGroup();
			group.setGroupName(ref.getGroupName());
			group.setDescription(ref.getDescription());
			group.setGroupType(ref.getGroupType());
			group.setGroupId(DbKey.createDbKey(ref.getGroupId()));
			ret.add(group);
		}
		return ret;
	}

	@DELETE
	@Path("tsgroup")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response deleteTsGroup(@QueryParam("groupid") Long groupId) throws DbException
	{
		try (TsGroupDAI dai = getLegacyTimeseriesDB().makeTsGroupDAO())
		{
			dai.deleteTsGroup(DbKey.createDbKey(groupId));
			return Response.status(HttpServletResponse.SC_OK)
					.entity("tsgroup with ID=" + groupId + " deleted").build();
		}
		catch (DbIoException ex)
		{
			throw new DbException("Unable to delete time series group", ex);
		}
	}
}
