package com.chatbot.ai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chatbot.ai.entity.ChunkEmbedding;

public interface ChunkEmbeddingRepository extends JpaRepository<ChunkEmbedding, Long> {
	
	 List<ChunkEmbedding> findByFileName(String fileName);
}
