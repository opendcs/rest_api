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
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.opendcs.odcsapi.dao.datasource.ConnectionPreparer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionOfficePreparer implements ConnectionPreparer
{
	private static final Logger logger = LoggerFactory.getLogger(SessionOfficePreparer.class);

	private final String office;

	public SessionOfficePreparer(String office)
	{
		this.office = office;
	}

	@Override
	public Connection prepare(Connection conn) throws SQLException
	{
		String sessionOffice = this.office;
		if(sessionOffice == null || !sessionOffice.isBlank())
		{
			logger.atDebug().log("Office is null or empty.");
			sessionOffice = "HQ";
		}
		String sql = "BEGIN cwms_ccp_vpd.set_ccp_session_ctx(cwms_util.get_office_code(:1), 2, :2 ); END;";
		try(PreparedStatement setApiUser = conn.prepareStatement(sql))
		{
			setApiUser.setString(1, sessionOffice);
			setApiUser.setString(2, sessionOffice);
			setApiUser.execute();
		}
		return conn;
	}
}
