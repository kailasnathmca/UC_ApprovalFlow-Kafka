FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/proposal-service-*.jar app.jar
RUN addgroup -S app && adduser -S app -G app
USER app
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
