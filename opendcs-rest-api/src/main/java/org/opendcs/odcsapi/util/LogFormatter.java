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

package org.opendcs.odcsapi.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Formats a log message in the same way as the opendcs ilex.util.Logger system.
 * @author mmaloney
 */
public class LogFormatter
	extends Formatter
{
	private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	private String nl = System.getProperty("line.separator");
	
	public LogFormatter()
	{
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	@Override
	public String format(LogRecord record)
	{
		return record.getLevel().toString() + " " 
			+ sdf.format(new Date(record.getMillis())) + " " + record.getMessage() + nl;
	}
}
