package org.opendcs.odcsapi.openapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.jaxrs2.ReaderListener;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.integration.api.OpenApiReader;
import io.swagger.v3.oas.models.OpenAPI;

@OpenAPIDefinition
public class OpenDcsOpenApiModifier implements ReaderListener
{
    private static final Logger log = LoggerFactory.getLogger(OpenDcsOpenApiModifier.class);
    @Override
    public void beforeScan(OpenApiReader reader, OpenAPI openAPI)
    {
        /* do nothing */
    }

    @Override
    public void afterScan(OpenApiReader reader, OpenAPI openAPI)
    {
        /**
         * todo: 
         * 1 update authcheck provider to return security scheme with runtime determined info
         * 2 add SecuritySchemes
         */
    }
    
}
