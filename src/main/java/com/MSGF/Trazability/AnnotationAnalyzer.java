package com.MSGF.Trazability;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;

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
                        processAnnotations(annotation, "Class: " + type.getNameAsString(), customAnnotations);
                    }
                }

                List<MethodDeclaration> methods = type.getMethods();
                for (MethodDeclaration method : methods) {
                    List<AnnotationExpr> methodAnnotations = method.getAnnotations();
                    for (AnnotationExpr annotation : methodAnnotations) {
                        if (customAnnotations.contains(annotation.getNameAsString())) {
                            processAnnotations(annotation, "Method: " + method.getNameAsString(), customAnnotations);
                        }
                    }
                }

                List<FieldDeclaration> fields = type.getFields();
                for (FieldDeclaration field : fields) {
                    List<AnnotationExpr> fieldAnnotations = field.getAnnotations();
                    for (AnnotationExpr annotation : fieldAnnotations) {
                        if (customAnnotations.contains(annotation.getNameAsString())) {
                            processAnnotations(annotation, "Field: " + field.getVariable(0).getNameAsString(), customAnnotations);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processAnnotations(AnnotationExpr annotation, String elementName, List<String> customAnnotations) {
        ObjectMapper objectMapper = new ObjectMapper();

        // Configura el ObjectMapper para formatear la salida JSON con sangría
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        ObjectNode result = objectMapper.createObjectNode();

        ObjectNode annotationNode = objectMapper.createObjectNode();
        annotationNode.put("Annotation", annotation.getNameAsString());

        if (annotation.isNormalAnnotationExpr()) {
            NormalAnnotationExpr normalAnnotation = annotation.asNormalAnnotationExpr();
            ObjectNode attributesNode = objectMapper.createObjectNode();

            for (MemberValuePair pair : normalAnnotation.getPairs()) {
                if (pair.getNameAsString().equals("variables")) {
                    String[] elements = pair.getValue().toString()
                            .replaceAll("\\{", "") // Eliminar la llave de apertura
                            .replaceAll("}", "")   // Eliminar la llave de cierre
                            .split(", ");          // Dividir por coma y espacio
                    ArrayNode elementsArray = objectMapper.createArrayNode();
                    for (String element : elements) {
                        elementsArray.add(element);
                    }
                    attributesNode.set("variables", elementsArray);
                } else {
                    attributesNode.put(pair.getNameAsString(), pair.getValue().toString());
                }
            }

            annotationNode.set("Attributes", attributesNode);
        }

        result.set("Anotacion " + elementName, annotationNode);

        try {
            String json = objectMapper.writeValueAsString(result);
            System.out.println(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
