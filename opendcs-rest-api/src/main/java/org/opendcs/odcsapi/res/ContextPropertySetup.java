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
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.ws.rs.core.Context;

import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.start.StartException;

@WebListener
public final class ContextPropertySetup implements ServletContextListener
{
	@Context
	private ServletContext servletContext;

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		//Move this information to the database. https://github.com/opendcs/rest_api/issues/191
		String officeId = servletContext.getInitParameter("opendcs.rest.api.cwms.office");
		if(officeId != null)
		{
			DbInterface.decodesProperties.setProperty("CwmsOfficeId", officeId);
		}
		String databaseType = servletContext.getInitParameter("editDatabaseType");
		if(databaseType != null)
		{
			try
			{
				DbInterface.setDatabaseType(databaseType);
			}
			catch(StartException e)
			{
				throw new IllegalStateException("Error setting database type: " + databaseType, e);
			}
		}
	}
}