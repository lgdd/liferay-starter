package com.github.lgdd.liferay.starter.services;

import com.github.lgdd.liferay.starter.domain.LiferayApp;
import com.github.lgdd.liferay.starter.util.StringUtil;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class JavaAppService {

  @Inject
  CommandService commandService;

  public void create(LiferayApp app, String baseWorkspace) throws Exception {
    var packageName = "org.acme.liferay." + app.getName().replaceAll("-", ".");
    var className = StringUtil.capitalize(app.getName(), "-", false);

    commandService.run("blade", "create", "-t", app.getTemplate().getName(),
        "-p", packageName, "-c", className, "--base", baseWorkspace, app.getName());
  }
}
