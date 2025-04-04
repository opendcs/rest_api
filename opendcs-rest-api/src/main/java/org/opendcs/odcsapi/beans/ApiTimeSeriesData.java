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
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents time series data, including its identifier and the associated data points.")
public final class ApiTimeSeriesData
{
	@Schema(description = "Identifier for the time series. This field is required.")
	private ApiTimeSeriesIdentifier tsid = null;

	@Schema(description = "A list of time series values representing the data points.")
	private List<ApiTimeSeriesValue> values = new ArrayList<>();

	public ApiTimeSeriesIdentifier getTsid()
	{
		return tsid;
	}

	public void setTsid(ApiTimeSeriesIdentifier tsid)
	{
		this.tsid = tsid;
	}

	public List<ApiTimeSeriesValue> getValues()
	{
		return values;
	}

	public void setValues(List<ApiTimeSeriesValue> values)
	{
		this.values = values;
	}

}
