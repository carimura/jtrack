package com.pinealpha.demos.jimage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.responses.PutObjectResponse;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;


public class App {

  private static final String FILEPATH = "/usr/share/jimage/";
  private static final String BUCKETIN = "jimage-in";
  private static final String BUCKETOUT = "jimage-out";
  private static final String OCICONFIG = FILEPATH + "config";
  private static final String TENANCY = "oracle-serverless-devrel";

  public static void main(String[] args) throws Exception {
    System.out.println("-------- Starting Jimage --------");
    nu.pattern.OpenCV.loadLocally();
    var provider = new ConfigFileAuthenticationDetailsProvider(OCICONFIG, "DEFAULT");
    var osclient = new ObjectStorageClient(provider);

    String imageNameIn = "face.jpg";
    String imageNameOut = "output.jpg";

    Mat img = Imgcodecs.imread(FILEPATH + imageNameIn);
    MatOfRect faces = detectFaces(img);
    drawBoxes(img, faces);
    Imgcodecs.imwrite(FILEPATH + imageNameOut, img);

    PostToSlack.post(FILEPATH + imageNameOut);

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


  private static void getImageFromOCI(String fileIn, ObjectStorage osclient) throws Exception {
    var goResp = osclient.getObject(
        GetObjectRequest.builder()
            .namespaceName(TENANCY)
            .bucketName(BUCKETIN)
            .objectName(fileIn)
            .build());
    try (InputStream inputStream = goResp.getInputStream()) {
      var file = new File(FILEPATH + fileIn);
      try (var outputStream = new FileOutputStream(file)) {

        int read;
        byte[] bytes = new byte[1024];

        while ((read = inputStream.read(bytes)) != -1) {
          outputStream.write(bytes, 0, read);
        }
        // maybe later: commons-io
        //IOUtils.copy(inputStream, outputStream);
      }

      System.err.println("Successfully submitted GET object request -- Request ID: " + goResp.getOpcRequestId());
    }
  }

  private static void putImageOnOCI(String imageNameOut, ObjectStorage osclient) throws Exception {
    Path path = Paths.get(FILEPATH + imageNameOut);
    byte[] data = Files.readAllBytes(path);
    var por = PutObjectRequest.builder()
        .namespaceName(TENANCY)
        .bucketName(BUCKETOUT)
        .objectName(imageNameOut)
        .putObjectBody(new ByteArrayInputStream(data))
        .build();
    PutObjectResponse poResp = osclient.putObject(por);
    System.err.println("Successfully submitted PUT object request -- Request ID: " + poResp.getOpcRequestId());
  }

}