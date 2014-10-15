package org.eu.eark.hsink;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

/*
 * A simple command-line client for the eark jersey-service
 */
public class RestClient {

  public static final String BASE_URI = "http://localhost:8081/hsink/";

  private Client client;
  private WebTarget target;

  public RestClient() {
  }

  public void init() {
    // causes failure when creating chunked requests
    // System.setProperty("sun.net.http.allowRestrictedHeaders","true");
    ClientConfig clientConfig = new ClientConfig();
    // clientConfig.connectorProvider((new HttpUrlConnectorProvider()));
    // clientConfig.connectorProvider(new GrizzlyConnectorProvider());
    client = ClientBuilder.newClient(clientConfig);
    //client.register(MultiPartFeature.class);
    client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
    // --client.property(ClientProperties.CHUNKED_ENCODING_SIZE, 10240);
  }

  public String sendReq(String resourcePath) {
    return target.path(resourcePath).request().get(String.class);
  }

  public Response putFileReq(String resourcePath, File inFile)
      throws FileNotFoundException {

    target = client.target(BASE_URI);
    target = target.path(resourcePath).path("files").path(inFile.getName());
    InputStream fileInStream = new FileInputStream(inFile);
    String contentDisposition = "attachment; filename=\"" + inFile.getName()
        + "\"";
    Invocation.Builder invocationBuilder = target
        .request(MediaType.APPLICATION_OCTET_STREAM_TYPE);
    invocationBuilder.header("Content-Disposition", contentDisposition);
    invocationBuilder.header("Content-Length", (int) inFile.length());
    Response response = invocationBuilder.put(Entity.entity(fileInStream,
        MediaType.APPLICATION_OCTET_STREAM_TYPE));
    System.out.println("Response status: " + response.getStatus());
    System.out.println("Response: " + response.getLocation());
    // return response.readEntity(String.class);
    return response;
  }

  public String getFileReq(String resourcePath, Path filePath) throws IOException {

    
    target = client.target(BASE_URI);
    target = target.path(resourcePath).path("files").path(filePath.toString());
    System.out.println("about to GET: "+target.toString());
    File outFile = new File("dwn." + filePath.getFileName().toString());
    OutputStream fileOutputStream = new FileOutputStream(outFile);
    //--ClientResponse response = target.request().get(ClientResponse.class);
    //--System.out.println("response status: "+response.getStatus());
    //--InputStream fileInputStream = response.getEntityStream();
    //return response.toString();
    InputStream fileInputStream = target.request().get(InputStream.class);
    Util.writeFile(fileInputStream, fileOutputStream);    
    return outFile.getAbsolutePath();
  }

  /**
   * Main method.
   * 
   * @param args
   * @throws IOException
   * @throws URISyntaxException 
   */
  public static void main(String[] args) throws IOException, URISyntaxException {

    if (args == null || args.length != 1 || !(new File(args[0])).exists())
      usage();

    String resourcePath = "fileresource";
    RestClient restClient = new RestClient();
    restClient.init();

    File inFile = new File(args[0]);
    Response response = null;
    try {
      response = restClient.putFileReq(resourcePath, inFile);
    } catch (FileNotFoundException ex) {
      System.out.println(ex.toString());
    }

    // String responseMsg = restClient.sendReq(resourcePath);
    System.out.println("Request to /" + resourcePath + " returned: "
        + response);
    
    URI responseURI = response.getLocation();
    Path path = Paths.get(responseURI.getPath());
    
    String reqFileName = path.getFileName().toString();
    String reqDirName = path.getParent().getFileName().toString();
    Path filePath = Paths.get(reqDirName,reqFileName);
    
    String resultFile = null;
    try {
      resultFile = restClient.getFileReq(resourcePath, filePath);
    } catch (FileNotFoundException ex) {
      ex.printStackTrace();
    }
    System.out.println("Request to /" + resourcePath + " returned: "
        + resultFile);

  }

  public static void usage() {
    System.out
        .println("client application to upload large files to the eArk HDFS storage service");
    System.out.println("usage: java -jar JARFILE path_to_file");
    System.exit(-1);
  }

}
