package io.github.damian1000.stocks.html;

import io.github.damian1000.stocks.exception.DataRetrievalError;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ContentHandlerDecorator;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class HtmlRetriever {

    private static final int TIMEOUT = 1000 * 30; // seconds

    private final PoolingHttpClientConnectionManager connectionManager;

    public HtmlRetriever() {
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(128);
        connectionManager.setDefaultMaxPerRoute(50);
    }

    public HtmlResponse getHtml(String url) throws DataRetrievalError {
        // Create an instance of HttpClient.
        HttpClient client = createHttpClient();
        HttpGet request = new HttpGet(url);
        request.addHeader("user-agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");

        int retryCount = 10;
        for (int i=1; i<=retryCount; i++) {
            try {
                log.trace("Attempt "+i+" to retrieve html from url: "+url);

                HttpResponse response = client.execute(request);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode < HttpStatus.SC_OK || statusCode >= HttpStatus.SC_MULTIPLE_CHOICES) {
                    throw new IOException("Unexpected HTTP status " + statusCode + " for " + url);
                }

                byte[] body;
                try (InputStream content = response.getEntity().getContent()) {
                    body = content.readAllBytes();
                }
                String rawHtml = new String(body, StandardCharsets.UTF_8);

                ContentHandlerDecorator textHandler = new BodyContentHandler(-1);
                Metadata metadata = new Metadata();
                AutoDetectParser parser = new AutoDetectParser();
                ParseContext context = new ParseContext();
                parser.parse(new ByteArrayInputStream(body), textHandler, metadata, context);

                String data = textHandler.toString();
                data = data.replace(String.valueOf((char)160), "");

                HtmlResponse htmlResponse = new HtmlResponse();
                htmlResponse.rawHtml = rawHtml;
                htmlResponse.parsedHtml = data;
                return htmlResponse;
            } catch (IOException | TikaException | SAXException e) {
                if (i == retryCount) {
                    log.error("Giving up while attempting retrieve data from url: "+url +" message: "+e.getMessage(), e);
                    throw new DataRetrievalError(e);
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                    throw new DataRetrievalError(e1);
                }
            } finally {
                request.releaseConnection();
            }
        }
        throw new DataRetrievalError(new IOException("Unable to retrieve html from " + url));
    }

    private HttpClient createHttpClient() {
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder = requestBuilder.setConnectTimeout(TIMEOUT);
        requestBuilder = requestBuilder.setSocketTimeout(TIMEOUT);

        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setDefaultRequestConfig(requestBuilder.build());
        builder.setConnectionManager(connectionManager);
        return builder.build();
    }

}
