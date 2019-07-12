package com.pinealpha.demos.jimage;

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


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App {

  private static final String FILEPATH = "/usr/share/jimage/";
  private static final String FILEIN = "test.jpg";
  private static final String FILEOUT = "output.jpg";
  private static final String BUCKETIN = "jimage-in";
  private static final String BUCKETOUT = "jimage-out";
  private static final String OCICONFIG = FILEPATH + "config";
  private static final String COMPARTMENT = "oracle-serverless-devrel";

  public static void main(String[] args) throws Exception {
    System.out.println("-------- Starting Jimage --------");

    AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(OCICONFIG, "DEFAULT");
    ObjectStorage osclient = new ObjectStorageClient(provider);

    getImage(osclient);
    processImage();
    putImage(osclient);

    System.out.println("--------// Ending Jimage --------");
  }

  private static void processImage() throws Exception {
    ConvertCmd cmd = new ConvertCmd();
    IMOperation op = new IMOperation();
    op.addImage(FILEPATH + FILEIN);
    double d = 180;
    op.rotate(d);
    op.addImage(FILEPATH + FILEOUT);
    cmd.run(op);
    Info imageInfo = new Info(FILEPATH + FILEOUT, true);
    System.out.println("Format: " + imageInfo.getImageFormat());
    System.out.println("Width: " + imageInfo.getImageWidth());
    System.out.println("Height: " + imageInfo.getImageHeight());
    System.out.println("Geometry: " + imageInfo.getImageGeometry());
    System.out.println("Depth: " + imageInfo.getImageDepth());
    System.out.println("Class: " + imageInfo.getImageClass());
  }

  private static void getImage(ObjectStorage osclient) throws Exception {
    GetObjectRequest gor = GetObjectRequest.builder()
        .namespaceName(COMPARTMENT)
        .bucketName(BUCKETIN)
        .objectName(FILEIN)
        .build();
    GetObjectResponse goResp = osclient.getObject(
        GetObjectRequest.builder()
            .namespaceName(COMPARTMENT)
            .bucketName(BUCKETIN)
            .objectName(FILEIN)
            .build());
    try (InputStream inputStream = goResp.getInputStream()) {
      File file = new File(FILEPATH + FILEIN);
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
    Path path = Paths.get(FILEPATH + FILEOUT);
    byte[] data = Files.readAllBytes(path);
    PutObjectRequest por = PutObjectRequest.builder()
        .namespaceName(COMPARTMENT)
        .bucketName(BUCKETOUT)
        .objectName(FILEOUT)
        .putObjectBody(new ByteArrayInputStream(data))
        .build();
    PutObjectResponse poResp = osclient.putObject(por);
    System.err.println("Successfully submitted put object request -- OPC request ID is " + poResp.getOpcRequestId());
  }


}