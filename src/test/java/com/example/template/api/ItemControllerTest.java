package com.example.template.api;

import com.example.template.api.dto.CreateItemRequest;
import com.example.template.api.dto.ItemResponse;
import com.example.template.config.SecurityConfig;
import com.example.template.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@Import(SecurityConfig.class)
class ItemControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ItemService itemService;

    @Test
    void listItems_returnsPage() throws Exception {
        var item = new ItemResponse(UUID.randomUUID(), "Widget", "A widget", Instant.now(), Instant.now());
        given(itemService.findAll(any(Pageable.class))).willReturn(new PageImpl<>(List.of(item)));

        mockMvc.perform(get("/api/v1/items")
                        .with(jwt().jwt(j -> j.claim("scope", "read"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Widget"));
    }

    @Test
    void createItem_returns201WithLocation() throws Exception {
        var id = UUID.randomUUID();
        var response = new ItemResponse(id, "Widget", "A widget", Instant.now(), Instant.now());
        var request = new CreateItemRequest("Widget", "A widget");

        given(itemService.create(any())).willReturn(response);

        mockMvc.perform(post("/api/v1/items")
                        .with(jwt().jwt(j -> j.claim("scope", "write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/items/" + id)));
    }

    @Test
    void createItem_withBlankName_returns422() throws Exception {
        var request = new CreateItemRequest("", null);

        mockMvc.perform(post("/api/v1/items")
                        .with(jwt().jwt(j -> j.claim("scope", "write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void createItem_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Widget\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteItem_returns204() throws Exception {
        var id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/items/{id}", id)
                        .with(jwt().jwt(j -> j.claim("scope", "write"))))
                .andExpect(status().isNoContent());
    }
}
