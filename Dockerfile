# Estágio de construção (build)
FROM maven:3.8.4-openjdk-11-slim AS build

# Diretório de trabalho para a aplicação
WORKDIR /app

# Copie apenas o arquivo pom.xml para obter dependências
COPY pom.xml .

# Baixe as dependências do Maven (isso ajuda a cachear as dependências se o arquivo pom não mudar)
RUN mvn dependency:go-offline

# Copie todo o código-fonte
COPY src ./src

# Construa o projeto com Maven
RUN mvn clean package -DskipTests

# Estágio de execução
FROM openjdk:11-jre-slim

# Porta que a aplicação vai usar
EXPOSE 8080

# Diretório de trabalho para a aplicação
WORKDIR /app

# Copie o arquivo JAR construído a partir do estágio de construção
COPY --from=build /app/target/gerar-excel-0.0.1-SNAPSHOT.jar app.jar

# Comando para iniciar a aplicação quando o contêiner iniciar
ENTRYPOINT ["java", "-jar", "app.jar"]
