package com.jacksam.productfilter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loginAndGetToken(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    // ─── Access control ────────────────────────────────────

    @Test
    void admin_operations_permitted() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void manager_cannot_accessAdminEndpoints() throws Exception {
        String token = loginAndGetToken("manager", "manager123");

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/user-access/bulk-grant")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userIds":[3],"productIds":[1],"accessLevel":"READ"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticated_adminEndpoint_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    // ─── User management ───────────────────────────────────

    @Test
    void createUser_success() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"secret","email":"alice@x.com",
                                 "displayName":"Alice","roleName":"VIEWER","departmentId":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.roles[0]").value("VIEWER"))
                .andExpect(jsonPath("$.departmentId").value(1));
    }

    @Test
    void createUser_duplicateUsername_returnsBadRequest() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"x","email":"x@x.com",
                                 "displayName":"Admin","roleName":"VIEWER","departmentId":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already exists: admin"));
    }

    @Test
    void createUser_unknownRole_returnsBadRequest() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"bob","password":"x","email":"bob@x.com",
                                 "displayName":"Bob","roleName":"GHOST","departmentId":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Role not found: GHOST"));
    }

    // ─── Access grant / revoke ─────────────────────────────

    @Test
    void grantAccess_viewerSeesProduct_thenRevoke() throws Exception {
        String adminToken = loginAndGetToken("admin", "admin123");
        String viewerToken = loginAndGetToken("viewer", "viewer123");

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(post("/api/admin/user-access/bulk-grant")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userIds":[3],"productIds":[1],"accessLevel":"READ"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(delete("/api/admin/user-access/3/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ─── Categories ────────────────────────────────────────

    @Test
    void listAndCreateCategory() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(get("/api/admin/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Books","description":"Reading","parentCategoryId":null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Books"));
    }

    @Test
    void updateCategory() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        String created = mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Books","description":"Reading","parentCategoryId":null}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(put("/api/admin/categories/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"E-Books","description":"Digital reading","parentCategoryId":null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("E-Books"));
    }

    @Test
    void deleteCategory() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        String created = mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Books","description":"Reading","parentCategoryId":null}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(delete("/api/admin/categories/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ─── Filter rules ──────────────────────────────────────

    @Test
    void filterRuleCrud() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(get("/api/admin/filter-rules")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        String created = mockMvc.perform(post("/api/admin/filter-rules")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Premium","field":"price","operator":"gt",
                                 "ruleValue":"2000","actionType":"TAG","actionValue":"premium","enabled":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Premium"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(put("/api/admin/filter-rules/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Premium Updated","field":"price","operator":"gt",
                                 "ruleValue":"2500","actionType":"TAG","actionValue":"premium","enabled":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Premium Updated"));

        mockMvc.perform(delete("/api/admin/filter-rules/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/filter-rules")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void evaluateFilterRules_returnsMatchingActions() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(post("/api/admin/filter-rules")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Premium","field":"price","operator":"gt",
                                 "ruleValue":"2000","actionType":"TAG","actionValue":"premium","enabled":true}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/filter-rules/evaluate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"product":{"name":"MacBook Pro 16","price":2499.99,"quantity":25}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("TAG"))
                .andExpect(jsonPath("$[0].value").value("premium"));
    }

    // ─── Audit logs ────────────────────────────────────────

    @Test
    void getAuditLogs_afterProductCreate() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Audited Widget","price":5.0,"quantity":1}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/audit-logs")
                        .header("Authorization", "Bearer " + token)
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].resourceType").value("PRODUCT"));
    }

    @Test
    void getAuditLogs_filterByUser() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(get("/api/admin/audit-logs")
                        .header("Authorization", "Bearer " + token)
                        .param("userId", "1"))
                .andExpect(status().isOk());
    }
}
