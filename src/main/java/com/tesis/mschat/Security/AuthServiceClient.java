package com.tesis.mschat.Security;


import com.tesis.mschat.model.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceClient.class);

    private String authServiceUrl = "http://localhost:8080/api/ms-auth/auth";

    private final RestTemplate restTemplate = new RestTemplate();

    public UserDTO validateTokenAndGetUser(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<UserDTO> response = restTemplate.exchange(
                authServiceUrl + "/token/getUser",
                HttpMethod.POST,
                entity,
                UserDTO.class
            );
            return response.getStatusCode() == HttpStatus.OK ? response.getBody() : null;
        } catch (Exception e) {
            logger.error("Error validating token with auth service", e);
            return null;
        }
    }
}
