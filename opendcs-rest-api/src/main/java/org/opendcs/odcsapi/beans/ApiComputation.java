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

package org.opendcs.odcsapi.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class ApiComputation
{
	private Long computationId = null;
	
	private String name = null;
	
	private String comment = null;

	private Long appId = null;
	
	private String applicationName = null;
	
	private Date lastModified = null;
	
	private boolean enabled;

	/** "No Limit", "Calendar" or "Now -" */
	private String effectiveStartType = "No Limit";
	
	/** Use if effectiveStartType = "Calendar" */
	private Date effectiveStartDate = null;
	
	/** Use if effectiveStartType = "Now -" */
	private String effectiveStartInterval = null;

	/** "No Limit", "Calendar" or "Now -", "Now +", or "Now" */
	private String effectiveEndType = "No Limit";
	
	/** Use if effectiveStartType = "Calendar" */
	private Date effectiveEndDate = null;
	
	/** Use if effectiveStartType = "Now -" */
	private String effectiveEndInterval = null;

	private Long algorithmId = null;
	
	private String algorithmName = null;
	
	/** A list of this computation's parameters. */
	private List<ApiCompParm> parmList = new ArrayList<>();

	/**
	 * Properties from the meta-data CompProperty records.
	 */
	private Properties props = new Properties();

	private Long groupId = null;
	private String groupName = null;
	
	public ApiCompParm findParm(String role)
	{
		for(ApiCompParm cp : parmList)
			if (cp.getAlgoRoleName().equalsIgnoreCase(role))
				return cp;
		return null;
	}
	
	public Long getComputationId()
	{
		return computationId;
	}
	public void setComputationId(Long computationId)
	{
		this.computationId = computationId;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getComment()
	{
		return comment;
	}
	public void setComment(String comment)
	{
		this.comment = comment;
	}
	public Long getAppId()
	{
		return appId;
	}
	public void setAppId(Long appId)
	{
		this.appId = appId;
	}
	public String getApplicationName()
	{
		return applicationName;
	}
	public void setApplicationName(String applicationName)
	{
		this.applicationName = applicationName;
	}
	public Date getLastModified()
	{
		return lastModified;
	}
	public void setLastModified(Date lastModified)
	{
		this.lastModified = lastModified;
	}
	public boolean isEnabled()
	{
		return enabled;
	}
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
	public String getEffectiveStartType()
	{
		return effectiveStartType;
	}
	public void setEffectiveStartType(String effectiveStartType)
	{
		this.effectiveStartType = effectiveStartType;
	}
	public Date getEffectiveStartDate()
	{
		return effectiveStartDate;
	}
	public void setEffectiveStartDate(Date effectiveStartDate)
	{
		this.effectiveStartDate = effectiveStartDate;
	}
	public String getEffectiveStartInterval()
	{
		return effectiveStartInterval;
	}
	public void setEffectiveStartInterval(String effectiveStartInterval)
	{
		this.effectiveStartInterval = effectiveStartInterval;
	}
	public String getEffectiveEndType()
	{
		return effectiveEndType;
	}
	public void setEffectiveEndType(String effectiveEndType)
	{
		this.effectiveEndType = effectiveEndType;
	}
	public Date getEffectiveEndDate()
	{
		return effectiveEndDate;
	}
	public void setEffectiveEndDate(Date effectiveEndDate)
	{
		this.effectiveEndDate = effectiveEndDate;
	}
	public String getEffectiveEndInterval()
	{
		return effectiveEndInterval;
	}
	public void setEffectiveEndInterval(String effectiveEndInterval)
	{
		this.effectiveEndInterval = effectiveEndInterval;
	}
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
	public List<ApiCompParm> getParmList()
	{
		return parmList;
	}
	public void setParmList(List<ApiCompParm> parmList)
	{
		this.parmList = parmList;
	}
	public Properties getProps()
	{
		return props;
	}
	public void setProps(Properties props)
	{
		this.props = props;
	}
	public Long getGroupId()
	{
		return groupId;
	}
	public void setGroupId(Long groupId)
	{
		this.groupId = groupId;
	}
	public String getGroupName()
	{
		return groupName;
	}
	public void setGroupName(String groupName)
	{
		this.groupName = groupName;
	}

}
