package com.chatbot.ai.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Chunk {
	
	private String text;
	private List<Float> embedding;

}
