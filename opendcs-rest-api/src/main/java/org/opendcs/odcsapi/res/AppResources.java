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

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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

import decodes.sql.DbKey;
import decodes.tsdb.CompAppInfo;
import decodes.tsdb.ConstraintException;
import decodes.tsdb.DbIoException;
import decodes.tsdb.LockBusyException;
import decodes.tsdb.NoSuchObjectException;
import decodes.tsdb.TsdbCompLock;
import ilex.cmdline.StdAppSettings;
import opendcs.dai.LoadingAppDAI;
import org.opendcs.odcsapi.appmon.ApiEventClient;
import org.opendcs.odcsapi.beans.ApiAppEvent;
import org.opendcs.odcsapi.beans.ApiAppRef;
import org.opendcs.odcsapi.beans.ApiAppStatus;
import org.opendcs.odcsapi.beans.ApiLoadingApp;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.DatabaseItemNotFoundException;
import org.opendcs.odcsapi.errorhandling.MissingParameterException;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.lrgsclient.ClientConnectionCache;
import org.opendcs.odcsapi.util.ApiConstants;
import org.opendcs.odcsapi.util.ApiEnvExpander;
import org.opendcs.odcsapi.util.ApiPropertiesUtil;
import org.opendcs.odcsapi.util.ProcWaiterCallback;
import org.opendcs.odcsapi.util.ProcWaiterThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resources for editing, monitoring, stopping, and starting processes.
 */
@Path("/")
public final class AppResources extends OpenDcsResource
{
	private static final String NO_APP_FOUND = "No such app with ID: %s";
	private static final Logger LOGGER = LoggerFactory.getLogger(AppResources.class);

	@Context private HttpServletRequest request;
	@Context private HttpHeaders httpHeaders;

	@GET
	@Path("apprefs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	public Response getAppRefs() throws DbException
	{
		try (LoadingAppDAI dai = getLegacyDatabase().makeLoadingAppDAO())
		{
			List<ApiAppRef> ret = dai.listComputationApps(false)
					.stream()
					.map(AppResources::map)
					.collect(Collectors.toList());
			return Response.status(HttpServletResponse.SC_OK)
					.entity(ret).build();
		}
		catch (DbIoException ex)
		{
			throw new DbException("Unable to retrieve apps", ex);
		}
	}

	static ApiAppRef map(CompAppInfo app)
	{
		ApiAppRef ret = new ApiAppRef();
		ret.setAppId(app.getAppId().getValue());
		ret.setAppName(app.getAppName());
		ret.setAppType(app.getAppType());
		ret.setComment(app.getComment());
		ret.setLastModified(app.getLastModified());
		return ret;
	}

	@GET
	@Path("app")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_GUEST})
	public Response getApp(@QueryParam("appid") Long appId)
			throws WebAppException, DbException
	{
		if (appId == null)
		{
			throw new MissingParameterException("Missing required appid parameter.");
		}
		try (LoadingAppDAI dai = getLegacyDatabase().makeLoadingAppDAO())
		{
			return Response.status(HttpServletResponse.SC_OK)
					.entity(mapLoading(dai.getComputationApp(DbKey.createDbKey(appId)))).build();
		}
		catch (NoSuchObjectException e)
		{
			return Response.status(HttpServletResponse.SC_NOT_FOUND)
					.entity(String.format(NO_APP_FOUND, appId)).build();
		}
		catch (DbIoException ex)
		{
			throw new DbException(String.format(NO_APP_FOUND, appId), ex);
		}
	}

	@POST
	@Path("app")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	public Response postApp(ApiLoadingApp app)
			throws DbException
	{
		try (LoadingAppDAI dai = getLegacyDatabase().makeLoadingAppDAO())
		{
			CompAppInfo compApp = map(app);
			dai.writeComputationApp(compApp);
			return Response.status(HttpServletResponse.SC_CREATED)
					.entity(map(compApp))
					.build();
		}
		catch(DbIoException ex)
		{
			throw new DbException("Unable to store app", ex);
		}
	}

	static CompAppInfo map(ApiLoadingApp app)
	{
		CompAppInfo ret = new CompAppInfo();
		if (app.getAppId() != null)
		{
			ret.setAppId(DbKey.createDbKey(app.getAppId()));
		}
		else
		{
			ret.setAppId(DbKey.NullKey);
		}
		ret.setAppName(app.getAppName());
		ret.setComment(app.getComment());
		ret.setLastModified(app.getLastModified());
		ret.setProperties(app.getProperties());
		ret.setManualEditApp(app.isManualEditingApp());
		String appType = app.getProperties().getProperty("appType");
		if (appType == null)
		{
			ret.setProperty("appType", app.getAppType());
		}
		return ret;
	}

	@DELETE
	@Path("app")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	public Response deleteApp(@QueryParam("appid") Long appId)
			throws DbException, WebAppException
	{
		if (appId == null)
		{
			throw new MissingParameterException("Missing required appid parameter.");
		}

		try (LoadingAppDAI dai = getLegacyDatabase().makeLoadingAppDAO())
		{
			CompAppInfo app = dai.getComputationApp(DbKey.createDbKey(appId));
			if (app == null)
			{
				throw new DbException(String.format(NO_APP_FOUND, appId));
			}
			dai.deleteComputationApp(app);
			return Response.status(HttpServletResponse.SC_NO_CONTENT)
					.entity("appId with ID " + appId + " deleted").build();
		}
		catch (NoSuchObjectException e)
		{
			throw new DatabaseItemNotFoundException(String.format(NO_APP_FOUND, appId), e);
		}
		catch (DbIoException | ConstraintException ex)
		{
			throw new DbException(String.format(NO_APP_FOUND, appId), ex);
		}
	}

	@GET
	@Path("appstat")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	public Response getAppStatus() throws DbException
	{
		List<ApiAppStatus> ret = new ArrayList<>();
		try (LoadingAppDAI dai = getLegacyDatabase().makeLoadingAppDAO())
		{
			for (TsdbCompLock lock : dai.getAllCompProcLocks())
			{
				ret.add(map(dai, lock));
			}
			return Response.status(HttpServletResponse.SC_OK)
					.entity(ret).build();
		}
		catch (DbIoException ex)
		{
			throw new DbException("Unable to retrieve app status", ex);
		}
	}

	static ApiAppStatus map(LoadingAppDAI dai, TsdbCompLock lock) throws DbIoException
	{
		ApiAppStatus ret = new ApiAppStatus();
		ret.setAppId(lock.getAppId().getValue());
		ret.setAppName(lock.getAppName());
		ret.setHostname(lock.getHost());
		ret.setPid((long) lock.getPID());
		ret.setHeartbeat(lock.getHeartbeat());
		ret.setStatus(lock.getStatus());
		if (dai != null)
		{
			try {
				ApiLoadingApp app = mapLoading(dai.getComputationApp(lock.getAppId()));
				if (app.getProperties() == null || app.getProperties().getProperty("EventPort") == null)
				{
					throw new DbIoException("EventPort property not found");
				}
				ret.setEventPort(Integer.parseInt(app.getProperties().getProperty("EventPort")));
				ret.setAppType(app.getAppType());
			}
			catch (DbIoException | NoSuchObjectException | NumberFormatException ex)
			{
				throw new DbIoException("Error mapping app status", ex);
			}
		}
		return ret;
	}


	@GET
	@Path("appevents")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	public Response getAppEvents(@QueryParam("appid") Long appId)
			throws WebAppException, DbException
	{
		if (appId == null)
		{
			throw new MissingParameterException("Missing required appid parameter.");
		}

		HttpSession session = request.getSession(true);
		ClientConnectionCache clientConnectionCache = ClientConnectionCache.getInstance();
		Optional<ApiEventClient> cli = clientConnectionCache.getApiEventClient(appId, session.getId());
		ApiAppStatus appStat = null;
		try(LoadingAppDAI dai = getLegacyDatabase().makeLoadingAppDAO())
		{
			ApiEventClient apiEventClient = null;
			appStat = getAppStatus(dai, appId);
			if (appStat == null)
			{
				cli.ifPresent(c -> clientConnectionCache.removeApiEventClient(c, session.getId()));
				throw new DatabaseItemNotFoundException("appid " + appId
						+ " is not running (no lock found).");
			}
			else if (appStat.getPid() == null)
			{
				cli.ifPresent(c -> clientConnectionCache.removeApiEventClient(c, session.getId()));
				throw new DatabaseItemNotFoundException("appid " + appId
						+ " (" + appStat.getAppName() + ") is not running.");
			}
			else if (System.currentTimeMillis() - appStat.getHeartbeat().getTime() > 20000L)
			{
				cli.ifPresent(c -> clientConnectionCache.removeApiEventClient(c, session.getId()));
				throw new DatabaseItemNotFoundException("appid " + appId
						+ " (" + appStat.getAppName() + ") is not running (stale heartbeat).");
			}
			else if (appStat.getPid() != null && (!cli.isPresent() || appStat.getPid() != cli.get().getPid()))
			{
				// This means that the app was stopped and restarted since we last checked for events.
				// Close the old client and open a new one with the correct PID.
				cli.ifPresent(c -> clientConnectionCache.removeApiEventClient(c, session.getId()));

				Integer port = appStat.getEventPort();
				if (port == null)
				{
					return Response.status(HttpServletResponse.SC_OK)
							.entity(new ArrayList<ApiAppEvent>()).build();
				}
				apiEventClient = new ApiEventClient(appId, appStat.getHostname(), port, appStat.getAppName(), appStat.getPid());
				apiEventClient.connect();
				// NOTE: event client added to user token ONLY if connect succeeds.
				clientConnectionCache.addApiEventClient(apiEventClient, session.getId());
			}
			if(apiEventClient == null)
			{
				throw new DatabaseItemNotFoundException("No API Event Client found or created");
			}
			return Response.status(HttpServletResponse.SC_OK)
					.entity(apiEventClient.getNewEvents()).build();
		}
		catch(ConnectException ex)
		{
			throw new WebAppException(HttpServletResponse.SC_CONFLICT,
					String.format("Cannot connect to %s.", appStat.getAppName()), ex);
			// Connection failed, event client not added to user token
		}
		catch(IOException ex)
		{
			cli.ifPresent(c -> clientConnectionCache.removeApiEventClient(c, session.getId()));
			throw new WebAppException(HttpServletResponse.SC_CONFLICT,
					String.format("Event socket to %s closed by app", appStat.getAppId()), ex);
		}
	}

	@POST
	@Path("appstart")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	public Response postAppStart(@QueryParam("appid") Long appId)
			throws WebAppException, DbException
	{
		if (appId == null)
		{
			throw new MissingParameterException("appId parameter required for this operation.");
		}

		try (LoadingAppDAI dai = getLegacyDatabase().makeLoadingAppDAO())
		{
			// Retrieve ApiLoadingApp and ApiAppStatus
			ApiLoadingApp loadingApp = mapLoading(dai.getComputationApp(DbKey.createDbKey(appId)));
			ApiAppStatus appStat = getAppStatus(dai, appId);



			// Error if already running and heartbeat is current
			if (appStat != null && appStat.getPid() != null && appStat.getHeartbeat() != null
					&& (System.currentTimeMillis() - appStat.getHeartbeat().getTime() < 20000L))
				throw new WebAppException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
						"App id=" + appId + " (" + loadingApp.getAppName() + ") is already running.");

			// Error if no "startCmd" property
			String startCmd = ApiPropertiesUtil.getIgnoreCase(loadingApp.getProperties(), "startCmd");
			if (startCmd == null)
				throw new WebAppException(HttpServletResponse.SC_PRECONDITION_FAILED,
						"App id=" + appId + " (" + loadingApp.getAppName() + ") has no 'startCmd' property.");


			StdAppSettings settings = new StdAppSettings();

			int pid;

			if (appStat == null || appStat.getPid() == null)
			{
				pid = appId.intValue();
			}
			else
			{
				pid = appStat.getPid().intValue();
			}

			// Obtain a lock on the app with a processId chosen from the DB and standard hostname
			dai.obtainCompProcLock(dai.getComputationApp(DbKey.createDbKey(loadingApp.getAppId())),
					pid, settings.getHostName());

			// ProcWaiterThread runBackground to execute command, use callback.
			ProcWaiterCallback pwcb = (procName, obj, exitStatus) ->
			{
				ApiLoadingApp loadingApp1 = (ApiLoadingApp)obj;
				LOGGER.info("App Termination: app {} was terminated with exit status {}",
						loadingApp1.getAppName(), exitStatus);
			};

			ProcWaiterThread.runBackground(ApiEnvExpander.expand(startCmd), "App:" + loadingApp.getAppName(),
					pwcb, loadingApp);

			return Response.status(HttpServletResponse.SC_OK)
					.entity("App with ID " + appId + " (" + loadingApp.getAppName() + ") started.").build();
		}
		catch (DbIoException | NoSuchObjectException | IOException | LockBusyException ex)
		{
			throw new WebAppException(HttpServletResponse.SC_BAD_REQUEST,
					String.format("Error attempting to start appId=%s", appId), ex);
		}
	}

	static ApiLoadingApp mapLoading(CompAppInfo app)
	{
		ApiLoadingApp ret = new ApiLoadingApp();
		ret.setAppId(app.getAppId().getValue());
		ret.setAppName(app.getAppName());
		ret.setComment(app.getComment());
		ret.setLastModified(app.getLastModified());
		ret.setManualEditingApp(app.getManualEditApp());
		ret.setAppType(app.getAppType());
		ret.setProperties(app.getProperties());
		return ret;
	}

	@POST
	@Path("appstop")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ApiConstants.ODCS_API_ADMIN, ApiConstants.ODCS_API_USER})
	public Response postAppStop(@QueryParam("appid") Long appId)
			throws WebAppException, DbException
	{
		if (appId == null)
		{
			throw new MissingParameterException("appId parameter required for this operation.");
		}

		try (LoadingAppDAI dai = getLegacyDatabase().makeLoadingAppDAO())
		{
			// Retrieve ApiLoadingApp and ApiAppStatus
			ApiLoadingApp loadingApp = mapLoading(dai.getComputationApp(DbKey.createDbKey(appId)));

			ApiAppStatus appStat = getAppStatus(dai, appId);

			if (appStat == null || appStat.getPid() == null)
			{
				throw new DatabaseItemNotFoundException(
						"appId " + appId + "(" + loadingApp.getAppName() + ") not currently running.");
			}

			dai.releaseCompProcLock(new TsdbCompLock(DbKey.createDbKey(appId), appStat.getPid().intValue(),
					appStat.getHostname(), appStat.getHeartbeat(), appStat.getStatus()));

			return Response.status(HttpServletResponse.SC_OK)
					.entity("App with ID " + appId + " (" + loadingApp.getAppName() + ") terminated.").build();
		}
		catch (NoSuchObjectException e)
		{
			throw new DatabaseItemNotFoundException(String.format("No such app found with ID %s", appId), e);
		}
		catch (DbIoException ex)
		{
			throw new WebAppException(HttpServletResponse.SC_BAD_REQUEST,
					String.format("Error attempting to stop appId=%s", appId), ex);
		}
	}

	private static ApiAppStatus getAppStatus(LoadingAppDAI dai, Long appId) throws  DbException
	{
		try
		{
			List<TsdbCompLock> locks = dai.getAllCompProcLocks();
			for (TsdbCompLock lock : locks)
			{
				if(lock.getAppId().getValue() == appId)
				{
					return map(dai, lock);
				}
			}

			Optional<ApiAppStatus> appStatus = dai.listComputationApps(false)
					.stream()
					.map(AppResources::statusMap)
					.filter(app -> app.getAppId().equals(appId))
					.findFirst();
			return appStatus.orElse(null);
		}
		catch (DbIoException ex)
		{
			throw new DbException(String.format("Error retrieving locks for app ID: %s", appId), ex);
		}
	}

	static ApiAppStatus statusMap(CompAppInfo app)
	{
		ApiAppStatus ret = new ApiAppStatus();
		ret.setAppId(app.getAppId().getValue());
		ret.setAppName(app.getAppName());
		ret.setAppType(app.getAppType());
		return ret;
	}

}
