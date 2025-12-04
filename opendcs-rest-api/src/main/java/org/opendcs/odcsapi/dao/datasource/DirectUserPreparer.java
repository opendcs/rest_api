package org.opendcs.odcsapi.dao.datasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.opendcs.odcsapi.dao.DbException;


public class DirectUserPreparer implements ConnectionPreparer
{
	private final String user;

	public DirectUserPreparer(String user)
	{
		this.user = user;
	}

	@Override
	public Connection prepare(Connection conn) throws SQLException
	{
		if(user != null)
		{
			String sql = "begin cwms_env.set_session_user_direct(upper(?)); end;";
			try(PreparedStatement setApiUser = conn.prepareStatement(sql))
			{
				setApiUser.setString(1, user);
				setApiUser.execute();
			}
		}

		return conn;
	}
}
