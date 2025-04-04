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

import java.io.IOException;
import java.util.UUID;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.MDC;

/**
 * This filter is responsible for generating and propagating trace information
 * for incoming requests and outgoing responses. It ensures traceability by
 * utilizing the <a href="https://www.w3.org/TR/trace-context/">W3C Trace Context standard headers</a>,
 * specifically `traceparent` and `tracestate`.
 */
@Provider
public final class TraceFilter implements ContainerRequestFilter, ContainerResponseFilter
{
	public static final String TRACE_PARENT_HEADER = "traceparent";
	private static final String TRACE_STATE_HEADER = "tracestate";

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException
	{
		String traceParent = requestContext.getHeaderString(TRACE_PARENT_HEADER);
		String traceState = requestContext.getHeaderString(TRACE_STATE_HEADER);
		if(traceParent == null)
		{
			traceParent = generateNewTraceparent();
		}
		requestContext.setProperty(TRACE_PARENT_HEADER, traceParent);
		requestContext.setProperty(TRACE_STATE_HEADER, traceState);
		//This is thread local
		MDC.put(TRACE_PARENT_HEADER, traceParent);
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException
	{
		String traceParent = (String) requestContext.getProperty(TRACE_PARENT_HEADER);
		String traceState = (String) requestContext.getProperty(TRACE_STATE_HEADER);
		responseContext.getHeaders().add(TRACE_PARENT_HEADER, traceParent);
		if(traceState != null)
		{
			responseContext.getHeaders().add(TRACE_STATE_HEADER, traceState);
		}
		MDC.clear();
	}

	private String generateNewTraceparent()
	{
		return "00-" + generateTraceId() + "-" + generateSpanId() + "-01";
	}

	private String generateTraceId()
	{
		return UUID.randomUUID().toString().replace("-", "");
	}

	private String generateSpanId()
	{
		return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
	}

}
