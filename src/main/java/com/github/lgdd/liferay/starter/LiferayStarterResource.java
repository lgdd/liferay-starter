package com.github.lgdd.liferay.starter;

import com.github.lgdd.liferay.starter.services.WorkspaceService;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Path("/api/liferay")
public class LiferayStarterResource {

    private static final Logger log = LoggerFactory.getLogger(LiferayStarterResource.class);

    private static final String JAVA_PKG_REGEX = "^(?:\\w+|\\w+\\.\\w+)+$";
    private static final String SEMVER_REGEX = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";

    @Inject
    WorkspaceService workspaceService;

    @GET
    @Path("/{version}/workspace/{tool}")
    @Produces("application/zip")
    public Response workspace(@PathParam String tool, @PathParam String version,
                              @QueryParam("projectGroupId") String projectGroupId,
                              @QueryParam("projectArtifactId") String projectArtifactId,
                              @QueryParam("projectVersion") String projectVersion) {

        if (!validateWorkspaceParams(tool, version, projectGroupId, projectArtifactId, projectVersion)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            byte[] workspaceZip = workspaceService
                    .createWorkspaceZip(tool, version, projectGroupId, projectArtifactId, projectVersion);

            String filename = projectArtifactId.isEmpty() ?
                    workspaceService.getWorkspaceName(tool, version) : projectArtifactId;

            Response.ResponseBuilder responseBuilder = Response.ok(workspaceZip);
            responseBuilder.type("application/zip");
            responseBuilder.header("Content-disposition", "attachment; filename=" + filename + ".zip");
            return responseBuilder.build();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean validateWorkspaceParams(String tool, String version, String projectGroupId, String projectArtifactId, String projectVersion) {
        List<String> liferayVersions = Arrays.asList("7.3", "7.2", "7.1", "7.0");
        if (!("gradle".equalsIgnoreCase(tool) || "maven".equalsIgnoreCase(tool))) {
            return false;
        }
        if (!liferayVersions.contains(version)) {
            return false;
        }
        if (!Pattern.matches(JAVA_PKG_REGEX, projectGroupId)) {
            return false;
        }
        if (!Pattern.matches(SEMVER_REGEX, projectVersion)) {
            return false;
        }
        return projectArtifactId != null;
    }

}
