import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.util.Map;

public class PhpInterpreter {
    private final String filename;      // requested filename with the path from the htdocs folder.
    private String params;              // the data user submitted with the request.
                                        // can be null if the request didn't contain query parameters or request body.
    private final String method;        // method of the request, "GET" or "POST".
    private final String contentType;
    private final String contentLength;

    /**
     * <b>Constructor</b>
     * Initialize the private fields, <b>filename, method, contentType, contentLength</b>
     * @param httpExchange  The HttpExchange instance bounded to this request.
     * @param filename      The filename with the relative path of the requested php file.
     */
    public PhpInterpreter(HttpExchange httpExchange, String filename) {
        // initialize the private fields.
        this.filename = filename;
        this.method = httpExchange.getRequestMethod();
        this.contentType = httpExchange.getRequestHeaders().getFirst("Content-Type");
        this.contentLength = httpExchange.getRequestHeaders().getFirst("Content-Length");

        try {
            // this is a POST request.
            if (method.equals("POST"))
                this.params = getPayload(httpExchange.getRequestBody());    // get the user input for this request.
            else if (method.equals("GET")) {
                this.params = httpExchange.getRequestURI().getQuery();      // get the url query parameters that came with this request.
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * <p>Parses the user's request body for POST requests and returns it as a string that can be passed to php-cgi process</p>
     * @param requestBody   The body of the request that contains input data.
     * @return              The user input as a string
     * @throws IOException  reading from the stream may throw an IOException
     */
    private String getPayload(InputStream requestBody) throws IOException {
        StringBuilder params = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));

        // until the requestBody (stream) is not empty, read from line by line and append to params.
        while (reader.ready()) {
            params.append(reader.readLine());
        }

        return params.toString();   // build the string from the string builder and return it.
    }


    /**
     * <p>This function runs the php-cgi interpreter from the system by passing the relevant data and php files and returns the output.</p>
     * @return  The output after interpreting the php file.
     */
    public String interpret() {
        String command = "php-cgi"; // this is the command this function runs.
        Process phpProcess;         // stores the process created from the above command.
        StringBuilder result = new StringBuilder(); // stores the output that will be returned.

        phpProcess = createProcess(command);    // create the process for the given command.

        // create a writer that can be used to send data to the created process.
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

        try {
            // wait for the process to finish.
            // the process returns 0 if it completed successfully.
            if (phpProcess.waitFor() == 0) {
                // we have to skip some part of the output that will be automatically prepended by CGI.
                boolean startAppending = false; // whether the given line should be added to the response or not.
                BufferedReader reader = new BufferedReader(new InputStreamReader(phpProcess.getInputStream())); // reader to ream from the process/
                for (int i=0; i<3 || !reader.ready(); i++)
                    reader.readLine();

                while (reader.ready()) {
                    result.append(reader.readLine());
                }
            // if the exit code is not 0, there was an error.
            } else {
                result.append("Error: Something went wrong when interpreting the php file."); // send the error message as the response.
            }
        } catch (InterruptedException | IOException e) {
            System.out.println(e.getMessage());
        }

        return result.toString();   // return the output of the php files or error message.
    }


    /**
     * <p>This function creates a system process according to given command.</p>
     * @param command   The command that will be used to create the process.
     * @return          The process that is created.
     */
    private Process createProcess(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);    // create a builder that will build (start) the process.

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

        Process phpProcess = null;
        try {
            phpProcess = processBuilder.start();    // finally create the process.
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return phpProcess; // returns the created process.
    }
}
