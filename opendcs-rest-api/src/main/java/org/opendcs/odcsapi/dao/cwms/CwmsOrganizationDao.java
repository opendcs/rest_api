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

package org.opendcs.odcsapi.dao.cwms;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.dao.OrganizationDao;

public final class CwmsOrganizationDao implements OrganizationDao
{
	private final DataSource dataSource;

	public CwmsOrganizationDao(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	@Override
	public List<String> retrieveOrganizationIds() throws DbException
	{
		try(Connection connection = dataSource.getConnection();
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT OFFICE_ID FROM CWMS_20.CWMS_OFFICE"))
		{
			List<String> ids = new ArrayList<>();
			while(rs.next())
			{
				ids.add(rs.getString(1));
			}
			return ids;
		}
		catch(SQLException ex)
		{
			throw new DbException("Unable to retrieve organization ids");
		}
	}
}
