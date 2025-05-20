package com.example.be_exercise.repository.httpclient;

import com.example.be_exercise.dto.brevo.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "${clients.brevo-client.name}", url = "${clients.brevo-client.url}")
public interface BrevoClient {
    @PostMapping
    void sendEmail(@RequestHeader(value = "api-key") String apiKey, @RequestBody EmailRequest request);
}