package com.pinealpha.demos.jimage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public class PostToSlack {
  private static OkHttpClient client = new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS).build();

  private static ObjectMapper objectMapper = new ObjectMapper();

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

  public static void post(String imagePath) throws Exception {
    System.out.println("Posting to Slack");

    String token = System.getenv("SLACK_TOKEN");

    byte[] data = Files.readAllBytes(new File(imagePath).toPath());

    RequestBody requestBody = new MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("token", token)
        .addFormDataPart("channels", "demostream")
        .addFormDataPart("title", "some image")
        .addFormDataPart("file", imagePath, RequestBody.create(null, data))
        .build();

    Request r = new Request.Builder()
        .url(new HttpUrl.Builder()
            .scheme("https")
            .host("slack.com")
            .addPathSegments("api/files.upload").build())
        .post(requestBody)
        .build();

    sendRequest(r);
  }

  private static void sendRequest(Request request) throws IOException {
    System.err.println("Sending" + request);
    Response res = client.newCall(request).execute();
    System.err.println("Got response " + res);
    if (!res.isSuccessful()) {
      throw new RuntimeException("Invalid response : " + res.toString());
    }

    String result = res.body().string();
    System.err.println("Got result" + result);
    SlackResponse sm = objectMapper.readValue(result, SlackResponse.class);
    if (!sm.ok) {
      throw new RuntimeException("Error from slack API :" + sm.error);
    }
    res.close();
    client.dispatcher().executorService().shutdown();
    client.connectionPool().evictAll();
  }

}