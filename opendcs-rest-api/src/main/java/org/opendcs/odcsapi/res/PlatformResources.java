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

import java.sql.SQLException;
import java.util.ArrayList;
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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opendcs.odcsapi.beans.ApiPlatform;
import org.opendcs.odcsapi.beans.ApiPlatformRef;
import org.opendcs.odcsapi.dao.ApiPlatformDAO;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.sec.AuthorizationCheck;
import org.opendcs.odcsapi.util.ApiConstants;
import org.opendcs.odcsapi.util.ApiHttpUtil;

@Path("/")
public class PlatformResources
{
	@Context HttpHeaders httpHeaders;

	@GET
	@Path("platformrefs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response gePlatformRefs(@QueryParam("tmtype") String tmtype)
		throws DbException, SQLException
	{
		HashMap<String, ApiPlatformRef> ret = new HashMap<>();
		Logger.getLogger(ApiConstants.loggerName).fine("getPlatforms, tmtype=" + tmtype);
		try (DbInterface dbi = new DbInterface();
			ApiPlatformDAO platformDAO = new ApiPlatformDAO(dbi))
		{
			ArrayList<ApiPlatformRef> platSpecs = platformDAO.getPlatformRefs(tmtype);
			for(ApiPlatformRef ps : platSpecs)
				ret.put(ps.getName(), ps);
		}
		Logger.getLogger(ApiConstants.loggerName).fine("getPlatforms returning " + ret.size() + " objects.");
		return ApiHttpUtil.createResponse(ret);
	}
	
	@GET
	@Path("platform")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getPlatform(@QueryParam("platformid") Long platformId)
		throws WebAppException, DbException, SQLException
	{
		if (platformId == null)
			throw new WebAppException(ErrorCodes.MISSING_ID, 
				"Missing required platformid parameter.");
		
		Logger.getLogger(ApiConstants.loggerName).fine("getPlatform id=" + platformId);
		try (DbInterface dbi = new DbInterface();
			ApiPlatformDAO dao = new ApiPlatformDAO(dbi))
		{
			return ApiHttpUtil.createResponse(dao.readPlatform(platformId));
		}
	}
	
	@POST
	@Path("platform")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response postPlatform(ApiPlatform platform)
		throws WebAppException, DbException, SQLException
	{
		Logger.getLogger(ApiConstants.loggerName)
				.fine("post platform received platformId=" + platform.getPlatformId());
		
		try (DbInterface dbi = new DbInterface();
			ApiPlatformDAO dao = new ApiPlatformDAO(dbi))
		{
			dao.writePlatform(platform);
			return ApiHttpUtil.createResponse(platform);
		}
	}
	
	@DELETE
	@Path("platform")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response deletePlatform(@QueryParam("platformid") Long platformId)
		throws WebAppException, DbException
	{
		Logger.getLogger(ApiConstants.loggerName)
				.fine("DELETE platform received id=" + platformId);
		
		// Use username and password to attempt to connect to the database
		try (DbInterface dbi = new DbInterface();
			ApiPlatformDAO platDao = new ApiPlatformDAO(dbi))
		{
			platDao.deletePlatform(platformId);
			return ApiHttpUtil.createResponse("Platform with ID " + platformId + " deleted");
		}
	}
	
	@GET
	@Path("platformstat")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response gePlatformStats(@QueryParam("netlistid") Long netlistId)
		throws DbException, SQLException
	{
		Logger.getLogger(ApiConstants.loggerName).fine("gePlatformStats, netlistid=" + netlistId);
		try (DbInterface dbi = new DbInterface();
			ApiPlatformDAO platformDAO = new ApiPlatformDAO(dbi))
		{
			return ApiHttpUtil.createResponse(platformDAO.getPlatformStatus(netlistId));
		}
	}

}
