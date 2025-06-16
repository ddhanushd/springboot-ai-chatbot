package com.chatbot.ai.controller;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.chatbot.ai.entity.ChatRequest;
import com.chatbot.ai.entity.ChatResponse;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
	
	 private final RestTemplate restTemplate = new RestTemplate();
	    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";

	    @PostMapping
	    public ChatResponse chat(@RequestBody ChatRequest request) {
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        String json = String.format("""
	            {
	              "model": "llama3.2",
	              "prompt": "%s",
	              "stream": false
	            }
	        """, request.getPrompt());

	        HttpEntity<String> entity = new HttpEntity<>(json, headers);
	        ResponseEntity<String> response = restTemplate.postForEntity(OLLAMA_URL, entity, String.class);

	        // Extract 'response' from Ollama's JSON
	        String result = new JSONObject(response.getBody()).getString("response");

	        return new ChatResponse(result);
	    }

}
