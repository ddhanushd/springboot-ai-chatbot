package com.chatbot.ai.util;

import java.util.List;

public class SimilarityUtil {
	
	 public static double cosineSimilarity(List<Float> vec1, List<Float> vec2) {
	        if (vec1.size() != vec2.size()) throw new IllegalArgumentException("Vector sizes must match");

	        double dotProduct = 0.0, normA = 0.0, normB = 0.0;

	        for (int i = 0; i < vec1.size(); i++) {
	            double a = vec1.get(i);
	            double b = vec2.get(i);
	            dotProduct += a * b;
	            normA += a * a;
	            normB += b * b;
	        }

	        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	    }

}
