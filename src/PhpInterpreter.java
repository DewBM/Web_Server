import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.util.Map;

public class PhpInterpreter {
    private final String filename;
    private String params;
    private final String method;
    private final String contentType;
    private final String contentLength;

    public PhpInterpreter(HttpExchange httpExchange, String filename) {
        this.filename = filename;
        this.method = httpExchange.getRequestMethod();
        this.contentType = httpExchange.getRequestHeaders().getFirst("Content-Type");
        this.contentLength = httpExchange.getRequestHeaders().getFirst("Content-Length");

        try {
            if (method.equals("POST"))
                this.params = getPayload(httpExchange.getRequestBody());
            else if (method.equals("GET")) {
                this.params = httpExchange.getRequestURI().getQuery();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    private String getPayload(InputStream requestBody) throws IOException {
        StringBuilder params = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));

        while (reader.ready()) {
            params.append(reader.readLine());
        }

        return params.toString();
    }


    public String interpret() {
        String command = "php-cgi";
        Process phpProcess;
        StringBuilder result = new StringBuilder();

        phpProcess = createProcess(command);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(phpProcess.getOutputStream()));
        try {
            if (params!=null){
                writer.write(params);
                writer.flush();
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        try {
            if (phpProcess.waitFor() == 0) {
                boolean startAppending = false;
                BufferedReader reader = new BufferedReader(new InputStreamReader(phpProcess.getInputStream()));
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (line.equals("<!DOCTYPE html>"))
                        startAppending = true;
                    if (startAppending)
                        result.append(line);

                }
            } else {
                result.append("Error!");
            }
        } catch (InterruptedException | IOException e) {
            System.out.println(e.getMessage());
        }

        return result.toString();
    }

    private Process createProcess(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        Map<String, String> env = processBuilder.environment();
        env.put("REQUEST_METHOD", method);
        env.put("GATEWAY_INTERFACE", "CGI/1.1");
        env.put("REDIRECT_STATUS",  "true");
        env.put("SCRIPT_FILENAME", filename);

        if (this.method.equals("POST")) {
            env.put("CONTENT_LENGTH", this.contentLength);
            env.put("CONTENT_TYPE", contentType);
        }

        else if (this.method.equals("GET")) {
            if (params!=null)
                env.put("QUERY_STRING", params);
        }

        Process phpProcess = null;
        try {
            phpProcess = processBuilder.start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return phpProcess;
    }
}
