package com.pinealpha.demos.jimage;

import java.io.File;
import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;

import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;


public class App {

  private static final String FILEPATH = "/usr/share/jimage/";

  public static void main(String[] args) throws Exception {
    System.out.println("-------- Starting Jimage --------");
    nu.pattern.OpenCV.loadLocally();

    String imageNameOut = "output.jpg";

    String query = System.getenv("QUERY") == "" ? "boom" : System.getenv("QUERY");

    System.out.println("USING QUERY --> " + query);

    ArrayList<String> images = Services.getImagesFromGiphy(query, 5);

    for (String imgID : images) {
      var usableURL = "https://i.giphy.com/media/" + imgID + "/480w_s.jpg";
      System.out.println("Trying usableURL --> " + usableURL);
      URL url = new URL(usableURL);
      BufferedImage image = ImageIO.read(url);
      try {
        ImageIO.write(image, "jpg", new File(FILEPATH + "temp.jpg"));
        Mat img = Imgcodecs.imread(FILEPATH + "temp.jpg");
        MatOfRect faces = detectFaces(img);
        drawBoxes(img, faces);
        Imgcodecs.imwrite(FILEPATH + "output.jpg", img);
        Services.postImageToSlack(FILEPATH + "output.jpg");
      } catch (Exception e) {
        System.out.println("*** COULDN'T FIND STILL IMAGE FOR " + usableURL + " ***");
      }
    }

    System.out.println("--------// Ending Jimage --------");
  }


  private static MatOfRect detectFaces(Mat image) {
    var faceCascade = new CascadeClassifier();
    faceCascade.load(FILEPATH + "haarcascade_frontalface_alt.xml");

    var faces = new MatOfRect();

    faceCascade.detectMultiScale(image, faces);

    return faces;
  }

  private static void drawBoxes(Mat image, MatOfRect faces) {
    for (Rect rect : faces.toArray()) {
      Imgproc.rectangle(image, new Point(rect.x, rect.y),
          new Point(rect.x + rect.width, rect.y + rect.height),
          new Scalar(0, 255, 0), 2);
    }
  }

}