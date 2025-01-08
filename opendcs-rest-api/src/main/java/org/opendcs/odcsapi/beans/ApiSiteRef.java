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

import java.util.HashMap;
import java.util.Map;

public class ApiSiteRef
{
	private Long siteId = null;
	
	/** nametype - namevalue */
	private Map<String, String> sitenames = new HashMap<>();
	
	private String publicName = null;
	
	private String description = null;
	
	public Long getSiteId()
	{
		return siteId;
	}

	public void setSiteId(Long siteId)
	{
		this.siteId = siteId;
	}

	public Map<String, String> getSitenames()
	{
		return sitenames;
	}

	public void setSitenames(Map<String, String> sitenames)
	{
		this.sitenames = sitenames;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getPublicName()
	{
		return publicName;
	}

	public void setPublicName(String publicName)
	{
		this.publicName = publicName;
	}
	
	

}
