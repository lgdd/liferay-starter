package com.github.lgdd.liferay.starter.services;

import static com.github.lgdd.liferay.starter.services.WorkspaceService.MAVEN_RESOURCES_DIR;
import static com.github.lgdd.liferay.starter.services.WorkspaceService.NODE_VERSION;
import static com.github.lgdd.liferay.starter.services.WorkspaceService.NPM_VERSION;

import com.github.lgdd.liferay.starter.domain.LiferayApp;
import com.github.lgdd.liferay.starter.domain.LiferayAppType;
import com.github.lgdd.liferay.starter.domain.LiferayWorkspace;
import com.github.lgdd.liferay.starter.util.StringUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.inject.Singleton;

@Singleton
public class ProjectFileService {

  public void updatePomFiles(
      Path workspacePath,
      String projectGroupId,
      String projectArtifactId,
      String projectVersion) throws IOException {

    var parentPom = workspacePath.resolve("pom.xml");
    var modulesPom = workspacePath.resolve("modules/pom.xml");
    var themesPom = workspacePath.resolve("themes/pom.xml");
    var warsPm = workspacePath.resolve("wars/pom.xml");

    var pomPaths = Arrays.asList(parentPom, modulesPom, themesPom, warsPm);

    for (Path pomPath : pomPaths) {
      updatePomFile(pomPath, projectGroupId, projectArtifactId, projectVersion);
    }
  }

  public void updatePomFile(
      Path pomPath,
      String projectGroupId,
      String projectArtifactId,
      String projectVersion) throws IOException {

    var charset = StandardCharsets.UTF_8;

    var content = Files.readString(pomPath, charset);
    content = content.replaceAll("" +
            "<groupId>" + projectArtifactId.replaceAll("-", ".") + "</groupId>",
        "<groupId>" + projectGroupId + "</groupId>");

    content = content.replaceAll("" +
            "<version>1.0.0</version>",
        "<version>" + projectVersion + "</version>");

    Files.write(pomPath, content.getBytes(charset));
  }

  public void updateModulesPomFile(Path modulesPomPath, LiferayApp app) throws IOException {
    var charset = StandardCharsets.UTF_8;
    var content = Files.readString(modulesPomPath, charset);
    var artifactId = LiferayAppType.THEME.equals(app.getType()) ?
        StringUtil.getThemeArtifactId(app.getName()) : app.getName();

    if (!content.contains("</modules>")) {
      content = content.replaceAll("</packaging>", "</packaging>\n\n\t<modules>\n\t</modules>\n");
    }

    content = content.replaceAll("</modules>", "\t<module>" + artifactId + "</module>\n\t</modules>");
    Files.write(modulesPomPath, content.getBytes(charset));
  }

  public void addPomToJavaScriptApp(Path appPath, LiferayApp app, LiferayWorkspace workspace)
      throws IOException {
    var pomContent = new BufferedReader(
        new InputStreamReader(
            WorkspaceService.class.getResourceAsStream(MAVEN_RESOURCES_DIR + "/js-pom.xml"))
    ).lines().collect(Collectors.joining("\n"));

    var file = new File(appPath.toAbsolutePath().toString(), "pom.xml");
    var charset = StandardCharsets.UTF_8;

    pomContent = pomContent.replaceAll("%PROJECT_GROUP_ID%", workspace.getProjectGroupId());
    pomContent = pomContent
        .replaceAll("%MODULES_ARTIFACT_ID%", workspace.getProjectArtifactId() + "-modules");
    pomContent = pomContent.replaceAll("%PROJECT_VERSION%", workspace.getProjectVersion());
    pomContent = pomContent.replaceAll("%APP_NAME%", app.getName());
    pomContent = pomContent.replaceAll("%NODE_VERSION%", NODE_VERSION);
    pomContent = pomContent.replaceAll("%NPM_VERSION%", NPM_VERSION);

    if (file.createNewFile()) {
      Files.write(file.toPath(), pomContent.getBytes(charset));
    }
  }

  public void addNpmrcFile(Path appPath) throws IOException {
    var file = new File(appPath.toAbsolutePath().toString(), ".npmrc");
    var charset = StandardCharsets.UTF_8;

    if (file.createNewFile()) {
      String content = "ignore-scripts=false";
      Files.write(file.toPath(), content.getBytes(charset));
    }
  }

}
