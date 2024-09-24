FROM eclipse-temurin:17-jre
WORKDIR /home/app
COPY --link layers/libs /home/app/libs
COPY --link layers/app /home/app/
COPY --link layers/resources /home/app/resources
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/home/app/application.jar"]
