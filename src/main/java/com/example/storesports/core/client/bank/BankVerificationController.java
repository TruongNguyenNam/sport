package com.example.storesports.core.client.bank;

import com.example.storesports.core.client.returnoder.payload.request.VerifyRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/client/bank")
public class BankVerificationController {

    @Value("${vietqr.api.client-id}")
    private String clientId;

    @Value("${vietqr.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest req) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", clientId);
        headers.set("x-api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("bin", req.getBankCode());
        body.put("accountNumber", req.getAccountNumber());

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.vietqr.io/v2/lookup",
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());

            if (!"00".equals(root.path("code").asText())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Xác minh thất bại: " + root.path("desc").asText()));
            }

            String accountName = root.path("data").path("accountName").asText(null);

            if (accountName == null || accountName.isBlank()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy tên tài khoản"));
            }

            return ResponseEntity.ok(Map.of("accountName", accountName));

        } catch (Exception e) {
            e.printStackTrace(); // optional
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Không thể xác minh tài khoản: " + e.getMessage()));
        }
    }
}
