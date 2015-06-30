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

  public final static String BASE_URI = "http://localhost:8081/hsink/";
  public final static String FILE_RESOURCE = "fileresource";

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
    System.out.println("sending: "+inFile.length()+" bytes...");
    Response response = invocationBuilder.put(Entity.entity(fileInStream,
        MediaType.APPLICATION_OCTET_STREAM_TYPE));
    System.out.println("Response status: " + response.getStatus());
    System.out.println("Response: " + response.getLocation());
    // return response.readEntity(String.class);
    return response;
  }

  public String getFileReq(String resourcePath, Path filePath, File outFile) throws IOException {

    
    target = client.target(BASE_URI);
    target = target.path(resourcePath).path("files").path(filePath.toString());
    System.out.println("Target for GET request: "+target.toString());
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

    if (args == null || args.length < 2 || args.length > 3)
      usage();
    
    File inFile = null;
    String baseURI = BASE_URI;

    String resourcePath = FILE_RESOURCE;
    RestClient restClient = new RestClient();
    restClient.init();
    Response response = null;
    URI downloadURI = null;
    File outFile = null;
    
    if(args[0].toLowerCase().equals("-u") || args[0].toLowerCase().equals("-t")) {
    	inFile = new File(args[1]);
    	if(!inFile.exists()) usage();
    	if(args.length > 2) baseURI = args[2];
        try {
            response = restClient.putFileReq(resourcePath, inFile);
          } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
          }    		    	
        System.out.println("Request to /" + resourcePath + " returned: " + response);
    }
    
    if(args[0].toLowerCase().equals("-d") || args[0].toLowerCase().equals("-t")) {
    	//-t roundtrip
    	if(response != null) downloadURI = response.getLocation();
    	else downloadURI = new URI(args[1]);

    	Path path = Paths.get(downloadURI.getPath());
    	String reqFileName = path.getFileName().toString();
        String reqDirName = path.getParent().getFileName().toString();
        Path filePath = Paths.get(reqDirName,reqFileName);
        
        if(response == null && args.length > 2) outFile = new File(args[2]);
        else outFile = new File("dwn." + filePath.getFileName().toString());
        System.out.println("outFile: "+outFile);
        
        try {
          System.out.println("Requesting: "+resourcePath+"/"+filePath);
          String resultFile = restClient.getFileReq(resourcePath, filePath, outFile);
          System.out.println("Output written to: " + resultFile);
        } catch (FileNotFoundException ex) {
          ex.printStackTrace();
        }
    }
  }

  public static void usage() {
    System.out
        .println("client application to upload amd download (large) files from/to HSink storage service");
    System.out.println("usage: java -jar JARFILE [options] [source URI] [target URI]");
    System.out.println("...file upload:   java -jar client.jar -u ./file [http://localhost:8081/hsink]");
    System.out.println("...file download: java -jar client.jar -d http://localhost:8081/hsink/.../file ./file");
    System.out.println("...roundtrip test: java -jar client.jar -t ./file [http://localhost:8081/hsink]");
    
    System.exit(-1);
  }

}
