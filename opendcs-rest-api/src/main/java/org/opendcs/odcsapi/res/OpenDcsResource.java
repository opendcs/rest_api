/*
 *  Copyright 2024 OpenDCS Consortium and its Contributors
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

package org.opendcs.odcsapi.res;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

class OpenDcsResource
{
	private final OpenDcsDatabase db;

	protected OpenDcsResource()
	{
		try
		{
			//TODO - need to iron out the correct way to handle data source.
			Context initialCtx = new InitialContext();
			Context envCtx = (Context)initialCtx.lookup("java:comp/env");
			DataSource dataSource = (DataSource)envCtx.lookup("jdbc/opentsdb");
			this.db = DatabaseService.getDatabaseFor(dataSource);
		}
		catch(NamingException e)
		{
			throw new IllegalStateException(e);
		}
	}

	protected OpenDcsDatabase getDb()
	{
		return this.db;
	}
}
