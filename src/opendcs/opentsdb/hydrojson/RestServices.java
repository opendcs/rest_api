package opendcs.opentsdb.hydrojson;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;

import opendcs.opentsdb.hydrojson.errorhandling.WebAppExceptionMapper;

@ApplicationPath("/")
public class RestServices
	extends ResourceConfig
{
	public RestServices()
	{
        packages("com.fasterxml.jackson.jaxrs.json");
        packages("opendcs.opentsdb.hydrojson");
		packages("io.swagger.v3.jaxrs2.integration.resources");
        register(WebAppExceptionMapper.class);
		OpenApiResource openApiResource = new OpenApiResource();
        register(openApiResource);
	}
}
