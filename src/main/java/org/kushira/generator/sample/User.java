package org.kushira.generator.sample;

import org.kushira.generator.annotation.JSONToString;

@JSONToString
public class User {

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    @Override()
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append("\"name\":").append("\"").append(name).append("\"");
        builder.append("{");
        return builder.toString();
    }
}
