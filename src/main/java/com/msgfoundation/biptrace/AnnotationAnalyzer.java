/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.msgfoundation.biptrace;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
/**
 *
 * @author danno_nz
 */
public class AnnotationAnalyzer {
    public static void analyzeAnnotationsInProject(String projectPath, String outputFileName) {
        File projectDirectory = new File(projectPath);

        if (!projectDirectory.exists() || !projectDirectory.isDirectory()) {
            System.err.println("Invalid project path.");
            return;
        }

        List<String> customAnnotations = Arrays.asList("BPMNTask", "BPMNGetVariables", "BPMNSetVariables", "BPMNGetterVariables", "BPMNSetterVariables");

        processJavaFiles(projectDirectory, customAnnotations, outputFileName);
    }

    private static void processJavaFiles(File directory, List<String> customAnnotations, String outputFileName) {
        ObjectNode result = JsonNodeFactory.instance.objectNode(); // Objeto JSON para almacenar todas las anotaciones

        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                processJavaFiles(file, customAnnotations, outputFileName);
            } else if (file.getName().endsWith(".java")) {
                processJavaFile(file, customAnnotations, result);
            }
        }

        try {
            // Verifica si el objeto result tiene al menos un campo antes de imprimir
            if (result.size() > 0) {
                if (!outputFileName.isEmpty() || !outputFileName.equals("")){
                    saveJsonToFile(result, outputFileName);
                }else {
                    // Imprime el JSON resultante después de procesar todas las anotaciones
                    String json = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(result);
                    System.out.println(json);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processJavaFile(File file, List<String> customAnnotations, ObjectNode result) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(file);

            List<TypeDeclaration<?>> types = cu.getTypes();

            for (TypeDeclaration<?> type : types) {
                ObjectNode classNode = JsonNodeFactory.instance.objectNode();

                List<AnnotationExpr> classAnnotations = type.getAnnotations();
                for (AnnotationExpr annotation : classAnnotations) {
                    if (customAnnotations.contains(annotation.getNameAsString())) {
                        processAnnotations(annotation, classNode, "Class: " + type.getNameAsString());
                    }
                }

                List<MethodDeclaration> methods = type.getMethods();
                for (MethodDeclaration method : methods) {
                    List<AnnotationExpr> methodAnnotations = method.getAnnotations();
                    for (AnnotationExpr annotation : methodAnnotations) {
                        if (customAnnotations.contains(annotation.getNameAsString())) {
                            processAnnotations(annotation, classNode, "Method: " + formatMethodName(method, annotation.getNameAsString()));
                        }
                    }
                }

                List<FieldDeclaration> fields = type.getFields();
                for (FieldDeclaration field : fields) {
                    List<AnnotationExpr> fieldAnnotations = field.getAnnotations();
                    for (AnnotationExpr annotation : fieldAnnotations) {
                        if (customAnnotations.contains(annotation.getNameAsString())) {
                            processAnnotations(annotation, classNode, "Field: " + formatFieldName(field, annotation.getNameAsString()));
                        }
                    }
                }

                // Verifica si el nodo de la clase tiene al menos un elemento antes de agregarlo al resultado
                if (classNode.size() > 0) {
                    result.set("BPM Class: " + type.getNameAsString(), classNode);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processAnnotations(AnnotationExpr annotation, ObjectNode classNode, String elementName) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode annotationNode = objectMapper.createObjectNode();

        if (annotation.isNormalAnnotationExpr()) {
            NormalAnnotationExpr normalAnnotation = annotation.asNormalAnnotationExpr();

            for (MemberValuePair pair : normalAnnotation.getPairs()) {
                String key = pair.getNameAsString();
                JsonNode valueNode;

                if (pair.getValue().isArrayInitializerExpr()) {
                    ArrayInitializerExpr arrayInitializer = pair.getValue().asArrayInitializerExpr();
                    ArrayNode variablesArray = objectMapper.createArrayNode();

                    for (Expression arrayValue : arrayInitializer.getValues()) {
                        if (arrayValue.isStringLiteralExpr()) {
                            String value = arrayValue.asStringLiteralExpr().getValue();
                            variablesArray.add(value);
                        }
                    }

                    valueNode = variablesArray;
                } else {
                    String value = pair.getValue().toString().replaceAll("\"", "");
                    valueNode = objectMapper.valueToTree(value);
                }

                annotationNode.set(key, valueNode);
            }

            // Verifica si el nodo de la anotación tiene al menos un elemento antes de agregarlo al nodo de la clase
            if (annotationNode.size() > 0) {
                classNode.set("BPM " + elementName, annotationNode);
            } else {
                classNode.put("BPM " + elementName, "No additional information");
            }
        } else {
            // Si no es de tipo NormalAnnotationExpr, aún así, agrega la información básica al nodo de la clase
            classNode.put("BPM " + elementName, "No additional information");
        }
    }

    private static String formatMethodName(MethodDeclaration method, String annotationName) {
        String methodName = method.getNameAsString();
        if (annotationName.toLowerCase().contains("setter")) {
            return methodName + " (setter)";
        } else if (annotationName.toLowerCase().contains("getter")) {
            return methodName + " (getter)";
        }
        return methodName;
    }

    private static String formatFieldName(FieldDeclaration field, String annotationName) {
        String fieldName = field.getVariable(0).getNameAsString();
        if (annotationName.toLowerCase().contains("set")) {
            return fieldName + " (set)";
        } else if (annotationName.toLowerCase().contains("get")) {
            return fieldName + " (get)";
        }
        return fieldName;
    }




    private static void saveJsonToFile(ObjectNode result, String outputFileName) {
        try {
            // Carpeta de salida constante
            String outputFolderPath = "output";

            File outputFolder = new File(outputFolderPath);
            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }

            String fileName = outputFileName + ".json";
            File outputFile = new File(outputFolderPath, fileName);

            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(result.toPrettyString());
            }

            System.out.println("JSON file saved to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
