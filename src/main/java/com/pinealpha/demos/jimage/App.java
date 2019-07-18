package com.pinealpha.demos.jimage;

import java.io.File;
import java.util.ArrayList;

import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;

import org.apache.commons.io.FileUtils;

public class App {

  private static final String FILEPATH = "/usr/share/jimage/";

  public static void main(String[] args) throws Exception {
    System.out.println("-------- Starting Jimage --------");
    String query = System.getenv("QUERY") == "" ? "boom" : System.getenv("QUERY");
    int num = System.getenv("NUM") == "" ? 3 : Integer.parseInt(System.getenv("NUM"));
    System.out.println("QUERY --> " + query);
    System.out.println("NUM --> " + num);

    ArrayList<String> images = Services.getImagesFromGiphy(query, num);

    for (String imgID : images) {
      var usableURL = "https://i.giphy.com/" + imgID + ".gif";
      System.out.println("usableURL --> " + usableURL);

      FileUtils.copyURLToFile(new URL(usableURL), new File(FILEPATH + "temp.gif"));

      var gifDecoder = new GifDecoder();
      var output = new FileImageOutputStream(new File(FILEPATH + "output.gif"));
      var writer = new GifEncoder(output, BufferedImage.TYPE_INT_RGB, 0, true);
      FaceDetect faceDetect = new FaceDetect();

      ArrayList<String> framePaths = gifDecoder.saveFramesFrom(FILEPATH + "temp.gif");

      for (String imgPath : framePaths) {
        writer.writeToSequence(
            faceDetect.processImage(framePaths.indexOf(imgPath), imgPath)
        );
      }
      writer.close();
      output.close();
      gifDecoder.close();
      faceDetect.cleanUp(framePaths.size());

      Services.postImageToSlack(FILEPATH + "output.gif");
    }

    System.out.println("--------// Ending Jimage --------");
  }

}