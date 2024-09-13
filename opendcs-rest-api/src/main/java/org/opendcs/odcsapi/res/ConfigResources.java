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

import org.opendcs.odcsapi.beans.ApiConfigScript;
import org.opendcs.odcsapi.beans.ApiConfigScriptSensor;
import org.opendcs.odcsapi.beans.ApiPlatformConfig;
import org.opendcs.odcsapi.dao.ApiConfigDAO;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.sec.AuthorizationCheck;
import org.opendcs.odcsapi.util.ApiConstants;
import org.opendcs.odcsapi.util.ApiHttpUtil;

@Path("/")
public class ConfigResources
{
	@Context HttpHeaders httpHeaders;

	@GET
	@Path("configrefs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getConfigRefs() throws DbException
	{
		Logger.getLogger(ApiConstants.loggerName).fine("geConfigRefs");
		try (DbInterface dbi = new DbInterface();
			ApiConfigDAO configDAO = new ApiConfigDAO(dbi))
		{
			return ApiHttpUtil.createResponse(configDAO.getConfigRefs());
		}
	}

	@GET
	@Path("config")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getConfig(@QueryParam("configid") Long configId) throws WebAppException, DbException, SQLException
	{
		if (configId == null)
			throw new WebAppException(ErrorCodes.MISSING_ID, 
				"Missing required configid parameter.");
		
		Logger.getLogger(ApiConstants.loggerName).fine("getConfig id=" + configId);
		try (DbInterface dbi = new DbInterface();
			ApiConfigDAO configDAO = new ApiConfigDAO(dbi))
		{
			return ApiHttpUtil.createResponse(configDAO.getConfig(configId));
		}
	}

	@POST
	@Path("config")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response postConfig(ApiPlatformConfig config) throws WebAppException, DbException, SQLException
	{
		Logger.getLogger(ApiConstants.loggerName).fine("post config received config " + config.getName() 
			+ " with ID=" + config.getConfigId());
		
		Logger.getLogger(ApiConstants.loggerName).fine("POST config script sensors: ");
		for(ApiConfigScript acs : config.getScripts())
		{
			Logger.getLogger(ApiConstants.loggerName).fine("\tscript " + acs.getName());
			for(ApiConfigScriptSensor acss : acs.getScriptSensors())
			{
				Logger.getLogger(ApiConstants.loggerName).fine("\t\t" + acss.prettyPrint());
			}
		}
		try (DbInterface dbi = new DbInterface();
			ApiConfigDAO configDAO = new ApiConfigDAO(dbi))
		{
			configDAO.writeConfig(config);
			return ApiHttpUtil.createResponse(config);
		}
	}
	
	@DELETE
	@Path("config")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response deleteConfig(@QueryParam("configid") Long configId) throws WebAppException, DbException, SQLException
	{
		Logger.getLogger(ApiConstants.loggerName).fine("DELETE config received configid=" + configId);
		
		// Use username and password to attempt to connect to the database
		try (DbInterface dbi = new DbInterface();
			ApiConfigDAO cfgDao = new ApiConfigDAO(dbi))
		{
			if (cfgDao.numPlatformsUsing(configId) > 0)
				return ApiHttpUtil.createResponse(" Cannot delete config with ID " + configId + " because it is used by one or more platforms.", ErrorCodes.NOT_ALLOWED);
				
			cfgDao.deleteConfig(configId);
			return ApiHttpUtil.createResponse("Config with ID " + configId + " deleted");
		}
	}


}
