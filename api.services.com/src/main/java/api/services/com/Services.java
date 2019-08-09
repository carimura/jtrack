package api.services.com;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;


import static java.time.temporal.ChronoUnit.SECONDS;

public class Services {

  private static HttpClient client = HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(30))
          .followRedirects(HttpClient.Redirect.NEVER)
          .proxy(ProxySelector.getDefault())
          .version(HttpClient.Version.HTTP_1_1)
          .build();

  private static ObjectMapper objectMapper = new ObjectMapper();
  private String slackToken;
  private String giphyToken;

  public Services(String sT, String gT) {
    slackToken = sT;
    giphyToken = gT;
  }

  public void postImageToSlack(String channel, File gifToPost) throws Exception {
    var publisher = new MultiPartBodyPublisher()
            .addPart("token", this.slackToken)
            .addPart("channels", channel)
            .addPart("title", "some image")
            .addPart("file", gifToPost.toPath());

    var request = HttpRequest.newBuilder()
            .uri(URI.create("https://slack.com/api/files.upload"))
            .header("Content-Type", "multipart/form-data;boundary=" + publisher.getBoundary())
            .POST(publisher.build())
            .timeout(Duration.ofMinutes(1))
            .build();

    sendRequest(request);
  }

  public void postMessageToSlack(String channel, String msg) throws Exception {
    var publisher = new MultiPartBodyPublisher()
            .addPart("token", this.slackToken)
            .addPart("channel", channel)
            .addPart("text", msg);

    var request = HttpRequest.newBuilder()
            .uri(URI.create("https://slack.com/api/chat.postMessage"))
            .header("Content-Type", "multipart/form-data;boundary=" + publisher.getBoundary())
            .POST(publisher.build())
            .build();

    sendRequest(request);
  }


  private static void sendRequest(HttpRequest request) throws IOException, InterruptedException {
    var response = client.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() > 202) {
      throw new RuntimeException("Invalid response : " + response.body());
    }

    String result = response.body();
    Slack.SlackResponse sm = objectMapper.readValue(result, Slack.SlackResponse.class);
    if (!sm.ok) {
      throw new RuntimeException("Error from slack API :" + sm.error);
    }

  }

  public ArrayList<String> getImagesFromGiphy(String query, int num) throws Exception {
    Random rand = new Random();
    int offset = rand.nextInt(20);

    var request = HttpRequest.newBuilder()
            .uri(new URI("http://api.giphy.com/v1/gifs/search?q=" + query + "&api_key=" + this.giphyToken + "&limit=" + num + "&offset=" + offset))
            .timeout(Duration.of(10, SECONDS))
            .version(HttpClient.Version.HTTP_1_1)
            .GET()
            .build();

    var response = client.send(request, HttpResponse.BodyHandlers.ofString());

    String respString = response.body();

    if (response.statusCode() > 202) {
      throw new IOException(respString);
    }

    ObjectMapper mapper = new ObjectMapper();
    JsonNode actualObj = mapper.readTree(respString);
    var ja = actualObj.get("data");

    var images = new ArrayList<String>();

    for (int i = 0; i < ja.size(); i++) {
      var currentJo = ja.get(i);
      var imgID = currentJo.get("id").textValue();

      images.add(imgID);
    }

    return images;
  }

}