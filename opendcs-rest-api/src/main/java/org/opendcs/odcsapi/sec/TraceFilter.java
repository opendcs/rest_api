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

package org.opendcs.odcsapi.sec;

import java.util.UUID;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.MDC;

@Provider
public final class TraceFilter implements ContainerRequestFilter, ContainerResponseFilter
{
	private static final String TRACEPARENT_HEADER = "traceparent";

	@Override
	public void filter(ContainerRequestContext request)
	{
		String traceparent = request.getHeaderString(TRACEPARENT_HEADER);
		if(traceparent == null)
		{
			traceparent = generateTraceparent();
			request.getHeaders().add(TRACEPARENT_HEADER, traceparent);
		}
		MDC.put(TRACEPARENT_HEADER, traceparent);
	}

	private String generateTraceparent()
	{
		String traceId = UUID.randomUUID().toString().replace("-", "");
		String spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
		return String.format("00-%s-%s-01", traceId, spanId);
	}

	@Override
	public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext)
	{
		MDC.clear();
	}
}
