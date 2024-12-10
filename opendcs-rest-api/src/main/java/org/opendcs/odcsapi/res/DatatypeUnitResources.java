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

import decodes.db.DataTypeSet;
import decodes.db.DatabaseException;
import decodes.db.DatabaseIO;
import decodes.db.EngineeringUnit;
import decodes.db.EngineeringUnitList;
import decodes.db.UnitConverterDb;
import decodes.db.UnitConverterSet;
import decodes.sql.DbKey;
import decodes.sql.UnitConverterIO;
import org.opendcs.odcsapi.beans.ApiUnit;
import org.opendcs.odcsapi.beans.ApiUnitConverter;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.sec.AuthorizationCheck;

/**
 * HTTP Resources relating to DataTypes, Engineering Units, and Conversions
 * @author mmaloney
 *
 */
@Path("/")
public class DatatypeUnitResources extends OpenDcsResource
{
	@Context HttpHeaders httpHeaders;
	private static final String NO_UNIT_CONVERTER = "No UnitConverterIO available.";

	@GET
	@Path("datatypelist")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getDataTypeList(@QueryParam("standard") String std) throws DbException
	{
		try
		{
			DatabaseIO dbIo = getLegacyDatabase();
			DataTypeSet set = new DataTypeSet();
			dbIo.readDataTypeSet(set);
			return Response.status(HttpServletResponse.SC_OK).entity(set).build();
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to retrieve data type list", e);
		}
	}


	@GET
	@Path("unitlist")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getUnitList() throws DbException
	{
		try
		{
			DatabaseIO dbIo = getLegacyDatabase();
			EngineeringUnitList euList = new EngineeringUnitList();
			dbIo.readEngineeringUnitList(euList);
			return Response.status(HttpServletResponse.SC_OK).entity(euList).build();
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to retrieve data type list", e);
		}
	}
	
	@POST
	@Path("eu")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response postEU(@QueryParam("fromabbr") String fromabbr, ApiUnit eu)
		throws DbException
	{
		try
		{
			EngineeringUnit unit = new EngineeringUnit(fromabbr, eu.getName(), eu.getAbbr(), eu.getMeasures());
			DatabaseIO dbIo = getLegacyDatabase();
			EngineeringUnitList euList = new EngineeringUnitList();
			euList.add(unit);
			dbIo.writeEngineeringUnitList(euList);
			return Response.status(HttpServletResponse.SC_OK)
					.entity("{\"message\": \"The Engineering Unit was Saved successfully.\"}").build();
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to store Engineering Unit list", e);
		}
	}

	@DELETE
	@Path("eu")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response deleteEU(@QueryParam("abbr") String abbr) throws DbException
	{
		try
		{
			DatabaseIO dbIo = getLegacyDatabase();
			EngineeringUnit unit = new EngineeringUnit(abbr, null, null, null);
			EngineeringUnitList euList = new EngineeringUnitList();
			euList.add(unit);
			dbIo.writeEngineeringUnitList(euList);
			return Response.status(HttpServletResponse.SC_OK).entity("EU with abbr " + abbr + " deleted").build();
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to store Engineering Unit list", e);
		}
	}

	@GET
	@Path("euconvlist")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getUnitConvList() throws DbException
	{
		try
		{
			DatabaseIO dbIo = getLegacyDatabase();
			UnitConverterSet unitConverterSet = new UnitConverterSet();
			dbIo.readUnitConverterSet(unitConverterSet);
			return Response.status(HttpServletResponse.SC_OK).entity(unitConverterSet).build();
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to retrieve Unit Converter list", e);
		}
	}
	
	@POST
	@Path("euconv")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response postEUConv(ApiUnitConverter euc) throws DbException
	{
		try
		{
			// TODO: Create a write method for UnitConverter in OpenDCS
			UnitConverterIO unitDao = createDb().getLegacyDatabase(UnitConverterIO.class)
					.orElseThrow(() -> new DbException(NO_UNIT_CONVERTER));

			unitDao.write(map(euc));
			return Response.status(HttpServletResponse.SC_OK).entity("EUConv Saved").build();
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to store Unit Converter list", e);
		}
	}

	static UnitConverterSet map(ApiUnitConverter euc) throws DbException
	{
		try
		{
			UnitConverterSet unitConverterSet = new UnitConverterSet();
			UnitConverterDb unitConverterDb = new UnitConverterDb(euc.getFromAbbr(), euc.getToAbbr());
			unitConverterDb.setId(DbKey.createDbKey(euc.getUcId()));
			unitConverterDb.algorithm = euc.getAlgorithm();
			unitConverterDb.coefficients[0] = euc.getA();
			unitConverterDb.coefficients[1] = euc.getB();
			unitConverterDb.coefficients[2] = euc.getC();
			unitConverterDb.coefficients[3] = euc.getD();
			unitConverterDb.coefficients[4] = euc.getE();
			unitConverterDb.coefficients[5] = euc.getF();
			unitConverterSet.addDbConverter(unitConverterDb);
			return unitConverterSet;
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to map Unit Converter", e);
		}
	}

	@DELETE
	@Path("euconv")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response deleteEUConv(@QueryParam("euconvid") Long id) throws DbException
	{
		try
		{
			// TODO: Create a delete method for UnitConverter in OpenDCS
			UnitConverterIO unitDao = createDb().getLegacyDatabase(UnitConverterIO.class)
					.orElseThrow(() -> new DbException(NO_UNIT_CONVERTER));
			UnitConverterDb unitConvDB = new UnitConverterDb("", "");
			unitConvDB.setId(DbKey.createDbKey(id));
			unitDao.delete(unitConvDB);
			return Response.status(HttpServletResponse.SC_OK).entity("EUConv with id=" + id + " deleted").build();
		}
		catch(SQLException | DatabaseException e)
		{
			throw new DbException("Unable to delete Unit Converter", e);
		}
	}
}
