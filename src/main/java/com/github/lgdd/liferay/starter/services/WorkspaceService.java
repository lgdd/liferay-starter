package com.github.lgdd.liferay.starter.services;

import com.github.lgdd.liferay.starter.domain.LiferayApp;
import com.github.lgdd.liferay.starter.domain.LiferayAppType;
import com.github.lgdd.liferay.starter.domain.LiferayWorkspace;
import com.github.lgdd.liferay.starter.util.StringUtil;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@Singleton
public class WorkspaceService {

  public byte[] createWorkspaceZip(String tool, String version, LiferayWorkspace workspace)
      throws Exception {
    var projectGroupId = workspace.getProjectGroupId();
    var projectArtifactId = workspace.getProjectArtifactId();
    var projectVersion = workspace.getProjectVersion();
    var apps = workspace.getApps();
    var buildPath = Files.createTempDirectory("liferayWorkspaces--");
    var workspaceName =
        projectArtifactId.isEmpty() ? StringUtil.toWorkspaceName(tool, version) : projectArtifactId;

    commandService.runInDirectory(buildPath.toFile()
        , "blade", "init", "-b", tool, "-v", version, workspaceName);

    var baos = new ByteArrayOutputStream();
    var workspacePath = buildPath.resolve(workspaceName);

    if ("maven".equalsIgnoreCase(tool)) {
      projectFileService
          .updatePomFiles(workspacePath, projectGroupId, projectArtifactId, projectVersion);
    }

    addJavaApps(apps, workspacePath);
    addJavaScriptApps(apps, tool, workspace, workspacePath);
    addThemes(apps, version, tool, workspace, workspacePath);

    archiveService.compressZipfile(workspacePath.toAbsolutePath().toString(), baos);

    clean(buildPath);

    return baos.toByteArray();
  }

  private void clean(Path buildPath) throws IOException {
    Files.walk(buildPath)
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
  }

  public void addJavaApps(List<LiferayApp> apps, Path workspacePath) {
    apps.stream()
        .filter(app -> LiferayAppType.JAVA.equals(app.getType()))
        .forEach(app -> {
          try {
            javaAppService.create(app, workspacePath.toAbsolutePath().toString());
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
  }

  public void addJavaScriptApps(
      List<LiferayApp> apps,
      String tool,
      LiferayWorkspace workspace,
      Path workspacePath) {

    apps.stream()
        .filter(app -> LiferayAppType.JAVASCRIPT.equals(app.getType()))
        .forEach(app -> {
          try {
            javaScriptAppService.create(app, tool, workspace, workspacePath);
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
  }

  public void addThemes(
      List<LiferayApp> apps,
      String version,
      String tool,
      LiferayWorkspace workspace,
      Path workspacePath) {

    apps.stream()
        .filter(app -> LiferayAppType.THEME.equals(app.getType()))
        .forEach(theme -> {
          try {
            themeService.create(theme, version, tool, workspace, workspacePath);
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
  }

  public static final String NODE_VERSION = "v12.18.0";
  public static final String NPM_VERSION = "6.14.4";
  public static final String MAVEN_RESOURCES_DIR = "/starter/maven";

  @Inject
  ArchiveService archiveService;

  @Inject
  CommandService commandService;

  @Inject
  ProjectFileService projectFileService;

  @Inject
  ThemeService themeService;

  @Inject
  JavaScriptAppService javaScriptAppService;

  @Inject
  JavaAppService javaAppService;
}
