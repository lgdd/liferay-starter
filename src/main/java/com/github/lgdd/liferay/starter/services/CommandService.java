package com.github.lgdd.liferay.starter.services;

import com.github.lgdd.liferay.starter.exception.CommandException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes commands to initialize Liferay Workspace and apps.
 */
@Singleton
public class CommandService {

  /**
   * Execute command and arguments.
   *
   * @param command command and arguments
   * @throws CommandException if the command fails
   */
  public void run(String... command) throws CommandException {
    var builder = new ProcessBuilder(command);
    execute(builder);
  }

  /**
   * Execute command and arguments in a given folder.
   *
   * @param directory folder where to execute the command
   * @param command   command and arguments
   * @throws CommandException if the command fails
   */
  public void runInDirectory(File directory, String... command) throws CommandException {
    var builder = new ProcessBuilder(command);
    builder.directory(directory);
    execute(builder);
  }

  /**
   * Execute a process builder containing command and arguments.
   *
   * @param builder process builder containing  command and arguments
   * @throws CommandException if the command fails
   */
  private void execute(ProcessBuilder builder) throws CommandException {
    try {
      debugCommand(builder.command());
      Process process = builder.start();
      var exitCode = process.waitFor();

      if (exitCode != 0) {
        throw new CommandException("Workspace build failed with exit code " + exitCode);
      }
      process.destroy();

    } catch (IOException | InterruptedException e) {
      throw new CommandException(e);
    }
  }

  /**
   * Prints a given command if debug is enabled.
   *
   * @param command command and arguments
   */
  private void debugCommand(List<String> command) {
    if (log.isDebugEnabled()) {
      StringBuilder builder = new StringBuilder(command.size());
      for (String arg : command) {
        builder.append(arg);
        builder.append(" ");
      }
      log.debug(builder.toString());
    }
  }

  private static final Logger log = LoggerFactory.getLogger(CommandService.class);

}
