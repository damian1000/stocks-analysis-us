package io.github.damian1000.stocks.analysis.us.stocklookup.service.yahoo;

import io.github.damian1000.stocks.exception.DataRetrievalError;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Talks to Yahoo Finance's {@code quoteSummary} JSON API. Yahoo gates the API
 * behind a cookie + "crumb" pair: you fetch a cookie, exchange it for a crumb,
 * then pass the crumb on each request. Crumbs expire, so a 401 triggers a single
 * refresh-and-retry. This replaces scraping the (now removed) HTML analysis page.
 */
@Component
@Slf4j
public class YahooFinanceClient {

    private static final String MODULES = "price,summaryDetail,financialData,earningsTrend,earningsHistory";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36";

    private final String apiBaseUrl;
    private final String cookieSeedUrl;
    private final CookieStore cookieStore = new BasicCookieStore();
    private final CloseableHttpClient httpClient;
    private volatile String crumb;

    public YahooFinanceClient() {
        this("https://query2.finance.yahoo.com", "https://fc.yahoo.com/");
    }

    // Visible for testing: lets a test point the client at a loopback server.
    YahooFinanceClient(String apiBaseUrl, String cookieSeedUrl) {
        this.apiBaseUrl = apiBaseUrl;
        this.cookieSeedUrl = cookieSeedUrl;
        this.httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
    }

    /** Returns the raw quoteSummary JSON for a symbol, refreshing the crumb once on a 401. */
    public String fetchQuoteSummary(String symbol) throws DataRetrievalError {
        try {
            return requestQuoteSummary(symbol);
        } catch (CrumbRejectedException e) {
            log.info("Yahoo rejected the crumb for {}; refreshing and retrying once", symbol);
            crumb = null;
            try {
                return requestQuoteSummary(symbol);
            } catch (CrumbRejectedException retryFailure) {
                throw new DataRetrievalError("Yahoo rejected the crumb twice for " + symbol);
            }
        }
    }

    private String requestQuoteSummary(String symbol) throws DataRetrievalError, CrumbRejectedException {
        String currentCrumb = ensureCrumb();
        String url = apiBaseUrl + "/v10/finance/quoteSummary/" + symbol
                + "?modules=" + MODULES
                + "&crumb=" + URLEncoder.encode(currentCrumb, StandardCharsets.UTF_8);
        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int status = response.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            if (status == 401) {
                throw new CrumbRejectedException();
            }
            if (status != 200) {
                throw new DataRetrievalError("Yahoo quoteSummary returned HTTP " + status + " for " + symbol);
            }
            return body;
        } catch (IOException e) {
            throw new DataRetrievalError(e);
        }
    }

    private String ensureCrumb() throws DataRetrievalError {
        String current = crumb;
        if (current == null || current.isBlank()) {
            current = fetchCrumb();
            crumb = current;
        }
        return current;
    }

    private String fetchCrumb() throws DataRetrievalError {
        seedCookies();
        HttpGet request = new HttpGet(apiBaseUrl + "/v1/test/getcrumb");
        request.addHeader("User-Agent", USER_AGENT);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String value = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            if (value == null || value.isBlank()) {
                throw new DataRetrievalError("Yahoo returned an empty crumb");
            }
            return value.trim();
        } catch (IOException e) {
            throw new DataRetrievalError(e);
        }
    }

    private void seedCookies() {
        HttpGet request = new HttpGet(cookieSeedUrl);
        request.addHeader("User-Agent", USER_AGENT);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            // The seed endpoint often returns a non-200 but still sets the cookie we need.
            EntityUtils.consumeQuietly(response.getEntity());
        } catch (IOException e) {
            log.warn("Yahoo cookie seed request failed ({}); attempting crumb fetch anyway", e.getMessage());
        }
    }

    /** Signals that Yahoo rejected the crumb (HTTP 401) so the caller can refresh once. */
    private static final class CrumbRejectedException extends Exception {
    }
}
