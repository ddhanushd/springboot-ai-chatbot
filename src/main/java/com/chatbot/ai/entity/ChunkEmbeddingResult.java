package com.chatbot.ai.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChunkEmbeddingResult {
	
	 private List<String> chunks;
	 private List<List<Float>> embeddings;

}
