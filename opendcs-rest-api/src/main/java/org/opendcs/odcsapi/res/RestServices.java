/*
 *  Copyright 2023 OpenDCS Consortium
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
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

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.sec.AuthorizationCheck;
import org.opendcs.odcsapi.sec.basicauth.BasicAuthCheck;
import org.opendcs.odcsapi.sec.cwms.ServletSsoAuthCheck;
import org.opendcs.odcsapi.sec.openid.OidcAuthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationPath("/")
public class RestServices extends ResourceConfig
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RestServices.class);

	public RestServices()
	{
		LOGGER.debug("Initializing odcsapi RestServices.");
		packages("org.opendcs.odcsapi");
		String authorizationType = DbInterface.getProperty("opendcs.rest.api.authorization");
		if("basic".equals(authorizationType))
		{
			register(AuthorizationCheck.class, BasicAuthCheck.class);
		}
		else if("openid".equals(authorizationType))
		{
			register(AuthorizationCheck.class, OidcAuthCheck.class);
		}
		else if("sso".equals(authorizationType))
		{
			register(AuthorizationCheck.class, ServletSsoAuthCheck.class);
		}
		else
		{
			throw new IllegalStateException("Property opendcs.rest.api.authorization must be configured to one of 'basic', 'openid', or 'sso'.");
		}
	}
}
