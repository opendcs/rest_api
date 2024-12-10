package org.opendcs.odcsapi.res;

import java.util.ArrayList;
import java.util.HashMap;

import decodes.db.DbEnum;
import decodes.db.EnumList;
import decodes.sql.DbKey;
import org.junit.jupiter.api.Test;
import org.opendcs.odcsapi.beans.ApiRefList;
import org.opendcs.odcsapi.beans.ApiRefListItem;
import org.opendcs.odcsapi.beans.ApiSeason;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendcs.odcsapi.res.ReflistResources.map;
import static org.opendcs.odcsapi.res.ReflistResources.mapSeasons;
import static org.opendcs.odcsapi.res.ReflistResources.mapToEnum;

final class ReflistResourcesTest
{
	@Test
	void testEnumMap() throws Exception
	{
		ApiRefList refList = new ApiRefList();
		refList.setReflistId(55674L);
		refList.setDescription("description");
		refList.setDefaultValue("defaultValue");
		refList.setEnumName("enumName");
		HashMap<String, ApiRefListItem> items = new HashMap<>();
		ApiRefListItem item = new ApiRefListItem();
		item.setValue("value");
		item.setDescription("description");
		item.setSortNumber(1);
		items.put(refList.getEnumName(), item);
		refList.setItems(items);

		EnumList enumList = mapToEnum(refList);

		assertNotNull(enumList);
		assertEquals(refList.getReflistId(), enumList.getEnum("enumName").getId().getValue());
		assertEquals(refList.getDescription(), enumList.getEnum("enumName").getDescription());
		assertEquals(refList.getDefaultValue(), enumList.getEnum("enumName").getDefault());
		assertEquals(refList.getItems().size(), enumList.size());
	}

	@Test
	void testSeasonMap() throws Exception
	{
		EnumList enumList = new EnumList();
		DbEnum dbEnum = new DbEnum("season");
		dbEnum.setDescription("description");
		dbEnum.setDefault("default");
		dbEnum.setId(DbKey.createDbKey(55674L));
		enumList.addEnum(dbEnum);

		ArrayList<ApiSeason> seasons = mapSeasons(enumList);

		assertNotNull(seasons);
		assertEquals(dbEnum.enumName, seasons.get(0).getName());
	}

	@Test
	void testEnumSeasonMap()
	{
		ApiSeason season = new ApiSeason();
		String abbr = "abbr";
		season.setAbbr(abbr);
		season.setName("name");
		season.setStart("start");
		season.setEnd("end");
		season.setSortNumber(1);
		season.setTz("UTC");

		DbEnum dbEnum = map(season, abbr);

		assertNotNull(dbEnum);
		assertEquals(season.getName(), dbEnum.getUniqueName());
	}
}
