# ================= BUILD STAGE =================
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN mvn -B clean package -DskipTests -Dquarkus.package.jar.type=fast-jar

# ================= RUNTIME STAGE =================
FROM registry.access.redhat.com/ubi9/openjdk-25-runtime:1.24

ENV LANGUAGE='en_US:en'

WORKDIR /deployments

# ================= QUARKUS APP =================
COPY --from=build --chown=185 /app/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /app/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /app/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /app/target/quarkus-app/quarkus/ /deployments/quarkus/

# ================= JWT KEYS =================
COPY --from=build --chown=185 /app/src/main/resources/privateKey.pem /deployments/privateKey.pem
COPY --from=build --chown=185 /app/src/main/resources/publicKey.pem /deployments/publicKey.pem

# ================= PORT =================
EXPOSE 8080

USER 185

ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT ["/opt/jboss/container/java/run/run-java.sh"]