package org.opendcs.odcsapi.beans;

import java.util.Date;

public class ApiPresentationRef
{
	private Long groupId;
	
	private String name = null;
	
	private String inheritsFrom = null;
	
	private transient Long inheritsFromId = null;
	
	private Date lastModified = null;
	
	private boolean isProduction = false;

	public Long getGroupId()
	{
		return groupId;
	}

	public void setGroupId(Long groupId)
	{
		this.groupId = groupId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getInheritsFrom()
	{
		return inheritsFrom;
	}

	public void setInheritsFrom(String inheritsFrom)
	{
		this.inheritsFrom = inheritsFrom;
	}

	public Date getLastModified()
	{
		return lastModified;
	}

	public void setLastModified(Date lastModified)
	{
		this.lastModified = lastModified;
	}

	public boolean isProduction()
	{
		return isProduction;
	}

	public void setProduction(boolean isProduction)
	{
		this.isProduction = isProduction;
	}

	public Long getInheritsFromId()
	{
		return inheritsFromId;
	}

	public void setInheritsFromId(Long inheritsFromId)
	{
		this.inheritsFromId = inheritsFromId;
	}
	
}