package com.MSGF.Trazability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TrazabilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrazabilityApplication.class, args);

		// Llama a la lógica de análisis
		AnnotationAnalyzer.analyzeAnnotationsInProject("D:\\Laboral\\MSG-Foundation\\MSGF-BPM-Engine");
	}
}
