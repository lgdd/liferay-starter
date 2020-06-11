package com.github.lgdd.liferay.starter.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum LiferayAppType {
  JAVA,
  JAVASCRIPT,
  THEME
}
