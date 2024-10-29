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

package org.opendcs.odcsapi.opendcs_dep;

import decodes.db.DatabaseException;
import decodes.tsdb.TimeSeriesDb;
import org.opendcs.database.DatabaseService;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.hydrojson.DbInterface;

/**
 * A few operations require using the openDCS TimeSeriesDb subclasses.
 * This class instantiates and manages those classes given a connection
 * provided by the web container.
 * 
 * @author mmaloney
 *
 */
public class TsdbManager
{
	/**
	 * Make an appropriate TimeSeriesDb subclass depending on the database type (CWMS,
	 * HDB, or OpenTSDB).
	 * Note: Currently the tsdb is provided with the one connection being used by this
	 * session, created by the web container. So don't close the connection because
	 * the container is managing this.
	 * @param dbi
	 * @return
	 * @throws DbException
	 * @deprecated access DAI objects through OpenDcsDatabase::getDao
	 */
	@Deprecated
	public static TimeSeriesDb makeTsdb(DbInterface dbi)
		throws DbException
	{
		try
		{
			return DatabaseService.getDatabaseFor(DbInterface.getDataSource())
					.getTimeSeriesDb();
		}
		catch(DatabaseException ex)
		{
			throw new DbException("", ex);
		}
	}
}
