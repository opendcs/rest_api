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

import java.util.ArrayList;
import java.util.List;
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
import decodes.tsdb.CompFilter;
import decodes.tsdb.ConstraintException;
import decodes.tsdb.DbCompAlgorithm;
import decodes.tsdb.DbCompParm;
import decodes.tsdb.DbComputation;
import decodes.tsdb.DbIoException;
import decodes.tsdb.NoSuchObjectException;
import decodes.tsdb.TsGroup;
import decodes.tsdb.compedit.ComputationInList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import opendcs.dai.ComputationDAI;
import org.opendcs.odcsapi.beans.ApiCompParm;
import org.opendcs.odcsapi.beans.ApiComputation;
import org.opendcs.odcsapi.beans.ApiComputationRef;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.DatabaseItemNotFoundException;
import org.opendcs.odcsapi.errorhandling.MissingParameterException;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.util.ApiConstants;

import static java.util.stream.Collectors.toList;

@Path("/")
public final class ComputationResources extends OpenDcsResource
{
	@Context HttpHeaders httpHeaders;

	@GET
	@Path("computationrefs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_USER, ApiConstants.ODCS_API_ADMIN})
	@Operation(
			summary = "Retrieve Computation References",
			description = "Example:  \n\n    http://localhost:8080/odcsapi/computationrefs",
			tags = {"REST - Computation Methods"},
			responses = {
					@ApiResponse(responseCode = "200", description = "Success",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
									array = @ArraySchema(schema = @Schema(implementation = ApiComputationRef.class)))),
					@ApiResponse(responseCode = "404", description = "No computations found matching the filter criteria"),
					@ApiResponse(responseCode = "500", description = "Internal Server Error")
			}
		)
	public Response getComputationRefs(
			@Parameter(schema = @Schema(implementation = Long.class),
					description = "Site ID to filter on") @QueryParam("site") Long siteId,
			@Parameter(schema = @Schema(implementation = Long.class),
					description = "Algorithm ID to filter on") @QueryParam("algorithm") Long algorithmId,
			@Parameter(schema = @Schema(implementation = Long.class),
					description = "Datatype ID to filter on") @QueryParam("datatype") Long dataTypeId,
			@Parameter(schema = @Schema(implementation = Long.class),
					description = "Group ID to filter on") @QueryParam("group") Long groupId,
			@Parameter(schema = @Schema(implementation = Long.class),
					description = "Process ID to filter on") @QueryParam("process") Long processId,
			@Parameter(schema = @Schema(implementation = Boolean.class),
					description = "Whether to filter only enabled computations") @QueryParam("enabled") Boolean enabled,
			@Parameter(schema = @Schema(implementation = String.class),
					description = "Interval code to filter on") @QueryParam("interval") String interval)
			throws DbException
	{
		try (ComputationDAI dai = getLegacyTimeseriesDB().makeComputationDAO())
		{
			CompFilter compFilter = new CompFilter();
			if (dataTypeId != null)
			{
				compFilter.setDataTypeId(DbKey.createDbKey(dataTypeId));
			}
			if (groupId != null)
			{
				compFilter.setGroupId(DbKey.createDbKey(groupId));
			}
			if (processId != null)
			{
				compFilter.setProcessId(DbKey.createDbKey(processId));
			}
			if (siteId != null)
			{
				compFilter.setSiteId(DbKey.createDbKey(siteId));
			}
			if (interval != null)
			{
				compFilter.setIntervalCode(interval);
			}
			List<ApiComputationRef> computationRefs = map(dai.compEditList(compFilter));
			return Response.status(HttpServletResponse.SC_OK).entity(computationRefs).build();
		}
		catch(DbIoException e)
		{
			throw new DbException("Unable to retrieve computation references", e);
		}
	}

	static ArrayList<ApiComputationRef> map(ArrayList<ComputationInList> computations)
	{
		ArrayList<ApiComputationRef> ret = new ArrayList<>();
		for (ComputationInList comp : computations)
		{
			ApiComputationRef ref = new ApiComputationRef();
			ref.setComputationId(comp.getComputationId().getValue());
			if (comp.getAlgorithmId() != null)
			{
				ref.setAlgorithmId(comp.getAlgorithmId().getValue());
			}
			else
			{
				ref.setAlgorithmId(DbKey.NullKey.getValue());
			}
			ref.setAlgorithmName(comp.getAlgorithmName());
			ref.setName(comp.getComputationName());
			ref.setEnabled(comp.isEnabled());
			ref.setDescription(comp.getDescription());
			ref.setProcessName(comp.getProcessName());
			if (comp.getProcessId() != null)
			{
				ref.setProcessId(comp.getProcessId().getValue());
			}
			else
			{
				ref.setProcessId(DbKey.NullKey.getValue());
			}
			ret.add(ref);
		}
		return ret;
	}

	@GET
	@Path("computation")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_USER, ApiConstants.ODCS_API_ADMIN})
	@Operation(
			summary = "Retrieve Computation by its ID",
			description = "Example: \n\n    http://localhost:8080/odcsapi/computation?computationid=4",
			tags = {"REST - Computation Methods"},
			responses = {
					@ApiResponse(responseCode = "200", description = "Success",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = ApiComputation.class))),
					@ApiResponse(responseCode = "400", description = "Missing required computationid parameter"),
					@ApiResponse(responseCode = "404", description = "Computation with the specified ID not found"),
					@ApiResponse(responseCode = "500", description = "Internal Server Error")
			}
	)
	public Response getComputation(@Parameter(required = true, description = "Unique Computation ID",
			schema = @Schema(implementation = Long.class, example = "4"))
		@QueryParam("computationid") Long compId)
			throws WebAppException, DbException
	{
		if (compId == null)
		{
			throw new MissingParameterException("Missing required computationid parameter.");
		}

		try (ComputationDAI dai = getLegacyTimeseriesDB().makeComputationDAO())
		{
			return Response.status(HttpServletResponse.SC_OK)
					.entity(map(dai.getComputationById(DbKey.createDbKey(compId)))).build();
		}
		catch(DbIoException e)
		{
			throw new DbException(String.format("Unable to retrieve computation by ID: %s", compId), e);
		}
		catch (NoSuchObjectException e)
		{
			throw new DatabaseItemNotFoundException(String.format("Computation with ID %s not found", compId), e);
		}
	}

	static ApiComputation map(DbComputation comp)
	{
		ApiComputation ret = new ApiComputation();
		if (comp.getId() != null)
		{
			ret.setComputationId(comp.getId().getValue());
		}
		else
		{
			ret.setComputationId(DbKey.NullKey.getValue());
		}
		if (comp.getAlgorithmId() != null)
		{
			ret.setAlgorithmId(comp.getAlgorithmId().getValue());
		}
		else
		{
			ret.setAlgorithmId(DbKey.NullKey.getValue());
		}
		ret.setComment(comp.getComment());
		if (comp.getAppId() != null)
		{
			ret.setAppId(comp.getAppId().getValue());
		}
		else
		{
			ret.setAppId(DbKey.NullKey.getValue());
		}
		ret.setEnabled(comp.isEnabled());
		ret.setEffectiveEndDate(comp.getValidEnd());
		ret.setEffectiveStartDate(comp.getValidStart());
		ret.setAlgorithmName(comp.getAlgorithmName());
		ret.setApplicationName(comp.getApplicationName());
		ret.setGroupName(comp.getGroupName());
		ret.setName(comp.getName());
		ret.setLastModified(comp.getLastModified());
		if (comp.getGroupId() != null)
		{
			ret.setGroupId(comp.getGroupId().getValue());
		}
		else
		{
			ret.setGroupId(DbKey.NullKey.getValue());
		}
		ret.setProps(comp.getProperties());
		ret.setParmList(new ArrayList<>(comp.getParmList()
				.stream()
				.map(ComputationResources::map)
				.collect(toList())));
		return ret;
	}

	static ApiCompParm map(DbCompParm parm)
	{
		ApiCompParm ret = new ApiCompParm();
		if (parm.getDataType() != null)
		{
			ret.setDataType(parm.getDataType().getDisplayName());
		}
		ret.setInterval(parm.getInterval());
		if (parm.getSiteName() != null)
		{
			ret.setSiteName(parm.getSiteName().getDisplayName());
		}
		if (parm.getSiteId() != null)
		{
			ret.setSiteId(parm.getSiteId().getValue());
		}
		else
		{
			ret.setSiteId(DbKey.NullKey.getValue());
		}
		ret.setUnitsAbbr(parm.getUnitsAbbr());
		ret.setAlgoParmType(parm.getAlgoParmType());
		ret.setAlgoRoleName(parm.getRoleName());
		ret.setDuration(parm.getDuration());
		ret.setInterval(parm.getInterval());
		ret.setDeltaT(parm.getDeltaT());
		if (parm.getDataTypeId() != null)
		{
			ret.setDataTypeId(parm.getDataTypeId().getValue());
		}
		else
		{
			ret.setDataTypeId(DbKey.NullKey.getValue());
		}
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
	@RolesAllowed({ApiConstants.ODCS_API_USER, ApiConstants.ODCS_API_ADMIN})
	@Operation(
			summary = "Create or Overwrite Existing OpenDCS Computation",
			description = "The Computation POST method takes a single OpenDCS Computation Record in JSON format,"
					+ " as described above for GET.  \n\n"
					+ "For creating a new record, leave computationId out of the passed data structure.  \n\n"
					+ "For overwriting an existing one, include the computationId that was previously returned. "
					+ "The computation in the database is replaced with the one sent.",
			tags = {"REST - Computation Methods"},
			requestBody = @RequestBody(
					description = "Computation",
					required = true,
					content = @Content(mediaType = MediaType.APPLICATION_JSON,
							schema = @Schema(implementation = ApiComputation.class),
						examples = {
								@ExampleObject(name = "Basic", value = ResourceExamples.ComputationExamples.BASIC),
								@ExampleObject(name = "New", value = ResourceExamples.ComputationExamples.NEW),
								@ExampleObject(name = "Update", value = ResourceExamples.ComputationExamples.UPDATE)
						}
					)
			),
			responses = {
					@ApiResponse(responseCode = "201", description = "Successfully stored computation",
							content = @Content(mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = ApiComputation.class))),
					@ApiResponse(responseCode = "500", description = "Internal Server Error")
			}
	)
	public Response postComputation(ApiComputation comp)
			throws DbException
	{
		try (ComputationDAI dai = getLegacyTimeseriesDB().makeComputationDAO())
		{
			DbComputation dbComp = map(comp);
			dai.writeComputation(dbComp);
			return Response.status(HttpServletResponse.SC_CREATED).entity(map(dbComp)).build();
		}
		catch(DbIoException e)
		{
			throw new DbException("Unable to store computation", e);
		}
	}

	static DbComputation map(ApiComputation comp)
	{
		DbComputation ret;
		if (comp.getComputationId() != null)
		{
			ret = new DbComputation(DbKey.createDbKey(comp.getComputationId()), comp.getName());
		}
		else
		{
			ret = new DbComputation(DbKey.NullKey, comp.getName());
		}
		if (comp.getAlgorithmId() != null)
		{
			ret.setAlgorithmId(DbKey.createDbKey(comp.getAlgorithmId()));
		}
		if (comp.getAppId() != null)
		{
			ret.setAppId(DbKey.createDbKey(comp.getAppId()));
		}
		ret.setComment(comp.getComment());
		ret.setEnabled(comp.isEnabled());
		ret.setValidEnd(comp.getEffectiveEndDate());
		ret.setValidStart(comp.getEffectiveStartDate());
		ret.setAlgorithmName(comp.getAlgorithmName());
		if (comp.getAlgorithmId() != null)
		{
			ret.setAlgorithm(new DbCompAlgorithm(DbKey.createDbKey(comp.getAlgorithmId()),
					comp.getAlgorithmName(), null, comp.getComment()));
		}
		ret.setApplicationName(comp.getApplicationName());
		ret.setGroup(new TsGroup().copy(comp.getGroupName()));
		ret.setLastModified(comp.getLastModified());
		if (comp.getGroupId() != null)
		{
			ret.setGroupId(DbKey.createDbKey(comp.getGroupId()));
		}
		else
		{
			ret.setGroupId(DbKey.NullKey);
		}
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
		if (parm == null)
		{
			return null;
		}
		DbCompParm ret = new DbCompParm(parm.getAlgoRoleName(),
				parm.getDataTypeId() != null ? DbKey.createDbKey(parm.getDataTypeId()) : DbKey.NullKey,
				parm.getInterval(), parm.getTableSelector(), parm.getDeltaT());
		if (parm.getDataTypeId() != null)
		{
			DataType dt = new DataType(parm.getDataType(), parm.getDataTypeId().toString());
			ret.setDataType(dt);
		}
		ret.setInterval(parm.getInterval());
		if (parm.getSiteId() != null)
		{
			Site site = new Site();
			site.setPublicName(parm.getSiteName());
			ret.setSite(site);
			ret.setSiteId(DbKey.createDbKey(parm.getSiteId()));
		}
		else
		{
			ret.setSiteId(DbKey.NullKey);
		}
		ret.setUnitsAbbr(parm.getUnitsAbbr());
		ret.setAlgoParmType(parm.getAlgoParmType());
		ret.setRoleName(parm.getAlgoRoleName());
		ret.setInterval(parm.getInterval());
		ret.setDeltaT(parm.getDeltaT());
		ret.setDeltaTUnits(parm.getDeltaTUnits());
		if (parm.getModelId() != null)
		{
			ret.setModelId(parm.getModelId());
		}
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
	@RolesAllowed({ApiConstants.ODCS_API_USER, ApiConstants.ODCS_API_ADMIN})
	@Operation(
			summary = "Delete Existing OpenDCS Computation",
			description = "Required argument computationid must be passed in the URL.",
			tags = {"REST - Computation Methods"},
			responses = {
					@ApiResponse(responseCode = "204", description = "Successfully deleted computation"),
					@ApiResponse(responseCode = "400", description = "Missing required computationid parameter"),
					@ApiResponse(responseCode = "500", description = "Internal Server Error")
			}
	)
	public Response deleteComputation(@Parameter(required = true, description = "Unique Computation ID",
			schema = @Schema(implementation = Long.class, example = "4"))
		@QueryParam("computationid") Long computationId)
			throws DbException, WebAppException
	{
		if (computationId == null)
		{
			throw new MissingParameterException("Missing required computationid parameter.");
		}

		try (ComputationDAI dai = getLegacyTimeseriesDB().makeComputationDAO())
		{
			dai.deleteComputation(DbKey.createDbKey(computationId));
			return Response.status(HttpServletResponse.SC_NO_CONTENT)
					.entity(String.format("Computation with ID: %d deleted", computationId)).build();
		}
		catch(DbIoException | ConstraintException e)
		{
			throw new DbException(String.format("Unable to delete computation by ID: %s", computationId), e);
		}
	}
}

