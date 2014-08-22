package org.eark.hdfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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

public class RestClient {

public static final String BASE_URI = "http://localhost:8080/myapp/";
	
    private Client client;
    private WebTarget target;
  
    public RestClient() {
	super();
    }
  
    public void init() {
  	ClientConfig clientConfig = new ClientConfig();
  	//clientConfig.connectorProvider((new HttpUrlConnectorProvider()));
  	//clientConfig.connectorProvider(new GrizzlyConnectorProvider());
  	client = ClientBuilder.newClient(clientConfig);
  	client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
  	//--client.property(ClientProperties.CHUNKED_ENCODING_SIZE, 10240);
  	target = client.target(BASE_URI);
    }
  
    public String sendReq(String resourcePath) {
  	return target.path(resourcePath).request().get(String.class);
    }
  
    public String putFileReq(String resourcePath, File inFile) throws FileNotFoundException {
	InputStream fileInStream = new FileInputStream(inFile);
        String contentDisposition = "attachment; filename=\"" + inFile.getName()+"\"";
        target = target.path(resourcePath).path("upload").path(inFile.getName());    		    		
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        invocationBuilder.header("Content-Disposition", contentDisposition);
        invocationBuilder.header("Content-Length", (int)inFile.length());
        Response response = invocationBuilder.put(Entity.entity(fileInStream, MediaType.APPLICATION_OCTET_STREAM_TYPE));
        System.out.println("Response status: "+response.getStatus());
        System.out.println("Response: "+response.getLocation());
        //return response.readEntity(String.class);
        return response.toString();
    }
  
    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

  	if(args == null || args.length != 1 || !(new File(args[0])).exists()) 
  		usage();

  	String resourcePath = "myresource";
  	RestClient restClient = new RestClient();
  	restClient.init();  	
  	
  	File inFile = new File(args[0]);    
  	String responseMsg = "null";

  	try {
  		responseMsg = restClient.putFileReq(resourcePath, inFile);
  	} catch (FileNotFoundException ex) {
  		System.out.println(ex.toString());
  	}
    
  	//String responseMsg = restClient.sendReq(resourcePath);
  	System.out.println("Request to /"+resourcePath+" returned: "+responseMsg);
    }
  
    public static void usage() {
  	System.out.println("client application to upload large files to the eArk HDFS storage service");
  	System.out.println("usage: java -jar JARFILE path_to_file");
  	System.exit(-1);
    }

}
