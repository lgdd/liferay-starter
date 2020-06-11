package com.github.lgdd.liferay.starter.util;

import com.github.lgdd.liferay.starter.domain.LiferayApp;

public abstract class StringUtil {

  public static String capitalize(String str, String separator, boolean spaced) {
    String words[] = str.split(separator);
    String capitalizeWord = "";
    String space = spaced ? " " : "";
    for (String w : words) {
      String first = w.substring(0, 1);
      String afterfirst = w.substring(1);
      capitalizeWord += first.toUpperCase() + afterfirst + space;
    }

    return capitalizeWord.trim();
  }

  public static String toWorkspaceName(String tool, String version) {
    return tool + "-liferay-workspace-" + version;
  }

  public static String getThemeArtifactId(String themeName) {
    return themeName.endsWith("-theme") ? themeName : themeName + "-theme";
  }
}
