package com.github.lgdd.liferay.starter.services;

import com.github.lgdd.liferay.starter.domain.LiferayApp;
import com.github.lgdd.liferay.starter.exception.CommandException;
import com.github.lgdd.liferay.starter.util.StringUtil;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Creates Java apps for a Liferay workspace.
 *
 * @see com.github.lgdd.liferay.starter.domain.LiferayAppType
 */
@Singleton
public class JavaAppService {

  /**
   * Creates Java app in a Liferay Workspace with given parameters.
   *
   * @param app           Java app parameters
   * @param baseWorkspace Liferay Workspace where the Java app will be created
   * @throws CommandException if the creation command fails
   */
  public void create(LiferayApp app, String baseWorkspace) throws CommandException {
    var packageName = "org.acme.liferay." + app.getName().replaceAll("-", ".");
    var className = StringUtil.capitalize(app.getName(), "-", false);

    commandService.run("blade", "create", "-t", app.getTemplate().getName(),
        "-p", packageName, "-c", className, "--base", baseWorkspace, app.getName());
  }

  @Inject
  CommandService commandService;

}
