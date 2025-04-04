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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents a decoded message, which includes information about the log messages and time series data.")
public final class ApiDecodedMessage
{
	@Schema(description = "The time when the message was received or created.", example = "2025-01-01T00:00:00.000[UTC]")
	private Date messageTime = null;

	@Schema(description = "A list of log messages associated with the decoded message.")
	private List<ApiLogMessage> logMessages = new ArrayList<>();

	@Schema(description = "A list of time series data extracted or associated with the decoded message.")
	private List<ApiDecodesTimeSeries> timeSeries = new ArrayList<>();

	public List<ApiLogMessage> getLogMessages()
	{
		return logMessages;
	}

	public void setLogMessages(List<ApiLogMessage> logMessages)
	{
		this.logMessages = logMessages;
	}

	public Date getMessageTime()
	{
		return messageTime;
	}

	public void setMessageTime(Date messageTime)
	{
		this.messageTime = messageTime;
	}

	public List<ApiDecodesTimeSeries> getTimeSeries()
	{
		return timeSeries;
	}

	public void setTimeSeries(List<ApiDecodesTimeSeries> timeSeries)
	{
		this.timeSeries = timeSeries;
	}
}
