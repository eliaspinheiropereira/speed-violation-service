# Speed Violation Service

## 📑 Índice
- [Descrição do Projeto](#descrição-do-projeto)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Pré-requisitos](#pré-requisitos)
- [Instruções de Execução](#instruções-de-execução)
    - [Execução Local](#execução-local)
    - [Execução via Docker](#execução-via-docker)
- [Exemplos de Uso com Postman](#exemplos-de-uso-com-postman)
    - [Caso sem Infração](#caso-sem-infração-post-apiv1violationsevaluate)
    - [Caso com Infração](#caso-com-infração-post-apiv1violationsevaluate)
    - [Caso de Busca](#caso-de-busca-com-postman)
    - [Consulta de Violações por Placa](#consulta-de-violações-por-placa)
- [Testando cobertura com jacoco](#testando-cobertura-com-jacoco)
- [Estrutura do Projeto](#estrutura-do-projeto)
    - [Camadas Principais](#-camadas-principais)

---

## Descrição do Projeto
O **Speed Violation Service** é um serviço backend desenvolvido em Java para monitorar e registrar violações de velocidade de veículos.  
Ele fornece uma API REST que pode ser integrada a sistemas de trânsito, câmeras de monitoramento ou sensores de velocidade, garantindo o registro e análise de infrações de forma eficiente e escalável.

---

## Tecnologias Utilizadas
- **Java 21** – Linguagem principal para desenvolvimento do serviço.
- **Spring Boot 4.1.0** – Framework para criação de aplicações REST rápidas e robustas.
- **Maven 3.9+** – Gerenciador de dependências e automação de build.
- **JUnit 5.10.2** – Framework de testes para garantir qualidade e confiabilidade.
- **Lombok 1.18.32** – Simplificação de código boilerplate (getters, setters, builders).
- **Springdoc OpenAPI 2.5.0** – Documentação interativa da API REST (Swagger UI).
- **Jacoco 0.8.12** – Ferramenta de cobertura de testes.
- **H2 Database 2.2.224** – Banco de dados em memória para desenvolvimento e testes.
- **Docker** – Para containerização e fácil deploy.

---

## Pré-requisitos

Antes de utilizar a API do **Speed Violation Service**, é necessário garantir que o ambiente esteja configurado com os seguintes requisitos:

- **Java 21** – Versão mínima necessária para compilar e executar o projeto.
- **Maven 3.9+** – Para gerenciamento de dependências e execução de builds.
- **Docker** (opcional) – Caso queira executar o serviço em containers.
- **Postman** (opcional) – Para realizar testes e simulações de requisições na API REST.
- **Git** – Para clonar o repositório e gerenciar versionamento.

---

## Instruções de Execução

### Execução Local

1. **Clonar o repositório**
   ```bash
   git clone https://github.com/eliaspinheiropereira/speed-violation-service.git
   cd speed-violation-service
   
2. **Compilar o projeto**
   ```bash
   mvn clean install
   ```

3. **Executar a aplicação**
   ```bash
   mvn spring-boot:run
   ```
   
4. **Acessar a API**
   - A API estará disponível em: `http://localhost:8080`
   - A documentação interativa (Swagger UI) pode ser acessada em: `http://localhost:8080/swagger-ui.html`
   - O banco de dados H2 pode ser acessado em: `http://localhost:8080/h2-console`

### Execução via Docker

1. **Clonar o repositório**
   ```bash
    git clone https://github.com/eliaspinheiropereira/speed-violation-service.git
    cd speed-violation-service
    ```
   
2. **Executar o serviço com Docker**
   ```bash
   docker-compose up -d   
   ```

3. **Acessar a API**
    - A API estará disponível em: `http://localhost:8080`
    - A documentação interativa (Swagger UI) pode ser acessada em: `http://localhost:8080/swagger-ui.html`
    - O banco de dados H2 pode ser acessado em: `http://localhost:8080/h2-console`

---

## Exemplos de Uso com Postman

O **Postman** pode ser utilizado para testar a API de forma prática e visual.  
Abaixo estão instruções para configurar as requisições:

---

### Caso sem Infração (POST /api/v1/violations/evaluate)

1. Abra o Postman e crie uma nova requisição.
2. Configure o método como **POST**.
3. Defina a URL: localhost:8080/api/v1/violations/evaluate
4. Vá até a aba **Headers** e adicione:
- **Key:** `x-origin`
- **Value:** `FIXED` (ou `MOBILE` / `HANDHELD`)
5. Vá até a aba **Body**, selecione **raw** e escolha o tipo **JSON**.
6. Insira o seguinte JSON para caso sem infração:
```json
{
  "licensePlate": "ABC1D24",
  "measuredSpeed": 67,
  "speedLimit": 60,
  "equipmentId": "RAD-CWB-001-20",
  "captureTimestamp": "2026-08-01T07:30:00Z"
}
```

### Caso com Infração (POST /api/v1/violations/evaluate)

1. Abra o Postman e crie uma nova requisição.
2. Configure o método como **POST**.
3. Defina a URL: localhost:8080/api/v1/violations/evaluate
4. Vá até a aba **Headers** e adicione:
- **Key:** `x-origin`
- **Value:** `FIXED` (ou `MOBILE` / `HANDHELD`)
5. Vá até a aba **Body**, selecione **raw** e escolha o tipo **JSON**.
6. Insira o seguinte JSON para caso com infração:
```json
{
  "licensePlate": "JUB6334",
  "measuredSpeed": 75,
  "speedLimit": 60,
  "equipmentId": "RAD-CWB-001-20",
  "captureTimestamp": "2026-08-01T07:30:00Z"
}
```

### Caso de Busca com Postman

Além de registrar infrações, a API permite consultar violações já processadas para um veículo específico.

### Consulta de Violações por Placa

1. Abra o Postman e crie uma nova requisição.
2. Configure o método como **GET**.
3. Defina a URL com o parâmetro de consulta: localhost:8080/api/v1/violations?licensePlate=ABC1D24
4. Clique em **Send** para enviar a requisição e visualizar as violações registradas para a placa especificada.
5. A resposta será um JSON contendo a lista de violações, incluindo detalhes como velocidade medida, limite de velocidade, data e hora da captura, e o ID do equipamento que registrou a infração.
6. Exemplo de resposta:
```json
[
  {
    "licensePlate": "ABC1D24",
    "equipmentId": "RAD-CWB-001-20",
    "measuredSpeed": 67,
    "consideredSpeed": 60,
    "speedLimit": 60,
    "excessPercentage": 0.0,
    "hasViolation": false,
    "violation": null,
    "processedAt": "2026-08-02T15:58:27.514Z"
  }
]
```

---

## Testando cobertura com jacoco

1. Após o projeto já iniciado pelo docker ou mvn.
2. entre na pasta do projeto chamada speed-violation-service
3. depois use o comando ./mvnw clean install
4. depois entre na pasta target/site/jacoco
5. abra com seu navegador o arquivo index.html
6. acesse io.gitub.eliaspinheiropereira.speed_violation_service.service
7. vai ter a cobertura dos testes das regras de negocio.

---

## Estrutura do Projeto

O **Speed Violation Service** segue a **Arquitetura em Camadas (Layered Architecture)**, típica de aplicações Spring Boot.  
Essa organização separa responsabilidades em pacotes distintos, facilitando manutenção, testes e escalabilidade.

---

### Camadas Principais

- **Controller Layer**  
  Responsável por expor os **endpoints REST** da aplicação.  
  Recebe as requisições HTTP e encaminha para a camada de serviço.

- **Service Layer**  
  Contém as **regras de negócio**.  
  Processa infrações de velocidade, cálculos de tolerância e percentual de excesso.

- **Model Layer**  
  Define as **entidades de domínio** e enums que representam os objetos centrais da aplicação.

- **DTO Layer**  
  Utilizada para **transferência de dados** entre cliente e servidor.  
  Facilita a serialização/deserialização de objetos JSON.

- **Repository Layer**  
  Responsável pela **persistência dos dados**.  
  Utiliza o banco de dados em memória **H2** para armazenar registros de violações.

- **Validation Layer**  
  Implementa regras de **validação customizadas**, como verificação de formato da placa de veículo.

- **Exception Layer**  
  Centraliza o tratamento de **erros e exceções**.  
  Inclui classes de exceção custom

- **Mapper Layer**  
  Contém classes para **mapeamento entre entidades e DTOs**, garantindo que os dados sejam corretamente transformados entre as camadas.

- **Configuration Layer**  
  Configurações gerais da aplicação, como **CORS**, **Swagger/OpenAPI**, e **propriedades do Spring Boot**.

- **Handler Layer**  
  Gerencia **exceções globais** e fornece respostas padronizadas para erros.


