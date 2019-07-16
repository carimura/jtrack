package com.pinealpha.demos.jimage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.responses.PutObjectResponse;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;

import org.opencv.core.Core;
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
  private static final String COMPARTMENT = "oracle-serverless-devrel";

  public static void main(String[] args) throws Exception {
    System.out.println("-------- Starting Jimage --------");
    ConfigFileAuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(OCICONFIG, "DEFAULT");
    ObjectStorageClient osclient = new ObjectStorageClient(provider);
    nu.pattern.OpenCV.loadLocally();

    Mat image = Imgcodecs.imread(FILEPATH + "face.jpg");

    MatOfRect faces = detectFaces(image);

    drawBoxes(image, faces);

    Imgcodecs.imwrite(FILEPATH + "output.jpg", image);

    putImageOnOCI(FILEPATH + "output.jpg", osclient);

    System.out.println("--------// Ending Jimage --------");
  }

  private static MatOfRect detectFaces(Mat image) {
    CascadeClassifier faceCascade = new CascadeClassifier();
    faceCascade.load(FILEPATH + "haarcascade_frontalface_alt.xml");

    MatOfRect faces = new MatOfRect();

    faceCascade.detectMultiScale(image, faces);

    return faces;
  }

  private static void drawBoxes(Mat image, MatOfRect faces) {
    for (Rect rect : faces.toArray()) {
      Imgproc.rectangle(image, new Point(rect.x, rect.y),
          new Point(rect.x + rect.width, rect.y + rect.height),
          new Scalar(255, 0, 0), 2);
    }
  }


  private static void getImageFromOCI(String fileIn, ObjectStorage osclient) throws Exception {
    GetObjectRequest gor = GetObjectRequest.builder()
        .namespaceName(COMPARTMENT)
        .bucketName(BUCKETIN)
        .objectName(fileIn)
        .build();
    GetObjectResponse goResp = osclient.getObject(
        GetObjectRequest.builder()
            .namespaceName(COMPARTMENT)
            .bucketName(BUCKETIN)
            .objectName(fileIn)
            .build());
    try (InputStream inputStream = goResp.getInputStream()) {
      File file = new File(FILEPATH + fileIn);
      try (FileOutputStream outputStream = new FileOutputStream(file)) {

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

  private static void putImageOnOCI(String file, ObjectStorage osclient) throws Exception {
    Path path = Paths.get(file);
    byte[] data = Files.readAllBytes(path);
    PutObjectRequest por = PutObjectRequest.builder()
        .namespaceName(COMPARTMENT)
        .bucketName(BUCKETOUT)
        .objectName("output.jpg")
        .putObjectBody(new ByteArrayInputStream(data))
        .build();
    PutObjectResponse poResp = osclient.putObject(por);
    System.err.println("Successfully submitted PUT object request -- Request ID: " + poResp.getOpcRequestId());
  }

}