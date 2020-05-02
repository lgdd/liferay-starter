package com.github.lgdd.liferay.starter.services;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Singleton
public class WorkspaceService {

    @Inject
    ArchiveService archiveService;

    public byte[] createWorkspaceZip(String tool, String version,
                                     String projectGroupId,
                                     String projectArtifactId,
                                     String projectVersion) throws Exception {

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

        archiveService.compressZipfile(workspacePath.toAbsolutePath().toString(), baos);

        Files.walk(buildPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

        process.destroy();

        return baos.toByteArray();
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
