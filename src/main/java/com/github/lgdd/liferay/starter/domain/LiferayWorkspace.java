package com.github.lgdd.liferay.starter.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public class LiferayWorkspace {

    private String projectGroupId;
    private String projectArtifactId;
    private String projectVersion;
    private List<LiferayApp> apps;

    public String getProjectGroupId() {
        return projectGroupId;
    }

    public void setProjectGroupId(String projectGroupId) {
        this.projectGroupId = projectGroupId;
    }

    public String getProjectArtifactId() {
        return projectArtifactId;
    }

    public void setProjectArtifactId(String projectArtifactId) {
        this.projectArtifactId = projectArtifactId;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public List<LiferayApp> getApps() {
        return apps;
    }

    public void setApps(List<LiferayApp> apps) {
        this.apps = apps;
    }
}
