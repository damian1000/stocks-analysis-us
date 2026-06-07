package io.github.damian1000.stocks.fx;

import io.github.damian1000.stocks.exception.DataRetrievalError;
import io.github.damian1000.stocks.html.HtmlParser;
import io.github.damian1000.stocks.html.HtmlRetriever;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CurrencyConverter {

    private final HtmlRetriever htmlRetriever;
    private final HtmlParser htmlParser;
    private final String providerUrl;
    private final String apiKey;

    private static final Map<String, Double> rates = new HashMap<>();

    public CurrencyConverter(HtmlRetriever htmlRetriever,
                             HtmlParser htmlParser,
                             @Value("${stocks.analysis.us.fx.provider-url:https://data.fixer.io/api/latest}") String providerUrl,
                             @Value("${stocks.analysis.us.fx.api-key:}") String apiKey) {
        this.htmlRetriever = htmlRetriever;
        this.htmlParser = htmlParser;
        this.providerUrl = providerUrl;
        this.apiKey = apiKey;
    }

    public double convert(String from, String to) throws DataRetrievalError {
        if (StringUtils.isEmpty(from) || StringUtils.isEmpty(to) || from.length() != 3 || to.length() != 3) {
            log.error("Invalid currency lookup from={} to={}", from, to);
            return 0.0;
        }
        if (StringUtils.isEmpty(apiKey)) {
            log.warn("FX_API_KEY not configured; skipping conversion {} -> {}", from, to);
            return 0.0;
        }
        Double cached = rates.get(from + to);
        if (cached != null) {
            return cached;
        }
        String url = String.format("%s?access_key=%s&symbols=%s,%s", providerUrl, apiKey, from, to);
        String html = htmlRetriever.getHtml(url).parsedHtml;

        String fromRateRaw = htmlParser.extractText(html, from + "\":", ",");
        String toRateRaw = htmlParser.extractText(html, to + "\":", "}");

        double fromRate = Double.parseDouble(fromRateRaw);
        double toRate = Double.parseDouble(toRateRaw);
        double conversion = toRate / fromRate;

        log.info("FX {} -> {} = {}", from, to, conversion);
        rates.put(from + to, conversion);
        return conversion;
    }
}
