package org.opendcs.odcsapi.res;

import java.sql.Date;
import java.time.Instant;
import java.util.stream.Collectors;

import decodes.sql.DbKey;
import decodes.tsdb.DbCompAlgorithm;
import decodes.tsdb.DbComputation;
import decodes.tsdb.TsGroup;
import org.junit.jupiter.api.Test;
import org.opendcs.odcsapi.beans.ApiComputation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendcs.odcsapi.res.ComputationResources.map;

final class ComputationResourceTest
{
	@Test
	void testApiComputeMap()
	{
		DbKey dbKey = DbKey.createDbKey(16704L);
		String name = "Test Name";
		DbComputation dbComp = new DbComputation(dbKey, name);
		dbComp.setAlgorithmName("Test Algorithm");
		dbComp.setComment("Test Comment");
		dbComp.setAppId(DbKey.createDbKey(1L));
		dbComp.setLastModified(Date.from(Instant.parse("2023-08-01T10:30:00Z")));
		dbComp.setAlgorithm(new DbCompAlgorithm("Test Algorithm"));
		dbComp.setEnabled(true);
		dbComp.setApplicationName("Test Application");
		dbComp.setValidStart(Date.from(Instant.parse("2023-07-01T00:30:00Z")));
		TsGroup group = new TsGroup();
		group.setGroupName("Test Group");
		group.setGroupId(DbKey.createDbKey(16753096L));
		dbComp.setGroup(group);
		dbComp.setEnabled(true);
		dbComp.setValidEnd(Date.from(Instant.parse("2023-08-03T00:30:00Z")));
		ApiComputation apiComp = map(dbComp);
		assertNotNull(apiComp);
		assertEquals(dbKey.getValue(), apiComp.getComputationId());
		assertEquals(dbComp.getAlgorithmName(), apiComp.getAlgorithmName());
		assertEquals(dbComp.getComment(), apiComp.getComment());
		assertEquals(dbComp.getAppId().getValue(), apiComp.getAppId());
		assertEquals(dbComp.getLastModified(), apiComp.getLastModified());
		assertEquals(dbComp.getAlgorithm().getName(), apiComp.getAlgorithmName());
		assertEquals(dbComp.isEnabled(), apiComp.isEnabled());
		assertEquals(dbComp.getApplicationName(), apiComp.getApplicationName());
		assertEquals(dbComp.getValidStart(), apiComp.getEffectiveStartDate());
		assertEquals(dbComp.getValidEnd(), apiComp.getEffectiveEndDate());
		assertTrue(apiComp.isEnabled());
		assertEquals(dbComp.getGroup().getGroupName(), apiComp.getGroupName());
		assertEquals(dbComp.getGroupId().getValue(), apiComp.getGroupId());
		assertEquals(dbComp.getName(), apiComp.getName());
		assertEquals(dbComp.getProperties(), apiComp.getProps());
		assertEquals(dbComp.getParmList().stream().map(ComputationResources::map).collect(Collectors.toList()),
				apiComp.getParmList());
	}

	@Test
	void testDbComputeMap()
	{
		ApiComputation apiComp = new ApiComputation();
		apiComp.setComputationId(16704L);
		apiComp.setName("Test Name");
		apiComp.setAlgorithmName("Test Algorithm");
		apiComp.setComment("Test Comment");
		apiComp.setAppId(1L);
		apiComp.setLastModified(Date.from(Instant.parse("2023-08-01T10:30:00Z")));
		apiComp.setAlgorithmName("None");
		apiComp.setEnabled(true);
		apiComp.setApplicationName("Test Application");
		apiComp.setEffectiveStartDate(Date.from(Instant.parse("2023-07-01T00:30:00Z")));
		apiComp.setEffectiveEndDate(Date.from(Instant.parse("2023-08-03T00:30:00Z")));
		apiComp.setAlgorithmId(197865L);
		apiComp.setGroupName("Test Group");
		apiComp.setGroupId(16753096L);
		DbComputation dbComp = map(apiComp);
		assertNotNull(dbComp);
		assertEquals(apiComp.getComputationId(), dbComp.getKey().getValue());
		assertEquals(apiComp.getName(), dbComp.getName());
		assertEquals(apiComp.getAlgorithmName(), dbComp.getAlgorithmName());
		assertEquals(apiComp.getComment(), dbComp.getComment());
		assertEquals(apiComp.getAppId(), dbComp.getAppId().getValue());
		assertEquals(apiComp.getLastModified(), dbComp.getLastModified());
		assertEquals(apiComp.getAlgorithmName(), dbComp.getAlgorithm().getName());
		assertEquals(apiComp.isEnabled(), dbComp.isEnabled());
		assertEquals(apiComp.getApplicationName(), dbComp.getApplicationName());
		assertEquals(apiComp.getEffectiveStartDate(), dbComp.getValidStart());
		assertEquals(apiComp.getEffectiveEndDate(), dbComp.getValidEnd());
		assertTrue(dbComp.isEnabled());
		assertEquals(apiComp.getGroupName(), dbComp.getGroup().getGroupName());
		assertEquals(apiComp.getGroupId(), dbComp.getGroupId().getValue());
		assertEquals(apiComp.getName(), dbComp.getName());
		assertEquals(apiComp.getProps(), dbComp.getProperties());
		assertEquals(apiComp.getParmList().stream().map(ComputationResources::map).collect(Collectors.toList()),
				dbComp.getParmList());
		assertEquals(apiComp.getAlgorithmId(), dbComp.getAlgorithm().getId().getValue());
	}
}
