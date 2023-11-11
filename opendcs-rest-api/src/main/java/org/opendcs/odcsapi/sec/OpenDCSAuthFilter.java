package org.opendcs.odcsapi.sec;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@PreMatching
public class OpenDCSAuthFilter implements ContainerRequestFilter 
{
    private final List<String> allowedRoles;

    public OpenDCSAuthFilter(String []roles)
    {
        allowedRoles = Arrays.asList(roles);
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException
    {
        SecurityContext securityContext = requestContext.getSecurityContext();
        boolean valid = allowedRoles.stream().anyMatch(securityContext::isUserInRole);
        if (!valid)
        {
            requestContext
                .abortWith(
                    Response.status(
                        Response.Status.FORBIDDEN.getStatusCode(), 
                        "User does not have appropriate role.")
                .build());
        }
    }    
}
