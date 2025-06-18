package com.chatbot.ai.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.chatbot.ai.entity.Chunk;

@Service
public class Embeddingservice {
	
	 private static final String OLLAMA_EMBED_URL = "http://localhost:11434/api/embeddings";
	    private final RestTemplate restTemplate = new RestTemplate();

	    public List<List<Float>> embedChunks(List<String> chunks) {
	        List<List<Float>> embeddings = new ArrayList<>();
	        for (String chunk : chunks) {
	            embeddings.add(embedText(chunk));
	        }
	        return embeddings;
	    }

	    private List<Float> generateEmbedding(String text) {
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        String payload = String.format("""
	        {
	            "model": "nomic-embed-text",
	            "prompt": %s
	        }
	        """, JSONObject.quote(text)); // handles escaping

	        HttpEntity<String> request = new HttpEntity<>(payload, headers);
	        ResponseEntity<String> response = restTemplate.postForEntity(OLLAMA_EMBED_URL, request, String.class);

	        JSONObject json = new JSONObject(response.getBody());
	        JSONArray embedding = json.getJSONArray("embedding");

	        return embedding.toList().stream().map(e -> ((Number) e).floatValue()).toList();
	    }
	    
	    public List<Float> embedText(String text) {
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        JSONObject body = new JSONObject();
	        body.put("model", "llama3.2");
	        body.put("prompt", text);

	        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

	        ResponseEntity<String> response = restTemplate.postForEntity(OLLAMA_EMBED_URL, request, String.class);
	        JSONObject json = new JSONObject(response.getBody());

	        List<Object> rawList = json.getJSONArray("embedding").toList();
	        List<Float> result = new ArrayList<>();
	        for (Object o : rawList) {
	            result.add(((Number) o).floatValue());
	        }
	        return result;
	    }


}
