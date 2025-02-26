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

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents a reference to a computation, including details about its algorithm, process, group, and status.")
public final class ApiComputationRef
{
	@Schema(description = "The unique identifier of the computation.")
	private Long computationId = null;
	@Schema(description = "The name of the computation.")
	private String name = null;
	@Schema(description = "The unique identifier of the algorithm associated with the computation.")
	private Long algorithmId = null;
	@Schema(description = "The name of the algorithm associated with the computation.")
	private String algorithmName = null;
	@Schema(description = "The unique identifier (PID) of the process associated with the computation.")
	private Long processId = null;
	@Schema(description = "The name of the process associated with the computation.")
	private String processName = null;
	@Schema(description = "Specifies whether the computation is enabled.")
	private boolean enabled = false;
	@Schema(description = "A detailed description of the computation.")
	private String description = null;
	@Schema(description = "The unique identifier of the group associated with the computation.")
	private Long groupId = null;
	@Schema(description = "The name of the group associated with the computation.")
	private String groupName = null;

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
	public Long getProcessId()
	{
		return processId;
	}
	public void setProcessId(Long processId)
	{
		this.processId = processId;
	}
	public String getProcessName()
	{
		return processName;
	}
	public void setProcessName(String processName)
	{
		this.processName = processName;
	}
	public boolean isEnabled()
	{
		return enabled;
	}
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = description;
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
