# syntax=docker/dockerfile:1
#
# Empacota o JAR ja construido e testado pelo CI (./mvnw verify) — nao compila aqui.
# Local:  ./mvnw -B -ntp package -DskipTests && docker build -t dscproject:local .

ARG JRE_IMAGE=eclipse-temurin:21.0.12_8-jre-noble@sha256:7739f0ffce786528961eea6bf46d9610ee968ac6127c9b2e93494757bdecce9f

FROM ${JRE_IMAGE} AS extrator
WORKDIR /build
COPY target/*.jar application.jar
# Separa dependencias (mudam pouco) do codigo da aplicacao (muda sempre) em camadas
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM ${JRE_IMAGE}
ARG VERSION=dev
ARG REVISION=unknown
LABEL org.opencontainers.image.title="dscproject" \
      org.opencontainers.image.source="https://github.com/sgtchacald/dscproject" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.revision="${REVISION}"

RUN groupadd --system --gid 10001 app \
 && useradd --system --uid 10001 --gid app --no-create-home --shell /usr/sbin/nologin app

WORKDIR /app
COPY --from=extrator /build/extracted/dependencies/ ./
COPY --from=extrator /build/extracted/spring-boot-loader/ ./
COPY --from=extrator /build/extracted/snapshot-dependencies/ ./
COPY --from=extrator /build/extracted/application/ ./

ENV TZ=America/Sao_Paulo \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/urandom"

USER app
EXPOSE 8080

# Docker puro; no compose de hml/prod o healthcheck e redefinido com os mesmos parametros
HEALTHCHECK --interval=15s --timeout=5s --start-period=120s --retries=5 \
  CMD curl -fsS http://localhost:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "-jar", "application.jar"]
