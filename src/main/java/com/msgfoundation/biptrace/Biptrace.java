package com.msgfoundation.biptrace;

public class Biptrace {

    public static void main(String[] args) {
        String projectPath = null; // Inicializado como null
        String outputFileName = ""; // Por defecto, una cadena vacía

        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("-p") && i + 1 < args.length) {
                    projectPath = args[i + 1];
                    i++; // Avanzar al siguiente argumento después de -p
                } else if (args[i].equalsIgnoreCase("-f")) {
                    if (i + 1 < args.length) {
                        outputFileName = args[i + 1];
                        i++; // Avanzar al siguiente argumento después de -f
                    } else {
                        System.err.println("Missing output file name. Usage: java -jar yourjar.jar -p 'projectPath' [-f <outputFileName>]");
                        return;
                    }
                }
            }
        } else {
            System.err.println("Please provide project path using -p 'projectPath'.");
            return;
        }

        // Si projectPath sigue siendo null, significa que -p no fue proporcionado correctamente
        if (projectPath == null) {
            System.err.println("Missing project path. Usage: java -jar yourjar.jar -p <projectPath> [-f <outputFileName>]");
            return;
        }

        // Realizar acciones basadas en los valores obtenidos
        AnnotationAnalyzer.analyzeAnnotationsInProject(projectPath, outputFileName);
    }
}

