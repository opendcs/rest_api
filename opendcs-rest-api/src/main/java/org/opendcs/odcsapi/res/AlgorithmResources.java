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

import org.opendcs.odcsapi.beans.ApiAlgorithm;
import org.opendcs.odcsapi.dao.ApiAlgorithmDAO;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.sec.AuthorizationCheck;
import org.opendcs.odcsapi.util.ApiConstants;
import org.opendcs.odcsapi.util.ApiHttpUtil;

@Path("/")
public class AlgorithmResources
{
	@Context HttpHeaders httpHeaders;
	
	@GET
	@Path("algorithmrefs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(AuthorizationCheck.ODCS_API_GUEST)
	public Response getAlgorithmRefs() throws DbException
	{
		Logger.getLogger(ApiConstants.loggerName).fine("getAlgorithmRefs");
		try (DbInterface dbi = new DbInterface();
			ApiAlgorithmDAO dao = new ApiAlgorithmDAO(dbi))
		{
			return ApiHttpUtil.createResponse(dao.getAlgorithmRefs());
		}
	}

	@GET
	@Path("algorithm")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(AuthorizationCheck.ODCS_API_GUEST)
	public Response getAlgorithm(@QueryParam("algorithmid") Long algoId)
			throws WebAppException, DbException
	{
		if(algoId == null)
		{
			throw new WebAppException(ErrorCodes.MISSING_ID,
					"Missing required algorithmid parameter.");
		}

		Logger.getLogger(ApiConstants.loggerName).fine("getAlgorithm algorithmid=" + algoId);

		try(DbInterface dbi = new DbInterface();
			ApiAlgorithmDAO dao = new ApiAlgorithmDAO(dbi))
		{
			return ApiHttpUtil.createResponse(dao.getAlgorithm(algoId));
		}
	}

	
	@POST
	@Path("algorithm")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response postAlgorithm(ApiAlgorithm algo)
		throws WebAppException, DbException, SQLException
	{
		Logger.getLogger(ApiConstants.loggerName).fine("post algo received algo " + algo.getName()
			+ " with ID=" + algo.getAlgorithmId());
		
		try (DbInterface dbi = new DbInterface();
			ApiAlgorithmDAO dao = new ApiAlgorithmDAO(dbi))
		{
			return ApiHttpUtil.createResponse(dao.writeAlgorithm(algo));
		}
	}

	@DELETE
	@Path("algorithm")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response deletAlgorithm(@QueryParam("algorithmid") Long algorithmId)
		throws WebAppException, DbException, SQLException
	{
		Logger.getLogger(ApiConstants.loggerName).fine("DELETE algorithm received algorithmId=" + algorithmId);
		// Use username and password to attempt to connect to the database
		try (DbInterface dbi = new DbInterface();
			ApiAlgorithmDAO dao = new ApiAlgorithmDAO(dbi))
		{
			dao.deleteAlgorithm(algorithmId);
			return ApiHttpUtil.createResponse("Algorithm with ID " + algorithmId + " deleted");
		}
	}
}
