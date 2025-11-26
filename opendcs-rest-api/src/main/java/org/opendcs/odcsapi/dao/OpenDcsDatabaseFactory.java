/*
 *  Copyright 2025 OpenDCS Consortium and its Contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License")
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opendcs.odcsapi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;

import decodes.db.DatabaseException;
import decodes.util.DecodesSettings;
import org.opendcs.database.DatabaseService;
import org.opendcs.database.api.OpenDcsDatabase;
import org.opendcs.odcsapi.dao.datasource.ConnectionPreparer;
import org.opendcs.odcsapi.dao.datasource.ConnectionPreparingDataSource;
import org.opendcs.odcsapi.dao.datasource.DelegatingConnectionPreparer;
import org.opendcs.odcsapi.dao.datasource.DirectUserPreparer;
import org.opendcs.odcsapi.dao.datasource.SessionOfficePreparer;
import org.opendcs.odcsapi.dao.datasource.SessionTimeZonePreparer;

public final class OpenDcsDatabaseFactory
{
	public static final String CWMS_DB_TYPE = "CWMS";
	public static final String OPENTSDB_DB_TYPE = "OPENTSDB";

	private OpenDcsDatabaseFactory()
	{
		throw new AssertionError("Utility class");
	}

	public static synchronized OpenDcsDatabase createDb(DataSource dataSource, String organization, String user)
	{
		List<ConnectionPreparer> preparers = new ArrayList<>();
		preparers.add(new SessionTimeZonePreparer());
		preparers.add(new SessionOfficePreparer(organization));
		if(user != null)
		{
			preparers.add(new DirectUserPreparer(user));
		}

		DataSource wrappedDataSource = new ConnectionPreparingDataSource(new DelegatingConnectionPreparer(preparers), dataSource);
		if(dataSource == null)
		{
			throw new IllegalStateException("No data source defined in context.xml");
		}
		try
		{
			Properties properties = new Properties();
			properties.put("CwmsOfficeId", organization);
			OpenDcsDatabase retval = DatabaseService.getDatabaseFor(wrappedDataSource, properties);
			return retval;
		}
		catch(DatabaseException ex)
		{
			throw new IllegalStateException("Error establishing database instance through data source.", ex);
		}
	}

	public static String getDatabaseType(DataSource dataSource)
	{
		String databaseType = "";
		try (Connection conn = dataSource.getConnection();
			 PreparedStatement stmt = conn.prepareStatement("select prop_value from tsdb_property WHERE prop_name = 'editDatabaseType'");
			 ResultSet rs = stmt.executeQuery())
		{
			if (rs.next())
			{
				databaseType = rs.getString("prop_value");
			}
		}
		catch (SQLException ex)
		{
			throw new IllegalStateException("editDatabaseType not set in tsdb_property table. Cannot determine the type of database.", ex);
		}
		return databaseType;
	}
}
