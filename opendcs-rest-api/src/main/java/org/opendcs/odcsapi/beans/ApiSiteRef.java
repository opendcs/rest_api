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

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents a reference to a site, including metadata such as IDs, names, and descriptions.")
public final class ApiSiteRef
{
	@Schema(description = "Unique numeric identifier for the site.", example = "1")
	private Long siteId = null;

	/**
	 * nametype - namevalue
	 */
	@Schema(description = "Map containing site names where the key represents the name type and the value the actual name.",
			example = "{\"CWMS\": \"OKVI4\", \"nwshb5\": \"OKVI4\"}")
	private Map<String, String> sitenames = new HashMap<>();

	@Schema(description = "Public name for the site.", example = "Barre Falls Dam", nullable = true)
	private String publicName = null;

	@Schema(description = "Description of the site.", example = "Iowa River at Oakville, IA (USGS)", nullable = true)
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
