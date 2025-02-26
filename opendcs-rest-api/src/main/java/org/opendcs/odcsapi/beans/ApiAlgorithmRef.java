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

/**
 * $Id: ApiAlgorithmRef.java,v 1.1 2022/11/29 15:05:13 mmaloney Exp $
 * 
 * Open Source Software
 * 
 * $Log: ApiAlgorithmRef.java,v $
 * Revision 1.1  2022/11/29 15:05:13  mmaloney
 * First cut of refactored DAOs and beans to remove dependency on opendcs.jar
 *
 * Revision 1.1.1.1  2022/11/10 15:26:19  cvs
 * odcsapi 1.0.0
 *
 * Revision 1.2  2013/03/21 18:27:39  mmaloney
 * DbKey Implementation
 *
 */
package org.opendcs.odcsapi.beans;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * This class holds the info for an algorithm in the on-screen list.
 */
@Schema(description = "API Algorithm Reference DTO that holds the summary information of an algorithm.")
public final class ApiAlgorithmRef
{
	@Schema(description = "Unique ID of the algorithm.")
	private Long algorithmId = null;

	@Schema(description = "The name of the algorithm.")
	private String algorithmName = "";

	@Schema(description = "Fully qualified Java execution class for the algorithm.")
	private String execClass = "";

	@Schema(description = "The number of computations using this algorithm.")
	private int numCompsUsing = 0;

	@Schema(description = "A brief description of the algorithm.", example = "Calculates the average value of all inputs.")
	private String description = "";

	public Long getAlgorithmId()
	{
		return algorithmId;
	}
	public void setAlgorithmId(Long algorithmId)
	{
		this.algorithmId = algorithmId;
	}
	public String getAlgorithmName()
	{
		return algorithmName;
	}
	public void setAlgorithmName(String algorithmName)
	{
		this.algorithmName = algorithmName;
	}
	public String getExecClass()
	{
		return execClass;
	}
	public void setExecClass(String execClass)
	{
		this.execClass = execClass;
	}
	public int getNumCompsUsing()
	{
		return numCompsUsing;
	}
	public void setNumCompsUsing(int numCompsUsing)
	{
		this.numCompsUsing = numCompsUsing;
	}
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}
}
