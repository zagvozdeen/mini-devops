import com.sun.net.httpserver.*;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;

public class Api {
    static int login(HttpExchange exchange) throws Exception {
        if (!exchange.getRequestURI().getPath().equals("/login")) {
            return 404;
        }
        if (!exchange.getRequestMethod().equals("POST")) {
            return 405;
        }

        var body = exchange.getRequestBody().readNBytes(4097);
        if (body.length > 4096) {
            return 413;
        }

        var fields = new HashMap<String, String>();
        for (String pair : new String(body, StandardCharsets.UTF_8).split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                fields.put(parts[0], URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
            }
        }

        try (var db = DriverManager.getConnection(
                "jdbc:postgresql://db:5432/login?connectTimeout=3&socketTimeout=3",
                "login",
                System.getenv("DB_PASSWORD"));
             var query = db.prepareStatement("SELECT 1 FROM users WHERE login = ? AND hash = crypt(?, hash)")) {
            query.setString(1, fields.get("login"));
            query.setString(2, fields.get("password"));

            try (var result = query.executeQuery()) {
                return result.next() ? 200 : 401;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        var server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/login", exchange -> {
            int status;
            try {
                status = login(exchange);
            } catch (IllegalArgumentException e) {
                status = 400;
            } catch (Exception e) {
                System.err.println(e.getClass().getSimpleName());
                status = 503;
            }

            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        });
        server.start();
    }
}
