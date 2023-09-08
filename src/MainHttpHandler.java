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

            }
        }

        // the request is for a specific resource.
        else {
            // requested resource is a php file.
            if (requestPath.endsWith(".php")) {
                // interpret the php file and then serve it.
                createResponse(httpExchange, root+requestPath, true);
            }
            // request any other file (html, js, css)
            else {
                // serve the requested file.
                createResponse(httpExchange, root+requestPath, false);
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

}
