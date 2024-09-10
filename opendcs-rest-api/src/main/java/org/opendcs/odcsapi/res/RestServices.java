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

import com.fasterxml.jackson.databind.ObjectMapper;

import org.opendcs.odcsapi.sec.basicauth.TokenAuthenticatorFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationPath("/")
public class RestServices extends ResourceConfig
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RestServices.class);
	ObjectMapper mapper;
	
	public RestServices()
	{
		LOGGER.debug("Initializing odcsapi RestServices.");
        packages("com.fasterxml.jackson.jaxrs.json");
        packages("opendcs.opentsdb.hydrojson");
		packages("opendcs.odcsapi");
		packages(TokenAuthenticatorFilter.class.getPackage().getName());
		register(TokenAuthenticatorFilter.class);
//		packages("opendcs.odcsapi.sec");
//		packages("opendcs.odcsapi.sec.basicau");
	}
}
