package com.pinealpha.demos.jimage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class Slack {
  public static class Upload {
    public String url;
    public String type = "auto";
    public String filename = "";
    public String title = "";
    public String initial_comment = "";
  }

  public static class Message {
    public String text;
  }

  public static class SlackRequest {
    public String channel;
    public Upload upload;
    public Message message;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class SlackResponse {
    public boolean ok;
    public String error;
  }
}
