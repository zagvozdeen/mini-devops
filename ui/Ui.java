import com.sun.net.httpserver.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class Ui {
    static final String FORM = """
        <!doctype html><html lang="ru"><meta charset="utf-8"><title>Вход</title>
        <main><h1>Вход</h1><form method="post" action="/login">
        <p><label>Логин <input name="login" autocomplete="username" required></label></p>
        <p><label>Пароль <input name="password" type="password" autocomplete="current-password" required></label></p>
        <button>Войти</button></form></main></html>
        """;
    static final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

    public static void main(String[] args) throws Exception {
        var server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String page = "Страница не найдена";
            int status = 404;
            if (path.equals("/") && exchange.getRequestMethod().equals("GET")) {
                page = FORM;
                status = 200;
            } else if (path.equals("/login") && exchange.getRequestMethod().equals("POST")) {
                var body = exchange.getRequestBody().readNBytes(4097);
                status = 413;
                page = "Слишком большой запрос";
                if (body.length <= 4096) {
                    try {
                        var request = HttpRequest.newBuilder(URI.create("http://api:8080/login"))
                            .timeout(Duration.ofSeconds(5)).header("Content-Type", "application/x-www-form-urlencoded")
                            .POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();
                        status = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
                    } catch (Exception e) { status = 503; }
                    page = switch (status) {
                        case 200 -> "Вход выполнен";
                        case 401 -> "Неверный логин или пароль";
                        default -> "Ошибка проверки учётных данных";
                    };
                }
            }
            var bytes = page.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(status, bytes.length);
            try (var out = exchange.getResponseBody()) { out.write(bytes); }
        });
        server.start();
    }
}
