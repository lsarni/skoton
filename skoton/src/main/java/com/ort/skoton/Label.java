package com.ort.skoton;

public class Label {
  private String language;
  private String value;

  public Label(String language, String value) {
    this.language = language;
    this.value = value;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
