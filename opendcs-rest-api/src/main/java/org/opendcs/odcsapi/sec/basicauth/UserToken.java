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

package org.opendcs.odcsapi.sec.basicauth;

import java.io.Serializable;

/**
 * Immutable token for storing a token string for a limited amount of time.
 */
final class UserToken implements Serializable
{
	static final String USER_TOKEN_ATTRIBUTE = "user-token";
	private static final long serialVersionUID = 7045984232599401413L;
	private String token = "";
	private String username = "";

	UserToken(String token, String username)
	{
		super();
		this.token = token;
		this.username = username;
	}

	String getToken()
	{
		return token;
	}

	String getUsername()
	{
		return username;
	}
}
