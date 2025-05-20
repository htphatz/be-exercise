package com.example.be_exercise.repository.httpclient;

import com.example.be_exercise.dto.n8n.N8nRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "${clients.n8n-client.name}", url = "${clients.n8n-client.url}")
public interface N8nClient {
    @PostMapping
    void sendToN8n(@RequestBody N8nRequest request);
}
