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

public final class ApiLrgsStatus
{
	private String hostname = null;
	private String systemStatus = null;
	private Boolean isUsable = null;
	private Date systemTime = null;
	private Integer maxClients = null;
	private Integer currentNumClients = null;
	private String lrgsVersion = null;
	
	private Long arcDirOldest = null;
	private Long arcDirNext = null;
	private Long arcDirWrap = null;
	private Long arcDirSize = null;
	private Long arcOldestOffset = null;
	private Date arcOldestMsgTime = null;
	private Long arcLastSeqNum = null;
	
	private List<ApiLrgsProcStatus> procStatus = new ArrayList<>();
	private List<ApiLrgsDownlinkStatus> downlinkStatus = new ArrayList<>();
	
	private ApiLrgsHourlyQuality[] hourlyArchiveQuality = null;
	
	public ApiLrgsStatus()
	{
		hourlyArchiveQuality = new ApiLrgsHourlyQuality[24];
		for(int i=0; i<24; i++)
		{
			hourlyArchiveQuality[i] = new ApiLrgsHourlyQuality();
			hourlyArchiveQuality[i].setHour(i);
		}
	}
	
	
	public String getSystemStatus()
	{
		return systemStatus;
	}
	public void setSystemStatus(String systemStatus)
	{
		this.systemStatus = systemStatus;
	}
	public Boolean getIsUsable()
	{
		return isUsable;
	}
	public void setIsUsable(Boolean isUsable)
	{
		this.isUsable = isUsable;
	}
	public Date getSystemTime()
	{
		return systemTime;
	}
	public void setSystemTime(Date systemTime)
	{
		this.systemTime = systemTime;
	}
	public Integer getMaxClients()
	{
		return maxClients;
	}
	public void setMaxClients(Integer maxClients)
	{
		this.maxClients = maxClients;
	}
	public Integer getCurrentNumClients()
	{
		return currentNumClients;
	}
	public void setCurrentNumClients(Integer currentNumClients)
	{
		this.currentNumClients = currentNumClients;
	}
	public String getLrgsVersion()
	{
		return lrgsVersion;
	}
	public void setLrgsVersion(String lrgsVersion)
	{
		this.lrgsVersion = lrgsVersion;
	}
	public Long getArcDirOldest()
	{
		return arcDirOldest;
	}
	public void setArcDirOldest(Long arcDirOldest)
	{
		this.arcDirOldest = arcDirOldest;
	}
	public Long getArcDirNext()
	{
		return arcDirNext;
	}
	public void setArcDirNext(Long arcDirNext)
	{
		this.arcDirNext = arcDirNext;
	}
	public Long getArcDirWrap()
	{
		return arcDirWrap;
	}
	public void setArcDirWrap(Long arcDirWrap)
	{
		this.arcDirWrap = arcDirWrap;
	}
	public Long getArcDirSize()
	{
		return arcDirSize;
	}
	public void setArcDirSize(Long arcDirSize)
	{
		this.arcDirSize = arcDirSize;
	}
	public Long getArcOldestOffset()
	{
		return arcOldestOffset;
	}
	public void setArcOldestOffset(Long arcOldestOffset)
	{
		this.arcOldestOffset = arcOldestOffset;
	}
	public Date getArcOldestMsgTime()
	{
		return arcOldestMsgTime;
	}
	public void setArcOldestMsgTime(Date arcOldestMsgTime)
	{
		this.arcOldestMsgTime = arcOldestMsgTime;
	}
	public Long getArcLastSeqNum()
	{
		return arcLastSeqNum;
	}
	public void setArcLastSeqNum(Long arcLastSeqNum)
	{
		this.arcLastSeqNum = arcLastSeqNum;
	}
	public List<ApiLrgsProcStatus> getProcStatus()
	{
		return procStatus;
	}
	public void setProcStatus(List<ApiLrgsProcStatus> procStatus)
	{
		this.procStatus = procStatus;
	}
	public List<ApiLrgsDownlinkStatus> getDownlinkStatus()
	{
		return downlinkStatus;
	}
	public void setDownlinkStatus(List<ApiLrgsDownlinkStatus> downlinkStatus)
	{
		this.downlinkStatus = downlinkStatus;
	}


	public ApiLrgsHourlyQuality[] getHourlyArchiveQuality()
	{
		return hourlyArchiveQuality;
	}


	public void setHourlyArchiveQuality(ApiLrgsHourlyQuality[] hourlyArchiveQuality)
	{
		this.hourlyArchiveQuality = hourlyArchiveQuality;
	}


	public String getHostname()
	{
		return hostname;
	}


	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}
}
