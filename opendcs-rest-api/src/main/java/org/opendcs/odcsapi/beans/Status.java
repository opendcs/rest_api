/*
 *  Copyright 2025 OpenDCS Consortium and its Contributors
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

package org.opendcs.odcsapi.beans;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents a status message response for API operations.")
public final class Status implements Serializable
{
	private static final long serialVersionUID = -9130603850117689481L;
	@Schema(description = "The status message providing additional details about the API operation.")
	private String message;

	public Status()
	{
	} // needed for JAXB

	public Status(String message)
	{
		this.message = message;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}
}
