package com.pinealpha.demos.jimage;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.stream.FileImageOutputStream;

import org.apache.commons.io.FileUtils;

public class App {

  private static final String FILEPATH = "/usr/share/jimage/";

  public static void main(String[] args) throws Exception {
    System.out.println("-------- Starting Jimage --------");
    String query = System.getenv("QUERY").equals("") ? "boom" : System.getenv("QUERY");
    int num = System.getenv("NUM").equals("") ? 3 : Integer.parseInt(System.getenv("NUM"));
    System.out.println("QUERY --> " + query);
    System.out.println("NUM --> " + num);

    ArrayList<String> images = Services.getImagesFromGiphy(query, num);

    for (String imgID : images) {
      var usableURL = "https://i.giphy.com/" + imgID + ".gif";
      System.out.println("usableURL --> " + usableURL);

      File originalGIF = new File(FILEPATH + "temp.gif");
      File finalGIF = new File(FILEPATH + "output.gif");
      var outputStream = new FileImageOutputStream(finalGIF);
      var inputStream = new FileInputStream(originalGIF);

      FileUtils.copyURLToFile(new URL(usableURL), originalGIF);

      var gifDecoder = new GifDecoder();

      var writer = new GifEncoder(outputStream, BufferedImage.TYPE_INT_RGB, 0, true);
      FaceDetect faceDetect = new FaceDetect();

      // note: Pair class is just a placeholder for origin GIF frame and its OpenCV Mat representation
      ArrayList<Pair> mats = gifDecoder.framesToMat(inputStream);

      for (Pair matFrame: mats) {
        writer.writeToSequence(
                faceDetect.processImageFromMat(matFrame)
        );
      }

      writer.close();
      outputStream.close();

      Services.postImageToSlack(finalGIF);
    }

    System.out.println("--------// Ending Jimage --------");
  }

}