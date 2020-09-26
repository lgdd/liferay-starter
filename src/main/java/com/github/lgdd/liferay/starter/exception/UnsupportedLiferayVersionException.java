package com.github.lgdd.liferay.starter.exception;

public class UnsupportedLiferayVersionException
    extends Exception {

  public UnsupportedLiferayVersionException() {
    super();
  }

  public UnsupportedLiferayVersionException(String message) {
    super(message);
  }

  public UnsupportedLiferayVersionException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnsupportedLiferayVersionException(Throwable cause) {
    super(cause);
  }

}
