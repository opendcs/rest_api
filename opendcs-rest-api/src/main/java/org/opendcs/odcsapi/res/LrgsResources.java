/*
 *  Copyright 2024 OpenDCS Consortium and its Contributors
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

import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
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
import decodes.db.NetworkList;
import decodes.db.NetworkListEntry;
import decodes.sql.DbKey;
import decodes.tsdb.TimeSeriesDb;
import org.opendcs.odcsapi.beans.ApiDataSource;
import org.opendcs.odcsapi.beans.ApiDataSourceGroupMember;
import org.opendcs.odcsapi.beans.ApiDataSourceRef;
import org.opendcs.odcsapi.beans.ApiNetList;
import org.opendcs.odcsapi.beans.ApiNetListItem;
import org.opendcs.odcsapi.beans.ApiRawMessage;
import org.opendcs.odcsapi.beans.ApiRawMessageBlock;
import org.opendcs.odcsapi.beans.ApiSearchCrit;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.DatabaseItemNotFoundException;
import org.opendcs.odcsapi.errorhandling.MissingParameterException;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.lrgsclient.ApiLddsClient;
import org.opendcs.odcsapi.lrgsclient.ClientConnectionCache;
import org.opendcs.odcsapi.lrgsclient.DdsProtocolError;
import org.opendcs.odcsapi.lrgsclient.DdsServerError;
import org.opendcs.odcsapi.lrgsclient.LrgsErrorCode;
import org.opendcs.odcsapi.sec.AuthorizationCheck;
import org.opendcs.odcsapi.util.ApiPropertiesUtil;

/**
 * Resources for interacting with an LRGS for DCP messages and status.
 */
@Path("/")
public class LrgsResources extends OpenDcsResource
{
	@Context private HttpServletRequest request;
	@Context private HttpHeaders httpHeaders;


	@POST
	@Path("searchcrit")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response postSearchCriteria(ApiSearchCrit searchcrit)
	{
		HttpSession session = request.getSession(true);
		// If session already contains an LddsClient, close and delete it.
		// I.e., if a message retrieval is already in progress, the new searchcrit
		// cancels it. A new client will have to be started on the next GET messages.
		ClientConnectionCache.getInstance().removeApiLddsClient(session.getId());
		String searchCritSessionAttribute = ApiSearchCrit.ATTRIBUTE;
		session.setAttribute(searchCritSessionAttribute, searchcrit);

		return Response.status(HttpServletResponse.SC_CREATED).entity(
				"Searchcrit cached for current session.").build();
	}

	@GET
	@Path("searchcrit")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public ApiSearchCrit getSearchCriteria() throws WebAppException
	{
		HttpSession session = request.getSession(false);
		if(session == null)
		{
			throw new DatabaseItemNotFoundException("No searchcrit is currently stored.");
		}
		String sessionAttribute = ApiSearchCrit.ATTRIBUTE;
		ApiSearchCrit searchcrit = (ApiSearchCrit) session.getAttribute(sessionAttribute);
		if (searchcrit == null)
		{
			throw new DatabaseItemNotFoundException("No searchcrit is currently stored.");
		}
		return searchcrit;
	}

	@GET
	@Path("messages")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public ApiRawMessageBlock getMessages() throws WebAppException
	{
		HttpSession session = request.getSession(true);
		String sessionAttribute = ApiSearchCrit.ATTRIBUTE;
		ApiSearchCrit searchcrit = (ApiSearchCrit) session.getAttribute(sessionAttribute);
		if (searchcrit == null)
		{
			throw new DatabaseItemNotFoundException("POST searchcrit required prior to GET messages.");
		}

		ApiDataSource dataSource = null;
		ClientConnectionCache clientConnectionCache = ClientConnectionCache.getInstance();
		ApiLddsClient client = clientConnectionCache.getApiLddsClient(session.getId())
				.orElse(null);

		String action = "connecting";
		// See if there is already an ApiLddsClient object in the userToken.
		// If so, skip the stuff below where I connect & send netlists & searchcrit.
		// Just skip to getting the next message block
		if (client == null)
		{
			DatabaseIO dbIo = null;
			// This is a new retrieval. Create client, send netlists & searchcrit.
			dbIo = getLegacyDatabase();
			try
			{
				String host;
				String s;
				dataSource = getApiDataSource(null);
				if (dataSource.getProps() == null)
				{
					host = null;
					s = null;
				}
				else
				{
					host = ApiPropertiesUtil.getIgnoreCase(dataSource.getProps(), "host");
					s = ApiPropertiesUtil.getIgnoreCase(dataSource.getProps(),"port");
				}
				if (host == null)
					host = dataSource.getName();
				int port = 16003;
				if (s != null)
				{
					try
					{
						port = Integer.parseInt(s.trim());
					}
					catch(NumberFormatException ex)
					{
						port = 16003;
					}
				}

				client = new ApiLddsClient(host, port);
				client.connect();
				clientConnectionCache.setApiLddsClient(client, session.getId());

				String username = dataSource.getProps().getProperty("username");
				String password = dataSource.getProps().getProperty("password");
				if (password == null)
					client.sendHello(username);
				else
					client.sendAuthHello(username, password);

				for(String nlname : searchcrit.getNetlistNames())
				{
					NetworkList netList = new NetworkList();
					netList.name = nlname;
					dbIo.readNetworkList(netList);
					Long nlId = netList.getId().getValue();
					if (nlId != null && nlId != DbKey.NullKey.getValue())
					{
						action = "sending netlist " + nlname + ", id=" + nlId;
						ApiNetList nl = map(netList);
						client.sendNetList(nl);
					}
				}

				action = "sending searchcrit";

				client.sendSearchCrit(searchcrit, dbIo);
			}
			catch (DbException | DatabaseException ex)
			{
				clientConnectionCache.removeApiLddsClient(session.getId());
				throw new WebAppException(HttpServletResponse.SC_BAD_REQUEST,
						"There was an error getting messages from the LRGS client: ", ex);
			}
			catch (UnknownHostException ex)
			{
				clientConnectionCache.removeApiLddsClient(session.getId());
				throw new WebAppException(HttpServletResponse.SC_PRECONDITION_FAILED,
						"Cannot connect to LRGS data source " + dataSource.getName() + ": " + ex);
			}
			catch (IOException ex)
			{
				clientConnectionCache.removeApiLddsClient(session.getId());
				throw new WebAppException(HttpServletResponse.SC_PRECONDITION_FAILED,
						"IO Error on LRGS data source " + dataSource.getName() + ": " + ex);
			}
			catch (DdsProtocolError | DdsServerError ex)
			{
				clientConnectionCache.removeApiLddsClient(session.getId());
				String em = "Error while " + action + ": " + ex;
				throw new WebAppException(HttpServletResponse.SC_CONFLICT, em);
			}
			finally
			{
				if (dbIo != null)
				{
					dbIo.close();
				}
			}
		}
		// ELSE use the existing client object already initialized.

		try
		{
			action = "getting message block";
			return client.getMsgBlockExt(60);
		}
		catch (IOException ex)
		{
			clientConnectionCache.removeApiLddsClient(session.getId());
			throw new WebAppException(HttpServletResponse.SC_PRECONDITION_FAILED, "IO Error on LRGS data source "
					+ dataSource.getName() + ": " + ex);
		}
		catch (DdsProtocolError ex)
		{
			clientConnectionCache.removeApiLddsClient(session.getId());
			throw new WebAppException(HttpServletResponse.SC_CONFLICT, "Error while " + action + ": " + ex);
		}
		catch (DdsServerError ex)
		{
			if (ex.Derrno == LrgsErrorCode.DUNTIL)
			{
				// The retrieval is now finished. Close the client
				clientConnectionCache.removeApiLddsClient(session.getId());

				ApiRawMessageBlock ret = new ApiRawMessageBlock();
				ret.setMoreToFollow(false);
				return ret;
			}
			// Any other server error returns an error.
			clientConnectionCache.removeApiLddsClient(session.getId());
			throw new WebAppException(HttpServletResponse.SC_CONFLICT, "Error while " + action + ": " + ex);
		}
	}

	static ApiNetList map(NetworkList netList)
	{
		ApiNetList ret = new ApiNetList();
		ret.setName(netList.name);
		ret.setLastModifyTime(netList.lastModifyTime);
		if (netList.getId() != null)
		{
			ret.setNetlistId(netList.getId().getValue());
		}
		else
		{
			ret.setNetlistId(DbKey.NullKey.getValue());
		}
		ret.setSiteNameTypePref(netList.siteNameTypePref);
		ret.setTransportMediumType(netList.transportMediumType);
		ret.setItems(map(netList.networkListEntries));
		return ret;
	}

	static HashMap<String, ApiNetListItem> map(HashMap<String, NetworkListEntry> listEntries)
	{
		HashMap<String, ApiNetListItem> ret = new HashMap<>();
		for (Map.Entry<String, NetworkListEntry> entry : listEntries.entrySet())
		{
			NetworkListEntry nle = listEntries.get(entry.getKey());
			ApiNetListItem item = new ApiNetListItem();
			item.setPlatformName(nle.getPlatformName());
			item.setDescription(nle.getDescription());
			item.setTransportId(nle.transportId);
			ret.put(entry.getKey(), item);
		}
		return ret;
	}

	/**
	 *
	 * @param dsName The name of the data source to retrieve
	 * @return The data source
	 * @throws DbException If there is an error getting the data source
	 * @throws WebAppException If there is no usable LRGS data source
	 */
	private ApiDataSource getApiDataSource(String dsName)
			throws DbException, WebAppException
	{
		TimeSeriesDb tsdb = getLegacyTimeseriesDB();
		DatabaseIO dbIo = null;
		try
		{
			dbIo = getLegacyDatabase();
			ApiDataSource dataSource = null;

			Properties tsdbProps = new Properties();

			tsdb.readTsdbProperties(tsdb.getConnection());
			for (Object keyObj : Collections.list(tsdb.getPropertyNames()))
			{
				String key = (String) keyObj;
				tsdbProps.setProperty(key, tsdb.getProperty(key));
			}
			if (dsName == null)
				dsName = tsdbProps.getProperty("api.datasource");
			if (dsName != null)
			{
				DbKey dsId = dbIo.lookupDataSourceId(dsName);
				if (dsId != DbKey.NullKey)
				{
					DataSource dataSource1 = new DataSource(dsId);
					dbIo.readDataSource(dataSource1);
					dataSource = map(dataSource1);
				}
			}
			if (dataSource == null)
			{
				// No api.datasource specified, or it doesn't exist. Try the first LRGS
				// datasource in the list.
				DataSourceList dsList = new DataSourceList();
				dbIo.readDataSourceList(dsList);
				ArrayList<ApiDataSourceRef> dataSourceRefs = map(dsList);
				for(ApiDataSourceRef dsr : dataSourceRefs)
					if (dsr.getType().equalsIgnoreCase("lrgs"))
					{
						DataSource dataSource2 = new DataSource(DbKey.createDbKey(dsr.getDataSourceId()));
						dbIo.readDataSource(dataSource2);
						dataSource = map(dataSource2);
						break;
					}
			}
			if (dataSource == null)
				throw new WebAppException(HttpServletResponse.SC_PRECONDITION_FAILED,
						"No usable LRGS datasource: "
								+ "Create one, then define 'api.datasource' in TSDB properties.");
			return dataSource;
		}
		catch (DatabaseException | SQLException ex)
		{
			throw new DbException("Cannot get API data source", ex);
		}
		finally
		{
			if (dbIo != null)
			{
				dbIo.close();
			}
		}
	}

	static ArrayList<ApiDataSourceRef> map(DataSourceList dsList)
	{
		ArrayList<ApiDataSourceRef> ret = new ArrayList<>();
		for (DataSource ds : dsList.getList())
		{
			ApiDataSourceRef dsr = new ApiDataSourceRef();
			if (ds.getId() != null)
			{
				dsr.setDataSourceId(ds.getId().getValue());
			}
			else
			{
				dsr.setDataSourceId(DbKey.NullKey.getValue());
			}
			dsr.setType(ds.dataSourceType);
			dsr.setName(ds.getName());
			dsr.setArguments(ds.getDataSourceArg());
			dsr.setUsedBy(ds.numUsedBy);
			ret.add(dsr);
		}
		return ret;
	}

	static ApiDataSource map(DataSource source)
	{
		ApiDataSource ret = new ApiDataSource();
		if (source.getId() != null)
		{
			ret.setDataSourceId(source.getId().getValue());
		}
		else
		{
			ret.setDataSourceId(DbKey.NullKey.getValue());
		}
		ret.setName(source.getName());
		ret.setType(source.dataSourceType);
		ret.setProps(source.getArguments());
		ret.setUsedBy(source.numUsedBy);
		ret.setGroupMembers(map(source.groupMembers));
		return ret;
	}

	static ArrayList<ApiDataSourceGroupMember> map(Vector<DataSource> sources)
	{
		ArrayList<ApiDataSourceGroupMember> ret = new ArrayList<>();
		for (DataSource ds : sources)
		{
			ApiDataSourceGroupMember dsgm = new ApiDataSourceGroupMember();
			if (ds.getId() != null)
			{
				dsgm.setDataSourceId(ds.getId().getValue());
			}
			else
			{
				dsgm.setDataSourceId(DbKey.NullKey.getValue());
			}
			dsgm.setDataSourceName(ds.getName());
			ret.add(dsgm);
		}
		return ret;
	}

	@GET
	@Path("message")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public ApiRawMessage getMessage(@QueryParam("tmid") String tmid, @QueryParam("tmtype") String tmtype)
			throws WebAppException
	{
		if (tmid == null)
		{
			throw new MissingParameterException("Missing required tmid argument.");
		}

		// Create and save searchcrit for tmid for last 8 hours
		ApiSearchCrit searchcrit = new ApiSearchCrit();
		searchcrit.getPlatformIds().add(tmid);
		searchcrit.setSince("now - 12 hours");
		searchcrit.setUntil("now");
		postSearchCriteria(searchcrit);

		// Get a message block and return the first (most recent) message in it.
		ApiRawMessageBlock mb = getMessages();
		if (mb.getMessages().isEmpty())
		{
			throw new DatabaseItemNotFoundException("No message for '"
					+ tmid + "' in last 12 hours.");
		}

		// This method gets a SINGLE message, so we're finished with client now.
		HttpSession session = request.getSession(false);
		if(session != null)
		{
			ClientConnectionCache.getInstance().removeApiLddsClient(session.getId());
		}
		return mb.getMessages().get(0);
	}

	@GET
	@Path("lrgsstatus")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getLrgsStatus(@QueryParam("source") String source)
			throws WebAppException
	{
		ApiDataSource dataSource = null;
		ApiLddsClient client = null;

		String action = "connecting";

		try
		{
			dataSource = getApiDataSource(source);
			String host;
			if (dataSource.getProps() == null)
			{
				host = null;
			}
			else
			{
				host = ApiPropertiesUtil.getIgnoreCase(dataSource.getProps(), "host");
			}
			if (host == null)
			{
				host = dataSource.getName();
			}
			int port = 16003;
			String s;
			if (dataSource.getProps() == null)
			{
				s = null;
			}
			else
			{
				s = ApiPropertiesUtil.getIgnoreCase(dataSource.getProps(), "port");
			}
			if (s != null)
			{
				try
				{
					port = Integer.parseInt(s.trim());
				}
				catch(NumberFormatException ex)
				{
					port = 16003;
				}
			}

			client = new ApiLddsClient(host, port);
			client.connect();

			String username = dataSource.getProps().getProperty("username");
			String password = dataSource.getProps().getProperty("password");
			if (password == null)
				client.sendHello(username);
			else
				client.sendAuthHello(username, password);

			action = "getting LRGS status";
			return Response.status(HttpServletResponse.SC_OK).entity(client.getLrgsStatus()).build();
		}
		catch (DbException ex)
		{
			throw new WebAppException(HttpServletResponse.SC_BAD_REQUEST,
					"There was an error connecting to the decodes database", ex);
		}
		catch (UnknownHostException ex)
		{
			throw new WebAppException(HttpServletResponse.SC_PRECONDITION_FAILED, "Cannot connect to LRGS data source "
					+ dataSource.getName() + ": ", ex);
		}
		catch (IOException ex)
		{
			throw new WebAppException(HttpServletResponse.SC_PRECONDITION_FAILED, "IO Error on LRGS data source "
					+ dataSource.getName() + ": ", ex);
		}
		catch (DdsProtocolError | DdsServerError ex)
		{
			String em = "Error while " + action + ": ";
			throw new WebAppException(HttpServletResponse.SC_CONFLICT, em, ex);
		}
		finally
		{
			if (client != null)
				client.disconnect();
		}
	}
}