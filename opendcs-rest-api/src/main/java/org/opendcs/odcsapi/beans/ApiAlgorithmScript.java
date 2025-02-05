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

public final class ApiAlgorithmScript
{
	public static final char TYPE_INIT = 'I';
	public static final char TYPE_BEFORE = 'B';
	public static final char TYPE_TIMESLICE = 'T';
	public static final char TYPE_AFTER = 'A';
	public static final char TYPE_TOOLTIP = 'P';
	public static final char TYPE_UNDEFINED = 'U';
	
	private String text = "";
	private char scriptType = TYPE_UNDEFINED;
	
	
	public String getText()
	{
		return text;
	}
	public void setText(String text)
	{
		this.text = text;
	}
	public char getScriptType()
	{
		return scriptType;
	}
	public void setScriptType(char scriptType)
	{
		this.scriptType = scriptType;
	}
}
