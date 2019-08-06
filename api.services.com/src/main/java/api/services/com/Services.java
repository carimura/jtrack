package api.services.com;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.math.BigInteger;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
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

  private HttpRequest.BodyPublisher ofMimeMultipartData(Map<Object, Object> data,
                                                        String boundary) throws IOException {
    var byteArrays = new ArrayList<byte[]>();
    byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=")
            .getBytes(StandardCharsets.UTF_8);
    for (Map.Entry<Object, Object> entry : data.entrySet()) {
      byteArrays.add(separator);

      if (entry.getValue() instanceof Path) {
        var path = (Path) entry.getValue();
        String mimeType = Files.probeContentType(path);
        byteArrays.add(("\"" + entry.getKey() + "\"; filename=\"" + path.getFileName()
                + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        byteArrays.add(Files.readAllBytes(path));
        byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
      }
      else {
        byteArrays.add(("\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n")
                .getBytes(StandardCharsets.UTF_8));
      }
    }
    byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));

    return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
  }

  public void postImageToSlack(String channel, File gifToPost) throws Exception {
    var gifPath = gifToPost.toPath();

    Map<Object, Object> data = new LinkedHashMap<>();
    data.put("token", this.slackToken);
    data.put("channels", channel);
    data.put("title", "some image");
    data.put("file", gifPath);
    String boundary = new BigInteger(256, new Random()).toString();

    var request = HttpRequest.newBuilder()
            .header("Content-Type", "multipart/form-data;boundary=" + boundary)
            .POST(ofMimeMultipartData(data, boundary))
            .uri(URI.create("https://slack.com/api/chat.postMessage"))
            .POST(ofMimeMultipartData(data, boundary))
            .build();

    sendRequest(request);
  }

  public void postMessageToSlack(String channel, String msg) throws Exception {
    Map<Object, Object> data = new LinkedHashMap<>();
    data.put("token", this.slackToken);
    data.put("channels", channel);
    data.put("text", msg);
    String boundary = new BigInteger(256, new Random()).toString();

    var request = HttpRequest.newBuilder()
            .header("Content-Type", "multipart/form-data;boundary=" + boundary)
            .POST(ofMimeMultipartData(data, boundary))
            .uri(URI.create("https://slack.com/api/chat.postMessage"))
            .POST(ofMimeMultipartData(data, boundary))
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

    var respString = "";

    if (response.statusCode() > 202) {
      throw new IOException(String.format("Unexpected code: %d\nBody: %s" + response.statusCode(), response.body()));
    } else {
      respString = response.body();
    }

    ObjectMapper mapper = new ObjectMapper();
    JsonNode actualObj = mapper.readTree(respString);
    var ja = actualObj.get("data");

    var images = new ArrayList<String>();

    for (int i = 0; i < ja.size(); i++) {
      var currentJo = ja.get(i);
      //System.out.println(currentJo.toString(2));
      //var imgURL = currentJo.getJSONObject("images").getJSONObject("480w_still").getString("url");
      var imgID = currentJo.get("id").textValue();

      images.add(imgID);
    }

    return images;
  }

}