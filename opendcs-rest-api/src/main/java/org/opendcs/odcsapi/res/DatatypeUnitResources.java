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

import decodes.db.CompositeConverter;
import decodes.db.DataTypeSet;
import decodes.db.DatabaseException;
import decodes.db.DatabaseIO;
import decodes.db.EngineeringUnit;
import decodes.db.EngineeringUnitList;
import decodes.db.LinearConverter;
import decodes.db.NullConverter;
import decodes.db.Poly5Converter;
import decodes.db.UnitConverter;
import decodes.db.UnitConverterDb;
import decodes.db.UnitConverterSet;
import decodes.db.UsgsStdConverter;
import decodes.sql.DbKey;
import org.opendcs.odcsapi.beans.ApiDataType;
import org.opendcs.odcsapi.beans.ApiUnit;
import org.opendcs.odcsapi.beans.ApiUnitConverter;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.WebAppException;
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
	private DatabaseIO dbIo;

	@GET
	@Path("datatypelist")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getDataTypeList(@QueryParam("standard") String std) throws DbException
	{
		try
		{
			dbIo = getLegacyDatabase();
			DataTypeSet set = new DataTypeSet();
			dbIo.readDataTypeSet(set, std);
			return Response.status(HttpServletResponse.SC_OK).entity(map(set)).build();
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to retrieve data type list", e);
		}
		finally
		{
			dbIo.close();
		}
	}

	static ArrayList<ApiDataType> map(DataTypeSet set)
	{
		ArrayList<ApiDataType> ret = new ArrayList<>();
		Iterator<decodes.db.DataType> it = set.iterator();
		while(it.hasNext())
		{
			decodes.db.DataType dt = it.next();
			ApiDataType adt = new ApiDataType();
			if (dt.getId() != null)
			{
				adt.setId(dt.getId().getValue());
			}
			else
			{
				adt.setId(DbKey.NullKey.getValue());
			}
			adt.setCode(dt.getCode());
			adt.setStandard(dt.getStandard());
			adt.setDisplayName(dt.getDisplayName());
			ret.add(adt);
		}
		return ret;
	}


	@GET
	@Path("unitlist")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getUnitList() throws DbException
	{
		try
		{
			dbIo = getLegacyDatabase();
			EngineeringUnitList euList = new EngineeringUnitList();
			dbIo.readEngineeringUnitList(euList);
			return Response.status(HttpServletResponse.SC_OK).entity(map(euList)).build();
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to retrieve data type list", e);
		}
		finally
		{
			dbIo.close();
		}
	}

	static ArrayList<ApiUnit> map(EngineeringUnitList unitList)
	{
		ArrayList<ApiUnit> ret = new ArrayList<>();
		Iterator<EngineeringUnit> it = unitList.iterator();
		while(it.hasNext())
		{
			EngineeringUnit eu = it.next();
			ApiUnit apiUnit = new ApiUnit();
			apiUnit.setAbbr(eu.abbr);
			apiUnit.setName(eu.getName());
			apiUnit.setMeasures(eu.measures);
			apiUnit.setFamily(eu.family);
			ret.add(apiUnit);
		}
		return ret;

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
			EngineeringUnit unit = new EngineeringUnit(fromabbr, eu.getName(), eu.getFamily(), eu.getMeasures());
			dbIo = getLegacyDatabase();
			EngineeringUnitList euList = new EngineeringUnitList();
			dbIo.readEngineeringUnitList(euList);
			euList.add(unit);
			dbIo.writeEngineeringUnitList(euList);
			return Response.status(HttpServletResponse.SC_OK)
					.entity(map(euList)).build();
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to store Engineering Unit list", e);
		}
		finally
		{
			dbIo.close();
		}
	}

	@DELETE
	@Path("eu")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response deleteEU(@QueryParam("abbr") String abbr) throws DbException, WebAppException
	{
		if (abbr == null)
		{
			throw new WebAppException(HttpServletResponse.SC_BAD_REQUEST, "Missing required abbr parameter");
		}

		try
		{
			dbIo = getLegacyDatabase();
			EngineeringUnit unit = new EngineeringUnit(abbr, "", "", "");
			EngineeringUnitList euList = new EngineeringUnitList();
			euList.add(unit);
			dbIo.writeEngineeringUnitList(euList);
			return Response.status(HttpServletResponse.SC_OK).entity("EU with abbr " + abbr + " deleted").build();
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to store Engineering Unit list", e);
		}
		finally
		{
			dbIo.close();
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
			dbIo = getLegacyDatabase();
			UnitConverterSet unitConverterSet = new UnitConverterSet();
			dbIo.readUnitConverterSet(unitConverterSet);
			return Response.status(HttpServletResponse.SC_OK).entity(map(unitConverterSet)).build();
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to retrieve Unit Converter list", e);
		}
		finally
		{
			dbIo.close();
		}
	}

	static List<ApiUnitConverter> map(UnitConverterSet unitSet)
	{
		List<ApiUnitConverter> ret = new ArrayList<>();
		Iterator<UnitConverterDb> it = unitSet.iteratorDb();
		while (it.hasNext())
		{
			UnitConverterDb unitConv = it.next();
			ApiUnitConverter euc = new ApiUnitConverter();
			if (unitConv.getId() != null)
			{
				euc.setUcId(unitConv.getId().getValue());
			}
			else
			{
				euc.setUcId(DbKey.NullKey.getValue());
			}
			euc.setFromAbbr(unitConv.fromAbbr);
			euc.setToAbbr(unitConv.toAbbr);
			euc.setAlgorithm(unitConv.algorithm);
			euc.setA(unitConv.coefficients[0] == 0.0 ? null : unitConv.coefficients[0]);
			euc.setB(unitConv.coefficients[1] == 0.0 ? null : unitConv.coefficients[1]);
			euc.setC(unitConv.coefficients[2] == 0.0 ? null : unitConv.coefficients[2]);
			euc.setD(unitConv.coefficients[3] == 0.0 ? null : unitConv.coefficients[3]);
			euc.setE(unitConv.coefficients[4] == 0.0 ? null : unitConv.coefficients[4]);
			euc.setF(unitConv.coefficients[5] == 0.0 ? null : unitConv.coefficients[5]);
			ret.add(euc);
		}
		return ret;
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
			dbIo = getLegacyDatabase();
			UnitConverterSet unitSet = map(euc);
			dbIo.writeUnitConverterSet(unitSet);
			return Response.status(HttpServletResponse.SC_OK).entity(map(unitSet)).build();
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to store Unit Converter list", e);
		}
		finally
		{
			dbIo.close();
		}
	}

	static UnitConverterSet map(ApiUnitConverter euc) throws DbException
	{
		try
		{
			UnitConverterSet unitConverterSet = new UnitConverterSet();
			UnitConverter unitConverter = null;
			EngineeringUnit fromEU = new EngineeringUnit(euc.getFromAbbr(), "", "", "");
			EngineeringUnit toEU = new EngineeringUnit(euc.getToAbbr(), "", "", "");
			switch(euc.getAlgorithm().toUpperCase())
			{
				case "POLY-5":
					unitConverter = new Poly5Converter(fromEU, toEU);
					break;
				case "LINEAR":
					unitConverter = new LinearConverter(fromEU, toEU);
					break;
				case "COMPOSITE":
					unitConverter = CompositeConverter.build(fromEU, toEU);
					break;
				case "NULL":
					unitConverter = new NullConverter(fromEU, toEU);
					break;
				case "USGS":
					unitConverter = new UsgsStdConverter(fromEU, toEU);
					break;
				default:
					throw new DbException("Unknown algorithm: " + euc.getAlgorithm());
			}
			double[] coeffs = new double[6];
			coeffs[0] = euc.getA() == null ? 0.0 : euc.getA();
			coeffs[1] = euc.getB() == null ? 0.0 : euc.getB();
			coeffs[2] = euc.getC() == null ? 0.0 : euc.getC();
			coeffs[3] = euc.getD() == null ? 0.0 : euc.getD();
			coeffs[4] = euc.getE() == null ? 0.0 : euc.getE();
			coeffs[5] = euc.getF() == null ? 0.0 : euc.getF();
			unitConverter.setCoefficients(coeffs);
			UnitConverterDb unitConverterDb = new UnitConverterDb(euc.getFromAbbr(), euc.getToAbbr());
			if (euc.getUcId() != null)
			{
				unitConverterDb.setId(DbKey.createDbKey(euc.getUcId()));
			}
			else
			{
				unitConverterDb.setId(DbKey.NullKey);
			}

			unitConverterDb.execConverter = unitConverter;
			unitConverterDb.algorithm = euc.getAlgorithm();
			unitConverterDb.coefficients[0] = euc.getA() == null ? 0.0 : euc.getA();
			unitConverterDb.coefficients[1] = euc.getB() == null ? 0.0 : euc.getB();
			unitConverterDb.coefficients[2] = euc.getC() == null ? 0.0 : euc.getC();
			unitConverterDb.coefficients[3] = euc.getD() == null ? 0.0 : euc.getD();
			unitConverterDb.coefficients[4] = euc.getE() == null ? 0.0 : euc.getE();
			unitConverterDb.coefficients[5] = euc.getF() == null ? 0.0 : euc.getF();
			unitConverterSet.addDbConverter(unitConverterDb);
			unitConverterSet.prepareForExec();
			unitConverterSet.addExecConverter(unitConverter);
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
	public Response deleteEUConv(@QueryParam("euconvid") Long id) throws DbException, WebAppException
	{
		if (id == null)
		{
			throw new WebAppException(HttpServletResponse.SC_BAD_REQUEST, "Missing required euconvid parameter");
		}

		try
		{
			dbIo = getLegacyDatabase();
			dbIo.deleteUnitConverterSet(id);
			return Response.status(HttpServletResponse.SC_OK).entity("EUConv with id=" + id + " deleted").build();
		}
		catch(DatabaseException e)
		{
			throw new DbException("Unable to delete Unit Converter", e);
		}
		finally
		{
			dbIo.close();
		}
	}
}
