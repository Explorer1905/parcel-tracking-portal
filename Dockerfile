FROM tomcat:10.1-jdk17-temurin

# Time zone for the JVM; must match APP_TIMEZONE used for the database session.
ENV TZ=Asia/Kolkata
ENV CATALINA_OPTS="-Dfile.encoding=UTF-8"

RUN rm -rf /usr/local/tomcat/webapps/*
COPY target/parcel-tracking-portal.war /usr/local/tomcat/webapps/parcel-tracking-portal.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
