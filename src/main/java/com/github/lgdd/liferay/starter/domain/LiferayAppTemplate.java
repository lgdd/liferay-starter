package com.github.lgdd.liferay.starter.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum LiferayAppTemplate {

  THEME("theme"),

  REACT("react"),
  VUEJS("vuejs"),
  ANGULAR("angular"),
  VANILLA("vanilla"),

  ACTIVATOR("activator"),
  API("api"),
  CONTROL_MENU_ENTRY("control-menu-entry"),
  FORM_FIELD("form-field"),
  FREEMARKER_PORTLET("freemarker-portlet"),
  MVC_PORTLET("mvc-portlet"),
  PANEL_APP("panel-app"),
  PORTLET_CONFIGURATION_ICON("portlet-configuration-icon"),
  PORTLET_PROVIDER("portlet-provider"),
  PORTLET_TOOLBAR_CONTRIBUTOR("portlet-toolbar-contributor"),
  REST("rest"),
  REST_BUILDER("rest-builder"),
  SERVICE_BUILDER("service-builder"),
  SIMULATION_PANEL_ENTRY("simulation-panel-entry"),
  SOCIAL_BOOKMARK("social-bookmark"),
  TEMPLATE_CONTEXT_CONTRIBUTOR("template-context-contributor");

  private final String name;

  LiferayAppTemplate(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
