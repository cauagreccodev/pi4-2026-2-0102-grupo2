# Etapa 1: Compilação
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY lib/ lib/
COPY src/ src/

RUN javac -encoding UTF-8 -cp "lib/*" -d out $(find src -name "*.java")

# Etapa 2: Execução
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/out/ out/
COPY --from=build /app/lib/ lib/

# Porta do Servidor WebSocket (RFC 6455)
EXPOSE 5000

CMD ["java", "-cp", "out:lib/*", "com.helpdesk.socket.Main"]
