package org.opendcs.odcsapi.res;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

import decodes.db.DataSource;
import decodes.sql.DbKey;
import org.junit.jupiter.api.Test;
import org.opendcs.odcsapi.beans.ApiDataSource;
import org.opendcs.odcsapi.beans.ApiDataSourceGroupMember;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.opendcs.odcsapi.res.DataSourceResources.map;

final class DataSourceResourcesTest
{
	@Test
	void testDataSourceMap() throws Exception
	{
		DataSource ds = new DataSource();
		ds.numUsedBy = 12;
		ds.setName("Parent");
		ds.setId(DbKey.createDbKey(57391L));
		Properties properties = new Properties();
		properties.setProperty("key", "value");
		ds.arguments = properties;
		ds.dataSourceType = "Test type";
		ds.setDataSourceArg("This is a data source arg");
		Vector<DataSource> groupMembers = new Vector<>();
		DataSource member = new DataSource();
		member.setName("This is a group member");
		groupMembers.add(member);
		ds.groupMembers = groupMembers;

		ApiDataSource apiData = map(ds);
		assertNotNull(apiData);
		assertEquals(apiData.getDataSourceId(), ds.getId().getValue());
		assertEquals(apiData.getName(), ds.getName());
		assertMatch(apiData.getGroupMembers(), ds.groupMembers);
	}

	private static void assertMatch(ArrayList<ApiDataSourceGroupMember> groupMembers, Vector<DataSource> groupMemVector)
	{
		for (ApiDataSourceGroupMember member : groupMembers)
		{
			boolean found = false;
			for (DataSource source : groupMemVector)
			{
				if (member.getDataSourceId().equals(source.getId().getValue()))
				{
					found = true;
					assertEquals(member.getDataSourceName(), source.getName());
				}
			}
			if (!found)
			{
				fail("Unable to find matching group member in list");
			}
		}
	}

	@Test
	void testDataSourceListMap()
	{
		Vector<DataSource> groupMembers = new Vector<>();

		ArrayList<ApiDataSourceGroupMember> members = map(groupMembers);
		assertNotNull(members);
		assertMatch(members, groupMembers);
	}

	@Test
	void testApiDataSourceMap()
	{
		ApiDataSource dataSource = new ApiDataSource();
		dataSource.setDataSourceId(58559642L);
		dataSource.setName("data source name");
		dataSource.setType("Test type");
		dataSource.setUsedBy(12);
		Properties props = new Properties();
		props.setProperty("Key", "Value");
		dataSource.setProps(props);
		ArrayList<ApiDataSourceGroupMember> memberList = new ArrayList<>();
		ApiDataSourceGroupMember member = new ApiDataSourceGroupMember();
		member.setDataSourceName("Child data source");
		member.setDataSourceId(8675309L);
		memberList.add(member);
		dataSource.setGroupMembers(memberList);

		DataSource result = map(dataSource);

		assertNotNull(result);
		assertEquals(dataSource.getName(), result.getName());
		assertEquals(dataSource.getDataSourceId(), result.getId().getValue());
		assertEquals(dataSource.getUsedBy(), result.numUsedBy);
		assertEquals(dataSource.getType(), result.dataSourceType);
		assertEquals(dataSource.getProps(), result.arguments);
		assertEquals(dataSource.getGroupMembers().size(), result.groupMembers.size());
		for (int i = 0; i < dataSource.getGroupMembers().size(); i++)
		{
			assertEquals(dataSource.getGroupMembers().get(i).getDataSourceName(), result.groupMembers.get(i).getName());
			assertEquals(dataSource.getGroupMembers().get(i).getDataSourceId(), result.groupMembers.get(i).getId().getValue());
		}
	}

	@Test
	void testDataSourceGroupMemberMap()
	{
		ArrayList<ApiDataSourceGroupMember> groupMembers = new ArrayList<>();
		ApiDataSourceGroupMember member = new ApiDataSourceGroupMember();
		member.setDataSourceId(123456789L);
		member.setDataSourceName("Test data source");
		groupMembers.add(member);

		Vector<DataSource> result = DataSourceResources.map(groupMembers);

		assertNotNull(result);
		assertEquals(groupMembers.size(), result.size());
		for (int i = 0; i < groupMembers.size(); i++)
		{
			assertEquals(groupMembers.get(i).getDataSourceName(), result.get(i).getName());
			assertEquals(groupMembers.get(i).getDataSourceId(), result.get(i).getId().getValue());
		}
	}
}
