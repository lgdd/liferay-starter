package com.github.lgdd.liferay.starter;

import com.github.lgdd.liferay.starter.services.WorkspaceCreator;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static javax.ws.rs.core.Response.ok;

@Path("/api")
public class LiferayStarterResource {

    private static final Logger log = LoggerFactory.getLogger(LiferayStarterResource.class);

    @Inject
    WorkspaceCreator workspaceCreator;

    @GET
    @Path("/workspace/{tool}/{version}")
    @Produces("application/zip")
    public Response workspace(@PathParam String tool, @PathParam String version,
                              @QueryParam("projectGroupId") String projectGroupId,
                              @QueryParam("projectArtifactId") String projectArtifactId,
                              @QueryParam("projectVersion") String projectVersion) {
        if(validateWorkspaceParams(tool, version, projectArtifactId)) {
            try {
                byte[] workspaceZip = workspaceCreator
                        .createWorkspaceZip(tool, version, projectGroupId, projectArtifactId, projectVersion);

                String filename = projectArtifactId.isEmpty()?
                        workspaceCreator.getWorkspaceName(tool, version) : projectArtifactId;

                Response.ResponseBuilder responseBuilder = Response.ok(workspaceZip);
                responseBuilder.type("application/zip");
                responseBuilder.header("Content-disposition", "attachment; filename=" + filename + ".zip");
                return responseBuilder.build();
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    private boolean validateWorkspaceParams(String tool, String version, String projectArtifactId) {
        List<String> liferayVersions = Arrays.asList("7.3", "7.2", "7.1", "7.0");
        if (!("gradle".equalsIgnoreCase(tool) || "maven".equalsIgnoreCase(tool))) {
            return false;
        }
        if(!liferayVersions.contains(version)) {
            return false;
        }
        return projectArtifactId != null;
    }

}
