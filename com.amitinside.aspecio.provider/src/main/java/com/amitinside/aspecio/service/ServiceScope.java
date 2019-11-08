package com.amitinside.aspecio.service;

public enum ServiceScope {

  SINGLETON("singleton"), BUNDLE("bundle"), PROTOTYPE("prototype");

  private final String value;

  ServiceScope(final String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static ServiceScope fromString(final String s) {
    if (s == null) {
      return SINGLETON;
    }
    switch (s) {
      case "bundle":
        return BUNDLE;
      case "prototype":
        return PROTOTYPE;
      default:
        return SINGLETON;
    }
  }
}
