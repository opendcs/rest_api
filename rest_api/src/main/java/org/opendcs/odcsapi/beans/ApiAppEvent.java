package org.opendcs.odcsapi.beans;

import java.util.Date;

/**
 * An event generated by a background application. Events are 
 * streamed from an app's event socket.
 */
public class ApiAppEvent
{
	private Long appId = null;
	private String appName = null;
	private String priority = null;
	private Date eventTime = null;
	private String eventText = null;
	public Long getAppId()
	{
		return appId;
	}
	public void setAppId(Long appId)
	{
		this.appId = appId;
	}
	public String getAppName()
	{
		return appName;
	}
	public void setAppName(String appName)
	{
		this.appName = appName;
	}
	public Date getEventTime()
	{
		return eventTime;
	}
	public void setEventTime(Date eventTime)
	{
		this.eventTime = eventTime;
	}
	public String getEventText()
	{
		return eventText;
	}
	public void setEventText(String eventText)
	{
		this.eventText = eventText;
	}
	public String getPriority()
	{
		return priority;
	}
	public void setPriority(String priority)
	{
		this.priority = priority;
	}
}