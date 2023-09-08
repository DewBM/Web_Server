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
    public void handle(HttpExchange httpExchange) throws IOException {

        // this server can handle only "GET" and "POST" requests.

        String method = httpExchange.getRequestMethod(); // the request type. "GET" or "POST" for this server.

        // if the method is "GET"
        if (method.equals("GET"))
            handleGetRequest(httpExchange);

        // the request method is "POST"
        else if (method.equals("POST"))
            handlePostRequest(httpExchange);
    }

    // as the name suggests, this method handles the get requests.
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
                System.out.println("Error: Couldn't locate resource " + root+requestPath);
                String error_msg = String.format("<html><body><h2>404 Not Found : Couldn't locate resource %s </h2></body></html>", root+requestPath);
                createErrorResponse(httpExchange, error_msg);
            }

        }
    }

    // this method handles the post requests.
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

    private void createResponse(HttpExchange httpExchange, String filename, boolean is_php) {
        OutputStream response = httpExchange.getResponseBody();

        if (is_php) {
            PhpInterpreter phpInterpreter = new PhpInterpreter(httpExchange, filename);
            String result = phpInterpreter.interpret();

            try {
                httpExchange.sendResponseHeaders(200, result.length());
                response.write(result.getBytes());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        else {
            File fileObj = new File(filename);
            Path path = fileObj.toPath();

            try {
                httpExchange.sendResponseHeaders(200, fileObj.length());
                Files.copy(path, response);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        try {
            response.flush();
            response.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    private void createErrorResponse(HttpExchange httpExchange, String error_msg) {
        OutputStream response = httpExchange.getResponseBody();

        try {
            httpExchange.sendResponseHeaders(404, error_msg.length());
            response.write(error_msg.getBytes());
            response.flush();
            response.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

}
