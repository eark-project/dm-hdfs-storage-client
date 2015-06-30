#HSink Client - A Java-based REST client for transfering large files 

##About
HSink Client (dm-hdfs-storage-client) is a Java-based client for HSink [dm-hdfs-storage](https://github.com/eark-project/dm-hdfs-storage), an Jersey based RESTful Web service for transfering large files over HTTP. HSink makes use of chunked transfer encoding allowing a client to transmit large files using HTTP. HSink Client is a Java based alternative to using amn HTTP/1.1 compliant HTTP client like *curl*.

##Building the application
The application depends on the Maven projects [global-configuration](https://github.com/eark-project/global-configuration) and [dm-parent](https://github.com/eark-project/dm-parent). Both projects must be downloaded, built, and installed before HSink can be compiled.

```bash
## download the project
git clone <THE_PROJECT_URL>
## move to the project's main directory
## build an install the project
mvn clean install
```

HSink Client can be easily built using Maven. 

```bash
git clone https://github.com/eark-project/dm-hdfs-storage-client.git
cd dm-hdfs-storage-clent
mvn clean package
```
##Using HSink Client
Hsink provides a command-line interface for uploading and downloading files from/to HSink. The default service URL is *http://localhost:8081/hsink*, if not specified.

```bash
usage: java -jar JARFILE [options] [source URI] [target URI]
...file upload:   java -jar client.jar -u ./file [http://localhost:8081/hsink]
...file download: java -jar client.jar -d http://localhost:8081/hsink/.../file ./file
...roundtrip test: java -jar client.jar -t ./file [http://localhost:8081/hsink]
```