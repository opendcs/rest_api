package org.opendcs.odcsapi.res;

import decodes.db.UnitConverterDb;
import decodes.db.UnitConverterSet;
import decodes.sql.DbKey;
import org.junit.jupiter.api.Test;
import org.opendcs.odcsapi.beans.ApiUnitConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendcs.odcsapi.res.DatatypeUnitResources.map;

final class DatatypeUnitResourcesTest
{
	@Test
	void testMapUnitConverter() throws Exception
	{
		ApiUnitConverter auc = new ApiUnitConverter();
		auc.setFromAbbr("ft");
		auc.setToAbbr("m");
		auc.setAlgorithm("none");
		auc.setA(1.0);
		auc.setB(2.0);
		auc.setC(3.0);
		auc.setD(4.0);
		auc.setE(5.0);
		auc.setF(6.0);
		auc.setUcId(1234L);
		UnitConverterSet ucs = map(auc);

		assertNotNull(ucs);
		UnitConverterDb ucdb = ucs.getById(DbKey.createDbKey(1234L));
		assertNotNull(ucdb);
		ucdb.prepareForExec();
		assertTrue(ucdb.isPrepared());
		assertEquals(1234L, ucdb.getId().getValue());
		assertEquals("ft->m", ucdb.toString());
		assertEquals("ft", ucdb.fromAbbr);
		assertEquals("m", ucdb.toAbbr);
		assertEquals("none", ucdb.algorithm);
		assertEquals(1.0, ucdb.coefficients[0]);
		assertEquals(2.0, ucdb.coefficients[1]);
		assertEquals(3.0, ucdb.coefficients[2]);
		assertEquals(4.0, ucdb.coefficients[3]);
		assertEquals(5.0, ucdb.coefficients[4]);
		assertEquals(6.0, ucdb.coefficients[5]);

	}
}
