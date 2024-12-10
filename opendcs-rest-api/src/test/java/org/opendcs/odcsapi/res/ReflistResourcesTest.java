package org.opendcs.odcsapi.res;

import java.util.ArrayList;

import decodes.db.DbEnum;
import decodes.db.EnumList;
import org.junit.jupiter.api.Test;
import org.opendcs.odcsapi.beans.ApiRefList;
import org.opendcs.odcsapi.beans.ApiSeason;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendcs.odcsapi.res.ReflistResources.map;
import static org.opendcs.odcsapi.res.ReflistResources.mapSeasons;
import static org.opendcs.odcsapi.res.ReflistResources.mapToEnum;

final class ReflistResourcesTest
{
	@Test
	void testEnumMap()
	{
		ApiRefList refList = new ApiRefList();


		EnumList enumList = mapToEnum(refList);
		assertNotNull(enumList);
	}

	@Test
	void testSeasonMap()
	{
		EnumList enumList = new EnumList();

		ArrayList<ApiSeason> seasons = mapSeasons(enumList);
		assertNotNull(seasons);
	}

	@Test
	void testEnumSeasonMap()
	{
		ApiSeason season = new ApiSeason();
		String abbr = "abbr";

		DbEnum dbEnum = map(season, abbr);
		assertNotNull(dbEnum);
	}
}
