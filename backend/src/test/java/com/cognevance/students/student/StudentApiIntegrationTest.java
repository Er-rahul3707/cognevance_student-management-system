package com.cognevance.students.student;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
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
        String updated = valid.replace("Asha Patel", "Asha P.").replace("asha@example.com", "ASHA@example.com");
        mvc.perform(put("/api/students/" + id).contentType(MediaType.APPLICATION_JSON).content(updated)).andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Asha P."));
        mvc.perform(get("/api/students/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.email").value("asha@example.com"));
        mvc.perform(delete("/api/students/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/students/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void persistsChangesAcrossCommittedRequests() throws Exception {
        String email = "persistence-" + java.util.UUID.randomUUID() + "@example.com";
        String request = valid.replace("asha@example.com", email);
        MvcResult created = mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated()).andReturn();
        int id = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mvc.perform(get("/api/students/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
        mvc.perform(delete("/api/students/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/students/" + id)).andExpect(status().isNotFound());
    }

    @Test void searchesByEmailCourseAndNumericId() throws Exception {
        MvcResult created = mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(valid))
                .andExpect(status().isCreated()).andReturn();
        int id = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        mvc.perform(get("/api/students").param("q", "EXAMPLE.COM"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get("/api/students").param("q", "COMPUTER SCIENCE"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get("/api/students").param("q", String.valueOf(id)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get("/api/students").param("q", "no-match"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    @Test void rejectsInvalidAndDuplicateEmail() throws Exception {
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content("{\"fullName\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.fullName").exists());
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(valid)).andExpect(status().isCreated());
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(valid.replace("asha@example.com", "ASHA@example.com")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("A student with this email already exists"));
    }

    @Test void reportsEmailPhoneAndEnrollmentValidationErrors() throws Exception {
        String invalid = valid.replace("asha@example.com", "not-an-email")
                .replace("+91 98765 43210", "phone-number")
                .replace("2025-01-10", "2999-01-10");
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.phone").exists())
                .andExpect(jsonPath("$.fieldErrors.enrollmentDate").exists());
    }

    @Test void rejectsMalformedJsonWithBadRequest() throws Exception {
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content("{broken"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Request body is missing or malformed"));
    }

    @Test void rejectsNonNumericIdsAsBadRequest() throws Exception {
        mvc.perform(get("/api/students/not-a-number")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A path or query parameter has an invalid format"));
    }
}
