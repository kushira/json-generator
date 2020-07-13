package org.kushira.generator;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.IOException;

public class GreetingPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        GreetingPluginExtension extension = project.getExtensions().create("generator", GreetingPluginExtension.class);
        project.getTasks().create("hello")
                .doLast(task -> {
                    if (extension.name != null && !extension.name.isBlank()) {
                        try {
                            JSONToStringGenerator.generate(extension.name);
                        } catch (ClassNotFoundException | IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("Hello Gradle!");
                    }
                });
    }
}