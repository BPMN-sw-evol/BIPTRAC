package com.MSGF.Trazability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TrazabilityApplication {
	static String projectPath = "D:\\Laboral\\MSG-Foundation\\MSGF-BPM-Engine";

	public static void main(String[] args) {
		SpringApplication.run(TrazabilityApplication.class, args);

		if (args.length > 0 && args[0].equalsIgnoreCase("-f")) {
			if (args.length > 1) {
				String outputFileName = args[1];
				performCustomAction(projectPath, outputFileName);
			} else {
				System.err.println("Missing output file name. Usage: java -jar yourjar.jar f <outputFileName>");
			}
		} else {
			// Llama a la lógica de análisis
			AnnotationAnalyzer.analyzeAnnotationsInProject(projectPath, "");
		}
	}

	private static void performCustomAction(String projectPath, String outputFileName) {
		AnnotationAnalyzer.analyzeAnnotationsInProject(projectPath, outputFileName);
	}
}