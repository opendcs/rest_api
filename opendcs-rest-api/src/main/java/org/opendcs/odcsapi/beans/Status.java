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

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendcs.odcsapi.sec.TraceFilter;
import org.slf4j.MDC;

@Schema(description = "Represents a status message response for API operations.")
public final class Status
{
	@Schema(description = "The status message providing additional details about the API operation.")
	private final String message;

	@Schema(description = "Trace parent for identifying the request.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private final String incidentIdentifier;

	public Status(String message)
	{
		this.message = message;
		this.incidentIdentifier = MDC.get(TraceFilter.TRACE_PARENT_HEADER);
	}

	public String getMessage()
	{
		return message;
	}

	public String getIncidentIdentifier()
	{
		return incidentIdentifier;
	}
}
