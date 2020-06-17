package com.github.lgdd.liferay.starter;

import com.github.lgdd.liferay.starter.domain.LiferayWorkspace;
import com.github.lgdd.liferay.starter.services.WorkspaceService;
import com.github.lgdd.liferay.starter.util.StringUtil;
import java.util.Arrays;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API to generate and download a Liferay Workspace.
 */
@Path("/api/liferay")
public class LiferayStarterResource {

  /**
   * Generates a Liferay Workspace given project parameters.
   *
   * @param tool      Maven or Gradle
   * @param version   Liferay version (7.0, 7.1, 7.2 or 7.3)
   * @param workspace Project parameters
   * @return zip file containing a Liferay Workspace
   * @see LiferayWorkspace
   */
  @POST
  @Path("/{version}/workspace/{tool}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces("application/zip")
  public Response workspace(@PathParam String tool, @PathParam String version,
      LiferayWorkspace workspace) {

    if (!validateWorkspaceParams(tool, version, workspace)) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    try {
      var workspaceZip = workspaceService
          .createWorkspaceZip(tool, version, workspace);

      var filename = workspace.getProjectArtifactId().isEmpty() ?
          StringUtil.toWorkspaceName(tool, version) : workspace.getProjectArtifactId();

      Response.ResponseBuilder responseBuilder = Response.ok(workspaceZip);
      responseBuilder.type("application/zip");
      responseBuilder.header("Content-disposition", "attachment; filename=" + filename + ".zip");
      return responseBuilder.build();
    } catch (Exception e) {
      log.error(e.getLocalizedMessage(), e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Checks if project parameters are valid.
   *
   * @param tool      Maven or Gradle
   * @param version   Liferay version (7.0, 7.1, 7.2 or 7.3)
   * @param workspace Project parameters
   * @return true if parameters are valid, false otherwise
   */
  private boolean validateWorkspaceParams(String tool, String version, LiferayWorkspace workspace) {
    var projectGroupId = workspace.getProjectGroupId();
    var projectArtifactId = workspace.getProjectArtifactId();
    var projectVersion = workspace.getProjectVersion();
    var liferayVersions = Arrays.asList("7.3", "7.2", "7.1", "7.0");
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

  private static final Logger log = LoggerFactory.getLogger(LiferayStarterResource.class);
  private static final String JAVA_PKG_REGEX = "^(?:\\w+|\\w+\\.\\w+)+$";
  private static final String SEMVER_REGEX = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";

  @Inject
  WorkspaceService workspaceService;

}
