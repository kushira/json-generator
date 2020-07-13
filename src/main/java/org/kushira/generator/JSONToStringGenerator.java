package org.kushira.generator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.kushira.generator.annotation.JSONToString;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class JSONToStringGenerator {

    private static final Set<Class<?>> PRIMITIVE_CLASSES = new HashSet<>();

    static {
        PRIMITIVE_CLASSES.add(Boolean.class);
        PRIMITIVE_CLASSES.add(Character.class);
        PRIMITIVE_CLASSES.add(Byte.class);
        PRIMITIVE_CLASSES.add(Short.class);
        PRIMITIVE_CLASSES.add(Integer.class);
        PRIMITIVE_CLASSES.add(Long.class);
        PRIMITIVE_CLASSES.add(Float.class);
        PRIMITIVE_CLASSES.add(Double.class);
        PRIMITIVE_CLASSES.add(Void.class);
        PRIMITIVE_CLASSES.add(BigDecimal.class);
    }

    public static void generate(String packageName) throws ClassNotFoundException, IOException {
        Reflections reflections = new Reflections(packageName);

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(JSONToString.class);
        for (Class<?> klass : classes) {
            generateToString(klass.getName());
        }
    }

    private static void generateToString(String className) throws ClassNotFoundException, IOException {
        Class<?> tClass = Class.forName(className);
        Field[] fields = tClass.getDeclaredFields();
        StringBuilder toStringBuilder = new StringBuilder();
        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addAndGetStatement("StringBuilder builder = new StringBuilder()");
        blockStmt.addAndGetStatement("builder.append(\"{\")");
        int count = 0;
        for (Field field : fields) {
            toStringBuilder.append("builder.append(\"\\\"").append(field.getName()).append("\\\":\")");
            if (field.getType().isAnnotationPresent(JSONToString.class)) {
                toStringBuilder.append(".append(").append(field.getName()).append(")");
            } else if (field.getType().isPrimitive() || PRIMITIVE_CLASSES.contains(field.getType())) {
                toStringBuilder.append(".append(").append(field.getName()).append(")");
            } else {
                toStringBuilder.append(".append(\"\\\"\")").append(".append(").append(field.getName()).append(")").append(".append(\"\\\"\")");
            }
            if (count < fields.length - 1) {
                toStringBuilder.append(".append(\",\")");
            }
            count++;
            blockStmt.addAndGetStatement(toStringBuilder.toString());
            toStringBuilder.setLength(0);
        }
        blockStmt.addAndGetStatement("builder.append(\"}\")");
        blockStmt.addAndGetStatement("return builder.toString()");
        File file = new File("src/main/java/" + tClass.getPackageName().replace(".", "/") + "/" + tClass.getSimpleName() + ".java");
        ParseResult<CompilationUnit> parseResult = new JavaParser().parse(file);
        CompilationUnit compilationUnit = parseResult.getResult().get();
        Optional<ClassOrInterfaceDeclaration> classOrInterfaceDeclaration = compilationUnit.getClassByName(tClass.getSimpleName());
        ClassOrInterfaceDeclaration classDeclaration = classOrInterfaceDeclaration.get();
        List<MethodDeclaration> methods = classDeclaration.getMethodsByName("toString");
        MethodDeclaration methodDeclaration;
        if (!methods.isEmpty()) {
            methodDeclaration = methods.get(0);
        } else {
            methodDeclaration = classDeclaration.addMethod("toString", Modifier.Keyword.PUBLIC);
            methodDeclaration.addAndGetAnnotation(Override.class);
            methodDeclaration.setType("String");
        }
        methodDeclaration.setBody(blockStmt);
        Files.write(file.toPath(), compilationUnit.toString().getBytes());
    }
}
