FROM gradle:8.14-jdk11 AS builder

RUN --mount=type=cache,target=/home/gradle/.gradle
WORKDIR /builddir
COPY . /builddir/
RUN gradle build --info --no-daemon


FROM alpine:3.21.3 AS tomcat_base
RUN apk --no-cache upgrade && \
    apk --no-cache add \
        openjdk11-jre \
        curl \
        bash

RUN mkdir /download && \
    cd /download && \
    wget https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.105/bin/apache-tomcat-9.0.105.tar.gz && \
    echo "904f10378ee2c7c68529edfefcba50c77eb677aa4586cfac0603e44703b0278f71f683b0295774f3cdcb027229d146490ef2c8868d8c2b5a631cf3db61ff9956 *apache-tomcat-9.0.105.tar.gz" > checksum.txt && \
    sha512sum -c checksum.txt && \
    tar xzf apache-tomcat-*tar.gz && \
    mv apache-tomcat-9.0.105 /usr/local/tomcat/ && \
    cd / && \
    rm -rf /download && \
    rm -rf /usr/local/tomcat/webapps/* && \
    mkdir /usr/local/tomcat/webapps/ROOT && \
    echo "<html><body>Nothing to see here</body></html>" > /usr/local/tomcat/webapps/ROOT/index.html
CMD ["/usr/local/tomcat/bin/catalina.sh","run"]

FROM tomcat_base AS api

COPY --from=builder /builddir/opendcs-rest-api/build/libs/*.war /usr/local/tomcat/webapps/odcsapi.war
COPY --from=builder /builddir/opendcs-web-client/build/libs/*.war /usr/local/tomcat/webapps/opendcs-web-client.war
COPY /docker_files/tomcat/conf/context.xml /usr/local/tomcat/conf
COPY /docker_files/tomcat/conf/tomcat-server.xml /usr/local/tomcat/conf/server.xml
COPY /docker_files/tomcat/conf/setenv.sh /usr/local/tomcat/bin
RUN curl -o /usr/local/tomcat/lib/ojdbc8.jar https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc8/23.9.0.25.07/ojdbc8-23.9.0.25.07.jar
ENV DCSTOOL_HOME="/"
EXPOSE 7000