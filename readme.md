# A Simple Web Server in Java

## Overview
This repo contains a simple web server I made with java from scratch without using any external libraries.

**Note -: I developed this in linux OS, so it is recommended to run this in the linux OS also. But if you want to run this in windows,  follow the step 1.1 in Guidelines section.**

## Prerequisites
1. The jar file is created with JDK 11. So to run it, make sure you have the same or higher version of jre installed.
   To check the java version, run the following command in command prompt/terminal.
     - ```console
       java -version.
       ```
2. You should have php-cgi installed in your system. To check it, type the following command.

   - ```console
     php-cgi -v
     ```
**If you don't have any of the above, install them before running the program.**

## Guidelines
Do the following steps to run the program.
1. Clone this repository.
   1. ### Windows
      If you are in **linux** or **macOS**, skip to the step 2.  
      1.    In **PhpInterpreter.java**, in function **createProcess(String command)** change the line 126 as follows.
            ```java
            env.put("SCRIPT_FILENAME", filename);
            ``` 
            ```java
            env.put("SCRIPT_FILENAME", "./"+filename);
            ```
         2. Then open command prompt and go to the directory that contains htdocs, icons, src folders and execute the following commands.
            ```console
            del Webserver.jar
            ```
            +   The above command will delete the existing Webserver.jar file.
            ```console
            javac -d bin src/*.java
            ```
            ```console
            jar cvfm Webserver.jar src/META-INF/MANIFEST.MF -C bin/ .
            ```
            + The above 2 commands will re-create the Webserver.jar file with the updated files.

2. Run the jar file with the following command.
   ```console
   java -jar Webserver.jar
   ```
   **Make sure htdocs, icons and Webserver.jar are in the same folder.**
3. The server is listening on the port 2728. So in the address-bar, enter the following url.
   [localhost:2728](http://localhost:2728)  
   **You can change the port the server is listening by changing the port in the following line in Main.java**  
     ```java
   int port = 2728;
   ```
4. You can edit the html, css, js, php files in the htdocs folder.
5. If you have any other html, css, js, php files, you should add them to the htdocs folder.
6. You can change the icon shown in the tab of your web browser by changing the icon in the icons folder.  
    **The icon with the name favicon.ico will be shown in the browser.**

## Source Code Review
+ I have attached 2 [HttpHandler](https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpHandler.html) objects to the server.
   ```java
   server.createContext("/favicon.ico", new FaviconHandler()); // route all requests to resource /favicon.ico to FaviconHandler.
   server.createContext("/", new MainHttpHandler()); // all other requests are handled by the MainHttpHandler.
    ```
  + For each request to the resource favicon.ico, a new instance of **FaviconHandler** class will be created and the request will be handled by it.
  + For any other request to a source that starts from the root, **MainHttpHandler** class will handle it.
  
+ To interpret the php files, I used the [ProcessBuilder](https://docs.oracle.com/javase/8/docs/api/java/lang/ProcessBuilder.html) class in java.
  ```java
  String command = "php-cgi"; // this is the command this function runs.
  ProcessBuilder processBuilder = new ProcessBuilder(command);    // create a builder that will build (start) the process.
  ```
    + This will make a system call to create a new process according to the given command. In this case it will create a new php-cgi process which is used to interpret php files.
  
+ The user input data we get through the request, either through url query parameters or request body, should be passed to the php interpreter. This is done through the [CGI](https://www.rfc-editor.org/rfc/rfc3875) protocol.
  + First we have to set some environmental variables of the created process according to the CGI protocol,   
    ```java
        Map<String, String> env = processBuilder.environment(); // gets a pointer to the environment attached to this process.

        // set the relevant environment fields to create the process.
        env.put("REQUEST_METHOD", method);
        env.put("GATEWAY_INTERFACE", "CGI/1.1");    // we are using cgi to interpret php files.
        env.put("REDIRECT_STATUS",  "true");
        env.put("SCRIPT_FILENAME", filename);

        if (this.method.equals("POST")) {
            env.put("CONTENT_LENGTH", this.contentLength);  // CONTENT_LENGTH, CONTENT_TYPE should be set only if the request is a POST request.
            env.put("CONTENT_TYPE", contentType);
        }

        else if (this.method.equals("GET")) {
            if (params!=null)
                env.put("QUERY_STRING", params);    // if it is a get request, we need to set QUERY_STRING env.
        }
    ```
  + After that we pass the data to the process through its input stream.
    ```java
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(phpProcess.getOutputStream()));
        try {
            // if there is data,
            if (params!=null){
                writer.write(params);   // send them to the running process.
                writer.flush();
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    ```

### Important
**Whenever you run the program, the executable file should be in the same folder as the htdocs and icons folders.**