package org.opendcs.odcsapi.dao.datasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
		if(office != null && !office.isBlank())
		{
			String sql = "BEGIN cwms_ccp_vpd.set_ccp_session_ctx(cwms_util.get_office_code(:1), 2, :2 ); END;";
			try(PreparedStatement setApiUser = conn.prepareStatement(sql))
			{
				setApiUser.setString(1, office);
				setApiUser.setString(2, office);
				setApiUser.execute();
			}
		}
		else
		{
			logger.atDebug().log("Office is null or empty.");
		}
		return conn;
	}
}
