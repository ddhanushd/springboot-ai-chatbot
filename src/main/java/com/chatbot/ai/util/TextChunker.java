package com.chatbot.ai.util;

import java.util.ArrayList;
import java.util.List;

public class TextChunker {
	
	  public static List<String> chunkText(String text, int maxWords) {
	        String[] sentences = text.split("(?<=[\\.\\!\\?])\\s+"); // split by sentence
	        List<String> chunks = new ArrayList();
	        StringBuilder chunk = new StringBuilder();
	        int wordCount = 0;

	        for (String sentence : sentences) {
	            int sentenceWords = sentence.trim().split("\\s+").length;

	            if (wordCount + sentenceWords > maxWords) {
	                chunks.add(chunk.toString().trim());
	                chunk = new StringBuilder();
	                wordCount = 0;
	            }

	            chunk.append(sentence).append(" ");
	            wordCount += sentenceWords;
	        }

	        if (!chunk.isEmpty()) {
	            chunks.add(chunk.toString().trim());
	        }

	        return chunks;
	    }

}
