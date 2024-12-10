package org.opendcs.odcsapi.res;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import decodes.db.DataPresentation;
import decodes.db.DataType;
import decodes.db.PresentationGroup;
import decodes.db.PresentationGroupList;
import decodes.sql.DbKey;
import org.junit.jupiter.api.Test;
import org.opendcs.odcsapi.beans.ApiPresentationElement;
import org.opendcs.odcsapi.beans.ApiPresentationGroup;
import org.opendcs.odcsapi.beans.ApiPresentationRef;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class PresentationResourcesTest
{
	@Test
	void testPresentationGroupListMap() throws Exception
	{
		PresentationGroupList pgl = new PresentationGroupList();
		PresentationGroup pg = new PresentationGroup();
		pg.setId(DbKey.createDbKey(1234L));
		pg.inheritsFrom = "Parent";
		pg.groupName = "Test";
		pg.lastModifyTime = Date.from(Instant.parse("2021-07-01T00:00:00Z"));
		pg.isProduction = true;
		DataPresentation dataPres = new DataPresentation();
		dataPres.setDataType(new DataType("Test", "TST"));
		dataPres.setId(DbKey.createDbKey(1234567L));
		dataPres.setMaxDecimals(2);
		dataPres.setMaxValue(100.0);
		dataPres.setMinValue(0.0);
		dataPres.setUnitsAbbr("TST");
		pg.addDataPresentation(dataPres);
		pgl.add(pg);

		ArrayList<ApiPresentationRef> presentationRefs = PresentationResources.map(pgl);
		assertNotNull(presentationRefs);
		assertEquals(1, presentationRefs.size());
		ApiPresentationRef apiPresentationRef = presentationRefs.get(0);
		assertEquals(pg.inheritsFrom, apiPresentationRef.getInheritsFrom());
		assertEquals(pg.groupName, apiPresentationRef.getName());
		assertEquals(pg.isProduction, apiPresentationRef.isProduction());
		assertEquals(pg.getId().getValue(), apiPresentationRef.getGroupId());
		assertEquals(pg.lastModifyTime, apiPresentationRef.getLastModified());
	}

	@Test
	void testPresentationGroupMap() throws Exception
	{
		PresentationGroup pg = new PresentationGroup();
		pg.groupName = "Test";
		pg.inheritsFrom = "Parent";
		pg.isProduction = true;
		PresentationGroup parentGroup = new PresentationGroup();
		parentGroup.groupName = "Parent";
		parentGroup.setId(DbKey.createDbKey(99895L));
		pg.parent = parentGroup;
		pg.lastModifyTime = Date.from(Instant.parse("2021-07-01T00:00:00Z"));
		DataPresentation dataPres = new DataPresentation();
		dataPres.setDataType(new DataType("Test", "TST"));
		dataPres.setId(DbKey.createDbKey(1234567L));
		dataPres.setMaxDecimals(2);
		dataPres.setMaxValue(100.0);
		dataPres.setMinValue(0.0);
		dataPres.setUnitsAbbr("TST");
		pg.addDataPresentation(dataPres);

		ApiPresentationGroup apiPresentationGroup = PresentationResources.map(pg);
		assertNotNull(apiPresentationGroup);
		assertEquals(pg.inheritsFrom, apiPresentationGroup.getInheritsFrom());
		assertEquals(pg.groupName, apiPresentationGroup.getName());
		assertEquals(pg.isProduction, apiPresentationGroup.isProduction());
		assertEquals(pg.getId().getValue(), apiPresentationGroup.getGroupId());
		assertEquals(pg.lastModifyTime, apiPresentationGroup.getLastModified());
		assertEquals(pg.getId().getValue(), apiPresentationGroup.getGroupId());
	}

	@Test
	void testApiPresentationGroupMap() throws Exception
	{
		ApiPresentationGroup apiPresentationGroup = new ApiPresentationGroup();
		apiPresentationGroup.setGroupId(1234L);
		apiPresentationGroup.setInheritsFrom("Parent");
		apiPresentationGroup.setName("Test");
		apiPresentationGroup.setProduction(true);
		apiPresentationGroup.setLastModified(Date.from(Instant.parse("2021-07-01T00:00:00Z")));
		ArrayList<ApiPresentationElement> elements = new ArrayList<>();
		ApiPresentationElement ape = new ApiPresentationElement();
		ape.setDataTypeCode("TST");
		ape.setMax(100.0);
		ape.setMin(0.0);
		ape.setUnits("TST");
		ape.setDataTypeStd("Test");
		ape.setFractionalDigits(2);
		elements.add(ape);
		apiPresentationGroup.setElements(elements);

		PresentationGroup pg = PresentationResources.map(apiPresentationGroup);

		assertNotNull(pg);
		assertEquals(apiPresentationGroup.getInheritsFrom(), pg.inheritsFrom);
		assertEquals(apiPresentationGroup.getName(), pg.groupName);
		assertEquals(apiPresentationGroup.isProduction(), pg.isProduction);
		assertEquals(apiPresentationGroup.getGroupId(), pg.getId().getValue());
		assertEquals(apiPresentationGroup.getLastModified(), pg.lastModifyTime);
		assertMatch(apiPresentationGroup.getElements(), pg.dataPresentations);
	}

	private void assertMatch(ArrayList<ApiPresentationElement> elements, Vector<DataPresentation> dataPresentations)
	{
		assertEquals(elements.size(), dataPresentations.size());
		int index = 0;
		for (DataPresentation presentation : dataPresentations)
		{
			ApiPresentationElement element = elements.get(index);
			assertEquals(element.getDataTypeCode(), presentation.getDataType().getCode());
			assertEquals(element.getUnits(), presentation.getUnitsAbbr());
			assertEquals(element.getFractionalDigits(), presentation.getMaxDecimals());
			assertEquals(element.getMax(), presentation.getMaxValue());
			assertEquals(element.getMin(), presentation.getMinValue());
			index++;
		}
	}


}
