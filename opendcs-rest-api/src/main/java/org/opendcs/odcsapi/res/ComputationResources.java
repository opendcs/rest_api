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
import java.util.stream.Collectors;
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

import decodes.db.DataType;
import decodes.db.Site;
import decodes.sql.DbKey;
import decodes.tsdb.ConstraintException;
import decodes.tsdb.DbCompAlgorithm;
import decodes.tsdb.DbCompParm;
import decodes.tsdb.DbComputation;
import decodes.tsdb.DbIoException;
import decodes.tsdb.NoSuchObjectException;
import decodes.tsdb.TsGroup;
import opendcs.dai.ComputationDAI;
import org.opendcs.odcsapi.beans.ApiCompParm;
import org.opendcs.odcsapi.beans.ApiComputation;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.sec.AuthorizationCheck;

@Path("/")
public class ComputationResources extends OpenDcsResource
{
	@Context HttpHeaders httpHeaders;
	
	@GET
	@Path("computationrefs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getComputationRefs(@QueryParam("site") String site,
		@QueryParam("algorithm") String algorithm,
		@QueryParam("datatype") String datatype,
		@QueryParam("group") String group,
		@QueryParam("process") String process,
		@QueryParam("enabled") Boolean enabled,
		@QueryParam("interval") String interval)
		throws WebAppException, DbException
	{
		try (ComputationDAI dai = createDb().getDao(ComputationDAI.class)
				.orElseThrow(() -> new DbException("No ComputationDAI implementation available")))
		{

//			return ApiHttpUtil.createResponse(dao.getComputationRefs(site, algorithm, datatype, group,
//					process, enabled, interval));
			return Response.status(HttpServletResponse.SC_NOT_IMPLEMENTED).build();
		}
	}

	@GET
	@Path("computation")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getComputation(@QueryParam("computationid") Long compId)
		throws WebAppException, DbException
	{
		if (compId == null)
			throw new WebAppException(ErrorCodes.MISSING_ID, 
				"Missing required computationid parameter.");
		
		try (ComputationDAI dai = createDb().getDao(ComputationDAI.class)
				.orElseThrow(() -> new DbException("No ComputationDAI implementation available")))
		{
			return Response.status(HttpServletResponse.SC_OK)
					.entity(map(dai.getComputationById(DbKey.createDbKey(compId)))).build();
		}
		catch(DbIoException | NoSuchObjectException e)
		{
			throw new DbException(String.format("Unable to retrieve computation by ID: %s", compId), e);
		}
	}

	static ApiComputation map(DbComputation comp)
	{
		ApiComputation ret = new ApiComputation();
		ret.setComputationId(comp.getId().getValue());
		ret.setAlgorithmId(comp.getAlgorithmId().getValue());
		ret.setComment(comp.getComment());
		ret.setAppId(comp.getAppId().getValue());
		ret.setEnabled(comp.isEnabled());
		ret.setEffectiveEndDate(comp.getValidEnd());
		ret.setEffectiveStartDate(comp.getValidStart());
		ret.setAlgorithmName(comp.getAlgorithmName());
		ret.setApplicationName(comp.getApplicationName());
		ret.setGroupName(comp.getGroupName());
		ret.setName(comp.getName());
		ret.setLastModified(comp.getLastModified());
		ret.setGroupId(comp.getGroupId().getValue());
		ret.setProps(comp.getProperties());
		ret.setParmList(new ArrayList<>(comp.getParmList()
				.stream()
				.map(ComputationResources::map)
				.collect(Collectors.toList())));
		return ret;
	}

	static ApiCompParm map(DbCompParm parm)
	{
		ApiCompParm ret = new ApiCompParm();
		ret.setDataType(parm.getDataType().getDisplayName());
		ret.setInterval(parm.getInterval());
		ret.setSiteName(parm.getSiteName().getNameValue());
		ret.setSiteId(parm.getSiteId().getValue());
		ret.setUnitsAbbr(parm.getUnitsAbbr());
		ret.setAlgoParmType(parm.getAlgoParmType());
		ret.setAlgoRoleName(parm.getRoleName());
		ret.setDuration(parm.getDuration());
		ret.setInterval(parm.getInterval());
		ret.setDeltaT(parm.getDeltaT());
		ret.setDataTypeId(parm.getDataTypeId().getValue());
		ret.setDeltaTUnits(parm.getDeltaTUnits());
		ret.setVersion(parm.getVersion());
		ret.setModelId(parm.getModelId());
		ret.setTableSelector(parm.getTableSelector());
		ret.setParamType(parm.getParamType());
		return ret;
	}
	
	@POST
	@Path("computation")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response postComputation(ApiComputation comp)
		throws DbException
	{
		try (ComputationDAI dai = createDb().getDao(ComputationDAI.class)
				.orElseThrow(() -> new DbException("No ComputationDAI implementation available")))
		{
			dai.writeComputation(map(comp));
			return Response.status(HttpServletResponse.SC_OK).build();
		}
		catch(DbIoException e)
		{
			throw new DbException("Unable to store computation", e);
		}
	}

	static DbComputation map(ApiComputation comp)
	{
		DbComputation ret = new DbComputation(DbKey.createDbKey(comp.getComputationId()), comp.getName());
		ret.setId(DbKey.createDbKey(comp.getComputationId()));
		ret.setAlgorithmId(DbKey.createDbKey(comp.getAlgorithmId()));
		ret.setAppId(DbKey.createDbKey(comp.getAppId()));
		ret.setComment(comp.getComment());
		ret.setEnabled(comp.isEnabled());
		ret.setValidEnd(comp.getEffectiveEndDate());
		ret.setValidStart(comp.getEffectiveStartDate());
		ret.setAlgorithmName(comp.getAlgorithmName());
		ret.setAlgorithm(new DbCompAlgorithm(DbKey.createDbKey(comp.getAlgorithmId()),
				comp.getAlgorithmName(), null, comp.getComment()));
		ret.setApplicationName(comp.getApplicationName());
		ret.setGroup(new TsGroup().copy(comp.getGroupName()));
		ret.setLastModified(comp.getLastModified());
		ret.setGroupId(DbKey.createDbKey(comp.getGroupId()));
		for (String prop : comp.getProps().stringPropertyNames())
		{
			ret.setProperty(prop, comp.getProps().getProperty(prop));
		}
		for (ApiCompParm parm : comp.getParmList())
		{
			ret.addParm(map(parm));
		}
		return ret;
	}

	static DbCompParm map(ApiCompParm parm)
	{
		DbCompParm ret = new DbCompParm(parm.getAlgoRoleName(),
				DbKey.createDbKey(parm.getSiteId()), parm.getInterval(),
				parm.getTableSelector(), parm.getDeltaT());
		DataType dt = new DataType(parm.getDataType(), parm.getDataTypeId().toString());
		ret.setDataType(dt);
		ret.setInterval(parm.getInterval());
		Site site = new Site();
		site.setPublicName(parm.getSiteName());
		ret.setSite(site);
		ret.setSiteId(DbKey.createDbKey(parm.getSiteId()));
		ret.setUnitsAbbr(parm.getUnitsAbbr());
		ret.setAlgoParmType(parm.getAlgoParmType());
		ret.setRoleName(parm.getAlgoRoleName());
		ret.setInterval(parm.getInterval());
		ret.setDeltaT(parm.getDeltaT());
		ret.setDataTypeId(DbKey.createDbKey(parm.getDataTypeId()));
		ret.setDeltaTUnits(parm.getDeltaTUnits());
		ret.setModelId(parm.getModelId());
		ret.setTableSelector(parm.getTableSelector());
		ret.setInterval(parm.getInterval());
		ret.setDeltaT(parm.getDeltaT());
		ret.setUnitsAbbr(parm.getUnitsAbbr());
		return ret;
	}

	@DELETE
	@Path("computation")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response deleteComputation(@QueryParam("computationid") Long computationId) throws DbException
	{
		try (ComputationDAI dai = createDb().getDao(ComputationDAI.class)
				.orElseThrow(() -> new DbException("No ComputationDAI implementation available")))
		{
			dai.deleteComputation(DbKey.createDbKey(computationId));
			return Response.status(HttpServletResponse.SC_OK)
					.entity("Computation with ID " + computationId + " deleted").build();
		}
		catch(DbIoException | ConstraintException e)
		{
			throw new DbException(String.format("Unable to delete computation by ID: %s", computationId), e);
		}
	}
}

