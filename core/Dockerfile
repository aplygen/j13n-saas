FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/core-*.jar app.jar

# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE=gcp

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]