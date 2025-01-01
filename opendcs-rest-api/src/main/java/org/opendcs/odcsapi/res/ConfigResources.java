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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

import decodes.db.ConfigSensor;
import decodes.db.DatabaseException;
import decodes.db.DatabaseIO;
import decodes.db.DecodesScript;
import decodes.db.DecodesScriptException;
import decodes.db.EngineeringUnit;
import decodes.db.PlatformConfig;
import decodes.db.PlatformConfigList;
import decodes.db.Poly5Converter;
import decodes.db.ScriptSensor;
import decodes.db.UnitConverter;
import decodes.db.UnitConverterDb;
import decodes.sql.DbKey;
import org.opendcs.odcsapi.beans.ApiConfigRef;
import org.opendcs.odcsapi.beans.ApiConfigScript;
import org.opendcs.odcsapi.beans.ApiConfigScriptSensor;
import org.opendcs.odcsapi.beans.ApiConfigSensor;
import org.opendcs.odcsapi.beans.ApiPlatformConfig;
import org.opendcs.odcsapi.beans.ApiUnitConverter;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.sec.AuthorizationCheck;

@Path("/")
public class ConfigResources extends OpenDcsResource
{
	@Context HttpHeaders httpHeaders;

	@GET
	@Path("configrefs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getConfigRefs() throws DbException
	{
		try
		{
			DatabaseIO dbIo = getLegacyDatabase();
			PlatformConfigList configList = new PlatformConfigList();
			dbIo.readConfigList(configList);
			return Response.status(HttpServletResponse.SC_OK).entity(map(configList)).build();
		}
		catch (DatabaseException ex)
		{
			throw new DbException("Error reading config list", ex);
		}
	}

	static List<ApiConfigRef> map(PlatformConfigList configList)
	{
		List<ApiConfigRef> configRefs = new ArrayList<>();
		for (PlatformConfig config : configList.values())
		{
			ApiConfigRef configRef = new ApiConfigRef();
			if (config.getId() != null)
			{
				configRef.setConfigId(config.getId().getValue());
			}
			else
			{
				configRef.setConfigId(DbKey.NullKey.getValue());
			}
			configRef.setName(config.getName());
			configRef.setNumPlatforms(config.numPlatformsUsing);
			configRef.setDescription(config.description);
			configRefs.add(configRef);
		}
		return configRefs;
	}

	@GET
	@Path("config")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getConfig(@QueryParam("configid") Long configId) throws WebAppException, DbException
	{
		if (configId == null)
		{
			throw new WebAppException(ErrorCodes.MISSING_ID,
					"Missing required configid parameter.");
		}

		try
		{
			DatabaseIO dbIo = getLegacyDatabase();
			PlatformConfig config = new PlatformConfig();
			config.setId(DbKey.createDbKey(configId));
			dbIo.readConfig(config); // TODO: This method does not return any data. Investigate why.
			return Response.status(HttpServletResponse.SC_OK).entity(map(config)).build();
		}
		catch (DatabaseException ex)
		{
			throw new DbException("Error reading config", ex);
		}
	}

	static ApiPlatformConfig map(PlatformConfig config)
	{
		ApiPlatformConfig apiConfig = new ApiPlatformConfig();
		if (config.getId() != null)
		{
			apiConfig.setConfigId(config.getId().getValue());
		}
		else
		{
			apiConfig.setConfigId(DbKey.NullKey.getValue());
		}
		apiConfig.setName(config.getName());
		apiConfig.setNumPlatforms(config.numPlatformsUsing);
		apiConfig.setDescription(config.description);
		return apiConfig;
	}

	@POST
	@Path("config")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response postConfig(ApiPlatformConfig config) throws DbException
	{
		try
		{
			DatabaseIO dbIo = getLegacyDatabase();
			PlatformConfig pc = map(config);
			dbIo.writeConfig(pc);
			return Response.status(HttpServletResponse.SC_OK)
					.entity(map(pc))
					.build();
		}
		catch (DatabaseException ex)
		{
			throw new DbException("Error saving config", ex);
		}
	}

	static PlatformConfig map(ApiPlatformConfig config) throws DbException
	{
		try
		{
			PlatformConfig pc = new PlatformConfig(config.getName());
			if (config.getConfigId() != null)
			{
				pc.setId(DbKey.createDbKey(config.getConfigId()));
			}
			else
			{
				pc.setId(DbKey.NullKey);
			}
			pc.description = config.getDescription();
			pc.numPlatformsUsing = config.getNumPlatforms();

			pc.configName = config.getName();
			pc.decodesScripts = map(config.getScripts(), pc);
			for (ApiConfigSensor sensor : config.getConfigSensors())
			{
				ConfigSensor configSensor = new ConfigSensor(null, sensor.getSensorNumber());
				configSensor.sensorName = sensor.getSensorName();
				configSensor.platformConfig = pc;
				pc.addSensor(configSensor);
			}

			return pc;
		}
		catch (DatabaseException ex)
		{
			throw new DbException("Error mapping platform config", ex);
		}
	}

	static Vector<DecodesScript> map(List<ApiConfigScript> scripts, PlatformConfig config) throws DbException
	{
		if (scripts == null)
		{
			return new Vector<>();
		}

		try
		{
			Vector<DecodesScript> decodesScripts = new Vector<>();
			for(ApiConfigScript script : scripts)
			{
				DecodesScript.DecodesScriptBuilder dsb = DecodesScript.empty();
				dsb.platformConfig(config);
				dsb.scriptName(script.getName());
				DecodesScript ds = dsb.build();
				for (ApiConfigScriptSensor sensor : script.getScriptSensors())
				{
					ds.addScriptSensor(map(sensor));
				}
				decodesScripts.add(ds);
			}
			return decodesScripts;
		}
		catch(DecodesScriptException | IOException | DatabaseException ex)
		{
			throw new DbException("Error mapping scripts", ex);
		}
	}

	static ScriptSensor map(ApiConfigScriptSensor sensor) throws DatabaseException
	{
		ScriptSensor scriptSensor = new ScriptSensor(null, sensor.getSensorNumber());
		scriptSensor.execConverter = map(sensor.getUnitConverter());
		UnitConverterDb rawConv = new UnitConverterDb(sensor.getUnitConverter().getFromAbbr(),
				sensor.getUnitConverter().getToAbbr());
		rawConv.algorithm = sensor.getUnitConverter().getAlgorithm();
		if (sensor.getUnitConverter().getUcId() != null)
		{
			rawConv.setId(DbKey.createDbKey(sensor.getUnitConverter().getUcId()));
		}
		else
		{
			rawConv.setId(DbKey.NullKey);
		}
		ApiUnitConverter uc = sensor.getUnitConverter();
		rawConv.coefficients = coefficientMap(uc);
		scriptSensor.rawConverter = rawConv;
		if (sensor.getUnitConverter().getUcId() != null)
		{
			scriptSensor.setUnitConverterId(DbKey.createDbKey(sensor.getUnitConverter().getUcId()));
		}
		else
		{
			scriptSensor.setUnitConverterId(DbKey.NullKey);
		}
		return scriptSensor;
	}

	static UnitConverter map(ApiUnitConverter unitConverter)
	{
		EngineeringUnit from = EngineeringUnit.getEngineeringUnit(unitConverter.getFromAbbr());
		EngineeringUnit to = EngineeringUnit.getEngineeringUnit(unitConverter.getToAbbr());
		Poly5Converter pc = new Poly5Converter(from, to);
		pc.setCoefficients(coefficientMap(unitConverter));
		return pc;
	}

	static double[] coefficientMap(ApiUnitConverter unitConverter)
	{
		double[] coeffs = new double[6];
		coeffs[0] = unitConverter.getA() != null ? unitConverter.getA() : 0.0;
		coeffs[1] = unitConverter.getB() != null ? unitConverter.getB() : 0.0;
		coeffs[2] = unitConverter.getC() != null ? unitConverter.getC() : 0.0;
		coeffs[3] = unitConverter.getD() != null ? unitConverter.getD() : 0.0;
		coeffs[4] = unitConverter.getE() != null ? unitConverter.getE() : 0.0;
		coeffs[5] = unitConverter.getF() != null ? unitConverter.getF() : 0.0;
		return coeffs;
	}


	@DELETE
	@Path("config")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response deleteConfig(@QueryParam("configid") Long configId) throws DbException, WebAppException
	{
		if (configId == null)
		{
			throw new WebAppException(HttpServletResponse.SC_BAD_REQUEST,
					"Missing required configid parameter.");
		}

		try
		{
			DatabaseIO dbIo = getLegacyDatabase();
			PlatformConfig pc = new PlatformConfig();
			pc.setId(DbKey.createDbKey(configId));
			dbIo.readConfig(pc);

			if (pc.numPlatformsUsing > 0)
			{
				return Response.status(HttpServletResponse.SC_METHOD_NOT_ALLOWED)
						.entity(" Cannot delete config with ID "
								+ configId + " because it is used by one or more platforms.")
						.build();
			}

			dbIo.deleteConfig(pc);
			return Response.status(HttpServletResponse.SC_OK)
					.entity("Config with ID " + configId + " deleted")
					.build();
		}
		catch (DatabaseException ex)
		{
			throw new DbException("Error deleting config", ex);
		}
	}
}
