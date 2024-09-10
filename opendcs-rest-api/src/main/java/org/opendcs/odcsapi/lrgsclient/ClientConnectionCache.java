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

package org.opendcs.odcsapi.lrgsclient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.opendcs.odcsapi.appmon.ApiEventClient;
import org.opendcs.odcsapi.beans.ApiAppStatus;
import org.opendcs.odcsapi.dao.ApiAppDAO;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.util.ApiBasicClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;

/**
 * This runs a continuous thread to check periodically for stale LDDS client connections
 * and close them.
 */
@WebListener
public class ClientConnectionCache implements ServletContextListener
{
	private static final long STALE_THRESHOLD_MS = 90000L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnectionCache.class);

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private final Map<String, CacheRecord> cache = new ConcurrentHashMap<>();

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		executor.scheduleAtFixedRate(this::checkClients, 0, 30, TimeUnit.SECONDS);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		executor.shutdown();
	}

	public void removeSession(String sessionId)
	{
		CacheRecord remove = cache.remove(sessionId);
		if(remove != null)
		{
			synchronized(remove)
			{
				ApiLddsClient cli = remove.getLddsClient();
				if(cli != null)
				{
					cli.info("Disconnecting LDDS Client due to session termination.");
					cli.disconnect();
				}
				remove.getApiEventClients().forEach(ApiBasicClient::disconnect);
			}
		}
	}

	public void setApiLddsClient(ApiLddsClient client, String sessionId)
	{
		CacheRecord cacheRecord = cache.computeIfAbsent(sessionId, s -> new CacheRecord());
		synchronized(cacheRecord)
		{
			cacheRecord.setLddsClient(client);
		}
	}

	public void addApiEventClient(ApiEventClient client, String sessionId)
	{
		CacheRecord cacheRecord = cache.computeIfAbsent(sessionId, s -> new CacheRecord());
		synchronized(cacheRecord)
		{
			cacheRecord.getApiEventClients().add(client);
		}
	}

	public void removeApiEventClient(ApiEventClient client, String sessionId)
	{
		CacheRecord cacheRecord = cache.computeIfAbsent(sessionId, s -> new CacheRecord());
		synchronized(cacheRecord)
		{
			cacheRecord.getApiEventClients().remove(client);
		}
	}

	public Optional<ApiLddsClient> getApiLddsClient(String sessionId)
	{
		CacheRecord cacheRecord = cache.computeIfAbsent(sessionId, s -> new CacheRecord());
		synchronized(cacheRecord)
		{
			return Optional.ofNullable(cacheRecord.getLddsClient());
		}
	}

	public Optional<ApiEventClient> getApiEventClient(Long appId, String sessionId)
	{
		CacheRecord cacheRecord = cache.computeIfAbsent(sessionId, s -> new CacheRecord());
		synchronized(cacheRecord)
		{
			return cacheRecord.getApiEventClients().stream()
					.filter(a -> Objects.equals(a.getAppId(), appId))
					.findAny();
		}
	}

	/**
	 * Called periodically to hang up any DDS clients or event clients
	 * that have gone stale, e.g. client starts a message retrieval and never completes it, or
	 * event clients with a stale heartbeat.
	 */
	private void checkClients()
	{
		checkLddsClients();
		try
		{
			checkApiEventClients();
		}
		catch(DbException e)
		{
			LOGGER.error("There was an error checking for stale clients.", e);
		}
	}

	private void checkApiEventClients() throws DbException
	{
		ArrayList<ApiAppStatus> appStatii;
		try(DbInterface dbi = new DbInterface();
			ApiAppDAO appDao = new ApiAppDAO(dbi))
		{
			appStatii = appDao.getAppStatus();
		}
		for(CacheRecord entry : cache.values())
		{
			synchronized(entry)
			{
				List<ApiEventClient> apiEventClients = entry.getApiEventClients();
				List<ApiEventClient> clientsToDisconnect = apiEventClients.stream()
						.filter(e -> appStatii.stream()
								.filter(a -> Objects.equals(e.getAppId(), a.getAppId()))
								.anyMatch(a -> a.getPid() == null
										|| a.getHeartbeat() == null
										|| System.currentTimeMillis() - a.getHeartbeat().getTime() > 20000L))
						.collect(toList());
				apiEventClients.removeAll(clientsToDisconnect);
			}
		}
	}

	private void checkLddsClients()
	{
		for(CacheRecord cacheRecord : cache.values())
		{
			synchronized(cacheRecord)
			{
				if(cacheRecord.getLddsClient() != null
						&& System.currentTimeMillis() - cacheRecord.getLddsClient().getLastActivity() > STALE_THRESHOLD_MS)
				{
					ApiLddsClient cli = cacheRecord.getLddsClient();
					cli.info("Hanging up due to " + (STALE_THRESHOLD_MS / 1000) + " seconds of inactivity.");
					cli.disconnect();
					cacheRecord.setLddsClient(null);
				}
			}
		}
	}

	private static final class CacheRecord
	{
		private final List<ApiEventClient> apiEventClients = new ArrayList<>();
		private ApiLddsClient lddsClient;

		private List<ApiEventClient> getApiEventClients()
		{
			return apiEventClients;
		}

		private ApiLddsClient getLddsClient()
		{
			return lddsClient;
		}

		private void setLddsClient(ApiLddsClient lddsClient)
		{
			this.lddsClient = lddsClient;
		}
	}
}
