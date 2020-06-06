package com.github.lgdd.liferay.starter.services;

import com.github.lgdd.liferay.starter.domain.LiferayApp;
import com.github.lgdd.liferay.starter.domain.LiferayAppType;
import com.github.lgdd.liferay.starter.domain.LiferayWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Singleton
public class WorkspaceService {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceService.class);

    @Inject
    ArchiveService archiveService;

    public byte[] createWorkspaceZip(String tool, String version, LiferayWorkspace workspace) throws Exception {
        var projectGroupId = workspace.getProjectGroupId();
        var projectArtifactId = workspace.getProjectArtifactId();
        var projectVersion = workspace.getProjectVersion();
        var apps = workspace.getApps();
        var buildPath = Files.createTempDirectory("liferayWorkspaces--");
        var workspaceName = projectArtifactId.isEmpty() ? getWorkspaceName(tool, version) : projectArtifactId;

        var builder = new ProcessBuilder("blade", "init", "-b", tool, "-v", version, workspaceName);

        builder.directory(buildPath.toFile());

        var process = builder.start();
        var exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Workspace build failed with exit code " + exitCode);
        }

        var baos = new ByteArrayOutputStream();
        var workspacePath = buildPath.resolve(workspaceName);

        if ("maven".equalsIgnoreCase(tool))
            updatePomFiles(workspacePath, projectGroupId, projectArtifactId, projectVersion);

        apps.parallelStream()
                .filter(app -> LiferayAppType.JAVA.equals(app.getType()))
                .forEach(app -> {
                    try {
                        addJavaApp(app, workspacePath.toAbsolutePath().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        apps.parallelStream()
                .filter(app -> LiferayAppType.JAVASCRIPT.equals(app.getType()))
                .forEach(app -> {
                    try {
                        addJavaScriptApp(app, workspacePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        apps.parallelStream()
                .filter(app -> LiferayAppType.THEME.equals(app.getType()))
                .forEach(app -> {
                    try {
                        addTheme(app, version, workspacePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        archiveService.compressZipfile(workspacePath.toAbsolutePath().toString(), baos);

        Files.walk(buildPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

        process.destroy();

        return baos.toByteArray();
    }

    private void addJavaApp(LiferayApp app, String baseWorkspace) throws Exception {
        var packageName = "org.acme.liferay." + app.getName().replaceAll("-", ".");
        var className = capitalize(app.getName(), "-", false);
        var builder = new ProcessBuilder(
                "blade", "create", "-t", app.getTemplate().getName(),
                "-p", packageName, "-c", className, "--base", baseWorkspace, app.getName());

        var process = builder.start();
        var exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Workspace build failed with exit code " + exitCode);
        }

        process.destroy();
    }

    private void addJavaScriptApp(LiferayApp app, Path baseWorkspace) throws Exception {
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

        var builder = new ProcessBuilder("yo", "liferay-js", "--config", config.getAbsolutePath(), "--skip-install");

        builder.directory(baseWorkspace.resolve("modules").toFile());

        var process = builder.start();
        var exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Workspace build failed with exit code " + exitCode);
        }

        process.destroy();
    }

    private void addTheme(LiferayApp app, String liferayVersion, Path baseWorkspace) throws Exception {
        var themeName = capitalize(app.getName(), "-", true);
        File config = File.createTempFile(".generator-liferay-theme", ".json");
        BufferedWriter bw = new BufferedWriter(new FileWriter(config));
        bw.write("{\n" +
                "  \"batchMode\": true,\n" +
                "  \"answers\": {\n" +
                "    \"*\": {\n" +
                "      \"themeName\": \"" + themeName + "\",\n" +
                "      \"themeId\": \"" + app.getName() + "\",\n" +
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

        var builder = new ProcessBuilder("yo", generator, "--config", config.getAbsolutePath(), "--skip-install");

        builder.directory(baseWorkspace.resolve("themes").toFile());

        var process = builder.start();
        var exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Workspace build failed with exit code " + exitCode);
        }

        process.destroy();
    }

    public static void debugCommand(List<String> command) {
        if (log.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder(command.size());
            for (String arg : command) {
                builder.append(arg);
                builder.append(" ");
            }
            log.debug(builder.toString());
        }
    }

    public static String capitalize(String str, String separator, boolean spaced) {
        String words[] = str.split(separator);
        String capitalizeWord = "";
        String space = spaced ? " " : "";
        for (String w : words) {
            String first = w.substring(0, 1);
            String afterfirst = w.substring(1);
            capitalizeWord += first.toUpperCase() + afterfirst + space;
        }

        return capitalizeWord.trim();
    }

    public String getWorkspaceName(String tool, String version) {
        return tool + "-liferay-workspace-" + version;
    }

    private void updatePomFiles(Path workspacePath,
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

    private void updatePomFile(Path pomPath,
                               String projectGroupId,
                               String projectArtifactId,
                               String projectVersion) throws IOException {

        var charset = StandardCharsets.UTF_8;

        var content = Files.readString(pomPath, charset);
        content = content.replaceAll("" +
                        "<groupId>" + projectArtifactId + "</groupId>",
                "<groupId>" + projectGroupId + "</groupId>");

        content = content.replaceAll("" +
                        "<version>1.0.0</version>",
                "<version>" + projectVersion + "</version>");

        Files.write(pomPath, content.getBytes(charset));
    }

}
