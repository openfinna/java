package org.openfinna.java.connector.classes.models.resource;

import java.util.List;

public class Author {
    private String name;
    private List<String> roles;
    private String type;

    public Author(String name, List<String> roles, String type) {
        this.name = name;
        this.roles = roles;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRole() {
        return roles;
    }

    public void setRole(List<String> roles) {
        this.roles = roles;
    }
}
