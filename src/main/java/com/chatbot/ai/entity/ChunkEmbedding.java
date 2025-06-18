package com.chatbot.ai.entity;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChunkEmbedding {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@Column(columnDefinition = "TEXT")
    private String chunkText;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "chunk_embedding_values", joinColumns = @JoinColumn(name = "chunk_id"))
    @Column(name = "embedding_value")
    private List<Float> embedding;

    @Column(columnDefinition = "TEXT")
    private String fileName;


}
