# Build Stage
FROM maven:3.9.3-amazoncorretto-17 AS build

RUN mkdir -p /build
WORKDIR /build

## Copy over the app files
COPY . /build/

RUN mvn -DskipTests=true -f pom.xml clean package

##################
# Deploy
##################
FROM ghcr.io/usepa/tomcat-10:latest

COPY --from=build /build/target/similar-compound.war /usr/local/tomcat/webapps/similar-compound.war

## Removing server.xml and replacing it. Default error page security fix.
RUN rm /usr/local/tomcat/conf/server.xml

COPY --from=build /build/server.xml /usr/local/tomcat/conf/server.xml

RUN rm -rf /usr/local/tomcat/webapps/ROOT

CMD ["catalina.sh", "run"]
