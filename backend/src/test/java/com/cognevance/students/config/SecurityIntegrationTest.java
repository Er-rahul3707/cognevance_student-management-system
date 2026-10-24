package com.cognevance.students.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:security;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.auth.username=test-admin",
        "app.auth.password=test-password-long-enough"
})
class SecurityIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    void rejectsMissingOrIncorrectCredentialsAndAllowsConfiguredAdmin() throws Exception {
        mvc.perform(get("/api/students")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/students").with(httpBasic("test-admin", "wrong-password")))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/students").with(httpBasic("test-admin", "test-password-long-enough")))
                .andExpect(status().isOk());
    }

    @Test
    void protectsWriteEndpointsAndAllowsAuthenticatedRequests() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/students")
                        .with(httpBasic("test-admin", "test-password-long-enough"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptsAuthorizationCorsPreflightFromConfiguredFrontend() throws Exception {
        mvc.perform(options("/api/students")
                        .header("Origin", "http://localhost:5500")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Access-Control-Allow-Origin", "http://localhost:5500"));
    }

    @Test
    void rejectsUnsafeAccountConfiguration() {
        SecurityConfig config = new SecurityConfig();
        assertThrows(IllegalStateException.class,
                () -> config.applicationUser("admin", "short", new BCryptPasswordEncoder()));
        assertThrows(IllegalStateException.class,
                () -> config.applicationUser("name:part", "test-password-long-enough", new BCryptPasswordEncoder()));
    }
}
