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
import java.util.stream.Collectors;

@Singleton
public class WorkspaceService {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceService.class);

    private static final String NODE_VERSION = "v12.18.0";
    private static final String NPM_VERSION = "6.14.4";
    private static final String MAVEN_RESOURCES_DIR = "/starter/maven";

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
                        addJavaScriptApp(app, tool, workspace, workspacePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        apps.parallelStream()
                .filter(app -> LiferayAppType.THEME.equals(app.getType()))
                .forEach(theme -> {
                    try {
                        addTheme(theme, version, tool, workspace, workspacePath);
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

    private void addJavaScriptApp(LiferayApp app, String tool, LiferayWorkspace workspace, Path baseWorkspace) throws Exception {
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

        var appPath = Path.of(baseWorkspace.resolve("modules").toAbsolutePath().toString(), app.getName());
        var modulesPomPath = Path.of(baseWorkspace.resolve("modules").toAbsolutePath().toString(), "pom.xml");
        addNpmrcFile(appPath);

        if ("maven".equalsIgnoreCase(tool)) {
            addPomToJavaScriptApp(appPath, app, workspace);
            updateModulesPomFile(modulesPomPath, app);
        }
    }

    private void addTheme(LiferayApp theme, String liferayVersion, String tool, LiferayWorkspace workspace, Path baseWorkspace) throws Exception {
        var themeName = capitalize(theme.getName(), "-", true);
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

        var builder = new ProcessBuilder("yo", generator, "--config", config.getAbsolutePath(), "--skip-install");

        builder.directory(baseWorkspace.resolve("themes").toFile());

        var process = builder.start();
        var exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Workspace build failed with exit code " + exitCode);
        }

        process.destroy();

        var themePath = Path.of(baseWorkspace.resolve("themes").toAbsolutePath().toString(), getThemeName(theme));
        var modulesPomPath = Path.of(baseWorkspace.resolve("themes").toAbsolutePath().toString(), "pom.xml");
        addNpmrcFile(themePath);

        if ("maven".equalsIgnoreCase(tool)) {
            addPomToTheme(themePath, theme, workspace);
            updateModulesPomFile(modulesPomPath, theme);
        }
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
                        "<groupId>" + projectArtifactId.replaceAll("-", ".") + "</groupId>",
                "<groupId>" + projectGroupId + "</groupId>");

        content = content.replaceAll("" +
                        "<version>1.0.0</version>",
                "<version>" + projectVersion + "</version>");

        Files.write(pomPath, content.getBytes(charset));
    }

    private void addNpmrcFile(Path appPath) throws IOException {
        var file = new File(appPath.toAbsolutePath().toString(), ".npmrc");
        var charset = StandardCharsets.UTF_8;

        if (file.createNewFile()) {
            String content = "ignore-scripts=false";
            Files.write(file.toPath(), content.getBytes(charset));
        }
    }

    private void updateModulesPomFile(Path modulesPomPath, LiferayApp app) throws IOException {
        var charset = StandardCharsets.UTF_8;
        var appName = LiferayAppType.THEME.equals(app.getType()) ? getThemeName(app) : app.getName();
        var content = Files.readString(modulesPomPath, charset);

        if (!content.contains("</modules>")) {
            content = content.replaceAll("</packaging>", "</packaging>\n\n\t<modules>\n\t</modules>\n");
        }

        content = content.replaceAll("</modules>", "\t<module>" + appName + "</module>\n\t</modules>");
        Files.write(modulesPomPath, content.getBytes(charset));
    }

    private void addPomToJavaScriptApp(Path appPath, LiferayApp app, LiferayWorkspace workspace) throws IOException {
        var pomContent = new BufferedReader(
                new InputStreamReader(WorkspaceService.class.getResourceAsStream(MAVEN_RESOURCES_DIR + "/js-pom.xml"))
        ).lines().collect(Collectors.joining("\n"));

        var file = new File(appPath.toAbsolutePath().toString(), "pom.xml");
        var charset = StandardCharsets.UTF_8;

        pomContent = pomContent.replaceAll("%PROJECT_GROUP_ID%", workspace.getProjectGroupId());
        pomContent = pomContent.replaceAll("%MODULES_ARTIFACT_ID%", workspace.getProjectArtifactId() + "-modules");
        pomContent = pomContent.replaceAll("%PROJECT_VERSION%", workspace.getProjectVersion());
        pomContent = pomContent.replaceAll("%APP_NAME%", app.getName());
        pomContent = pomContent.replaceAll("%NODE_VERSION%", NODE_VERSION);
        pomContent = pomContent.replaceAll("%NPM_VERSION%", NPM_VERSION);

        if (file.createNewFile()) {
            Files.write(file.toPath(), pomContent.getBytes(charset));
        }
    }

    private void addPomToTheme(Path appPath, LiferayApp theme, LiferayWorkspace workspace) throws IOException {
        var pomContent = new BufferedReader(
                new InputStreamReader(WorkspaceService.class.getResourceAsStream(MAVEN_RESOURCES_DIR + "/theme-pom.xml"))
        ).lines().collect(Collectors.joining("\n"));

        var file = new File(appPath.toAbsolutePath().toString(), "pom.xml");
        var charset = StandardCharsets.UTF_8;
        var themeName = getThemeName(theme);

        pomContent = pomContent.replaceAll("%PROJECT_GROUP_ID%", workspace.getProjectGroupId());
        pomContent = pomContent.replaceAll("%THEMES_ARTIFACT_ID%", workspace.getProjectArtifactId() + "-themes");
        pomContent = pomContent.replaceAll("%PROJECT_VERSION%", workspace.getProjectVersion());
        pomContent = pomContent.replaceAll("%THEME_NAME%", themeName);
        pomContent = pomContent.replaceAll("%NODE_VERSION%", NODE_VERSION);
        pomContent = pomContent.replaceAll("%NPM_VERSION%", NPM_VERSION);

        if (file.createNewFile()) {
            Files.write(file.toPath(), pomContent.getBytes(charset));
        }
    }

    private String getThemeName(LiferayApp theme) {
        return  theme.getName().endsWith("-theme") ? theme.getName() : theme.getName() + "-theme";
    }
}
