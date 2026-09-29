# NutriExpress — API REST de Delivery de Comida Saudável

Back-end do recurso **Prato** de um aplicativo de delivery de comida saudável, construído em camadas:

```
Controller → Service → Repository (Spring Data JPA) → PostgreSQL
```

- **Controller** (`PratoController`): só recebe a requisição, valida com `@Valid`, delega ao Service e devolve o `ResponseEntity` com o status certo.
- **Service** (`PratoService`): regras de negócio e conversões `toEntity()` / `toDTO()`. Recebe e devolve apenas DTOs.
- **Repository** (`PratoRepository extends JpaRepository`): acesso ao banco, sem SQL escrito à mão.
- **Model** (`Prato`, `@Entity`): mapeado para a tabela `pratos` e nunca devolvido pela API.
- **DTOs** (Java Records): `PratoRequestDTO` (entrada, com Bean Validation), `PratoResponseDTO` (saída) e `PratoValorRequestDTO` (PATCH do valor).

## Tecnologias

Java 17+ · Spring Boot 4.1 (Web MVC, Data JPA, Validation) · Hibernate · PostgreSQL · Lombok · Maven · H2 (somente nos testes automatizados)

## Como rodar

**Pré-requisitos:** JDK 17 ou superior e Docker (ou um PostgreSQL local).

1. Suba o PostgreSQL:
   ```bash
   docker run --name nutri-express-postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres
   ```
2. Inicie a aplicação na raiz do projeto:
   ```bash
   ./mvnw spring-boot:run        # Linux/macOS/Git Bash
   mvnw.cmd spring-boot:run      # Windows (cmd/PowerShell)
   ```
3. A API sobe em `http://localhost:8080`. O log mostra a conexão com o PostgreSQL e a criação da tabela `pratos`.

A conexão padrão é `jdbc:postgresql://localhost:5432/postgres`, com usuário `postgres` e senha `postgres`. Para usar outro banco, defina as variáveis `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`.

## Endpoints

| Verbo | Rota | Descrição | Status |
|---|---|---|---|
| GET | `/pratos` | Lista todos os pratos | 200 |
| GET | `/pratos/{id}` | Busca um prato pelo ID | 200 / 404 |
| GET | `/pratos?categoria=vegano` | Filtra por categoria (sem diferenciar maiúsculas) | 200 |
| POST | `/pratos` | Cria um prato e devolve o recurso criado com o header `Location` | 201 / 400 / 409 |
| PUT | `/pratos/{id}` | Atualiza todos os dados de um prato | 200 / 400 / 404 / 409 |
| DELETE | `/pratos/{id}` | Remove um prato | 204 / 404 |
| PATCH | `/pratos/{id}/valor` | **Bônus:** altera somente o valor | 200 / 400 / 404 |
| GET | `/pratos/calorias?max=500` | **Bônus:** pratos com até X calorias, do mais leve ao mais calórico | 200 / 400 |

### Exemplo de corpo (POST / PUT)

```json
{
  "nome": "Bowl de Quinoa",
  "descricao": "Quinoa, grão-de-bico e legumes assados",
  "valor": 32.90,
  "categoria": "vegano",
  "calorias": 420,
  "quantidade": 350,
  "unidadeMedida": "g"
}
```

**Validações do `PratoRequestDTO`:**
- `nome`: obrigatório, até 100 caracteres.
- `descricao`: opcional, até 500 caracteres.
- `valor`: obrigatório, maior que zero, com até 2 casas decimais.
- `categoria`: `vegano`, `low carb`, `fitness` ou `sobremesa saudável`.
- `calorias`: obrigatório, maior ou igual a zero.
- `quantidade`: obrigatório, maior que zero.
- `unidadeMedida`: `g` ou `ml`.

### Exemplo de corpo (PATCH `/pratos/{id}/valor`)

```json
{ "valor": 27.00 }
```

## Regra de negócio

**Não podem existir dois pratos com o mesmo nome.** A comparação ignora maiúsculas, minúsculas e espaços nas pontas, então `"Salada Caesar"` e `"  salada caesar "` contam como o mesmo prato. No cardápio, o nome identifica o prato para o cliente, e dois pratos homônimos com preços ou calorias diferentes gerariam pedidos errados. A regra vale no POST e no PUT (o próprio prato pode manter o nome atual) e responde **409 Conflict**. Ela está documentada em `PratoService`.

## Escolha do `PratoResponseDTO`

A resposta expõe todos os campos do prato, incluindo o `id`. Em um app de comida saudável, as informações nutricionais (calorias, quantidade e unidade) são justamente o que o cliente usa para escolher, e nenhum campo de `Prato` é interno ou sensível. Manter um DTO separado da entidade ainda garante que campos adicionados à tabela no futuro, como dados de auditoria, não vazem automaticamente na API.

## Tratamento de erros (bônus)

O `GlobalExceptionHandler` (`@ControllerAdvice`) padroniza as respostas de erro. A API nunca devolve 500 por entrada inválida ou ID inexistente.

| Situação | Status | Corpo |
|---|---|---|
| Corpo reprovado no `@Valid` (`MethodArgumentNotValidException`) | 400 | mapa `erros` com campo → mensagem |
| Parâmetro inválido, ausente ou de tipo errado, ou JSON malformado | 400 | mensagem |
| `PratoNaoEncontradoException` | 404 | `"Prato não encontrado com o ID: X"` |
| Nome de prato duplicado | 409 | mensagem |

```json
{
  "status": 400,
  "mensagem": "Dados inválidos",
  "erros": { "nome": "O nome do prato é obrigatório", "valor": "O valor deve ser maior que zero" },
  "timestamp": "2026-09-29T12:00:00"
}
```

## Testes e evidências

- **Coleção do Postman:** `postman/NutriExpress.postman_collection.json`. Ela segue o roteiro do enunciado (criar 3 pratos, listar, buscar, 404, filtro, PUT, DELETE, `@Valid` com 400) e os bônus. Cada requisição tem testes automáticos. Para usar, importe no Postman e rode pelo *Collection Runner*, com a API no ar.
- **Testes automatizados** (MockMvc + H2, sem precisar do PostgreSQL):
  ```bash
  ./mvnw test
  ```

## Estrutura

```
src/main/java/br/com/nutriexpress/delivery/
├── NutriExpressApplication.java
├── model/        Prato.java (@Entity → tabela pratos)
├── repository/   PratoRepository.java (extends JpaRepository)
├── dto/          PratoRequestDTO, PratoResponseDTO, PratoValorRequestDTO, ErroResponseDTO (records)
├── service/      PratoService.java (regras de negócio + toEntity/toDTO)
├── controller/   PratoController.java
└── exception/    GlobalExceptionHandler, PratoNaoEncontradoException, PratoJaCadastradoException
src/main/resources/application.properties   (configuração do PostgreSQL)
```
