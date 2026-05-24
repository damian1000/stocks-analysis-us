package com.dfh.fx;

import com.dfh.exception.DataRetrievalError;
import com.dfh.html.HtmlParser;
import com.dfh.html.HtmlResponse;
import com.dfh.html.HtmlRetriever;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CurrencyConverterTest {

    private static final String PROVIDER_URL = "https://example.test/fx";
    private static final String API_KEY = "test-key";

    @Test
    void convertsUsingFxProviderResponse() throws DataRetrievalError {
        HtmlRetriever retriever = mock(HtmlRetriever.class);
        HtmlParser parser = new HtmlParser();
        HtmlResponse response = new HtmlResponse();
        response.parsedHtml = "{\"rates\":{\"GBP\":0.80,\"USD\":1.05}}";
        when(retriever.getHtml(contains("symbols=GBP,USD"))).thenReturn(response);

        CurrencyConverter converter = new CurrencyConverter(retriever, parser, PROVIDER_URL, API_KEY);

        double rate = converter.convert("GBP", "USD");

        assertThat(rate, is(closeTo(1.05 / 0.80, 0.0001)));
    }

    @Test
    void returnsZeroWhenApiKeyMissing() throws DataRetrievalError {
        HtmlRetriever retriever = mock(HtmlRetriever.class);
        CurrencyConverter converter = new CurrencyConverter(retriever, new HtmlParser(), PROVIDER_URL, "");

        double rate = converter.convert("GBP", "USD");

        assertThat(rate, is(0.0));
        verify(retriever, never()).getHtml(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void returnsZeroForInvalidCurrencyCode() throws DataRetrievalError {
        HtmlRetriever retriever = mock(HtmlRetriever.class);
        CurrencyConverter converter = new CurrencyConverter(retriever, new HtmlParser(), PROVIDER_URL, API_KEY);

        assertThat(converter.convert("GB", "USD"), is(0.0));
        assertThat(converter.convert("", "USD"), is(0.0));
        verify(retriever, never()).getHtml(org.mockito.ArgumentMatchers.anyString());
    }
}
