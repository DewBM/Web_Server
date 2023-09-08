import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {
        // Initialize the ip address and port for the server to listen to.
        String address = "localhost";
        int port = 2728;

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(address, port), 10); // initialize the server with the given ip address and port
            server.createContext("/favicon.ico", new FaviconHandler()); // route all requests to resource /favicon.ico to FaviconHandler.
            server.createContext("/", new MainHttpHandler()); // all other requests are handled by the MainHttpHandler.
            server.start(); // start the server.
            
            System.out.println("Server is listening on " + address + ":"+port);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
}
