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
import java.util.concurrent.CopyOnWriteArrayList;

import org.opendcs.odcsapi.appmon.ApiEventClient;
import org.opendcs.odcsapi.beans.ApiAppStatus;
import org.opendcs.odcsapi.util.ApiBasicClient;

import static java.util.stream.Collectors.toList;

/**
 * This runs a continuous thread to check periodically for stale LDDS client connections
 * and close them.
 */
public final class ClientConnectionCache
{
	private static final long STALE_LDDS_THRESHOLD_MS = 90_000L;
	private static final long STALE_API_CLIENT_THRESHOLD_MS = 20_000L;
	private static final ClientConnectionCache instance = new ClientConnectionCache();

	private final Map<String, CacheRecord> cache = new ConcurrentHashMap<>();

	private ClientConnectionCache()
	{
		//private constructor
	}

	public static ClientConnectionCache getInstance()
	{
		return instance;
	}

	public void removeSession(String sessionId)
	{
		CacheRecord remove = cache.remove(sessionId);
		if(remove != null)
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

	public void setApiLddsClient(ApiLddsClient client, String sessionId)
	{
		CacheRecord cacheRecord = cache.computeIfAbsent(sessionId, s -> new CacheRecord());
		cacheRecord.setLddsClient(client);
	}

	public void addApiEventClient(ApiEventClient client, String sessionId)
	{
		CacheRecord cacheRecord = cache.computeIfAbsent(sessionId, s -> new CacheRecord());
		cacheRecord.getApiEventClients().add(client);
	}

	public void removeApiEventClient(ApiEventClient client, String sessionId)
	{
		CacheRecord cacheRecord = cache.computeIfAbsent(sessionId, s -> new CacheRecord());
		cacheRecord.getApiEventClients().remove(client);
	}

	public Optional<ApiLddsClient> getApiLddsClient(String sessionId)
	{
		CacheRecord cacheRecord = cache.computeIfAbsent(sessionId, s -> new CacheRecord());
		return Optional.ofNullable(cacheRecord.getLddsClient());
	}

	public Optional<ApiEventClient> getApiEventClient(Long appId, String sessionId)
	{
		CacheRecord cacheRecord = cache.computeIfAbsent(sessionId, s -> new CacheRecord());
		return cacheRecord.getApiEventClients().stream()
				.filter(a -> Objects.equals(a.getAppId(), appId))
				.findAny();
	}

	void removeExpiredApiEventClients(ArrayList<ApiAppStatus> appStatii)
	{
		for(ClientConnectionCache.CacheRecord entry : cache.values())
		{
			List<ApiEventClient> apiEventClients = entry.getApiEventClients();
			List<ApiEventClient> clientsToDisconnect = apiEventClients.stream()
					.filter(e -> appStatii.stream()
							.filter(a -> Objects.equals(e.getAppId(), a.getAppId()))
							.anyMatch(a -> a.getPid() == null
									|| a.getHeartbeat() == null
									|| System.currentTimeMillis() - a.getHeartbeat().getTime() > STALE_API_CLIENT_THRESHOLD_MS))
					.collect(toList());
			apiEventClients.removeAll(clientsToDisconnect);
		}
	}

	public void removeExpiredLddsClients()
	{
		for(ClientConnectionCache.CacheRecord cacheRecord : cache.values())
		{
			if(cacheRecord.getLddsClient() != null
					&& System.currentTimeMillis() - cacheRecord.getLddsClient().getLastActivity() > STALE_LDDS_THRESHOLD_MS)
			{
				ApiLddsClient cli = cacheRecord.getLddsClient();
				cli.info("Hanging up due to " + (STALE_LDDS_THRESHOLD_MS / 1000) + " seconds of inactivity.");
				cli.disconnect();
				cacheRecord.setLddsClient(null);
			}
		}
	}

	static final class CacheRecord
	{
		private final List<ApiEventClient> apiEventClients = new CopyOnWriteArrayList<>();
		private ApiLddsClient lddsClient;

		List<ApiEventClient> getApiEventClients()
		{
			return apiEventClients;
		}

		synchronized ApiLddsClient getLddsClient()
		{
			return lddsClient;
		}

		synchronized void setLddsClient(ApiLddsClient lddsClient)
		{
			this.lddsClient = lddsClient;
		}
	}
}