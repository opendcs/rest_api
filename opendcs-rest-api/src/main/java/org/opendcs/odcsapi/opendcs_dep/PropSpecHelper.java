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

import javax.servlet.http.HttpServletResponse;

import decodes.util.PropertiesOwner;
import decodes.util.PropertySpec;
import org.opendcs.odcsapi.beans.ApiPropSpec;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropSpecHelper
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PropSpecHelper.class);
	public static ApiPropSpec[] getPropSpecs(String className)
		throws WebAppException
	{
		PropertySpec[] ps = getDecodesPropSpecs(className);
		ApiPropSpec[] ret = new ApiPropSpec[ps.length];
		for(int i=0; i < ps.length; i++)
			ret[i] = new ApiPropSpec(ps[i].getName(), ps[i].getType(), ps[i].getDescription());
		return ret;
	}
	
	
	public static PropertySpec[] getDecodesPropSpecs(String className)
		throws WebAppException
	{
		//className is user controlled, so it is logged at trace level.
		LOGGER.trace("PropSpecHelper.getPropSpecs class='{}'", className);
		if (className.equalsIgnoreCase("decodes.datasource.FtpDataSource"))
		{
			// Can't instantiate the class because it requires Apache FTP libs.
			// Kludge: copy the specs here.
			PropertySpec[] ftpDsPropSpecs =
			{
				new PropertySpec("host", PropertySpec.HOSTNAME,
					"FTP Data Source: Host name or IP Address of FTP Server"),
				new PropertySpec("port", PropertySpec.INT,
					"FTP Data Source: Listening port on FTP Server (default = 21)"),
				new PropertySpec("username", PropertySpec.STRING,
					"FTP Data Source: User name with which to connect to FTP server"),
				new PropertySpec("password", PropertySpec.STRING,
					"FTP Data Source: Password on the FTP server"),
				new PropertySpec("remoteDir", PropertySpec.STRING,
					"FTP Data Source: remote directory - blank means root"),
				new PropertySpec("localDir", PropertySpec.DIRECTORY,
					"FTP Data Source: optional local directory in which to store downloaded"
					+ " file. If not supplied, received file is processed by DECODES but not"
					+ " saved locally."),
				new PropertySpec("filenames", PropertySpec.STRING,
					"Space-separated list of files to download from server"),
				new PropertySpec("xferMode", 
					PropertySpec.JAVA_ENUM + "decodes.datasource.FtpMode",
					"FTP Data Source: FTP transfer mode"),
				new PropertySpec("deleteFromServer", PropertySpec.BOOLEAN,
					"FTP Data Source: (default=false) Set to true to delete file from server "
					+ "after retrieval. (May be disallowed on some servers.)"),
				new PropertySpec("ftpActiveMode", PropertySpec.BOOLEAN,
					"FTP Data Source: (default=false for passive mode) Set to true to " +
					"use FTP active mode."),
				new PropertySpec("nameIsMediumId", PropertySpec.BOOLEAN,
					"Use with OneMessageFile=true if the downloaded filename is to be treated as a medium ID"
					+ " in order to link this data with a platform."),
				new PropertySpec("ftps", PropertySpec.BOOLEAN, "(default=false) Use Secure FTP."),
				new PropertySpec("newerThan", PropertySpec.STRING, 
					"Either a Date/Time in the format [[[CC]YY] DDD] HH:MM[:SS], "
					+ "or a string of the form 'now - N incr',"
					+ " where N is an integer and incr is minutes, hours, or days."),

			};
			return ftpDsPropSpecs;
		}
		else if (className.equalsIgnoreCase("decodes.datasource.WebDirectoryDataSource"))
		{
			PropertySpec[] webdirPropSpecs =
				{
					new PropertySpec("directoryUrl", PropertySpec.STRING,
						"(required) URL of the directory that lists the file names containing messages"),
					new PropertySpec("urlFieldDelimiter", PropertySpec.STRING,
						"(default = underscore) Delimiter for fields within the filenames."),
					new PropertySpec("urlTimePos", PropertySpec.INT,
						"(default=3) Position of time within the delimited file name (1=first pos)"),
					new PropertySpec("urlIdPos", PropertySpec.INT,
						"(default=5) Position of the transport medium ID within the file name (1=first pos)"),
					new PropertySpec("urlTimeFormat", PropertySpec.STRING,
							"(default = HHmmss) SimpleDateFormat format string for time in the filename"),
					new PropertySpec("urlTimeZone", PropertySpec.STRING,
							"(default = UTC) Time Zone for the time within the directory and file names")
				};
			return webdirPropSpecs;

		}
		else if (className.equalsIgnoreCase("decodes.dbeditor.RoutingSpecEditPanel"))
		{
			PropertySpec rsPropSpecs[] = 
				{
					// Properties implemented directly by RoutingSpecThread:
					new PropertySpec("noLimits", PropertySpec.BOOLEAN,
						"Do NOT Apply Sensor min/max limits."),
					new PropertySpec("removeRedundantData", PropertySpec.BOOLEAN,
						"Remove Redundant DCP Message Data."),
					new PropertySpec("compConfig", PropertySpec.FILENAME,
						"Name of in-line computations config file"),
					new PropertySpec("usgsSummaryFile", PropertySpec.FILENAME,
						"Optional USGS-Format Summary File"),
					new PropertySpec("RawArchivePath", PropertySpec.STRING, 
						"Path to raw archive file. Defining this turns on the raw-archive function. " +
						"Example: $DCSTOOL_HOME/raw-archive/fts/$DATE(yyMMdd).fts"),
					new PropertySpec("RawArchiveStartDelim", PropertySpec.STRING, 
						"String placed before each message in the file"),
					new PropertySpec("RawArchiveEndDelim", PropertySpec.STRING, 
						"String placed after each message in the file"),
					new PropertySpec("RawArchiveMaxAge", PropertySpec.STRING, 
						"Example: '1 year'. Files older than this are deleted."),
					new PropertySpec("debugLevel", PropertySpec.INT,
						"(default=0) Set to 1, 2, 3 for increasing levels of debug information" +
						" when this routing spec is run."),
					new PropertySpec("updatePlatformStatus", PropertySpec.BOOLEAN,
						"(default=true) set to false to NOT update platform status records as messages are processed."),
					new PropertySpec("purgeOldEvents", PropertySpec.BOOLEAN,
						"(default=true) Set to false to tell this routing spec to NOT attempt to "
						+ "purge expired events from the database. Also see DecodesSettings.eventPurgeDays")
				};
			return rsPropSpecs;
		}
		else if (className.equalsIgnoreCase("decodes.tsdb.algo.PythonAlgorithm"))
			return new PropertySpec[0];
		else if (className.equalsIgnoreCase("decodes.cwms.rating.CwmsRatingSingleIndep"))
		{
			PropertySpec[] specs =
				{
					new PropertySpec("templateVersion", PropertySpec.STRING,
						"(default=USGS-EXSA) Used as the version part of the rating template string"),
					new PropertySpec("specVersion", PropertySpec.STRING,
						"(default=Production) Used as the version part of the rating spec."),
					new PropertySpec("useDepLocation", PropertySpec.BOOLEAN,
						"(default=false) false means use location from first INdep param. "
						+ "True means use location from DEP param.")
				};
			return specs;
		}
		else if (className.equalsIgnoreCase("decodes.cwms.rating.CwmsRatingSingleIndep")
			|| className.equalsIgnoreCase("decodes.cwms.rating.CwmsRatingMultiIndep"))
		{
			PropertySpec[] specs =
				{
					new PropertySpec("templateVersion", PropertySpec.STRING,
						"(default=USGS-EXSA) Used as the version part of the rating template string"),
					new PropertySpec("specVersion", PropertySpec.STRING,
						"(default=Production) Used as the version part of the rating spec."),
					new PropertySpec("useDepLocation", PropertySpec.BOOLEAN,
						"(default=false) false means use location from first INdep param. "
						+ "True means use location from DEP param.")
				};
			return specs;
		}
		// The above special cases failed. Try to instantiate an object and ask it for
		// its prop specs.
		PropertiesOwner pw = null;
		try
		{
			if (className.equalsIgnoreCase("decodes.db.ConfigSensor"))
				pw = new decodes.db.ConfigSensor(null, 0);
			else if (className.equalsIgnoreCase("decodes.consumer.StringBufferConsumer"))
				pw = new decodes.consumer.StringBufferConsumer(new StringBuffer());
			else
			{
				Class<?> c = Class.forName(className);
				pw = (PropertiesOwner)c.newInstance();
			}
			return pw.getSupportedProps();
		}
		catch (Exception ex)
		{
			throw new WebAppException(HttpServletResponse.SC_CONFLICT, "Cannot get property specs for '" + className + "'", ex);
		}
	}
}
