package parcial.server;

import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class Backend {
    private final int PORT;
    private final HashMap<String, String> database;

    public Backend(int port) {
        this.PORT = port;
        this.database = new HashMap<>();
    }

    private Map<String, String> getParams(String path) {
        HashMap<String, String> result = new HashMap<>();

        if (path == null) {
            return result;
        }

        String[] parts = path.split("\\?");

        if (parts.length != 2) {
            return result;
        }

        String params = parts[1];
        String[] pairs = params.split("&");

        for (String pair : pairs) {
            String[] pairParts = pair.split("=");

            if (pairParts.length != 2) {
                continue;
            }

            result.put(pairParts[0], pairParts[1]);
        }

        return result;
    }

    private Map<String, String> getRequestLine(String request) {

        HashMap<String, String> result = new HashMap<>();
        String firstLine = request.split("\n")[0];
        String[] parts = firstLine.split(" ");

        if (parts.length != 3) {
            System.out.println("Request line invalido: " + firstLine);
        }

        result.put("method", parts[0]);
        result.put("path", parts[1]);
        result.put("protocol", parts[2]);

        return result;
    }

    private String getBasicResponse(String value) {
        String outputLine = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "<meta charset=\"UTF-8\">\n"
                + "<title>Title of the document</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "<h1>" + value + "</h1>\n"
                + "</body>\n"
                + "</html>\n";

        return outputLine;
    }

    private String getBadRequestResponse(String message) {
        String outputLine = "HTTP/1.1 400 Bad Request\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "<meta charset=\"UTF-8\">\n"
                + "<title>Title of the document</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "<h1>" + message + "</h1>\n"
                + "</body>\n"
                + "</html>\n";

        return outputLine;
    }

    public String getNotFoundResponse(String key) {
        String outputLine = "HTTP/1.1 404 OK\r\n"
                + "Content-Type: application/json; charset=utf-8\r\n"
                + "\r\n"
                + "{ \"key\": \"" + key + "\", \"error\": \"key_not_found\" }";

        return outputLine;
    }

    private void handleGetKeyValue(PrintWriter output, Map<String, String> params) {
        String key = params.get("key");

        if (key == null) {
            String outputLine = getBadRequestResponse("No se proporciono el param \"key\"");
            output.println(outputLine);
            return;
        }

        String value = database.get(key);

        if (value == null) {
            String outputLine = getNotFoundResponse(key);
            output.println(outputLine);
            return;
        }

        String outputLine = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: application/json; charset=utf-8\r\n"
                + "\r\n"
                + "{ \"key\": \"" + key + "\", \"value\": \"" + value + "\" }";
        output.println(outputLine);
    }

    private void handleSetkeyValue(PrintWriter output, Map<String, String> params) {
        String key = params.get("key");

        if (key == null) {
            String outputLine = getBadRequestResponse("No se proporciono el param \"key\"");
            output.println(outputLine);
            return;
        }

        String value = params.get("value");

        if (value == null) {
            String outputLine = getBadRequestResponse("No se proporciono el param \"value\"");
            output.println(outputLine);
            return;
        }

        database.put(key, value);
        String outputLine = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: application/json; charset=utf-8\r\n"
                + "\r\n"
                + "{ \"key\": \"" + key + "\", \"value\": \"" + value + "\", \"status\": \"created\" }";
        output.println(outputLine);
    }

    public void handleDefaultRoute(PrintWriter output) {
        output.println(getBasicResponse(
                "No ingresaste una ruta valida entre /setkv?key=<key>&value=<value> o /getkv?key=<key>"));
    }

    public void start() throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(this.PORT);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        System.out.println("Servidor corriendo en http://localhost:" + this.PORT);
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

            Map<String, String> requestLineParts = getRequestLine(request);
            String path = requestLineParts.get("path");
            Map<String, String> params = getParams(path);

            if (path.startsWith("/getkv")) {
                handleGetKeyValue(out, params);
            } else if (path.startsWith("/setkv")) {
                handleSetkeyValue(out, params);
            } else {
                handleDefaultRoute(out);
            }

            out.close();
            in.close();

            clientSocket.close();
        }
        serverSocket.close();
    }
}
