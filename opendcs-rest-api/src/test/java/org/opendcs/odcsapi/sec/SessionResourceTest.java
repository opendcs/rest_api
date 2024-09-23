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

package org.opendcs.odcsapi.sec;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;
import org.opendcs.odcsapi.res.RestServices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionResourceTest extends JerseyTest
{

	private HttpSession httpSession;
	private SecurityContext securityContext;

	@Override
	protected Application configure()
	{
		return new ResourceConfig(SessionResource.class)
				.register(RestServices.class)
				.register(SecurityFilter.class)
				.register(new AbstractBinder()
				{
					@Override
					protected void configure()
					{
						httpSession = mock(HttpSession.class);
						AuthorizationCheck authCheck = mock(AuthorizationCheck.class);
						securityContext = mock(SecurityContext.class);
						when(authCheck.authorize(any(), any())).thenReturn(securityContext);
						HttpServletRequest mockRequest = mock(HttpServletRequest.class);
						when(mockRequest.getSession(anyBoolean())).thenReturn(httpSession);
						bind(mockRequest).to(HttpServletRequest.class);
						bind(authCheck).to(AuthorizationCheck.class);
					}
				});
	}

	@Test
	void testCheckRequiresAuthentication()
	{
		when(securityContext.isUserInRole(any())).thenReturn(false);
		Response response = target("/check").request().get();
		assertEquals(403, response.getStatus(), "Check should return 401 since user is unauthenticated");
	}

	@Test
	void testLogout()
	{
		when(securityContext.isUserInRole(any())).thenReturn(true);
		Response response = target("/logout").request().get();
		assertEquals(200, response.getStatus(), "Logout should return 200");
		verify(httpSession).invalidate();
	}
}
