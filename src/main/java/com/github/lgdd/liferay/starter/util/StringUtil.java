package com.github.lgdd.liferay.starter.util;

import com.github.lgdd.liferay.starter.exception.UnsupportedLiferayVersionException;
import java.util.Arrays;

/**
 * Utility class to transform strings.
 */
public abstract class StringUtil {

  /**
   * Capitalizes words joined by a separator.
   *
   * @param str       joined words
   * @param separator character separating joined words
   * @param spaced    true if capitalized words should be spaced, false otherwise
   * @return capitalized words
   */
  public static String capitalize(String str, String separator, boolean spaced) {

    String[] words = str.split(separator);
    StringBuilder capitalizeWord = new StringBuilder();
    String space = spaced ? " " : "";
    for (String w : words) {
      String first = w.substring(0, 1);
      String afterfirst = w.substring(1);
      capitalizeWord.append(first.toUpperCase())
                    .append(afterfirst)
                    .append(space);
    }

    return capitalizeWord.toString().trim();
  }

  /**
   * Returns a workspace name using the tool and version.
   *
   * @param tool    Maven or Gradle
   * @param version Liferay version (7.0, 7.1, 7.2 or 7.3)
   * @return a workspace name
   */
  public static String toWorkspaceName(String tool, String version) {

    return tool + "-liferay-workspace-" + version;
  }

  /**
   * Returns the artifact ID of a theme. Add a suffix if the app name doesn't end with -theme.
   *
   * @param themeName the name of the app
   * @return the artifact ID of the theme
   */
  public static String getThemeArtifactId(String themeName) {

    return themeName.endsWith("-theme") ? themeName : themeName + "-theme";
  }

  public static String getLiferayVersionNumber(String liferayProductVersion)
      throws UnsupportedLiferayVersionException {

    for (String version : Arrays.asList("7.0", "7.1", "7.2", "7.3", "7.4")) {
      if (liferayProductVersion.contains(version)) {
        return version;
      }
    }
    throw new UnsupportedLiferayVersionException();
  }
}
