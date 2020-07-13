package org.kushira.generator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SupportedAnnotationTypes(
        "org.kushira.generator.annotation.JSONToString")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class JSONProcessor extends AbstractProcessor {

    private static final Set<String> PRIMITIVE_CLASSES = new HashSet<>();

    private static final Pattern COLLECTION_PATTERN = Pattern.compile("(java.util.Collection)<([a-zA-Z.]*)>");
    private static final Pattern MAP_PATTERN = Pattern.compile("(java.util.Map)<([a-zA-Z.]*),([a-zA-Z.]*)>");

    static {
        PRIMITIVE_CLASSES.add(Boolean.class.getCanonicalName());
        PRIMITIVE_CLASSES.add(Character.class.getCanonicalName());
        PRIMITIVE_CLASSES.add(Byte.class.getCanonicalName());
        PRIMITIVE_CLASSES.add(Short.class.getCanonicalName());
        PRIMITIVE_CLASSES.add(Integer.class.getCanonicalName());
        PRIMITIVE_CLASSES.add(Long.class.getCanonicalName());
        PRIMITIVE_CLASSES.add(Float.class.getCanonicalName());
        PRIMITIVE_CLASSES.add(Double.class.getCanonicalName());
        PRIMITIVE_CLASSES.add(Void.class.getCanonicalName());
        PRIMITIVE_CLASSES.add(BigDecimal.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<Element> allElements = new HashSet<>();
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            allElements.addAll(annotatedElements);
        }

        Set<String> annotatedClasses = new HashSet<>();

        for (Element element : allElements) {
            annotatedClasses.add(((TypeElement) element).getQualifiedName().toString());
        }

        for (Element element : allElements) {
            TypeElement typeElement = (TypeElement) element;
            try {
                generateToString(typeElement, annotatedClasses);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private void generateToString(TypeElement typeElement, Set<String> annotatedClasses) throws
            IOException {
        List<? extends Element> fields = typeElement.getEnclosedElements();
        StringBuilder toStringBuilder = new StringBuilder();
        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addAndGetStatement("StringBuilder builder = new StringBuilder()");
        blockStmt.addAndGetStatement("builder.append(\"{\")");
        Matcher matcher;
        boolean hasPrevious = false;
        for (Element field : fields) {
            if (field.getKind().isField()) {
                VariableElement variableElement = (VariableElement) field;
                TypeMirror fieldType = variableElement.asType();
                if (hasPrevious) {
                    blockStmt.addAndGetStatement("builder.append(\",\")");
                }
                toStringBuilder.append("builder.append(\"\\\"").append(field.getSimpleName()).append("\\\":\")");
                if (annotatedClasses.contains(fieldType.toString())) {
                    toStringBuilder.append(".append(").append(field.getSimpleName()).append(")");
                } else if (fieldType.getKind().isPrimitive() || PRIMITIVE_CLASSES.contains(fieldType.toString())) {
                    toStringBuilder.append(".append(").append(field.getSimpleName()).append(")");
                } else if ((matcher = isCollection(this.processingEnv.getTypeUtils().directSupertypes(fieldType))) != null) {
                    String dataType = matcher.group(2);
                    toStringBuilder.append(".append(\"[\");");
                    toStringBuilder.append(field.getSimpleName()).append(".forEach(value -> ");
                    if (PRIMITIVE_CLASSES.contains(dataType) || annotatedClasses.contains(dataType)) {
                        toStringBuilder.append("builder.append(value)").append(".append(\",\")");
                    } else {
                        toStringBuilder.append("builder.append(\"\\\"\")").append(".append(value)").append(".append(\"\\\"\")").append(".append(\",\")");
                    }
                    toStringBuilder.append(");");
                    toStringBuilder.append("builder.append(\"]\")");
                } else if (((matcher = isMap(fieldType)) != null) || ((matcher = isCollection(this.processingEnv.getTypeUtils().directSupertypes(fieldType))) != null)) {
                    String dataType = matcher.group(3);
                    toStringBuilder.append(".append(\"{\");");
                    toStringBuilder.append(field.getSimpleName()).append(".forEach((key, value) -> ");
                    toStringBuilder.append("builder.append(\"\\\"\")").append(".append(key)").append(".append(\"\\\"\")").append(".append(\":\")");
                    if (PRIMITIVE_CLASSES.contains(dataType) || annotatedClasses.contains(dataType)) {
                        toStringBuilder.append(".append(value)").append(".append(\",\")");
                    } else {
                        toStringBuilder.append(".append(\"\\\"\")").append(".append(value)").append(".append(\"\\\"\")").append(".append(\",\")");
                    }
                    toStringBuilder.append(");");
                    toStringBuilder.append("builder.append(\"}\")");
                } else {
                    toStringBuilder.append(".append(\"\\\"\")").append(".append(").append(field.getSimpleName()).append(")").append(".append(\"\\\"\")");
                }
                hasPrevious = true;
                blockStmt.addAndGetStatement(toStringBuilder.toString());
                toStringBuilder.setLength(0);
            }
        }
        blockStmt.addAndGetStatement("builder.append(\"}\")");
        blockStmt.addAndGetStatement("return builder.toString()");
        File file = new File("src/main/java/" + typeElement.getQualifiedName().toString().replace(".", "/") + ".java");
        ParseResult<CompilationUnit> parseResult = new JavaParser().parse(file);
        CompilationUnit compilationUnit = parseResult.getResult().get();
        Optional<ClassOrInterfaceDeclaration> classOrInterfaceDeclaration = compilationUnit.getClassByName(typeElement.getSimpleName().toString());
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

    private Matcher isCollection(List<? extends TypeMirror> typeMirrors) {
        for (TypeMirror typeMirror : typeMirrors) {
            Matcher matcher = COLLECTION_PATTERN.matcher(typeMirror.toString());
            if (matcher.find()) {
                return matcher;
            }
        }

        return null;
    }

    private Matcher isMap(TypeMirror typeMirror) {
        Matcher matcher = MAP_PATTERN.matcher(typeMirror.toString());
        if (matcher.find()) {
            return matcher;
        }

        return null;
    }

}
