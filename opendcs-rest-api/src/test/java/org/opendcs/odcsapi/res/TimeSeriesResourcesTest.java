package org.opendcs.odcsapi.res;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;

import decodes.cwms.CwmsTsId;
import decodes.db.Site;
import decodes.sql.DbKey;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.TimeSeriesIdentifier;
import decodes.tsdb.TsGroup;
import opendcs.opentsdb.Interval;
import org.junit.jupiter.api.Test;
import org.opendcs.odcsapi.beans.ApiInterval;
import org.opendcs.odcsapi.beans.ApiTimeSeriesData;
import org.opendcs.odcsapi.beans.ApiTimeSeriesIdentifier;
import org.opendcs.odcsapi.beans.ApiTsGroup;
import org.opendcs.odcsapi.beans.ApiTsGroupRef;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendcs.odcsapi.res.TimeSeriesResources.map;
import static org.opendcs.odcsapi.res.TimeSeriesResources.mapRef;

final class TimeSeriesResourcesTest
{
	@Test
	void testTSIdentifierMap() throws Exception
	{
		ArrayList<TimeSeriesIdentifier> identifiers = new ArrayList<>();
		TimeSeriesIdentifier id = new CwmsTsId();
		id.setDescription("Test description");
		id.setUniqueString("unique test");
		Site site = new Site();
		site.setPublicName("Public name");
		site.setActive(true);
		site.setLastModifyTime(Date.from(Instant.parse("2021-08-01T00:00:00Z")));
		site.setElevation(100.0);
		site.setLocationType("Test loc type");
		site.setDescription("Test description");
		id.setSite(site);
		id.setInterval("minute");
		id.setTableSelector("Test");
		id.setStorageUnits("m");
		id.setSiteName("Test site");
		id.setDisplayName("Test display");
		id.setKey(DbKey.createDbKey(88795L));
		id.setReadTime(55933124L);
		identifiers.add(id);

		ArrayList<ApiTimeSeriesIdentifier> apiIdentifiers = map(identifiers, false);
		assertNotNull(apiIdentifiers);
		assertEquals(identifiers.size(), apiIdentifiers.size());
		ApiTimeSeriesIdentifier apiId = apiIdentifiers.get(0);
		assertMatch(id, apiId);
	}

	@Test
	void testCTimeSeriesMap() throws Exception
	{
		CTimeSeries cts = new CTimeSeries(DbKey.createDbKey(86795L), null, null);
		cts.setBriefDescription("Test desc");
		cts.setComputationId(DbKey.createDbKey(86775L));
		cts.setInterval("hour");
		cts.addDependentCompId(DbKey.createDbKey(86785L));
		cts.addTaskListRecNum(56);
		cts.setDisplayName("Test display");
		TimeSeriesIdentifier id = new CwmsTsId();
		id.setDescription("Test description");
		id.setUniqueString("unique test");
		id.setKey(DbKey.createDbKey(88795L));
		id.setInterval("minute");
		id.setSiteName("Test site");
		id.setStorageUnits("m");
		id.setTableSelector("Test");
		Site site = new Site();
		site.setPublicName("Public name");
		site.setActive(true);
		site.setLastModifyTime(Date.from(Instant.parse("2021-08-01T00:00:00Z")));
		site.setElevation(100.0);
		site.setLocationType("Test loc type");
		site.setId(DbKey.createDbKey(885L));
		site.setDescription("Test description");
		id.setSite(site);
		cts.setTimeSeriesIdentifier(id);

		ApiTimeSeriesData apiTimeSeriesData = map(cts);

		assertNotNull(apiTimeSeriesData);
		assertMatch(cts.getTimeSeriesIdentifier(), apiTimeSeriesData.getTsid());
		// TODO: Add value list assertions
	}

	private void assertMatch(TimeSeriesIdentifier id, ApiTimeSeriesIdentifier apiId)
	{
		assertEquals(id.getDescription(), apiId.getDescription());
		assertEquals(id.getUniqueString(), apiId.getUniqueString());
		assertEquals(id.getKey().getValue(), apiId.getKey());
		assertEquals(id.getStorageUnits(), apiId.getStorageUnits());
	}

	@Test
	void testIntervalMap()
	{
		ApiInterval apiInterval = new ApiInterval();
		apiInterval.setIntervalId(1234L);
		apiInterval.setCalConstant("10");
		apiInterval.setName("Test interval");
		apiInterval.setCalMultilier(2);

		Interval interval = map(apiInterval);
		assertNotNull(interval);
		assertEquals(apiInterval.getIntervalId(), interval.getKey().getValue());
		assertEquals(Integer.parseInt(apiInterval.getCalConstant()), interval.getCalConstant());
		assertEquals(apiInterval.getName(), interval.getName());
		assertEquals(apiInterval.getCalMultilier(), interval.getCalMultiplier());
	}

	@Test
	void testTSGroupRefMap()
	{
		ArrayList<TsGroup> tsGroups = new ArrayList<>();
		TsGroup tsGroup = new TsGroup();
		tsGroup.setGroupId(DbKey.createDbKey(1234L));
		tsGroup.setGroupName("Test group");
		tsGroup.setGroupType("Test type");
		tsGroup.setDescription("Test description");
		tsGroup.setIsExpanded(false);
		ArrayList<TsGroup> groups = new ArrayList<>();
		TsGroup tsGroup2 = new TsGroup();
		tsGroup2.setGroupId(DbKey.createDbKey(1235L));
		tsGroup2.setGroupName("Test group 2");
		tsGroup2.setGroupType("Test type 2");
		tsGroup2.setDescription("Test description 2");
		groups.add(tsGroup2);
		tsGroup.setIntersectedGroups(groups);
		tsGroups.add(tsGroup);

		ArrayList<ApiTsGroupRef> apiTsGroupRefs = mapRef(tsGroups);
		assertNotNull(apiTsGroupRefs);
		ApiTsGroupRef apiTsGroupRef = apiTsGroupRefs.get(0);
		assertNotNull(apiTsGroupRef);
		assertEquals(tsGroup.getGroupId().getValue(), apiTsGroupRef.getGroupId());
		assertEquals(tsGroup.getGroupName(), apiTsGroupRef.getGroupName());
		assertEquals(tsGroup.getGroupType(), apiTsGroupRef.getGroupType());
		assertEquals(tsGroup.getDescription(), apiTsGroupRef.getDescription());
	}

	@Test
	void testTSGroupMap()
	{
		TsGroup tsGroup = new TsGroup();
		tsGroup.setGroupId(DbKey.createDbKey(1234L));
		tsGroup.setGroupName("Test group");
		tsGroup.setGroupType("Test type");
		tsGroup.setDescription("Test description");
		tsGroup.setIsExpanded(false);
		ArrayList<TsGroup> groups = new ArrayList<>();
		TsGroup tsGroup2 = new TsGroup();
		tsGroup2.setGroupId(DbKey.createDbKey(1235L));
		tsGroup2.setGroupName("Test group 2");
		tsGroup2.setGroupType("Test type 2");
		tsGroup2.setDescription("Test description 2");
		groups.add(tsGroup2);
		tsGroup.setIntersectedGroups(groups);

		ApiTsGroup apiTsGroup = map(tsGroup);

		assertNotNull(apiTsGroup);
		assertEquals(tsGroup.getGroupId().getValue(), apiTsGroup.getGroupId());
		assertEquals(tsGroup.getGroupName(), apiTsGroup.getGroupName());
		assertEquals(tsGroup.getGroupType(), apiTsGroup.getGroupType());
		assertEquals(tsGroup.getDescription(), apiTsGroup.getDescription());
		assertEquals(tsGroup.getIntersectedGroups().size(), apiTsGroup.getIntersectGroups().size());
		assertEquals(tsGroup.getIntersectedGroups().get(0).getGroupId().getValue(), apiTsGroup.getIntersectGroups().get(0).getGroupId());
		assertEquals(tsGroup.getIntersectedGroups().get(0).getGroupName(), apiTsGroup.getIntersectGroups().get(0).getGroupName());
		assertEquals(tsGroup.getIntersectedGroups().get(0).getGroupType(), apiTsGroup.getIntersectGroups().get(0).getGroupType());
		assertEquals(tsGroup.getIntersectedGroups().get(0).getDescription(), apiTsGroup.getIntersectGroups().get(0).getDescription());
	}

	@Test
	void testApiTSGroupMap()
	{
		ApiTsGroup apiTsGroup = new ApiTsGroup();
		apiTsGroup.setDescription("Test description");
		apiTsGroup.setGroupName("Test group");
		apiTsGroup.setGroupType("Test type");
		apiTsGroup.setGroupId(1234L);

		TsGroup tsGroup = map(apiTsGroup);

		assertNotNull(tsGroup);
		assertEquals(apiTsGroup.getGroupId(), tsGroup.getGroupId().getValue());
		assertEquals(apiTsGroup.getGroupName(), tsGroup.getGroupName());
		assertEquals(apiTsGroup.getGroupType(), tsGroup.getGroupType());
		assertEquals(apiTsGroup.getDescription(), tsGroup.getDescription());
	}

	@Test
	void testGroupRefListMap()
	{
		ArrayList<TsGroup> groups = new ArrayList<>();
		TsGroup tsGroup = new TsGroup();
		tsGroup.setGroupId(DbKey.createDbKey(1234L));
		tsGroup.setGroupName("Test group");
		tsGroup.setGroupType("Test type");
		tsGroup.setDescription("Test description");
		tsGroup.setIsExpanded(false);
		groups.add(tsGroup);
		TsGroup tsGroup2 = new TsGroup();
		tsGroup2.setGroupId(DbKey.createDbKey(1235L));
		tsGroup2.setGroupName("Test group 2");
		tsGroup2.setGroupType("Test type 2");
		tsGroup2.setDescription("Test description 2");
		tsGroup2.setIsExpanded(true);
		tsGroup2.setIntersectedGroups(groups);
		groups.add(tsGroup2);

		ArrayList<ApiTsGroupRef> apiTsGroupRefs = mapRef(groups);
		assertNotNull(apiTsGroupRefs);
		assertEquals(groups.size(), apiTsGroupRefs.size());
		ApiTsGroupRef apiTsGroupRef = apiTsGroupRefs.get(0);
		assertNotNull(apiTsGroupRef);
		assertEquals(tsGroup.getGroupId().getValue(), apiTsGroupRef.getGroupId());
		assertEquals(tsGroup.getGroupName(), apiTsGroupRef.getGroupName());
		assertEquals(tsGroup.getGroupType(), apiTsGroupRef.getGroupType());
		assertEquals(tsGroup.getDescription(), apiTsGroupRef.getDescription());
		ApiTsGroupRef apiTsGroupRef2 = apiTsGroupRefs.get(1);
		assertNotNull(apiTsGroupRef2);
		assertEquals(tsGroup2.getGroupId().getValue(), apiTsGroupRef2.getGroupId());
		assertEquals(tsGroup2.getGroupName(), apiTsGroupRef2.getGroupName());
		assertEquals(tsGroup2.getGroupType(), apiTsGroupRef2.getGroupType());
		assertEquals(tsGroup2.getDescription(), apiTsGroupRef2.getDescription());
	}
}
