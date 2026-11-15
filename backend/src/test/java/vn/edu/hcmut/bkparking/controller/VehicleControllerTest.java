package vn.edu.hcmut.bkparking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loginAndGetToken(String username, String password) throws Exception {
        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        MvcResult res = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();
    }

    // -------------------------------------------------------------------------
    // 1. PHÂN HỆ USER: TEST CASES HỢP LỆ & NGOẠI LỆ
    // -------------------------------------------------------------------------

    @Test
    void testGetMyVehicles_Success() throws Exception {
        String token = loginAndGetToken("sv.tran", "123456");

        mockMvc.perform(get("/api/v1/vehicles/my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testRegisterVehicle_Success() throws Exception {
        String token = loginAndGetToken("sv.tran", "123456");

        String payload = """
                {
                  "licensePlate": "59T1-999.01",
                  "vehicleType": "MOTORBIKE",
                  "brand": "Yamaha",
                  "model": "Grande",
                  "color": "Trắng"
                }
                """;

        mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.licensePlate").value("59T1-999.01"));
    }

    @Test
    void testRegisterVehicle_DuplicatePlate_Returns409() throws Exception {
        String token = loginAndGetToken("sv.tran", "123456");

        String duplicatePayload = """
                {
                  "licensePlate": "59X3-682.45",
                  "vehicleType": "MOTORBIKE"
                }
                """;

        mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicatePayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("DUPLICATE_LICENSE_PLATE"));
    }

    @Test
    void testRegisterVehicle_MissingLicensePlate_Returns400() throws Exception {
        String token = loginAndGetToken("sv.tran", "123456");

        String invalidPayload = """
                {
                  "vehicleType": "MOTORBIKE"
                }
                """;

        mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void testRegisterVehicle_ExceedLimit_Returns400() throws Exception {
        String token = loginAndGetToken("sv.nguyen", "123456");

        String payload = """
                {
                  "licensePlate": "61D1-888.88",
                  "vehicleType": "MOTORBIKE",
                  "brand": "Honda",
                  "model": "Future",
                  "color": "Đỏ"
                }
                """;

        mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VEHICLE_LIMIT_EXCEEDED"));
    }

    @Test
    void testGetMyVehicles_Unauthorized_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/my"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    // -------------------------------------------------------------------------
    // 2. PHÂN HỆ ADMIN: DUYỆT / TỪ CHỐI & XEM DANH SÁCH
    // -------------------------------------------------------------------------

    @Test
    void testAdminGetVehicles_Pending_Success() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");

        mockMvc.perform(get("/api/v1/admin/vehicles")
                        .param("status", "PENDING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    void testAdminApproveVehicle_Success_SavesReviewerAndDate() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");

        mockMvc.perform(patch("/api/v1/admin/vehicles/2/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.qrToken").isNotEmpty())
                .andExpect(jsonPath("$.data.reviewerName").value("Phạm Quản Trị"))
                .andExpect(jsonPath("$.data.reviewedAt").isNotEmpty());
    }

    @Test
    void testAdminRejectVehicle_Success_SavesReasonAndReviewer() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");

        String rejectPayload = """
                {
                  "reason": "Ảnh chụp giấy đăng ký xe bị mờ, không khớp số khung"
                }
                """;

        mockMvc.perform(patch("/api/v1/admin/vehicles/2/reject")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rejectPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectionReason").value("Ảnh chụp giấy đăng ký xe bị mờ, không khớp số khung"))
                .andExpect(jsonPath("$.data.qrToken").isEmpty())
                .andExpect(jsonPath("$.data.reviewerName").value("Phạm Quản Trị"))
                .andExpect(jsonPath("$.data.reviewedAt").isNotEmpty());
    }

    @Test
    void testAdminEndpoints_ForbiddenForRegularUser() throws Exception {
        String userToken = loginAndGetToken("sv.tran", "123456");

        mockMvc.perform(get("/api/v1/admin/vehicles")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mockMvc.perform(patch("/api/v1/admin/vehicles/2/approve")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }
}
