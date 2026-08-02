# Etapa 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia os arquivos de configuração do Maven
COPY pom.xml .

# Copia o código-fonte
COPY src ./src

# Compila a aplicação
RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia o JAR construído da etapa anterior
COPY --from=builder /app/target/speed-violation-service-*.jar app.jar

# Expõe a porta
EXPOSE 8080

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]

