/*
 *  Copyright 2024 OpenDCS Consortium
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

package org.opendcs.odcsapi.sec;

import java.util.Enumeration;
import javax.inject.Inject;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.opendcs.odcsapi.appmon.ApiEventClient;
import org.opendcs.odcsapi.lrgsclient.ApiLddsClient;
import org.opendcs.odcsapi.lrgsclient.ClientConnectionCache;

@WebListener
public class SessionDisconnect implements HttpSessionListener
{
	@Inject
	private ClientConnectionCache clientConnectionCache;

	@Override
	public void sessionCreated(HttpSessionEvent se)
	{
		//No-op
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se)
	{
		clientConnectionCache.removeSession(se.getSession().getId());
	}
}
