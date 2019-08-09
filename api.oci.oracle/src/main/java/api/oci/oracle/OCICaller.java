package api.oci.oracle;

import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.PutObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OCICaller {

  private static final String BUCKETIN = "jimage-in";
  private static final String BUCKETOUT = "jimage-out";
  private static final String TENANCY = "oracle-serverless-devrel";

  public static void getImageFromOCI(String filePath, String fileIn) throws Exception {
    var provider = new ConfigFileAuthenticationDetailsProvider(filePath + "config", "DEFAULT");
    var osclient = new ObjectStorageClient(provider);
    var goResp = osclient.getObject(
        GetObjectRequest.builder()
            .namespaceName(TENANCY)
            .bucketName(BUCKETIN)
            .objectName(fileIn)
            .build());
    try (InputStream inputStream = goResp.getInputStream()) {
      var file = new File(filePath + fileIn);
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

  public static void putImageOnOCI(String filePath, String imageNameOut) throws Exception {
    var provider = new ConfigFileAuthenticationDetailsProvider(filePath + "config", "DEFAULT");
    var osclient = new ObjectStorageClient(provider);
    Path path = Paths.get(filePath + imageNameOut);
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
