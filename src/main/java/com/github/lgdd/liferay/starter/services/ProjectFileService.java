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

/**
 * Update project files with given project parameters.
 */
@Singleton
public class ProjectFileService {

  /**
   * Update pom files for a given Maven Liferay Workspace.
   *
   * @param workspacePath     Liferay Workspace location
   * @param projectGroupId    project group ID
   * @param projectArtifactId project artifact ID
   * @param projectVersion    project version
   * @throws IOException if it fails to update the files
   */
  public void updatePomFiles(
      Path workspacePath,
      String projectGroupId,
      String projectArtifactId,
      String projectVersion) throws IOException {

    var parentPom = workspacePath.resolve("pom.xml");
    var modulesPom = workspacePath.resolve("modules/pom.xml");
    var themesPom = workspacePath.resolve("themes/pom.xml");

    var pomPaths = Arrays.asList(parentPom, modulesPom, themesPom);

    for (Path pomPath : pomPaths) {
      updatePomFile(pomPath, projectGroupId, projectArtifactId, projectVersion);
    }
  }

  /**
   * Update a pom file for a given Maven Liferay Workspace.
   *
   * @param pomPath           pom file location
   * @param projectGroupId    project group ID
   * @param projectArtifactId project artifact ID
   * @param projectVersion    project version
   * @throws IOException if it fails to update the file
   */
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

  /**
   * Update the pom file for modules in a given Maven Liferay Workspace.
   *
   * @param modulesPomPath pom file for modules subfolder of a Liferay Workspace
   * @param app            app information to add in the modules pom file
   * @throws IOException if it fails to update the file
   */
  public void updateModulesPomFile(Path modulesPomPath, LiferayApp app) throws IOException {
    var charset = StandardCharsets.UTF_8;
    var content = Files.readString(modulesPomPath, charset);
    var artifactId = LiferayAppType.THEME.equals(app.getType()) ?
        StringUtil.getThemeArtifactId(app.getName()) : app.getName();

    if (!content.contains("</modules>")) {
      content = content.replaceAll("</packaging>",
          "</packaging>\n\n\t<modules>\n\t</modules>\n");
    }

    content = content
        .replaceAll("</modules>",
            "\t<module>" + artifactId + "</module>\n\t</modules>");
    Files.write(modulesPomPath, content.getBytes(charset));
  }

  /**
   * Create a pom file for a JavaScript app.
   *
   * @param appPath   app location in a given Liferay Workspace
   * @param app       app information
   * @param workspace Liferay Workspace
   * @throws IOException if it fails to create the pom file
   */
  public void addPomToJavaScriptApp(Path appPath, LiferayApp app, LiferayWorkspace workspace)
      throws IOException {
    var pomContent = new BufferedReader(
        new InputStreamReader(
            WorkspaceService.class
                .getResourceAsStream(MAVEN_RESOURCES_DIR + "/js-pom.xml")))
        .lines()
        .collect(Collectors.joining("\n"));

    var file = new File(appPath.toAbsolutePath().toString(), "pom.xml");
    var charset = StandardCharsets.UTF_8;

    pomContent = pomContent.replaceAll("%APP_NAME%", app.getName());
    pomContent = pomContent.replaceAll("%NODE_VERSION%", NODE_VERSION);
    pomContent = pomContent.replaceAll("%NPM_VERSION%", NPM_VERSION);
    pomContent = pomContent.replaceAll("%PROJECT_GROUP_ID%", workspace.getProjectGroupId());
    pomContent = pomContent.replaceAll("%PROJECT_VERSION%", workspace.getProjectVersion());
    pomContent = pomContent.replaceAll("%MODULES_ARTIFACT_ID%",
        workspace.getProjectArtifactId() + "-modules");

    if (file.createNewFile()) {
      Files.write(file.toPath(), pomContent.getBytes(charset));
    }
  }

  /**
   * Create a pom file for a Theme.
   *
   * @param appPath   theme location
   * @param theme     theme parameters
   * @param workspace Liferay Workspace parameters
   * @throws IOException if it fails to create the file
   */
  public void addPomToTheme(
      Path appPath,
      LiferayApp theme,
      LiferayWorkspace workspace) throws IOException {

    var pomContent = new BufferedReader(
        new InputStreamReader(
            WorkspaceService.class
                .getResourceAsStream(MAVEN_RESOURCES_DIR + "/theme-pom.xml")))
        .lines()
        .collect(Collectors.joining("\n"));

    var file = new File(appPath.toAbsolutePath().toString(), "pom.xml");
    var charset = StandardCharsets.UTF_8;
    var themeArtifactId = StringUtil.getThemeArtifactId(theme.getName());

    pomContent = pomContent.replaceAll("%PROJECT_GROUP_ID%", workspace.getProjectGroupId());
    pomContent = pomContent
        .replaceAll("%THEMES_ARTIFACT_ID%", workspace.getProjectArtifactId() + "-themes");
    pomContent = pomContent.replaceAll("%PROJECT_VERSION%", workspace.getProjectVersion());
    pomContent = pomContent.replaceAll("%THEME_NAME%", themeArtifactId);
    pomContent = pomContent.replaceAll("%NODE_VERSION%", NODE_VERSION);
    pomContent = pomContent.replaceAll("%NPM_VERSION%", NPM_VERSION);

    if (file.createNewFile()) {
      Files.write(file.toPath(), pomContent.getBytes(charset));
    }
  }

  /**
   * Add an .npmrc file to allow scripts execution for Themes and JavaScript apps.
   *
   * @param appPath app location
   * @throws IOException if it fails to create the file
   */
  public void addNpmrcFile(Path appPath) throws IOException {
    var file = new File(appPath.toAbsolutePath().toString(), ".npmrc");
    var charset = StandardCharsets.UTF_8;

    if (file.createNewFile()) {
      String content = "ignore-scripts=false";
      Files.write(file.toPath(), content.getBytes(charset));
    }
  }

  /**
   * Update liferay-theme.json with relative path to deploy folder.
   *
   * @param themePath theme location
   * @throws IOException if it fails to update the file
   */
  public void updateLiferayThemeJson(Path themePath) throws IOException {
    var filePath = Path.of(themePath.toAbsolutePath().toString(), "liferay-theme.json");
    var charset = StandardCharsets.UTF_8;
    var content = "{\n"
        + "  \"LiferayTheme\": {\n"
        + "    \"appServerPath\": \"../bundles/tomcat\",\n"
        + "    \"deployPath\": \"../../bundles/deploy\",\n"
        + "    \"deploymentStrategy\": \"LocalAppServer\",\n"
        + "    \"url\": \"http://localhost:8080\"\n"
        + "  }\n"
        + "}\n";
    Files.write(filePath, content.getBytes(charset));
  }

}
