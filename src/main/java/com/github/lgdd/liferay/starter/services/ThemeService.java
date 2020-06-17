package com.github.lgdd.liferay.starter.services;

import com.github.lgdd.liferay.starter.domain.LiferayApp;
import com.github.lgdd.liferay.starter.domain.LiferayWorkspace;
import com.github.lgdd.liferay.starter.exception.CommandException;
import com.github.lgdd.liferay.starter.util.StringUtil;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Creates a Theme for a Liferay workspace.
 */
@Singleton
public class ThemeService {

  /**
   * Create a Theme for a Liferay Workspace given project parameters.
   *
   * @param theme          theme parameters
   * @param liferayVersion Liferay version (7.0, 7.1, 7.2 or 7.3)
   * @param tool           Maven or Gradle
   * @param workspace      Liferay Workspace parameters
   * @param baseWorkspace  Liferay Workspace location
   * @throws IOException      if it fails to create the theme config file
   * @throws CommandException if the creation command fails
   */
  public void create(
      LiferayApp theme,
      String liferayVersion,
      String tool,
      LiferayWorkspace workspace,
      Path baseWorkspace) throws IOException, CommandException {

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
    projectFileService.updateLiferayThemeJson(themePath);

    if ("maven".equalsIgnoreCase(tool)) {
      projectFileService.addPomToTheme(themePath, theme, workspace);
      projectFileService.updateModulesPomFile(modulesPomPath, theme);
    }
  }

  @Inject
  CommandService commandService;

  @Inject
  ProjectFileService projectFileService;

}
