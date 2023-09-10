import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainHttpHandler implements HttpHandler {
    private final String root = "htdocs"; // this is the folder where all the resource files are located.
    @Override
    public void handle(HttpExchange httpExchange) {

        // this server can handle only "GET" and "POST" requests.

        String method = httpExchange.getRequestMethod(); // the request type. "GET" or "POST" for this server.

        // if the method is "GET"
        if (method.equals("GET"))
            handleGetRequest(httpExchange);

        // the request method is "POST"
        else if (method.equals("POST"))
            handlePostRequest(httpExchange);
    }

    /**
     * This function is invoked when a GET request is received.<br>
     * Handles the GET request and send the appropriate response.
     * @param httpExchange : The HttpExchange bounded to this request.
     */
    private void handleGetRequest(HttpExchange httpExchange) {
        String requestPath = httpExchange.getRequestURI().getPath();    // get the path(file) client requested.

        // request is to the root.
        // should serve the index file.
        if (requestPath.equals("/")) {
            // the file index.html exists in the htdocs folder.
            if (Files.exists(Paths.get(root+"/index.html"))) {
                // serve the index.html file.
                createResponse(httpExchange, root+"/index.html", false);
            }
            // cannot find index.html but index.php file exists.
            else if (Files.exists(Paths.get(root+"/index.php"))) {
                // interpret the php file and serve the output.
                createResponse(httpExchange, root+"/index.php", true);
            }
            // cannot find a valid index file
            else {
                // sends an error message to the client.
                System.out.println("Error: Couldn't locate an index file.");
                String error_msg = "<html><body><h2>404 Not Found : Couldn't locate an index file</h2></body></html>";

                createErrorResponse(httpExchange, error_msg);
            }
        }

        // the request is for a specific resource.
        else {
            // first check whether the requested resource exists or not.

            // file exists
            if (Files.exists(Paths.get(root+requestPath))){
                // create the response with the requested resource.
                createResponse(httpExchange, root+requestPath, requestPath.endsWith(".php"));
            }
            // requested resource doesn't exists
            else {
                // send the error message as the response.
                System.out.println("Error: Couldn't locate resource " + root+requestPath);
                String error_msg = String.format("<html><body><h2>404 Not Found : Couldn't locate resource %s </h2></body></html>", root+requestPath);
                createErrorResponse(httpExchange, error_msg);
            }

        }
    }


    /**
     * This function is invoked when the request is a POST request.<br>
     * Handles the POST request and sends the appropriate response.
     * @param httpExchange : The HttpExchange instance bounded to this request.
     */
    private void handlePostRequest(HttpExchange httpExchange) {
        String requestPath = httpExchange.getRequestURI().getPath();

        // post request is never for the index files.

        // the requested file (.php) exists.
        if (Files.exists(Paths.get(root+requestPath))) {
            // interpret the php file and serve.
            createResponse(httpExchange, root+requestPath, true);
        }
        // the requested resource doesn't exist.
        else {
            // send an error message as the response.
            System.out.println("Error: Couldn't locate an index file.");
            String error_msg = "<html><body><h2>404 Not Found : Couldn't locate an index file</h2></body></html>";

            createErrorResponse(httpExchange, error_msg);
        }
    }


    /**
     * <b>This is called only if the requested resource is found.</b>
     * <p> Locate the requested resource within htdocs folder, interpret it if it's a php file
     * and send the output of the php file or content of the file as the response.</p>
     * @param httpExchange  : The HttpExchange instance bounded to this request.
     * @param filename      : filename of the resource to serve to the client. (within htdocs folder).
     * @param is_php        : whether the requested resource is a php file or not.
     */
    private void createResponse(HttpExchange httpExchange, String filename, boolean is_php) {
        OutputStream response = httpExchange.getResponseBody(); // this is the outputstream that we should write the response to.

        // the requested resource is a php file.
        if (is_php) {
            PhpInterpreter phpInterpreter = new PhpInterpreter(httpExchange, filename); // initialize the php interpreter according to the request.
            String result = phpInterpreter.interpret(); // interpret the requested php file and get the output.

            try {
                httpExchange.sendResponseHeaders(200, result.length()); // send the ok code (200) and response length as headers.
                response.write(result.getBytes());  // write the output of the php file to response.
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        // requested resource is not a php file.
        else {
            File fileObj = new File(filename);  // open the requested resource as a File object.
            Path path = fileObj.toPath();   // get the path of the opened file.

            try {
                httpExchange.sendResponseHeaders(200, fileObj.length()); // send the ok code(200) and response length as headers.
                Files.copy(path, response); // copy the whole file to the response directly.
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        try {
            response.flush();   // flush the data through the outputstream.
            response.close();   // and close the file.
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * <b>This function is called if the requested resource cannot be located.</b>
     * <p>Send the error Not found response to the client</p>
     * @param httpExchange  The HttpExchange instance bounded to this request.
     * @param error_msg     The html formatted error message that should be sent as the response.
     */
     private void createErrorResponse(HttpExchange httpExchange, String error_msg) {
        OutputStream response = httpExchange.getResponseBody(); // this is the outputstream the response should be written to.

        try {
            httpExchange.sendResponseHeaders(404, error_msg.length());  // send the Not found (404) code and response length as headers.
            response.write(error_msg.getBytes());   // write the error message to the response.
            response.flush();   // flush the outputstream,
            response.close();   // close the file.
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

}
