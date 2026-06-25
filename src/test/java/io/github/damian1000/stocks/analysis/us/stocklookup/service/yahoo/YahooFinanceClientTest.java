package io.github.damian1000.stocks.analysis.us.stocklookup.service.yahoo;

import com.sun.net.httpserver.HttpServer;
import io.github.damian1000.stocks.exception.DataRetrievalError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives YahooFinanceClient against a loopback server that mimics Yahoo's
 * cookie → crumb → quoteSummary flow, so the crumb caching and 401 refresh-and-retry
 * are covered without hitting Yahoo.
 */
class YahooFinanceClientTest {

    private static final String BODY = "{\"quoteSummary\":{\"result\":[{\"price\":{\"longName\":\"Test Co\"}}],\"error\":null}}";

    private HttpServer server;
    private YahooFinanceClient client;
    private final AtomicInteger crumbRequests = new AtomicInteger();
    private final AtomicInteger quoteRequests = new AtomicInteger();
    private final Queue<Integer> quoteStatuses = new ConcurrentLinkedQueue<>();
    private volatile Integer forcedStatus = null;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/seed", exchange -> {
            exchange.getResponseHeaders().add("Set-Cookie", "A1=token; Path=/");
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.createContext("/v1/test/getcrumb", exchange -> {
            byte[] body = ("crumb-" + crumbRequests.incrementAndGet()).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (var os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.createContext("/v10/finance/quoteSummary/", exchange -> {
            quoteRequests.incrementAndGet();
            int status = forcedStatus != null ? forcedStatus : (quoteStatuses.isEmpty() ? 200 : quoteStatuses.poll());
            byte[] body = (status == 200 ? BODY : "{\"error\":\"" + status + "\"}").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, body.length);
            try (var os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
        String base = "http://127.0.0.1:" + server.getAddress().getPort();
        client = new YahooFinanceClient(base, base + "/seed");
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void fetchesQuoteSummaryAndCachesTheCrumb() throws DataRetrievalError {
        assertTrue(client.fetchQuoteSummary("AAPL").contains("Test Co"));
        client.fetchQuoteSummary("MSFT");

        assertEquals(1, crumbRequests.get(), "crumb should be fetched once and reused");
        assertEquals(2, quoteRequests.get());
    }

    @Test
    void refreshesCrumbAndRetriesOnceOn401() throws DataRetrievalError {
        quoteStatuses.add(401); // first quoteSummary call is rejected, then default 200

        assertTrue(client.fetchQuoteSummary("AAPL").contains("Test Co"));

        assertEquals(2, crumbRequests.get(), "a 401 should trigger one crumb refresh");
        assertEquals(2, quoteRequests.get(), "the request is retried once after refreshing");
    }

    @Test
    void throwsWhenCrumbRejectedTwice() {
        forcedStatus = 401;
        assertThrows(DataRetrievalError.class, () -> client.fetchQuoteSummary("AAPL"));
    }

    @Test
    void throwsOnNonRetryableServerError() {
        forcedStatus = 500;
        assertThrows(DataRetrievalError.class, () -> client.fetchQuoteSummary("AAPL"));
    }
}
