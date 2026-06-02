FROM tomcat:9-jdk8

COPY target/managelab.war /usr/local/tomcat/webapps/managelab.war

EXPOSE 8080