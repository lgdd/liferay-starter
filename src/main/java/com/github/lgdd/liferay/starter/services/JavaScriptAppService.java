package com.github.lgdd.liferay.starter.services;

import com.github.lgdd.liferay.starter.domain.LiferayApp;
import com.github.lgdd.liferay.starter.domain.LiferayWorkspace;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class JavaScriptAppService {

  @Inject
  CommandService commandService;

  @Inject
  ProjectFileService projectFileService;

  public void create(LiferayApp app, String tool, LiferayWorkspace workspace,
      Path baseWorkspace) throws Exception {
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


}
