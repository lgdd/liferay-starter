package com.github.lgdd.liferay.starter.services;

import com.github.lgdd.liferay.starter.exception.CommandException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CommandService {

  private static final Logger log = LoggerFactory.getLogger(CommandService.class);

  public void run(String... command) throws CommandException {
    var builder = new ProcessBuilder(command);
    execute(builder);
  }

  public void runInDirectory(File directory, String... command) throws CommandException {
    var builder = new ProcessBuilder(command);
    builder.directory(directory);
    execute(builder);
  }

  private void execute(ProcessBuilder builder) throws CommandException {
    try {
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

}
