package app.main.entrypoint;

import api.facedetect.com.*;
import api.gif.com.*;

import api.services.com.Services;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;


public class App {

  private static final String FILEPATH = "/usr/share/jtrack/";

  static File processSingleGIF(FaceDetect faceDetect, String usableURL, String storeFolder) throws IOException {
    File originalGIF = new File(storeFolder + "original.gif");
    File finalGIF = new File(storeFolder + "final.gif");

    FileUtils.copyURLToFile(new URL(usableURL), originalGIF);

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

  public static void main(String[] args) throws Exception {
    FaceDetect faceDetect = new FaceDetect();

    System.out.println("-------- Starting jtrack --------");
    String query = StringUtils.isEmpty(System.getenv("QUERY")) ? "boom" : System.getenv("QUERY");
    int num = StringUtils.isEmpty(System.getenv("NUM")) ? 3 : Integer.parseInt(System.getenv("NUM"));
    Boolean previewImage = StringUtils.isEmpty(System.getenv("PREVIEW")) ? Boolean.FALSE : Boolean.parseBoolean(System.getenv("PREVIEW"));
    System.out.println("QUERY --> " + query);
    System.out.println("NUM --> " + num);
    System.out.println("PREVIEW --> " + previewImage);

    var giphyToken = System.getenv("GIPHY_TOKEN");
    var slackToken = System.getenv("SLACK_TOKEN");
    var services = new Services(slackToken, giphyToken);
    var images = services.getImagesFromGiphy(query, num);
    //images = Arrays.asList("U6pavBhRsbNbPzrwWg", "5aLrlDiJPMPFS", "XbxZ41fWLeRECPsGIJ", "5GoVLqeAOo6PK", "nXxOjZrbnbRxS");
    services.postMessageToSlack("demostream", "Processing " + images.size() + " images from keyword " + query + "...");

    for (String imgID : images) {
      var usableURL = previewImage ? "https://i.giphy.com/media/" + imgID + "/200.gif" : "https://i.giphy.com/" + imgID + ".gif";
      System.out.println("\nusableURL --> " + usableURL);
      var finalGIF = processSingleGIF(faceDetect, usableURL, FILEPATH);
      services.postImageToSlack("demostream", finalGIF);
    }
    services.postMessageToSlack("demostream", "Finished!");

    System.out.println("--------// Ending jtrack --------");
  }

}