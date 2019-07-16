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

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

import org.bytedeco.flandmark.FLANDMARK_Model;
import static org.bytedeco.flandmark.global.flandmark.flandmark_detect;
import static org.bytedeco.flandmark.global.flandmark.flandmark_init;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.*;
import org.bytedeco.opencv.opencv_highgui.*;
import org.bytedeco.opencv.opencv_imgproc.*;
import org.bytedeco.opencv.opencv_objdetect.*;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_face.*;
import static org.bytedeco.opencv.global.opencv_highgui.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_objdetect.*;

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

    // get image from object storage
    getImageFromOCI("test.jpg", osclient);

    Mat image = imread(FILEPATH + "face.jpg");

    // detect face boxes - simple
    detectFaceBoxes(image);

    // detect face landmarks - fancy
    //Point2fVectorVector landmarks = detectFaceMarks(image);

    // draw something on the faces and write result to disk
    //drawFaces(landmarks, image);

    // put the image back on object store
    putImageOnOCI(osclient);

    System.out.println("--------// Ending Jimage --------");
  }

  private static void detectFaceBoxes(Mat image) throws Exception {
    File faceCascadeFile = new File(FILEPATH + "haarcascade_frontalface_alt.xml");
    FLANDMARK_Model model = flandmark_init(FILEPATH + "face_landmark_model.dat");
    CascadeClassifier faceCascade = new CascadeClassifier(faceCascadeFile.getCanonicalPath());

    RectVector faces = new RectVector();
    faceCascade.detectMultiScale(image, faces);

    long nFaces = faces.size();
    System.out.println("Faces detected: " + nFaces);
    if (nFaces == 0) {
      throw new Exception("No faces detected");
    }
    int bbox[] = new int[4];
    for (int iface = 0; iface < nFaces; ++iface) {
      Rect rect = faces.get(iface);

      bbox[0] = rect.x();
      bbox[1] = rect.y();
      bbox[2] = rect.x() + rect.width();
      bbox[3] = rect.y() + rect.height();

      Mat orig = image;
      rectangle(orig,
          new Point(bbox[0], bbox[1]),
          new Point(bbox[2], bbox[3]),
          new Scalar(255, 0, 255, 128),
          2, 1, 0
      );
    }
    imwrite(FILEPATH + "output.jpg", image);

  }

  private static Point2fVectorVector detectFaceMarks(Mat image) throws Exception {
    File faceCascadeFile = new File(FILEPATH + "haarcascade_frontalface_alt.xml");
    CascadeClassifier faceCascade = new CascadeClassifier(faceCascadeFile.getCanonicalPath());
    FacemarkKazemi facemark = FacemarkKazemi.create();
    facemark.loadModel(FILEPATH + "face_landmark_model.dat");

    RectVector faces = new RectVector();

    faceCascade.detectMultiScale(image, faces);
    long nFaces = faces.size();
    System.out.println("Faces detected: " + nFaces);

    if (nFaces == 0) {
      throw new Exception("No faces detected");
    }

    Point2fVectorVector landmarks = new Point2fVectorVector();

    boolean success = facemark.fit(image, faces, landmarks);

    return landmarks;
  }

  private static void drawFaces(Point2fVectorVector landmarks, Mat image) {
      for (long i = 0; i < landmarks.size(); i++) {
        Point2fVector v = landmarks.get(i);
        drawFacemarks(image, v, Scalar.YELLOW);
      }
      imwrite(FILEPATH + "output.jpg", image);
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
    }
  }

  private static void putImageOnOCI(ObjectStorage osclient) throws Exception {
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