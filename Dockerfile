# ── Build stage ────────────────────────────────────────────────────────────────
# Pre-requisite: run  mvn clean package -DskipTests  before building the image.
# The repackaged fat-jar with classifier 'exec' is the runtime artifact.

FROM eclipse-temurin:25-jre AS runtime

WORKDIR /app

# Copy the fat JAR produced by spring-boot-maven-plugin (classifier=exec)
COPY target/ubot-scenario-engine-*-exec.jar app.jar

# Expose application and actuator port
EXPOSE 8080

# JVM tuning for virtual threads + containers
ENTRYPOINT ["java", \
  "-XX:+UseZGC", \
  "-XX:+ZGenerational", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
