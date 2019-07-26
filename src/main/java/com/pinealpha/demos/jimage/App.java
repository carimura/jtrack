package com.pinealpha.demos.jimage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.stream.FileImageOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.StringUtils;

public class App {

  private static final String FILEPATH = "/usr/share/jimage/";

  public static File processSingleGIF(FaceDetect faceDetect, String usableURL, String storeFolder) throws IOException {
    File originalGIF = new File(storeFolder + "original.gif");
    File finalGIF = new File(storeFolder + "final.gif");

    FileUtils.copyURLToFile(new URL(usableURL), originalGIF);

    var outputStream = new FileImageOutputStream(finalGIF);
    var inputStream = new FileInputStream(originalGIF);
    var gifDecoder = new GifDecoder();

    var gifEncoder = new GifEncoder(outputStream, BufferedImage.TYPE_INT_RGB, 0, true);

    gifDecoder.read(inputStream);

    faceDetect.processFrameWithDetections(gifDecoder, (BufferedImage img) -> {
      gifEncoder.writeToSequence(img);
      return img;
    });

    gifEncoder.close();
    outputStream.close();

    return finalGIF;
  }

  public static void main(String[] args) throws Exception {
    // should happen only once
    FaceDetect faceDetect = new FaceDetect();

    System.out.println("-------- Starting Jimage --------");
    String query = StringUtils.isEmpty(System.getenv("QUERY")) ? "boom" : System.getenv("QUERY");
    int num = StringUtils.isEmpty(System.getenv("NUM")) ? 3 : Integer.parseInt(System.getenv("NUM"));
    Boolean previewImage = StringUtils.isEmpty(System.getenv("PREVIEW")) ? Boolean.FALSE : Boolean.parseBoolean(System.getenv("PREVIEW"));
    System.out.println("QUERY --> " + query);
    System.out.println("NUM --> " + num);
    System.out.println("PREVIEW --> " + previewImage);

    List<String> images = Services.getImagesFromGiphy(query, num);

    //images = Arrays.asList("U6pavBhRsbNbPzrwWg", "5aLrlDiJPMPFS", "XbxZ41fWLeRECPsGIJ", "5GoVLqeAOo6PK", "nXxOjZrbnbRxS");

    Services.postMessageToSlack("demostream", "Processing " + images.size() + " images from keyword " + query + "...");

    var timer = new StopWatch();
    var timer2 = new StopWatch();
    for (String imgID : images) {
      timer = StopWatch.createStarted();
      var usableURL = previewImage ? "https://i.giphy.com/media/" + imgID + "/200.gif" : "https://i.giphy.com/" + imgID + ".gif";
      System.out.println("\nusableURL --> " + usableURL);
      timer2 = StopWatch.createStarted();
      var finalGIF = processSingleGIF(faceDetect, usableURL, FILEPATH);
      System.out.println("processGIF took " + timer2.toString());
      timer2 = StopWatch.createStarted();
      Services.postImageToSlack("demostream", finalGIF);
      System.out.println("post to slack took " + timer2.toString());
      System.out.println("entire image took " + timer.toString());
    }
    Services.postMessageToSlack("demostream", "Finished!");

    System.out.println("--------// Ending Jimage --------");
  }

}