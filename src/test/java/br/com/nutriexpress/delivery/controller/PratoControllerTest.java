package br.com.nutriexpress.delivery.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.com.nutriexpress.delivery.repository.PratoRepository;

/**
 * Executa o roteiro de testes do enunciado (e os desafios extras) contra a API
 * completa: Controller -> Service -> Repository -> banco H2 em memória.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PratoControllerTest {

    private static final String BOWL_VEGANO = """
            {"nome": "Bowl de Quinoa", "descricao": "Quinoa, grão-de-bico e legumes assados",
             "valor": 32.90, "categoria": "vegano", "calorias": 420, "quantidade": 350, "unidadeMedida": "g"}
            """;
    private static final String FRANGO_FITNESS = """
            {"nome": "Frango com Batata-Doce", "descricao": "Peito de frango grelhado",
             "valor": 29.50, "categoria": "fitness", "calorias": 510, "quantidade": 400, "unidadeMedida": "g"}
            """;
    private static final String MOUSSE_SOBREMESA = """
            {"nome": "Mousse de Cacau", "descricao": "Cacau 70% e biomassa de banana",
             "valor": 14.00, "categoria": "sobremesa saudável", "calorias": 180, "quantidade": 120, "unidadeMedida": "ml"}
            """;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PratoRepository repository;

    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    @Test
    void postCriaPratoERetorna201ComLocation() throws Exception {
        mvc.perform(post("/pratos").contentType(MediaType.APPLICATION_JSON).content(BOWL_VEGANO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/pratos/" + idDoUnico())))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Bowl de Quinoa"))
                .andExpect(jsonPath("$.valor").value(32.90))
                .andExpect(jsonPath("$.categoria").value("vegano"))
                .andExpect(jsonPath("$.unidadeMedida").value("g"));
    }

    @Test
    void getListaTodosOsPratos() throws Exception {
        criar(BOWL_VEGANO);
        criar(FRANGO_FITNESS);
        criar(MOUSSE_SOBREMESA);

        mvc.perform(get("/pratos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void getPorIdRetorna200() throws Exception {
        long id = criar(FRANGO_FITNESS);

        mvc.perform(get("/pratos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Frango com Batata-Doce"));
    }

    @Test
    void getPorIdInexistenteRetorna404() throws Exception {
        mvc.perform(get("/pratos/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Prato não encontrado com o ID: 999"));
    }

    @Test
    void getPorCategoriaFiltraIgnorandoMaiusculas() throws Exception {
        criar(BOWL_VEGANO);
        criar(FRANGO_FITNESS);

        mvc.perform(get("/pratos").param("categoria", "Vegano"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Bowl de Quinoa"));
    }

    @Test
    void putAtualizaPratoERetorna200() throws Exception {
        long id = criar(BOWL_VEGANO);
        String atualizado = BOWL_VEGANO.replace("32.90", "35.00").replace("420", "450");

        mvc.perform(put("/pratos/{id}", id).contentType(MediaType.APPLICATION_JSON).content(atualizado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.valor").value(35.00))
                .andExpect(jsonPath("$.calorias").value(450));
    }

    @Test
    void putInexistenteRetorna404() throws Exception {
        mvc.perform(put("/pratos/{id}", 999).contentType(MediaType.APPLICATION_JSON).content(BOWL_VEGANO))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRemoveERetorna204() throws Exception {
        long id = criar(MOUSSE_SOBREMESA);

        mvc.perform(delete("/pratos/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/pratos/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void deleteInexistenteRetorna404() throws Exception {
        mvc.perform(delete("/pratos/{id}", 999)).andExpect(status().isNotFound());
    }

    @Test
    void postInvalidoRetorna400ComMapaDeErros() throws Exception {
        String invalido = """
                {"nome": "  ", "valor": -5, "categoria": "churrasco", "calorias": 100, "quantidade": 200, "unidadeMedida": "kg"}
                """;

        mvc.perform(post("/pratos").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").value("O nome do prato é obrigatório"))
                .andExpect(jsonPath("$.erros.valor").value("O valor deve ser maior que zero"))
                .andExpect(jsonPath("$.erros.categoria").exists())
                .andExpect(jsonPath("$.erros.unidadeMedida").exists());
    }

    @Test
    void regraDeNegocioBloqueiaNomeDuplicadoCom409() throws Exception {
        criar(BOWL_VEGANO);
        String mesmoNome = BOWL_VEGANO.replace("Bowl de Quinoa", "  bowl de quinoa ");

        mvc.perform(post("/pratos").contentType(MediaType.APPLICATION_JSON).content(mesmoNome))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void regraDeNegocioBloqueiaPutParaNomeDeOutroPrato() throws Exception {
        criar(BOWL_VEGANO);
        long idFrango = criar(FRANGO_FITNESS);
        String roubandoNome = FRANGO_FITNESS.replace("Frango com Batata-Doce", "Bowl de Quinoa");

        mvc.perform(put("/pratos/{id}", idFrango).contentType(MediaType.APPLICATION_JSON).content(roubandoNome))
                .andExpect(status().isConflict());
    }

    @Test
    void patchValorAlteraSomenteOValor() throws Exception {
        long id = criar(FRANGO_FITNESS);

        mvc.perform(patch("/pratos/{id}/valor", id).contentType(MediaType.APPLICATION_JSON).content("{\"valor\": 27.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(27.00))
                .andExpect(jsonPath("$.nome").value("Frango com Batata-Doce"))
                .andExpect(jsonPath("$.calorias").value(510));
    }

    @Test
    void patchValorInvalidoOuInexistente() throws Exception {
        long id = criar(FRANGO_FITNESS);

        mvc.perform(patch("/pratos/{id}/valor", id).contentType(MediaType.APPLICATION_JSON).content("{\"valor\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.valor").exists());
        mvc.perform(patch("/pratos/{id}/valor", 999).contentType(MediaType.APPLICATION_JSON).content("{\"valor\": 10}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCaloriasFiltraPeloMaximo() throws Exception {
        criar(BOWL_VEGANO);
        criar(FRANGO_FITNESS);
        criar(MOUSSE_SOBREMESA);

        mvc.perform(get("/pratos/calorias").param("max", "450"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("Mousse de Cacau"))
                .andExpect(jsonPath("$[1].nome").value("Bowl de Quinoa"));
    }

    @Test
    void entradasMalformadasRetornam400EmVezDe500() throws Exception {
        mvc.perform(get("/pratos/calorias").param("max", "-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/pratos/calorias")).andExpect(status().isBadRequest());
        mvc.perform(get("/pratos/abc")).andExpect(status().isBadRequest());
        mvc.perform(post("/pratos").contentType(MediaType.APPLICATION_JSON).content("{ isso não é json"))
                .andExpect(status().isBadRequest());
    }

    private long criar(String json) throws Exception {
        String resposta = mvc.perform(post("/pratos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(resposta.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private long idDoUnico() {
        return repository.findAll().get(0).getId();
    }
}
