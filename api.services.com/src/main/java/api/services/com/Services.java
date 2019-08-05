package api.services.com;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import org.json.JSONObject;
import org.json.JSONArray;

public class Services {
  private static OkHttpClient client = new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS).build();
  private static ObjectMapper objectMapper = new ObjectMapper();
  private String slackToken;
  private String giphyToken;

  public Services(String sT, String gT) {
    slackToken = sT;
    giphyToken = gT;
  }

  public void postImageToSlack(String channel, File gifToPost) throws Exception {
    var gifPath = gifToPost.toPath();
    byte[] data = Files.readAllBytes(gifPath);

    var requestBody = new MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("token", this.slackToken)
        .addFormDataPart("channels", channel)
        .addFormDataPart("title", "some image")
        .addFormDataPart(
                "file",
                gifPath.toString(),
                RequestBody.create(null, data))
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

  public void postMessageToSlack(String channel, String msg) throws Exception {
    var requestBody = new MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("token", this.slackToken)
        .addFormDataPart("channel", channel)
        .addFormDataPart("text", msg)
        .build();

    Request r = new Request.Builder()
        .url(new HttpUrl.Builder()
            .scheme("https")
            .host("slack.com")
            .addPathSegments("api/chat.postMessage").build())
        .post(requestBody)
        .build();

    sendRequest(r);
  }


  private static void sendRequest(Request request) throws IOException {
    Response res = client.newCall(request).execute();
    if (!res.isSuccessful()) {
      throw new RuntimeException("Invalid response : " + res.toString());
    }

    String result = res.body().string();
    Slack.SlackResponse sm = objectMapper.readValue(result, Slack.SlackResponse.class);
    if (!sm.ok) {
      throw new RuntimeException("Error from slack API :" + sm.error);
    }
    res.close();
    client.dispatcher().executorService().shutdown();
    client.connectionPool().evictAll();
  }


  public ArrayList<String> getImagesFromGiphy(String query, int num) throws Exception {
    Random rand = new Random();
    int offset = rand.nextInt(20);

    var request = new Request.Builder()
        .url("http://api.giphy.com/v1/gifs/search?q=" + query + "&api_key=" + this.giphyToken + "&limit=" + num + "&offset=" + offset)
        .build();

    var respString = "";
    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
      respString = response.body().string();
    }

    JSONObject results = new JSONObject(respString);

    JSONArray ja = results.getJSONArray("data");

    var images = new ArrayList<String>();

    for (int i = 0; i < ja.length(); i++) {
      var currentJo = ja.getJSONObject(i);
      //System.out.println(currentJo.toString(2));
      //var imgURL = currentJo.getJSONObject("images").getJSONObject("480w_still").getString("url");
      var imgID = currentJo.getString("id");

      images.add(imgID);
    }

    return images;
  }

}