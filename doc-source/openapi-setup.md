# OpenAPI Configuration

The OpenAPI specification represents the structure and expected input and output values of the
REST API.  

It can be visualized and interacted with via the Swagger UI, which represents the API
resources and their functionality in a user-friendly manner.

## Getting Started

To view the API specification, navigate to the Swagger UI located at:
`https://[REST_API_URL]/odcsapi/swagger-ui.html`.

On development machines running the REST API locally, this will be located at `http://localhost:7000/odcsapi/swagger-ui.html`.

> [!NOTE]
> The Swagger UI is only available when the REST API is running.

## OpenAPI Specification Generation

In order to provide multiple options for developers to interact with the API,
the OpenAPI specification is generated in one of two ways.

### Automated Generation
To automatically generate the OpenAPI specification, initiate the `run` Gradle task.

The OpenAPI specification will be generated upon runtime and will be available at the Swagger UI
endpoint mentioned above.

The raw JSON or YAML for the specification can be found at `http://localhost:7000/odcsapi/openapi.json`
or `http://localhost:7000/odcsapi/openapi.yaml`, respectively.

On the local machine, the current specification can be found at within the `open-dcs-rest-api` source 
code at `/build/resources/main/swaggerui/open_api.json`.  
It will only be present if the `run` task has already been initiated at least once without 
cleaning the project afterward.

### Manual Generation

For ease of use, a manual generation method has been developed to avoid the requirement of running
the webserver. This method is useful for developers who are working on modifying the API specification and
wish to quickly generate the OpenAPI specification to view changes.

> [!TIP]
> Generated JSON and YAML OpenAPI specifications can be viewed by copying the content of the file and pasting it into the
> [Swagger Editor](`https://editor.swagger.io/`).

To manually generate the OpenAPI specification, run the `generateOpenAPI` Gradle task found within
the `documentation` group. The default output format is JSON.

The manually generated OpenAPI specification will be placed in the `/build/swagger` directory. The
file will be named `OpenDCS-REST-API.json`.

To change the output format from the default JSON to YAML, edit the `generateOpenAPI` task in the
`build.gradle` file for the opendcs-rest-api project. Change the `outputFormat` parameter from
`JSON` to `YAML`. The file will be located in the same location as the JSON file, but with the YAML
file extension. To revert back to the JSON format, simply change the parameter value back to `JSON`.

To remove the generated OpenAPI specification, run the `cleanOpenAPI` Gradle task, located in the 
`documentation` group. This will remove both the generated JSON and YAML files from
the `/build/swagger` directory.

## Annotations

The OpenAPI specification is generated using annotations from the `io.swagger.core.v3:swagger-jaxrs2`
library. These annotations are used to describe the API endpoints and request and response bodies. 
This is done by annotating the `Resource` endpoint classes and the appropriate DTO classes, 
located in the `org.opendcs.odcsapi.res` and `org.opendcs.odcsapi.beans` packages, respectively.

> [!TIP] 
> The available annotations and their usage can be found 
> [here](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations).

### Examples

Example input data for the `POST` endpoints can be found in the
[ResourceExamples](../opendcs-rest-api/src/main/java/org/opendcs/odcsapi/res/ResourceExamples.java) 
class, located in the `org.opendcs.odcsapi.res` package. These examples provide various levels of 
input data for the different endpoints, which can be useful for determining the expected input data 
for each endpoint.

Adding new examples to the
[ResourceExamples](../opendcs-rest-api/src/main/java/org/opendcs/odcsapi/res/ResourceExamples.java) 
class is simple. Create a new inner class for the endpoint resource and add a 
`public static final String` constant with the JSON-formatted example data. 
**Remember to escape special characters in the JSON data.**

> [!IMPORTANT]  
> Due to a limitation of the Swagger annotations implementation, the examples must be String constants.
As a result, the examples must be placed within the
[ResourceExamples](../opendcs-rest-api/src/main/java/org/opendcs/odcsapi/res/ResourceExamples.java)
class as a JSON-formatted String constant and referenced by the `@ExampleObject` annotations located 
within the `@RequestBody` annotation of each relevant endpoint. 