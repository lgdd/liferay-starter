package com.github.lgdd.liferay.starter;

import com.github.lgdd.liferay.starter.services.WorkspaceCreator;
import org.eclipse.microprofile.health.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Readiness
@ApplicationScoped
public class LiferayStarterHealthCheck implements HealthCheck {

	@Inject
	private WorkspaceCreator workspaceCreator;

	@Override
	public HealthCheckResponse call() {

		HealthCheckResponseBuilder responseBuilder =
										HealthCheckResponse.named("Liferay Starter Health Check");
		if(workspaceCreator != null) {
			responseBuilder.up();
		} else {
			responseBuilder.down();
		}
		return responseBuilder.build();
	}
}
