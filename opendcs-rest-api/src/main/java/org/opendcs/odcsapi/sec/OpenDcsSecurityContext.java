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

import java.security.Principal;
import javax.ws.rs.core.SecurityContext;

public final class OpenDcsSecurityContext implements SecurityContext
{
	private final boolean secure;
	private final OpenDcsPrincipal principal;
	private final String scheme;

	public OpenDcsSecurityContext(OpenDcsPrincipal principal, boolean secure,
			String scheme)
	{
		this.principal = principal;
		this.secure = secure;
		this.scheme = scheme;
	}

	@Override
	public Principal getUserPrincipal()
	{
		return principal;
	}

	@Override
	public boolean isUserInRole(String role)
	{
		return principal.getRoles()
				.stream()
				.anyMatch(e -> e.getRole().equals(role));
	}

	@Override
	public boolean isSecure()
	{
		return secure;
	}

	@Override
	public String getAuthenticationScheme()
	{
		return scheme;
	}
}
