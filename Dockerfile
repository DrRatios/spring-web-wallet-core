FROM tomcat:9.0-jdk17

WORKDIR /app

COPY target/spring-web-wallet-*.war app.war

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar","-war"]