package com.MSGF.Trazability;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AnnotationAnalyzer {
    public static void analyzeAnnotationsInProject(String projectPath) {
        File projectDirectory = new File(projectPath);

        if (!projectDirectory.exists() || !projectDirectory.isDirectory()) {
            System.err.println("Invalid project path.");
            return;
        }

        List<String> customAnnotations = Arrays.asList("BPMNTask", "SetVariables", "GetVariables");

        processJavaFiles(projectDirectory, customAnnotations);
    }

    private static void processJavaFiles(File directory, List<String> customAnnotations) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                processJavaFiles(file, customAnnotations);
            } else if (file.getName().endsWith(".java")) {
                processJavaFile(file, customAnnotations);
            }
        }
    }

    private static void processJavaFile(File file, List<String> customAnnotations) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(file);

            List<TypeDeclaration<?>> types = cu.getTypes();

            for (TypeDeclaration<?> type : types) {
                List<AnnotationExpr> classAnnotations = type.getAnnotations();
                for (AnnotationExpr annotation : classAnnotations) {
                    if (customAnnotations.contains(annotation.getNameAsString())) {
                        processAnnotations(annotation, "Class: " + type.getNameAsString());
                    }
                }

                List<MethodDeclaration> methods = type.getMethods();
                for (MethodDeclaration method : methods) {
                    List<AnnotationExpr> methodAnnotations = method.getAnnotations();
                    for (AnnotationExpr annotation : methodAnnotations) {
                        if (customAnnotations.contains(annotation.getNameAsString())) {
                            processAnnotations(annotation, "Method: " + method.getNameAsString());
                        }
                    }
                }

                List<FieldDeclaration> fields = type.getFields();
                for (FieldDeclaration field : fields) {
                    List<AnnotationExpr> fieldAnnotations = field.getAnnotations();
                    for (AnnotationExpr annotation : fieldAnnotations) {
                        if (customAnnotations.contains(annotation.getNameAsString())) {
                            processAnnotations(annotation, "Field: " + field.getVariable(0).getNameAsString());
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processAnnotations(AnnotationExpr annotation, String elementName) {
        ObjectMapper objectMapper = new ObjectMapper();

        // Configura el ObjectMapper para formatear la salida JSON con sangría
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        ObjectNode result = objectMapper.createObjectNode();

        ObjectNode annotationNode = objectMapper.createObjectNode();

        if (annotation.isNormalAnnotationExpr()) {
            NormalAnnotationExpr normalAnnotation = annotation.asNormalAnnotationExpr();
            ObjectNode attributesNode = objectMapper.createObjectNode();

            for (MemberValuePair pair : normalAnnotation.getPairs()) {
                String key = pair.getNameAsString();
                JsonNode valueNode;

                if (pair.getValue().isArrayInitializerExpr()) {
                    // Procesa el valor como una lista de cadenas
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
                    // Procesa otros valores como cadenas simples
                    String value = pair.getValue().toString().replaceAll("\"", "");
                    valueNode = objectMapper.valueToTree(value);
                }

                annotationNode.set(key, valueNode);
            }
        }



        result.set("BPM " + elementName, annotationNode);

        try {
            String json = objectMapper.writeValueAsString(result);
            System.out.println(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
