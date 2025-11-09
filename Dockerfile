# Multi-stage build otimizado para Render
FROM gradle:8.5-jdk17-alpine AS build

WORKDIR /app

# Copiar todos os arquivos necessários
COPY build.gradle settings.gradle ./
COPY src ./src

# Build da aplicação usando o Gradle da imagem base
# (não precisa do wrapper porque a imagem já tem Gradle instalado)
RUN gradle bootJar -x test --no-daemon

# Etapa de runtime - imagem menor
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Criar usuário não-root para segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar JAR da etapa de build
COPY --from=build /app/build/libs/*.jar app.jar

# Expor porta da aplicação (Render usa PORT como variável de ambiente)
EXPOSE 8080

# Variáveis de ambiente padrão (podem ser sobrescritas no Render)
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Healthcheck para monitoramento
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Comando de inicialização
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
