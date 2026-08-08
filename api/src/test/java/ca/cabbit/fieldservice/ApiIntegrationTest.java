package ca.cabbit.fieldservice;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class ApiIntegrationTest {
    @Autowired MockMvc mvc; @Autowired ObjectMapper json;
    private String token;
    @BeforeEach void login() throws Exception {
        String body=mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"admin@test.local","password":"ChangeMe123!","deviceId":"test-device"}
                """)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        token=json.readTree(body).get("accessToken").asText();
    }
    @Test void protectsAuthenticatedEndpoints(){try{mvc.perform(get("/api/v1/resources/customers")).andExpect(status().isUnauthorized());}catch(Exception e){throw new RuntimeException(e);}}
    @Test void createsAndReadsTenantResource() throws Exception {
        String created=mvc.perform(post("/api/v1/resources/customers").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("""
                {"data":{"name":"Maple Test Customer","phone":"555-0100"}}
                """)).andExpect(status().isCreated()).andExpect(jsonPath("$.version").value(0)).andReturn().getResponse().getContentAsString();
        assertThat(json.readTree(created).get("id").asText()).isNotBlank();
        mvc.perform(get("/api/v1/resources/customers").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.items[0].data.name").value("Maple Test Customer"));
    }
    @Test void syncIsIdempotent() throws Exception {
        String operation="22222222-2222-2222-2222-222222222222";String entity="33333333-3333-3333-3333-333333333333";
        String payload="{\"deviceId\":\"test-device\",\"operations\":[{\"operationId\":\""+operation+"\",\"entityType\":\"materials\",\"entityId\":\""+entity+"\",\"action\":\"CREATE\",\"localTimestamp\":\"2026-01-01T00:00:00Z\",\"payload\":{\"name\":\"Salt\"}}]}";
        mvc.perform(post("/api/v1/sync").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isOk()).andExpect(jsonPath("$.results[0].status").value("APPLIED"));
        mvc.perform(post("/api/v1/sync").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isOk()).andExpect(jsonPath("$.results[0].status").value("APPLIED"));
    }
}

