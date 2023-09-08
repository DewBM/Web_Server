import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class FaviconHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        // request to the favicon.ico should be a get request.
        if (httpExchange.getRequestMethod().equals("GET")) {
            createResponse(httpExchange);
        }
    }

    private void createResponse(HttpExchange httpExchange) {
        String root = "icons";  // favicon is located in the "icons" folder.
        OutputStream response = httpExchange.getResponseBody(); // response for the request should be written to this stream.

        File icon = new File(root+httpExchange.getRequestURI().getPath()); // create a new File object from the icon that should bw sent.

        if (icon.exists()) {
            httpExchange.getResponseHeaders().set("Content-Type", "image/x-icon");  // set the content type of the response.

            try {
                httpExchange.sendResponseHeaders(200, icon.length()); // send the response headers.
                Files.copy(icon.toPath(), response);    // directly pass the icon to the response outputstream.
                response.flush(); // flush the data and
                response.close(); // close the stream.
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        else {
            String error_msg = "Warning: Couldn't locate resource icons/favicon.ico";
            System.out.println(error_msg);
            try {
                httpExchange.sendResponseHeaders(404, error_msg.length()); // send the response headers.
                response.write(error_msg.getBytes());   // write the error message to the outputstream.
                response.flush(); // flush the data and
                response.close(); // close the stream.
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
