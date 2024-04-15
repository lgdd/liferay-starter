package com.github.lgdd.liferay.starter;

import com.github.lgdd.liferay.starter.services.ArchiveService;
import com.github.lgdd.liferay.starter.services.CommandService;
import com.github.lgdd.liferay.starter.services.JavaAppService;
import com.github.lgdd.liferay.starter.services.JavaScriptAppService;
import com.github.lgdd.liferay.starter.services.ProjectFileService;
import com.github.lgdd.liferay.starter.services.ThemeService;
import com.github.lgdd.liferay.starter.services.WorkspaceService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Health check endpoint to verify application readiness.
 */
@Readiness
@ApplicationScoped
public class LiferayStarterHealthCheck implements HealthCheck {

  @Override
  public HealthCheckResponse call() {

    var responseBuilder = HealthCheckResponse.named("Liferay Starter Health Check");

    if (workspaceService != null
            && archiveService != null
            && commandService != null
            && projectFileService != null
            && javaAppService != null
            && javaScriptAppService != null
            && themeService != null) {
      responseBuilder.up();
    } else {
      responseBuilder.down();
    }
    return responseBuilder.build();
  }

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

  @Inject
  JavaAppService javaAppService;

  @Inject
  JavaScriptAppService javaScriptAppService;

}
