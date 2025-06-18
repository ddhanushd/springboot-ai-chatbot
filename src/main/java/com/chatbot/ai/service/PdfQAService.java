package com.chatbot.ai.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.chatbot.ai.entity.Chunk;
import com.chatbot.ai.entity.ChunkEmbedding;
import com.chatbot.ai.entity.ChunkEmbeddingResult;
import com.chatbot.ai.repository.ChunkEmbeddingRepository;
import com.chatbot.ai.util.SimilarityUtil;

@Service
public class PdfQAService {

	private final Embeddingservice embeddingService = new Embeddingservice();
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Autowired
    private ChunkEmbeddingRepository chunkRepo;

    public String askQuestion(String question, List<Chunk> chunks) {
        // Step 1: Embed question
        List<Float> questionEmbedding = embeddingService.embedText(question);

        // Step 2: Compute similarity
        List<Chunk> topChunks = chunks.stream()
                .sorted(Comparator.comparingDouble(c -> -SimilarityUtil.cosineSimilarity(questionEmbedding, c.getEmbedding())))
                .limit(3)
                .collect(Collectors.toList());

        // Step 3: Build prompt
        String context = topChunks.stream().map(Chunk::getText).collect(Collectors.joining("\n\n"));
        String fullPrompt = "Answer the question based on the following context:\n\n" + context + "\n\nQuestion: " + question;

        return generateAnswer(fullPrompt);
    }

    private String generateAnswer(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject payload = new JSONObject();
        payload.put("model", "llama3.2");
        payload.put("prompt", prompt);
        payload.put("stream", false);

        HttpEntity<String> request = new HttpEntity<>(payload.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:11434/api/generate", request, String.class);

        return new JSONObject(response.getBody()).getString("response");
    }
    
    public List<String> extractTextChunks(MultipartFile file) throws IOException {
        PDDocument document = PDDocument.load(file.getInputStream());
        PDFTextStripper stripper = new PDFTextStripper();
        String fullText = stripper.getText(document);
        document.close();

        // Split into chunks
        List<String> chunks = new ArrayList<>();
        int chunkSize = 500; // characters per chunk
        for (int start = 0; start < fullText.length(); start += chunkSize) {
            int end = Math.min(fullText.length(), start + chunkSize);
            chunks.add(fullText.substring(start, end));
        }

        return chunks;
    }
    
 // Inside PdfQAService.java
    public ChunkEmbeddingResult extractAndCacheIfNeeded(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        List<String> chunks;
        List<List<Float>> embeddings;

        List<ChunkEmbedding> existingChunks = chunkRepo.findByFileName(fileName);

        if (!existingChunks.isEmpty()) {
            chunks = existingChunks.stream().map(ChunkEmbedding::getChunkText).toList();
            embeddings = existingChunks.stream().map(ChunkEmbedding::getEmbedding).toList();
            return new ChunkEmbeddingResult(chunks, embeddings);
        }

        chunks = extractTextChunks(file);
        embeddings = embeddingService.embedChunks(chunks);

        for (int i = 0; i < chunks.size(); i++) {
            ChunkEmbedding ce = new ChunkEmbedding();
            ce.setChunkText(chunks.get(i));
            ce.setEmbedding(embeddings.get(i));
            ce.setFileName(fileName);
            chunkRepo.save(ce);
        }

        return new ChunkEmbeddingResult(chunks, embeddings);
    }

}
