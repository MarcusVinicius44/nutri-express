# NutriExpress — API de Pratos

API REST de delivery de comida saudável feita com Spring Boot e PostgreSQL.

## Como rodar

Requisitos: **Java 17 ou superior** e **PostgreSQL**.

**1. Baixar o projeto**

```bash
git clone https://github.com/MarcusVinicius44/nutri-express.git
cd nutri-express
```

**2. Banco de dados** (escolha uma opção)

- **Docker:**
  ```bash
  docker run --name nutri-express-postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres
  ```
- **PostgreSQL instalado no computador:** a aplicação conecta em `localhost:5432`, banco `postgres`, usuário `postgres`, senha `postgres`. Se a sua senha for outra, defina a variável `DB_PASSWORD` antes de rodar (no PowerShell: `$env:DB_PASSWORD="sua_senha"`).

**3. Aplicação** (na pasta do projeto)

```bash
./mvnw spring-boot:run      # Linux/macOS
.\mvnw.cmd spring-boot:run  # Windows (PowerShell)
```

A API sobe em `http://localhost:8080`, e a tabela `pratos` é criada automaticamente.

## Endpoints

| Verbo | Rota | Descrição | Status |
|---|---|---|---|
| GET | `/pratos` | Lista todos os pratos | 200 |
| GET | `/pratos/{id}` | Busca um prato pelo ID | 200 / 404 |
| GET | `/pratos?categoria=vegano` | Filtra por categoria | 200 |
| POST | `/pratos` | Cria um prato | 201 / 400 / 409 |
| PUT | `/pratos/{id}` | Atualiza um prato | 200 / 400 / 404 / 409 |
| DELETE | `/pratos/{id}` | Remove um prato | 204 / 404 |
| PATCH | `/pratos/{id}/valor` | Altera só o valor (bônus) | 200 / 400 / 404 |
| GET | `/pratos/calorias?max=500` | Pratos com até X calorias (bônus) | 200 / 400 |

- **400:** dados inválidos.
- **404:** prato não encontrado.
- **409:** já existe um prato com o mesmo nome (regra de negócio).

**Exemplo de corpo (POST / PUT):**

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

- `categoria`: `vegano`, `low carb`, `fitness` ou `sobremesa saudável`.
- `unidadeMedida`: `g` ou `ml`.

**Exemplo de corpo (PATCH `/pratos/{id}/valor`):**

```json
{ "valor": 27.00 }
```

## Testes

A coleção do Postman com os testes está em `postman/NutriExpress.postman_collection.json`.
