package com.cognevance.students.student;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @Transactional
@TestPropertySource(properties = {"spring.datasource.url=jdbc:h2:mem:students;MODE=MySQL;DB_CLOSE_DELAY=-1", "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=", "spring.jpa.hibernate.ddl-auto=create-drop"})
class StudentApiIntegrationTest {
    @Autowired MockMvc mvc;
    private final String valid = "{\"fullName\":\"Asha Patel\",\"email\":\"asha@example.com\",\"phone\":\"+91 98765 43210\",\"course\":\"Computer Science\",\"enrollmentDate\":\"2025-01-10\"}";
    @Test void createReadUpdateAndDeleteStudent() throws Exception {
        MvcResult created = mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(valid)).andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber()).andReturn();
        int id = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        mvc.perform(get("/api/students").param("q", "Asha")).andExpect(status().isOk()).andExpect(jsonPath("$[0].email").value("asha@example.com"));
        mvc.perform(get("/api/students/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Asha Patel"));
        String updated = valid.replace("Asha Patel", "Asha P.");
        mvc.perform(put("/api/students/" + id).contentType(MediaType.APPLICATION_JSON).content(updated)).andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Asha P."));
        mvc.perform(delete("/api/students/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/students/" + id)).andExpect(status().isNotFound());
    }
    @Test void rejectsInvalidAndDuplicateEmail() throws Exception {
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content("{\"fullName\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.fullName").exists());
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(valid)).andExpect(status().isCreated());
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(valid.replace("asha@example.com", "ASHA@example.com")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("A student with this email already exists"));
    }
    @Test void rejectsMalformedJsonWithBadRequest() throws Exception {
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content("{broken"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Request body is missing or malformed"));
    }
}
