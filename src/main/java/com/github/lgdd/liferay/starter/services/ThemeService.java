package com.github.lgdd.liferay.starter.services;

import static com.github.lgdd.liferay.starter.services.WorkspaceService.MAVEN_RESOURCES_DIR;
import static com.github.lgdd.liferay.starter.services.WorkspaceService.NODE_VERSION;
import static com.github.lgdd.liferay.starter.services.WorkspaceService.NPM_VERSION;

import com.github.lgdd.liferay.starter.domain.LiferayApp;
import com.github.lgdd.liferay.starter.domain.LiferayWorkspace;
import com.github.lgdd.liferay.starter.util.StringUtil;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ThemeService {

  @Inject
  CommandService commandService;

  @Inject
  ProjectFileService projectFileService;

  public void create(
      LiferayApp theme,
      String liferayVersion,
      String tool,
      LiferayWorkspace workspace,
      Path baseWorkspace) throws Exception {

    var themeName = StringUtil.capitalize(theme.getName(), "-", true);
    File config = File.createTempFile(".generator-liferay-theme", ".json");
    BufferedWriter bw = new BufferedWriter(new FileWriter(config));
    bw.write("{\n" +
        "  \"batchMode\": true,\n" +
        "  \"answers\": {\n" +
        "    \"*\": {\n" +
        "      \"themeName\": \"" + themeName + "\",\n" +
        "      \"themeId\": \"" + theme.getName() + "\",\n" +
        "      \"liferayVersion\": \"" + liferayVersion + "\",\n" +
        "      \"fontAwesome\": true\n" +
        "    }\n" +
        "  }\n" +
        "}\n");
    bw.close();

    var generator = "liferay-theme";

    if ("7.0".equals(liferayVersion) || "7.1".equals(liferayVersion)) {
      generator = "old-" + generator;
    }

    commandService.runInDirectory(baseWorkspace.resolve("themes").toFile()
        , "yo", generator, "--config", config.getAbsolutePath(), "--skip-install");

    var themePath = Path.of(
        baseWorkspace.resolve("themes").toAbsolutePath().toString(),
        StringUtil.getThemeArtifactId(theme.getName())
    );

    var modulesPomPath = Path.of(
        baseWorkspace.resolve("themes").toAbsolutePath().toString(),
        "pom.xml"
    );

    projectFileService.addNpmrcFile(themePath);

    if ("maven".equalsIgnoreCase(tool)) {
      addPomToTheme(themePath, theme, workspace);
      projectFileService.updateModulesPomFile(modulesPomPath, theme);
    }
  }

  private void addPomToTheme(
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
}
