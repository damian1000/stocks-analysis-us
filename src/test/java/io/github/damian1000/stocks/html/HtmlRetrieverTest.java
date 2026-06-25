package io.github.damian1000.stocks.html;

import com.sun.net.httpserver.HttpServer;
import io.github.damian1000.stocks.exception.DataRetrievalError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives HtmlRetriever against a loopback HttpServer. Retries/backoff are
 * configured to small values so the failure path is exercised without real delays.
 */
class HtmlRetrieverTest {

    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/ok", exchange -> {
            byte[] body = "<html><body>Hello Zacks</body></html>".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (var os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.createContext("/boom", exchange -> {
            byte[] body = "error".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, body.length);
            try (var os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void returnsRawAndParsedHtmlOnSuccess() throws DataRetrievalError {
        HtmlResponse response = new HtmlRetriever(5000, 1, 0).getHtml(baseUrl + "/ok");
        assertTrue(response.rawHtml.contains("Hello Zacks"), "raw HTML preserved");
        assertTrue(response.parsedHtml.contains("Hello Zacks"), "Tika-extracted text present");
    }

    @Test
    void retriesThenThrowsDataRetrievalErrorOnPersistentFailure() {
        HtmlRetriever retriever = new HtmlRetriever(5000, 2, 0);
        assertThrows(DataRetrievalError.class, () -> retriever.getHtml(baseUrl + "/boom"));
    }
}
