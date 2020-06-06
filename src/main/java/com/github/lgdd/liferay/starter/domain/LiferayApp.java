package com.github.lgdd.liferay.starter.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.ArrayList;
import java.util.List;

@RegisterForReflection
public class LiferayApp {

    private int id;
    private LiferayAppType type;
    private String name;
    private LiferayAppTemplate template;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LiferayAppType getType() {
        return type;
    }

    public void setType(LiferayAppType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LiferayAppTemplate getTemplate() {
        return template;
    }

    public void setTemplate(LiferayAppTemplate template) {
        this.template = template;
    }

    static List<String> TEMPLATES = new ArrayList<>() {{
        add("activator");
        add("api");
        add("control-menu-entry");
        add("form-field");
        add("freemarker-portlet");
        add("mvc-portlet");
        add("npm-angular-portlet");
        add("npm-react-portlet");
        add("npm-vuejs-portlet");
        add("panel-app");
        add("portlet-configuration-icon");
        add("portlet-provider");
        add("portlet-toolbar-contributor");
        add("rest");
        add("service-builder");
        add("simulation-panel-entry");
        add("social-bookmark");
        add("template-context-contributor");
    }};

}
