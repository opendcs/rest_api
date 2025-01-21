package org.opendcs.odcsapi.res;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import decodes.db.DataSource;
import decodes.db.DataSourceList;
import decodes.db.NetworkList;
import decodes.db.NetworkListEntry;
import decodes.sql.DbKey;
import org.junit.jupiter.api.Test;
import org.opendcs.odcsapi.beans.ApiDataSource;
import org.opendcs.odcsapi.beans.ApiDataSourceGroupMember;
import org.opendcs.odcsapi.beans.ApiDataSourceRef;
import org.opendcs.odcsapi.beans.ApiNetList;
import org.opendcs.odcsapi.beans.ApiNetListItem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendcs.odcsapi.res.LrgsResources.map;

final class LrgsResourcesTest
{
	@Test
	void testNetworkListMap() throws Exception
	{
		NetworkList networkList = new NetworkList();
		networkList.setId(DbKey.createDbKey(886521L));
		networkList.name = "Test Network List";
		networkList.transportMediumType = "GOES";
		networkList.siteNameTypePref = "NWS";
		networkList.lastModifyTime = Date.from(Instant.parse("2021-07-01T00:00:00Z"));

		ApiNetList apiNetList = map(networkList);

		assertNotNull(apiNetList);
		assertEquals(networkList.getId().getValue(), apiNetList.getNetlistId());
		assertEquals(networkList.name, apiNetList.getName());
		assertEquals(networkList.transportMediumType, apiNetList.getTransportMediumType());
		assertEquals(networkList.siteNameTypePref, apiNetList.getSiteNameTypePref());
		assertEquals(networkList.lastModifyTime, apiNetList.getLastModifyTime());
	}

	@Test
	void testNetworkListHashMap() throws Exception
	{
		HashMap<String, NetworkListEntry> listEntries = new HashMap<>();
		NetworkList parent = new NetworkList();
		parent.lastModifyTime = Date.from(Instant.parse("2021-07-01T00:00:00Z"));
		parent.name = "Network List";
		parent.transportMediumType = "GOES";
		parent.setId(DbKey.createDbKey(886521L));
		NetworkListEntry entry = new NetworkListEntry(parent, "TID-1234");
		entry.setDescription("Network entry description");
		entry.setPlatformName("Network Platform");
		listEntries.put("TID-1234", entry);

		HashMap<String, ApiNetListItem> apiNetListItems = map(listEntries);

		assertNotNull(apiNetListItems);
		assertEquals(1, apiNetListItems.size());
		ApiNetListItem apiNetListItem = apiNetListItems.get("TID-1234");
		assertNotNull(apiNetListItem);
		assertEquals("Network entry description", apiNetListItem.getDescription());
		assertEquals("Network Platform", apiNetListItem.getPlatformName());
		assertEquals("TID-1234", apiNetListItem.getTransportId());
	}

	@Test
	void testDataSourceListMap() throws Exception
	{
		DataSourceList dataSourceList = new DataSourceList();
		DataSource dataSource = new DataSource();
		dataSource.numUsedBy = 1;
		dataSource.dataSourceType = "LRGS";
		Properties dataSourceProps = new Properties();
		dataSourceProps.setProperty("host", "localhost");
		dataSource.arguments = dataSourceProps;
		dataSource.setId(DbKey.createDbKey(123456L));
		dataSourceList.add(dataSource);

		ArrayList<ApiDataSourceRef> apiDataSourceRefs = map(dataSourceList);

		assertNotNull(apiDataSourceRefs);
		assertEquals(1, apiDataSourceRefs.size());
		ApiDataSourceRef apiDataSourceRef = apiDataSourceRefs.get(0);
		assertNotNull(apiDataSourceRef);
		assertEquals(123456L, apiDataSourceRef.getDataSourceId());
		assertEquals(dataSource.dataSourceType, apiDataSourceRef.getType());
		assertEquals(dataSource.getName(), apiDataSourceRef.getName());
		assertEquals(dataSource.getDataSourceArg(), apiDataSourceRef.getArguments());
	}

	@Test
	void testDataSourceMap() throws Exception
	{
		DataSource dataSource = new DataSource();
		dataSource.dataSourceType = "LRGS";
		dataSource.setId(DbKey.createDbKey(123456L));
		dataSource.numUsedBy = 11;
		Properties dataSourceProps = new Properties();
		dataSourceProps.setProperty("host", "localhost");
		dataSource.arguments = dataSourceProps;
		Vector<DataSource> groupMembers = new Vector<>();
		DataSource groupMember = new DataSource();
		groupMember.dataSourceType = "LRGS";
		groupMember.setId(DbKey.createDbKey(654321L));
		groupMember.numUsedBy = 1;
		groupMembers.add(groupMember);
		dataSource.groupMembers = groupMembers;

		ApiDataSource apiDataSource = map(dataSource);

		assertNotNull(apiDataSource);
		assertEquals(123456L, apiDataSource.getDataSourceId());
		assertEquals(dataSource.dataSourceType, apiDataSource.getType());
		assertEquals(dataSource.getName(), apiDataSource.getName());
		assertMatch(dataSource.groupMembers, apiDataSource.getGroupMembers());
	}

	private void assertMatch(Vector<DataSource> sources, List<ApiDataSourceGroupMember> groupMembers)
	{
		assertEquals(sources.size(), groupMembers.size());
		Iterator<DataSource> srcIter = sources.iterator();
		int index = 0;
		while (srcIter.hasNext())
		{
			DataSource source = srcIter.next();
			ApiDataSourceGroupMember groupMember = groupMembers.get(index);
			assertEquals(source.getId().getValue(), groupMember.getDataSourceId());
			assertEquals(source.getName(), groupMember.getDataSourceName());
			index++;
		}
	}

	@Test
	void testDataSourceGroupMemberMap() throws Exception
	{
		Vector<DataSource> sources = new Vector<>();
		DataSource dataSource = new DataSource();
		dataSource.dataSourceType = "LRGS";
		dataSource.setId(DbKey.createDbKey(123456L));
		dataSource.numUsedBy = 22;
		Properties dataSourceProps = new Properties();
		dataSourceProps.setProperty("host", "localhost");
		dataSource.arguments = dataSourceProps;
		sources.add(dataSource);

		ArrayList<ApiDataSourceGroupMember> groupMembers = map(sources);

		assertNotNull(groupMembers);
		assertEquals(1, groupMembers.size());
		ApiDataSourceGroupMember groupMember = groupMembers.get(0);
		assertNotNull(groupMember);
		assertEquals(123456L, groupMember.getDataSourceId());
		assertEquals(dataSource.getName(), groupMember.getDataSourceName());
	}
}