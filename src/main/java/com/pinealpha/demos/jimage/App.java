package com.pinealpha.demos.jimage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.Info;

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
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
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

    getImage("test.jpg", osclient);
    //processImage("test.jpg");
    detectFace("face.jpg");
    putImage(osclient);

    System.out.println("--------// Ending Jimage --------");
  }

  private static void detectFace(String fileIn) {
    nu.pattern.OpenCV.loadShared();

    CascadeClassifier faceDetector = new CascadeClassifier();
    faceDetector.load(FILEPATH + "haarcascade_frontalface_alt.xml");

    Mat image = Imgcodecs.imread(FILEPATH + fileIn);
    MatOfRect faceDetections = new MatOfRect();
    faceDetector.detectMultiScale(image, faceDetections);

    for (Rect rect : faceDetections.toArray())
    {
      Imgproc.rectangle(image, new Point(rect.x, rect.y),
          new Point(rect.x + rect.width, rect.y + rect.height),
          new Scalar(0, 255, 0));
    }

    Imgcodecs.imwrite(FILEPATH + "output.jpg", image);
  }

  private static void processImage(String fileIn) throws Exception {
    ConvertCmd cmd = new ConvertCmd();
    IMOperation op = new IMOperation();
    op.addImage(FILEPATH + fileIn);
    double d = 180;
    op.rotate(d);
    op.addImage(FILEPATH + "output.jpg");
    cmd.run(op);
    Info imageInfo = new Info(FILEPATH + "output.jpg", true);
    System.out.println("Format: " + imageInfo.getImageFormat());
    System.out.println("Width: " + imageInfo.getImageWidth());
    System.out.println("Height: " + imageInfo.getImageHeight());
    System.out.println("Geometry: " + imageInfo.getImageGeometry());
    System.out.println("Depth: " + imageInfo.getImageDepth());
    System.out.println("Class: " + imageInfo.getImageClass());
  }

  private static void getImage(String fileIn, ObjectStorage osclient) throws Exception {
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
    }
  }

  private static void putImage(ObjectStorage osclient) throws Exception {
    Path path = Paths.get(FILEPATH + "output.jpg");
    byte[] data = Files.readAllBytes(path);
    PutObjectRequest por = PutObjectRequest.builder()
        .namespaceName(COMPARTMENT)
        .bucketName(BUCKETOUT)
        .objectName("output.jpg")
        .putObjectBody(new ByteArrayInputStream(data))
        .build();
    PutObjectResponse poResp = osclient.putObject(por);
    System.err.println("Successfully submitted put object request -- OPC request ID is " + poResp.getOpcRequestId());
  }

}