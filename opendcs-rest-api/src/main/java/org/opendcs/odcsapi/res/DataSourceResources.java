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
import decodes.sql.DataSourceListIO;
import decodes.sql.DbKey;
import org.opendcs.odcsapi.beans.ApiDataSource;
import org.opendcs.odcsapi.beans.ApiDataSourceGroupMember;
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
			DataSourceListIO dao = new DataSourceListIO(null);
			DataSourceList dsl = new DataSourceList();
			dao.read(dsl);

			return Response.status(HttpServletResponse.SC_OK).entity(dsl).build();
		}
		catch (DatabaseException | SQLException ex)
		{
			throw new DbException("Error reading data source list: " + ex);
		}
	}

	@GET
	@Path("datasource")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response geDataSource(@QueryParam("datasourceid") Long dataSourceId)
		throws WebAppException, DbException
	{
		if (dataSourceId == null)
			throw new WebAppException(ErrorCodes.MISSING_ID, 
				"Missing required datasourceid parameter.");
		
		try
		{
			DataSourceListIO dao = new DataSourceListIO(null);
			ApiDataSource ret = map(dao.readDS(DbKey.createDbKey(dataSourceId)));
			if (ret == null)
				throw new WebAppException(ErrorCodes.NO_SUCH_OBJECT, 
					"No such DECODES data source with id=" + dataSourceId + ".");
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
		ads.setDataSourceId(ds.getId().getValue());
		ads.setName(ds.getName());
		ads.setType(ds.dataSourceType);
		ads.setProps(ds.getArguments());
		ads.setGroupMembers(map(ds.groupMembers));
		return ads;
	}

	static ArrayList<ApiDataSourceGroupMember> map(Vector<DataSource> groupMembers)
	{
		if (groupMembers == null)
			return new ArrayList<>();
		ArrayList<ApiDataSourceGroupMember> ret = new ArrayList<>();
		for(DataSource ds : groupMembers)
		{
			ApiDataSourceGroupMember ads = new ApiDataSourceGroupMember();
			ads.setDataSourceId(ds.getId().getValue());
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
	public Response postDatasource(ApiDataSource datasource) throws DbException, SQLException
	{
		try
		{
			DataSourceListIO dao = new DataSourceListIO(null);
			dao.write(map(datasource));
			return Response.status(HttpServletResponse.SC_OK)
					.entity(String.format("Successfully stored data source with ID: %s", datasource.getDataSourceId()))
					.build();
		}
		catch (DatabaseException ex)
		{
			throw new DbException("Error writing data source: " + ex);
		}
	}

	static DataSource map(ApiDataSource ads)
	{
		DataSource ds = new DataSource(DbKey.createDbKey(ads.getDataSourceId()));
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
			return ret;
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
	public Response deleteDatasource(@QueryParam("datasourceid") Long datasourceId) throws DbException
	{
		try
		{
			DataSourceListIO dao = new DataSourceListIO(null);
			DataSource ds = dao.readDS(DbKey.createDbKey(datasourceId));
			if (ds == null)
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

			dao.delete(ds);
			return Response.status(HttpServletResponse.SC_OK)
					.entity("Datasource with ID " + datasourceId + " deleted").build();
		}
		catch (DatabaseException | SQLException ex)
		{
			throw new DbException("Error deleting data source: " + ex);
		}
	}


}
