package org.opendcs.odcsapi.res;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import decodes.db.RoutingSpec;
import decodes.db.RoutingSpecList;
import decodes.db.ScheduleEntry;
import decodes.sql.DbKey;
import org.junit.jupiter.api.Test;
import org.opendcs.odcsapi.beans.ApiRouting;
import org.opendcs.odcsapi.beans.ApiRoutingRef;
import org.opendcs.odcsapi.beans.ApiScheduleEntry;
import org.opendcs.odcsapi.beans.ApiScheduleEntryRef;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.opendcs.odcsapi.res.RoutingResources.map;

final class RoutingResourcesTest
{
	@Test
	void testRoutingRefListMap() throws Exception
	{
		RoutingSpecList routingSpecList = new RoutingSpecList();
		RoutingSpec routingSpec = buildRoutingSpec();
		routingSpecList.add(routingSpec);
		List<ApiRoutingRef> apiRoutingRefs = map(routingSpecList);
		ApiRoutingRef apiRoutingRef = apiRoutingRefs.get(0);
		assertNotNull(apiRoutingRef);
		assertEquals(apiRoutingRef.getRoutingId(), routingSpec.getId().getValue());
		assertEquals(apiRoutingRef.getName(), routingSpec.getName());
	}

	@Test
	void testApiRoutingMap() throws Exception
	{
		RoutingSpec routingSpec = buildRoutingSpec();
		ApiRouting apiRouting = map(routingSpec);
		assertNotNull(apiRouting);
		assertEquals(apiRouting.getRoutingId(), routingSpec.getId().getValue());
		assertEquals(apiRouting.getName(), routingSpec.getName());
		assertEquals(apiRouting.getOutputTZ(), routingSpec.outputTimeZone.getID());
		assertEquals(apiRouting.getLastModified(), routingSpec.lastModifyTime);
		assertEquals(apiRouting.isEnableEquations(), routingSpec.enableEquations);
		assertEquals(new Vector<>(apiRouting.getNetlistNames()), routingSpec.networkListNames);
	}

	@Test
	void testRoutingSpecMap() throws Exception
	{
		ApiRouting apiRouting = new ApiRouting();
		apiRouting.setRoutingId(1234L);
		apiRouting.setName("TestRoutingSpec");
		RoutingSpec routingSpec = map(apiRouting);
		assertNotNull(routingSpec);
		assertEquals(routingSpec.getId().getValue(), apiRouting.getRoutingId());
		assertEquals(routingSpec.getName(), apiRouting.getName());
		if (routingSpec.outputTimeZone != null)
		{
			assertEquals(routingSpec.outputTimeZone.getID(), apiRouting.getOutputTZ());
		}
		else if (apiRouting.getOutputTZ() != null)
		{
			fail("routingSpec.outputTimeZone is null, but apiRouting.getOutputTZ() is not null");
		}
		assertEquals(routingSpec.lastModifyTime, apiRouting.getLastModified());
		assertEquals(routingSpec.enableEquations, apiRouting.isEnableEquations());
		assertEquals(routingSpec.networkListNames, new Vector<>(apiRouting.getNetlistNames()));
	}

	@Test
	void testScheduleEntryRefMap()
	{
		List<ScheduleEntry> scheduleEntries = new ArrayList<>();
		ScheduleEntry scheduleEntry = new ScheduleEntry(DbKey.createDbKey(1234L));
		scheduleEntry.setEnabled(true);
		scheduleEntry.setName("TestScheduleEntry");
		scheduleEntry.setRoutingSpecId(DbKey.createDbKey(5678L));
		scheduleEntry.setLastModified(Date.from(Instant.parse("2021-02-01T00:00:00Z")));
		scheduleEntry.setLoadingAppName("TestAppName");
		scheduleEntry.setRoutingSpecName("TestRoutingSpec");
		scheduleEntry.setTimezone("UTC");
		scheduleEntry.setStartTime(Date.from(Instant.parse("2021-01-01T00:00:00Z")));
		scheduleEntries.add(scheduleEntry);
		List<ApiScheduleEntryRef> apiScheduleEntryRefs = map(scheduleEntries);
		assertNotNull(apiScheduleEntryRefs);
		ApiScheduleEntryRef apiScheduleEntryRef = apiScheduleEntryRefs.get(0);
		assertNotNull(apiScheduleEntryRef);
		assertEquals(apiScheduleEntryRef.getSchedEntryId(), scheduleEntry.getKey().getValue());
		assertEquals(apiScheduleEntryRef.getName(), scheduleEntry.getName());
		assertEquals(apiScheduleEntryRef.getAppName(), scheduleEntry.getLoadingAppName());
		assertEquals(apiScheduleEntryRef.getRoutingSpecName(), scheduleEntry.getRoutingSpecName());
		assertEquals(apiScheduleEntryRef.getLastModified(), scheduleEntry.getLastModified());
	}

	@Test
	void testScheduleEntryMap() throws Exception
	{
		ApiScheduleEntry apiScheduleEntry = new ApiScheduleEntry();
		apiScheduleEntry.setSchedEntryId(1234L);
		apiScheduleEntry.setName("TestScheduleEntry");
		apiScheduleEntry.setAppName("TestAppName");
		apiScheduleEntry.setRoutingSpecName("TestRoutingSpec");
		apiScheduleEntry.setLastModified(Date.from(Instant.parse("2021-02-01T00:00:00Z")));
		apiScheduleEntry.setAppId(5678L);
		apiScheduleEntry.setRoutingSpecId(9012L);
		apiScheduleEntry.setStartTime(Date.from(Instant.parse("2021-01-01T00:00:00Z")));
		apiScheduleEntry.setEnabled(true);
		apiScheduleEntry.setTimeZone("UTC");
		apiScheduleEntry.setRunInterval("1h");
		ScheduleEntry scheduleEntry = map(apiScheduleEntry);
		assertNotNull(scheduleEntry);
		assertEquals(scheduleEntry.getKey().getValue(), apiScheduleEntry.getSchedEntryId());
		assertEquals(scheduleEntry.getName(), apiScheduleEntry.getName());
		assertEquals(scheduleEntry.getLoadingAppName(), apiScheduleEntry.getAppName());
		assertEquals(scheduleEntry.getRoutingSpecName(), apiScheduleEntry.getRoutingSpecName());
		assertEquals(scheduleEntry.getLastModified(), apiScheduleEntry.getLastModified());
		assertEquals(scheduleEntry.getLoadingAppId().getValue(), apiScheduleEntry.getAppId());
		assertEquals(scheduleEntry.getRoutingSpecId().getValue(), apiScheduleEntry.getRoutingSpecId());
		assertEquals(scheduleEntry.getStartTime(), apiScheduleEntry.getStartTime());
		assertEquals(scheduleEntry.isEnabled(), apiScheduleEntry.isEnabled());
		assertEquals(scheduleEntry.getTimezone(), apiScheduleEntry.getTimeZone());
		assertEquals(scheduleEntry.getRunInterval(), apiScheduleEntry.getRunInterval());
	}

	private RoutingSpec buildRoutingSpec() throws Exception
	{
		RoutingSpec routingSpec = new RoutingSpec();
		routingSpec.setName("TestRoutingSpec");
		routingSpec.setId(DbKey.createDbKey(1234L));
		routingSpec.outputTimeZone = TimeZone.getTimeZone("UTC");
		routingSpec.lastModifyTime = Date.from(Instant.parse("2021-02-01T00:00:00Z"));
		routingSpec.enableEquations = true;
		routingSpec.networkListNames = new Vector<>(Arrays.asList("TestNet", "TestNet2"));
		return routingSpec;
	}
}
