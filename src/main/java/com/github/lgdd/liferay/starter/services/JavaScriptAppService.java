package com.github.lgdd.liferay.starter.services;

import com.github.lgdd.liferay.starter.domain.LiferayApp;
import com.github.lgdd.liferay.starter.domain.LiferayWorkspace;
import com.github.lgdd.liferay.starter.exception.CommandException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Creates JavaScript apps (React, Vue, Angular, Vanilla JS) for a Liferay workspace.
 *
 * @see com.github.lgdd.liferay.starter.domain.LiferayAppType
 */
@Singleton
public class JavaScriptAppService {

  /**
   * Creates a JavaScript app for a Liferay workspace given parameters.
   *
   * @param app           app parameter
   * @param tool          Maven or Gradle
   * @param workspace     Liferay Workspace parameters
   * @param baseWorkspace Liferay Workspace where the JavaScript app will be created
   * @throws IOException      if it fails to create the app config file
   * @throws CommandException if the creation command fails
   */
  public void create(
          LiferayApp app,
          String tool,
          LiferayWorkspace workspace,
          Path baseWorkspace) throws IOException, CommandException {

    File config = File.createTempFile(".generator-liferay-js", ".json");
    BufferedWriter bw = new BufferedWriter(new FileWriter(config));
    bw.write("{\n" +
            "  \"batchMode\": true,\n" +
            "  \"answers\": {\n" +
            "    \"*\": {\n" +
            "      \"target\": \"" + app.getTemplate().getName() + "-portlet\",\n" +
            "      \"folder\": \"" + app.getName() + "\",\n" +
            "      \"category\": \"category.sample\",\n" +
            "      \"liferayDir\": \"../../bundles\",\n" +
            "      \"useConfiguration\": true,\n" +
            "      \"useLocalization\": true,\n" +
            "      \"sampleWanted\": true\n" +
            "    }\n" +
            "  }\n" +
            "}\n");
    bw.close();

    commandService.runInDirectory(baseWorkspace.resolve("modules").toFile()
            , "yo", "liferay-js", "--config", config.getAbsolutePath(), "--skip-install");

    var appPath = Path.of(
            baseWorkspace.resolve("modules").toAbsolutePath().toString(),
            app.getName()
    );
    var modulesPomPath = Path.of(
            baseWorkspace.resolve("modules").toAbsolutePath().toString(),
            "pom.xml"
    );
    projectFileService.addNpmrcFile(appPath);

    if ("maven".equalsIgnoreCase(tool)) {
      projectFileService.addPomToJavaScriptApp(appPath, app, workspace);
      projectFileService.updateModulesPomFile(modulesPomPath, app);
    }
  }

  @Inject
  CommandService commandService;

  @Inject
  ProjectFileService projectFileService;

}
