# Run stage
FROM amazoncorretto:17-alpine-jdk
ADD target/seguridad_vecinal-0.0.1.jar seguridad_vecinal-0.0.1.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","seguridad_vecinal-0.0.1.jar"]