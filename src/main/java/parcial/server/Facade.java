package parcial.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class Facade {

    private final int PORT;
    private final int BACKEND_PORT;

    public Facade(int port, int backend_port) {
        this.PORT = port;
        this.BACKEND_PORT = backend_port;
    }

    public void start() throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(this.PORT);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        System.out.println("Facade corriendo en http://localhost:" + this.PORT);
        Socket clientSocket = null;

        while (true) {
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                break;
            }
            PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            String request = "";

            while ((inputLine = in.readLine()) != null) {
                request += inputLine + "\n";
                if (!in.ready()) {
                    break;
                }
            }

            String firstLine = request.split("\n")[0];
            String path = firstLine.split(" ")[1];

            if (path.equals("/")) {
                byte[] content = Files.readAllBytes(FileSystems.getDefault().getPath("src/main/java/res/index.html"));
                String outputLine = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/html\r\n"
                        + "\r\n"
                        + new String(content);
                
                out.println(outputLine);
                out.close();
                in.close();
                clientSocket.close();
                continue;
            }

            URL obj = new URL("http://localhost:" + BACKEND_PORT + path);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.connect();

            int responseCode = con.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    responseCode >= 200 && responseCode < 300
                            ? con.getInputStream()
                            : con.getErrorStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }

            String outputLine = "HTTP/1.1 " + responseCode + con.getResponseMessage() + "\r\n"
                    + "Content-Type: " + con.getContentType() + "\r\n"
                    + "\r\n"
                    + response;

            reader.close();

            out.println(outputLine);
            out.close();
            in.close();

            clientSocket.close();
        }
        serverSocket.close();
    }
}
