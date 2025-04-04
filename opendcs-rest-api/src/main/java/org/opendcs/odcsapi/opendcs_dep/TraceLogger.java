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

package org.opendcs.odcsapi.opendcs_dep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggingEventBuilder;

final class TraceLogger extends ilex.util.Logger
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TraceLogger.class);

	public TraceLogger()
	{
		super("trace");
	}

	@Override
	public void close()
	{
		//No-op - fascade to SLF4j
	}

	@Override
	public void doLog(int priority, String msg)
	{
		// In addition to saving the message, write it to the API's log.
		LoggingEventBuilder level;
		switch(priority)
		{
			case ilex.util.Logger.E_DEBUG3:
			case ilex.util.Logger.E_DEBUG2:
				level = LOGGER.atTrace();
				break;
			case ilex.util.Logger.E_DEBUG1:
				level = LOGGER.atDebug();
				break;
			case ilex.util.Logger.E_WARNING:
				level = LOGGER.atWarn();
				break;
			case ilex.util.Logger.E_FATAL:
				level = LOGGER.atError();
				break;
			case ilex.util.Logger.E_INFORMATION:
			default:
				level = LOGGER.atInfo();
		}
		level.log(msg);
	}
}
