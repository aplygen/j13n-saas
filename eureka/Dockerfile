FROM eclipse-temurin:24-jdk

EXPOSE 9999

VOLUME [ "/logs" ]

ENV INSTANCE_ID=default

ENTRYPOINT ["java", "-Dlogging.file.name=/logs/eureka-${INSTANCE_ID}.log", "-jar", "eureka-1.1.0.jar"]
