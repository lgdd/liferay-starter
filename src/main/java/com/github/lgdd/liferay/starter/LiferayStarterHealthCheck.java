package com.github.lgdd.liferay.starter;

import com.github.lgdd.liferay.starter.services.ArchiveService;
import com.github.lgdd.liferay.starter.services.CommandService;
import com.github.lgdd.liferay.starter.services.ProjectFileService;
import com.github.lgdd.liferay.starter.services.ThemeService;
import com.github.lgdd.liferay.starter.services.WorkspaceService;
import org.eclipse.microprofile.health.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Readiness
@ApplicationScoped
public class LiferayStarterHealthCheck implements HealthCheck {

  @Inject
  WorkspaceService workspaceService;

  @Inject
  ArchiveService archiveService;

  @Inject
  CommandService commandService;

  @Inject
  ProjectFileService projectFileService;

  @Inject
  ThemeService themeService;

  @Override
  public HealthCheckResponse call() {

    var responseBuilder = HealthCheckResponse.named("Liferay Starter Health Check");

    if (workspaceService != null
        && archiveService != null
        && commandService != null
        && projectFileService != null
        && themeService != null) {
      responseBuilder.up();
    } else {
      responseBuilder.down();
    }
    return responseBuilder.build();
  }
}
