# OpenAPI Configuration

The Swagger UI is a tool that allows you to visualize and interact with the API’s resources without 
having any of the implementation logic in place. It’s automatically generated from your OpenAPI 
(formerly known as Swagger) Specification, with the visual documentation making it easy for 
back-end implementation and client-side consumption.

## Getting Started

To view the API specification, navigate to the Swagger UI located at:
`https://[REST_API_URL]/odcsapi/swagger-ui.html`.

On development machines running the REST API locally, this will be located at `http://localhost:7000/odcsapi/swagger-ui.html`.

## OpenAPI Specification Generation

In order to provide options for developers to interact with the API, the OpenAPI specification is
generated in one of two ways.

### Automated Generation
To automatically generate the OpenAPI specification, initiate the `run` Gradle task. The OpenAPI
specification will be generated upon runtime and will be available at the Swagger UI endpoint.

The raw JSON or YAML for the specification can be found at `http://localhost:7000/odcsapi/openapi.json`
or `http://localhost:7000/odcsapi/openapi.yaml`, respectively.

On the local machine, the current specification can be found at within the open-dcs-rest-api source 
code at `/build/resources/main/swaggerui/open_api.yaml`. It will only be present if the `run` task has already been
initiated at least once without cleaning the project.

### Manual Generation

For ease of use, a manual generation method has been developed to avoid the requirement of running
the webserver. This method is useful for developers who are working on the API specification and
wish to quickly generate the OpenAPI specification to view changes.

To manually generate the OpenAPI specification, run the `generateOpenAPI` Gradle task found within
the `documentation` group.

The manually generated OpenAPI specification will be placed in the `build/swagger` directory. The
file will be named `OpenDCS-REST-API.json`.

To change the output format from the default JSON to YAML, edit the `generateOpenAPI` task in the
`build.gradle` file for the opendcs-rest-api project. Change the `outputFormat` parameter from
`JSON` to `YAML`. The file will be located in the same location as the JSON file, but with the YAML
file extension.

To remove the generated OpenAPI specification, run the `cleanOpenAPI` Gradle task, located in the 
`documentation` group.

## Annotations

The OpenAPI specification is generated using annotations from the `swagger-annotations` library.
These annotations are used to describe the API endpoints and request and response bodies. This is
done by annotating the `Resource` endpoint classes and the appropriate DTO classes, located in the
`org.opendcs.odcsapi.res` and `org.opendcs.odcsapi.beans` packages, respectively.

The available annotations and their details can be found at the following link: ``