package app.main.entrypoint;

import api.facedetect.com.*;
import api.gif.com.*;

import api.services.com.Services;

import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


public class App {

  private static final String FILEPATH = "/usr/share/jtrack/";

  static File processSingleGIF(FaceDetect faceDetect, String usableURL, String storeFolder) throws IOException {
    File originalGIF = new File(storeFolder + "original.gif");
    File finalGIF = new File(storeFolder + "final.gif");

    InputStream in = new URL(usableURL).openStream();
    Files.copy(in, originalGIF.toPath(), StandardCopyOption.REPLACE_EXISTING);

    FileImageOutputStream outputStream = new FileImageOutputStream(finalGIF);
    FileInputStream inputStream = new FileInputStream(originalGIF);
    GifDecoder gifDecoder = new GifDecoder();

    var gifEncoder = new GifEncoder(outputStream, BufferedImage.TYPE_INT_RGB, 0, true);

    gifDecoder.read(inputStream);

    var wg = new FiberWaitGroup();
    // note: Pair class is just a placeholder for origin GIF frame and its OpenCV Mat representation
    faceDetect.processFrameWithDetections(wg, gifDecoder);

    // await until all fibers are done
    // retrieve results from futures
    var resultedFrames = wg.awaitForResult();
    System.out.println("all fibers are out");

    for (Object result: resultedFrames) {
      gifEncoder.writeToSequence((BufferedImage) result);
    }

    gifEncoder.close();
    outputStream.close();

    return finalGIF;
  }

  private static boolean isEmpty(CharSequence cs) {
    return cs == null || cs.length() == 0;
  }

  public static void main(String[] args) throws Exception {
    FaceDetect faceDetect = new FaceDetect();

    System.out.println("-------- Starting jtrack --------");
    String query = isEmpty(System.getenv("QUERY")) ? "boom" : System.getenv("QUERY");
    int num = isEmpty(System.getenv("NUM")) ? 3 : Integer.parseInt(System.getenv("NUM"));
    var previewImage = isEmpty(System.getenv("PREVIEW")) ? Boolean.FALSE : Boolean.parseBoolean(System.getenv("PREVIEW"));
    System.out.println("QUERY --> " + query);
    System.out.println("NUM --> " + num);
    System.out.println("PREVIEW --> " + previewImage);

    var giphyToken = System.getenv("GIPHY_TOKEN");
    var slackToken = System.getenv("SLACK_TOKEN");
    var services = new Services(slackToken, giphyToken);

    // todo: foreach + lambda instead of for-loop
    var images = services.getImagesFromGiphy(query, num);
    //images = Arrays.asList("U6pavBhRsbNbPzrwWg", "5aLrlDiJPMPFS", "XbxZ41fWLeRECPsGIJ", "5GoVLqeAOo6PK", "nXxOjZrbnbRxS");

    // todo: wrap with fiber, outbound net IO operation
    services.postMessageToSlack("demostream", "Processing " + images.size() + " images from keyword " + query + "...");

    for (String imgID : images) {
      var usableURL = previewImage ? "https://i.giphy.com/media/" + imgID + "/200.gif" : "https://i.giphy.com/" + imgID + ".gif";
      System.out.println("\nusableURL --> " + usableURL);
      var finalGIF = processSingleGIF(faceDetect, usableURL, FILEPATH);

      // todo: wrap with fiber, outbound net IO operation
      services.postImageToSlack("demostream", finalGIF);
    }

    // todo: wrap with fiber, outbound net IO operation
    services.postMessageToSlack("demostream", "Finished!");

    System.out.println("--------// Ending jtrack --------");
  }

}