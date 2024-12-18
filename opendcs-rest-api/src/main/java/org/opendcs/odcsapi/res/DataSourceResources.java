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

import decodes.db.DataSource;
import decodes.db.DataSourceList;
import decodes.db.DatabaseException;
import decodes.db.DatabaseIO;
import decodes.sql.DbKey;
import org.opendcs.odcsapi.beans.ApiDataSource;
import org.opendcs.odcsapi.beans.ApiDataSourceGroupMember;
import org.opendcs.odcsapi.beans.ApiDataSourceRef;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.sec.AuthorizationCheck;

@Path("/")
public class DataSourceResources extends OpenDcsResource
{
	@Context HttpHeaders httpHeaders;

	@GET
	@Path("datasourcerefs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getDataSourceRefs() throws DbException
	{
		try
		{
			DatabaseIO dbio = getLegacyDatabase();
			DataSourceList dsl = new DataSourceList();
			dbio.readDataSourceList(dsl);
			return Response.status(HttpServletResponse.SC_OK).entity(map(dsl)).build();
		}
		catch (DatabaseException ex)
		{
			throw new DbException("Error reading data source list: " + ex);
		}
	}

	static ArrayList<ApiDataSourceRef> map(DataSourceList dsl)
	{
		ArrayList<ApiDataSourceRef> ret = new ArrayList<>();
		for(DataSource ds : dsl.getList())
		{
			ApiDataSourceRef adr = new ApiDataSourceRef();
			if (ds.getId() != null)
			{
				adr.setDataSourceId(ds.getId().getValue());
			}
			else
			{
				adr.setDataSourceId(DbKey.NullKey.getValue());
			}
			adr.setName(ds.getName());
			adr.setType(ds.dataSourceType);
			adr.setUsedBy(ds.numUsedBy);
			adr.setArguments(map(ds.getArguments()));
			ret.add(adr);
		}
		return ret;
	}

	static String map(Properties props)
	{
		if (props == null || props.isEmpty())
			return null;

		StringBuilder retVal = new StringBuilder();
		for (Object key : props.keySet())
		{
			retVal.append(key).append("=").append(props.getProperty((String) key)).append(",");
		}
		return retVal.toString();
	}

	@GET
	@Path("datasource")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response geDataSource(@QueryParam("datasourceid") Long dataSourceId)
			throws WebAppException, DbException
	{
		if (dataSourceId == null)
		{
			throw new WebAppException(ErrorCodes.MISSING_ID,
					"Missing required datasourceid parameter.");
		}

		try
		{
			DatabaseIO dbio = getLegacyDatabase();
			DataSource ds = new DataSource(DbKey.createDbKey(dataSourceId));
			dbio.readDataSource(ds);

			if (ds.getName() == null)
			{
				throw new WebAppException(ErrorCodes.NO_SUCH_OBJECT,
						"No such DECODES data source with id=" + dataSourceId + ".");
			}
			ApiDataSource ret = map(ds);
			return Response.status(HttpServletResponse.SC_OK).entity(ret).build();
		}
		catch (DatabaseException ex)
		{
			throw new DbException("Error reading data source: " + ex);
		}
	}

	static ApiDataSource map(DataSource ds)
	{
		if (ds == null)
			return null;
		ApiDataSource ads = new ApiDataSource();
		if (ds.getId() != null)
		{
			ads.setDataSourceId(ds.getId().getValue());
		}
		else
		{
			ads.setDataSourceId(DbKey.NullKey.getValue());
		}
		ads.setName(ds.getName());
		ads.setType(ds.dataSourceType);
		ads.setProps(ds.getArguments());
		ads.setGroupMembers(map(ds.groupMembers));
		return ads;
	}

	static ArrayList<ApiDataSourceGroupMember> map(Vector<DataSource> groupMembers)
	{
		if (groupMembers == null)
		{
			return new ArrayList<>();
		}
		ArrayList<ApiDataSourceGroupMember> ret = new ArrayList<>();
		for(DataSource ds : groupMembers)
		{
			ApiDataSourceGroupMember ads = new ApiDataSourceGroupMember();
			if (ds.getId() != null)
			{
				ads.setDataSourceId(ds.getId().getValue());
			}
			else
			{
				ads.setDataSourceId(DbKey.NullKey.getValue());
			}
			ads.setDataSourceName(ds.getName());
			ret.add(ads);
		}
		return ret;
	}

	@POST
	@Path("datasource")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response postDatasource(ApiDataSource datasource) throws DbException, WebAppException
	{
		if (datasource == null)
		{
			throw new WebAppException(HttpServletResponse.SC_BAD_REQUEST,
					"Missing required data source object.");
		}
		try
		{
			DatabaseIO dbio = getLegacyDatabase();
			DataSource source = map(datasource);
			dbio.writeDataSource(source);
			return Response.status(HttpServletResponse.SC_OK)
					.entity(map(source))
					.build();
		}
		catch (DatabaseException ex)
		{
			throw new DbException("Error writing data source: " + ex);
		}
	}

	static DataSource map(ApiDataSource ads) throws DatabaseException
	{
		DataSource ds = new DataSource();
		if (ads.getDataSourceId() != null)
		{
			ds.setId(DbKey.createDbKey(ads.getDataSourceId()));
		}
		else
		{
			ds.setId(DbKey.NullKey);
		}
		ds.setName(ads.getName());
		ds.dataSourceType = ads.getType();
		ds.arguments = ads.getProps();
		ds.numUsedBy = ads.getUsedBy();
		ds.groupMembers = map(ads.getGroupMembers());
		return ds;
	}

	static Vector<DataSource> map(ArrayList<ApiDataSourceGroupMember> groupMembers)
	{
		Vector<DataSource> ret = new Vector<>();
		if (groupMembers == null)
		{
			return ret;
		}
		for(ApiDataSourceGroupMember ads : groupMembers)
		{
			DataSource ds = new DataSource(DbKey.createDbKey(ads.getDataSourceId()));
			ds.setName(ads.getDataSourceName());
			ret.add(ds);
		}
		return ret;
	}

	@DELETE
	@Path("datasource")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response deleteDatasource(@QueryParam("datasourceid") Long datasourceId) throws DbException, WebAppException
	{
		try
		{
			if (datasourceId == null)
			{
				throw new WebAppException(ErrorCodes.MISSING_ID, "Missing required datasourceid parameter.");
			}
			DatabaseIO dao = getLegacyDatabase();
			DataSource ds = new DataSource(DbKey.createDbKey(datasourceId));
			dao.readDataSource(ds);
			if (ds.getName() == null)
			{
				return Response.status(HttpServletResponse.SC_NOT_FOUND)
						.entity("No such data source with ID " + datasourceId).build();
			}

			if (ds.numUsedBy > 0)
			{
				return Response.status(HttpServletResponse.SC_METHOD_NOT_ALLOWED)
						.entity(" Cannot delete datasource with ID " + datasourceId
								+ " because it is used by the following number of routing specs: "
								+ ds.numUsedBy).build();
			}

			dao.deleteDataSource(ds);
			return Response.status(HttpServletResponse.SC_OK)
					.entity("Datasource with ID " + datasourceId + " deleted").build();
		}
		catch (DatabaseException ex)
		{
			throw new DbException("Error deleting data source: " + ex);
		}
	}


}
