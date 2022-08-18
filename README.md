# rest_api
Rest API that serves OpenDCS database objects as JSON


## Target

This web app is targeted at Tomcat and Jetty, as such the Jersey JAX-RS jars will be pulled in with the build.
You'll need to manually remove these if you try to run the application in a full Java EE Application Server