package org.opendcs;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendcs.fixtures.AppTestBase;
import org.opendcs.fixtures.annotations.ConfiguredField;
import org.opendcs.fixtures.annotations.EnableIfSql;

import decodes.tsdb.TimeSeriesDb;


public class CredentialTest extends AppTestBase
{

    @ConfiguredField
    TimeSeriesDb db;

    @Test
    @EnableIfSql
    public void test_credentials()
    {
        assertNotNull(db);
    }
}
