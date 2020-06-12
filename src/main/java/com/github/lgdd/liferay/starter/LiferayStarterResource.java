package com.github.lgdd.liferay.starter;

import com.github.lgdd.liferay.starter.domain.LiferayApp;
import com.github.lgdd.liferay.starter.domain.LiferayAppTemplate;
import com.github.lgdd.liferay.starter.domain.LiferayWorkspace;
import com.github.lgdd.liferay.starter.services.WorkspaceService;
import com.github.lgdd.liferay.starter.util.StringUtil;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Path("/api/liferay")
public class LiferayStarterResource {

  private static final Logger log = LoggerFactory.getLogger(LiferayStarterResource.class);

  private static final String JAVA_PKG_REGEX = "^(?:\\w+|\\w+\\.\\w+)+$";
  private static final String SEMVER_REGEX = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";

  @Inject
  WorkspaceService workspaceService;

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


}
