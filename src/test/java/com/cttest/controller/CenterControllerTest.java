package com.cttest.controller;

import com.cttest.model.entity.Center;
import com.cttest.service.CenterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CenterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CenterService centerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void getAll_ShouldReturnCenters() throws Exception {
        Center center = new Center();
        center.setId(1);
        center.setName("Test Center");
        Page<Center> page = new PageImpl<>(List.of(center));
        when(centerService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/centers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Center"));
    }

    @Test
    @WithMockUser
    void getById_ShouldReturnCenter() throws Exception {
        Center center = new Center();
        center.setId(1);
        center.setName("Test Center");
        when(centerService.findById(1)).thenReturn(center);

        mockMvc.perform(get("/api/centers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Center"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_ShouldReturnCreatedCenter() throws Exception {
        Center center = new Center();
        center.setName("New Center");

        Center saved = new Center();
        saved.setId(1);
        saved.setName("New Center");
        when(centerService.save(any(Center.class))).thenReturn(saved);

        mockMvc.perform(post("/api/centers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(center)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Center"));
    }

}
