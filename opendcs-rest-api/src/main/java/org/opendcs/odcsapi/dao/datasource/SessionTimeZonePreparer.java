package org.opendcs.odcsapi.dao.datasource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nullable;

public final class SessionTimeZonePreparer implements ConnectionPreparer
{

	private static void setSessionTimeZoneUtc(Connection connection) throws SQLException
	{
		String sql = "alter session set time_zone = 'UTC'";
		try(CallableStatement statement = connection.prepareCall(sql))
		{
			statement.execute();
		}
	}

	@Override
	public Connection prepare(Connection conn) throws SQLException
	{
		setSessionTimeZoneUtc(conn);
		return conn;
	}

}