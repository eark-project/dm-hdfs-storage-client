package org.eu.eark.hsink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Util {
    
    public static void writeFile(InputStream fileInputStream, OutputStream outputStream) throws IOException {
	try {
    	    byte[] buffer = new byte[1024];
    	    int bytesRead;

    	    while((bytesRead = fileInputStream.read(buffer)) !=-1) {
    		outputStream.write(buffer, 0, bytesRead);
    	    }
    	    fileInputStream.close();
    	    outputStream.flush();
    	} catch (IOException e) {
    	    e.printStackTrace();
    	} finally {
    	    outputStream.close();
    	}
    }

}
