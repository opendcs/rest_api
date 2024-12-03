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

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import javax.ws.rs.core.Context;

import decodes.db.DatabaseException;
import org.opendcs.database.DatabaseService;
import org.opendcs.database.api.OpenDcsDao;
import org.opendcs.database.api.OpenDcsDatabase;

import static org.opendcs.odcsapi.res.DataSourceContextCreator.DATA_SOURCE_ATTRIBUTE_KEY;

class OpenDcsResource
{
	@Context
	private ServletContext context;

	final <T extends OpenDcsDao> T getDao(Class<T> daoClass)
	{
		return createDb().getDao(daoClass)
				.orElseThrow(() -> new UnsupportedOperationException("Endpoint is unsupported by the OpenDCS REST API."));
	}

	final OpenDcsDatabase createDb()
	{
		try
		{
			DataSource dataSource = (DataSource) context.getAttribute(DATA_SOURCE_ATTRIBUTE_KEY);
			if(dataSource == null)
			{
				throw new IllegalStateException("No data source defined in context.xml");
			}
			return DatabaseService.getDatabaseFor(dataSource);
		}
		catch(DatabaseException e)
		{
			throw new IllegalStateException("Error connecting to the database via JNDI", e);
		}
	}
}
