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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

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

import decodes.db.DatabaseException;
import decodes.db.DatabaseIO;
import decodes.db.Platform;
import decodes.db.PlatformConfig;
import decodes.db.PlatformList;
import decodes.db.PlatformSensor;
import decodes.db.PlatformStatus;
import decodes.db.Site;
import decodes.db.TransportMedium;
import decodes.sql.DbKey;
import decodes.tsdb.DbIoException;
import opendcs.dai.PlatformStatusDAI;
import org.opendcs.odcsapi.beans.ApiPlatform;
import org.opendcs.odcsapi.beans.ApiPlatformRef;
import org.opendcs.odcsapi.beans.ApiPlatformSensor;
import org.opendcs.odcsapi.beans.ApiPlatformStatus;
import org.opendcs.odcsapi.beans.ApiTransportMedium;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.sec.AuthorizationCheck;

@Path("/")
public class PlatformResources extends OpenDcsResource
{
	@Context HttpHeaders httpHeaders;

	@GET
	@Path("platformrefs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getPlatformRefs(@QueryParam("tmtype") String tmtype)
			throws DbException
	{
		Map<String, ApiPlatformRef> ret = new LinkedHashMap<>();
		try
		{
			// TODO: Add support for filtering by transport media type
			DatabaseIO dbio = getLegacyDatabase();
			PlatformList platformList = new PlatformList();
			dbio.readPlatformList(platformList);
			List<ApiPlatformRef> platSpecs = map(platformList);
			for(ApiPlatformRef ps : platSpecs)
				ret.put(ps.getName(), ps);
			return Response.status(HttpServletResponse.SC_OK).entity(ret).build();
		}
		catch (DatabaseException ex)
		{
			throw new DbException("Unable to retrieve platform list", ex);
		}
	}

	static List<ApiPlatformRef> map(PlatformList platformList)
	{
		List<ApiPlatformRef> ret = new ArrayList<>();
		Iterator<Platform> platform = platformList.iterator();
		while (platform.hasNext())
		{
			ApiPlatformRef ref = new ApiPlatformRef();
			Platform plat = platform.next();
			ref.setName(plat.getDisplayName());
			if (plat.getId() != null)
			{
				ref.setPlatformId(plat.getId().getValue());
			}
			else
			{
				ref.setPlatformId(DbKey.NullKey.getValue());
			}
			ref.setAgency(plat.getAgency());
			ref.setConfig(plat.getConfigName());
			ref.setDescription(plat.getDescription());
			if (plat.getConfig() != null && plat.getConfig().getId() != null)
			{
				ref.setConfigId(plat.getConfig().getId().getValue());
			}
			else
			{
				ref.setConfigId(DbKey.NullKey.getValue());
			}
			if (plat.getSite() != null)
			{
				ref.setSiteId(plat.getSite().getId().getValue());
			}
			else
			{
				ref.setSiteId(DbKey.NullKey.getValue());
			}
			ref.setTransportMedia(plat.getProperties());
			ret.add(ref);
		}
		return ret;
	}

	@GET
	@Path("platform")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getPlatform(@QueryParam("platformid") Long platformId)
			throws WebAppException, DbException
	{
		if (platformId == null)
		{
			throw new WebAppException(HttpServletResponse.SC_BAD_REQUEST,
					"Missing required platformid parameter.");
		}

		try
		{
			DatabaseIO dbio = getLegacyDatabase();
			Platform platform = new Platform();
			platform.setId(DbKey.createDbKey(platformId));
			dbio.readPlatform(platform);
			return Response.status(HttpServletResponse.SC_OK).entity(map(platform)).build();
		}
		catch (DatabaseException ex)
		{
			throw new DbException("Unable to retrieve platform", ex);
		}
	}

	static ApiPlatform map(Platform platform)
	{
		ApiPlatform ret = new ApiPlatform();
		if (platform.getId() != null)
		{
			ret.setPlatformId(platform.getId().getValue());
		}
		else
		{
			ret.setPlatformId(DbKey.NullKey.getValue());
		}
		ret.setAgency(platform.getAgency());
		if (platform.getSite() != null && platform.getSite().getId() != null)
		{
			ret.setSiteId(platform.getSite().getId().getValue());
		}
		ret.setDescription(platform.getDescription());
		ret.setDesignator(platform.getPlatformDesignator());
		if (platform.getConfig() != null && platform.getConfig().getId() != null)
		{
			ret.setConfigId(platform.getConfig().getId().getValue());
		}
		else
		{
			ret.setConfigId(DbKey.NullKey.getValue());
		}
		ret.setProduction(platform.isProduction);
		if (platform.getSite() != null && platform.getSite().getId() != null)
		{
			ret.setSiteId(platform.getSite().getId().getValue());
		}
		else
		{
			ret.setSiteId(DbKey.NullKey.getValue());
		}
		ret.setTransportMedia(map(platform.getTransportMedia()));
		return ret;
	}

	static ArrayList<ApiTransportMedium> map(Iterator<TransportMedium> transportMedium)
	{
		ArrayList<ApiTransportMedium> transportMedia = new ArrayList<>();
		while (transportMedium.hasNext())
		{
			TransportMedium tm = transportMedium.next();
			ApiTransportMedium apiTm = new ApiTransportMedium();
			apiTm.setMediumId(tm.getMediumId());
			apiTm.setMediumType(tm.getMediumType());
			apiTm.setBaud(tm.getBaud());
			apiTm.setAssignedTime(tm.assignedTime);
			apiTm.setChannelNum(tm.channelNum);
			apiTm.setDataBits(tm.getDataBits());
			apiTm.setTimezone(tm.getTimeZone());
			apiTm.setStopBits(tm.getStopBits());
			apiTm.setParity(String.valueOf(tm.getParity()));
			apiTm.setDoLogin(tm.isDoLogin());
			apiTm.setPassword(tm.getPassword());
			apiTm.setUsername(tm.getUsername());
			apiTm.setScriptName(tm.scriptName);
			apiTm.setTransportInterval(tm.transmitInterval);
			apiTm.setTransportWindow(tm.transmitWindow);

			transportMedia.add(apiTm);
		}
		return transportMedia;
	}

	@POST
	@Path("platform")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response postPlatform(ApiPlatform platform)
			throws DbException
	{
		try
		{
			DatabaseIO dbio = getLegacyDatabase();
			Platform plat = map(platform);
			dbio.writePlatform(plat);
			return Response.status(HttpServletResponse.SC_OK)
					.entity(map(plat))
					.build();
		}
		catch (DatabaseException ex)
		{
			throw new DbException(String.format("Unable to store platform with name: %s", platform.getName()), ex);
		}
	}

	static Platform map(ApiPlatform platform) throws DatabaseException
	{
		Platform ret = new Platform();
		if (platform.getPlatformId() != null)
		{
			ret.setId(DbKey.createDbKey(platform.getPlatformId()));
		}
		else
		{
			ret.setId(DbKey.NullKey);
		}
		ret.setAgency(platform.getAgency());
		ret.setDescription(platform.getDescription());
		ret.setPlatformDesignator(platform.getDesignator());
		ret.lastModifyTime = platform.getLastModified();
		ret.platformSensors = platMap(platform.getPlatformSensors());
		if (platform.getConfigId() != null)
		{
			PlatformConfig config = new PlatformConfig();
			config.setId(DbKey.createDbKey(platform.getConfigId()));
			ret.setConfig(config);
		}
		ret.isProduction = platform.isProduction();
		if (platform.getSiteId() != null)
		{
			Site site = new Site();
			site.setId(DbKey.createDbKey(platform.getSiteId()));
			ret.setSite(site);
		}
		ret.transportMedia = map(platform.getTransportMedia());
		return ret;
	}

	static Vector<PlatformSensor> platMap(List<ApiPlatformSensor> platformSensors) throws DatabaseException
	{
		Vector<PlatformSensor> ret = new Vector<>();
		for (ApiPlatformSensor sensor: platformSensors)
		{
			PlatformSensor ps = new PlatformSensor();
			ps.sensorNumber = sensor.getSensorNum();
			if (sensor.getActualSiteId() != null)
			{
				Site site = new Site();
				site.setId(DbKey.createDbKey(sensor.getActualSiteId()));
				ps.site = site;
			}
			ps.setUsgsDdno(sensor.getUsgsDdno());
			Properties props = sensor.getSensorProps();
			for (String name : props.stringPropertyNames())
			{
				ps.setProperty(name, props.getProperty(name));
			}
			ret.add(ps);
		}
		return ret;
	}

	static Vector<TransportMedium> map(List<ApiTransportMedium> transportMedium)
	{
		Vector<TransportMedium> ret = new Vector<>();
		for (ApiTransportMedium tm : transportMedium)
		{
			Platform platform = new Platform();
			TransportMedium t = new TransportMedium(platform);
			t.setMediumId(tm.getMediumId());
			t.setMediumType(tm.getMediumType());
			t.setBaud(tm.getBaud());
			t.assignedTime = tm.getAssignedTime();
			t.channelNum = tm.getChannelNum();
			t.setDataBits(tm.getDataBits());
			t.setTimeZone(tm.getTimezone());
			t.setStopBits(tm.getStopBits());
			t.setParity(tm.getParity().charAt(0));
			t.setDoLogin(tm.getDoLogin());
			t.setPassword(tm.getPassword());
			t.setUsername(tm.getUsername());
			t.scriptName = tm.getScriptName();
			t.transmitInterval = tm.getTransportInterval();
			t.transmitWindow = tm.getTransportWindow();
			t.channelNum = tm.getChannelNum();
			t.assignedTime = tm.getAssignedTime();
			ret.add(t);
		}
		return ret;
	}

	@DELETE
	@Path("platform")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response deletePlatform(@QueryParam("platformid") Long platformId)
			throws DbException, WebAppException
	{
		if (platformId == null)
		{
			throw new WebAppException(HttpServletResponse.SC_BAD_REQUEST,
					"Missing required platformid parameter.");
		}

		try
		{
			DatabaseIO dbio = getLegacyDatabase();
			Platform plat = new Platform();
			plat.setId(DbKey.createDbKey(platformId));
			dbio.deletePlatform(plat);
			return Response.status(HttpServletResponse.SC_OK)
					.entity("Platform with ID " + platformId + " deleted")
					.build();
		}
		catch (DatabaseException ex)
		{
			throw new DbException(String.format("Unable to delete platform with ID: %s", platformId), ex);
		}
	}

	@GET
	@Path("platformstat")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getPlatformStats(@QueryParam("netlistid") Long netlistId)
			throws DbException, WebAppException
	{
		if (netlistId == null)
		{
			throw new WebAppException(HttpServletResponse.SC_BAD_REQUEST,
					"Missing required netlistid parameter.");
		}

		DatabaseIO dbio = getLegacyDatabase();
		try (PlatformStatusDAI dao = dbio.makePlatformStatusDAO())
		{
			PlatformStatus status = dao.readPlatformStatus(DbKey.createDbKey(netlistId));
			return Response.status(HttpServletResponse.SC_OK).entity(map(status)).build();
		}
		catch (DbIoException ex)
		{
			throw new DbException(String.format("Unable to retrieve platform status with ID: %s", netlistId), ex);
		}
	}

	static List<ApiPlatformStatus> map(PlatformStatus status)
	{
		if (status == null)
		{
			return new ArrayList<>();
		}
		List<ApiPlatformStatus> ret = new ArrayList<>();
		ApiPlatformStatus ps = new ApiPlatformStatus();
		if (status.getPlatformId() != null)
		{
			ps.setPlatformId(status.getPlatformId().getValue());
		}
		else
		{
			ps.setPlatformId(DbKey.NullKey.getValue());
		}
		ps.setAnnotation(status.getAnnotation());
		ps.setLastContact(status.getLastContactTime());
		ps.setLastError(status.getLastErrorTime());
		ps.setLastMessage(status.getLastMessageTime());
		ps.setRoutingSpecName(status.getLastRoutingSpecName());
		ret.add(ps);
		return ret;
	}

}