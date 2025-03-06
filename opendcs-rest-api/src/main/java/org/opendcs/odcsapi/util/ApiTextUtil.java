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

/*
*  $Id: ApiTextUtil.java,v 1.2 2022/12/06 13:45:51 mmaloney Exp $
*
*  $Source: /home/cvs/repo/odcsapi/src/main/java/org/opendcs/odcsapi/util/ApiTextUtil.java,v $
*
*  $State: Exp $
*
*  $Log: ApiTextUtil.java,v $
*  Revision 1.2  2022/12/06 13:45:51  mmaloney
*  Refactor to stop using ilex.util.Logger and start using java.util.logging.
*
*  Revision 1.1  2022/11/29 15:05:13  mmaloney
*  First cut of refactored DAOs and beans to remove dependency on opendcs.jar
*
*  Revision 1.5  2020/01/31 19:42:07  mmaloney
*  Added intEqual method to compare Integer objects allowing for null.
*
*  Revision 1.4  2019/12/11 14:31:52  mmaloney
*  Added splitQuoted method.
*
*  Revision 1.3  2019/06/10 19:35:05  mmaloney
*  Added dateEqual method.
*
*  Revision 1.2  2019/03/28 13:00:11  mmaloney
*  Added strEqualNE - consider null string the same as blank string.
*
*  Revision 1.1.1.1  2014/05/19 15:28:59  mmaloney
*  OPENDCS 6.0 Initial Checkin
*
*  Revision 1.4  2012/11/12 19:14:05  mmaloney
*  CWMS uses 't' to mean true.
*
*  Revision 1.3  2011/01/17 16:35:23  mmaloney
*  Added getFirstLine method.
*
*  Revision 1.2  2009/10/08 17:15:52  mjmaloney
*  fixed comment
*
*  Revision 1.1  2008/04/04 18:21:10  cvs
*  Added legacy code to repository
*
*  Revision 1.20  2007/09/29 21:58:41  mmaloney
*  dev
*
*  Revision 1.19  2007/05/25 14:07:13  mmaloney
*  dev
*
*  Revision 1.18  2006/12/23 18:16:05  mmaloney
*  dev
*
*  Revision 1.17  2004/08/30 14:50:32  mjmaloney
*  Javadocs
*
*  Revision 1.16  2004/06/21 13:31:53  mjmaloney
*  Added scanAssign method.
*
*  Revision 1.15  2004/05/21 18:28:04  mjmaloney
*  Added startsWithIgnoreCase method.
*
*  Revision 1.14  2004/04/02 18:58:17  mjmaloney
*  Created.
*
*  Revision 1.13  2003/12/15 15:21:14  mjmaloney
*  Improvements to support LRGS Config Editor & EDL files.
*
*  Revision 1.12  2003/11/15 20:36:43  mjmaloney
*  Added compareIgnoreCase method that tolerates null arguments.
*
*  Revision 1.11  2003/09/02 14:37:28  mjmaloney
*  Added TeeLogger. Added more control on msg format to Logger.
*  Added TextUtil.fixedLengthFields method.
*
*  Revision 1.10  2002/10/29 00:57:13  mjmaloney
*  Added right/left justify functions to TextUtil.
*
*  Revision 1.9  2002/08/29 05:59:56  chris
*  Added the split() method; also added some tests.
*
*  Revision 1.8  2001/11/09 14:35:22  mike
*  dev
*
*  Revision 1.7  2001/10/05 17:49:59  mike
*  Added HumanReadableFormatter
*
*  Revision 1.6  2001/03/19 03:11:57  mike
*  *** empty log message ***
*
*  Revision 1.5  2000/12/31 14:14:20  mike
*  Added containsNoWhiteSpace method.
*
*  Revision 1.4  2000/12/29 02:50:05  mike
*  dev
*
*  Revision 1.3  2000/12/27 22:03:54  mike
*  Added isAllWhiteSpace
*
*  Revision 1.2  2000/12/24 02:41:07  mike
*  dev
*
*  Revision 1.1  2000/01/07 23:04:51  mike
*  Created
*
*
*/
package org.opendcs.odcsapi.util;

/**
* This class contains a set of static methods that supplement the methods
* in the java.text package.
*/
public class ApiTextUtil
{

	/**
	* Returns true if string is the word "true", "on", or "yes".
	* Otherwise, returns false.
	* @param s the input string
	* @return true if string is the word "true", "on", or "yes".
	*/
	public static boolean str2boolean( String s )
	{
		if (s == null)
			return false;
		s = s.trim().toLowerCase();
		return s.startsWith("t") || s.startsWith("y")
			|| s.equalsIgnoreCase("on");
	}

	/**
	 * @return true if passed string is a hex number.
	 */
	public static boolean isHexString(String s)
	{
		int len = s.length();
		for(int i=0; i<len; i++)
			if (!ApiByteUtil.isHexChar((byte)s.charAt(i)))
				return false;
		return true;
	}
}
