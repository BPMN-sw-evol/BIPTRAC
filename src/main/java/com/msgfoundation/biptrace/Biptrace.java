package com.msgfoundation.biptrace;

import java.util.Scanner;

public class Biptrace {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String projectPath = null;
        String outputFileName = "";
        String[] projectPaths = null;

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
            System.out.print("Ingrese la cantidad de proyectos a procesar: ");
            int numberOfProjects = scanner.nextInt();
            scanner.nextLine(); // Consumir el salto de línea

            projectPaths = new String[numberOfProjects];

            for (int i = 0; i < numberOfProjects; i++) {
                System.out.print("Ingrese la ruta del proyecto #" + (i + 1) + ": ");
                projectPaths[i] = scanner.nextLine();
            }

            System.out.print("Ingrese el nombre del archivo de salida (o presione Enter para dejarlo vacío): ");
            outputFileName = scanner.nextLine();
        }

        scanner.close();

        // Realizar acciones basadas en los valores obtenidos
        if (projectPath != null) {
            // Si se proporciona la ruta del proyecto a través de los argumentos
            AnnotationAnalyzer.analyzeAnnotationsInProject(projectPath, outputFileName);
        } else {
            // Si las rutas de los proyectos se ingresan desde la entrada estándar
            for (String path : projectPaths) {
                AnnotationAnalyzer.analyzeAnnotationsInProject(path, outputFileName);
            }
        }
    }
}
