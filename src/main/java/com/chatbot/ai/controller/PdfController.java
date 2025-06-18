package com.chatbot.ai.controller;

import com.chatbot.ai.service.Embeddingservice;
import com.chatbot.ai.service.PdfQAService;
import com.chatbot.ai.entity.ChatResponse;
import com.chatbot.ai.entity.ChunkEmbeddingResult;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    @Autowired
    private PdfQAService pdfService;

    @Autowired
    private Embeddingservice embeddingService;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";

    private List<String> chunks = new ArrayList<>();
    private List<List<Float>> chunkEmbeddings = new ArrayList<>();

    @PostMapping("/extract")
    public ResponseEntity<?> extractText(@RequestParam("file") MultipartFile file) {
//        try {
//            chunks = pdfService.extractTextChunks(file); // save for reuse
//            chunkEmbeddings = embeddingService.embedChunks(chunks);
//            return ResponseEntity.ok("PDF uploaded and embedded successfully. Ready to ask.");
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process PDF.");
//        }
    	
    	  try {
    	        ChunkEmbeddingResult result = pdfService.extractAndCacheIfNeeded(file);
    	        this.chunks = result.getChunks();
    	        this.chunkEmbeddings = result.getEmbeddings();
    	        return ResponseEntity.ok("PDF uploaded and embedded successfully.");
    	    } catch (IOException e) {
    	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process PDF.");
    	    }
    }

    @PostMapping("/ask")
    public ResponseEntity<?> askFromPdf(@RequestParam("question") String question) {
        if (chunks.isEmpty() || chunkEmbeddings.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload a PDF first using /extract.");
        }

        try {
            // Step 1: Embed the question
            List<Float> questionEmbedding = embeddingService.embedText(question);

            // Step 2: Find top 3 similar chunks
            Map<String, Float> similarityMap = new HashMap<>();
            for (int i = 0; i < chunkEmbeddings.size(); i++) {
                List<Float> chunkEmbedding = chunkEmbeddings.get(i);
                String chunkText = chunks.get(i);

                if (chunkEmbedding == null || chunkEmbedding.isEmpty()) {
                    continue; // Skip invalid embeddings
                }

                float score = cosineSimilarity(questionEmbedding, chunkEmbedding);
                similarityMap.put(chunkText, score);
            }

            if (similarityMap.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No valid chunks found for similarity comparison.");
            }


            // Step 3: Sort by similarity and select top 3
            List<String> topChunks = similarityMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .toList();

            String context = String.join("\n", topChunks);
            String prompt = String.format("""
            		You are an intelligent assistant. Use ONLY the provided context below to answer the user's question. 
            		If the answer cannot be found in the context, say "I don't know".

            		Context:
            		%s

            		Question:
            		%s

            		Answer in 1-2 lines, strictly based on the context.
            		""", context, question);
            // Step 4: Send to Ollama
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject body = new JSONObject();
            body.put("model", "llama3.2");
            body.put("prompt", prompt);
            body.put("stream", false);

            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(OLLAMA_URL, entity, String.class);

            JSONObject responseBody = new JSONObject(response.getBody());
            String answer = responseBody.getString("response");

            return ResponseEntity.ok(new ChatResponse(answer));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error answering the question.");
        }
    }

    // Cosine Similarity
    private float cosineSimilarity(List<Float> v1, List<Float> v2) {
        float dot = 0f, norm1 = 0f, norm2 = 0f;
        for (int i = 0; i < v1.size(); i++) {
            dot += v1.get(i) * v2.get(i);
            norm1 += v1.get(i) * v1.get(i);
            norm2 += v2.get(i) * v2.get(i);
        }
        return (float) (dot / (Math.sqrt(norm1) * Math.sqrt(norm2) + 1e-10));
    }
}
