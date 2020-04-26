package com.github.lgdd.liferay.starter;

import com.github.lgdd.liferay.starter.services.ArchiveService;
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

    @Override
    public HealthCheckResponse call() {

        HealthCheckResponseBuilder responseBuilder =
                HealthCheckResponse.named("Liferay Starter Health Check");
        if (workspaceService != null && archiveService != null) {
            responseBuilder.up();
        } else {
            responseBuilder.down();
        }
        return responseBuilder.build();
    }
}
